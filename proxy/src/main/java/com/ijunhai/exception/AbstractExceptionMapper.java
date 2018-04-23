package com.ijunhai.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static com.ijunhai.contants.ProxyConstants.JSON_MAPPER;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

public abstract class AbstractExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

    protected abstract Exceptions.ExceptionEnum getExceptionEnum();

    @Override
    public Response toResponse(E e) {
        Exceptions.ExceptionEnum exceptionEnum = getExceptionEnum();
        ExceptionMessage exceptionMessage = new ExceptionMessage(exceptionEnum.getCode(), exceptionEnum.getError(), e.getMessage());
        try {
            String entity = JSON_MAPPER.writeValueAsString(exceptionMessage);
            return Response.status(1404).type(TEXT_PLAIN_TYPE).entity(entity).build();
        } catch (Exception ex) {
            return Response.status(500).type(TEXT_PLAIN_TYPE).entity("server error when return exception").build();
        }
    }

}
