package com.ijunhai.resource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ijunhai.exception.Exceptions;
import com.ijunhai.model.ModelProcessor;
import com.ijunhai.model.QueryModel;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static com.ijunhai.contants.ProxyConstants.JSON_MAPPER;

@Path("/")
public class QueryResource {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query")
    public String queryPost(String modelStr) throws Exception {
        try {
            QueryModel model = JSON_MAPPER.readValue(modelStr, QueryModel.class);
            ModelProcessor modelProcessor = new ModelProcessor(model);
            List resultList = modelProcessor.process();
            return JSON_MAPPER.writeValueAsString(resultList);
        } catch (JsonParseException | JsonMappingException ex) {
            throw new Exceptions.JsonFormatException(ex);
        }
    }
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("test")
    public String test() {
        return "server is ok";
    }

}
