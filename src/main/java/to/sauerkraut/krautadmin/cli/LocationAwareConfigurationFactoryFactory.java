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
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;

import javax.validation.Validator;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 * @param <T> the configuration class
 */
public class LocationAwareConfigurationFactoryFactory<T> implements ConfigurationFactoryFactory<T> {
    @Override
    public ConfigurationFactory<T> create(final Class<T> klass, final Validator validator,
                                          final ObjectMapper objectMapper, final String propertyPrefix) {
        return new LocationAwareConfigurationFactory<>(klass, validator, objectMapper, propertyPrefix);
    }
}
