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
package to.sauerkraut.krautadmin.db.setup;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 * @param <T>
 */
public class DatabaseAutoCreationBundle<T extends Configuration & HasDatabaseConfiguration>
        implements ConfiguredBundle<T> {
    private final Logger logger = LoggerFactory.getLogger(DatabaseAutoCreationBundle.class);

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(final Bootstrap<?> bootstrap) {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(final T configuration, final Environment environment) throws Exception {

        final KrautAdminConfiguration.DatabaseConfiguration conf = configuration.getDatabaseConfiguration();
        if (conf == null || !conf.isCreate()) {
            logger.debug("Database auto creation disabled. Set 'create: true' in configuration to enable.");
            return;
        }
        
        // create if required (without creation work with db is impossible)
        final ODatabaseDocumentTx database = new ODatabaseDocumentTx(conf.getUri());
        try {
            // memory, local, plocal modes support simplified db creation,
            // but remote database must be created differently
            if (isLocalDatabase(conf.getUri()) && !database.exists()) {
                logger.info("Creating database: '{}'", conf.getUri());
                database.create();
                setupLocalDatabaseUser(database, conf.getUser(), conf.getPass(), conf.isDropInsecureUsersOnCreate());
            }
        } finally {
            database.close();
        }
    }
    
    private void setupLocalDatabaseUser(final ODatabaseDocumentTx database, final String user, final String password, 
            final boolean dropUnchangedExistingUsers) {
        final List<String> unchangedExistingUsersToDrop = new ArrayList<>();
        boolean userHasExisted = false;
        
        for (ODocument insecureDefaultUser : database.getMetadata().getSecurity().getAllUsers()) {
            final String currentDbUser = insecureDefaultUser.field("name");
            if (currentDbUser.equalsIgnoreCase(user)) {
                userHasExisted = true;
                insecureDefaultUser.field("password", password);
                insecureDefaultUser.save();
            } else {
                unchangedExistingUsersToDrop.add(currentDbUser);
            }
        }
        if (!userHasExisted) {
            database.getMetadata().getSecurity().createUser(user, password, 
                new String[]{"admin"}); 
        }
        
        if (dropUnchangedExistingUsers) {
            for (String insecureUserToDrop : unchangedExistingUsersToDrop) {
                database.getMetadata().getSecurity().dropUser(insecureUserToDrop);
            }
        }
        
    }

    /**
     * @return true if database is local, false for remote
     */
    private boolean isLocalDatabase(final String uri) {
        return uri != null && !uri.startsWith("remote:");
    }
}
