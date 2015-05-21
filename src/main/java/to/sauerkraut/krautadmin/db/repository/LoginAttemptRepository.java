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
import ru.vyarus.guice.persist.orient.repository.command.ext.param.Param;
import ru.vyarus.guice.persist.orient.repository.command.query.Query;
import ru.vyarus.guice.persist.orient.support.repository.mixin.crud.ObjectCrud;
import to.sauerkraut.krautadmin.db.model.LoginAttempt;

import java.util.Date;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Transactional
@ProvidedBy(DynamicSingletonProvider.class)
public interface LoginAttemptRepository extends ObjectCrud<LoginAttempt> {
    
    @Query("update LoginAttempt set lastAttempt = :lastAttempt, hashedIp = :hashedIp upsert where hashedIp = :hashedIp")
    int upsert(@Param("hashedIp") String hashedIp, @Param("lastAttempt") Date lastAttempt);
    
    @Query("select from LoginAttempt where hashedIp = ? and lastAttempt > ? limit 1")
    LoginAttempt findByHashedIpAndNewerThan(String hashedIp, long timeBefore);

    @Query("select from LoginAttempt where hashedIp = ? and lastAttempt < ? limit 1")
    LoginAttempt findByHashedIpAndOlderThan(String hashedIp, long timeAfter);

    @Query("delete from LoginAttempt where lastAttempt < ?")
    int deleteOlderThan(long timeAfter);

    @Query("delete from LoginAttempt where hashedIp = ? limit 1")
    int deleteByHashedIp(String hashedIp);

    @Query("select from LoginAttempt where hashedIp = ? limit 1")
    LoginAttempt findByHashedIp(String hashedIp);

    @Query("select from LoginAttempt where hashedIp = ? and (failedAttempts >= ? or lastAttempt > ?) limit 1")
    LoginAttempt findByHashedIpAndLimitExceededOrNewerThan(String hashedIp, int maximumFailedAttempts, long timeBefore);

    @Query("select from LoginAttempt where hashedIp = ? and failedAttempts >= ? limit 1")
    LoginAttempt findByHashedIpAndLimitExceeded(String hashedIp, int maximumFailedAttempts);

    @Query("update LoginAttempt increment failedAttempts = 1 where hashedIp = ? limit 1")
    int increaseFailedAttemptByOneForHashedIp(String hashedIp);
}
