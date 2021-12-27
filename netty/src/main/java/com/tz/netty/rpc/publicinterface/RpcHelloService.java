package com.tz.netty.rpc.publicinterface;

//这个是接口，是服务提供方和 服务消费方都需要
public interface RpcHelloService {
    String hello(String mes);
}
