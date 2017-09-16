package ycheng.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import ycheng.util.Recommender;

public class Application implements Daemon {
    private static DaemonContext context;
    private static Server jettyServer;
    private static Properties properties;
    private static Recommender recommender = new Recommender();

    public static synchronized Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try(InputStream stream = classLoader.getResourceAsStream("app.properties")) {
                if (stream != null) {
                    properties.load(stream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public static void reload() {
        if (context != null) {
            context.getController().reload();
        }
    }

    public static Recommender getRecommender() {
        return recommender;
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        Application.context = context;

        properties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try(InputStream stream = classLoader.getResourceAsStream("app.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        }

        jettyServer = createJettyServer(
                Integer.parseInt(properties.getProperty("web.port")),
                properties.getProperty("web.package"));
    }

    @Override
    public void start() throws Exception {
        jettyServer.start();
    }

    @Override
    public void stop() throws Exception {
        jettyServer.stop();
    }

    @Override
    public void destroy() {
        jettyServer.destroy();
    }


    static Server createJettyServer(int port, String packageName) throws Exception {
        Server jettyServer = new Server();
        ServerConnector connector=new ServerConnector(jettyServer);
        connector.setPort(port);
        connector.setReuseAddress(true);
        jettyServer.addConnector(connector);

        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/*");

        jettyServer.setHandler(servletContextHandler);

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.PROVIDER_PACKAGES, packageName);
        resourceConfig.register(MultiPartFeature.class);
        ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
        servletContextHandler.addServlet(servletHolder, "/api/*");

        ServletHolder holderStatic = new ServletHolder("static", DefaultServlet.class);
        holderStatic.setInitParameter("resourceBase","./html");
        holderStatic.setInitParameter("dirAllowed","false");
        holderStatic.setInitParameter("pathInfoOnly","true");
        servletContextHandler.addServlet(holderStatic, "/*");

        return jettyServer;
    }
}
