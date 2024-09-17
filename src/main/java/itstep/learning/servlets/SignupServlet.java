package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.User;
import itstep.learning.models.form.UserSignupFormModel;
import itstep.learning.rest.RestResponse;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Singleton
public class SignupServlet extends HttpServlet {
    private final FormParseService formParseService;
    private final FileService fileService;
    private final UserDao userDao;

    @Inject
    public SignupServlet(FormParseService formParseService, FileService fileService, UserDao userDao) {
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.userDao = userDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute( "page", "signup" );
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestResponse restResponse = new RestResponse();
        resp.setContentType( "application/json" );

        UserSignupFormModel model;
        try {
            model = getModelFromRequest( req );
        }
        catch( Exception ex ) {
            restResponse.setStatus( "Error" );
            restResponse.setData( ex.getMessage() );
            resp.getWriter().print(
                    new Gson().toJson( restResponse )
            );
            return;
        }

        // передаємо на БД
        User user = userDao.signup( model );
        if( user == null ) {
            restResponse.setStatus( "Error" );
            restResponse.setData( "500 DB Error, details on server logs" );
            resp.getWriter().print(
                    new Gson().toJson( restResponse )
            );
            return;
        }

        restResponse.setStatus( "Ok" );
        restResponse.setData( model );
        resp.getWriter().print(
                new Gson().toJson( restResponse )
        );
    }

    private UserSignupFormModel getModelFromRequest( HttpServletRequest req ) throws Exception {
        SimpleDateFormat dateParser =
                new SimpleDateFormat("yyyy-MM-dd");
        FormParseResult res = formParseService.parse( req );

        UserSignupFormModel model = new UserSignupFormModel();

        model.setName( res.getFields().get("user-name") );
        if( model.getName() == null || model.getName().isEmpty() ) {
            throw new Exception( "Missing or empty required field: 'user-name'" );
        }

        model.setEmail( res.getFields().get("user-email") );

        try {
            model.setBirthdate(
                    dateParser.parse(
                            res.getFields().get("user-birthdate")
                    )
            );
        }
        catch( ParseException ex ) {
            throw new Exception( ex.getMessage() );
        }

        // зберігаємо файл-аватарку та одержуємо його збережене ім'я
        String uploadedName = null;
        FileItem avatar = res.getFiles().get( "user-avatar" );
        if( avatar.getSize() > 0 ) {
            uploadedName = fileService.upload( avatar );
            model.setAvatar( uploadedName );
        }
        System.out.println( uploadedName );

        model.setPassword( res.getFields().get( "user-password" ) );
        return model;
    }
}
/*
Д.З. Реалізувати повну валідацію даних від форми реєстрації користувача:
наявність необхідних полів, структуру полів (e-mail, birthdate),
збіг паролів (пароль  та повтор). Формувати повідомлення щодо помилок
у відповідності до їх причини.
 */