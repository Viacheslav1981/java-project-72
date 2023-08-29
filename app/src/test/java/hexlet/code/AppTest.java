package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;
import io.ebean.Database;

public class AppTest {

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Database database;


    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @Test
    void testWelcome() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Анализатор страниц");
    }


    @Test
    public void testCreateUrl() {
        String name = "https://example.com";

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", name)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse response = Unirest
                .get(baseUrl + "/urls")
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody().toString()).contains(name);
        assertThat(response.getBody().toString()).contains("Страница успешно добавлена");

        Url actualUrl = new QUrl()
                .name.equalTo(name)
                .findOne();

        assertThat(actualUrl).isNotNull();
        assertThat(actualUrl.getName()).isEqualTo(name);
    }

    @Test
    public void testShowUrl() {
        String name = "https://example.com";

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", name)
                .asEmpty();

        Url url = new QUrl()
                .name.equalTo(name)
                .findOne();

        Integer id = Math.toIntExact(url.getId());

        HttpResponse response = Unirest
                .get(baseUrl + "/urls/" + id)
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody().toString()).contains("Сайт " + name);
    }


}
