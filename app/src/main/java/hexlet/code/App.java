package hexlet.code;

import hexlet.code.controllers.UrlController;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;


public class App {

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    public static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProd() {
        return getMode().equals("production");
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8000");
        return Integer.valueOf(port);
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    public static Javalin getApp() {
       // System.setProperty("APP_ENV", "development");


        Javalin app = Javalin.create(config -> {
            if (!isProd()) {
                config.plugins.enableDevLogging();
            }
            JavalinThymeleaf.init(getTemplateEngine());
        });

        addRoutes(app);
        app.before(ctx -> ctx.attribute("ctx", ctx));

        return app;

    }

    //cd java_projects/java-project-72/app

    public static void addRoutes(Javalin app) {
        app.get("/", UrlController.newUrl);
        app.get("/urls", UrlController.listUrls);
        app.post("/urls", UrlController.createUrl);
        app.get("/urls/{id}", UrlController.showUrl);
        app.post("/urls/{id}/checks", UrlController.makeCheck);
    }

}

