import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier {
    private final int N; // Número de threads que precisam sincronizar
    private int count = 0; // Contador de threads que já invocaram await
    private final Lock l = new ReentrantLock();
    private final Condition condition = l.newCondition();

    public Barrier(int N) {
        this.N = N;
    }

    public void await() throws InterruptedException {
        l.lock(); // Vamos estar a trabalhar com variáveis de estado partilhado
        try {
            count++; // Incrementa o número de threads que invocaram await
            if(count < N) { // Uso se if não chega
                while (count < N) { // Não se pode usar if, porque uma thread pode ser acordada antes de todas as outras chegarem à barreira, levando-a a prosseguir sem a condição count < N ser realmente satisfeita
                    condition.await(); // Bloqueia até que todas as N threads cheguem
                }
            } else {
                condition.signalAll(); // Liberta todas as threads
            } // if else por causa da última
        } finally {
            l.unlock();
        }
    }
}
