package com.aruistar.geekwalk.domain;

import io.vertx.core.json.JsonObject;

/**
 * @author panlf
 * @date 2021/8/20
 */
public class ProxyUrl {
  private String url;
  private Integer weight;


  public ProxyUrl(JsonObject jsonObject){
     this.url = jsonObject.getString("url");
     this.weight = jsonObject.getInteger("weight",1);
  }


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Integer getWeight() {
    return weight;
  }

  public void setWeight(Integer weight) {
    this.weight = weight;
  }
}
