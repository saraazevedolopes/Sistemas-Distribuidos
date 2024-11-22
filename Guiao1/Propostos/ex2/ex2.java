// javac ex2.java
// java Main 10 1000 100, não é suposto dar o valor esperado

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
        
        /* Quando várias threads executam simultaneamente, podem ler e modificar o saldo ao mesmo tempo,                 
        causando condições de corrida. Isso resulta em incrementos incorretos no saldo. Seria necessário                 
        um acesso ao método deposit, de cada vez */                
    }        
    
    // Our single account, for now        
    private Account savings = new Account(0);        
    
    // Account balance        
    public int balance() {                
        return savings.balance();                
    }        
    
    // Deposit        
    boolean deposit(int value) {                
        return savings.deposit(value);                
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
        for(long i = 0; i < I; i++) {                        
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
