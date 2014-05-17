package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.control;

import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Bank;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Card;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.GameConstants;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Map;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Ovine;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Region;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.RegionType;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Street;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.network.ServerThread;
import java.util.ArrayList;

/**
 * E' il controllo della partita. Si occupa di crearne una a seconda del numero
 * dei giocatori.
 *
 * @author francesco.angelo-umberto.difabrizio
 */
public class GameManager {//TODO: pattern memento per ripristini?

    private final ServerThread server;

    private final Map map;
    private ArrayList<Player> players = new ArrayList<Player>();
    private final int playersNumber;
    private final int[] playersHashCode; //valore cached degli hash dei giocatori
    private final Bank bank;

    public GameManager(int playersNumber, ServerThread server) {
        this.playersNumber = playersNumber;
        this.playersHashCode = new int[playersNumber]; //creo un array della dimensione del numero dei player
        this.map = new Map(); //creo la mappa univoca del gioco
        this.server = server; //creo il collegamento all'univoco serverThread
        this.setUpPlayers(playersNumber); //setto arraylist giocatori e array hashcode giocatori
        this.bank = new Bank(GameConstants.NUM_CARDS.getValue(),
                GameConstants.NUM_FENCES.getValue());
    }

    /**
     * dato il numero di player, riempie l'arraylist dei player e riempie
     * l'array dei rispettivi hashcode
     *
     * @param numbPlayer
     */
    private void setUpPlayers(int numbPlayer) {
        for (int i = 0; i < playersNumber; i++) { //per ogni giocatore
            players.add(new Player());   //TODO:occhio al costruttore id player       //lo aggiungo alla lista dei giocatori
            playersHashCode[i] = players.get(i).hashCode();//salvo il suo hashcode
        }
    }

    /**
     * metodo principale che viene invocato dal server thread per avviare la
     * partita
     */
    public void SetUpGame() {
        this.setUpMap();
        this.setUpSocketPlayerMap();
        this.setUpAnimals();
        this.setUpShepherds();
        this.setUpFences();
        this.setUpShift();
    }

    /**
     * Per ogni terreno regione della mappa aggiungi un collegamento ad un
     * animale il cui tipo è scelto in maniera randomica
     */
    private void setUpAnimals() {
        Region[] region = this.map.getRegions();//recupera l'array delle regioni
        for (Region reg : region) { //per ogni regione
            reg.addOvine(new Ovine());//aggiungi un ovino (a caso)
        }
    }

    /**
     * Chiede ad ogni giocatore dove posizionare il proprio pastore
     */
    private void setUpShepherds() {
        String stringedStreet;
        String errorString = "Strada già occupata, prego riprovare:";
        Street chosenStreet;
        for (int i = 0; i < this.playersNumber; i++) {
            //ciclo che chiede la strada per un pastore ogni volta che questa risulta già occupata
            //TODO: fare un metodo della roba while?
            while (true) {
                try {
                    stringedStreet = this.server.talkTo(this.playersHashCode[i],
                            "In quale strada vuoi posizionare il pastore?"); //raccogli decisione
                    chosenStreet = convertStringToStreet(stringedStreet); //traducila in oggetto steet
                    if (!isStreetFree(chosenStreet)) { //se la strada è occuapata
                        throw new Exception(errorString); //solleva eccezione
                    }
                    break;                          //altrimenti brekka il while
                } catch (Exception e) {
                    this.server.sendTo(this.playersHashCode[i], e.getMessage());//manda il messaggio di errore al client
                }
            }
            this.players.get(i).getShepherd().moveTo(chosenStreet); //sposta il pastore 
            //creo una carta con valore 0 e di tipo casuale e l'aggiungo a 
            //quelle del pastore corrispondente al mio player
            this.players.get(i).getShepherd().addCard(new Card(0, RegionType.
                    getRandomRegionType())); //aggiungi la carta
            //invia conferma riepilogativa
            this.server.sendTo(this.playersHashCode[i],
                    "Pastore posizionato. Hai una carta terreno di tipo: " + RegionType.MOUNTAIN.
                    toString());
        }

    }

    private void setUpShift() {
        //TODO
    }

    /**
     * chiama l'omonimo metodo della Map
     */
    private void setUpMap() {
        this.map.setUp();
    }

    /**
     * chiama l'omonimo metodo del serverThread
     */
    private void setUpSocketPlayerMap() {
        this.server.setUpSocketPlayerMap(playersHashCode);
    }

    /**
     * Carica i recinti finali e non nel banco
     */
    private void setUpFences() {
        int i; // counter
        //salvo il numero di recinti non finali
        int numbOfNonFinalFences = GameConstants.NUM_FENCES.getValue() - GameConstants.NUM_FINAL_FENCES.
                getValue();
        //carico il numero di recinti non finali in bank
        for (i = 0; i < numbOfNonFinalFences; i++) {
            bank.loadFence(i);
        }
        //carico i recinti finali in bank. 'i' parte da dove l'ha lasciato il 
        //ciclo sopra è arriva alla fine dell'array
        for (; i < GameConstants.NUM_FENCES.getValue(); i++) {
            bank.loadFinalFence(i);
        }
    }

    private void setUpCards() {
        for (int i = 0; i < RegionType.values().length - 1; i++)            //per ogni tipo di regione - sheepsburg  
            for (int j = 0; j < GameConstants.NUM_CARDS_FOR_REGION_TYPE.getValue(); j++) { //per tante quante sono le carte di ogni tipo
                //crea una carta col valore giusto( j crescente da 0 al max) e tipo giusto(dipendente da i) 
                Card cardToAdd = new Card(j, RegionType.values()[i]);
                //calcola la posizione a cui aggiungerla(fondamentale per la getCard che
                //usa una politica basata sugli indici)
                int position = (i * GameConstants.NUM_CARDS_FOR_REGION_TYPE.getValue()) + j;
                //caricala
                bank.loadCard(cardToAdd, position);
            }

    }

    /**
     * Controlla se street è libera
     *
     * @param street
     *
     * @return true se libera, false altrimenti
     */
    private boolean isStreetFree(Street street) {
        return street.isFree();
    }

    /**
     * converte una stringa street che indica l'id della strada sulla mappa
     * nell'oggetto Street corrispondente
     *
     * @param street
     *
     * @return una Street
     */
    private Street convertStringToStreet(String street) {
        return map.getStreets()[Integer.parseInt(street)];
    }

}