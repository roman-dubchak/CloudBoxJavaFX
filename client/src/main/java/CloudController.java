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
        // создаем файл по реквесту
//        try {
//            FileInfo fileInfoFromServer = (FileInfo) is.readObject();
//            LOG.info("File in server" + fileInfoFromServer);
//            String fileNameFromServer = fileInfoFromServer.getFileName();
//            Path pathFileInServer = Paths.get("server/serverFiles" + fileNameFromServer);
//            if (Files.notExists(pathFileInServer)){
//                File fileInServer = new File(pathFileInServer.toString());
//                byte [] dataFileFromServer = fileInfoFromServer.getData();
//                Files.readAllBytes(pathFileInServer);
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
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
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            fillClientData();
            fillServerData();

            Thread readThread = new Thread(()->{
                while (true) {
                    try {
                        AbstractMassage massage = (AbstractMassage) is.readObject();
                        process(massage);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }                 }
            });
            readThread.setDaemon(true);
            readThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(AbstractMassage massage) {
        if (massage instanceof ListFilesServer){
            ListFilesServer ls = (ListFilesServer) massage;

        }
    }

    public void deleteFileInClient(ActionEvent actionEvent) {
    }

    public void deleteFileInCloud(ActionEvent actionEvent) {
    }
}
