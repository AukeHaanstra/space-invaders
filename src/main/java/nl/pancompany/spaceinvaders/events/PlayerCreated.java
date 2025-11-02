package nl.pancompany.spaceinvaders.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.pancompany.spaceinvaders.shared.Direction;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlayerCreated extends SpriteCreated {

    public PlayerCreated(String imagePath, int startX, int startY, int speed, Direction direction) {
        super(imagePath, startX, startY, speed, direction);
    }
}
