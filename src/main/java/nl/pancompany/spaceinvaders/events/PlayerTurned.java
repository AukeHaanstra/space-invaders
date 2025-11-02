package nl.pancompany.spaceinvaders.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.pancompany.spaceinvaders.shared.Direction;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlayerTurned extends SpriteTurned {

    public PlayerTurned(Direction direction) {
        super(direction);
    }
}
