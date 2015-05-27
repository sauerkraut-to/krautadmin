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
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.sauerkraut.krautadmin.auth.Realm;
import to.sauerkraut.krautadmin.client.dto.ExceptionDetails;
import to.sauerkraut.krautadmin.client.dto.GenericResponse;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 */
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class);
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
            Exception translatedException = exception;
            if (exception instanceof IncorrectCredentialsException) {
                translatedException = new IncorrectCredentialsException(Realm.ERROR_WRONG_CREDENTIALS);
            }
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(defaultJSON(translatedException))
                    .type(MediaType.APPLICATION_JSON).build();
        } else if (exception instanceof ShiroException) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(defaultJSON(exception))
                    .type(MediaType.APPLICATION_JSON).build();
        } else if (exception instanceof ConstraintViolationException) {
            final ConstraintViolationException constraintViolationException = (ConstraintViolationException) exception;
            final List<GenericResponse.ConstraintViolation> constraintViolations = new ArrayList<>();
            for (ConstraintViolation violation : constraintViolationException.getConstraintViolations()) {
                final String propertyPath = violation.getPropertyPath().toString();
                final String message = violation.getMessage();
                final String className = violation.getRootBeanClass().getCanonicalName();
                final String classNameSimple = violation.getRootBeanClass().getSimpleName();
                constraintViolations.add(
                        new GenericResponse.ConstraintViolation(propertyPath, message, className, classNameSimple));
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(defaultJSON(exception, constraintViolations))
                    .type(MediaType.APPLICATION_JSON).build();
        } else {
            LOG.debug(exception.getMessage(), exception);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(defaultJSON(exception))
                    .type(MediaType.APPLICATION_JSON).build();
        }
        //TODO: handle lack of permissions exception
        //TODO: render NotFoundException as HTML
    }

    private String defaultJSON(final Exception exception) {
        return defaultJSON(exception, null);
    }

    private String defaultJSON(final Exception exception,
                               final List<GenericResponse.ConstraintViolation> constraintViolations) {
        final ExceptionDetails exceptionDetails = new ExceptionDetails(
                String.valueOf(exception.getMessage()).concat("."), exception.getClass().getSimpleName());
        final GenericResponse genericResponse = new GenericResponse<>(null, exceptionDetails);

        if (constraintViolations != null) {
            genericResponse.getConstraintViolations().addAll(constraintViolations);
        }

        try {
            return MAPPER.writeValueAsString(new GenericResponse<>(null, exceptionDetails));
        } catch (JsonProcessingException e) {
            return "{\"success\": false, \"exception\": {\"exceptionName\": \"JsonProcessingException\", "
                    + "\"message\": \"Entschuldigung, ein interner Fehler ist aufgetreten.\"}, \"payload\": null}";
        }
    }
}
