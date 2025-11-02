package nl.pancompany.spaceinvaders.events;

import lombok.Data;
import nl.pancompany.spaceinvaders.shared.Direction;

@Data
public class SpriteCreated {

    private final String imagePath;
    private final int startX;
    private final int startY;
    private final int speed;
    private final Direction direction;
}
