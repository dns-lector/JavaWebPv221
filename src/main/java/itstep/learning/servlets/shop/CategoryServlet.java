package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CategoryDao;
import itstep.learning.models.form.ShopCategoryFormModel;
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

@Singleton
public class CategoryServlet extends HttpServlet {
    private final RestService restService;
    private final FormParseService formParseService;
    private final FileService fileService;
    private final CategoryDao categoryDao;

    @Inject
    public CategoryServlet(RestService restService, FormParseService formParseService, FileService fileService, CategoryDao categoryDao) {
        this.restService = restService;
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.categoryDao = categoryDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        restService.sendRestResponse( resp, categoryDao.all() );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult formParseResult = formParseService.parse( req );
        String name = formParseResult.getFields().get( "category-name" );
        if ( name == null || name.isEmpty() ) {
            restService.sendRestError( resp, "Missing required field: 'category-name'" );
            return;
        }
        String description = formParseResult.getFields().get( "category-description" );
        if ( description == null || description.isEmpty() ) {
            restService.sendRestError( resp, "Missing required field: 'category-description'" );
            return;
        }
        // зберігаємо файл-аватарку та одержуємо його збережене ім'я
        String uploadedName;
        FileItem avatar = formParseResult.getFiles().get( "category-img" );
        if( avatar.getSize() > 0 ) {
            uploadedName = fileService.upload( avatar );
        }
        else {
            restService.sendRestError( resp, "Missing required file: 'category-img'" );
            return;
        }

        restService.sendRestResponse( resp,
                categoryDao.add(
                        new ShopCategoryFormModel()
                                .setName( name )
                                .setDescription( description )
                                .setSavedFilename( uploadedName )
                )
        );
    }
}
/*
Д.З. Використати впроваджені до системи ролі користувачів
Обмежити виведення форми додавання нових товарів
 для користувачів з роллю "Адміністратор"

 */
