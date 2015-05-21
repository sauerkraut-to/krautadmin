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
package to.sauerkraut.krautadmin.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 * @param <T> the type of the response's payload
 */
public class GenericResponse<T> {
    @JsonProperty("exception")
    private ExceptionDetails exceptionDetails;
    @JsonProperty
    private T payload;
    @JsonProperty
    private final List<ConstraintViolation> constraintViolations;

    public GenericResponse() {
        constraintViolations = new ArrayList<>();
    }

    public GenericResponse(final T payload) {
        this(payload, null);
    }

    public GenericResponse(final T payload, final ExceptionDetails exceptionDetails) {
        this();
        this.payload = payload;
        this.exceptionDetails = exceptionDetails;
    }

    @JsonProperty("success")
    public boolean isSuccess() {
        return exceptionDetails == null;
    }

    public ExceptionDetails getExceptionDetails() {
        return exceptionDetails;
    }

    public void setExceptionDetails(final ExceptionDetails exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(final T payload) {
        this.payload = payload;
    }

    public List<ConstraintViolation> getConstraintViolations() {
        return constraintViolations;
    }

    /**
     * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
     */
    public static final class ConstraintViolation {
        @JsonProperty
        private String propertyPath;
        @JsonProperty
        private String message;
        @JsonProperty
        private String className;
        @JsonProperty
        private String classNameSimple;

        public ConstraintViolation(final String propertyPath, final String message, final String className,
                                   final String classNameSimple) {
            this.propertyPath = propertyPath;
            this.message = message;
            this.className = className;
            this.classNameSimple = classNameSimple;
        }

        public String getPropertyPath() {
            return propertyPath;
        }

        public void setPropertyPath(final String propertyPath) {
            this.propertyPath = propertyPath;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(final String message) {
            this.message = message;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(final String className) {
            this.className = className;
        }

        public String getClassNameSimple() {
            return classNameSimple;
        }

        public void setClassNameSimple(final String classNameSimple) {
            this.classNameSimple = classNameSimple;
        }
    }
}
