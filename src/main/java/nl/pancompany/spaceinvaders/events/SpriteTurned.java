package nl.pancompany.spaceinvaders.events;

import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

public record SpriteTurned(SpriteId id, Direction direction) {

}
