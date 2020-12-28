public class FileRequestDelete extends AbstractMassage{
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public FileRequestDelete(String fileName) {
        this.fileName = fileName;
    }
}
