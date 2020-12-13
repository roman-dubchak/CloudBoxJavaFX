package nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class FileChannelsIntro {
    public static void main(String[] args) throws IOException {
//        Path path = Path.of("server", "serverFiles", "User1", "srv.txt");
//        SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.APPEND);
        RandomAccessFile raf = new RandomAccessFile("server/serverFiles/srv.txt", "rw");
        FileChannel channel = raf.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(20);
        //  1 2 _ _ _ .... | cap (write)
        // |                    pos
        //    |                    lim
//        buffer.put("Hello world".getBytes(StandardCharsets.UTF_8));
//        buffer.flip();
//        buffer.clear();
//        channel.write(buffer);
        channel.read(buffer);
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.print((char) buffer.get());
        }
        System.out.println();
        buffer.rewind();
        buffer.put("OK".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.print((char) buffer.get());
        }
    }
}
