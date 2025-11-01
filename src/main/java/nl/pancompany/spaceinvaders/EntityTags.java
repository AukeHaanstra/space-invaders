package nl.pancompany.spaceinvaders;

import nl.pancompany.eventstore.query.Tag;

public interface EntityTags {

    Tag GAME = Tag.of(Constants.GAME_TAG, Constants.GAME_ID);
    Tag PLAYER = Tag.of(Constants.PLAYER_ID, Constants.PLAYER_ID);
    Tag SHOT = Tag.of(Constants.SHOT_TAG, Constants.SHOT_ID);

}
