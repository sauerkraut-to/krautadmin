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
package to.sauerkraut.krautadmin.job;

import com.fiestacabin.dropwizard.quartz.Scheduled;
import javax.inject.Inject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;
import to.sauerkraut.krautadmin.db.repository.LoginAttemptRepository;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@DisallowConcurrentExecution
@Scheduled(interval = 1, unit = TimeUnit.MINUTES)
public class DeleteOldLoginAttemptsJob implements org.quartz.Job {

    @Inject
    private LoginAttemptRepository loginAttemptRepository;
    @Inject
    private KrautAdminConfiguration configuration;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final int banDays = configuration.getSecurityConfiguration().getBanDays();
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -banDays);
        loginAttemptRepository.deleteOlderThan(cal.getTimeInMillis());
    }
}
