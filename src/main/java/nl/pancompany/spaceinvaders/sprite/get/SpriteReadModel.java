package nl.pancompany.spaceinvaders.sprite.get;

import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.awt.*;

public record SpriteReadModel(SpriteId spriteId, boolean visible, Image image, boolean dying, int x, int y, int speed,
                              Direction direction) {

    public SpriteReadModel withVisible(boolean visible) {
        return new SpriteReadModel(spriteId, visible, image, dying, x, y, speed, direction);
    }

    public SpriteReadModel withImage(Image image) {
        return new SpriteReadModel(spriteId, visible, image, dying, x, y, speed, direction);
    }

    public SpriteReadModel withDying(boolean dying) {
        return new SpriteReadModel(spriteId, visible, image, dying, x, y, speed, direction);
    }

    public SpriteReadModel withX(int x) {
        return new SpriteReadModel(spriteId, visible, image, dying, x, y, speed, direction);
    }

    public SpriteReadModel withY(int y) {
        return new SpriteReadModel(spriteId, visible, image, dying, x, y, speed, direction);
    }

    public SpriteReadModel withDirection(Direction direction) {
        return new SpriteReadModel(spriteId, visible, image, dying, x, y, speed, direction);
    }

}
