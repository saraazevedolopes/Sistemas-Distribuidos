import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EchoClient {

    public static void main(String[] args) {
        try (
            Socket socket = new Socket("localhost", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream()); 
            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Escreva um número para enviar ao servidor e use Ctrl+D para terminar:");

            String userInput;
            while ((userInput = systemIn.readLine()) != null) {  // Lê até Ctrl+D
                try {
                    Integer.parseInt(userInput); // Validação de entrada para garantir que é um número
                    out.println(userInput);
                    out.flush();

                    // Receber e mostrar a soma acumulada
                    String response = in.readLine();
                    System.out.println("Soma acumulada: " + response);
                } catch (NumberFormatException e) {
                    System.out.println("Erro: Entrada inválida, por favor insira um número inteiro.");
                }
            }

            // Indica ao servidor que a comunicação acabou
            socket.shutdownOutput();

            // Receber e mostrar a média ao fechar
            String average = in.readLine();
            System.out.println("Média de todos os números: " + average);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
