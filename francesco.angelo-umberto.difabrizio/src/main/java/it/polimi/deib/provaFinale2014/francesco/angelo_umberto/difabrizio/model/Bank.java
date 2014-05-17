/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model;

/**
 *
 * @author Francesco
 */
public class Bank {
    private Card[] unusedCards;
    private Fence[] unusedFences;

    public Bank(int numCards, int numeFences) {
        this.unusedCards = new Card[numCards];
        this.unusedFences = new Fence[numeFences];
    }

    public Card[] getUnusedCards() {
        return unusedCards;
    }
    /**
     * Cerca una carta del tipo specificato nell'array delle carte disponibili
     * e la ritorna se esiste, altrimenti solleva un eccezione se  le carte sono
     * finite
     * @param type Tipo di carta voluto
     * @return una Card del tipo chiesto
     * @throws Exception Se la carta non c'è
     */
    public Card getCard(RegionType type) throws Exception{
    	String missingCardMessage = "Non ci sono più carte per il tipo " + type.toString(); //preparo un messaggio di errore
        //l'algoritmo che segue a come idea di indicizzare l'array
        //le carte vengono caricate nell'array di quelle a disposizione della 
        //banca in maniera ordinata
    	for( int i = type.getIndex() * GameConstants.NUM_CARDS_FOR_REGION_TYPE.getValue();
    			i < (type.getIndex() + 1) * GameConstants.NUM_CARDS_FOR_REGION_TYPE.getValue(); i++){
    		if( this.unusedCards[i] != null ){
    			Card foundedCard = this.unusedCards[i];
    			this.unusedCards[i] = null;
    			return foundedCard;
    		}
    	}
    	throw new Exception(missingCardMessage);
    }
    /**
     * Crea un recinto non finale e lo aggiunge all'array nella posizione 
     * specificata
     * @param position 
    */
    public void loadFence(int position){
        unusedFences[position] = new Fence(false);
    }
    /**
     * Crea un recinto finale e lo aggiunge all'array nella posizione 
     * specificata
     * @param position 
     */
    public void loadFinalFence(int position){
        unusedFences[position] = new Fence(true);
    }
    /**
     * Riceve una card e la posiziona dove richiesto nell'array delle carte
     * non usate(del banco)
     * @param card Carta da caricare
     * @param position Posizione in cui caricarla
     */
    public void loadCard(Card card, int position){
        unusedCards[position] = card;
    }
    public Fence[] getUnusedFences() { //TODO: serve?
        return unusedFences;
    }

}
