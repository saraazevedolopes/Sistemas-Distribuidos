import java.util.Random;

// feito por mim, tem outro teste feito por mim no outro ficheiro

class Mover implements Runnable {
    Bank b;
    int s;

    public Mover(Bank b, int s) {
        this.b = b;
        this.s = s; // Inicializar 's'
    }

    public void run() {
        System.out.println("Started thread-" + Thread.currentThread().getId());
        final int moves = 10000000;
        int from, to;
        Random rand = new Random();

        for (int m = 0; m < moves; m++) {
            from = rand.nextInt(s);
            while ((to = rand.nextInt(s)) == from); // Garantir que 'from' e 'to' são diferentes
            b.transfer(from, to, 1);
        }
    }
}

public class BankTest {
    public static void main(String[] args) throws InterruptedException {
        final int N = 10;

        Bank b = new Bank();

        // Teste 1: Criar contas e fazer transferências simultâneas
        System.out.println("=== Teste 1: Transferências Simultâneas ===");

        for (int i = 0; i < N; i++) {
            b.createAccount(1000);
        }

        System.out.println("Saldo total inicial: " + b.totalBalance(new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }));

        // Criar duas threads que fazem transferências entre contas
        Thread t1 = new Thread(new Mover(b, N));
        Thread t2 = new Thread(new Mover(b, N));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Saldo total final após transferências: " + b.totalBalance(new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }));

        // Teste 2: Fecho de contas enquanto transferências ocorrem
        System.out.println("\n=== Teste 2: Fecho de Contas Durante Transferências ===");

        for (int i = 0; i < N; i++) {
            b.createAccount(1000);
        }

        System.out.println("Saldo total inicial: " + b.totalBalance(new int[]{0,1,2,3,4,5,6,7,8,9}));

        // Criar duas threads que fazem transferências
        Thread transfer1 = new Thread(new Mover(b, N));
        Thread transfer2 = new Thread(new Mover(b, N));

        transfer1.start();
        transfer2.start();

        // Fechar uma conta enquanto as threads estão a fazer transferências
        Thread closer = new Thread(() -> {
            try {
                Thread.sleep(100); // Pequena pausa para garantir que as threads já começaram
                int balance = b.closeAccount(5);
                System.out.println("Conta 5 fechada com saldo: " + balance);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        closer.start();
        transfer1.join();
        transfer2.join();
        closer.join();

        System.out.println("Saldo total final após fechar a conta 5: " + b.totalBalance(new int[]{0,1,2,3,4,6,7,8,9}));

        // Teste 3: Deadlock em Transferências Opostas
        System.out.println("\n=== Teste 3: Deadlock em Transferências Opostas ===");

        int acc1 = b.createAccount(1000);
        int acc2 = b.createAccount(1000);

        System.out.println("Saldo total inicial: " + b.totalBalance(new int[]{acc1, acc2}));

        // Thread que transfere de acc1 para acc2
        Thread oppTrans1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                b.transfer(acc1, acc2, 10);
            }
        });

        // Thread que transfere de acc2 para acc1
        Thread oppTrans2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                b.transfer(acc2, acc1, 10);
            }
        });

        oppTrans1.start();
        oppTrans2.start();
        oppTrans1.join();
        oppTrans2.join();

        System.out.println("Saldo total final após transferências opostas: " + b.totalBalance(new int[]{acc1, acc2}));

        // Teste 4: Operações de Saldo e Depósitos Simultâneos
        System.out.println("\n=== Teste 4: Operações de Saldo e Depósitos Simultâneos ===");

        int acc3 = b.createAccount(1000);

        // Thread que verifica o saldo repetidamente
        Thread balanceChecker = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                System.out.println("Saldo da conta: " + b.balance(acc3));
            }
        });

        // Thread que faz depósitos repetidos
        Thread depositor = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                b.deposit(acc3, 10);
            }
        });

        balanceChecker.start();
        depositor.start();
        balanceChecker.join();
        depositor.join();

        System.out.println("Saldo final após depósitos e verificações de saldo: " + b.balance(acc3));
    }
}
