package nl.pancompany.spaceinvaders.sprite.creator;

import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

public record RegisterSpriteCreated(SpriteId spriteId, String imagePath, int startX, int startY, int speed, Direction direction) {

}
