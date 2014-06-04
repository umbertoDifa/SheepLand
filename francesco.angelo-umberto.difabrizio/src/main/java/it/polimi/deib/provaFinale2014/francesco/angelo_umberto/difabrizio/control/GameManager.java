package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.control;

import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.control.exceptions.FinishedFencesException;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Bank;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Card;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Dice;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.GameConstants;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Map;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Ovine;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.OvineType;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Region;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.RegionType;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Shepherd;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.SpecialAnimal;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.Street;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.exceptions.NodeNotFoundException;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.exceptions.RegionNotFoundException;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model.exceptions.StreetNotFoundException;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.network.ServerManager;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.network.TrasmissionController;
import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.utility.DebugLogger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * E' il controllo della partita. Si occupa di crearne una a seconda del numero
 * dei giocatori.
 *
 * @author francesco.angelo-umberto.difabrizio
 */
public class GameManager implements Runnable {
    
    private final Thread myThread;
    /**
     * The map of a certain game. It holds the charateristics of the region and
     * of the streets as well as the position of the blackSheep and the wolf
     */
    private final Map map;
    private List<Player> players = new ArrayList<Player>();
    private String[] clientNickNames;
    private final int playersNumber;
    /**
     * It's the controller of the trasmission, it has the duty of use the right
     * trasmission between server and client (rmi or socket)
     */
    private final TrasmissionController controller;
    /**
     * rappresenterà il segnalino indicante il primo giocatore del giro
     */
    private int firstPlayer;
    /**
     * It's the player currently playing
     */
    protected int currentPlayer;
    /**
     * It's the number of shepherd that each player has
     */
    protected final int shepherd4player;
    /**
     * It's the bank which stores fences and cards so that the game manager can
     * take them during the game
     */
    private final Bank bank;  //per permettere a player di usarlo

    /**
     * Creates a game manager connecting it to a given list of clientNickNames
     * and with a specified type of connection
     *
     * @param clientNickNames NickNames of the clients connected to the game
     * @param controller      Type of connection between cllient and server
     */
    public GameManager(List<String> clientNickNames,
                       TrasmissionController controller) {
        
        this.controller = controller;
        //salvo il numero di player
        this.playersNumber = clientNickNames.size();

        //salvo i loro nomi in un array
        this.clientNickNames = clientNickNames.toArray(new String[playersNumber]);

        //creo la mappa univoca del gioco
        this.map = new Map();

        //creo la banca
        this.bank = new Bank(GameConstants.NUM_CARDS.getValue(),
                GameConstants.NUM_INITIAL_CARDS.getValue(),
                GameConstants.NUM_FENCES.getValue());

        //setto il pastore principale
        if (this.playersNumber <= ControlConstants.NUM_FEW_PLAYERS.getValue()) {
            this.shepherd4player = ControlConstants.SHEPHERD_FOR_FEW_PLAYERS.getValue();
        } else {
            this.shepherd4player = ControlConstants.STANDARD_SHEPHERD_FOR_PLAYER.getValue();
        }
        try {
            //setto arraylist dei giocatori
            this.setUpPlayers();
        } catch (RemoteException ex) {
            Logger.getLogger(DebugLogger.class.getName()).log(Level.SEVERE,
                    ex.getMessage(), ex);
            //TODO stesso discorso della run()
        }
        
        controller.setNick2PlayerMap(this.clientNickNames, players);
        
        myThread = new Thread(this);
    }

    /**
     * It's the method to start the gameManager as a thread
     */
    public void start() {
        myThread.start();
    }
    
    public void run() {
        try {
            this.startGame();
        } catch (RemoteException ex) {
            Logger.getLogger(DebugLogger.class.getName()).log(Level.SEVERE,
                    ex.getMessage(), ex);
            //TODO chiamare una funzione per avviasare il server
            //oppure lo faccio io ovvero setto il nickName offline
        }
    }
    
    public TrasmissionController getController() {
        return controller;
    }
    
    public Map getMap() {
        return map;
    }
    
    public Bank getBank() {
        return bank;
    }

    /**
     * Riempie l'arraylist dei player assegnando ad ognuno il nickName
     * corrispondente nell'array dei nickName rispettando l'ordine
     */
    private void setUpPlayers() throws RemoteException {
        //per ogni giocatore
        for (int i = 0; i < playersNumber; i++) {
            try {
                //lo aggiungo alla lista dei giocatori
                players.add(new Player(this, clientNickNames[i]));
            } catch (RemoteException ex) {
                Logger.getLogger(DebugLogger.class.getName()).log(Level.SEVERE,
                        ex.getMessage(), ex);
                throw new RemoteException(
                        "Il player:" + clientNickNames[i] + " si è disconnesso");
            }
        }
    }

    /**
     * Metodo principale che viene invocato dal server thread per creare tutti
     * gli oggetti di una partita e avviarla
     */
    private void SetUpGame() throws RemoteException {
        DebugLogger.println("Avvio partita");
        
        controller.broadcastStartGame();
        
        DebugLogger.println("SetUpMap Avviato");
        this.setUpMap();
        
        DebugLogger.println("SetUpAnimals Avviato");
        this.setUpAnimals();
        
        DebugLogger.println("SetUpcards Avviato");
        this.setUpCards();
        
        DebugLogger.println("SetUpFences Avviato");
        this.setUpFences();
        
        DebugLogger.println("SetUpShift Avviato");
        this.setUpShift();
        DebugLogger.println(
                "SetUpShift Terminato: il primo giocatore e'" + this.firstPlayer);
        
        DebugLogger.println("SetUpinitial Avviato");
        this.setUpInitialCards();
        
        DebugLogger.println("broadcastinitial conditions");
        
        for (String client : clientNickNames) {
            this.broadcastInitialConditions(client);
        }
        
        DebugLogger.println("brodcast cards");
        this.brodcastCards();
        
        this.setUpShepherds();
        DebugLogger.println("SetUpshpherds terminato");
        
    }
    
    private void brodcastCards() throws RemoteException {
        for (int i = 0; i < playersNumber; i++) {
            refreshCards(i);
        }
    }
    
    private void refreshCards(int indexOfPlayer) throws RemoteException {
        int numberOfCards = players.get(indexOfPlayer).shepherd[0].getMyCards().size();
        
        for (int j = 0; j < numberOfCards; j++) {
            Card card = players.get(indexOfPlayer).shepherd[0].getMyCards().get(
                    j);
            controller.refreshCard(clientNickNames[indexOfPlayer],
                    card.getType().toString(), card.getValue());
        }
    }
    
    private void setUpInitialCards() {
        
        for (int i = 0; i < clientNickNames.length; i++) {
            //aggiungi la carta prendendola dalle carte iniziali della banca
            Card initialCard = this.bank.getInitialCard();
            
            this.players.get(i).shepherd[0].addCard(
                    initialCard);
        }
        
    }

    /**
     * Per ogni terreno regione della mappa aggiungi un collegamento ad un
     * animale il cui tipo è scelto in maniera randomica e posiziono pecora nera
     * e lupo a sheepsburg
     */
    private void setUpAnimals() {
        int SHEEPSBURG_ID = 18;
        //recupera l'array delle regioni
        Region[] region = this.map.getRegions();

        //per ogni regione tranne shepsburg
        for (int i = 0; i < region.length - 1; i++) {
            //aggiungi un ovino (a caso)          
            region[i].addOvine(new Ovine());
        }

        //posiziono lupo e pecora nera a sheepsburg
        map.getBlackSheep().setAt(map.getRegions()[SHEEPSBURG_ID]);
        map.getWolf().setAt(map.getRegions()[SHEEPSBURG_ID]);
    }

    /**
     * Chiede ad ogni giocatore dove posizionare il proprio pastore
     */
    private void setUpShepherds() throws RemoteException {
        int i;//indice giocatori
        int j;//indice pastori
        boolean outcomeOk;

        //per ogni playerint 
        for (i = 0; i < this.playersNumber; i++) {
            //setto il player corrente
            currentPlayer = (firstPlayer + i) % playersNumber;

            //per ogni suo pastore
            for (j = 0; j < this.shepherd4player; j++) {
                outcomeOk = false;
                while (!outcomeOk) {
                    //prova a chiedere la strada per il j-esimo pastore                    
                    outcomeOk = controller.askSetUpShepherd(
                            clientNickNames[currentPlayer], j);
                }//while               
            }//for pastori
        }//for giocatori
    }
    
    private void setUpShift() {
        //creo oggetto random
        Random random = new Random();
        //imposto il primo giocatore a caso tra quelli presenti
        this.firstPlayer = random.nextInt(this.playersNumber);
    }

    /**
     * chiama l'omonimo metodo della Map
     */
    private void setUpMap() {
        this.map.setUp();
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

    /**
     * Inserisce le carte nella banca, lo stesso numero per ogni regione,
     * nell'ordine in cui sono le enum della RegionType così da poter usare una
     * ricerca indicizzata per trovarle in seguito
     */
    private void setUpCards() {
        //per ogni tipo di regione - sheepsburg 
        int i;
        int j;
        for (i = 0; i < RegionType.values().length - 1; i++) {
            //per tante quante sono le carte di ogni tipo
            for (j = 0; j < GameConstants.NUM_CARDS_FOR_REGION_TYPE.getValue(); j++) {
                //crea una carta col valore giusto( j crescente da 0 al max) e tipo giusto(dipendente da i) 
                Card cardToAdd = new Card(j, RegionType.values()[i]);
                //caricala
                bank.loadCard(cardToAdd);
            }
        }
    }
    
    private void playTheGame() throws RemoteException {
        int[][] classification;
        int numOfWinners = 1;
        
        try {
            DebugLogger.println("Avvio esecuzione giri");
            this.executeRounds();
        } catch (FinishedFencesException ex) {
            
            Logger.getLogger(DebugLogger.class.getName()).log(
                    Level.SEVERE, ex.getMessage(), ex);
        } finally {
            //se il gioco va come deve o se finisco i recinti quando non devono cmq calcolo i punteggi
            //stilo la classifica in ordine decrescente
            classification = this.calculatePoints();
            
            DebugLogger.println("prima while");

            //calcolo quanti sono al primo posto a parimerito
            for (int i = 0; i < classification[1].length - 1; i++)
                if (classification[1][i] == classification[1][i + 1]) {
                    numOfWinners++;
                }
            
            DebugLogger.println("calcolo vincitori eff");
            
            int i;
            //per tutti i vincitori
            for (i = 0; i < numOfWinners; i++) {
                controller.sendRank(true, clientNickNames[classification[0][i]],
                        classification[1][i]);
            }
            //per tutti gli altri
            for (; i < playersNumber; i++) {
                controller.sendRank(false, clientNickNames[classification[0][i]],
                        classification[1][i]);
                
            }
            DebugLogger.println("invio classifica");
            controller.sendClassification(classificationToString(classification));
        }
    }
    
    private void startGame() throws RemoteException {
        DebugLogger.println("SetUpGameAvviato");
        this.SetUpGame();
        
        DebugLogger.println("SetUpGame Effettuato");
        this.playTheGame();

        //gameFinished
    }
    
    private void broadcastInitialConditions(String client) throws
            RemoteException {

        //broadcast regions
        int numbOfSheep, numbOfLamb, numbOfRam;
        
        for (int i = 0; i < this.map.getRegions().length; i++) {
            numbOfLamb = 0;
            numbOfRam = 0;
            numbOfSheep = 0;
            Region region = this.map.getRegions()[i];
            for (Ovine ovine : region.getMyOvines()) {
                if (ovine.getType() == OvineType.SHEEP) {
                    numbOfSheep++;
                } else if (ovine.getType() == OvineType.LAMB) {
                    numbOfLamb++;
                } else if (ovine.getType() == OvineType.RAM) {
                    numbOfRam++;
                }
            }
            //refersh la regione a tutti i client

            controller.refreshRegion(client, i, numbOfSheep,
                    numbOfRam, numbOfLamb);
            
        }

        //broadcast streets
        boolean fence;
        String shepherdName;
        
        int streetsNumber = this.map.getStreets().length;
        for (int i = 0; i < streetsNumber; i++) {
            Street street = this.map.getStreets()[i];
            fence = false;
            shepherdName = null;
            if (street.hasFence()) {
                fence = true;
            } else if (street.hasShepherd()) {
                shepherdName = getPlayerNickNameByShepherd(street.getShepherd());
            }
            
            controller.refreshStreet(client, i, fence, shepherdName);
            
        }

        //broadcast money        
        controller.refreshMoney(client);

        //broadcast money        
    }
    
    private void executeRounds() throws FinishedFencesException, RemoteException {
        currentPlayer = this.firstPlayer;
        boolean lastRound = false;
        
        while (!(lastRound && currentPlayer == this.firstPlayer)) {
            //prova a fare un turno
            DebugLogger.println("Avvio esecuzione turno");

            //se il player è Online
            if (ServerManager.Nick2ClientProxyMap.get(
                    clientNickNames[currentPlayer]).isOnline()) {

                //controllo se il player ha bisogno di un refresh
                if (ServerManager.Nick2ClientProxyMap.get(
                        clientNickNames[currentPlayer]).needRefresh()) {
                    
                    this.broadcastInitialConditions(
                            clientNickNames[currentPlayer]);
                }

                //before starting anyone shift the last action is setted 
                //to none of the possibles
                players.get(currentPlayer).lastAction = ActionConstants.NO_ACTION.getValue();

                //the shepherd used is set to none too
                players.get(currentPlayer).lastShepherd = null;
                
                lastRound = this.executeShift(currentPlayer);
                
                nextPlayer();
                
                evolveLambs();

                //controllo se ho finito il giro
                //se il prossimo a giocare è il primo del giro
                if (currentPlayer == this.firstPlayer) {
                    //1)avvio il market  
                    //FIXME this.startMarket();
                    //2)muovo il lupo
                    DebugLogger.println("muovo lupo");
                    this.moveSpecialAnimal(this.map.getWolf());
                    DebugLogger.println("lupo mosso");
                }

                //refresho le condizioni a tutti
                for (String client : clientNickNames) {
                    this.broadcastInitialConditions(client);
                }
                
            } else {
                DebugLogger.println(
                        "Player offline:" + clientNickNames[currentPlayer]);
                //player offline
                //skip player
                nextPlayer();
            }
            
        }//while
    }
    
    private void nextPlayer() {
        //aggiorno il player che gioca 
        currentPlayer++;
        //conto in modulo playersNumber
        currentPlayer %= this.playersNumber;
    }
    
    private boolean executeShift(int player) throws FinishedFencesException,
                                                    RemoteException {
        DebugLogger.println("Broadcast giocatore di turno");
        
        controller.refreshCurrentPlayer(clientNickNames[player]);
        
        DebugLogger.println("Muovo pecora nera");
        //muovo la pecora nera
        this.moveSpecialAnimal(this.map.getBlackSheep());

        //faccio fare le azioni al giocatore
        for (int i = 0; i < GameConstants.NUM_ACTIONS.getValue(); i++) {
            
            DebugLogger.println(
                    "Avvio choose and make action per il player " + player);
            //scegli l'azione e falla
            this.players.get(player).chooseAndMakeAction();
        }

        //se sono finiti i recinti normali chiamo l'ultimo giro
        return this.bank.numberOfUsedFence() >= GameConstants.NUM_FENCES.getValue() - GameConstants.NUM_FINAL_FENCES.getValue();
    }
    
    private void moveSpecialAnimal(SpecialAnimal animal) throws RemoteException {
        //salvo la regione in cui si trova l'animale
        Region actualAnimalRegion = animal.getMyRegion();
        int startRegionIndex = 0;
        Region endRegion;
        //cerco la strada che dovrebbe attraversare
        Street potentialWalkthroughStreet;
        int streetValue = Dice.roll();
        
        try {
            startRegionIndex = map.getNodeIndex(
                    actualAnimalRegion);
            
            potentialWalkthroughStreet = this.map.getStreetByValue(
                    actualAnimalRegion, streetValue);

            //calcola regione d'arrivo
            endRegion = this.map.getEndRegion(actualAnimalRegion,
                    potentialWalkthroughStreet);

            //cerco di farlo passare (nel caso del lupo si occupa pure di farlo mangiare)
            String result = animal.moveThrough(potentialWalkthroughStreet,
                    endRegion);

            //tutto ok      
            controller.refreshSpecialAnimal(animal,
                    result + "," + streetValue + "," + startRegionIndex + "," + map.getNodeIndex(
                            endRegion));
        } catch (StreetNotFoundException ex) {
            Logger.getLogger(DebugLogger.class.getName()).log(Level.SEVERE,
                    "nok" + ex.getMessage(), ex);
            controller.refreshSpecialAnimal(animal,
                    "nok" + "," + streetValue + "," + startRegionIndex);
        } catch (RegionNotFoundException ex) {
            Logger.getLogger(DebugLogger.class.getName()).log(Level.SEVERE,
                    "nok" + ex.getMessage(), ex);
            controller.refreshSpecialAnimal(animal,
                    "nok" + "," + streetValue + "," + startRegionIndex);
        } catch (NodeNotFoundException ex) {
            //non può accadere
            Logger.getLogger(DebugLogger.class.getName()).log(Level.SEVERE,
                    "nok" + ex.getMessage(), ex);
            controller.refreshSpecialAnimal(animal,
                    "nok" + "," + streetValue + "," + startRegionIndex);
        }
    }
    
    private void startMarket() {
        //iteratore sui player
        int i;
        //per ogni player 
        for (i = 0; i < this.playersNumber; i++) {
            //raccogli cosa vuole vendere
            this.players.get(i).sellCards();
        }
        //lancia il dado per sapere il primo a comprare
        //il modulo serve ad essere sicuro che venga selezionato un player esistente
        int playerThatBuys = Dice.roll() % this.playersNumber;
        //per ogni player 
        for (i = 0; i < this.playersNumber; i++) {
            //chiedi se vuole comprare           
            this.players.get(playerThatBuys).buyCards();
            //aggiorno il prossimo player
            playerThatBuys = (playerThatBuys + 1) % this.playersNumber;
        }
    }

    /**
     * dato un pastore risale al giocatore
     *
     * @param shepherd
     *
     * @return player corrispondente al pastore
     */
    private String getPlayerNickNameByShepherd(Shepherd shepherd) {
        for (Player player : players) {
            for (int i = 0; i < shepherd4player; i++) {
                if (player.shepherd[i] == shepherd) {
                    return player.getPlayerNickName();
                }
            }
        }
        return null;
    }
    
    private int[][] calculatePoints() {
        DebugLogger.println("Calculate points");
        
        int[][] classification = new int[2][playersNumber];
        int tmp1, tmp2;
        //per ogni giocatore
        int i = 0;
        for (Player player : players) {
            //per ogni tipo di regione
            classification[0][i] = i;
            for (RegionType type : RegionType.values()) {
                //aggiungo al suo punteggio num di pecore in quel tipo di regione per num di carte di quel tipo
                classification[1][i] += player.shepherd[0].numOfMyCardsOfType(
                        type) * map.numberOfOvineIn(type);
            }
            //aggiungo i soldi avanzati
            classification[1][i] += player.shepherd[0].getWallet().getAmount();
            
            DebugLogger.println(
                    "player " + clientNickNames[i] + " punti " + classification[1][i]);
            i++;
        }

        //ordino la classifica
        for (int j = 0; j < playersNumber; j++) {
            for (int k = j; k < playersNumber; k++) {
                if (classification[1][j] < classification[1][k]) {
                    tmp1 = classification[0][j];
                    tmp2 = classification[1][j];
                    classification[0][j] = classification[0][k];
                    classification[1][j] = classification[1][k];
                    classification[0][k] = tmp1;
                    classification[1][k] = tmp2;
                }
            }
        }
        
        return classification;
    }
    
    private String classificationToString(int[][] classification) {
        String result = "";
        for (int i = 0; i < playersNumber; i++) {
            result += clientNickNames[classification[0][i]] + "," + classification[1][i] + ",";
        }
        DebugLogger.println("creta classifica stringhizzata " + result);
        return result;
    }
    
    private void evolveLambs() {
        //per tutti gli ovini che sono agnelli
        for (Region region : this.map.getRegions()) {
            for (Ovine ovine : region.getMyOvines()) {
                if (ovine.getType() == OvineType.LAMB) {
                    //se l'età è quella della trasformazione
                    if (ovine.getAge() == GameConstants.LAMB_EVOLUTION_AGE.getValue()) {
                        //trasformalo
                        ovine.setType(OvineType.getRandomLambEvolution());
                    } else {
                        //altrimenti
                        //aumenta l'età
                        ovine.setAge(ovine.getAge() + 1);
                    }
                }
                
            }
        }
    }
}
