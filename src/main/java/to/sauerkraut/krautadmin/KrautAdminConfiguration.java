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
package to.sauerkraut.krautadmin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.inject.Inject;
import com.google.inject.persist.PersistService;

import javax.servlet.DispatcherType;
import javax.validation.Valid;
import javax.validation.constraints.*;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.secnod.dropwizard.shiro.ShiroConfiguration;
import ru.vyarus.dropwizard.orient.configuration.HasOrientServerConfiguration;
import ru.vyarus.dropwizard.orient.configuration.OrientServerConfiguration;
import to.sauerkraut.krautadmin.cli.MetadataAware;
import to.sauerkraut.krautadmin.core.Toolkit;
import to.sauerkraut.krautadmin.db.setup.HasDatabaseConfiguration;
import to.sauerkraut.krautadmin.job.scheduler.ExtendedSchedulerConfiguration;
import to.sauerkraut.krautadmin.resources.assets.ConfiguredAssetsBundle;
import to.sauerkraut.krautadmin.resources.assets.HasAssetsConfiguration;

import java.util.List;
import java.util.Map;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class KrautAdminConfiguration extends Configuration 
    implements HasOrientServerConfiguration, HasDatabaseConfiguration, HasAssetsConfiguration, MetadataAware {

    @JsonIgnore
    private String applicationLocation;
    @JsonIgnore
    private String configurationPath;
    @JsonIgnore
    private String jarName;
    @JsonIgnore
    private String release;
    @JsonIgnore
    private String artifactName;
    @JsonIgnore
    private boolean updatePending;

    @JsonProperty
    private String applicationSecret;
    
    @NotNull
    @Valid
    @JsonProperty("db")
    private DatabaseConfiguration databaseConfiguration;

    @NotNull
    @Valid
    @JsonProperty("assets")
    private AssetsConfiguration assetsConfiguration;

    @Valid
    @JsonProperty
    private List<ResponseCachingConfiguration> responseCachingConfigurations = Lists.newArrayList();
    
    @NotNull
    @Valid
    @JsonProperty("security")
    private SecurityConfiguration securityConfiguration;
    
    @NotNull
    @Valid
    @JsonProperty("scheduler")
    private ExtendedSchedulerConfiguration schedulerConfiguration;

    @Valid
    @JsonProperty("orient-server")
    private JarLocationAwareOrientServerConfiguration orientServerConfiguration;
    
    @NotNull
    @Valid
    @JsonProperty("shiro")
    private ShiroConfiguration shiroConfiguration;
    
    @Inject
    private PersistService orientService;

    @Override
    public JarLocationAwareOrientServerConfiguration getOrientServerConfiguration() {
        return orientServerConfiguration;
    }
    
    public void setOrientServerConfiguration(
            final JarLocationAwareOrientServerConfiguration orientServerConfiguration) {
        this.orientServerConfiguration = orientServerConfiguration;
    }
    
    public void setDatabaseConfiguration(final DatabaseConfiguration dbConfiguration) {
        this.databaseConfiguration = dbConfiguration;
    }
    
    @Override
    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    public PersistService getOrientService() {
        return orientService;
    }

    public void setOrientService(final PersistService orientService) {
        this.orientService = orientService;
    }

    public ExtendedSchedulerConfiguration getSchedulerConfiguration() {
        return schedulerConfiguration;
    }

    public void setSchedulerConfiguration(final ExtendedSchedulerConfiguration schedulerConfiguration) {
        this.schedulerConfiguration = schedulerConfiguration;
    }

    public ShiroConfiguration getShiroConfiguration() {
        return shiroConfiguration;
    }

    public void setShiroConfiguration(final ShiroConfiguration shiroConfiguration) {
        this.shiroConfiguration = shiroConfiguration;
    }

    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    public void setSecurityConfiguration(final SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    public AssetsConfiguration getAssetsConfiguration() {
        return assetsConfiguration;
    }

    public void setAssetsConfiguration(final AssetsConfiguration assetsConfiguration) {
        this.assetsConfiguration = assetsConfiguration;
    }

    @Override
    public void setApplicationLocation(final String applicationLocation) {
        this.applicationLocation = applicationLocation;
    }

    @Override
    public String getApplicationLocation() {
        return applicationLocation;
    }

    @Override
    public void setConfigurationPath(final String configurationPath) {
        this.configurationPath = configurationPath;
    }

    @Override
    public String getConfigurationPath() {
        return configurationPath;
    }

    @Override
    public String getJarName() {
        return jarName;
    }

    @Override
    public void setJarName(final String jarName) {
        this.jarName = jarName;
    }

    public boolean isUpdatePending() {
        return updatePending;
    }

    public void setUpdatePending(final boolean updatePending) {
        this.updatePending = updatePending;
    }

    public List<ResponseCachingConfiguration> getResponseCachingConfigurations() {
        return responseCachingConfigurations;
    }

    public void setResponseCachingConfigurations(
            final List<ResponseCachingConfiguration> responseCachingConfigurations) {
        this.responseCachingConfigurations = responseCachingConfigurations;
    }

    @Override
    public String getRelease() {
        return release;
    }

    @Override
    public void setRelease(final String release) {
        this.release = release;
    }

    @Override
    public String getArtifactName() {
        return artifactName;
    }

    @Override
    public void setArtifactName(final String artifactName) {
        this.artifactName = artifactName;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(final String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public static class SecurityConfiguration {
        @NotBlank
        @JsonProperty
        private String passwordHashFormat;
        @JsonProperty
        private int passwordHashIterations = 1000000;
        @JsonProperty
        private int maximumFailedAttempts = 3;
        @JsonProperty
        private int banDays = 1;
        @JsonProperty
        private boolean authorizationCachingEnabled;
        @Valid
        @JsonProperty("remember-me-cookie")
        private RememberMeCookieConfiguration rememberMeCookieConfiguration = new RememberMeCookieConfiguration();

        public String getPasswordHashFormat() {
            return passwordHashFormat;
        }

        public void setPasswordHashFormat(final String passwordHashFormat) {
            this.passwordHashFormat = passwordHashFormat;
        }

        public int getPasswordHashIterations() {
            return passwordHashIterations;
        }

        public void setPasswordHashIterations(final int passwordHashIterations) {
            this.passwordHashIterations = passwordHashIterations;
        }

        public int getMaximumFailedAttempts() {
            return maximumFailedAttempts;
        }

        public void setMaximumFailedAttempts(final int maximumFailedAttempts) {
            this.maximumFailedAttempts = maximumFailedAttempts;
        }

        public int getBanDays() {
            return banDays;
        }

        public void setBanDays(final int banDays) {
            this.banDays = banDays;
        }

        public RememberMeCookieConfiguration getRememberMeCookieConfiguration() {
            return rememberMeCookieConfiguration;
        }

        public void setRememberMeCookieConfiguration(
                final RememberMeCookieConfiguration rememberMeCookieConfiguration) {
            this.rememberMeCookieConfiguration = rememberMeCookieConfiguration;
        }

        public boolean isAuthorizationCachingEnabled() {
            return authorizationCachingEnabled;
        }

        public void setAuthorizationCachingEnabled(final boolean authorizationCachingEnabled) {
            this.authorizationCachingEnabled = authorizationCachingEnabled;
        }

        /**
         *
         * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
         */
        public static class RememberMeCookieConfiguration {
            public static final String DEFAULT_NAME = "goldkeks";
            public static final int DEFAULT_VERSION = -1;
            public static final int DEFAULT_MAX_AGE = 60 * 60 * 24 * 365;

            @JsonProperty
            private int maxAgeSeconds = DEFAULT_MAX_AGE;
            @JsonProperty
            private int version = DEFAULT_VERSION;
            @JsonProperty
            private boolean secure = true;
            @JsonProperty
            private boolean httpOnly = true;
            @JsonProperty
            @NotBlank
            private String name = DEFAULT_NAME;
            @JsonProperty
            private String domain;
            @JsonProperty
            private String path;

            public int getMaxAgeSeconds() {
                return maxAgeSeconds;
            }

            public void setMaxAgeSeconds(final int maxAgeSeconds) {
                this.maxAgeSeconds = maxAgeSeconds;
            }

            public int getVersion() {
                return version;
            }

            public void setVersion(final int version) {
                this.version = version;
            }

            public boolean isSecure() {
                return secure;
            }

            public void setSecure(final boolean secure) {
                this.secure = secure;
            }

            public boolean isHttpOnly() {
                return httpOnly;
            }

            public void setHttpOnly(final boolean httpOnly) {
                this.httpOnly = httpOnly;
            }

            public String getName() {
                return name;
            }

            public void setName(final String name) {
                this.name = name;
            }

            public String getDomain() {
                return domain;
            }

            public void setDomain(final String domain) {
                this.domain = domain;
            }

            public String getPath() {
                return path;
            }

            public void setPath(final String path) {
                this.path = path;
            }
        }
    }
    
    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public static class DatabaseConfiguration {
        @NotBlank
        @JsonProperty
        private String uri;

        @NotBlank
        @JsonProperty
        private String user;

        @NotBlank
        @JsonProperty
        private String pass;

        @JsonProperty
        private boolean create;
        
        @JsonProperty
        private boolean dropInsecureUsersOnCreate;

        public String getUri() {
            return uri;
        }

        public String getUser() {
            return user;
        }

        public String getPass() {
            return pass;
        }

        public boolean isCreate() {
            return create;
        }

        public void setUri(final String uri) {
            this.uri = Toolkit.parseDbPath(uri);
        }

        public void setUser(final String user) {
            this.user = user;
        }

        public void setPass(final String pass) {
            this.pass = pass;
        }

        public void setCreate(final boolean create) {
            this.create = create;
        }

        public boolean isDropInsecureUsersOnCreate() {
            return dropInsecureUsersOnCreate;
        }

        public void setDropInsecureUsersOnCreate(final boolean dropInsecureUsersOnCreate) {
            this.dropInsecureUsersOnCreate = dropInsecureUsersOnCreate;
        }
    }

    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public static class ResponseCachingConfiguration {
        @NotNull
        @JsonProperty
        private Map<String, String> initParameters = Maps.newHashMap();
        @JsonProperty
        @NotBlank
        private String filterName;
        @JsonProperty
        @NotBlank
        private String filterPath;
        @NotNull
        @NotEmpty
        @JsonProperty
        private List<DispatcherType> dispatcherTypes = Lists.newArrayList();

        public Map<String, String> getInitParameters() {
            return initParameters;
        }

        public void setInitParameters(final Map<String, String> initParameters) {
            this.initParameters = initParameters;
        }

        public String getFilterName() {
            return filterName;
        }

        public void setFilterName(final String filterName) {
            this.filterName = filterName;
        }

        public String getFilterPath() {
            return filterPath;
        }

        public void setFilterPath(final String filterPath) {
            this.filterPath = filterPath;
        }

        public List<DispatcherType> getDispatcherTypes() {
            return dispatcherTypes;
        }

        public void setDispatcherTypes(final List<DispatcherType> dispatcherTypes) {
            this.dispatcherTypes = dispatcherTypes;
        }
    }

    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public static class AssetsConfiguration {
        @NotNull
        @JsonProperty
        private String cacheSpec = ConfiguredAssetsBundle.DEFAULT_CACHE_SPEC.toParsableString();

        @NotNull
        @JsonProperty
        private Map<String, String> overrides = Maps.newHashMap();

        @NotNull
        @JsonProperty
        private Map<String, String> mimeTypes = Maps.newHashMap();

        /** The caching specification for how to memoize assets. */
        public String getCacheSpec() {
            return cacheSpec;
        }

        public Iterable<Map.Entry<String, String>> getOverrides() {
            return Iterables.unmodifiableIterable(overrides.entrySet());
        }

        public Iterable<Map.Entry<String, String>> getMimeTypes() {
            return Iterables.unmodifiableIterable(mimeTypes.entrySet());
        }
    }
    
    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public static class JarLocationAwareOrientServerConfiguration extends OrientServerConfiguration {
        
        /**
         * Directory may not exist - orient will create it when necessary.
         * Special variable '$TMP' can be used. It will be substituted
         * by system temp directory path ('java.io.tmpdir').
         * Special variable '$APP' can be used. It will be substituted
         * by current app's directory path - in fat jars this will be the folder
         * containing the fat jar.
         *
         * @param filesPath path to store database files.
         */
        @JsonProperty
        @Override
        public void setFilesPath(final String filesPath) {
            super.setFilesPath(Toolkit.parseDbPath(filesPath));
        }
    }
}
