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
package to.sauerkraut.admin.db.repository;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicSingletonProvider;
import com.google.inject.persist.Transactional;
import ru.vyarus.guice.persist.orient.repository.command.query.Query;
import ru.vyarus.guice.persist.orient.repository.core.ext.service.result.ext.detach.DetachResult;
import ru.vyarus.guice.persist.orient.support.repository.mixin.crud.ObjectCrud;

import java.util.List;
import to.sauerkraut.admin.db.model.SampleEntity;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Transactional
@ProvidedBy(DynamicSingletonProvider.class)
public interface SampleEntityRepository extends ObjectCrud<SampleEntity> {

    // executing everything and detaching to avoid problems 
    // (normally you would have some dto conversion inside transaction)
    @Query("select from SampleEntity")
    @DetachResult
    List<SampleEntity> dontDoThat();

    @Query("select count(*) from SampleEntity")
    int count();
}
