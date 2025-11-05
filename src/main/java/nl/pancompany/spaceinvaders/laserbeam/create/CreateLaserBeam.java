package nl.pancompany.spaceinvaders.laserbeam.create;

import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

public record CreateLaserBeam(SpriteId spriteId, String entityName, String imagePath, int x, int y, int speed,
                              Direction direction) {

}
