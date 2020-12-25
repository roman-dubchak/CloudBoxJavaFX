import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractFileInboundHandler extends SimpleChannelInboundHandler<AbstractMassage> {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractFileInboundHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMassage massage) throws Exception {
        LOG.info("File info: {}", massage);
        if(massage instanceof ListRequest){
            ctx.writeAndFlush(new ListFilesServer(getServerFiles()));
        }

        if (massage instanceof FileInfo){
            FileInfo fileInfo = (FileInfo) massage;
            Files.write(Paths.get("server/serverFiles", fileInfo.getFileName()),
                    fileInfo.getData(),
                    StandardOpenOption.CREATE);
            ctx.writeAndFlush(new ListFilesServer(getServerFiles()));
            LOG.info("Server successfully received file: {}" + fileInfo);
        }

        if (massage instanceof FileRequest){
            FileRequest request = (FileRequest) massage;
            ctx.writeAndFlush(new FileInfo(Paths.get("server/serverFiles", request.getFileName())));
        }
    }

    private List<String> getServerFiles() throws IOException {
        return Files.list(Paths.get("server/serverFiles"))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("error = ",cause);
        ctx.close();
    }
}
