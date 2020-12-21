import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.LoggerFactory;

import java.io.File;

import org.slf4j.Logger;

public class ByteToFileInboundHandler extends ChannelInboundHandlerAdapter {
    private final static Logger LOG = LoggerFactory.getLogger(ByteToFileInboundHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        LOG.info("buf: {}", buf);
        File file = null;
        byte[] data = new byte[255];
        buf.readBytes(data);
        buf.release();


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("error = ",cause);
        ctx.close();
    }
}
