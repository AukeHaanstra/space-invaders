package nl.pancompany.spaceinvaders.events;

import lombok.Data;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

@Data
public class SpriteCreated {

    private final SpriteId id;
    private final String imagePath;
    private final int startX;
    private final int startY;
    private final int speed;
    private final Direction direction;
}
