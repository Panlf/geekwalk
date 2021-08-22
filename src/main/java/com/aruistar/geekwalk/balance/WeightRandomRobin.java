package com.aruistar.geekwalk.balance;

import com.aruistar.geekwalk.domain.ProxyUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 权重轮询算法
 * @author panlf
 * @date 2021/8/20
 */
public class WeightRandomRobin {

  public String getProxyUrl(List<ProxyUrl> proxyUrlList){

    List<String> urlList = new ArrayList<>();

    for(ProxyUrl proxyUrl:proxyUrlList){
      for(int i=0;i<proxyUrl.getWeight();i++){
          urlList.add(proxyUrl.getUrl());
      }
    }

    int randomPos = new Random().nextInt(urlList.size());

    return urlList.get(randomPos);
  }

}
