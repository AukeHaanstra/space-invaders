package nl.pancompany.spaceinvaders.sprite.get;

import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

public record SpriteReadModel(SpriteId spriteId, String entityName, boolean visible, String imagePath, boolean explosionTriggered, int x, int y, int speed,
                              Direction direction) {

    public SpriteReadModel withVisible(boolean visible) {
        return new SpriteReadModel(spriteId, entityName, visible, imagePath, explosionTriggered, x, y, speed, direction);
    }

    public SpriteReadModel withImage(String imagePath) {
        return new SpriteReadModel(spriteId, entityName, visible, imagePath, explosionTriggered, x, y, speed, direction);
    }

    public SpriteReadModel withExplosionTriggered(boolean explosionTriggered) {
        return new SpriteReadModel(spriteId, entityName, visible, imagePath, explosionTriggered, x, y, speed, direction);
    }

    public SpriteReadModel withX(int x) {
        return new SpriteReadModel(spriteId, entityName, visible, imagePath, explosionTriggered, x, y, speed, direction);
    }

    public SpriteReadModel withY(int y) {
        return new SpriteReadModel(spriteId, entityName, visible, imagePath, explosionTriggered, x, y, speed, direction);
    }

    public SpriteReadModel withDirection(Direction direction) {
        return new SpriteReadModel(spriteId, entityName, visible, imagePath, explosionTriggered, x, y, speed, direction);
    }

}
