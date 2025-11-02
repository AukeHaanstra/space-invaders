package nl.pancompany.spaceinvaders.sprite.get;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteMoved;
import nl.pancompany.spaceinvaders.events.SpriteStopped;
import nl.pancompany.spaceinvaders.events.SpriteTurned;

import static java.lang.String.format;
import static nl.pancompany.spaceinvaders.shared.Direction.NONE;

@RequiredArgsConstructor
public class SpriteProjector {

    private final SpriteRepository spriteRepository;

    @EventHandler
    void handle(SpriteCreated spriteCreated) {
        if (spriteRepository.findById(spriteCreated.getId()).isPresent()) {
            throw new IllegalStateException(format("Sprite with id %s not found.", spriteCreated.getId()));
        }
        SpriteReadModel sprite = new SpriteReadModel(spriteCreated.getId(), false, null, false,
                spriteCreated.getStartX(), spriteCreated.getStartY(), spriteCreated.getSpeed(), spriteCreated.getDirection());
        spriteRepository.save(sprite);
    }

    @EventHandler
    void handle(SpriteTurned spriteTurned) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteTurned.getId());
        spriteRepository.save(sprite.withDirection(spriteTurned.getDirection()));
    }

    @EventHandler
    void handle(SpriteStopped spriteStopped) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteStopped.getId());
        spriteRepository.save(sprite.withDirection(NONE));
    }

    @EventHandler
    void handle(SpriteMoved spriteMoved) {
        SpriteReadModel sprite = spriteRepository.findByIdOrThrow(spriteMoved.getId());
        spriteRepository.save(sprite.withX(spriteMoved.getNewX()).withY(spriteMoved.getNewY()));
    }
}
