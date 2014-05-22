package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Classe del client visto dal server, contiene il socket e gli stream per
 * parlarci
 *
 * @author francesco.angelo-umberto.difabrizio
 */
public class Sclient {

    private final Socket socket;
    private Scanner fromClient;
    private PrintWriter toClient;

    public Sclient(Socket socket) {
        this.socket = socket;
        try {
            //inizializzo stream out
            this.toClient = new PrintWriter(this.socket.getOutputStream());

            //inizializzo streamo in
            this.fromClient = new Scanner(this.socket.getInputStream());
        } catch (IOException ex) {
            //se fallisce la creazione di un canale di scambio dati
            System.err.println(ex.getMessage()); 
            //TODO: provalo
        }
    }

    /**
     * Manda un messaggio al client attraverso il proprio socket
     *
     * @param message
     */
    public void send(String message) {
        toClient.println(message);
        //flusha lo stream e controlla eventuali errori
        toClient.checkError();
        //TODO: gestisci un errore se lo stream si sputtana
    }

    /**
     * Riceve un messaggio dal client
     *
     * @return la stringa ricevuta dal client
     */
    public String receive() throws NoSuchElementException, IllegalStateException {
        //TODO:gestisci queste eccez
        String answer = fromClient.nextLine();
        return answer;
    }

}
