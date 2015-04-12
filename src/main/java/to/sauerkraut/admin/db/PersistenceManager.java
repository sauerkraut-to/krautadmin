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

import com.google.inject.persist.PersistService;
import io.dropwizard.lifecycle.Managed;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Singleton
public class PersistenceManager implements Managed {

    @Inject
    private PersistService persistService;

    @Override
    public void start() throws Exception {
        persistService.start();
    }

    @Override
    public void stop() throws Exception {
        persistService.stop();
    }
}
