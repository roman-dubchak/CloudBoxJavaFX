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
    private String serverDir = "server/serverFiles";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMassage massage) throws Exception {
        LOG.info("File info: {}", massage);
        if(massage instanceof ListRequest){
            ctx.writeAndFlush(new ListFilesServer(getServerFiles()));
        }

        if (massage instanceof FileInfo){
            FileInfo fileInfo = (FileInfo) massage;
            LOG.info("Client upload file type: {}", fileInfo.getFileType());
            if(fileInfo.getFileType().toString() == "FILE") {
                LOG.info("Client upload file: {}", fileInfo);
                Files.write(Paths.get(serverDir, fileInfo.getFileName()),
                        fileInfo.getData(),
                        StandardOpenOption.CREATE);
                ctx.writeAndFlush(new ListFilesServer(getServerFiles()));
                LOG.info("Server successfully received file: {}", fileInfo);
            }
            LOG.info("Client tries to upload a folder {}", fileInfo.getFileName(), fileInfo.getFileType());
        }

        if (massage instanceof FileRequest){
            LOG.info("Client requested to download the file: {}", ((FileRequest) massage).getFileName());
            FileRequest request = (FileRequest) massage;
            ctx.writeAndFlush(new FileInfo(Paths.get(serverDir, request.getFileName())));
        }
    }

    private List<String> getServerFiles() throws IOException {
        return Files.list(Paths.get(serverDir))
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
