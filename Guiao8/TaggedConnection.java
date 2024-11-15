public class TaggedConnection implements AutoCloseable {
        public static class Frame {
            public final int tag;
            public final byte[] data;
            public Frame(int tag, byte[] data) { this.tag = tag; this.data = data; }
        }
                
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

        public void send(int tag, byte[] data) throws IOException {
            ls.lock();
            try {
                os.writeInt(4 + data.length); // respeita o formato de uma FramedConnection
                os.writeInt(tag);
                os.write(data); 

                os.flush();
            } finally {
                ls.unlock();
            }
        }

        public void send(Frame Frame) throws IOException {
            send(frame.tag, frame.data);
        }

        public Frame receive() throws IOException {
            lr.lock();
            try {
                int length = is.readInt(); 
                int tag = is.readInt();
                byte[] data = new byte[length];
                is.readFully(data); 
                return new Frame(tag, data);
            } finally {
                lr.unlock();
            }
        }

        public void close() throws IOException {
            socket.close();
        }
}