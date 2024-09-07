package itstep.learning.servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/signup")
public class SignupServlet extends HomeServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute( "page", "signup" );
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
/*
Д.З. У файлі-шаблоні (_layout) реалізувати навігаційну панель з
посиланнями на домашню сторінку, сторінку про JSP (переробити
index.jsp під сервлет з представленням), сторінку про сервлети.
Реалізувати "підсвітку" (<li class="active">) навігатора активної сторінки
 */