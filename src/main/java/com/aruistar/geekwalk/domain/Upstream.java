package com.aruistar.geekwalk.domain;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

import java.net.URL;

/**
 * @author panlf
 * @date 2021/8/9
 */
public class Upstream {
  String path;
  String url;
  String prefix;

  HttpClient httpClient;

  public String getPath() {
    return path;
  }

  public String getUrl() {
    return url;
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }

  public String getPrefix() {
    return prefix;
  }

  public Upstream(JsonObject json, Vertx vertx){
    this.prefix = json.getString("prefix");
    this.url = json.getString("url");

    try {
      String host = new URL(url).getHost();
      int port = new URL(url).getPort();
      this.path = new URL(url).getPath();
      HttpClientOptions clientOptions = new HttpClientOptions();
      clientOptions.setDefaultHost(host);
      clientOptions.setDefaultPort(port);

      this.httpClient = vertx.createHttpClient(clientOptions);
    }catch (Exception e){
      e.printStackTrace();
    }
  }
}
