package nl.pancompany.spaceinvaders.events;

import lombok.Data;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

@Data
public class SpriteTurned {

    private final SpriteId id;
    private final Direction direction;
}
