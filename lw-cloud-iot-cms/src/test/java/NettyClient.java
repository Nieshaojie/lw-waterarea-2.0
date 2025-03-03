
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Value;

/**
 * @ClassName:NettyClient
 * @Description:netty客户端测试类
 * @Author:R.Gong
 * @Date:2022/11/1 16:56
 * @Version:1.0
 **/
public class NettyClient {

    private static final String SERVER = "192.168.124.89";

    //    @Value("${server_port}")
    private Integer serverPort = 6688;

    public static void main(String[] args) {
        new NettyClient().run();
    }

    public void run() {

        System.out.println("客户端启动中");

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {

                    }

                });


        int index = 0;
        int finalPort;
        try {
            bootstrap.connect(SERVER, serverPort).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    System.out.println("创建连接失败 ");
                }
            }).get();

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
