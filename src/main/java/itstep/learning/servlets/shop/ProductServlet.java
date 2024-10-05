package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.shop.Product;
import itstep.learning.rest.RestService;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Singleton
public class ProductServlet  extends HttpServlet {
    private final RestService restService;
    private final FormParseService formParseService;
    private final FileService fileService;
    private final ProductDao productDao;

    @Inject
    public ProductServlet(RestService restService, FormParseService formParseService, FileService fileService, ProductDao productDao) {
        this.restService = restService;
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.productDao = productDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String productId = req.getParameter( "id" );
        if( productId != null ) {
            getProductById( productId, req, resp );
            return;
        }

        String categoryId = req.getParameter( "categoryId" );
        if( categoryId != null ) {
            getProductsByCategoryId(categoryId, req, resp);
            return;
        }

        restService.sendRestError( resp, "Missing one of the required parameters: 'id' or 'categoryId'" );
    }

    private void getProductsByCategoryId( String categoryId, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UUID categoryUuid;
        try { categoryUuid = UUID.fromString( categoryId ); }
        catch( IllegalArgumentException ignored ) {
            restService.sendRestError( resp, "Invalid category id: " + categoryId );
            return;
        }
        restService.sendRestResponse( resp, productDao.allFromCategory( categoryUuid ) );
    }

    private void getProductById( String id, HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        Product product = productDao.getProductByIdOrSlug( id );
        if( product != null ) {
            restService.sendRestResponse( resp, product );
        }
        else {
            restService.sendRestError( resp, "Product not found: " + id );
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if( req.getAttribute( "Claim.Sid" ) == null ) {
            restService.sendRestError( resp, "Unauthorized. Token empty or rejected" );
            return;
        }
        try {
            Product product = getModelFromRequest( req ) ;
            product = productDao.add( product );
            if( product == null ) {
                restService.sendRestError( resp, "Server Error" );
            }
            else {
                restService.sendRestResponse( resp, product );
            }
        }
        catch( Exception ex ) {
            restService.sendRestError( resp, ex.getMessage() );
            // return;
        }
    }

    private Product getModelFromRequest( HttpServletRequest req ) throws Exception {
        Product product = new Product();
        FormParseResult formParseResult = formParseService.parse( req );

        String slug = formParseResult.getFields().get( "product-slug" );
        if( slug != null && ! slug.isEmpty() ) {
            slug = slug.trim();
            if( slug.isEmpty() ||
                    ! productDao.isSlugFree( slug ) ) {
                throw new Exception( "Slug '" + slug + "' is empty or not free" );
            }
            product.setSlug( slug );
        }

        try {
            product.setCategoryId(
                    UUID.fromString(
                            formParseResult.getFields().get( "product-category-id" )
                    )
            );
        }
        catch( IllegalArgumentException ignored ) {
            throw new Exception( "Missing or empty or incorrect required field: 'product-category-id'" );
        }

        try {
            product.setPrice(
                    Double.parseDouble(
                            formParseResult.getFields().get( "product-price" )
                    )
            );
        }
        catch( Exception ignored ) {
            throw new Exception( "Missing or empty or incorrect required field: 'product-price'" );
        }

        product.setName( formParseResult.getFields().get( "product-name" ) );
        if( product.getName() == null || product.getName().isEmpty() ) {
            throw new Exception( "Missing or empty required field: 'product-name'" );
        }
        product.setDescription( formParseResult.getFields().get( "product-description" ) );
        if( product.getDescription() == null || product.getDescription().isEmpty() ) {
            throw new Exception( "Missing or empty required field: 'product-description'" );
        }

        FileItem avatar = formParseResult.getFiles().get( "product-img" );
        if( avatar.getSize() > 0 ) {
            int dotPosition = avatar.getName().lastIndexOf(".");
            if( dotPosition == -1 ) {
                throw new Exception( "Rejected file without extension: 'product-img'" );
            }
            String ext = avatar.getName().substring( dotPosition );
            String[] extensions = { ".jpg", ".jpeg", ".png", ".svg", ".bmp" };
            if( Arrays.stream(extensions).noneMatch( (e) -> e.equals(ext) ) ) {
                throw new Exception( "Rejected file with non-image extension: 'product-img'" );
            }
            product.setImageUrl( fileService.upload( avatar ) );
        }
        else {
            throw new Exception( "Missing or empty required file: 'product-img'" );
        }
        return product;
    }
}
