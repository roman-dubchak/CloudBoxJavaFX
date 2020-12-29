public class FileRequestRename extends AbstractMassage{
    private final String oldFileName;
    private final String newFileName;;

    public FileRequestRename(String oldFileName, String newFileName) {
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public String getOldFileName() {
        return oldFileName;
    }
}
