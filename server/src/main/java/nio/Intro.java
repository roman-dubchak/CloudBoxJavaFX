package nio;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Intro {

    static void pathTest() {
        Path path = Path.of("server", "serverFiles", "User1", "srv.txt");
        System.out.println(path.toAbsolutePath().toString());
    }

    static void printFileBytes(String fileName) throws IOException {
        System.out.println(Arrays.toString(
                Intro.class.getResourceAsStream(fileName).readAllBytes()));
    }

    static void testWatcher() throws IOException {
        Path path = Path.of("server", "serverFiles");
        WatchService service = FileSystems.getDefault().newWatchService();
        new Thread(() -> {
            while (true) {
                WatchKey key = null;
                try {
                    key = service.take(); // block
                    if (key.isValid()) {
                        List<WatchEvent<?>> watchEvents = key.pollEvents();
                        for (WatchEvent<?> watchEvent : watchEvents) {
                            System.out.println(watchEvent.context());
                            System.out.println(watchEvent.kind());
                            System.out.println(watchEvent.count());
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        path.register(service,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
    }

    static void testFiles() throws IOException {
//        Files.copy(
//                Path.of("server", "serverFiles", "User1", "srv.txt"),
//                Path.of("server", "serverFiles", "User2", "srv.txt"),
//                StandardCopyOption.REPLACE_EXISTING);

//        Files.write(Path.of("server", "serverFiles", "User2", "srv.txt"),
//                List.of("Hello", "world", "lol"),
//                StandardOpenOption.CREATE);

        Files.walkFileTree(Path.of(""), Set.of(FileVisitOption.values()), 2,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println(file.toString());
                        return super.visitFile(file, attrs);
                    }
                });


    }

    public static void main(String[] args) throws IOException {
        testFiles();
    }
}
