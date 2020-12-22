import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import jdk.internal.util.xml.impl.ReaderUTF8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CloudController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(CloudController.class);
    private String clientDir = "client/clientDir";
    private String serverFiles = "server/serverFiles/User1";


    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    public ListView<String> clientListView;
    public ListView<String> serverListView; // заменить на ViewTables

    public void uploadInCloud(ActionEvent actionEvent) throws IOException {
        String fileNameFromClient = clientListView.getSelectionModel().getSelectedItem();
        File fileInClient = new File(clientDir + "/" + fileNameFromClient);
        os.writeObject(new FileInfo(fileInClient.toPath()));
        os.flush();
        try {
            FileInfo fileInfoFromServer = (FileInfo) is.readObject();
            LOG.info("File in server" + fileInfoFromServer);
            String fileNameFromServer = fileInfoFromServer.getFileName();
            Path pathFileInServer = Paths.get(serverFiles + "/" + fileNameFromServer);
            if (Files.notExists(pathFileInServer)){
                File fileInServer = new File(pathFileInServer.toString());
                byte [] dataFileFromServer = fileInfoFromServer.getDataFile();
                Files.readAllBytes(pathFileInServer);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        fillServerData();
    }

    public void download(ActionEvent actionEvent) throws IOException {
        fillClientData();
    }

    private void fillServerData() {
        try {
            serverListView.getItems().clear();
            serverListView.getItems().addAll(getServerFiles());
        } catch (IOException e) {
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

    private List<String> getServerFiles() throws IOException {
        // ctrl + alt + v, cmd + opt + v
        Path serverDirPath = Paths.get(serverFiles);
        // Files
        return Files.list(serverDirPath)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
    }

    private List<String> getClientFiles() throws IOException {
        Path clientDirPath = Paths.get(clientDir);
        return Files.list(clientDirPath)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fillClientData();
        fillServerData();
        try {
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFileInClient(ActionEvent actionEvent) {
    }

    public void deleteFileInCloud(ActionEvent actionEvent) {
    }
}
