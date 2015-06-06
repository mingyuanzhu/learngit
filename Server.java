/**
 * 
 */
package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 
 * 
 * @author zhumingyuan
 * @date Jun 6, 2015 7:57:29 PM
 */
public class Server {

    private Selector selector;
    
    private int i = 0;
    
    public static void main(String[] args) {
        Server server = new Server();
        server.connect(8080);
        server.listen();
    }

    public void connect(int port) {
        try {
            // 初始化选择器
            selector = Selector.open();
            // 构造channel
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            SocketAddress address = new InetSocketAddress(port);
            serverChannel.socket().bind(address);
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("server had been started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        if (selector.isOpen()) {
            for (;;) {
                try {
                    selector.selectNow();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        if (selectionKey.isAcceptable()) {
                            ServerSocketChannel serverChannel =
                                            (ServerSocketChannel) selectionKey.channel();
                            // 获得和客户端连接的通道
                            SocketChannel channel = serverChannel.accept();
                            // 设置成非阻塞
                            channel.configureBlocking(false);
                            // 在这里可以给客户端发送信息哦
                            write(channel);                            
                            // 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
                            channel.register(this.selector, SelectionKey.OP_READ);

                            // 获得了可读的事件
                        } else if (selectionKey.isReadable()) {
                            read(selectionKey);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void read(SelectionKey selectionKey) {
        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        // 创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(100);
        try {
            channel.read(buffer);
            byte[] data = buffer.array();
            String msg = new String(data).trim();
            System.out.println("receive from client:" + msg);
            write(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void write(SocketChannel channel) {
        ByteBuffer outBuffer = ByteBuffer.wrap(new String("push msg " + i++ + " to server.").getBytes());
        try {
            // 将消息回送给客户端
            channel.write(outBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
