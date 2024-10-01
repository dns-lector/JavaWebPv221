package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.rest.RestService;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class ProductServlet  extends HttpServlet {
    private final RestService restService;
    private final FormParseService formParseService;
    private final FileService fileService;

    @Inject
    public ProductServlet(RestService restService, FormParseService formParseService, FileService fileService) {
        this.restService = restService;
        this.formParseService = formParseService;
        this.fileService = fileService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if( req.getAttribute( "Claim.Sid" ) == null ) {
            restService.sendRestError( resp, "Unauthorized. Token empty or rejected" );
            return;
        }
        FormParseResult formParseResult = formParseService.parse( req );
        restService.sendRestResponse( resp,
                "Files: " + formParseResult.getFiles().size() +
                " Fields: " + formParseResult.getFields().size() );
    }
}
