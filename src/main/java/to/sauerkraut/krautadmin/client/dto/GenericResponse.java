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

/**
 * @author sauerkraut.to <gutsverwalter@sauerkraut.to>
 * @param <T> the type of the response's payload
 */
public class GenericResponse<T> {
    @JsonProperty("exception")
    private ExceptionDetails exceptionDetails;
    private T payload;

    public GenericResponse() {

    }

    public GenericResponse(final T payload) {
        this.payload = payload;
    }

    public GenericResponse(final T payload, final ExceptionDetails exceptionDetails) {
        this.payload = payload;
        this.exceptionDetails = exceptionDetails;
    }

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
}
