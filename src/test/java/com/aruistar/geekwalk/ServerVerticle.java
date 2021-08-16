package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author panlf
 * @date 2021/8/3
 */
public class ServerVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    HttpServer httpServer = vertx.createHttpServer();

    Router router  = Router.router(vertx);

    router.get("/hello").handler(routerContext ->{
      HttpServerResponse httpServerResponse = routerContext.response();

      HttpServerRequest httpServerRequest = routerContext.request();

      httpServerResponse.end("hello world");
    });

    router.post("/hello")
      .handler(BodyHandler.create())
      .handler(routerContext -> {
        JsonObject paramJson = routerContext.getBodyAsJson();
        routerContext.response().end(paramJson.getString("name"));
      });

    router.errorHandler(500,rc->{
        rc.failure().printStackTrace();
          rc.response().setStatusCode(500).end("Server Verticle Error :"+ rc.failure().getMessage());
    });

    httpServer.requestHandler(router).listen(8080,event -> {
        if(event.succeeded()){
          System.out.println("Server Open on 8080 port");
        }
    });
  }
}
