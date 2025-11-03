package nl.pancompany.spaceinvaders;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import nl.pancompany.spaceinvaders.game.get.GameQueryHandler;
import nl.pancompany.spaceinvaders.game.get.GameReadModel;
import nl.pancompany.spaceinvaders.game.get.GetGame;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteById;
import nl.pancompany.spaceinvaders.sprite.get.SpriteQueryHandler;
import nl.pancompany.spaceinvaders.sprite.get.SpriteReadModel;

import java.util.Optional;

@Slf4j
@Builder
public class QueryApi {

    private final SpriteQueryHandler spriteQueryHandler;
    private final GameQueryHandler gameQueryHandler;

    public Optional<SpriteReadModel> query(GetSpriteById getSpriteById) {
        return spriteQueryHandler.get(getSpriteById);
    }

    public Optional<GameReadModel> query(GetGame getGame) {
        return gameQueryHandler.get();
    }
}
