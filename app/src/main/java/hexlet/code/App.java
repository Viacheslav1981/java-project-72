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

import java.io.File;
import java.io.IOException;
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


    //export JDBC_DATABASE_URL=jdbc:postgresql://db:5432/postgres?password=aWp4u78ME7fVgqMo5kneBD5XgpcLhUJE&user=dbpostges_user

    /*
   APP_ENV=production
JDBC_DATABASE_PASSWORD=J5y3LMsnhlx8GXVkxyFO
JDBC_DATABASE_URL=jdbc:postgresql://${{ PGHOST }}:${{ PGPORT }}/${{ PGDATABASE }}
JDBC_DATABASE_USERNAME=postgres
PGDATABASE=railway
PGHOST=containers-us-west-131.railway.app
PGPORT=8032
                                 host                  port    db
            jdbc:postgresql://dpg-cjid3bb37aks73fsvpjg:5432/project4_3kqr

# У строки в переменной следующий формат: {driver}:{provider}://{host}:{port}/{db}?password={password}&user={user}
export JDBC_DATABASE_URL=jdbc:postgresql://dpg-cka1be6v3ddc739qtfa0-a:5432/postges?password=aWp4u78ME7fVgqMo5kneBD5XgpcLhUJE&user=dbpostges_user
Hostname

dpg-cka1be6v3ddc739qtfa0-a

Port

5432

Database

dbpostges

     */

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

    public static Javalin getApp() throws IOException, SQLException {

        var hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(getDatabaseUrl());
        var dataSource = new HikariDataSource(hikariConfig);

        var url = App.class.getClassLoader().getResource("schema.sql");

        var file = new File(url.getFile());
        var sql = Files.lines(file.toPath())
                .collect(Collectors.joining("\n"));

        //log.info(sql);

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

    private static String getDatabaseUrl() {
        return System.getenv()
                .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project");
    }

    //cd java_projects/java-project-72/app

    public static void addRoutes(Javalin app) {
        app.get("/", UrlController.newUrl);
        app.get("/urls", UrlController.listUrls);
        app.post("/urls", UrlController.createUrl);
        app.get("/urls/{id}", UrlController.showUrl);
      //  app.post("/urls/{id}/checks", UrlController.makeCheck);
    }

}

