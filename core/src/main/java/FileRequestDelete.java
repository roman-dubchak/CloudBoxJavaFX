public class FileRequestDelete extends AbstractMassage{
    private final String fileName;

    public FileRequestDelete(String oldFileName) {
        this.fileName = oldFileName;

    }

    public String getFileName() {
        return fileName;
    }

}
