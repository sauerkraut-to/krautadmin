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
package to.sauerkraut.krautadmin.resources;

import javax.inject.Inject;
import to.sauerkraut.krautadmin.KrautAdminConfiguration;
import to.sauerkraut.krautadmin.client.dto.GenericResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
@Path("/application")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    @Inject
    private KrautAdminConfiguration configuration;

    @GET
    @Path("/loginDelayMilliseconds")
    public GenericResponse<Long> getLoginDelayMilliseconds() {
        return new GenericResponse<>(configuration.getSecurityConfiguration().getLoginDelayMilliseconds());
    }

    @POST
    @Path("/keepAlive")
    public GenericResponse<String> keepAlive() {
        return new GenericResponse<>();
    }
}