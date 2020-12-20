import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

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

    private String clientDir = "client/clientDir";
    private String serverFiles = "server/serverFiles/User1";


    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    public ListView<String> clientListView;
    public ListView<String> serverListView; // заменить на ViewTables

    public void uploadInCloud(ActionEvent actionEvent) throws IOException {
        String fileName = clientListView.getSelectionModel().getSelectedItem();
        os.writeObject(fileName);
        File file = new File(clientDir + "/" + fileName);
        long size = file.length();
        os.writeObject(size);
        os.writeObject(file);

//        byte[] buffer = new byte[255];
//        FileInputStream fis = new FileInputStream(file);
//        for (int i = 0; i < (size + 255) / 256; i++) {
//            int read = fis.read(buffer);
//            Network.get().getOut().write(buffer, 0, read);
//        }
        os.flush();
        fillClientData();
    }

    public void download(ActionEvent actionEvent) throws IOException {
//
//        Network.get().getOut().writeUTF("/download");
//        String fileName = serverListView.getSelectionModel().getSelectedItem();
//        Network.get().getOut().writeUTF(fileName);
//        long size = Network.get().getIn().readLong();
//        File file = new File(clientDir + "/" + fileName);
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//        FileOutputStream fos = new FileOutputStream(file);
//        byte [] buffer = new byte[256];
//        for (int i = 0; i < (size + 255) / 256; i++) {
//            if (i == (size + 255) / 256 - 1) {
//                for (int j = 0; j < size % 256; j++) {
//                    fos.write(Network.get().getIn().readByte());
//                }
//            } else {
//                int read = Network.get().getIn().read(buffer);
//                fos.write(buffer, 0, read);
//            }
//        }
//        fos.close();
        fillServerData();
    }

    private void fillServerData() {
        try {
            serverListView.getItems().clear();
            serverListView.getItems().addAll(getServerFiles());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not fill client files");
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
        Path clientDirPath = Paths.get(serverFiles);
        // Files
        return Files.list(clientDirPath)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
    }

    private List<String> getClientFiles() throws IOException {
        // ctrl + alt + v, cmd + opt + v
        Path clientDirPath = Paths.get(clientDir);
        // Files
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
