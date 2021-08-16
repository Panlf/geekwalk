package com.aruistar.geekwalk.launcher;

import io.vertx.core.Launcher;

import java.util.Arrays;

/**
 * @author panlf
 * @date 2021/8/9
 */
public class GeekLauncher extends Launcher {
  public static void main(String[] args) {

    if(args.length>0) {
      Arrays.stream(args).forEach(System.out::println);
    }

    new GeekLauncher().dispatch(args);
  }
}
