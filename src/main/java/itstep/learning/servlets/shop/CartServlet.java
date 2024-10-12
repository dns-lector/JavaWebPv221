package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.stream.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

@Singleton
public class CartServlet extends RestServlet {
    private final CartDao cartDao;
    private final StringReader stringReader;
    private final Logger logger;

    @Inject
    public CartServlet(CartDao cartDao, StringReader stringReader, Logger logger) {
        this.cartDao = cartDao;
        this.stringReader = stringReader;
        this.logger = logger;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse().setMeta(
                new RestMetaData()
                        .setUri( "/shop/cart" )
                        .setMethod( req.getMethod() )
                        .setLocale( "uk-UA" )
                        .setServerTime( new Date() )
                        .setName( "Shop Cart API" )
                        .setAcceptMethods(new String[]{ "GET", "POST", "PUT", "DELETE" })
        );
        super.service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if( ! req.getContentType().startsWith( "application/json" ) ) {
            super.sendRest( 415, "'application/json' expected" );
        }
        String jsonString;
        try {
            jsonString = stringReader.read( req.getInputStream() );
        }
        catch( IOException ex ) {
            logger.warning( ex.getMessage() );
            super.sendRest( 400, "JSON could not be extracted" );
            return;
        }
        super.sendRest( 201, jsonString );
    }
}
