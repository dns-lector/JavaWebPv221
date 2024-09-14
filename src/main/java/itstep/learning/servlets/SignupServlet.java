package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.models.form.UserSignupFormModel;
import itstep.learning.rest.RestResponse;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;

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

    @Inject
    public SignupServlet( FormParseService formParseService ) {
        this.formParseService = formParseService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute( "page", "signup" );
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SimpleDateFormat dateParser =
                new SimpleDateFormat("yyyy-MM-dd");
        RestResponse restResponse = new RestResponse();
        resp.setContentType( "application/json" );

        FormParseResult res = formParseService.parse( req );

        UserSignupFormModel model = new UserSignupFormModel();

        model.setName( res.getFields().get("user-name") );
        if( model.getName() == null || model.getName().isEmpty() ) {
            restResponse.setStatus( "Error" );
            restResponse.setData( "Missing or empty required field: 'user-name'" );
            resp.getWriter().print(
                    new Gson().toJson( restResponse )
            );
            return;
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
            restResponse.setStatus( "Error" );
            restResponse.setData( ex.getMessage() );
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
}
/*
Д.З. Реалізувати повну валідацію даних від форми реєстрації користувача:
наявність необхідних полів, структуру полів (e-mail, birthdate),
збіг паролів (пароль  та повтор). Формувати повідомлення щодо помилок
у відповідності до їх причини.
 */