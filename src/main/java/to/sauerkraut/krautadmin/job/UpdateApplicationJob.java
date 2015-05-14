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
import com.google.inject.Inject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@DisallowConcurrentExecution
@Scheduled(interval = 10, unit = TimeUnit.MINUTES)
public class UpdateApplicationJob implements org.quartz.Job {
    
    @Log
    private static Logger logger;
    @Inject
    private KrautAdminConfiguration configuration;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        logger.info("Application location: {}", configuration.getApplicationLocation());
        logger.info("Configuration path: {}", configuration.getConfigurationPath());
        logger.info(".jar name: {}", configuration.getJarName() == null ? "<not in .jar>" : configuration.getJarName());
        logger.info("Application release: {}", configuration.getRelease());
    }
}
