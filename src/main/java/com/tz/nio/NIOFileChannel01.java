package com.tz.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 使用FileChannel向本地文件读写数据
 */
public class NIOFileChannel01 {
    public static void main(String[] args) throws IOException {
        // 本地文件写数据
        write();
        // 本地文件读数据
        read();
        // 使用一个 Buffer 完成文件读取、写入
        transferFromDiy();
    }

    private static void transferFromDiy() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File("d:\\file01.txt"));
        FileChannel fileChannel01 = fileInputStream.getChannel();
        FileOutputStream fileOutputStream = new FileOutputStream("file02.txt");
        FileChannel fileChannel02 = fileOutputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(512);

        while (true) { //循环读取

            /*
            public final Buffer clear() {
                position = 0;
                limit = capacity;
                mark = -1;
                return this;
            }
            */
            //清空 buffer
            // 为什么需要清空buffer：如果不进行clear position = limit,从channel读到buffer后 read = 0无法终止
            byteBuffer.clear();
            // 数据从channel中读取到buffer
            int read = fileChannel01.read(byteBuffer);
            System.out.println("read = " + read);
            if (read == -1) { //表示读完
                break;
            }

            //将 buffer 中的数据写入到 fileChannel02--2.txt
            // 这里反转的目的就是将buffer中的position= 0 limit = 实际存储内容字节大小，当从buffer写到channel后position = limit
            byteBuffer.flip();
            fileChannel02.write(byteBuffer);
        }

        //关闭相关的流
        fileInputStream.close();
        fileOutputStream.close();
    }

    private static void read() throws IOException {
        //创建文件的输入流
        File file = new File("d:\\file01.txt");
        FileInputStream fileInputStream = new FileInputStream(file);

        //通过 fileInputStream 获取对应的 FileChannel -> 实际类型 FileChannelImpl
        FileChannel fileChannel = fileInputStream.getChannel();

        //创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());

        //将通道的数据读入到 Buffer
        fileChannel.read(byteBuffer);

        //将 byteBuffer 的字节数据转成 String
        System.out.println(new String(byteBuffer.array()));
        fileInputStream.close();
    }

    private static void write() throws IOException {
        String str = "hello,nio";
        // 1.创建文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream("d:\\\\file01.txt");
        // 2.通过文件输出流创建FileChannel
        FileChannel fileChannel = fileOutputStream.getChannel();
        // 3.创建ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        // 4.write to buffer
        byteBuffer.put(str.getBytes());
        // 5.buffer反转
        byteBuffer.flip();
        // 6.write to channel
        fileChannel.write(byteBuffer);
        // 7.相关流的关闭
        fileOutputStream.close();
    }


}
