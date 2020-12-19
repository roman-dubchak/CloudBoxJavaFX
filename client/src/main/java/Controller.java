import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> listView;
    public TextField txt;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void sendMessage(ActionEvent event) throws IOException {
        String text = txt.getText();
        out.writeUTF(text);
        out.flush();
        txt.clear();
    }

    private void initStreams() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initStreams();
            Thread reader = new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readUTF();
                        listView.getItems().add(message);
                    }
                } catch (Exception e) {
                    System.err.println("Exception while read!");
                }
            });
            reader.setDaemon(true);
            reader.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        listView.addEventHandler(EventType.ROOT, event -> {
//            if (event.getEventType().toString().equals("MOUSE_RELEASED")) {
//                int index = listView.getSelectionModel().getSelectedIndex();
//                listView.getItems().remove(index);
//            }
//        });
    }

    public void download(ActionEvent actionEvent) {

    }

    public void updateCient(ActionEvent actionEvent) {
    }

    public void uploadInCloud(ActionEvent actionEvent) {

    }

    public void updateCloud(ActionEvent actionEvent) {

    }
}
