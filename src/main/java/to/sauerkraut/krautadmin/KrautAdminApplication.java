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

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javassist.CtClass;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.realm.Realm;
import org.appwork.exceptions.WTFException;
import org.jdownloader.plugins.controller.PluginClassLoader;
import org.secnod.dropwizard.shiro.ShiroBundle;
import org.secnod.dropwizard.shiro.ShiroConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.orient.OrientServerBundle;
import to.sauerkraut.krautadmin.db.setup.DatabaseAutoCreationBundle;
import to.sauerkraut.krautadmin.core.Toolkit;
import to.sauerkraut.krautadmin.db.repository.UserRepository;

import static javassist.ClassPool.getDefault;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public class KrautAdminApplication extends Application<KrautAdminConfiguration> {
    private static String applicationContainingFolder;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public static String getApplicationContainingFolder() {
        return applicationContainingFolder;
    }
          
    /**
     * This is the application's entry point.
     * @param args Execute the fat jar without any parameters to see 
     * the available start parameters. Also pass config file path.
     * @throws Exception 
     */
    public static void main(final String[] args) throws Exception {
        try {
            applicationContainingFolder = Toolkit.getApplicationContainingFolder();
            final CtClass jdUtilsMainClass = getDefault().get("org.appwork.utils.Application");
            final CtClass[] methodParamsGetRootByClass = new CtClass[]{
                    getDefault().get(Class.class.getCanonicalName()),
                    getDefault().get(String.class.getCanonicalName()),
            };
            final CtClass[] methodParamsGetRoot = new CtClass[]{
                    getDefault().get(Class.class.getCanonicalName()),
            };
            final String nl = "\n";
            Toolkit.modifyByteCode(jdUtilsMainClass, "getRoot", methodParamsGetRoot, "{ " + nl
                    + "return " + KrautAdminApplication.class.getCanonicalName()
                    + ".getApplicationContainingFolder();" + nl
                    + "} ", false);
            Toolkit.modifyByteCode(jdUtilsMainClass, "getRootByClass", methodParamsGetRootByClass, "{  " + nl
                    + "try {" + nl
                    + "            java.io.File appRoot = "
                    + "new java.io.File(getRoot(Class.forName(\""
                    + KrautAdminApplication.class.getCanonicalName() + "\")));" + nl + nl
                    + "            if (appRoot.isFile()) {" + nl
                    + "                appRoot = appRoot.getParentFile();" + nl
                    + "            }" + nl
                    + "            if ($2 != null) {" + nl
                    + "                return new java.io.File(appRoot, $2);" + nl
                    + "            } " + nl
                    + "            return appRoot;" + nl
                    + "        } catch (Exception e) {" + nl
                    + "            return null;" + nl
                    + "        }" + nl
                    + "  }", false);
            // load modified JD Utils class
            final Class modifiedJdUtilAppClass = jdUtilsMainClass.toClass();
            // modify additional JD fields
            Toolkit.setPrivateStaticField(modifiedJdUtilAppClass.getDeclaredField("ROOT"),
                    applicationContainingFolder);
            Toolkit.setPrivateStaticField(modifiedJdUtilAppClass.getDeclaredField("IS_JARED"),
                    Boolean.TRUE);
            // disable jd dynamic libraries, as we include everything we need via maven
            Toolkit.setFinalStaticField(PluginClassLoader.class.getDeclaredField("DYNAMIC_LOADABLE_LOBRARIES"),
                    new HashMap<String, String>());
        } catch (Exception ex) {
            throw new WTFException("could not override or instantiate one or "
                    + "more JD statics - app start is going to fail", ex);
        }
        new KrautAdminApplication().run(args);
    }

    @Override
    public String getName() {
        return "KrautAdmin";
    }

    @Override
    public void initialize(final Bootstrap<KrautAdminConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html", "client"));
        bootstrap.addBundle(new DatabaseAutoCreationBundle());
        bootstrap.addBundle(new OrientServerBundle(getConfigurationClass()));
        bootstrap.addBundle(GuiceBundle.<KrautAdminConfiguration>builder()
                .modules(new KrautAdminModule())
                .enableAutoConfig(getClass().getPackage().getName())
                .searchCommands(true)
                .build());
        bootstrap.addBundle(new ShiroBundle<KrautAdminConfiguration>() {
            @Override
            protected ShiroConfiguration narrow(final KrautAdminConfiguration configuration) {
                return configuration.getShiroConfiguration();
            }
            @Override
            protected Collection<Realm> createRealms(final KrautAdminConfiguration configuration) {
                final KrautAdminConfiguration.SecurityConfiguration securityConfiguration = 
                        configuration.getSecurityConfiguration();
                final HashedCredentialsMatcher hashedCredentialsMatcher = 
                        new HashedCredentialsMatcher(securityConfiguration.getPasswordHashFormat());
                hashedCredentialsMatcher.setHashIterations(securityConfiguration.getPasswordHashIterations());
                hashedCredentialsMatcher.setStoredCredentialsHexEncoded(false);
                final to.sauerkraut.krautadmin.auth.Realm r = 
                        new to.sauerkraut.krautadmin.auth.Realm(hashedCredentialsMatcher);
                final UserRepository userRepository = GuiceBundle.getInjector().getInstance(UserRepository.class);
                r.setUserRepository(userRepository);
                return Collections.singleton((Realm) r);
            }
        });
    }

    @Override
    public void run(final KrautAdminConfiguration configuration,
                    final Environment environment) {
        
    }
}
