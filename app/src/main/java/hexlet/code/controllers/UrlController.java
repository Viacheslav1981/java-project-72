package hexlet.code.controllers;


import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.net.URL;


public final class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

    public static Handler listUrls = ctx -> {

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> urlPagedList = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .findPagedList();

        List<Url> urls = urlPagedList.getList();



        ctx.attribute("urls", urls);
        ctx.render("showUrlsList.html");

    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo((int) id)
                .findOne();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String createdAt = simpleDateFormat.format(Date.from(url.getCreatedAt()));

        ctx.attribute("id", url.getId());
        ctx.attribute("name", url.getName());
        ctx.attribute("createdAt", createdAt);
        ctx.attribute("urlChecks", url.getUrlChecks());
        ctx.render("showUrl.html");

    };
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

            List<Url> urlsList = new QUrl()
                    .findList();

            for (Url value : urlsList) {
                if (value.getName().contains(nameUrl)) {
                    isUrlInList = true;
                    // break;
                }
            }

            if (isUrlInList) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/urls");


            } else {
                url.save();

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

    public static Handler makeCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(1L);

        Url url = new QUrl()
                .id.equalTo((int) id)
                .findOne();

        HttpResponse response = Unirest
                .get(url.getName())
                .asString();

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

        try {
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

}
