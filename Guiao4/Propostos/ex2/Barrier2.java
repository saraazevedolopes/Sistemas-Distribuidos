import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier2 {
    private final int N; // Número de threads que precisam sincronizar
    private int count = 0; // Contador de threads que já invocaram await
    private int returned = 0; // Contador de threads que retornaram
    private final Lock l = new ReentrantLock(); // Lock para sincronização
    private final Condition cond = l.newCondition(); // Condição associada ao lock
    private boolean open = false; // Indica se a barreira está aberta

    public Barrier2(int N) {
        this.N = N;
    }

    public void await() throws InterruptedException {
        l.lock(); 
        try {
            while(open) { 
                cond.await();
            }

            /* Aguardar enquanto a barreira está aberta é necessário, porque 
            indica que a fase de sincronização atual ainda está em andamento, 
            e a nova thread não pode avançar até que todas as threads tenham 
            completado essa fase antes de passar para a próxima. */

            count++; // Incrementa o número de threads que invocaram await
            if(count < N) {
                while (count < N) { // Não se pode usar if !
                    cond.await(); // Bloqueia até que todas as N threads cheguem
                }
            } else {
                cond.signalAll(); // Liberta todas as threads
                open = true;
                // counter = 0; quando as threads forem a adquirir o lock, o counter está a 0 e ficam todas adormecidas
            } // if else por causa da última
            // returned ++; funciona apenas 99% do tempo, é possível usar a barreira, quando nem tudo voltou ao início, uma thread mais adiantada
            // if(returned == N) {
            //     count = 0;
            //     returned = 0;
            // }

            returned ++; 
            if(returned == N) { // todas as threads que estavam à espera na barreira acordaram e retornaram.
                count = 0;
                returned = 0;
                open = false;
                cond.signalAll(); // acordar as threads da próxima barreira à espera
            }

        } finally {
            l.unlock();
        }
    }
}
