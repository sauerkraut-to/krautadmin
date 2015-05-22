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
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import ru.vyarus.guice.persist.orient.db.data.DataInitializer;

import javax.inject.Singleton;

import to.sauerkraut.binding.yamlbeans.YamlReader;
import to.sauerkraut.krautadmin.KrautAdminApplication;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;
import to.sauerkraut.krautadmin.core.IO;
import to.sauerkraut.krautadmin.db.model.*;
import to.sauerkraut.krautadmin.db.repository.FrontendDataMirrorRepository;
import to.sauerkraut.krautadmin.db.repository.ModelRepository;
import to.sauerkraut.krautadmin.db.repository.SampleEntityRepository;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Singleton
@Transactional
public class ApplicationUpgradeManagerAndFixturesLoader implements DataInitializer {
    
    @Log
    private static Logger logger;
    @Inject
    private SampleEntityRepository repository;
    @Inject
    private ModelRepository modelRepository;
    @Inject
    private FrontendDataMirrorRepository frontendDataMirrorRepository;
    @Inject
    private KrautAdminConfiguration configuration;
    
    @Override
    public void initializeData() {
        modelRepository.deleteAll();

        HashMap<String, Object> fixturesMap = null;

        try {
            fixturesMap = loadFixtures("/databaseFixtures/fixtures.yml",
                    configuration.getDatabaseConfiguration().getDefaultDataModelPackage());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("loading fixtures failed");
        }

        persistLoadedFixtures(fixturesMap);

        if (repository.count() == 0) {
            logger.info("Bootstrapping sample data");

            for (int i = 0; i < 20; i++) {
                //repository.save(new SampleEntity("comment" + i));
                (new SampleEntity("comment" + i)).save();
            }
            
            final FrontendDataMirror frontendDataMirror = new FrontendDataMirror(
                    FrontendDataMirror.SecureProtocol.SSL, true);
            frontendDataMirror.setActive(true);
            frontendDataMirror.setFtpServer("ftp.test.org");
            frontendDataMirror.setFtpUsername("testuser");
            frontendDataMirror.setFtpPassword("testpass");
            
            frontendDataMirrorRepository.save(frontendDataMirror);
        }
        
        //TODO: at the end execute update scripts in right order and only if not executed yet
    }

    public static HashMap<String, Object> loadFixtures(final String classpathFilePath,
                                                       final String defaultDataModelPackage) throws IOException {
        final YamlReader reader = new YamlReader(IO.readContentAsString(
                ApplicationUpgradeManagerAndFixturesLoader.class
                        .getResourceAsStream(classpathFilePath)));

        for (final ClassPath.ClassInfo info
                : ClassPath.from(KrautAdminApplication.getClassLoader()).getTopLevelClasses()) {
            if (info.getName().startsWith(defaultDataModelPackage.concat("."))) {
                final Class<?> clazz = info.load();
                reader.getConfig().setClassTag(clazz.getSimpleName(), clazz);
            }
        }

        final Object o = reader.read();

        if (o instanceof HashMap) {
            return (HashMap<String, Object>) o;
        } else {
            return null;
        }
    }

    public static void persistLoadedFixtures(final HashMap<String, Object> loadedFixtures) {
        if (loadedFixtures == null) {
            return;
        }

        for (Map.Entry<String, Object> fixturesEntry : loadedFixtures.entrySet()) {
            logger.info("loading fixture(s) {}", fixturesEntry.getKey());
            if (Model.class.isAssignableFrom(fixturesEntry.getValue().getClass())) {
                final Model modelInstance = (Model) fixturesEntry.getValue();
                modelInstance.validateAndSave();

            } else if (Collection.class.isAssignableFrom(fixturesEntry.getValue().getClass())) {
                final Collection<Object> modelsCollection = (Collection<Object>) fixturesEntry.getValue();
                for (Object possibleModelInstance : modelsCollection) {
                    if (Model.class.isAssignableFrom(possibleModelInstance.getClass())) {
                        final Model modelInstance = (Model) possibleModelInstance;
                        modelInstance.validateAndSave();
                    }
                }
            }
        }
    }
}
