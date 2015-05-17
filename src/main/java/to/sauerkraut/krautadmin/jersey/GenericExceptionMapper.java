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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EXCEPTION_NAME_KEY = "exceptionName";
    private static final String EXCEPTION_MESSAGE_KEY = "message";
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
        final Map<String, String> responseMap = new HashMap<>();
        responseMap.put(EXCEPTION_NAME_KEY, exception.getClass().getSimpleName());
        responseMap.put(EXCEPTION_MESSAGE_KEY, exception.getMessage());

        try {
            return MAPPER.writeValueAsString(responseMap);
        } catch (JsonProcessingException e) {
            return "{\"" + EXCEPTION_NAME_KEY + "\":\"JsonProcessingException\", "
                    + "\"" + EXCEPTION_MESSAGE_KEY + "\":\"An internal error occurred\"}";
        }
    }
}
