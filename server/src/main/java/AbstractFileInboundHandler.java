import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class AbstractFileInboundHandler extends SimpleChannelInboundHandler<AbstractFile> {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractFileInboundHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractFile abstractFile) throws Exception {
        LOG.info("File info: {}", abstractFile);
        if (abstractFile instanceof FileInfo){
            ctx.write(abstractFile);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("error = ",cause);
        ctx.close();
    }
}
