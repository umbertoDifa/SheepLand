package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.network;

import java.util.Scanner;

/**
 * The class is a manager that collects connections from clients and starts
 * games when there are enough cilents. It starts a thread any time it needs to
 * start a new game but it allows only a maximum of games.
 *
 * @author francesco.angelo-umberto.difabrizio
 */
public class ServerManager {

    /**
     * Main method, creates a serverMangager and starts it
     *
     * @param args
     */
    public static void main(String[] args) {
        //creo un server su una certa porta

        String answer;
        int choice;
        int port = 5050;
        String serverName = "sheepland";

        Scanner stdIn = new Scanner(System.in);
        boolean stringValid = false;

        while (!stringValid) {
            try {
                System.out.println(
                        "Scegli connessione:\n1- Socket\n2- RMI");
                answer = stdIn.nextLine();
                choice = Integer.parseInt(answer);

                if (choice == 1) {
                    stringValid = true;
                    ServerSockets server = new ServerSockets(port);
                    server.start();
                } else if (choice == 2) {
                    stringValid = true;
                    ServerRmiImpl server = new ServerRmiImpl(serverName, "localhost", port);
                    server.start();
                } else {
                    System.out.println("La scelta inserita non è valida\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Scelta non valida\n");

            }
        }
        System.out.println("Server spento.");
    }

}
