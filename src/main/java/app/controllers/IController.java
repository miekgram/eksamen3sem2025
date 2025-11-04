package app.controllers;

import io.javalin.http.Handler;

public interface IController {

    Handler getAll();
    Handler getById();
    Handler create();
    Handler update();
    Handler delete();
}