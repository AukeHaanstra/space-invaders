package nl.pancompany.spaceinvaders.test.sprite;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.SpriteTurned;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.sprite.turn.TurnSprite;
import nl.pancompany.spaceinvaders.shared.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.shared.Constants.*;
import static nl.pancompany.spaceinvaders.shared.Direction.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class TurnSpriteTest {

    EventStore eventStore;
    CommandApi commandApi;
    EventBus eventBus;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        commandApi = spaceInvaders.getCommandApi();
        eventBus = eventStore.getEventBus();
    }

    @Test
    void given__whenTurnSprite_thenIllegalState() {
        assertThatThrownBy(() -> commandApi.publish(new TurnSprite(PLAYER_SPRITE_ID, RIGHT))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenSpriteCreated_whenTurnSpriteNull_thenIllegalArgument() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_ENTITY, PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, EntityTags.PLAYER));

        assertThatThrownBy(() -> commandApi.publish(new TurnSprite(PLAYER_SPRITE_ID, null))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenSpriteCreated_whenTurnSpriteNone_thenIllegalArgument() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_ENTITY, PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, EntityTags.PLAYER));

        assertThatThrownBy(() -> commandApi.publish(new TurnSprite(PLAYER_SPRITE_ID, NONE))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenSpriteCreated_whenTurnSpriteRight_thenSpriteTurned() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_ENTITY, PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, EntityTags.PLAYER));

        commandApi.publish(new TurnSprite(PLAYER_SPRITE_ID, RIGHT));

        Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteTurned.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(SpriteTurned.class)).isEqualTo(new SpriteTurned(PLAYER_SPRITE_ID, RIGHT));
        assertThat(events.getFirst().tags()).contains(Tag.of(PLAYER_ENTITY));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenSpriteCreated_whenTurnSpriteLeft_thenSpriteTurned() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_ENTITY, PLAYER_IMAGE_PATH, PLAYER_START_X, PLAYER_START_Y, PLAYER_SPEED, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, EntityTags.PLAYER));

        commandApi.publish(new TurnSprite(PLAYER_SPRITE_ID, LEFT));

        Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteTurned.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(SpriteTurned.class)).isEqualTo(new SpriteTurned(PLAYER_SPRITE_ID, LEFT));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }
}
