import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Handler implements Runnable {

    private static int inc = 0;
    private String userName;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean running;

    public Handler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        inc++;
        userName = "User" + inc;
        initStreams();
        running = true;
    }

    private void initStreams() throws IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private String readMessage() throws IOException {
        return in.readUTF();
    }

    public void writeMessage(String message) throws IOException {
        out.writeUTF(message);
        out.flush();
    }

    private String wrapMessageWithName(String message) {
        return userName + ": " + message;
    }

    @Override
    public void run() {
        try {
            while (running) {
                String message = readMessage();
                System.out.println("Received: " + message);
                server.sendMessageForAll(wrapMessageWithName(message));
            }
        } catch (Exception e) {
            System.err.println("Exception while read or write!");
            server.kick(this);
        } finally {
            close();
        }
    }

    public void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
