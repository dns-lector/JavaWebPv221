package itstep.learning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class RestService {
    public void sendRestError( HttpServletResponse resp, String message ) throws IOException {
        sendRestError( resp, 400, message );
    }
    public void sendRestError( HttpServletResponse resp, int code, String message ) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus( new RestResponseStatus( code ) );
        restResponse.setData( message );
        sendRest( resp, restResponse );
    }
    public void sendRestResponse(HttpServletResponse resp, Object data) throws IOException {
        sendRestResponse( resp, 200, data );
    }
    public void sendRestResponse(HttpServletResponse resp, int code, Object data) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus( new RestResponseStatus( code ) );
        restResponse.setData( data );
        sendRest( resp, restResponse );
    }
    public void sendRest(HttpServletResponse resp, RestResponse restResponse) throws IOException {
        resp.setContentType( "application/json" );
        Gson gson = new GsonBuilder().serializeNulls().create();
        resp.getWriter().print( gson.toJson( restResponse ) );
    }
}
