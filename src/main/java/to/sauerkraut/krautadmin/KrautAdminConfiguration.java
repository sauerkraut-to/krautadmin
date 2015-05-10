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

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.hibernate.validator.constraints.NotBlank;
import org.secnod.dropwizard.shiro.ShiroConfiguration;
import ru.vyarus.dropwizard.orient.configuration.HasOrientServerConfiguration;
import ru.vyarus.dropwizard.orient.configuration.OrientServerConfiguration;
import to.sauerkraut.krautadmin.core.Toolkit;
import to.sauerkraut.krautadmin.db.setup.HasDatabaseConfiguration;
import to.sauerkraut.krautadmin.job.ExtendedSchedulerConfiguration;
import to.sauerkraut.krautadmin.resources.assets.ConfiguredAssetsBundle;
import to.sauerkraut.krautadmin.resources.assets.HasAssetsConfiguration;

import java.util.Map;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class KrautAdminConfiguration extends Configuration 
    implements HasOrientServerConfiguration, HasDatabaseConfiguration, HasAssetsConfiguration {
    
    @NotNull
    @Valid
    @JsonProperty("db")
    private DatabaseConfiguration databaseConfiguration;

    @NotNull
    @Valid
    @JsonProperty("assets")
    private AssetsConfiguration assetsConfiguration;
    
    @NotNull
    @Valid
    @JsonProperty("security")
    private SecurityConfiguration securityConfiguration;
    
    @NotNull
    @Valid
    @JsonProperty("scheduler")
    private ExtendedSchedulerConfiguration schedulerConfiguration;
    
    @NotNull
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

    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public static class SecurityConfiguration {
        @NotBlank
        @JsonProperty
        private String passwordHashFormat;
        
        @JsonProperty
        private int passwordHashIterations;

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
    public class AssetsConfiguration {
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
        @JsonProperty("files-path")
        @Override
        public void setFilesPath(final String filesPath) {
            super.setFilesPath(Toolkit.parseDbPath(filesPath));
        }
    }
}
