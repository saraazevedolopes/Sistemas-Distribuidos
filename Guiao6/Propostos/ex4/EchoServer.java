import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class Regsiter {
    private int sum = 0;
    private int count = 0;
    private Lock l = new ReentrantLock();
    public void add(int val) {
        l.lock();
        try {
        sum += val;
        count += 1;
        } finally {
            l.unlock();
        }
    }
    public int average 
}

class ClientHandler extends Thread {
    private Socket socket;
    private Register register;
    public ClientHandler(Socket socket) { this.socket = socket; }

    public void run() {
        while (true) {
                Socket socket = ss.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                int sum = 0;
                // int count = 0;
                String line;
                while ((line = in.readLine()) != null) {
                    int num = Integer.parseInt(line);
                    sum += num;
                    register.add(num);
                    //count += 1;
                    out.println(sum);
                    out.flush();
                }
                int average = count > 0 ? sum / count : 0;
                out.println(average);
                // socket.shutdownOutput(); não é preciso, só para ilustrar
                // socket.shutdownInput();
                out.flush();
                socket.close();
    }
}

public class EchoServer {

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(12345);

    while(true) {
        Socket socket = ss.accept;
        new Client
    }
            
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
