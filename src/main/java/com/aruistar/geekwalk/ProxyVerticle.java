package com.aruistar.geekwalk;

import com.aruistar.geekwalk.domain.Upstream;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;

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

    config().getJsonArray("upstream").forEach(json -> {
      upstreamList.add(new Upstream((JsonObject) json, vertx));
    });

    HttpServerOptions serverOptions = new HttpServerOptions();
    serverOptions.setTcpKeepAlive(true);
    HttpServer httpServer = vertx.createHttpServer(serverOptions);


    httpServer.requestHandler(req -> {
      String path = req.path();

      HttpServerResponse resp = req.response();

      req.pause();

      for (Upstream upstream : upstreamList) {
        if (path.startsWith(upstream.getPrefix())) {

          String uri = req.uri().replace(upstream.getPrefix(),upstream.getPath());

          upstream.getHttpClient().request(req.method(), uri, ar -> {
            if (ar.succeeded()) {
              HttpClientRequest reqUpStream = ar.result();
              reqUpStream.headers().setAll(req.headers());

              reqUpStream.send(req).onSuccess(respUpstream -> {

                resp.setStatusCode(respUpstream.statusCode());
                resp.headers().setAll(respUpstream.headers());
                resp.send(respUpstream);

              }).onFailure(err -> {

                err.printStackTrace();
                resp.setStatusCode(500).end(err.getMessage());

              });

            } else {
              ar.cause().printStackTrace();
              resp.setStatusCode(500).end(ar.cause().getMessage());
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
}
