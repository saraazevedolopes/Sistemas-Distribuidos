import java.util.concurrent.locks.*;

/* A nova solução é mais eficiente, porque permite mais operações concorrentes ao reduzir o 
escopo dos bloqueios para cada conta individual, em vez de bloquear o banco inteiro. Isso 
melhora o desempenho geral, especialmente em cenários em que muitas threads realizam operações 
em contas diferentes ao mesmo tempo.

Resumo da Evolução:
Maior paralelismo e eficiência: O uso de locks ao nível de cada conta permite que o banco 
suporte múltiplas operações simultâneas, enquanto a versão anterior era limitada por um 
bloqueio global.
Segurança contra inconsistências e deadlocks: O novo design leva em consideração potenciais
problemas de deadlock e garante que todas as operações em contas individuais ocorram de 
maneira segura e consistente.

Há sempre bloqueio global para a total balance
*/

public class Bank {

    private static class Account {
        private int balance;

        Lock l = new ReentrantLock();

        // mesmo problema de blocos separados no tempo
        // não quero que aconteça nada entre o "w" e o "d", depois do w e antes do d alguém se pode intrometer
        Account (int balance) { this.balance = balance; }

        int balance () { 
            l.lock();
            try {
                return balance; 
            }
            finally {
                l.unlock();
            }
        }
        
        boolean deposit (int value) {
            l.lock();
            try {
                balance += value;
                return true;
            }
            finally {
                l.unlock();
            }
        }
        boolean withdraw (int value) {
            l.lock();
            try {
            if (value > balance)
                return false;
            balance -= value;
            return true;
            }
            finally {
                l.unlock();
            }
        }
    }

    // Bank slots and vector of accounts
    private final int slots; // usar final sempre que possível
    private Account[] av;
    
    public Bank (int n) {
        slots=n;
        av=new Account[slots];
        for (int i=0; i<slots; i++)
            av[i]=new Account(0);
    }


    // Account balance
    public int balance (int id) {
        if (id < 0 || id >= slots) return 0; 
            return av[id].balance();
    }

    // Deposit
    public boolean deposit (int id, int value) {
        if (id < 0 || id >= slots) return false;
        return av[id].deposit(value); 
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw (int id, int value) {
        if (id < 0 || id >= slots) return false;
        return av[id].withdraw(value);
    }

    // Transfer, pode acontecer algo entre withdraw e deposit, precisa de lock
    public boolean transfer (int from, int to, int value) {
        if (from < 0 || from >= slots || to < 0 || to >= slots) return false; 
        Account cf = av[from];
        Account ct = av[to];
        if(from < to) { // resolve deadlock, ciclo
            cf.l.lock(); // resolve o problema do ciclo
            ct.l.lock();
        }
        else {
            ct.l.lock();
            cf.l.lock();
        }
        try {
            return cf.withdraw(value) && ct.deposit(value); // não usa mais o container (bloqueia 2 contas em vez de av inteiro)
        } finally {
            cf.l.unlock();
            ct.l.unlock();
        }
    }

    /*
    Vamos supor que temos duas contas: conta 1 e conta 2, e duas threads (Thread A e Thread B) a 
    fazer transferências entre essas contas ao mesmo tempo.

    Thread A quer transferir dinheiro de conta 1 para conta 2.
    Thread B quer transferir dinheiro de conta 2 para conta 1.
    Se ambas as threads começarem a bloquear as contas ao mesmo tempo, pode ocorrer o seguinte:

    Thread A bloqueia conta 1.
    Thread B bloqueia conta 2.
    Agora, Thread A quer bloquear conta 2, mas Thread B já a bloqueou.
    Thread B quer bloquear conta 1, mas Thread A já a bloqueou.
    Nenhuma thread pode continuar porque estão esperando que a outra libere o bloqueio na conta que precisam. 
    Isso cria um deadlock, e ambas as threads ficam presas indefinidamente. */

    // ciclos, uma thread quer o lock da outra, mas já tem um lock, deadlock

    // TotalBalance, calcula o saldo total de todas as contas no banco, pode acontecer algo entre balance, precisa de lock
    public int totalBalance () { // 3 loops diferentes, porque é necessário garantir a ordem bloqueio, cálculo, desbloqueio
        int sum = 0;
        for(int i = 0; i < slots; i++) {
            av[i].l.lock();
        }
        for(int i = 0; i < slots; i++) {
            sum += av[i].balance(); 
        }
        for(int i = 0; i < slots; i++) {
            av[i].l.unlock();
        }    
        return sum;
    }
}

/*
    Thread A está calcula o saldo total, e ela bloqueia a conta 1, lê o saldo e desbloqueia a conta 1. 
    Nesse ponto, a Thread B pode intervir e fazer uma transferência ou depósito que altera o saldo da 
    conta 1 ou de outras contas que ainda não foram processadas pela Thread A.

    Quando a Thread A for calcular o saldo da próxima conta (conta 2, por exemplo), o saldo da conta 1 
    já pode ter sido modificado por outra operação da Thread B.
    Assim, o saldo total calculado pela Thread A será inconsistente, porque o saldo que ela somou para 
    a conta 1 pode não refletir o valor final após as operações concorrentes.
*/

// TPC: estratégia que funcione, mas tentar ter o lock adquirido o menor tempo possível. 
// Não conseguiremos ter isto para ter locks adquidridos para menos tempo?
// no transfer, libertar o 1º (levantamento) quando não estou a trabalhar nele?
// ponto de partida para pensar no próximo guião
