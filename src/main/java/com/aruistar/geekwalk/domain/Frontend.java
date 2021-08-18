package com.aruistar.geekwalk.domain;

import io.vertx.core.json.JsonObject;

/**
 * @author panlf
 * @date 2021/8/17
 */
public class Frontend {
  String prefix;
  String dir;
  String reroute404;

  public Frontend(JsonObject jsonObject) {
    this.dir = jsonObject.getString("dir");
    this.prefix = jsonObject.getString("prefix");
    if(!jsonObject.getString("reroute404","").isBlank()){
      this.reroute404 = jsonObject.getString("reroute404");
    }
  }

  public String getReroute404() {
    return reroute404;
  }

  public void setReroute404(String reroute404) {
    this.reroute404 = reroute404;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }
}
