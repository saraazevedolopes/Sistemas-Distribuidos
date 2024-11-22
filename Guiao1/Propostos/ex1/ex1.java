// javac ex1.java
// java Main 2 10

class Increment implements Runnable {        
    private final long I;        
    
    public Increment(long i) {                
        this.I = i;                
    }        
    
    public void run() {                
        
        for(long i = 0; i < I; i++) {                        
            System.out.println(i+1);                        
        }                
    }        
}

class Main {        
    public static void main(String[] args) throws InterruptedException {                
        final int N = Integer.parseInt(args[0]); // Número de threads                
        final long I = Long.parseLong(args[1]);  // Número de iterações para cada thread                
        
        Thread[] a = new Thread[N];                
        
        // Criação e inicialização das threads                
        for (int i = 0; i < N; i++) {                        
            a[i] = new Thread(new Increment(I));                        
            a[i].start(); // podia estar num for diferente                        
        }                
        
        // Espera que todas as threads terminem                
        for (int i = 0; i < N; i++) {                        
            a[i].join();                        
        }                
        
        System.out.println("Fim");                
    }        
}
