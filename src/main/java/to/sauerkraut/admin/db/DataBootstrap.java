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
package to.sauerkraut.admin.db;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import ru.vyarus.guice.persist.orient.db.data.DataInitializer;

import javax.inject.Singleton;
import to.sauerkraut.admin.db.model.SampleEntity;
import to.sauerkraut.admin.db.repository.SampleEntityRepository;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Singleton
@Transactional
public class DataBootstrap implements DataInitializer {

    @Inject
    private SampleEntityRepository repository;
    @Log
    private Logger logger;

    @Override
    public void initializeData() {
        if (repository.count() == 0) {
            logger.info("Bootstrapping sample data");

            for (int i = 0; i < 20; i++) {
                repository.save(new SampleEntity("comment" + i));
            }
        }
    }
}
