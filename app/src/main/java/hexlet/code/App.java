package hexlet.code;

import io.javalin.Javalin;

public class App {

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(8080);
    }

    public static Javalin getApp() {

        Javalin app = Javalin.create(javalinConfig ->
                javalinConfig.plugins.enableDevLogging());

        addRoutes(app);

        return app;

    }

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.result("Hello World"));

    }

}
