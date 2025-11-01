package nl.pancompany.spaceinvaders.player.turn;

import nl.pancompany.spaceinvaders.events.SpriteTurned.TurnDirection;
import nl.pancompany.spaceinvaders.shared.TurnSprite;

public class TurnPlayer extends TurnSprite {


    public TurnPlayer(TurnDirection turnDirection) {
        super(turnDirection);
    }
}
