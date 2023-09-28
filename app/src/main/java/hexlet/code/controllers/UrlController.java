package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public final class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

    public static Handler listUrls = ctx -> {

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;


        List<Url> urls = UrlRepository.getUrls();

        Map<Long, UrlCheck> urlChecks = null;
        try {
            urlChecks = UrlCheckRepository.findLatestChecks();
        } catch (SQLException throwables) {
            log.error(throwables.getMessage(), throwables);
        }

        ctx.attribute("urls", urls);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("showUrlsList.html");

    };
    public static Handler showUrl = ctx -> {

        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = UrlRepository.findById(id).orElse(null);

        if (url == null) {
            throw new NotFoundResponse("The ulr you are looking for is not found");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String createdAt = simpleDateFormat.format(Date.from(url.getCreatedAtToInstant()));

        List<UrlCheck> urlChecks = UrlCheckRepository.getAllChecks(url.getId());

        urlChecks.sort((o2, o1) -> o1.getId().compareTo(o2.getId()));

        ctx.attribute("id", url.getId());
        ctx.attribute("name", url.getName());
        ctx.attribute("createdAt", createdAt);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("showUrl.html");
    };


    public static Handler newUrl = ctx -> {
        Url url = new Url();
        ctx.attribute("url", url);
        ctx.render("index.html");
    };

    public static Handler createUrl = ctx -> {
        String nameUrl = ctx.formParam("url");

        try {
            URL urlFull = new URL(nameUrl);
            String protocol = urlFull.getProtocol();
            String authority = urlFull.getAuthority();

            nameUrl = protocol + "://" + authority;

            Url url = new Url(nameUrl);

            boolean isUrlInList = false;

            List<Url> urlsList = UrlRepository.getUrls();

            for (Url value : urlsList) {
                if (value.getName().equals(nameUrl)) {
                    isUrlInList = true;
                }
            }

            if (isUrlInList) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/urls");


            } else {
                UrlRepository.save(url);

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flash-type", "success");

                ctx.redirect("/urls");

            }

        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");

        }
    };


    public static Handler makeCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(1L);

        Url url = UrlRepository.findById(id).orElse(null);

        HttpResponse response;

        try {
            response = Unirest
                    .get(url.getName())
                    .asString();
            int statusCode = response.getStatus();


            Document document = Jsoup.parse(response.getBody().toString());
            String title = document.title();


            Element h1Element = document.selectFirst("h1");
            String h1 = h1Element == null
                    ? ""
                    : h1Element.text();
            Element descriptionElement = document.selectFirst("meta[name=description]");
            String description = descriptionElement == null
                    ? ""
                    : descriptionElement.attr("content");

            Timestamp createdAt = new Timestamp(System.currentTimeMillis());

            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url.getId());
            urlCheck.setCreatedAt(createdAt);

            UrlCheckRepository.save(urlCheck);

            LOGGER.info("Страница проверена");
            ctx.sessionAttribute("flash-type", "success");
            ctx.sessionAttribute("flash", "Страница успешно проверена");

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");

        } catch (Exception exception) {
            LOGGER.warn("Ошибка при добавлении данных в БД");
        }

        ctx.redirect("/urls/" + id);
    };


}
