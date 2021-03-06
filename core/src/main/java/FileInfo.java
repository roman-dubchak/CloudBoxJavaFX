import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo extends AbstractMassage {
    private static final long serialVersionUID = 21980928349190L;

    public enum FileType {
        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

    private final String fileName;
    private final byte [] data;
    private final int num;



    private boolean finish;
    private FileType fileType;
    private Long fileSize;
    private LocalDateTime lastModified;

    public String getFileName() {
        return fileName;
    }
    public byte[] getData() {
        return data;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    public FileType getFileType() {
        return fileType;
    }

//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }

    public int getNum() {
        return num;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish() {
        finish = true;
    }

    public FileInfo(String fileName, byte[] data, int num) {
        this.fileName = fileName;
        this.data = data;
        this.num = num;
    }

//    public FileInfo(Path path) {
//        try {
//            this.fileName = path.getFileName().toString();
//            this.data = Files.readAllBytes(path); // продумать для больших файлов
//            this.fileSize = Files.size(path);
//            this.fileType = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
//            if (this.fileType == FileType.DIRECTORY){
//                this.fileSize = -1L;
//            }
//            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(),
//                                ZoneOffset.ofHours(0));
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Unable to create file info from path");
//        }
//    }

    @Override
    public String toString() {
        return "File{"+
                "Last modified " + lastModified +
                ", File name " + fileName + "\'" +
                ", Data file " + data + "\'" +
                ", Type file " + fileType + "\'" +
                ", Size File " + fileSize + "\'" + "}";
    }
}
