import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class ClientHandler extends Thread { // Classe para tratar cada cliente numa nova thread
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream())) {

            int sum = 0;
            int count = 0;
            String line;

            // Processa cada número enviado pelo cliente e calcula a soma acumulada
            while ((line = in.readLine()) != null) {
                int num = Integer.parseInt(line);
                sum += num;
                count++;
                out.println(sum); // Apenas envia o valor acumulado sem mensagem
                out.flush();
            }

            // Envia a média ao cliente quando ele termina de enviar dados
            int average = count > 0 ? sum / count : 0;
            out.println(average); // Apenas envia o valor médio
            out.flush();

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

public class EchoServer {
    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();  // Inicia uma nova thread para o cliente
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
