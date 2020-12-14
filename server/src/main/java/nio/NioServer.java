package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NioServer {

    private final ByteBuffer buffer = ByteBuffer.allocate(256);
    private String path = "server/serverFiles";
    private String pathUpDir = "";
    private ServerSocketChannel serverChannel;
    private Selector selector;

    public NioServer() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8189));
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");
        while (serverChannel.isOpen()) {
            selector.select(); // block
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey selectionKey = keyIterator.next();
                if (selectionKey.isAcceptable()) {
                    System.out.println("Client accepted");
                    processAccept();
                }
                if (selectionKey.isReadable()) {
                    processRead(selectionKey);
                }
                keyIterator.remove();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NioServer();
    }

    private void processRead(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        int read = channel.read(buffer);
        if (read == -1) {
            channel.close();
        }
        if (read > 0) {
            buffer.flip();
            StringBuilder msg = new StringBuilder();
            while (buffer.hasRemaining()) {
                msg.append((char) buffer.get());
            }
            System.out.println("Received: " + msg);
            buffer.clear();
            String message = msg.toString().trim();
            if (message.equals("--list")) {
                channel.write(ByteBuffer.wrap(("cd - перейти на папку выше\n\r" +
                                                "mkdir - создать папку, mkdir dirname\n\r" +
                                                "touch - созадть файл, touch filename\n\r" +
                                                "ls - показать список файлов\n\r" +
                                                "write - записать в файл\n\r" +
                                                "echo - эхо, echo message\n\r" +
                                                "cat - вывести содержимое файла, cat filename\n\r").getBytes(StandardCharsets.UTF_8)));
            } else if (message.startsWith("cat ")) {
                // TODO: 11.12.2020  вывести содержимое файла
                String fileName = message.split(" +")[1];
                if (Files.exists(Path.of(path, fileName))){
                    // прочитать файл, пока не учитывал размеры файла
                    byte[] byteBuffer = Files.readAllBytes(Path.of(path, fileName));
                    // записать файл в канал
                    channel.write(ByteBuffer.wrap(byteBuffer));
                }
            } else if (message.startsWith("touch ")) {
                // TODO: 11.12.2020 создать файл
                String fileName = message.split(" +")[1];
                if (Files.notExists(Path.of(path, fileName))){
                    Files.createFile(Path.of(path, fileName));
                }
            } else if (message.startsWith("echo ")) {
                // TODO: 11.12.2020 эхо
                byte[] echoMsg = new byte[256];
                // выводим текст после "echo "
                for (int i = 5; i < message.getBytes().length; i++) {
                    echoMsg[i] = message.getBytes()[i];
                }
                channel.write(ByteBuffer.wrap(echoMsg));
            } else if (message.equals("cd")) {
                // TODO: 11.12.2020 переместиться на деректорию выше
                if(Files.isDirectory(Path.of(path))) {
//                    Вариант 2 со сплитом строки path
//                    String[] pathDirArr = path.split("/");
//                    for (int i = 0; i < pathDirArr.length - 1; i++) {
//                        if ((pathDirArr.length - i) > 2) {
//                            pathUpDir = pathUpDir + pathDirArr[i] + "/";
//                        } else pathUpDir = pathUpDir + pathDirArr[i];
//                    }
//                    System.out.println("Parent path is " + pathUpDir);
//                }
//                if (Files.isDirectory(Path.of(pathUpDir))){
//                    path = pathUpDir;
//                    System.out.println("New path is " + path);
//                }
                    Path pathDir = Path.of(path);
                    if (Files.isDirectory(pathDir)) {
                        path = pathDir.getParent().toString();
                        System.out.println("New path is " + path);
                    }
                }
            } else if (message.equals("ls")) {
                String info = Files.list(Path.of(path))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.joining(", "));
                info += "\n\r";
                channel.write(ByteBuffer.wrap(info.getBytes(StandardCharsets.UTF_8)));
            } else if (message.startsWith("mkdir ")) {
                String dirName = message.split(" +")[1];
                if (Files.notExists(Path.of(path, dirName))) {
                    Files.createDirectory(Path.of(path, dirName));
                }
            } else {
                channel.write(ByteBuffer.wrap("Unknown command!\n\r".getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    private void processAccept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        buffer.put("Welcome to MikeOS. Show commands with --list\n\r".getBytes(StandardCharsets.UTF_8));
        buffer.rewind();
        channel.write(buffer);
        buffer.clear();
    }
}
