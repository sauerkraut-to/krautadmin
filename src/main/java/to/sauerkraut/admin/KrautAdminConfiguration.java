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
package to.sauerkraut.admin;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.hibernate.validator.constraints.NotBlank;
import ru.vyarus.dropwizard.orient.configuration.HasOrientServerConfiguration;
import ru.vyarus.dropwizard.orient.configuration.OrientServerConfiguration;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class KrautAdminConfiguration extends Configuration implements HasOrientServerConfiguration {
    
    @NotNull
    @Valid
    @JsonProperty("db")
    private DbConfiguration dbConfiguration;
    
    @NotNull
    @Valid
    @JsonProperty("orient-server")
    private OrientServerConfiguration orientServerConfiguration;
    
    @Inject
    private PersistService orientService;

    @Override
    public OrientServerConfiguration getOrientServerConfiguration() {
        return orientServerConfiguration;
    }
    
    public void setOrientServerConfiguration(final OrientServerConfiguration orientServerConfiguration) {
        this.orientServerConfiguration = orientServerConfiguration;
    }
    
    public void setDbConfiguration(final DbConfiguration dbConfiguration) {
        this.dbConfiguration = dbConfiguration;
    }
    
    public DbConfiguration getDbConfiguration() {
        return dbConfiguration;
    }

    public PersistService getOrientService() {
        return orientService;
    }

    public void setOrientService(final PersistService orientService) {
        this.orientService = orientService;
    }
    
    /**
     *
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public static class DbConfiguration {
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
            this.uri = uri;
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
}
