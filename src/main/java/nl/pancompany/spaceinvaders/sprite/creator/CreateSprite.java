package nl.pancompany.spaceinvaders.sprite.creator;

import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

public record CreateSprite(SpriteId spriteId, String entityName, String imagePath, int startX, int startY, int speed,
                           Direction direction) {

}
