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
package to.sauerkraut.krautadmin.db.repository;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicSingletonProvider;
import com.google.inject.persist.Transactional;
import java.util.Set;
import ru.vyarus.guice.persist.orient.repository.command.query.Query;
import ru.vyarus.guice.persist.orient.support.repository.mixin.crud.ObjectCrud;
import to.sauerkraut.krautadmin.db.model.Permission;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Transactional
@ProvidedBy(DynamicSingletonProvider.class)
public interface PermissionRepository extends ObjectCrud<Permission> {

    @Query("select from Permission")
    Set<Permission> list();
    
    @Query("select count(*) from Permission")
    int count();
    
    @Query("select from Permission where name=? limit 1")
    Permission findByName(String name);
}
