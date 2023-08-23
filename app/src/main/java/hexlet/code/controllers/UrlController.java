package hexlet.code.controllers;


import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

    public static Handler listUrls = ctx -> {
        /*
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Article> pagedArticles = new QArticle()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

         */

      //  List<Article> articles = pagedArticles.getList();

      //  ctx.attribute("articles", articles);
     //   ctx.attribute("page", page);
   //     ctx.render("articles/index.html");
      //  removeFlashMessage(ctx);

        PagedList<Url> urlPagedList = new QUrl()
                .findPagedList();

        List<Url> urls = urlPagedList.getList();

        ctx.attribute("urls", urls);
        ctx.render("showUrlsList.html");
    };
    public static Handler newUrl = ctx -> {
        Url url = new Url();
        ctx.attribute("url", url);
        ctx.render("mainPage.html");
    };

    public static Handler createUrl = ctx -> {
        String nameUrl = ctx.formParam("url");
        Url url = new Url(nameUrl);

        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
       // ctx.redirect("/urls");
        ctx.redirect("/urls");

    };


}
