package nl.pancompany.spaceinvaders.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlayerTurned extends SpriteTurned {

    public PlayerTurned(TurnDirection turnDirection) {
        super(turnDirection);
    }
}
