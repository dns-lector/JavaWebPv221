package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.Token;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

@Singleton
public class AuthServlet extends HttpServlet {
    private final Logger logger;
    private final UserDao userDao;
    private final TokenDao tokenDao;
    private final RestService restService;

    @Inject
    public AuthServlet(Logger logger, UserDao userDao, TokenDao tokenDao, RestService restService) {
        this.logger = logger;
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.restService = restService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String authHeader = req.getHeader( "Authorization" );
        if( authHeader == null ) {
            restService.sendRestError( resp, "Missing Authorization header" );
            return;
        }
        if( ! authHeader.startsWith( "Basic " ) ) {
            restService.sendRestError( resp, "Basic Authorization scheme only" );
            return;
        }
        String credentials64 = authHeader.substring( 6 );
        String credentials;
        try {
            credentials = new String(
                    Base64.getUrlDecoder().decode( credentials64 )
            );
        }
        catch( IllegalArgumentException ex ) {
            logger.warning( ex.getMessage() );
            restService.sendRestError( resp, "Illegal Credential format" );
            return;
        }
        String[] parts = credentials.split( ":", 2 );
        User user = userDao.authenticate( parts[0], parts[1] );
        if( user == null ) {
            restService.sendRestError( resp, "Invalid username or password" );
            return;
        }
        Token token = tokenDao.create( user ) ;
        restService.sendRestResponse( resp, token );
    }

}
