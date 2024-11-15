package g8;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

public class SimpleClient {

    public static class FramedConnection implements AutoCloseable {
        private final Socket socket;
        private final DataInputStream is;
        private final DataOutputStream os;
        private final Lock lr = new ReentrantLock();
        private final Lock ls = new ReentrantLock();

        public FramedConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.is = new DataInputStream((new BufferedInputStream(socket.getInputStream())));
            this.os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }

        public void send(byte[] data) throws IOException {
            ls.lock();
            try {
                os.writeInt(data.length); 
                os.write(data); 
                os.flush();
            } finally {
                ls.unlock();
            }
        }

        public byte[] receive() throws IOException {
            lr.lock();
            try {
                int length = is.readInt(); 
                byte[] data = new byte[length];
                is.readFully(data); 
                return data;
            } finally {
                lr.unlock();
            }
        }

        public void close() throws IOException {
            socket.close();
        }
    }

    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        FramedConnection c = new FramedConnection(s);

        // send requests
        c.send("Ola".getBytes());
        c.send("Hola".getBytes()); 
        c.send("Hello".getBytes());

        // get replies
        byte[] b1 = c.receive();
        byte[] b2 = c.receive();
        byte[] b3 = c.receive();
        System.out.println("Some Reply: " + new String(b1));
        System.out.println("Some Reply: " + new String(b2));
        System.out.println("Some Reply: " + new String(b3));

        c.close();
    }
}
