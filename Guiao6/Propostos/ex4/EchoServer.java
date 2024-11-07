import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EchoServer {

    // Classe aninhada para armazenamento partilhado de soma e contagem
    static class Register {
        private int sum = 0;
        private int count = 0;
        private final Lock l = new ReentrantLock();

        public void add(int val) {
            l.lock();
            try {
                sum += val;
                count += 1;
            } finally {
                l.unlock();
            }
        }

        public int getSum() {
            l.lock();
            try {
                return sum;
            } finally {
                l.unlock();
            }
        }

        public double average() {
            l.lock();
            try {
                return count > 0 ? (double) sum / count : 0;
            } finally {
                l.unlock();
            }
        }
    }

    // Classe alinhada ClientHandler para tratar cada conexão cliente
    static class ClientHandler extends Thread {
        private final Socket socket;
        private final Register register;

        public ClientHandler(Socket socket, Register register) {
            this.socket = socket;
            this.register = register;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String line;
                while ((line = in.readLine()) != null) {
                    try {
                        int num = Integer.parseInt(line);
                        register.add(num);
                        out.println(register.getSum());  // Enviar apenas a soma acumulada como número
                    } catch (NumberFormatException e) {
                        // Ignorar entradas inválidas e não enviar resposta
                    }
                }

                // Envia apenas a média acumulada ao cliente ao fechar a conexão
                out.println(register.average());

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Register register = new Register(); // Instância do registo partilhado

        try (ServerSocket ss = new ServerSocket(12345)) {
            while (true) {
                Socket socket = ss.accept();
                new ClientHandler(socket, register).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
