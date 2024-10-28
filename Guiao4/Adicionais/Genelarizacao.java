import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Agreement {
    private Lock l = new ReentrantLock();
    private Condition cond = l.newCondition();

    private final int N;
    private int ctr = 0;

    public Agreement(int N) { 
        this.N = N; 
    }

    /* A classe Instance encapsula o resultado das propostas feitas pelas threads,
    permitindo que cada thread atualize seu próprio estado e reinicializando o 
    resultado para novas rodadas de propostas.*/
    private static class Instance { 
        int res = Integer.MIN_VALUE; 
    }

    private Instance current = new Instance();

    public int propose(int choice) throws InterruptedException {
        l.lock();
        try {
            Instance my = this.current;
            my.res = Math.max(my.res, choice);
            this.ctr += 1;
            if (this.ctr < N) {
                while (current == my)
                    cond.await();
            } else {
                cond.signalAll();
                this.ctr = 0;
                current = new Instance(); 
            }
            return my.res;
        } finally {
            l.unlock();
        }
    }
}
