package nl.pancompany.spaceinvaders.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlayerMoved extends SpriteMoved {
    public PlayerMoved(int newX, int newY) {
        super(newX, newY);
    }
}
