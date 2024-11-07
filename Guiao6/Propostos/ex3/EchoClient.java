import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EchoClient {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput;
            while ((userInput = systemIn.readLine()) != null) {
                out.println(userInput);
                out.flush();

                String response = in.readLine();
                if (response != null) {
                    System.out.println("Soma até agora: " + response);
                }
            }

            // Se o utilizador pressionar Ctrl+D, sai do loop e finaliza a comunicação
            socket.shutdownOutput(); // Informa ao servidor que o cliente terminou de enviar dados
            String average = in.readLine(); // Recebe a média final do servidor, se disponível
            if (average != null) {
                System.out.println("Média: " + average);
            }

            socket.close(); // Fecha o socket após encerrar comunicação com o servidor

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
