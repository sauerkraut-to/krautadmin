/*
 * Copyright (C) 2015 sauerkraut.to <gutsverwalter@sauerkraut.to>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package to.sauerkraut.krautadmin.job;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiestacabin.dropwizard.quartz.Scheduled;
import com.google.common.io.Files;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;
import to.sauerkraut.krautadmin.core.Toolkit;

import javax.validation.*;
import javax.validation.constraints.Min;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@SuppressWarnings("checkstyle:classfanoutcomplexity")
@DisallowConcurrentExecution
@Scheduled(interval = 10, unit = TimeUnit.MINUTES)
public class UpdateApplicationJob implements org.quartz.Job {
    public static final String LATEST_RELEASE_METADATA_URL =
            "https://api.github.com/repos/sauerkraut-to/krautadmin/releases/latest";
    private static final String UPDATE_FILE_PREFIX = "krautadmin-";
    
    @Log
    private static Logger logger;
    @Inject
    private KrautAdminConfiguration configuration;
    @Inject
    private ValidatorFactory validatorFactory;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final String jarName = configuration.getJarName();

        /* TODO: check last loaded release as of database -> if not pending and .jar exists and .jar is valid
         * and release > currentRelease then skip this update check
         * (user has not yet switched to new .jar even though it exists and is valid)
         */
        if (configuration.isUpdatePending()) {
            logger.info("skipping application auto-update due to a pending update");
        } else if (jarName == null) {
            logger.info("skipping application auto-update due to the app not being bundled as a .jar");
        } else {
            final String jarPath = configuration.getApplicationLocation().concat(File.separator).concat(jarName);
            final String applicationLocation = configuration.getApplicationLocation();
            final String currentRelease = configuration.getRelease();
            logger.info("currently running application release: {}", currentRelease);

            try {
                loadAndInstallApplicationUpdate(jarPath, currentRelease, applicationLocation);
            } catch (MalformedURLException e) {
                logger.error("url to application repo seems to be malformed", e);
            } catch (JsonMappingException e) {
                logger.error("unable to map GitHub response for latest-release-request to GithubRelease.class", e);
            } catch (JsonParseException e) {
                logger.error("unable to parse GitHub response for latest-release-request", e);
            } catch (IOException e) {
                logger.error("unable to fetch GitHub response for latest-release-request", e);
            } catch (AssertionError e) {
                logger.error("received incomplete response data from GitHub for latest-release-request", e);
            } catch (Exception e) {
                logger.error("application update failed unexpectedly", e);
            }
        }
    }

    private void loadAndInstallApplicationUpdate(final String jarPath, final String currentRelease,
                                                 final String applicationLocation) throws Exception {
        final URL applicationRepoUrl =
                new URL(LATEST_RELEASE_METADATA_URL);
        final ObjectMapper responseMapper = new ObjectMapper();
        final GitHubRelease latestRelease = responseMapper.readValue(applicationRepoUrl, GitHubRelease.class);
        final Validator validator = validatorFactory.getValidator();
        final Set<ConstraintViolation<GitHubRelease>> violations = validator.validate(latestRelease);
        assert violations.size() > 1;
        logger.info("latest application release: {}", latestRelease.getTagName());

        if (currentRelease.compareToIgnoreCase(latestRelease.getTagName()) < 0) {
            final String applicationUpdateJarPath = applicationLocation.concat(File.separator).
                    concat(UPDATE_FILE_PREFIX).concat(latestRelease.getTagName()).concat(".jar");
            ByteArrayOutputStream applicationUpdateOutputStream = null;
            int expectedApplicationUpdateSize = -1;
            boolean updateSuccess = false;
            logger.info("application needs to be updated, attempting download ...");
            for (GitHubReleaseAsset releaseAsset : latestRelease.getAssets()) {
                if ("application/java-archive".equalsIgnoreCase(releaseAsset.getContentType())) {
                    expectedApplicationUpdateSize = releaseAsset.getContentSize();
                    try {
                        applicationUpdateOutputStream = readApplicationUpdate(releaseAsset);
                    } catch (URISyntaxException e) {
                        logger.error("url to updated application .jar seems to be malformed", e);
                    } catch (Exception e) {
                        logger.error("could not download updated application .jar", e);
                    }
                    break;
                }
            }

            final byte[] applicationUpdateBytes = applicationUpdateOutputStream == null
                    ? new byte[]{} : applicationUpdateOutputStream.toByteArray();

            logger.info("validating downloaded application update integrity ...");
            if (validateApplicationUpdateSize(expectedApplicationUpdateSize, applicationUpdateBytes.length)) {
                updateSuccess = writeApplicationUpdate(applicationUpdateJarPath, applicationUpdateBytes);
            }

            if (updateSuccess) {
                logger.warn("successfully updated the application - it needs to be restarted!");
                configuration.setUpdatePending(true);
                saveUpdateSuccessNote(latestRelease.getTagName());
                attemptLaunchScriptModification(jarPath, applicationUpdateJarPath);
                attemptApplicationRestart();
            } else {
                logger.warn("aborting application update due to errors, trying again later");
            }
        }
    }

    private void saveUpdateSuccessNote(final String releaseUpdatedTo) {
        //TODO: implement & show notice of pending updates to users in acp
    }

    private synchronized void attemptLaunchScriptModification(final String oldJarPath, final String newJarPath) {
        logger.info("attempting to modify the launch script for the application (if exists, might fail) - "
                + "on failure please modify it manually to point to the latest application .jar");

        //TODO: implement for Debian-based Linux distributions
    }

    private static synchronized void attemptApplicationRestart() {
        logger.info("attempting to restart the application (might fail) - "
                + "on failure please restart it manually");

        try {
            Toolkit.restartApplication();
        } catch (Exception e) {
            logger.error("automatic application restart failed", e);
        }
    }

    private boolean validateApplicationUpdateSize(final int expectedSize, final int actualSize) {
        if (actualSize < 1) {
            logger.error("downloaded application update was empty");
        } else if (actualSize != expectedSize) {
            logger.error("downloaded application update size ({}) was different from expected size ({})",
                    actualSize, expectedSize);
        } else {
            return true;
        }

        return false;
    }

    private static synchronized boolean writeApplicationUpdate(final String applicationUpdateJarPath,
                                                               final byte[] updateContent) {
        try {
            final File updateJarFile = new File(applicationUpdateJarPath);
            Files.write(updateContent, updateJarFile);
            Toolkit.validateZipFile(updateJarFile);
            logger.info("downloaded application update integrity checks passed, "
                    + "trying to install the update now ...");
            return true;
        } catch (ZipException e) {
            logger.error("downloaded application update zip archive integrity check failed", e);
        } catch (IOException e) {
            logger.error("error writing downloaded application update or overriding existing .jar", e);
        } catch (Exception e) {
            logger.error("downloaded application update could not be verified "
                    + "or existing .jar could not be replaced", e);
        }

        return false;
    }

    private ByteArrayOutputStream readApplicationUpdate(final GitHubReleaseAsset releaseAsset) throws Exception {
        final ByteArrayOutputStream applicationUpdateOutputStream = new ByteArrayOutputStream(
                releaseAsset.getContentSize());
        final URI releaseUrl = new URI(releaseAsset.getDownloadUrl());
        final HttpGet httpGetRequest = new HttpGet(releaseUrl);
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpResponse httpResponse = httpClient.execute(httpGetRequest);
        final int httpResponseStatus = httpResponse.getStatusLine().getStatusCode();
        if (httpResponseStatus != 200) {
            logger.error("updated application .jar was not available on "
                    + "remote server, returning http status {}", httpResponseStatus);
        } else {
            final HttpEntity httpEntity = httpResponse.getEntity();
            final InputStream applicationUpdateInputStream = httpEntity.getContent();
            logger.info("application update download starting ...");
            try {
                IOUtils.copy(applicationUpdateInputStream, applicationUpdateOutputStream);
            } finally {
                applicationUpdateInputStream.close();
            }
        }

        return applicationUpdateOutputStream;
    }

    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class GitHubRelease {
        @NotBlank
        @JsonProperty("tag_name")
        private String tagName;

        @NotEmpty
        @Valid
        @JsonProperty("assets")
        private List<GitHubReleaseAsset> assets;

        public String getTagName() {
            return tagName;
        }

        public void setTagName(final String tagName) {
            this.tagName = tagName;
        }

        public List<GitHubReleaseAsset> getAssets() {
            return assets;
        }

        public void setAssets(final List<GitHubReleaseAsset> assets) {
            this.assets = assets;
        }
    }

    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class GitHubReleaseAsset {
        @NotBlank
        @JsonProperty("content_type")
        private String contentType;

        @NotBlank
        @JsonProperty("browser_download_url")
        private String downloadUrl;

        @Min(1)
        @JsonProperty("size")
        private int contentSize;

        public String getContentType() {
            return contentType;
        }

        public void setContentType(final String contentType) {
            this.contentType = contentType;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(final String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public int getContentSize() {
            return contentSize;
        }

        public void setContentSize(final int contentSize) {
            this.contentSize = contentSize;
        }
    }
}
