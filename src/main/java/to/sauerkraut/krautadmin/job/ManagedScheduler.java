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

import com.fiestacabin.dropwizard.quartz.GuiceJobFactory;
import com.fiestacabin.dropwizard.quartz.Scheduled;
import io.dropwizard.lifecycle.Managed;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import static org.quartz.TriggerBuilder.newTrigger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class ManagedScheduler implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(ManagedScheduler.class);

    private final Scheduler scheduler;
    private final GuiceJobFactory jobFactory;
    private final ExtendedSchedulerConfiguration config;

    @Inject
    public ManagedScheduler(final Scheduler scheduler, final GuiceJobFactory jobFactory,
            final ExtendedSchedulerConfiguration config) {
        this.scheduler = scheduler;
        this.jobFactory = jobFactory;
        this.config = config;
    }

    @Override
    public void start() throws Exception {
        scheduler.setJobFactory(jobFactory);
        scheduler.start();

        final Reflections reflections = new Reflections(config.getBasePackage(), new SubTypesScanner());
        final Set<Class<? extends Job>> scheduledClasses = reflections.getSubTypesOf(Job.class);

        for (Class<? extends Job> scheduledClass : scheduledClasses) {
            final Scheduled scheduleAnn = scheduledClass
                    .getAnnotation(Scheduled.class);
            if (scheduleAnn != null) {
                final JobDetail job = newJob(scheduledClass).build();
                final Trigger trigger = buildTrigger(scheduleAnn);

                LOG.info("Scheduled job {} with trigger {}", job, trigger);
                scheduler.scheduleJob(job, trigger);
            }
        }
    }

    public Trigger buildTrigger(final Scheduled ann) {
        final TriggerBuilder<Trigger> trigger = newTrigger();

        if (ann.cron() != null && ann.cron().trim().length() > 0) {
            trigger.withSchedule(CronScheduleBuilder.cronSchedule(ann.cron()).inTimeZone(config.getTimezone()));
        } else if (ann.interval() != -1) {
            final Calendar intervalStartCalendar = Calendar.getInstance();
            intervalStartCalendar.add(Calendar.SECOND, config.getIntervalInitialDelaySeconds());

            trigger.withSchedule(simpleSchedule()
                    .withIntervalInMilliseconds(
                            TimeUnit.MILLISECONDS.convert(ann.interval(), ann.unit()))
                    .repeatForever()).startAt(intervalStartCalendar.getTime());
        } else {
            throw new IllegalArgumentException("One of 'cron', 'interval' is required for the @Scheduled annotation");
        }

        return trigger.build();
    }

    @Override
    public void stop() throws Exception {
        scheduler.shutdown();
    }

}
