package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import jakarta.servlet.http.HttpServletResponse;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {

    @Test
    public void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static MockWebServer mockServer;
    private static Javalin app;
    private static String baseUrl;
    private static final String CORRECT_URL = "https://www.google.com";
    private static final String URL_FOR_NON_EXISTING_ENTITY_TEST = "https://www.dzen.ru";
    private static final String WRONG_URL = "www.danger.su";

    private static Path getFixturePath(String fileName) {
        return Paths.get("src", "test", "resources", "fixtures", fileName)
                .toAbsolutePath().normalize();
    }

    private static String readFixture(String fileName) throws IOException {
        Path filePath = getFixturePath(fileName);
        return Files.readString(filePath).trim();
    }

    private static String getDatabaseUrl() {
        return System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project");
    }

    @BeforeAll
    public static void beforeAll() throws SQLException, IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        mockServer = new MockWebServer();
        MockResponse mockedResponse = new MockResponse()
                .setBody(readFixture("index.html"));
        mockServer.enqueue(mockedResponse);
        mockServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockServer.shutdown();
    }

    @BeforeEach
    public void beforeEach() throws SQLException {
        UrlRepository.truncateDB();
        UrlCheckRepository.truncateDB();

        Url firstUrl = new Url(CORRECT_URL);
        firstUrl.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        UrlRepository.save(firstUrl);
    }

    @Test
    public void testWelcome() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        int status = response.getStatus();
        assertThat(status).isEqualTo(HttpServletResponse.SC_OK);
    }




    @Test
    public void testCreateUrl() {
        HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                .field("url", CORRECT_URL)
                .asString();

        int postQueryStatus = response.getStatus();
        Assertions.assertEquals(postQueryStatus, HttpServletResponse.SC_FOUND);

        response = Unirest.get(baseUrl + "/urls").asString();

        int getQueryStatus = response.getStatus();
        String responseBody = response.getBody();

        assertThat(getQueryStatus).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(responseBody).contains(CORRECT_URL);
    }

    @Test
    public void testShowUrls() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
        String body = response.getBody();
        int getQueryStatus = response.getStatus();

        assertThat(getQueryStatus).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(body).contains(CORRECT_URL);
    }

    @Test
    public void testShowUrlById() throws SQLException {

        Url actualUrl = UrlRepository.findByName(CORRECT_URL).orElseThrow(
                () -> new SQLException("url with the name " + CORRECT_URL + " was not found!"));

        Long id = actualUrl.getId();

        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + id).asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(body).contains(CORRECT_URL,
                actualUrl.getCreatedAt()
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        Url wrongUrl = new Url(URL_FOR_NON_EXISTING_ENTITY_TEST);
        wrongUrl.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        UrlRepository.save(wrongUrl);
        Long idForDeletion = UrlRepository.findByName(URL_FOR_NON_EXISTING_ENTITY_TEST)
                .orElseThrow(() -> new SQLException("wrongUrl with name " + URL_FOR_NON_EXISTING_ENTITY_TEST
                        + " was not found in DB!"))
                .getId();

        UrlRepository.delete(idForDeletion);
        response = Unirest.get(baseUrl + "/urls/" + idForDeletion).asString();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }


    @Test
    public void testUrlCheck() throws SQLException, IOException {

        Javalin additionalApp = App.getApp();

        String url = mockServer.url("/").toString().replaceAll("/$", "");

        JavalinTest.test(additionalApp, (server, client) -> {
            String requestBody = "url=" + url;
            assertThat(client.post("/urls", requestBody).code()).isEqualTo(HttpServletResponse.SC_OK);

            Url actualUrl = UrlRepository.findByName(url).orElse(null);
            assertThat(actualUrl).isNotNull();
            System.out.println("\n!!!!!");
            System.out.println(actualUrl);

            System.out.println("\n");
            assertThat(actualUrl.getName()).isEqualTo(url);

            client.post("/urls/" + actualUrl.getId() + "/checks");

            assertThat(client.get("/urls/" + actualUrl.getId()).code())
                    .isEqualTo(HttpServletResponse.SC_OK);

            var actualCheck = UrlCheckRepository.findLastCheckByUrlId(actualUrl.getId())
                    .orElse(null);
            assertThat(actualCheck).isNotNull();
            assertThat(actualCheck.getTitle()).isEqualTo("Test page");
            assertThat(actualCheck.getH1()).isEqualTo("Testing");
            assertThat(actualCheck.getDescription()).isEqualTo("statements of great people");
        });
    }

    @Test
    public void testNotFound() {
        HttpResponse<String> response = Unirest.post(baseUrl)
                .field("url", WRONG_URL)
                .asString();

        int postQueryStatus = response.getStatus();
        assertThat(postQueryStatus).isEqualTo(HttpServletResponse.SC_NOT_FOUND);

        response = Unirest.get(baseUrl + "/urls").asString();
        assertThat(response.getBody()).doesNotContain(WRONG_URL);
    }

}
