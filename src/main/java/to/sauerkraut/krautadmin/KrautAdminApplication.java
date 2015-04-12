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
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.orient.OrientServerBundle;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class KrautAdminApplication extends Application<KrautAdminConfiguration> {
          
    /**
     * This is the application's entry point.
     * @param args Execute the fat jar without any parameters to see 
     * the available start parameters. Also pass config file path.
     * @throws Exception 
     */
    public static void main(final String[] args) throws Exception {
        new KrautAdminApplication().run(args);
    }

    @Override
    public String getName() {
        return "KrautAdmin";
    }

    @Override
    public void initialize(final Bootstrap<KrautAdminConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html", "client"));
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
