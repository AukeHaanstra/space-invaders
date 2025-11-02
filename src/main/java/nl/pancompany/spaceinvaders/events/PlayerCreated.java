package nl.pancompany.spaceinvaders.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlayerCreated extends SpriteCreated {

    public PlayerCreated(String imagePath, int startX, int startY, int speed) {
        super(imagePath, startX, startY, speed);
    }
}
