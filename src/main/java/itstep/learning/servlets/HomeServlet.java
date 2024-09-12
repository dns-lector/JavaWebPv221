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
import java.sql.Connection;

@Singleton
public class HomeServlet extends HttpServlet {
    private final HashService hashService;
    private final Connection connection;

    @Inject
    public HomeServlet(@Named("digest") HashService hashService, Connection connection) {
        this.hashService = hashService;
        this.connection = connection;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute( "hash",
                connection == null ? "NO connection" : "Connection OK" );
        // ~ return View()
        req.setAttribute( "page", "home" );
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
/*
Д.З. Після надсилання форми реєстрації користувача вивести (замість
порожньої сторінки) передані дані (для файлу - ім'я та *розмір)
 */