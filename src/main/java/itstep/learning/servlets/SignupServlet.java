package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        FormParseResult res = formParseService.parse( req );
        System.out.println( res.getFields().size() + " " + res.getFiles().size() );
        System.out.println( res.getFields().toString() );
    }
}
/*
Д.З. У файлі-шаблоні (_layout) реалізувати навігаційну панель з
посиланнями на домашню сторінку, сторінку про JSP (переробити
index.jsp під сервлет з представленням), сторінку про сервлети.
Реалізувати "підсвітку" (<li class="active">) навігатора активної сторінки
 */