/* Alternativa a Barrier2. Tanto Barrier2 como Barrier3, são soluções corretas para o exercício 2 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Barrier3 {
    private Lock l = new ReentrantLock();
    private Condition cond = l.newCondition();

    private final int N;
    private int ctr = 0;
    private int phase = 0;

    public Barrier3(int N) { 
        this.N = N; 
    }

    public void await() throws InterruptedException {
        l.lock();
        try {
            int phase = this.phase;
            this.ctr += 1;
            if (this.ctr < N) {
                while (this.phase == phase)
                    cond.await();
            } else {
                cond.signalAll();
                this.ctr = 0;
                this.phase += 1;
            }
        } finally {
            l.unlock();
        }
    }
}
