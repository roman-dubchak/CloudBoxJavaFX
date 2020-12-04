import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ConcurrentLinkedDeque<Handler> clients;
    private final ExecutorService worker;

    public Server() throws IOException {
        clients = new ConcurrentLinkedDeque<>();
        worker = Executors.newFixedThreadPool(4);
        try (ServerSocket server = new ServerSocket(8189)) {
            System.out.println("Server started!");
            while (true) {
                System.out.println("Waiting connection!");
                Socket socket = server.accept();
                System.out.println("Client connected!");
                Handler handler = new Handler(this, socket);
                clients.add(handler);
                worker.execute(handler);
            }
        }
    }

    public void sendMessageForAll(String message) throws IOException {
        for (Handler client : clients) {
            client.writeMessage(message);
        }
    }

    public void kick(Handler handler) {
        clients.remove(handler);
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
