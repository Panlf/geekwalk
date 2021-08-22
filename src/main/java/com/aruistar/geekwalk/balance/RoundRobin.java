package com.aruistar.geekwalk.balance;

import com.aruistar.geekwalk.domain.ProxyUrl;

import java.util.List;

/**
 * 轮询负载均衡
 * @author panlf
 * @date 2021/8/20
 */
public class RoundRobin {
  private static Integer pos = 0;

  public String getProxyUrl(List<ProxyUrl> proxyUrlList){
    String url;
    synchronized (pos){
      if(pos > proxyUrlList.size()){
        pos = 0;
      }
      url = proxyUrlList.get(pos).getUrl();
      pos++;
    }
    return url;
  }

}
