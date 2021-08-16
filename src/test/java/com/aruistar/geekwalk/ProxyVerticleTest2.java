package com.aruistar.geekwalk;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
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
public class ProxyVerticleTest2 {

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext){
    //消息总线方式，感觉好蠢的样子
    EventBus eventBus = vertx.eventBus();
    FileSystem fs = vertx.fileSystem();
    fs.readFile("src/main/resources/config.json",result->{
      if(result.succeeded()){
        JsonObject config = result.result().toJsonObject();
        eventBus.publish("startServer",config);
      }
    });

    eventBus.consumer("startServer",handler->{
      JsonObject config = (JsonObject) handler.body();

      DeploymentOptions deploymentOptions = new DeploymentOptions();
      deploymentOptions.setConfig(config);

      vertx.deployVerticle(new ServerVerticle(), ar -> {
        if (ar.succeeded()) {
          vertx.deployVerticle(new ProxyVerticle(), deploymentOptions, testContext.succeedingThenComplete());
        }
      });
    });
  }

 // @BeforeEach
  void test01(Vertx vertx, VertxTestContext testContext){
    EventBus eventBus = vertx.eventBus();
    FileSystem fs = vertx.fileSystem();
    fs.readFile("src/main/resources/config.json",result->{
      if(result.succeeded()){
        JsonObject config = result.result().toJsonObject();
        eventBus.publish("startServer",config);
      }
    });

    eventBus.consumer("startServer",handler->{
      System.out.println(handler.body());
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
  void testProxyServerGet(Vertx vertx, VertxTestContext testContext){

    WebClient client = WebClient.create(vertx);
    client.get(9091,"127.0.0.1","/hello")
      .send()
      .onSuccess(response->{
        System.out.println(response.statusCode());
        System.out.println(response.bodyAsString());
        testContext.completeNow();
      }).onFailure(handler->{
      System.out.println(handler.getMessage());
    });
  }
}
