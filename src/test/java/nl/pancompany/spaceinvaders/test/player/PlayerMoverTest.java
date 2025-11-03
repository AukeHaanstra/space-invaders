package nl.pancompany.spaceinvaders.test.player;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.Constants.*;
import static nl.pancompany.spaceinvaders.shared.Direction.LEFT;
import static nl.pancompany.spaceinvaders.shared.Direction.RIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PlayerMoverTest {

    EventStore eventStore;
    EventBus eventBus;
    CommandApi commandApi;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        eventBus = eventStore.getEventBus();
        commandApi = spaceInvaders.getCommandApi();
    }

    @Test
    void givenGameCreated_whenInitiateGameCycle_thenPlayerNotMoved() throws InterruptedException {
        // given
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteCreated.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
        });

        // when
        commandApi.publish(new InitiateGameCycle());

        // then
        Thread.sleep(500);
        Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteMoved.class));
        assertThat(eventStore.read(query)).isEmpty();
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenGameCreatedAndPlayerTurnedRight_whenInitiateGameCycle_thenPlayerMoved() throws InterruptedException {
        // given
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteCreated.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
        });

        SpriteTurned spriteTurned = new SpriteTurned(PLAYER_SPRITE_ID, RIGHT);
        eventStore.append(Event.of(spriteTurned, EntityTags.PLAYER));

        // when
        commandApi.publish(new InitiateGameCycle());

        // then
        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteMoved.class));
            List<SequencedEvent> sequencedEvents = eventStore.read(query);
            assertThat(sequencedEvents).hasSize(1);
            assertThat(sequencedEvents.getFirst().payload(SpriteMoved.class)).isEqualTo(new SpriteMoved(PLAYER_SPRITE_ID,
                    PLAYER_START_X + PLAYER_SPEED, PLAYER_START_Y));
        });
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenGameCreatedAndPlayerTurnedLeft_whenInitiateGameCycle_thenPlayerMoved() throws InterruptedException {
        // given
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteCreated.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
        });

        SpriteTurned spriteTurned = new SpriteTurned(PLAYER_SPRITE_ID, LEFT);
        eventStore.append(Event.of(spriteTurned, EntityTags.PLAYER));

        // when
        commandApi.publish(new InitiateGameCycle());

        // then
        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteMoved.class));
            List<SequencedEvent> sequencedEvents = eventStore.read(query);
            assertThat(sequencedEvents).hasSize(1);
            assertThat(sequencedEvents.getFirst().payload(SpriteMoved.class)).isEqualTo(new SpriteMoved(PLAYER_SPRITE_ID,
                    PLAYER_START_X - PLAYER_SPEED, PLAYER_START_Y));
        });
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenGameCreatedAndPlayerStopped_whenInitiateGameCycle_thenPlayerNotMoved() throws InterruptedException {
        // given
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteCreated.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
        });

        SpriteStopped spriteStopped = new SpriteStopped(PLAYER_SPRITE_ID);
        eventStore.append(Event.of(spriteStopped, EntityTags.PLAYER));

        // when
        commandApi.publish(new InitiateGameCycle());

        // then
        Thread.sleep(500);
        Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteMoved.class));
        assertThat(eventStore.read(query)).isEmpty();
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenGameCreatedAndPlayerTurnedAndPlayerMovedToRightBorder_whenInitiateGameCycle_thenPlayerNotMovedAgain() throws InterruptedException {
        // given
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteCreated.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
        });

        SpriteTurned spriteTurned = new SpriteTurned(PLAYER_SPRITE_ID, RIGHT);
        eventStore.append(Event.of(spriteTurned, EntityTags.PLAYER));

        SpriteMoved spriteMoved = new SpriteMoved(PLAYER_SPRITE_ID, PLAYER_STOP_X_RIGHT, PLAYER_START_Y);
        eventStore.append(Event.of(spriteMoved, EntityTags.PLAYER));

        // when
        commandApi.publish(new InitiateGameCycle());

        // then
        Thread.sleep(500);
        Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteMoved.class));
        List<SequencedEvent> sequencedEvents = eventStore.read(query);
        assertThat(sequencedEvents).hasSize(1);
        assertThat(sequencedEvents.getFirst().payload(SpriteMoved.class)).isEqualTo(new SpriteMoved(PLAYER_SPRITE_ID,
                PLAYER_STOP_X_RIGHT, PLAYER_START_Y));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenGameCreatedAndPlayerTurnedAndPlayerMovedToLeftBorder_whenInitiateGameCycle_thenPlayerNotMovedAgain() throws InterruptedException {
        // given
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, EntityTags.GAME));

        await().untilAsserted(() -> {
            Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteCreated.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
        });

        SpriteTurned spriteTurned = new SpriteTurned(PLAYER_SPRITE_ID, LEFT);
        eventStore.append(Event.of(spriteTurned, EntityTags.PLAYER));

        SpriteMoved spriteMoved = new SpriteMoved(PLAYER_SPRITE_ID, PLAYER_STOP_X_LEFT, PLAYER_START_Y);
        eventStore.append(Event.of(spriteMoved, EntityTags.PLAYER));

        // when
        commandApi.publish(new InitiateGameCycle());

        // then
        Thread.sleep(500);
        Query query = Query.of(EntityTags.PLAYER, Type.of(SpriteMoved.class));
        List<SequencedEvent> sequencedEvents = eventStore.read(query);
        assertThat(sequencedEvents).hasSize(1);
        assertThat(sequencedEvents.getFirst().payload(SpriteMoved.class)).isEqualTo(new SpriteMoved(PLAYER_SPRITE_ID,
                PLAYER_STOP_X_LEFT, PLAYER_START_Y));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

}
