package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controllers.UrlController;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.stream.Collectors;


public class App {

    public static void main(String[] args) throws SQLException, IOException {
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
        String port = System.getenv().getOrDefault("PORT", "8080");
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

    private static InputStream getFileFromResourceAsStream(String fileName) {
        ClassLoader classLoader = App.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream(fileName);
        return is;
    }

    private static String getContentFromStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static Javalin getApp() throws IOException, SQLException {

        var hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(getDatabaseUrl());
        var dataSource = new HikariDataSource(hikariConfig);

        var url = App.class.getClassLoader().getResource("schema.sql");

        // var dataSource = new HikariDataSource(hikariConfig);
        String sql = getContentFromStream(getFileFromResourceAsStream("schema.sql"));

        // var file = new File(url.getFile());
        // var sql = Files.lines(file.toPath())
        //      .collect(Collectors.joining("\n"));


        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

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


    //export JDBC_DATABASE_URL=jdbc:postgresql://db:5432/postgres?password=password&user=postgres
    //export JDBC_DATABASE_URL=jdbc:postgresql://dpg-ckdscldjhfbs73fqrc6g-a.oregon-postgres.render.com:5432/dbpostgres_z47u?password=yQss7SXC5x8lngqv932PN4Mj3iJrhHJI&user=dbpostgres_z47u_user

    private static String getDatabaseUrl() {
        return System.getenv()
                .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project");

    }

    public static void addRoutes(Javalin app) {
        app.get("/", UrlController.newUrl);
        app.get("/urls", UrlController.listUrls);
        app.post("/urls", UrlController.createUrl);
        app.get("/urls/{id}", UrlController.showUrl);
        app.post("/urls/{id}/checks", UrlController.makeCheck);
    }

}

