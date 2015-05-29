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
import com.samaxes.filter.CacheFilter;
import com.samaxes.filter.NoCacheFilter;
import com.samaxes.filter.NoETagFilter;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.shiro.codec.Base64;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ResourceConfig;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;
import ru.vyarus.guice.ext.ExtAnnotationsModule;
import ru.vyarus.guice.persist.orient.OrientModule;
import ru.vyarus.guice.persist.orient.RepositoryModule;
import ru.vyarus.guice.persist.orient.support.AutoScanSchemeModule;
import ru.vyarus.guice.validator.ImplicitValidationModule;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.eclipse.jetty.server.session.SessionHandler;
import ru.vyarus.guice.persist.orient.db.data.DataInitializer;
import to.sauerkraut.krautadmin.auth.PasswordService;
import to.sauerkraut.krautadmin.auth.Realm;
import to.sauerkraut.krautadmin.auth.SecureShiroBundle;
import to.sauerkraut.krautadmin.core.IO;
import to.sauerkraut.krautadmin.core.crypto.TwofishCipherService;
import to.sauerkraut.krautadmin.db.ApplicationUpgradeManagerAndFixturesLoader;
import to.sauerkraut.krautadmin.db.model.Model;
import to.sauerkraut.krautadmin.jersey.GenericExceptionMapper;
import to.sauerkraut.krautadmin.job.scheduler.ExtendedSchedulerConfiguration;
import to.sauerkraut.krautadmin.job.scheduler.SchedulerModule;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@SuppressWarnings({"checkstyle:classdataabstractioncoupling", "checkstyle:classfanoutcomplexity"})
public class KrautAdminModule extends AbstractModule implements
        EnvironmentAwareModule,
        BootstrapAwareModule<KrautAdminConfiguration>,
        ConfigurationAwareModule<KrautAdminConfiguration> {

    private static final String PATH_MATCH_ALL = "/*";
    private KrautAdminConfiguration configuration;
    private Bootstrap<KrautAdminConfiguration> bootstrap;
    private Environment environment;
    private Application application;

    public KrautAdminModule(final Application application) {
        super();
        this.application = application;
    }

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
        disableResponseCaching();
        disableResponseETag();
        configureCustomResponseCaching();
        // if needed, we could enable cross origin requests
        //enableCrossOriginRequests();
        enableSessions();
        registerCustomExceptionMapper();
        bindPasswordService();
        bindSupplement();
        bindDb();
        bindScheduler();
        bindApplication();
        generateApplicationSecretIfNecessary(bindCipherService());
        requestStaticInjection(Model.class, ApplicationUpgradeManagerAndFixturesLoader.class, Realm.class,
                SecureShiroBundle.class);
    }

    private void generateApplicationSecretIfNecessary(final TwofishCipherService cipherService) {
        if (configuration.getApplicationSecret() == null) {
            final byte[] applicationSecret = cipherService.generateNewKey().getEncoded();
            final String applicationSecretBase64 = Base64.encodeToString(applicationSecret);
            configuration.setApplicationSecret(applicationSecretBase64);
            IO.writeContent(System.lineSeparator() + "applicationSecret: \""
                    + applicationSecretBase64 + "\""
                    + System.lineSeparator(), new File(configuration.getConfigurationPath()), true);
        }
    }

    private TwofishCipherService bindCipherService() {
        final TwofishCipherService cipherService = new TwofishCipherService();
        cipherService.setKeySize(TwofishCipherService.RECOMMENDED_KEY_SIZE);
        cipherService.setMode(TwofishCipherService.RECOMMENDED_OPERATION_MODE);
        cipherService.setModeName(TwofishCipherService.RECOMMENDED_OPERATION_MODE.name());
        cipherService.setPaddingScheme(TwofishCipherService.RECOMMENDED_PADDING_SCHEME);
        cipherService.setGenerateInitializationVectors(true);
        bind(TwofishCipherService.class).toInstance(cipherService);
        return cipherService;
    }

    private void configureCustomResponseCaching() {
        final List<KrautAdminConfiguration.ResponseCachingConfiguration> cachingConfigurations =
                configuration.getResponseCachingConfigurations();

        for (KrautAdminConfiguration.ResponseCachingConfiguration guiCachingConfiguration : cachingConfigurations) {
            final FilterRegistration.Dynamic guiCacheFilter =
                    environment.servlets().addFilter(guiCachingConfiguration.getFilterName(), CacheFilter.class);
            guiCacheFilter.setInitParameters(guiCachingConfiguration.getInitParameters());

            guiCacheFilter.addMappingForUrlPatterns(EnumSet.copyOf(guiCachingConfiguration.getDispatcherTypes()),
                    true, guiCachingConfiguration.getFilterPath());
        }
    }

    private void disableResponseCaching() {
        environment.servlets().addFilter("noCache",
                NoCacheFilter.class).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true,
                PATH_MATCH_ALL);
    }

    private void disableResponseETag() {
        environment.servlets().addFilter("noETag",
                NoETagFilter.class).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true,
                PATH_MATCH_ALL);
    }

    private void enableCrossOriginRequests() {
        environment.servlets().addFilter("crossOrigin",
                CrossOriginFilter.class).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true,
                PATH_MATCH_ALL);
    }

    private void bindApplication() {
        bind(Application.class).toInstance(application);
    }
    
    private void registerCustomExceptionMapper() {
        // Remove all of Dropwizard's custom ExceptionMappers
        final ResourceConfig jrConfig = environment.jersey().getResourceConfig();
        final Set<Object> dwSingletons = jrConfig.getSingletons();
        final List<Object> singletonsToRemove = new ArrayList<>();

        for (Object s : dwSingletons) {
            if (s instanceof ExceptionMapper && s.getClass().getName().startsWith("io.dropwizard.jersey.")) {
                singletonsToRemove.add(s);
            }
        }

        for (Object s : singletonsToRemove) {
            jrConfig.getSingletons().remove(s);
        }

        // Register the custom ExceptionMapper(s)
        environment.jersey().register(new GenericExceptionMapper());
        // the following is currently not needed, as we handle ShiroExceptions in the GenericExceptionMapper as well
        //environment.jersey().register(new ShiroExceptionMapper());
    }
    
    private void enableSessions() {
        final HashSessionManager hashSessionManager = new HashSessionManager();
        hashSessionManager.setSessionIdPathParameterName("none");
        environment.getApplicationContext().setSessionHandler(new SessionHandler(hashSessionManager));
    }
    
    private void bindPasswordService() {
        final KrautAdminConfiguration.SecurityConfiguration securityConfiguration = 
                configuration.getSecurityConfiguration();
        final PasswordService passwordService = new PasswordService();
        final ConfigurableHashService hashService = passwordService.getConfigurableHashService();
        hashService.setHashAlgorithmName(securityConfiguration.getPasswordHashFormat());
        hashService.setHashIterations(securityConfiguration.getPasswordHashIterations());
        bind(PasswordService.class).toInstance(passwordService);
    }
    
    private void bindScheduler() {
        bind(ExtendedSchedulerConfiguration.class).toInstance(configuration.getSchedulerConfiguration());
        install(new SchedulerModule());
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
        bind(DataInitializer.class).to(ApplicationUpgradeManagerAndFixturesLoader.class);
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
