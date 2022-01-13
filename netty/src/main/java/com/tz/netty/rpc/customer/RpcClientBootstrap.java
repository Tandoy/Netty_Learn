package com.tz.netty.rpc.customer;

import com.tz.netty.rpc.netty.RpcNettyClient;
import com.tz.netty.rpc.publicinterface.RpcHelloService;

public class RpcClientBootstrap {
    //这里定义协议头
    public static final String providerName = "HelloService#hello#";

    public static void main(String[] args) throws Exception {

        //创建一个消费者 其实就是netty客户端
        RpcNettyClient customer = new RpcNettyClient();

        //创建代理对象 其实也就是启动netty客户端
        RpcHelloService service = (RpcHelloService) customer.getBean(RpcHelloService.class, providerName);

        for (; ; ) {
            Thread.sleep(2 * 1000);
            //通过代理对象调用服务提供者的方法(服务)
            String res = service.hello("你好 dubbo~");
            System.out.println("调用的结果 res= " + res);
        }
    }
}
