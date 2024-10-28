import java.util.*;
import java.util.concurrent.locks.*;

// -> egoísta, enquanto seguro o martelo (único) e espero pelas serras, não deixo os demais usar martelo
// cooperativa, ou há todos os produtos que a pessoa quer ou fica à espera

class Warehouse {
    private Map<String, Product> map =  new HashMap<String, Product>();

    Lock l = new ReentrantLock();

    private class Product { // classe aninhada (classe que é declarada dentro de outra)
        int quantity = 0; 
        Condition cond = l.newCondition(); 
        /*
        Por que usar o Lock da classe externa?

        Para haver um bloqueio global quando qualquer produto é acedido, 
        garantindo que todas as operações de fornecimento (supply) e 
        consumo (consume) em qualquer produto no armazém são executadas 
        de forma mutuamente exclusiva.
        */
    }

    private Product get(String item) {
        Product p;
        l.lock();
        try {
            p = map.get(item);
            if (p != null) return p;
        } finally {
            l.unlock();
        }
        p = new Product();
        map.put(item, p); // pode ficar fora do lock, porque mesmo que duas threads insiram simultaneamente, o map sobrescreve a entrada, garantindo consistência.
        return p;
    }

    public void supply(String item, int quantity) {
        l.lock();
        try {
            Product p = get(item);
            p.quantity += quantity;
            p.cond.signalAll();

            /*
            O Que Faz o signalAll()?
            
            signalAll() é necessário para notificar todas as threads que
            estão à espera de que a quantidade de um determinado produto
            foi atualizada. As threads que ficam à espera resultam sempre 
            da função consume.

            Quando signal() Seria Suficiente?

            Quando houver certeza de que apenas uma thread estaria à 
            espera de cada produto em qualquer momento ou se quisesse 
            garantir que apenas uma thread por vez acedesse ao recurso, 
            então signal() seria suficiente. No entanto, na maioria dos 
            casos, para garantir que todas as threads interessadas sejam 
            notificadas corretamente e evitar bloqueios desnecessários, 
            signalAll() é a escolha mais segura.
            */
        } finally {
            l.unlock();
        }
    }

    // Errado se faltar algum produto... (comentário do código fornecido inicialmente)
    public void consume(Set<String> items) throws InterruptedException { // necessário  throws InterruptedException, porque await() da classe Condition lança uma exceção
        l.lock();
        try {        
            for (String s : items) {
                Product p = get(s);
                while(p.quantity <= 0) { // enquanto não houver a quantidade do produto desejado, espera-se
                        p.cond.await();
                }
                get(s).quantity--;
            }
        } finally {
            l.unlock();
        }
    }

}