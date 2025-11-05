package nl.pancompany.spaceinvaders;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.spaceinvaders.alien.getcount.AlienDownCountQueryHandler;
import nl.pancompany.spaceinvaders.alien.getcount.GetAlienDownCount;
import nl.pancompany.spaceinvaders.game.get.GameQueryHandler;
import nl.pancompany.spaceinvaders.game.get.GameReadModel;
import nl.pancompany.spaceinvaders.game.get.GetGame;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteByEntityName;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteById;
import nl.pancompany.spaceinvaders.sprite.get.SpriteQueryHandler;
import nl.pancompany.spaceinvaders.sprite.get.SpriteReadModel;

import java.util.List;
import java.util.Optional;

import static nl.pancompany.spaceinvaders.shared.Constants.REPLAY_MESSAGE;

@Slf4j
@Builder
public class QueryApi {

    private final SpriteQueryHandler spriteQueryHandler;
    private final GameQueryHandler gameQueryHandler;
    private final AlienDownCountQueryHandler alienDownCountQueryHandler;
    private final EventStore eventStore;

    public Optional<SpriteReadModel> query(GetSpriteById getSpriteById) {
        return spriteQueryHandler.get(getSpriteById);
    }

    public List<SpriteReadModel> query(GetSpriteByEntityName getSpriteByEntityName) {
        return spriteQueryHandler.get(getSpriteByEntityName);
    }

    public Optional<GameReadModel> query(GetGame getGame) {
        return gameQueryHandler.get(getGame);
    }

    public int query(GetAlienDownCount getAlienDownCount) {
        return alienDownCountQueryHandler.get(getAlienDownCount);
    }

    public int getLastEventPosition() {
        return eventStore.getLastSequencePosition()
                .orElseThrow(() -> new IllegalStateException("Game not yet started, nothing to replay."))
                .value();
    }

    public boolean isReplay() {
        Optional<GameReadModel> gameReadModel = gameQueryHandler.get(new GetGame());
        if (gameReadModel.isPresent() && !gameReadModel.get().inGame() && REPLAY_MESSAGE.equals(gameReadModel.get().message().get())) {

            return true;
        }
        return false;
    }
}
