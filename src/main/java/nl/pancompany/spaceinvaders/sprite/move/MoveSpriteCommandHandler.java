package nl.pancompany.spaceinvaders.sprite.move;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteMoved;
import nl.pancompany.spaceinvaders.events.SpriteRestsInPeace;
import nl.pancompany.spaceinvaders.events.SpriteTurned;
import nl.pancompany.spaceinvaders.shared.Direction;

import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;

@RequiredArgsConstructor
public class MoveSpriteCommandHandler {

    private final EventStore eventStore;

    public void decide(MoveSprite moveSprite) {
        Tag spriteTag = Tag.of(SPRITE_ENTITY, moveSprite.spriteId().toString());
        StateManager<SpriteState> stateManager = eventStore.loadState(SpriteState.class,
                Query.of(spriteTag, Types.or(SpriteCreated.class, SpriteTurned.class, SpriteMoved.class,
                        SpriteRestsInPeace.class)));
        SpriteState spriteState = stateManager.getState().orElseThrow(() -> new IllegalStateException("Sprite cannot move before being created."));
        // Almost no game rules here, commands are sent from higher level policies (in player and alien mover automations)
        if (spriteState.visible) { // i.e. not R.I.P.
            stateManager.apply(new SpriteMoved(moveSprite.spriteId(), moveSprite.newX(), moveSprite.newY()),
                    Tags.and(spriteTag, Tag.of(spriteState.entityName), EntityTags.GAME));
        }
    }

    private static class SpriteState {

        int x;
        int y;
        Direction direction;
        boolean visible;
        String entityName;

        @StateCreator
        SpriteState(SpriteCreated spriteCreated) {
            x = spriteCreated.startX();
            y = spriteCreated.startY();
            direction = spriteCreated.direction();
            visible = true;
            entityName = spriteCreated.entityName();
        }

        @EventSourced
        void evolve(SpriteTurned spriteTurned) {
            direction = spriteTurned.direction();
        }

        @EventSourced
        void evolve(SpriteMoved spriteMoved) {
            x = spriteMoved.newX();
            y = spriteMoved.newY();
        }

        @EventSourced
        void evolve(SpriteRestsInPeace spriteRestsInPeace) {
            visible = false;
        }
    }
}
