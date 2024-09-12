package itstep.learning.ioc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import itstep.learning.services.stream.BaosStringReader;
import itstep.learning.services.stream.StringReader;

import javax.servlet.ServletContextEvent;

public class AppContextListener extends GuiceServletContextListener {

    private final StringReader stringReader = new BaosStringReader();

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
                new ServicesModule( stringReader ),
                new WebModule(),
                new DbModule( stringReader )
        );
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        // додаткові дії при вимкненні застосунку
    }
}
/*
ContextListener - підписник на подію створення контексту застосунку
(деплою) - запуску застосунку
 */