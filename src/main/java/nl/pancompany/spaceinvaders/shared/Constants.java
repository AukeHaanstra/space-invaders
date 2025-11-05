package nl.pancompany.spaceinvaders.shared;

import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.List;
import java.util.stream.IntStream;

public interface Constants {

    // Set this high to allow more processing time for events, at the cost of a lower framerate
    // MULTIPLIER = 1 was the original setting.
    // MULTIPLIER = 7 (119 ms = 8Hz) works fine with eventsourcing on a fast machine.
    int MULTIPLIER = 7;
    String REPLAY_MESSAGE = "Replay...";

    // Ids
    String GAME_ID = "1";
    SpriteId PLAYER_SPRITE_ID = SpriteId.of("1");
    SpriteId LASER_SPRITE_ID = SpriteId.of("2");
    List<SpriteId> ALIEN_SPRITE_IDS = IntStream.range(3, 27) // 24 aliens
            .mapToObj(String::valueOf)
            .map(SpriteId::of)
            .toList();
    List<SpriteId> BOMB_SPRITE_IDS = IntStream.range(27, 51) // 24 bombs, one for each alien
            .mapToObj(String::valueOf)
            .map(SpriteId::of)
            .toList();

    // Entity names
    String SPRITE_ENTITY = "Sprite";
    String GAME_ENTITY = "Game";
    String PLAYER_ENTITY = "Player";
    String ALIEN_ENTITY = "Alien";
    String BOMB_ENTITY = "Bomb";
    String LASER_ENTITY = "Laser";

    // Board
    int BOARD_WIDTH = 358;
    int BOARD_HEIGHT = 350;
    int GROUND_Y = 290;

    // Alien
    int ALIEN_BORDER_RIGHT = 17; // 12 px alien + 5 px border
    int ALIEN_BORDER_LEFT = 5;
    int ALIEN_HEIGHT = 12;
    int ALIEN_WIDTH = 12;
    int ALIEN_START_X = 150;
    int ALIEN_START_Y = 5;
    int ALIEN_SPEED = 1 * MULTIPLIER;
    String ALIEN_IMAGE_PATH = "/images/alien.png";
    int ALIEN_STEP_DOWN = 15;
    String ALIEN_EXPLOSION_IMAGE_PATH = "/images/explosion.png";

    // Bomb
    String BOMB_IMAGE_PATH = "/images/bomb.png";
    String BOMB_EXPLOSION_IMAGE_PATH = "/images/explosion.png";
    int BOMB_SPEED = 1 * MULTIPLIER;
    int BOMB_HEIGHT = 5;
    // TODO: Easily win game (no bombs)
//    int CHANCE = 15;
    int CHANCE = 5;

    // Player
    int PLAYER_BORDER = 5;
    int PLAYER_WIDTH = 15;
    int PLAYER_HEIGHT = 10;
    int PLAYER_START_X = 270;
    int PLAYER_START_Y = 280;
    int PLAYER_SPEED = 2 * MULTIPLIER;
    String PLAYER_IMAGE_PATH = "/images/player.png";
    // Player derived constants
    int PLAYER_STOP_X_LEFT = PLAYER_BORDER;
    int PLAYER_STOP_X_RIGHT = BOARD_WIDTH - (PLAYER_WIDTH + PLAYER_BORDER);

    // Laser
    String LASER_IMAGE_PATH = "/images/laser.png";
    int LASER_SPEED = 4 * MULTIPLIER;

    // Game
    int NUMBER_OF_ALIENS_TO_DESTROY = 24;
    int DELAY = 17 * MULTIPLIER;// 60 Hz refresh rate -> 17 ms between each gamecycle

}
