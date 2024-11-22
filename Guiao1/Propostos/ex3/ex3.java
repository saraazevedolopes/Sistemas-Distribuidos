// javac ex2.java
// java Main 10 1000 100, é suposto dar o valor esperado

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Bank {        
    
    private static class Account {                
        private int balance;                
        
        Account(int balance) {                        
            this.balance = balance;                        
        }                
        
        int balance() {                        
            return balance;                        
        }                
        
        boolean deposit(int value) {                         
            balance += value;                        
            return true;                        
        }                
        
        /* void m1() { // exemplo genérico                        
            l.lock();                        
            try { // importante que o try venha depois do lock, pois é sempre invocado o lock independentemente de coisas                                
                // coisas                                
            } finally {                                
                l.unlock();                                
            }                        
            
            // v = null;                        
            // v.meth() estas 2 linhas levam a um deadlock                        
            
            l.unlock();                        
        } */                
    }        
    
    // Our single account, for now        
    private Account savings = new Account(0);        
    Lock l = new ReentrantLock();        
    
    // Account balance        
    public int balance() {                
        return savings.balance();                
    }        
    
    // Deposit        
    boolean deposit(int value) {                
        l.lock(); // lock previne que apenas uma thread de cada vez acesse a deposit, resolvendo o problema do exercício 2                
        try {                        
            return savings.deposit(value);                        
        } finally {                        
            l.unlock();                        
        }                
    }        
}

class Depositor implements Runnable {        
    private final long I;        
    private final Bank b;        
    private final int V;        
    
    public Depositor(long I, Bank b, int V) {                
        this.I = I;                
        this.b = b;                
        this.V = V;                
    }        
    
    public void run() {                
        for (long i = 0; i < I; i++) {                        
            b.deposit(V);                        
        }                
    }        
}

class Main {        
    public static void main(String[] args) throws InterruptedException {                
        final int N = Integer.parseInt(args[0]); // Número de threads                
        final long I = Long.parseLong(args[1]);  // Número de iterações para cada thread                
        final int V = Integer.parseInt(args[2]); // Valor do depósito                
        
        Bank b = new Bank();                
        Thread[] a = new Thread[N];                
        
        // Criação e inicialização das threads                
        for (int i = 0; i < N; i++) {                        
            a[i] = new Thread(new Depositor(I, b, V));                        
            a[i].start(); // podia estar num for diferente                        
        }                
        
        // Espera que todas as threads terminem                
        for (int i = 0; i < N; i++) {                        
            a[i].join();                        
        }                
        
        System.out.println("Saldo final: " + b.balance());                
    }        
}
