package itstep.learning.filters.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@Singleton
public class SessionAuthFilter implements Filter {
    private final UserDao userDao;

    @Inject
    public SessionAuthFilter(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        UUID userId = (UUID) session.getAttribute("userId");
        if (userId != null) {
            User user = userDao.getUserById( userId ) ;
            if( user != null ) {
                req.setAttribute("Claim.Sid", userId);
                req.setAttribute("Claim.Name", user.getName());
                req.setAttribute("Claim.Avatar", user.getAvatar());
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
