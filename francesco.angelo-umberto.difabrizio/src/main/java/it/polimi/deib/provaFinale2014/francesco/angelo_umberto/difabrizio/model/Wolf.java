package it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.model;

import it.polimi.deib.provaFinale2014.francesco.angelo_umberto.difabrizio.control.CannotMoveWolfException;

public class Wolf extends SpecialAnimal {

    @Override
    public void setAt(Region region) {
    }

    @Override
    public void moveThrough(Street street, Region endRegion) throws
            CannotMoveWolfException {
        if (!street.hasFence() || super.getMyRegion().isAllFenced()) {//se la strada non è sbarrata o tutta la regione è sbarrata
            this.setAt(endRegion);
        } else {
            //la strada è sbarrata ma altre sono libere
            throw new CannotMoveWolfException(
                    "La strada è sbarrata il lupo non può passare");
        }
    }
;

}
