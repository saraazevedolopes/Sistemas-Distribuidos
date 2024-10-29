import java.util.concurrent.locks.*;
 // javac BancoMultiplasContas.java 
 // javac BankTest.java 
 // javac BankTest2.java 
 // java BankTest
 // java BankTest2
 // ex1

public class BancoMultiplasContas {

    Lock l = new ReentrantLock();

    private static class Account {
        private int balance;

        Account (int balance)  { 
            this.balance = balance; 
        }

        int balance () { 
            return balance; 
        }

        boolean deposit (int value) {
            balance += value;
            return true;
        }

        boolean withdraw (int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    // Bank slots and vector of accounts
    private final int slots; // usar final sempre que possível
    private Account[] av;
    
    public BancoMultiplasContas (int n) {
        slots=n;
        av=new Account[slots];
        for (int i=0; i<slots; i++)
            av[i]=new Account(0);
    }


    // Account balance
    public int balance (int id) {
        l.lock();
        if (id < 0 || id >= slots) return 0; // podia estar dentro do try, mas é mais eficiente fora
        try {
            return av[id].balance();
        }
        finally {
            l.unlock();
        }
    }

    // Deposit
    public boolean deposit (int id, int value) {
        l.lock();
        if (id < 0 || id >= slots) return false;
        try {
            return av[id].deposit(value);
        }
        finally {
            l.unlock();
        }
        
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw (int id, int value) {
        if (id < 0 || id >= slots) return false;
        l.lock();
        try {
            return av[id].withdraw(value);
        }
        finally {
            l.unlock();
        }
    }

    // a partir daqui é ex2

    // Transfer
    public boolean transfer (int from, int to, int value) { // ver o que é datarace
        if (from < 0 || from >= slots || to < 0 || to >= slots) return false; 
        l.lock(); // BLOQUEIO GLOBAL DO BANCO, qualquer lock neste código causa um bloqueio global
        try {
            return withdraw(from, value) && deposit(to, value); // e se o depósito não funciona e o withdraw sim? (não existe conta, daí ser necessária a linha 81)
        }
        finally {
            l.unlock();
        }
    }

    /*
    No caso da função transfer, um data race ocorreria se duas threads acessassem as mesmas contas ao mesmo tempo,
    sem sincronização. Por exemplo, se uma thread realiza o withdraw de uma conta enquanto outra faz o deposit na
    mesma conta, os saldos podem ser corrompidos ou inconsistentes, pois ambas as threads podem ler e modificar o
    saldo simultaneamente sem controle. Isso pode resultar em uma transferência incompleta ou incorreta. Usar um 
    lock previne esse problema, garantindo que a transferência ocorra de forma atômica.
    */

    // TotalBalance
    public int totalBalance () {
        l.lock();
        try {
            int sum = 0;
            for(int i = 0; i < slots; i++) { // valores espalhados no tempo são Balances para formar o Total Balance
                sum += balance(i); // valores espelhados no tempo, entre os pontos não há nada adquirido, pode aparecer uma thread, totalBalance e Tranfer precisam de lock para resolver
            }    
        return sum;
        
        } finally {
            l.unlock();
        }   
    }
}

/*
Data race?

Um data race ocorre quando duas ou mais threads acessam a mesma variável compartilhada 
simultaneamente, pelo menos uma delas modifica o valor, e não há mecanismos de 
sincronização apropriados para controlar o acesso, resultando em comportamento 
imprevisível e inconsistências.
*/