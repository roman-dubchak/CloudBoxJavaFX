import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CloudController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(CloudController.class);
    private String clientDir = "client/clientDir";

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private File oldFile;
    private File newFile;
    private String fileNameFromServerRename;

    @FXML
    public HBox hBoxTextField;
    public HBox hBoxTextFieldServer;
    public HBox hBoxButton;
    public TextField textField;
    public TextField textFieldServer;
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
                        StandardOpenOption.CREATE);
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
        hBoxTextField.setVisible(false);
        hBoxTextField.setPrefSize(0.0,0.0);
        hBoxTextFieldServer.setVisible(false);
        hBoxTextFieldServer.setPrefSize(0.0,0.0);
//        hBoxButton.setPrefSize(480.0,50.0);
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
        String fileNameFromClientDel = clientListView.getSelectionModel().getSelectedItem();
        try {
            Files.delete(Paths.get(clientDir,fileNameFromClientDel));
            LOG.info("Client to delete the file {}", fileNameFromClientDel);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File not found on client directory");
        }
        fillClientData();
    }

    public void deleteFileInCloud(ActionEvent actionEvent) {
        String fileNameFromServerDel = serverListView.getSelectionModel().getSelectedItem();
        try {
            os.writeObject(new FileRequestDelete(fileNameFromServerDel));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            getServerFiles();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        fillServerData();
    }

    public void renameFileInClient(ActionEvent actionEvent) {
        String fileNameFromClientRename = clientListView.getSelectionModel().getSelectedItem();

        hBoxTextField.setVisible(true);
        hBoxTextField.setPrefSize(450.0,40.0);
        textField.requestFocus();

        oldFile = new File(Paths.get(clientDir, fileNameFromClientRename).toString());
        LOG.info("Try rename file {}", oldFile.getName());

    }

    public void renamePopup(ActionEvent actionEvent) {
        String newNameFile = textField.getText();
        LOG.info("Text for name {}", newNameFile);

        newFile = new File(Paths.get(clientDir, newNameFile).toString());
        LOG.info("newfile with new name {}", newFile.getName());

        if (oldFile.renameTo(newFile)){
            LOG.info("Rename the oldFile to {}", newFile.getName());
        } else LOG.info("DIDN'T rename the oldFile to {}", newFile.getName());

        fillClientData();
        textField.clear();
        hBoxTextField.setVisible(false);
        hBoxTextField.setPrefSize(0.0,0.0);
        clientListView.requestFocus();
     }

    public void renameFileInCloud(ActionEvent actionEvent) {
        fileNameFromServerRename = serverListView.getSelectionModel().getSelectedItem();

        hBoxTextFieldServer.setVisible(true);
        hBoxTextFieldServer.setPrefSize(450.0,40.0);
        textFieldServer.requestFocus();

    }

    public void renamePopupServer(ActionEvent actionEvent) {
        String newNameFile = textFieldServer.getText();

        try {
            os.writeObject(new FileRequestRename(fileNameFromServerRename, newNameFile));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fillServerData();

        textFieldServer.clear();
        hBoxTextFieldServer.setVisible(false);
        hBoxTextFieldServer.setPrefSize(0.0,0.0);
        serverListView.requestFocus();
    }
}
