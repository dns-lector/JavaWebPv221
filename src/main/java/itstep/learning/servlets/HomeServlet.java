package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import itstep.learning.services.hash.HashService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class HomeServlet extends HttpServlet {
    private final HashService hashService;

    @Inject
    public HomeServlet( @Named("digest") HashService hashService ) {
        this.hashService = hashService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute( "hash",
                hashService.digest( "123" ) + "<br/>" +
                hashService.hashCode() + "<br/>" +
                this.hashCode() );
        // ~ return View()
        req.setAttribute( "page", "home" );
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
/*
Д.З. Створити фільтр - контроль проходження
Суть: у фільтрі закладається у request певний атрибут, наприклад,
"control" з довільним значенням. Сервлети перевіряють, чи є
в запиті відповідний атрибут, якщо є, то працюють, якщо ні, то
доступ вважається нелегальним.
На Home сервлеті вивести дані щодо контролю (пройдений / ні)
а також інжектувати хеш-підпис та вивести його значення для рядка "123"
 */