public class FileRequest extends AbstractMassage{
    private final String fileName;

    public FileRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
