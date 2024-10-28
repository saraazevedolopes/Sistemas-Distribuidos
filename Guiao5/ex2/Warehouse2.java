import java.util.*;
import java.util.concurrent.locks.*;

// egoísta, enquanto seguro o martelo (único) e espero pelas serras, não deixo os demais usar martelo
// -> cooperativa, ou há todos os produtos que a pessoa quer ou fica à espera

class Warehouse2 {
    private Map<String, Product> map =  new HashMap<String, Product>();

    Lock l = new ReentrantLock();

    private class Product { 
        int quantity = 0; 
        Condition cond = l.newCondition(); 
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
        map.put(item, p);
        return p;
    }

    public void supply(String item, int quantity) {
        l.lock();
        try {
            Product p = get(item);
            p.quantity += quantity;
            p.cond.signalAll();
        } finally {
            l.unlock();
        }
    }

    // verifica e retorna o primeiro produto do array que tem quantidade insuficiente (<= 0) ou null se todos os produtos estiverem disponíveis
    private Product missing(Product[] a) {
        for(Product p : a) {
            if(p.quantity <= 0) {
                return p;
            }
        }
        return null;
    }
    
    // Errado se faltar algum produto...(comentário do código fornecido inicialmente)
    public void consume(Set<String> items) throws InterruptedException {
        l.lock();
        try {   
            /* array a é usado para armazenar as referências aos produtos que a thread 
            quer consumir para garantir que todos os produtos estão disponíveis antes 
            de proceder com o consumo real 
            
            Sem o array, teríamos que procurar cada produto no mapa várias vezes (para 
            verificar a quantidade e depois para consumir)
            */
            Product[] a = new Product[items.size()]; 
            int i = 0;                                  
            for (String s : items) {        
                a[i++] = get(s);
            }

            for(Product p : a) {
                while(p.quantity <= 0) { 
                        p.cond.await();
                }
            }    

            // Product missingProduct; alternativa com missing
            // while ((missingProduct = missing(a)) != null) {
            //     missingProduct.cond.await();
            // }

            for(Product p : a) {
                p.quantity--;
            }

        } finally {
            l.unlock();
        }
    }

}