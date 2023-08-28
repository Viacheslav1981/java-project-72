package hexlet.code.controllers;


import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
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

        for (Url url : urls) {
            System.out.println(url);

        }

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

                // ctx.render("mainPage.html");

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

            //  ctx.redirect("mainPage.html");

        }

        // System.out.println(urlFull.getPort());

        //  System.out.println(urlFull.getContent());
        //  System.out.println(urlFull.toURI());


        // url.save();

        //  ctx.sessionAttribute("flash", "Страница успешно добавлена");
        //  ctx.sessionAttribute("flash-type", "success");

        //  ctx.redirect("/urls");
        //  ctx.redirect("/urls");

    };


}
