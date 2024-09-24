package itstep.learning.ioc;

import com.google.inject.servlet.ServletModule;
import itstep.learning.filters.*;
import itstep.learning.filters.auth.SessionAuthFilter;
import itstep.learning.servlets.*;

public class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        // Третій, рекомендований спосіб реєстрації фільтрів ...
        filter( "/*" ).through( CharsetFilter.class );
        filter( "/*" ).through( SessionAuthFilter.class );

        // ... та сервлетів
        serve( "/"         ).with( HomeServlet.class     );
        serve( "/auth"     ).with( AuthServlet.class     );
        serve( "/file/*"   ).with( DownloadServlet.class );
        serve( "/servlets" ).with( ServletsServlet.class );
        serve( "/signup"   ).with( SignupServlet.class   );
        serve( "/spa"      ).with( SpaServlet.class      );

        // !! не забути зняти з фільтрів/сервлетів анотації @Web...
        // !! та додати анотації @Singleton
    }
}
