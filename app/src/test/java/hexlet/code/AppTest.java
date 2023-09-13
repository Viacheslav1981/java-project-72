package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.io.File;

import org.jsoup.Jsoup;


public class AppTest {

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static MockWebServer server;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        server = new MockWebServer();

        File html = new File("src/test/resources/templates_test/urlCheckTest.html");
        String body = Jsoup.parse(html, "UTF-8").toString();
        server.enqueue(new MockResponse().setBody(body));
        server.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        server.shutdown();
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

        int id = Math.toIntExact(url.getId());

        HttpResponse response = Unirest
                .get(baseUrl + "/urls/" + id)
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody().toString()).contains("Сайт " + name);
    }


    @Test
    public void testUrlCheck() throws IOException {
       // MockWebServer server = new MockWebServer();

    //    File html = new File("src/test/resources/templates_test/urlCheckTest.html");
    //    String body = Jsoup.parse(html, "UTF-8").toString();
    //    server.enqueue(new MockResponse().setBody(body));
    //    server.start();

        String serverUrl = server.url("").toString();

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", serverUrl)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        Url url = new QUrl()
                .findOne();

        assertThat(url).isNotNull();
        assertThat(url.getId()).isEqualTo(1);
        assertThat(url.getName()).isEqualTo(serverUrl.substring(0, serverUrl.length() - 1));

        long id = url.getId();

        HttpResponse response = Unirest
                .post(baseUrl + "/urls/" + id + "/checks")
                .asEmpty();

      //  server.shutdown();

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/urls/" + id);

        HttpResponse urlPage = Unirest
                .get(baseUrl + "/urls/" + id)
                .asString();

        assertThat(urlPage.getStatus()).isEqualTo(200);
        assertThat(urlPage.getBody().toString()).contains("<td>200</td>");
        assertThat(urlPage.getBody().toString()).contains("<td>Анализатор страниц</td>");
    }


}
