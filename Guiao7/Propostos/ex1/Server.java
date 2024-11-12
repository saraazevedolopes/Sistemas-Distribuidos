import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static java.util.Arrays.asList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;

class ContactManager {
    private HashMap<String, Contact> contacts = new HashMap<>();
    private final Lock l = new ReentrantLock();

    public void update(Contact c) {
        this.l.lock();
        try {
            contacts.put(c.name(), c);
        } finally {
            this.l.unlock();
        }
    }

    public ContactList getContacts() { 
        this.l.lock();
        try {
            ContactList contactList = new ContactList();
            contactList.addAll(contacts.values()); 
            return contactList;
        } finally {
            this.l.unlock();
        }
    }
}

class ServerWorker implements Runnable {
    private Socket socket;
    private ContactManager manager;

    public ServerWorker(Socket socket, ContactManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    @Override
    public void run() { 
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()))) {
                Boolean open = true;

                while(true) {
                    Contact contact = Contact.deserialize(in);
                    
                    if(contact == null) {
                        open = false;
                    } else {
                    contacts.update(contact);
                    }
                }
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
               
            }
     }
}

public class Server {

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        ContactManager manager = new ContactManager();
        // example pre-population
        manager.update(new Contact("John", 20, 253123321, null, asList("john@mail.com")));
        manager.update(new Contact("Alice", 30, 253987654, "CompanyInc.", asList("alice.personal@mail.com", "alice.business@mail.com")));
        manager.update(new Contact("Bob", 40, 253123456, "Comp.Ld", asList("bob@mail.com", "bob.work@mail.com")));

        while (true) {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new ServerWorker(socket, manager));
            worker.start();
        }
    }

}
