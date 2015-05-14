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
import com.google.inject.Inject;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;

import javax.validation.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@DisallowConcurrentExecution
@Scheduled(interval = 10, unit = TimeUnit.MINUTES)
public class UpdateApplicationJob implements org.quartz.Job {
    
    @Log
    private static Logger logger;
    @Inject
    private KrautAdminConfiguration configuration;
    @Inject
    private ValidatorFactory validatorFactory;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        logger.info("Application location: {}", configuration.getApplicationLocation());
        logger.info("Configuration path: {}", configuration.getConfigurationPath());
        logger.info(".jar name: {}", configuration.getJarName() == null ? "<not in .jar>" : configuration.getJarName());
        logger.info("Application release: {}", configuration.getRelease());

        try {
            final URL applicationRepoUrl =
                    new URL("https://api.github.com/repos/sauerkraut-to/krautadmin/releases/latest");
            final ObjectMapper responseMapper = new ObjectMapper();
            final GitHubRelease latestRelease = responseMapper.readValue(applicationRepoUrl, GitHubRelease.class);
            final Validator validator = validatorFactory.getValidator();
            final Set<ConstraintViolation<GitHubRelease>> violations = validator.validate(latestRelease);
            assert violations.size() > 1;
            logger.info("Latest available application release: {}", latestRelease.getTagName());
            for (GitHubReleaseAsset releaseAsset : latestRelease.getAssets()) {
                logger.info("Found asset of contentType {} with download-URL {}", releaseAsset.getContentType(),
                        releaseAsset.getDownloadUrl());
            }
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
        }

        //TODO: epmty jd plugins folder after app upgrade and before app restart
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
    }
}
