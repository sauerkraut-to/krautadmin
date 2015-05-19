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
package to.sauerkraut.krautadmin.jersey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import to.sauerkraut.krautadmin.client.dto.ExceptionDetails;
import to.sauerkraut.krautadmin.client.dto.GenericResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Response toResponse(final Exception exception) {
        if (exception instanceof WebApplicationException) {
            final WebApplicationException webApplicationException = (WebApplicationException) exception;
            final Response webApplicationExceptionResponse = webApplicationException.getResponse();
            return Response.status(webApplicationExceptionResponse.getStatus())
                    .entity(defaultJSON(webApplicationException))
                    .type(MediaType.APPLICATION_JSON).build();
        } else if (exception instanceof AuthenticationException) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(defaultJSON(exception))
                    .type(MediaType.APPLICATION_JSON).build();
        } else if (exception instanceof ShiroException) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(defaultJSON(exception))
                    .type(MediaType.APPLICATION_JSON).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(defaultJSON(exception))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        //TODO: handle lack of permissions exception
    }

    private String defaultJSON(final Exception exception) {
        final ExceptionDetails exceptionDetails = new ExceptionDetails(
                String.valueOf(exception.getMessage()).concat("."), exception.getClass().getSimpleName());
        try {
            return MAPPER.writeValueAsString(new GenericResponse<>(null, exceptionDetails));
        } catch (JsonProcessingException e) {
            return "{\"success\": false, \"exception\": {\"exceptionName\": \"JsonProcessingException\", "
                    + "\"message\": \"Entschuldigung, ein interner Fehler ist aufgetreten.\"}, \"payload\": null}";
        }
    }
}
