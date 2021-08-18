package com.aruistar.geekwalk;

import com.aruistar.geekwalk.domain.Frontend;
import com.aruistar.geekwalk.domain.Upstream;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author panlf
 * @date 2021/8/3
 */
public class ProxyVerticle extends AbstractVerticle {


  @Override
  public void start() throws Exception {
    System.out.println(config());
    int port = config().getInteger("port");

    List<Upstream> upstreamList = new ArrayList<>();
    List<Frontend> frontendList = new ArrayList<>();

    config().getJsonArray("upstream").forEach(json -> {
      upstreamList.add(new Upstream((JsonObject) json, vertx));
    });

    config().getJsonArray("frontend").forEach(json -> {
      frontendList.add(new Frontend((JsonObject) json));
    });

    HttpServerOptions serverOptions = new HttpServerOptions();
    serverOptions.setTcpKeepAlive(true);
    HttpServer httpServer = vertx.createHttpServer(serverOptions);


    Router router = Router.router(vertx);
    for (Frontend frontend:frontendList){
      router.route(frontend.getPrefix())
        .handler(StaticHandler.create().setAllowRootFileSystemAccess(true)
        .setWebRoot(frontend.getDir()));
    }


    router.errorHandler(404,err->{
      String path = err.request().path();
      for(Frontend frontend:frontendList){
        if(path.startsWith(frontend.getPrefix()) && frontend.getReroute404() != null){
          err.reroute(frontend.getReroute404());
          return;
        }
      }

      err.response().setStatusCode(404).end("404");
    });

    httpServer.requestHandler(req -> {
      String path = req.path();
      HttpServerResponse resp = req.response();


      for(Frontend frontend:frontendList){
        if(path.startsWith(frontend.getPrefix())){
          router.handle(req);
          return;
        }
      }


      req.pause();

      for (Upstream upstream : upstreamList) {
        if (path.startsWith(upstream.getPrefix())) {

          String uri = req.uri().replace(upstream.getPrefix(),upstream.getPath());

          HttpClient upstreamClient = upstream.getHttpClient();

          String upgrade = req.getHeader("Upgrade");

          if(upgrade != null && upgrade.equalsIgnoreCase("websocket")){
            Future<ServerWebSocket> future = req.toWebSocket();
            future.onSuccess(ws->{
                upstreamClient.webSocket(uri).onSuccess(clientWS ->{
                    ws.frameHandler(clientWS::writeFrame);
                    ws.closeHandler(x->{
                      clientWS.close();
                    });

                    clientWS.frameHandler(ws::writeFrame);
                    clientWS.closeHandler(x->ws.close());
                }).onFailure(err->{
                  error(resp,err);
                });
            }).onFailure(err->{
              error(resp,err);
            });
            return;
          }



          upstreamClient.request(req.method(), uri, ar -> {
            if (ar.succeeded()) {
              HttpClientRequest reqUpStream = ar.result();
              reqUpStream.headers().setAll(req.headers());

              reqUpStream.send(req).onSuccess(respUpstream -> {

                resp.setStatusCode(respUpstream.statusCode());
                resp.headers().setAll(respUpstream.headers());
                resp.send(respUpstream);

              }).onFailure(err -> {
                error(resp,ar.cause());
              });

            } else {
              error(resp,ar.cause());
            }
          });

          break;
        }
      }

    }).listen(port, event -> {
      if (event.succeeded()) {
        System.out.println("ProxyServer Open on " + port + " port");
      }
    });
  }

  void error(HttpServerResponse response,Throwable err){
    response.setStatusCode(500).end(err.getMessage());
  }
}
