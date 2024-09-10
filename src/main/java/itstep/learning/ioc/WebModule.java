package itstep.learning.ioc;

import com.google.inject.servlet.ServletModule;
import itstep.learning.filters.*;
import itstep.learning.servlets.*;

public class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        // Третій, рекомендований спосіб реєстрації фільтрів ...
        filter( "/*" ).through( CharsetFilter.class );

        // ... та сервлетів
        serve( "/"         ).with( HomeServlet.class     );
        serve( "/servlets" ).with( ServletsServlet.class );
        serve( "/signup"   ).with( SignupServlet.class   );

        // !! не забути зняти з фільтрів/сервлетів анотації @Web...
        // !! та додати анотації @Singleton
    }
}
