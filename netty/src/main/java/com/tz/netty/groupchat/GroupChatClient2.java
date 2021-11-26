package com.tz.netty.groupchat;

public class GroupChatClient2 {
    public static void main(String[] args) {
        new GroupChatClient("127.0.0.1", 8023).run();
    }
}
