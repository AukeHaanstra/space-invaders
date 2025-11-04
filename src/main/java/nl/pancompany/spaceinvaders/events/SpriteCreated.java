package nl.pancompany.spaceinvaders.events;

import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

public record SpriteCreated(SpriteId id, String entityName, String imagePath, int startX, int startY, int speed, Direction direction) {

}
