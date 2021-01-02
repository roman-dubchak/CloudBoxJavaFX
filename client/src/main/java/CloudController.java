import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
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

    private Scene scene;

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
        if (fileNameFromClient.contains("[DIR]")) return;
        os.writeObject(new FileInfo(Paths.get(clientDir, fileNameFromClient)));
        os.flush();
        clientListView.requestFocus();
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileNameFromServer = serverListView.getSelectionModel().getSelectedItem();
        if (fileNameFromServer.contains("[DIR]")) return;
        os.writeObject(new FileRequest(fileNameFromServer));
        os.flush();
        serverListView.requestFocus();
    }

    private void fillClientViews() {
        try {
            clientListView.getItems().clear();
            clientListView.getItems().addAll(Files.list(Paths.get(clientDir))
                    .map(path ->{
                        if (Files.isDirectory(path)){
                            return "[DIR]" + path.getFileName().toString();
                        } else return path.getFileName().toString();
                    }).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not fill client files");
        }
    }

    private void fillServerViews (List<String> list) {
        serverListView.getItems().clear();
        serverListView.getItems().addAll(list);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hBoxTextField.setVisible(false);
        hBoxTextField.setPrefSize(0.0,0.0);
        hBoxTextFieldServer.setVisible(false);
        hBoxTextFieldServer.setPrefSize(0.0,0.0);

        try {
            Socket socket = new Socket("localhost", 8190);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            fillClientViews();
            os.writeObject(new ListRequest());
            os.flush();

            Thread readThread = new Thread(()->{
                while (true) {
                    try {
                        AbstractMassage massage = (AbstractMassage) is.readObject();
                        Platform.runLater(() -> {
                            try {
                                process(massage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        });

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

    // TODO: [DIR]

    private void process(AbstractMassage massage) throws IOException, ClassNotFoundException {
        if (massage instanceof ListFilesServer){
            ListFilesServer list = (ListFilesServer) massage;
            LOG.info((((ListFilesServer)massage).getFiles()).toString());
            fillServerViews(list.getFiles());
                    }
        if (massage instanceof FileInfo){
            FileInfo fileInfo = (FileInfo) massage;
            if (fileInfo.getFileType().toString() == "FILE") {
                Files.write(Paths.get(clientDir, fileInfo.getFileName()),
                        fileInfo.getData(),
                        StandardOpenOption.CREATE);

            } else {
                LOG.info("");
            }
        }
        fillClientViews();
    }

    public void deleteFileInClient(ActionEvent actionEvent) {
        String fileNameFromClientDel = clientListView.getSelectionModel().getSelectedItem();
        try {
            Files.delete(Paths.get(clientDir,fileNameFromClientDel));
            LOG.info("Client to delete the file {}", fileNameFromClientDel);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File not found on client directory");
        }
        fillClientViews();
    }

    public void deleteFileInCloud(ActionEvent actionEvent) {
        String fileNameFromServerDel = serverListView.getSelectionModel().getSelectedItem();
        serverListView.requestFocus();
        try {
            os.writeObject(new FileRequestDelete(fileNameFromServerDel));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameFileInClient(ActionEvent actionEvent) {
        String fileNameFromClientRename = clientListView.getSelectionModel().getSelectedItem();
        hBoxTextField.setVisible(true);
        hBoxTextField.setPrefSize(450.0,40.0);
        textField.requestFocus();

        oldFile = new File(Paths.get(clientDir, fileNameFromClientRename).toString());
        LOG.info("Try rename file {}", oldFile.getName());

    }

    // TODO: POPUP WINDOW

    public void renamePopup(ActionEvent actionEvent) {
        String newNameFile = textField.getText();
        LOG.info("Text for name {}", newNameFile);

        newFile = new File(Paths.get(clientDir, newNameFile).toString());
        LOG.info("newfile with new name {}", newFile.getName());

        if (oldFile.renameTo(newFile)){
            LOG.info("Rename the oldFile to {}", newFile.getName());
        } else LOG.info("DIDN'T rename the oldFile to {}", newFile.getName());

        fillClientViews();
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

        textFieldServer.clear();
        hBoxTextFieldServer.setVisible(false);
        hBoxTextFieldServer.setPrefSize(0.0,0.0);
        serverListView.requestFocus();
    }
}
