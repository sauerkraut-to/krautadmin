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
import java.io.IOException;
import java.util.HashMap;
import org.jdownloader.plugins.controller.PluginClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.orient.OrientServerBundle;
import to.sauerkraut.krautadmin.db.setup.DatabaseAutoCreationBundle;
import to.sauerkraut.krautadmin.core.Toolkit;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class KrautAdminApplication extends Application<KrautAdminConfiguration> {
    
    private static String jarFolder;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public static String getJarFolder() {
        return jarFolder;
    }
          
    /**
     * This is the application's entry point.
     * @param args Execute the fat jar without any parameters to see 
     * the available start parameters. Also pass config file path.
     * @throws Exception 
     */
    public static void main(final String[] args) throws Exception {
        try {
            jarFolder = Toolkit.getJarContainingFolder();
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        new KrautAdminApplication().run(args);
    }

    @Override
    public String getName() {
        return "KrautAdmin";
    }

    @Override
    public void initialize(final Bootstrap<KrautAdminConfiguration> bootstrap) {
        // disable jd dynamic libraries, as we include everything we need via maven
        // TODO: throw / capsule?
        try {
            Toolkit.setFinalStaticField(PluginClassLoader.class.getDeclaredField("DYNAMIC_LOADABLE_LOBRARIES"), 
                    new HashMap<String, String>());
        } catch (Exception ex) {
            logger.error("could not disable some jd automatism - app start is going to fail", ex);
        }
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html", "client"));
        bootstrap.addBundle(new DatabaseAutoCreationBundle());
        bootstrap.addBundle(new OrientServerBundle(getConfigurationClass()));
        bootstrap.addBundle(GuiceBundle.<KrautAdminConfiguration>builder()
                .modules(new KrautAdminModule())
                .enableAutoConfig(getClass().getPackage().getName())
                .searchCommands(true)
                .build());
    }

    @Override
    public void run(final KrautAdminConfiguration configuration,
                    final Environment environment) {
        
    }
}
