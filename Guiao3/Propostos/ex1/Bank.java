import java.util.*;
import java.util.concurrent.locks.*;


class Bank {

    private static class Account {
        private int balance;
        Account(int balance) { this.balance = balance; }

        Lock l = new ReentrantLock(); // ao nível de conta
        
        int balance() { return balance; }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    Lock l = new ReentrantLock(); // outro ao nível de banco

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance); // objetos acabados de criar, não preciam de lock, caso aqui se iniciasse a vazio e depois fizesse um depósito antes de adicionar às outras contas sim, não há estado partilhado, cuidado com vars static
        l.lock(); // bloqueio global, preciso do map com todas as contas
        try {
        // Accout c = new Account(0);
        // depois deposit...aqui não precisava de lock e unlock
        int id = nextId;
        nextId += 1;
        map.put(id, c);
        return id;
        } finally {
            l.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        l.lock(); // bloqueio global, preciso do map com todas as contas para obter a que quero
        try {
            c = map.remove(id);
            if (c == null) return 0;
            c.l.lock(); // bloqueio a conta que quero
        } finally {
            l.unlock(); // já não preciso do map todo
        }        

        try {
            return c.balance(); // relembrar que libertar o lock no fim de tudo, não tem concorrência nenhuma
        } finally {
            c.l.unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) { // igual a closeAccount
        Account c;
        l.lock();
        try {
            c = map.get(id);
            if (c == null) return 0;
            c.l.lock();
        } finally {
            l.unlock();
        }

        try {
            return c.balance();
        } finally {
            c.l.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c; // igual a closeAccount
        l.lock();
        try {
            c = map.get(id);
            if (c == null) return false;
            c.l.lock();
        } finally {
            l.unlock();
        }

        try {
            return c.deposit(value);
        } finally {
            c.l.unlock();
        }

    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) { // igual a closeAccount, -em falta- por os locks, mas é sempre igual
        Account c = map.get(id);
        if (c == null)
            return false;
        return c.withdraw(value);
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        l.lock();
        try {
            cfrom = map.get(from); // só há uma thread de cada vez a adquirir o lock das contas, porque não era possível adquirir deadlock, ou seja, não era precisa ordem de aquisição
            cto = map.get(to);
            if (cfrom == null || cto ==  null) return false;
            if(from < to) { // e se tirar o if, then, else
                cfrom.l.lock();
                cto.l.lock();
            } else {
                cto.l.lock();
                cfrom.l.lock();
            } 
        } finally {
            l.unlock();
        }
        try {
            try {
                if(!cfrom.withdraw(value)) return false;
            } finally {
                cfrom.l.unlock();
            }
            return cto.deposit(value);
        } finally {
            cto.l.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        Account[] ac = new Account[ids.length];
        l.lock();
        try {
            for(int i = 0; i < ids.length; i++) {
                ac[i] = map.get(ids[i]);
                if (ac[i] == null) {
                    return 0;
                }
            }
            for(Account c : ac) {
                c.l.lock();
            }

        } finally {
            l.unlock();
        }

        int total = 0;
        for (Account c : ac) {
            total += c.balance();
            c.l.unlock();
        } 
        return total;
    }

}

/* TPC: neste banco em particular, será que precisava de me preocupar com a ordem de aquisição?? 
   TPC: ponto 3, usar locks que distinguem escritas de leituras. 
   Passar o lock do banco a ser um ReentrantReadWriteLock lock e todas as operações que consultem 
   o banco read lock e as que fazem update (create) usam o write lock.
   A maioria então usa read, que são as que usam gets */

