import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.ObjectInputFilter;

import org.slf4j.Logger;

public class ByteToFileInboundHandler extends SimpleChannelInboundHandler<FileInfo> {
    private final static Logger LOG = LoggerFactory.getLogger(ByteToFileInboundHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileInfo fileInfo) throws Exception {
        LOG.info("File name: {}, size: {}, type: {}",
                fileInfo.getFileName(), fileInfo.getFileSize(), fileInfo.getFileType());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("error = ",cause);
        ctx.close();
    }
}
