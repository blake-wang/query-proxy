import com.fasterxml.jackson.core.JsonProcessingException;
import com.ijunhai.exception.Exceptions;
import com.ijunhai.model.QueryModel;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static com.ijunhai.contants.ProxyConstants.JSON_MAPPER;

@Path("test")
public class TestResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getMessage(String model) throws Exception {

        try {
            QueryModel model1 = JSON_MAPPER.readValue(model, QueryModel.class);
            String s = JSON_MAPPER.writeValueAsString(model1);
            return s;
        } catch (JsonProcessingException e) {
            throw new Exceptions.JsonFormatException(e);
        } catch (IOException e) {
            throw new Exceptions.JsonFormatException(e);
        }
    }
}