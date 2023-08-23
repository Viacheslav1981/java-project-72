package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class RootController {
    public static Handler main = ctx -> {
        ctx.render("mainPage.html");
    };
}
