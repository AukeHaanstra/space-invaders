package nl.pancompany.spaceinvaders.shared;

import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import static nl.pancompany.spaceinvaders.Constants.ALIEN_SPRITE_IDS;
import static nl.pancompany.spaceinvaders.Constants.BOMB_SPRITE_IDS;

public class IdUtil {

    private IdUtil() {
    }

    public static SpriteId getBombId(SpriteId alienId) {
        return BOMB_SPRITE_IDS.get(ALIEN_SPRITE_IDS.indexOf(alienId));
    }

    public static boolean isAlien(SpriteId spriteId) {
        return ALIEN_SPRITE_IDS.contains(spriteId);
    }


}
