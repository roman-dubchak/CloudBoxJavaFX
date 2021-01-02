import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractFileInboundHandler extends SimpleChannelInboundHandler<AbstractMassage> {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractFileInboundHandler.class);
    private String serverDir = "server/serverFiles";
    private Path serverPath;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverPath = Paths.get(serverDir);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMassage massage) throws Exception {
        LOG.info("File info: {}", massage);
        if(massage instanceof ListRequest){
            ctx.writeAndFlush(new ListFilesServer(serverPath));
        } else

        if (massage instanceof MoveRequest){
            // TODO
//            if (Files.isDirectory(Paths.get(MoveRequest)massage)){}
            MoveRequest moveRequest = (MoveRequest) massage;
            serverPath = serverPath.resolve(moveRequest.getDir());
            LOG.info("Move in folder {}", moveRequest.getDir());
            ctx.writeAndFlush(new ListFilesServer(serverPath));
        } else

        if (massage instanceof MoveUpFolder){
            // TODO
//            if (Files.isDirectory(Paths.get(MoveRequest)massage)){}
            serverPath = serverPath.getParent();
            LOG.info("Move up the folder {}", serverPath.toString());
            ctx.writeAndFlush(new ListFilesServer(serverPath));
        } else

        if (massage instanceof FileInfo){
            FileInfo fileInfo = (FileInfo) massage;
            LOG.info("Client upload file type: {}", fileInfo.getFileType());
            if(fileInfo.getFileType().toString() == "FILE") {
                LOG.info("Client upload file: {}", fileInfo);
                Files.write(Paths.get(serverDir, fileInfo.getFileName()),
                        fileInfo.getData(),
                        StandardOpenOption.CREATE);
                ctx.writeAndFlush(new ListFilesServer(serverPath));
                LOG.info("Server successfully received file: {}", fileInfo);
            }
            LOG.info("Client tries to upload a folder {}", fileInfo.getFileName(), fileInfo.getFileType());
        } else

        if (massage instanceof FileRequest) {
            LOG.info("Client requested to download the file: {}", ((FileRequest) massage).getFileName());
            FileRequest request = (FileRequest) massage;
            InputStream in = Files.newInputStream(serverPath.resolve(request.getFileName()));
            byte[] buffer = new byte[10_000];
            int read = 0;
            int count = 1;
            while ((read = in.read(buffer)) != -1) {
                byte[] tmp = new byte[read];
                System.arraycopy(buffer, 0, tmp, 0, read);
                ctx.writeAndFlush(new FileInfo(
                        request.getFileName(),
                        tmp,
                        count
                ));
                count++;
            }

            FileInfo fileFinal = new FileInfo(
                    request.getFileName(),
                    new byte[]{},
                    count
            );
            fileFinal.setFinish();
            ctx.writeAndFlush(fileFinal);

        } else

        if(massage instanceof FileRequestDelete){
            Files.delete(Paths.get(serverDir,((FileRequestDelete) massage).getFileName()));
            LOG.info("Client to delete the file {}", massage);
            ctx.writeAndFlush(new ListFilesServer(serverPath));
        } else

        if(massage instanceof FileRequestRename){
            Files.move((Paths.get(serverDir, ((FileRequestRename)massage).getOldFileName())),
                    (Paths.get(serverDir, ((FileRequestRename)massage).getNewFileName())));

            LOG.info("Client rename file the file {}",
                    ((FileRequestRename) massage).getOldFileName(),
                    ((FileRequestRename) massage).getNewFileName());
            ctx.writeAndFlush(new ListFilesServer(serverPath));
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
