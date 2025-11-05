package nl.pancompany.spaceinvaders.laserbeam.create;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteRespawned;
import nl.pancompany.spaceinvaders.shared.EntityTags;

import java.util.Optional;

import static nl.pancompany.spaceinvaders.shared.Constants.*;

@RequiredArgsConstructor
public class CreateLaserBeamCommandHandler {

    private final EventStore eventStore;

    public void decide(CreateLaserBeam createLaserBeam) {
        Tag spriteTag = Tag.of(SPRITE_ENTITY, createLaserBeam.spriteId().toString());
        StateManager<LaserState> stateManager = eventStore.loadState(LaserState.class,
                Query.of(spriteTag, Type.of(SpriteCreated.class)));
        Optional<LaserState> spriteState = stateManager.getState();
        if (spriteState.isPresent()) {
            stateManager.apply(new SpriteRespawned(
                            createLaserBeam.spriteId(),
                            createLaserBeam.x(),
                            createLaserBeam.y()),
                    Tags.and(spriteTag, Tag.of(createLaserBeam.entityName()), EntityTags.GAME));
        } else {
            stateManager.apply(new SpriteCreated(
                            createLaserBeam.spriteId(),
                            createLaserBeam.entityName(),
                            createLaserBeam.imagePath(),
                            createLaserBeam.x(),
                            createLaserBeam.y(),
                            createLaserBeam.speed(),
                            createLaserBeam.direction()),
                    Tags.and(spriteTag, Tag.of(createLaserBeam.entityName()), EntityTags.GAME));
        }
    }

    private static class LaserState {

        String entityName;

        @StateCreator
        LaserState(SpriteCreated spriteCreated) {
            entityName = spriteCreated.entityName();
        }

        @EventSourced
        void evolve(SpriteRespawned spriteRespawned) {
        }

    }

}
