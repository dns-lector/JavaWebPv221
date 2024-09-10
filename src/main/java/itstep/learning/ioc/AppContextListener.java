package itstep.learning.ioc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import javax.servlet.ServletContextEvent;

public class AppContextListener extends GuiceServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        // додаткові дії при створенні контексту (при запуску)
    }

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
                new ServicesModule(),
                new WebModule()
        );
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