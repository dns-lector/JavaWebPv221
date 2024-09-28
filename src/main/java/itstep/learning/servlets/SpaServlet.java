package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class SpaServlet extends HttpServlet {
    private final Logger logger;
    private final TokenDao tokenDao;

    @Inject
    public SpaServlet(Logger logger, TokenDao tokenDao) {
        this.logger = logger;
        this.tokenDao = tokenDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ~ return View()
        req.setAttribute( "page", "spa" );
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String authHeader = req.getHeader( "Authorization" );
        if( authHeader == null ) {
            sendRestError( resp, "Missing Authorization header" );
            return;
        }
        if( ! authHeader.startsWith( "Bearer " ) ) {
            sendRestError( resp, "Bearer Authorization scheme only" );
            return;
        }
        String token = authHeader.substring( 7 );
        UUID tokenId;
        try {
            tokenId = UUID.fromString( token );
        }
        catch( IllegalArgumentException ex ) {
            logger.warning( ex.getMessage() );
            sendRestError( resp, "Illegal token format" );
            return;
        }
        try {
            User user = tokenDao.getUserByTokenId( tokenId ) ;
            sendRestResponse( resp, user );
        }
        catch( Exception ex ) {
            sendRestError( resp, ex.getMessage() );
        }
    }

    private void sendRestError(HttpServletResponse resp, String message) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus( "Error" );
        restResponse.setData( message );
        sendRest(resp, restResponse);
    }
    private void sendRestResponse(HttpServletResponse resp, Object data) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus( "Ok" );
        restResponse.setData( data );
        sendRest(resp, restResponse);
    }
    private void sendRest(HttpServletResponse resp, RestResponse restResponse) throws IOException {
        resp.setContentType( "application/json" );
        Gson gson = new GsonBuilder().serializeNulls().create();
        resp.getWriter().print( gson.toJson( restResponse ) );
    }
}
/*
Д.З. Забезпечити збереження даних про користувача, які надходять при
одержані токену (автентифікації) та відображати їх на сторінці сайту
(ім'я користувача та/або аватарку)
Після виходу або вичерпання терміну токену прибирати ці дані
 */