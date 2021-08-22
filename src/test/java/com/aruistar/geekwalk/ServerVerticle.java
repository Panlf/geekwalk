package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author panlf
 * @date 2021/8/3
 */
public class ServerVerticle extends AbstractVerticle {

 private static final Logger log = LoggerFactory.getLogger(ServerVerticle.class);

  @Override
  public void start() throws Exception {

    int port = config().getInteger("port");


    HttpServer httpServer = vertx.createHttpServer();

    Router router  = Router.router(vertx);


    router.route().handler(rc->{
      HttpServerRequest httpServerRequest = rc.request();

      log.info("request host : {} ,\n" +
          "request localAddress : {} ,\n" +
          "request method : {} ,\n" +
          "request uri : {},\n",
        httpServerRequest.host(),
        httpServerRequest.localAddress(),
        httpServerRequest.method(),
        httpServerRequest.uri());

      rc.next();
    });

    router.route("/websocket").handler(routingContext ->{
      HttpServerResponse response = routingContext.response();
      HttpServerRequest request = routingContext.request();

      request.toWebSocket().onSuccess(ws->{
        ws.writeTextMessage("hello websocket");
      });
    });


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

    httpServer.requestHandler(router).listen(port,event -> {
        if(event.succeeded()){
          System.out.println("Server Open on "+port+" port");
        }
    });
  }
}
