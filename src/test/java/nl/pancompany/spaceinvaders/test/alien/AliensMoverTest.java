package nl.pancompany.spaceinvaders.test.alien;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import nl.pancompany.spaceinvaders.shared.Constants;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosion;
import nl.pancompany.spaceinvaders.test.TestUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.shared.Constants.*;
import static nl.pancompany.spaceinvaders.shared.Constants.ALIEN_SPEED;
import static nl.pancompany.spaceinvaders.shared.Constants.ALIEN_SPRITE_IDS;
import static nl.pancompany.spaceinvaders.shared.EntityTags.GAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class AliensMoverTest {

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
    void givenAlienRIP_whenGameCycleInitiated_thenNotAlienMoved() throws InterruptedException {
        SpriteId alienSpriteId = ALIEN_SPRITE_IDS.getFirst();
        Tag alienTag = Tag.of(ALIEN_ENTITY);
        Tag alienSpriteTag = Tag.of(SPRITE_ENTITY, alienSpriteId.toString());
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));
        await().untilAsserted(() -> {
            Query query = Query.of(alienSpriteTag, Type.of(SpriteCreated.class));
            assertThat(eventStore.read(query)).hasSize(1);
        });
        SpriteRestsInPeace spriteRestsInPeace = new SpriteRestsInPeace(alienSpriteId);
        eventStore.append(Event.of(spriteRestsInPeace, Tags.and(alienSpriteTag, alienTag)));

        GameCycleInitiated gameCycleInitiated = new GameCycleInitiated();
        eventStore.append(Event.of(gameCycleInitiated, GAME));

        Thread.sleep(500);
        Query query = Query.of(alienSpriteTag, Type.of(SpriteMoved.class));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events).hasSize(0);
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenGameCreated_whenGameCycleInitiated_thenAlienMovedLeft() {
        SpriteId alienSpriteId = ALIEN_SPRITE_IDS.getFirst();
        Tag alienSpriteTag = Tag.of(SPRITE_ENTITY, alienSpriteId.toString());
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));
        await().untilAsserted(() -> {
            Query query = Query.of(alienSpriteTag, Type.of(SpriteCreated.class));
            assertThat(eventStore.read(query)).hasSize(1);
        });

        GameCycleInitiated gameCycleInitiated = new GameCycleInitiated();
        eventStore.append(Event.of(gameCycleInitiated, GAME));

        await().untilAsserted(() -> {
            Query query = Query.of(alienSpriteTag, Type.of(SpriteMoved.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
            SpriteMoved spriteMoved = events.getLast().payload(SpriteMoved.class);
            assertThat(spriteMoved.newX()).isEqualTo(ALIEN_START_X - ALIEN_SPEED);
        });
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenGameCreatedAndSpriteTurned_whenGameCycleInitiated_thenAlienMovedRight() {
        SpriteId alienSpriteId = ALIEN_SPRITE_IDS.getFirst();
        Tag alienTag = Tag.of(ALIEN_ENTITY);
        Tag alienSpriteTag = Tag.of(SPRITE_ENTITY, alienSpriteId.toString());
        GameCreated gameCreated = new GameCreated();
        eventStore.append(Event.of(gameCreated, GAME));
        await().untilAsserted(() -> {
            Query query = Query.of(alienSpriteTag, Type.of(SpriteCreated.class));
            assertThat(eventStore.read(query)).hasSize(1);
        });
        SpriteTurned spriteTurned = new SpriteTurned(alienSpriteId, Direction.RIGHT);
        eventStore.append(Event.of(spriteTurned, Tags.and(alienSpriteTag, alienTag)));

        GameCycleInitiated gameCycleInitiated = new GameCycleInitiated();
        eventStore.append(Event.of(gameCycleInitiated, Tags.and(alienSpriteTag, alienTag)));

        await().untilAsserted(() -> {
            Query query = Query.of(alienSpriteTag, Type.of(SpriteMoved.class));
            List<SequencedEvent> events = eventStore.read(query);
            assertThat(events).hasSize(1);
            SpriteMoved spriteMoved = events.getLast().payload(SpriteMoved.class);
            assertThat(spriteMoved.newX()).isEqualTo(ALIEN_START_X + ALIEN_SPEED);
        });
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

    @Test
    void givenAlienTurnedUp_whenGameCycleInitiated_thenIllegalState() {
        TestUtil.withoutLogging( () -> {
            SpriteId alienSpriteId = ALIEN_SPRITE_IDS.getFirst();
            Tag alienTag = Tag.of(ALIEN_ENTITY);
            Tag alienSpriteTag = Tag.of(SPRITE_ENTITY, alienSpriteId.toString());
            GameCreated gameCreated = new GameCreated();
            eventStore.append(Event.of(gameCreated, GAME));
            await().untilAsserted(() -> {
                Query query = Query.of(alienSpriteTag, Type.of(SpriteCreated.class));
                assertThat(eventStore.read(query)).hasSize(1);
            });
            SpriteTurned spriteTurned = new SpriteTurned(alienSpriteId, Direction.UP);
            eventStore.append(Event.of(spriteTurned, Tags.and(alienSpriteTag, alienTag)));

            GameCycleInitiated gameCycleInitiated = new GameCycleInitiated();
            eventStore.append(Event.of(gameCycleInitiated, Tags.and(alienSpriteTag, alienTag)));

            await().untilAsserted(() -> {
                assertThat(eventBus.hasLoggedExceptions()).isTrue();
                assertThat(eventBus.getLoggedExceptions()).hasSize(1);
                assertThat(eventBus.getLoggedExceptions().getFirst().exception()).isInstanceOf(IllegalStateException.class);
            });
        });
    }



}
