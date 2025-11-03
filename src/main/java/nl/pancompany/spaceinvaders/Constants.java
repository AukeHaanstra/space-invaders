package nl.pancompany.spaceinvaders;

import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.List;
import java.util.stream.IntStream;

public interface Constants {

    // Ids
    String GAME_ID = "1";
    SpriteId PLAYER_SPRITE_ID = SpriteId.of("1");
    SpriteId SHOT_SPRITE_ID = SpriteId.of("2");
    List<SpriteId> ALIEN_SPRITE_IDS = IntStream.range(3, 27) // 24 aliens
            .mapToObj(String::valueOf)
            .map(SpriteId::of)
            .toList();
    List<SpriteId> BOMB_SPRITE_IDS = IntStream.range(27, 51) // 24 bombs, one for each alien
            .mapToObj(String::valueOf)
            .map(SpriteId::of)
            .toList();
    default SpriteId getBombId(SpriteId alienId) {
        return BOMB_SPRITE_IDS.get(ALIEN_SPRITE_IDS.indexOf(alienId));
    }

    // Entity names
    String SPRITE_ENTITY = "Sprite";
    String GAME_ENTITY = "Game";
    String PLAYER_ENTITY = "Player";
    String ALIEN_ENTITY = "Alien";
    String SHOT_ENTITY = "Shot";

    // Board
    int BOARD_WIDTH = 358;
    int BOARD_HEIGHT = 350;

    // Alien
    int ALIEN_BORDER_RIGHT = 17; // 12 px alien + 5 px border
    int ALIEN_BORDER_LEFT = 5;
    int ALIEN_HEIGHT = 12;
    int ALIEN_WIDTH = 12;
    int ALIEN_START_X = 150;
    int ALIEN_START_Y = 5;
    int ALIEN_SPEED = 1;
    String ALIEN_IMAGE_PATH = "/images/alien.png";

    // Bomb
    String BOMB_IMAGE_PATH = "/images/bomb.png";
    int BOMB_SPEED = 1;

    // Player
    int PLAYER_BORDER = 5;
    int PLAYER_WIDTH = 15;
    int PLAYER_HEIGHT = 10;
    int PLAYER_START_X = 270;
    int PLAYER_START_Y = 280;
    int PLAYER_SPEED = 2;
    String PLAYER_IMAGE_PATH = "/images/player.png";
    // Player derived constants
    int PLAYER_STOP_X_LEFT = PLAYER_BORDER;
    int PLAYER_STOP_X_RIGHT = BOARD_WIDTH - (PLAYER_WIDTH + PLAYER_BORDER);


    // Miscellaneous
    int GROUND = 290;
    int BOMB_HEIGHT = 5;

    int GO_DOWN = 15;
    int NUMBER_OF_ALIENS_TO_DESTROY = 24;
    // TODO: Easily win game (no bombs)
//    int CHANCE = 15;
    int CHANCE = 5;
    int DELAY = 17;// 60 Hz refresh rate -> 17 ms between each gamecycle

}
