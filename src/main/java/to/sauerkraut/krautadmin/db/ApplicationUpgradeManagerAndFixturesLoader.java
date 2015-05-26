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
package to.sauerkraut.krautadmin.db;

import com.google.common.reflect.ClassPath;
import javax.inject.Inject;

import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import ru.vyarus.guice.persist.orient.db.data.DataInitializer;

import javax.inject.Singleton;
import javax.validation.ConstraintViolationException;

import to.sauerkraut.binding.yamlbeans.YamlReader;
import to.sauerkraut.krautadmin.KrautAdminApplication;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;
import to.sauerkraut.krautadmin.auth.PasswordService;
import to.sauerkraut.krautadmin.core.IO;
import to.sauerkraut.krautadmin.core.Toolkit;
import to.sauerkraut.krautadmin.db.model.*;
import to.sauerkraut.krautadmin.db.repository.*;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Singleton
@Transactional
public class ApplicationUpgradeManagerAndFixturesLoader implements DataInitializer {

    public static final String CLASSPATH_PATH_TO_DATABASE_FIXTURES = "/databaseFixtures/";
    public static final String CLASSPATH_PATH_TO_DATABASE_UPDATES = "/databaseUpdates/";
    public static final String CLASSPATH_PATH_TO_CONFIGURATION_UPDATES = "/configurationUpdates/";
    
    @Log
    private static Logger logger;
    @Inject
    private static LoadedDatabaseFixtureRepository loadedDatabaseFixtureRepository;
    @Inject
    private static LoadedConfigurationUpdateRepository loadedConfigurationUpdateRepository;
    @Inject
    private static LoadedDatabaseUpdateRepository loadedDatabaseUpdateRepository;
    @Inject
    private static ModelRepository modelRepository;
    @Inject
    private FrontendDataMirrorRepository frontendDataMirrorRepository;
    @Inject
    private PasswordService passwordService;
    @Inject
    private KrautAdminConfiguration configuration;
    
    @Override
    public void initializeData() {
        boolean hasDatabaseUpdateChanges;
        boolean hasDatabaseFixtureChanges;
        boolean hasConfigurationUpdateChanges;

        try {
            hasDatabaseUpdateChanges =
                    applyDatabaseUpdateFiles(listAvailableUpdateFiles(CLASSPATH_PATH_TO_DATABASE_UPDATES));
        } catch (RuntimeException e) {
            logger.error("applying database update(s) failed, application upgrade aborted", e);
            throw e;
        }

        try {
            hasDatabaseFixtureChanges =
                    applyDatabaseFixtureFiles(listAvailableUpdateFiles(CLASSPATH_PATH_TO_DATABASE_FIXTURES));
        } catch (RuntimeException e) {
            logger.error("applying database fixture(s) failed, application upgrade aborted", e);
            throw e;
        }

        try {
            hasConfigurationUpdateChanges =
                    applyConfigurationUpdateFiles(listAvailableUpdateFiles(CLASSPATH_PATH_TO_CONFIGURATION_UPDATES));
        } catch (RuntimeException e) {
            logger.error("applying configuration update(s) failed, application upgrade aborted", e);
            throw e;
        }

        if (hasConfigurationUpdateChanges) {
            configuration.setUpdatePending(true);
            /*try {
                Toolkit.restartApplication();
            } catch (Exception e) {
                logger.error("failed to restart application - updates pending, please restart manually");
            }*/
        }
    }

    public static boolean applyDatabaseFixtureFile(final long fileIndex, final String classpathFilePath) {
        boolean madeChanges = false;
        HashMap<String, Object> fixturesMap = null;

        if (null == loadedDatabaseFixtureRepository.findByNumber(fileIndex)) {
            try {
                fixturesMap = loadDatabaseFixture(classpathFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            persistDatabaseFixture(fixturesMap);
            madeChanges = true;
            logger.info("successfully applied database fixture file {}", classpathFilePath);
            (new LoadedDatabaseFixture(fileIndex)).validateAndSave();
        }

        return madeChanges;
    }

    public static boolean applyDatabaseFixtureFiles(final Map<Long, String> availableDatabaseFixtures) {
        boolean madeChanges = false;
        if (availableDatabaseFixtures != null) {
            for (Map.Entry<Long, String> availableDatabaseFixture : availableDatabaseFixtures.entrySet()) {
                final boolean currentMadeChanges = applyDatabaseFixtureFile(availableDatabaseFixture.getKey(),
                        availableDatabaseFixture.getValue());
                madeChanges = currentMadeChanges ? true : madeChanges;
            }
        }

        return madeChanges;
    }

    public static boolean applyDatabaseUpdateFile(final long fileIndex, final String classpathFilePath) {
        boolean madeChanges = false;

        if (null == loadedDatabaseUpdateRepository.findByNumber(fileIndex)) {
            modelRepository.executeSql(IO.readContentAsString(IO.class.getResourceAsStream(classpathFilePath)));
            madeChanges = true;
            logger.info("successfully applied database update file {}", classpathFilePath);
            (new LoadedDatabaseUpdate(fileIndex)).validateAndSave();
        }

        return madeChanges;
    }

    public static boolean applyDatabaseUpdateFiles(final Map<Long, String> availableDatabaseFixtures) {
        boolean madeChanges = false;
        if (availableDatabaseFixtures != null) {
            for (Map.Entry<Long, String> availableDatabaseFixture : availableDatabaseFixtures.entrySet()) {
                final boolean currentMadeChanges = applyDatabaseFixtureFile(availableDatabaseFixture.getKey(),
                        availableDatabaseFixture.getValue());
                madeChanges = currentMadeChanges ? true : madeChanges;
            }
        }

        return madeChanges;
    }

    public static boolean applyConfigurationUpdateFile(final long fileIndex, final String classpathFilePath) {
        boolean madeChanges = false;
        if (null == loadedConfigurationUpdateRepository.findByNumber(fileIndex)) {

            //TODO: implement yaml-merging

            madeChanges = true;
            logger.info("successfully applied configuration update file {}", classpathFilePath);
            (new LoadedConfigurationUpdate(fileIndex)).validateAndSave();
        }

        return madeChanges;
    }

    public static boolean applyConfigurationUpdateFiles(final Map<Long, String> availableConfigurationUpdates) {
        boolean madeChanges = false;
        if (availableConfigurationUpdates != null) {
            for (Map.Entry<Long, String> availableConfigurationUpdate : availableConfigurationUpdates.entrySet()) {
                final boolean currentMadeChanges = applyConfigurationUpdateFile(availableConfigurationUpdate.getKey(),
                        availableConfigurationUpdate.getValue());
                madeChanges = currentMadeChanges ? true : madeChanges;
            }
        }

        return madeChanges;
    }

    public static Map<Long, String> listAvailableUpdateFiles(final String classpathFolderPath) {
        /*
         * we choose a treemap to assure the update-files will be loaded in the right order,
         * beginning with the lowest number
         */
        final TreeMap<Long, String> entriesMap = new TreeMap<>();
        final List<String> folderEntries = IO.listFilenamesOfClasspathFolder(classpathFolderPath);
        if (folderEntries != null) {
            for (String folderEntry : folderEntries) {
                final String entryValue = classpathFolderPath.concat(String.valueOf(folderEntry));
                Long entryKey;
                try {
                    entryKey = Long.parseLong(folderEntry.split("\\.")[0]);
                } catch (Exception e) {
                    entryKey = null;
                }

                if (entryKey != null) {
                    entriesMap.put(entryKey, entryValue);
                }
            }
        }

        return entriesMap;
    }

    public static HashMap<String, Object> loadDatabaseFixture(final String classpathFilePath) throws IOException {
        final YamlReader reader =
                new YamlReader(IO.readContentAsString(IO.class.getResourceAsStream(classpathFilePath)));

        for (final ClassPath.ClassInfo info
                : ClassPath.from(KrautAdminApplication.getClassLoader()).getTopLevelClassesRecursive("to.sauerkraut")) {
            if (info.getName().contains(".model.")) {
                final Class<?> clazz = info.load();
                if (Model.class.isAssignableFrom(clazz)) {
                    reader.getConfig().setClassTag(clazz.getSimpleName(), clazz);
                }
            }
        }

        final Object o = reader.read();

        if (o instanceof HashMap) {
            return (HashMap<String, Object>) o;
        } else {
            return null;
        }
    }

    public static void persistDatabaseFixture(final HashMap<String, Object> loadedFixtures) {
        if (loadedFixtures == null) {
            return;
        }

        for (Map.Entry<String, Object> fixturesEntry : loadedFixtures.entrySet()) {
            logger.info("loading fixture-part {}", fixturesEntry.getKey());
            if (Model.class.isAssignableFrom(fixturesEntry.getValue().getClass())) {
                final Model modelInstance = (Model) fixturesEntry.getValue();
                modelInstance.validateAndSave();

            } else if (Collection.class.isAssignableFrom(fixturesEntry.getValue().getClass())) {
                final Collection<Object> modelsCollection = (Collection<Object>) fixturesEntry.getValue();
                for (Object possibleModelInstance : modelsCollection) {
                    if (Model.class.isAssignableFrom(possibleModelInstance.getClass())) {
                        final Model modelInstance = (Model) possibleModelInstance;
                        try {
                            modelInstance.validateAndSave();
                        } catch (ConstraintViolationException e) {
                            Toolkit.logConstraintViolations(e.getConstraintViolations());
                            throw e;
                        }
                    }
                }
            }
        }
    }
}
