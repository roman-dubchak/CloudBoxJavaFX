import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ListFilesServer extends AbstractMassage{

    private final List<String> files;

    public ListFilesServer(Path path) throws IOException {
        this.files = Files.list(path).
                map(p ->{
            if (Files.isDirectory(p)){
                return "[DIR]" + p.getFileName().toString();
            } else return p.getFileName().toString();
            }).collect(Collectors.toList());
    }

    public List<String> getFiles() {
        return files;
    }
}
