package nl.pancompany.spaceinvaders.events;

import lombok.Data;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

@Data
public class SpriteMoved {

    private final SpriteId id;
    private final int newX;
    private final int newY;
}
