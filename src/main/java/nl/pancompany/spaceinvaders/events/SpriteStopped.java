package nl.pancompany.spaceinvaders.events;

import lombok.Data;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

@Data
public class SpriteStopped {

    private final SpriteId id;
}
