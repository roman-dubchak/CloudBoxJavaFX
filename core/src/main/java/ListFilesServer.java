import java.util.List;

public class ListFilesServer extends AbstractMassage{

    private final List<String> files;

    public ListFilesServer(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }
}
