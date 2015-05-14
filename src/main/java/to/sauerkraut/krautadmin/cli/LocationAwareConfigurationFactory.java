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
package to.sauerkraut.krautadmin.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import to.sauerkraut.krautadmin.core.Toolkit;

import javax.validation.Validator;
import java.io.IOException;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 * @param <T> the configuration class
 */
public class LocationAwareConfigurationFactory<T> extends ConfigurationFactory<T> {
    /**
     * Creates a new configuration factory for the given class.
     *
     * @param klass          the configuration class
     * @param validator      the validator to use
     * @param objectMapper   the Jackson {@link ObjectMapper} to use
     * @param propertyPrefix the system property name prefix used by overrides
     */
    public LocationAwareConfigurationFactory(final Class<T> klass, final Validator validator,
                                             final ObjectMapper objectMapper, final String propertyPrefix) {
        super(klass, validator, objectMapper, propertyPrefix);
    }

    @Override
    public T build(final ConfigurationSourceProvider provider, final String path)
            throws IOException, ConfigurationException {

        final T config = super.build(provider, path);

        if (config instanceof ApplicationLocationAware) {
            final ApplicationLocationAware c1 = (ApplicationLocationAware) config;
            try {
                c1.setApplicationLocation(Toolkit.getApplicationContainingFolder());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        if (config instanceof ConfigurationLocationAware) {
            final ConfigurationLocationAware c2 = (ConfigurationLocationAware) config;
            c2.setConfigurationLocation(path);
        }

        return config;
    }
}
