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
package to.sauerkraut.krautadmin;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.palominolabs.metrics.guice.MetricsInstrumentationModule;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;
import ru.vyarus.guice.ext.ExtAnnotationsModule;
import ru.vyarus.guice.persist.orient.OrientModule;
import ru.vyarus.guice.persist.orient.RepositoryModule;
import ru.vyarus.guice.persist.orient.db.data.DataInitializer;
import ru.vyarus.guice.persist.orient.support.AutoScanSchemeModule;
import ru.vyarus.guice.validator.ImplicitValidationModule;
import to.sauerkraut.krautadmin.db.DataBootstrap;

import javax.ws.rs.Path;
import java.lang.reflect.AnnotatedElement;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class KrautAdminModule extends AbstractModule implements
        EnvironmentAwareModule,
        BootstrapAwareModule<KrautAdminConfiguration>,
        ConfigurationAwareModule<KrautAdminConfiguration> {

    private KrautAdminConfiguration configuration;
    private Bootstrap<KrautAdminConfiguration> bootstrap;
    private Environment environment;

    @Override
    public void setConfiguration(final KrautAdminConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setBootstrap(final Bootstrap<KrautAdminConfiguration> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bindSupplement();
        bindDb();
    }

    private void bindDb() {
        final KrautAdminConfiguration.DatabaseConfiguration db = configuration.getDatabaseConfiguration();
        final String pkg = configuration.getClass().getPackage().getName();
        
        install(new OrientModule(db.getUri(), db.getUser(), db.getPass())
                .autoCreateLocalDatabase(false));
        // auto scan classpath for entities annotated with @Persistent
        install(new AutoScanSchemeModule(pkg));
        install(new RepositoryModule());

        // not required, but using this feature of persist-orient to fill database
        bind(DataInitializer.class).to(DataBootstrap.class);
    }

    private void bindSupplement() {
        // exclude resources, because dropwizard handles validation manually there
        install(new ImplicitValidationModule(bootstrap.getValidatorFactory())
                .withMatcher(Matchers.not(Matchers.annotatedWith(Path.class))));

        final String pkg = configuration.getClass().getPackage().getName();
        // enables @PostConstruct @PreDestroy and @Log annotations
        install(new ExtAnnotationsModule(pkg));

        bindMetrics();
    }

    private void bindMetrics() {
        // exclude resources, because dropwizard will handle them manually
        final Matcher<? super TypeLiteral<?>> matcher = new AbstractMatcher<TypeLiteral<?>>() {
            private final Matcher<AnnotatedElement> actualMatcher = Matchers.not(Matchers.annotatedWith(Path.class));

            @Override
            public boolean matches(final TypeLiteral<?> typeLiteral) {
                return actualMatcher.matches(typeLiteral.getRawType());
            }
        };
        install(new MetricsInstrumentationModule(environment.metrics(), matcher));
    }
}
