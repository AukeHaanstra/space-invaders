package nl.pancompany.spaceinvaders.test.sprite;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencePosition;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.spaceinvaders.QueryApi;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.*;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteById;
import nl.pancompany.spaceinvaders.sprite.get.SpriteReadModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static nl.pancompany.spaceinvaders.Constants.*;
import static nl.pancompany.spaceinvaders.EntityTags.PLAYER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetSpriteTest {

    EventStore eventStore;
    QueryApi queryApi;
    EventBus eventBus;
    Query playerQuery = Query.taggedWith(PLAYER).build();

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        queryApi = spaceInvaders.getQueryApi();
        eventBus = eventStore.getEventBus();
    }

    @Test
    void givenNoSpriteCreated_thenNoReadModelFound() {
        assertThat(queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID))).isEmpty();
    }

    @Test
    void givenSpriteCreated_thenReadModelFound() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X,
                PLAYER_START_Y, PLAYER_SPEED, Direction.LEFT); // changed to LEFT
        eventStore.append(Event.of(spriteCreated, PLAYER));
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(1));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().imagePath()).isEqualTo(PLAYER_IMAGE_PATH);
        assertThat(readModel.get().x()).isEqualTo(PLAYER_START_X);
        assertThat(readModel.get().y()).isEqualTo(PLAYER_START_Y);
        assertThat(readModel.get().speed()).isEqualTo(PLAYER_SPEED);
        assertThat(readModel.get().direction()).isEqualTo(Direction.LEFT);
        assertThat(readModel.get().explosionTriggered()).isEqualTo(false);
        assertThat(readModel.get().visible()).isEqualTo(true);
    }

    @Test
    void givenSpriteCreated_whenSpriteTurned_thenDirectionIsUpdated() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X,
                PLAYER_START_Y, PLAYER_SPEED, Direction.LEFT);
        eventStore.append(Event.of(spriteCreated, PLAYER));

        SpriteTurned spriteTurned = new SpriteTurned(PLAYER_SPRITE_ID, Direction.RIGHT);
        eventStore.append(Event.of(spriteTurned, PLAYER));
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(2));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().direction()).isEqualTo(Direction.RIGHT);
    }

    @Test
    void givenSpriteCreated_whenSpriteMoved_thenCoordinatesAreUpdated() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X,
                PLAYER_START_Y, PLAYER_SPEED, Direction.LEFT);
        eventStore.append(Event.of(spriteCreated, PLAYER));

        SpriteMoved spriteMoved = new SpriteMoved(PLAYER_SPRITE_ID, 42, 24);
        eventStore.append(Event.of(spriteMoved, PLAYER));
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(2));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().x()).isEqualTo(42);
        assertThat(readModel.get().y()).isEqualTo(24);
    }

    @Test
    void givenSpriteCreated_whenSpriteStopped_thenDirectionIsUpdated() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X,
                PLAYER_START_Y, PLAYER_SPEED, Direction.LEFT);
        eventStore.append(Event.of(spriteCreated, PLAYER));

        SpriteStopped spriteStopped = new SpriteStopped(PLAYER_SPRITE_ID);
        eventStore.append(Event.of(spriteStopped, PLAYER));
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(2));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().direction()).isEqualTo(Direction.NONE);
    }

    @Test
    void givenSpriteCreated_whenSpriteImageChanged_thenImagePathIsUpdated() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X,
                PLAYER_START_Y, PLAYER_SPEED, Direction.LEFT);
        eventStore.append(Event.of(spriteCreated, PLAYER));

        SpriteImageChanged spriteImageChanged = new SpriteImageChanged(PLAYER_SPRITE_ID, "newPath");
        eventStore.append(Event.of(spriteImageChanged, PLAYER));
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(2));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().imagePath()).isEqualTo("newPath");
    }

    @Test
    void givenSpriteCreated_whenSpriteExplosionTriggered_thenImagePathIsUpdated() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X,
                PLAYER_START_Y, PLAYER_SPEED, Direction.LEFT);
        eventStore.append(Event.of(spriteCreated, PLAYER));

        SpriteExplosionTriggered spriteExplosionTriggered = new SpriteExplosionTriggered(PLAYER_SPRITE_ID);
        eventStore.append(Event.of(spriteExplosionTriggered, PLAYER));
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(2));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().explosionTriggered()).isTrue();
    }

    @Test
    void givenSpriteCreated_whenRestInPeaceSprite_thenVisibilityIsUpdated() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X,
                PLAYER_START_Y, PLAYER_SPEED, Direction.LEFT);
        eventStore.append(Event.of(spriteCreated, PLAYER));

        SpriteRestsInPeace spriteRestsInPeace = new SpriteRestsInPeace(PLAYER_SPRITE_ID);
        eventStore.append(Event.of(spriteRestsInPeace, PLAYER));
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(2));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().visible()).isFalse();
    }

    @Test
    void givenSpriteEvents_whenPartialReplay_thenReadModelPartiallyUpdated() throws InterruptedException {
        // given
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X, // 1
                PLAYER_START_Y, PLAYER_SPEED, Direction.LEFT); // LEFT: 1st value
        SpriteTurned spriteTurned = new SpriteTurned(PLAYER_SPRITE_ID, Direction.RIGHT); // 2, RIGHT: 2nd value
        SpriteMoved spriteMoved = new SpriteMoved(PLAYER_SPRITE_ID, 42, 24); // 3
        SpriteStopped spriteStopped = new SpriteStopped(PLAYER_SPRITE_ID); // 4, NONE: 3rd value
        SpriteImageChanged spriteImageChanged = new SpriteImageChanged(PLAYER_SPRITE_ID, "newPath"); // 5
        SpriteExplosionTriggered spriteExplosionTriggered = new SpriteExplosionTriggered(PLAYER_SPRITE_ID); // 6
        SpriteRestsInPeace spriteRestsInPeace = new SpriteRestsInPeace(PLAYER_SPRITE_ID); // 7
        eventStore.append(Event.of(spriteCreated, PLAYER), Event.of(spriteTurned, PLAYER), Event.of(spriteMoved, PLAYER),
                Event.of(spriteStopped, PLAYER), Event.of(spriteImageChanged, PLAYER),
                Event.of(spriteExplosionTriggered, PLAYER), Event.of(spriteRestsInPeace, PLAYER));

        // assert given events have updated the read model
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(7));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().imagePath()).isEqualTo("newPath");
        assertThat(readModel.get().x()).isEqualTo(42);
        assertThat(readModel.get().y()).isEqualTo(24);
        assertThat(readModel.get().speed()).isEqualTo(PLAYER_SPEED);
        assertThat(readModel.get().direction()).isEqualTo(Direction.NONE); // 3rd value
        assertThat(readModel.get().explosionTriggered()).isEqualTo(true);
        assertThat(readModel.get().visible()).isEqualTo(false);

        // when
        eventBus.replay(SequencePosition.of(3)); // replay events 0, 1, 2

        // then
        await().untilAsserted(() -> {
            Optional<SpriteReadModel> spriteReadModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
            assertThat(spriteReadModel.get().x()).isEqualTo(42);
        });

        Thread.sleep(400); // wait a little longer for any events to be erroneously replayed
        assertThat(eventStore.read(playerQuery)).hasSize(7);
        readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));
        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().imagePath()).isEqualTo(PLAYER_IMAGE_PATH); // old value
        assertThat(readModel.get().x()).isEqualTo(42); // new value
        assertThat(readModel.get().y()).isEqualTo(24); // new value
        assertThat(readModel.get().speed()).isEqualTo(PLAYER_SPEED);
        assertThat(readModel.get().direction()).isEqualTo(Direction.RIGHT); // 2nd value
        assertThat(readModel.get().explosionTriggered()).isEqualTo(false); // old value
        assertThat(readModel.get().visible()).isEqualTo(true);
    }

}
