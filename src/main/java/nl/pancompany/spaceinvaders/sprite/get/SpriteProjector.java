package nl.pancompany.spaceinvaders.sprite.get;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.ResetHandler;
import nl.pancompany.spaceinvaders.events.*;

import static java.lang.String.format;
import static nl.pancompany.spaceinvaders.shared.Direction.NONE;

@RequiredArgsConstructor
public class SpriteProjector {

    private final SpriteRepository spriteRepository;

    @ResetHandler
    void reset() {
        spriteRepository.deleteAll();
    }

    @EventHandler(enableReplay = true)
    void create(SpriteCreated spriteCreated) {
        if (spriteRepository.findById(spriteCreated.id()).isPresent()) {
            throw new IllegalStateException(format("Sprite with id %s already exists.", spriteCreated.id()));
        }
        SpriteReadModel sprite = new SpriteReadModel(spriteCreated.id(), spriteCreated.entityName(), true, spriteCreated.imagePath(), false,
                spriteCreated.startX(), spriteCreated.startY(), spriteCreated.speed(), spriteCreated.direction());
        spriteRepository.save(sprite);
    }

    @EventHandler(enableReplay = true)
    void update(SpriteTurned spriteTurned) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteTurned.id());
        spriteRepository.save(sprite.withDirection(spriteTurned.direction()));
    }

    @EventHandler(enableReplay = true)
    void update(SpriteStopped spriteStopped) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteStopped.id());
        spriteRepository.save(sprite.withDirection(NONE));
    }

    @EventHandler(enableReplay = true)
    void update(SpriteMoved spriteMoved) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteMoved.id());
        spriteRepository.save(sprite.withX(spriteMoved.newX()).withY(spriteMoved.newY()));
    }

    @EventHandler(enableReplay = true)
    void update(SpriteImageChanged spriteImageChanged) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteImageChanged.id());
        spriteRepository.save(sprite.withImage(spriteImageChanged.imagePath()));
    }

    @EventHandler(enableReplay = true)
    void update(SpriteExplosionTriggered spriteExplosionTriggered) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteExplosionTriggered.id());
        spriteRepository.save(sprite.withExplosionTriggered(true));
    }

    @EventHandler(enableReplay = true)
    void update(SpriteRestsInPeace spriteRestsInPeace) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteRestsInPeace.id());
        spriteRepository.save(sprite.withVisible(false));
    }
}
