package hexlet.code.controllers;


import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.PagedList;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public final class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

    public static Handler listUrls = ctx -> {

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;


        List<Url> urls = UrlRepository.getUrls();

        ctx.attribute("urls", urls);
        ctx.render("showUrlsList.html");

    };

    /*
    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = UrlRepository.findById(id)
                .orElseThrow(() -> new NotFoundResponse("Url not found"));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String createdAt = simpleDateFormat.format(Date.from(url.getCreatedAt()));

        List<UrlCheck> urlChecks = url.getUrlChecks();
        urlChecks.sort((o2, o1) -> o1.getId().compareTo(o2.getId()));

        ctx.attribute("id", url.getId());
        ctx.attribute("name", url.getName());
        ctx.attribute("createdAt", createdAt);
        ctx.attribute("urlChecks", url.getUrlChecks());
        ctx.render("showUrl.html");

    };

     */

    public static Handler newUrl = ctx -> {
        Url url = new Url();
        ctx.attribute("url", url);
        ctx.render("mainPage.html");
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
                    // break;
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
            ctx.render("mainPage.html");

        }
    };

    /*
    public static Handler makeCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(1L);

        Url url = new QUrl()
                .id.equalTo((int) id)
                .findOne();

        HttpResponse response = null;
        try {
            response = Unirest
                    .get(url.getName())
                    .asString();

        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");

        }

        try {
            Document document = Jsoup.parse(response.getBody().toString());

            UrlCheck urlCheck = new UrlCheck();

            int statusCode = response.getStatus();
            String title = document.title();


            Element h1Element = document.selectFirst("h1");
            String h1 = h1Element == null
                    ? ""
                    : h1Element.text();
            Element descriptionElement = document.selectFirst("meta[name=description]");
            String description = descriptionElement == null
                    ? ""
                    : descriptionElement.attr("content");
            urlCheck.setStatusCode(statusCode);
            urlCheck.setTitle(title);
            urlCheck.setDescription(description);
            urlCheck.setH1(h1);
            urlCheck.setUrl(url);
            urlCheck.save();

            url.setId(id);

            List<UrlCheck> urlChecks = new ArrayList<>();
            urlChecks.addAll(url.getUrlChecks());
            urlChecks.add(urlCheck);


            url.setUrlChecks(urlChecks);

            url.save();
            LOGGER.info("Страница проверена");
            ctx.sessionAttribute("flash-type", "success");
            ctx.sessionAttribute("flash", "Страница успешно проверена");
        } catch (Exception exception) {
            LOGGER.warn("Ошибка при добавлении данных в БД");
        }

        ctx.redirect("/urls/" + id);
    };

     */

}
