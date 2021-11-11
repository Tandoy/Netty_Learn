import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BIO网络模型
 * BIO会为客户端的每个连接创建一个线程进行通讯响应，适合连接数少且长的应用
 */
public class BIOServer {
    public static void main(String[] args) throws IOException {
        // 线程池机制
        // 1.创建线程池
        // 2.如果客户端有请求连接，则创建一个线程与之通讯
        ExecutorService threadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(6666);
        System.out.println("the server is started.....");
        // 一直循环监听
        while (true) {
            System.out.println("id: " + Thread.currentThread().getId() + "  name: " + Thread.currentThread().getName());
            // 获取到客户端socket
            Socket socket = serverSocket.accept();
            // 创建一个线程与之通讯
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    // 具体与客户端通讯细节
                    handle(socket);
                }
            });
        }
    }

    // 具体与客户端通讯细节方法
    private static void handle(Socket socket) {
        System.out.println("id: " + Thread.currentThread().getId() + "  name: " + Thread.currentThread().getName());
        byte[] bytes = new byte[1024];
        //通过socket获取输入流
        try {
            InputStream inputStream = socket.getInputStream();
            // 循环读取客户端发送的消息
            while (true) {
                System.out.println("id: " + Thread.currentThread().getId() + "  name: " + Thread.currentThread().getName());
                System.out.println("read....");
                int read = inputStream.read(bytes);
                if (read != -1) { // 没有读取完
                    System.out.println(new String(bytes,0,read));
                }else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
