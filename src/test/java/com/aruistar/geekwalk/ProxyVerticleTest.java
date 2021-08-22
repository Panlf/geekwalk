package com.aruistar.geekwalk;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author panlf
 * @date 2021/8/16
 */
@ExtendWith(VertxExtension.class)
public class ProxyVerticleTest {

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext){
    FileSystem fs = vertx.fileSystem();
    fs.readFile("src/main/resources/config.json",result->{
      if(result.succeeded()){
        JsonObject  serverConfigA = new JsonObject();
        serverConfigA.put("port",8081);
        DeploymentOptions optionsA = new DeploymentOptions();
        optionsA.setConfig(serverConfigA);
        vertx.deployVerticle(new ServerVerticle(),optionsA,a1->{
            if(a1.succeeded()){
              JsonObject  serverConfigB = new JsonObject();
              serverConfigB.put("port",8080);
              DeploymentOptions optionsB = new DeploymentOptions();
              optionsB.setConfig(serverConfigB);
              vertx.deployVerticle(new ServerVerticle(),optionsB,ar -> {
                if (ar.succeeded()) {
                  JsonObject config = result.result().toJsonObject();
                  DeploymentOptions deploymentOptions = new DeploymentOptions();
                  deploymentOptions.setConfig(config);
                  vertx.deployVerticle(new ProxyVerticle(), deploymentOptions, testContext.succeedingThenComplete());
                }
              });
            }
        });
      }
    });
  }

  @Test
  void start_server(Vertx vertx, VertxTestContext testContext){

    WebClient client = WebClient.create(vertx);
    client.get(8080,"127.0.0.1","/hello")
      .send()
      .onSuccess(response->{
        System.out.println(response.statusCode());
        System.out.println(response.bodyAsString());
        testContext.completeNow();
      }).onFailure(handler->{
       System.out.println(handler.getMessage());
    });
  }

  @Test
  void testProxyServerPost(Vertx vertx, VertxTestContext testContext){

    WebClient client = WebClient.create(vertx);
    client.post(9091,"127.0.0.1","/hello")
      .sendJson(new JsonObject().put("name","vertx hello!!!"))
      .onSuccess(response->{
        System.out.println(response.statusCode());
        System.out.println(response.bodyAsString());
        testContext.completeNow();
      }).onFailure(handler->{
      System.out.println(handler.getMessage());
    });
  }


  @Test
  void testProxyServerGet(Vertx vertx, VertxTestContext testContext) throws InterruptedException {

    WebClient client = WebClient.create(vertx);

    for(int i=0;i<10;i++) {

     // Thread.sleep(1000);
      client.get(9091, "127.0.0.1", "/hello")
        .send()
        .onSuccess(response -> {
          System.out.println(response.statusCode());
          System.out.println(response.bodyAsString());
          testContext.completeNow();
        }).onFailure(handler -> {
        System.out.println(handler.getMessage());
      });
    }
  }

  @Test
  void testFrontend(Vertx vertx, VertxTestContext testContext){

    WebClient client = WebClient.create(vertx);
    client.get(9091,"127.0.0.1","/web1")
      .send()
      .onSuccess(response->{
        System.out.println(response.statusCode());
        System.out.println(response.bodyAsString());
        testContext.completeNow();
      }).onFailure(handler->{
      System.out.println(handler.getMessage());
    });
  }

  @Test
  void testFrontend404(Vertx vertx, VertxTestContext testContext){

    WebClient client = WebClient.create(vertx);
    client.get(9091,"127.0.0.1","/web2/nonage")
      .send()
      .onSuccess(response->{
        System.out.println(response.statusCode());
        System.out.println(response.bodyAsString());
        testContext.completeNow();
      }).onFailure(handler->{
      System.out.println(handler.getMessage());
    });
  }

  @Test
  void testWebSocket(Vertx vertx, VertxTestContext testContext){
    HttpClientOptions clientOptions = new HttpClientOptions();
    clientOptions.setDefaultHost("127.0.0.1");
    clientOptions.setDefaultPort(9091);

    HttpClient client = vertx.createHttpClient(clientOptions);

    client.webSocket("/websocket").onSuccess(ws->{
      ws.handler(replyBuffer ->{

        System.out.println(replyBuffer);

        testContext.completeNow();
      });
    }).onFailure(testContext::failNow);
  }
}
