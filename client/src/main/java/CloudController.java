import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CloudController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(CloudController.class);
    private String clientDir = "client/clientDir";

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    public ListView<String> clientListView;
    public ListView<String> serverListView; // заменить на ViewTables

    public void uploadInCloud(ActionEvent actionEvent) throws IOException {
        String fileNameFromClient = clientListView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileInfo(Paths.get(clientDir, fileNameFromClient)));
        os.flush();
        fillServerData();
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileNameFromServer = serverListView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileNameFromServer));
        os.flush();

        try {
            FileInfo fileInfo = (FileInfo) is.readObject();
            if (fileInfo.getFileType().toString() == "FILE") {
                Files.write(Paths.get(clientDir, fileInfo.getFileName()),
                        fileInfo.getData(),
                        StandardOpenOption.CREATE_NEW);
            } else {
                LOG.info("");
//                для передачи папки
//                Files.createDirectories(Paths.get(clientDir, fileInfo.getFileName()));
            }

        } catch(ClassNotFoundException e){
                e.printStackTrace();
                throw new RuntimeException("File not found on server");
            }

        fillClientData();
    }


    private void fillServerData() {
        try {
            serverListView.getItems().clear();
            serverListView.getItems().addAll(getServerFiles());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not fill server files");
        }
    }

    private void fillClientData() {
        try {
            clientListView.getItems().clear();
            clientListView.getItems().addAll(getClientFiles());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not fill client files");
        }
    }

    private List<String> getServerFiles() throws IOException, ClassNotFoundException {
        os.writeObject(new ListRequest());
        os.flush();
        // записать в отдельный тред
        ListFilesServer lf = (ListFilesServer) is.readObject();
        return lf.getFiles();
//        List<String> ls = new ArrayList<String>();
//        process(new ListFilesServer(ls));
//        return ls;
//        return process((AbstractMassage) new ListFilesServer);
    }

    private List<String> getClientFiles() throws IOException {
        Path clientDirPath = Paths.get(clientDir);
        return Files.list(clientDirPath)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8190);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

//            Thread readThread = new Thread(()->{
//                while (true) {
//                    try {
//                        AbstractMassage massage = (AbstractMassage) is.readObject();
//                        process(massage);
//                    } catch (ClassNotFoundException | IOException e) {
//                        e.printStackTrace();
//                    }                 }
//            });
//            readThread.setDaemon(true);
//            readThread.start();

            fillClientData();
            fillServerData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void process(AbstractMassage massage) throws IOException, ClassNotFoundException {
//        if (massage instanceof ListFilesServer){
//            ListFilesServer ls = (ListFilesServer) massage;
//            getServerFiles().addAll(ls.getFiles());
//        }
//    }

    public void deleteFileInClient(ActionEvent actionEvent) {
    }

    public void deleteFileInCloud(ActionEvent actionEvent) {
    }

    public void renameFileInClient(ActionEvent actionEvent) {
    }

    public void renameFileInCloud(ActionEvent actionEvent) {
    }
}
