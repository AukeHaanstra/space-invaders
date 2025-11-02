package nl.pancompany.spaceinvaders;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteById;
import nl.pancompany.spaceinvaders.sprite.get.SpriteQueryHandler;

import java.util.Optional;

@Slf4j
@Builder
public class QueryApi {

    private final SpriteQueryHandler spriteQueryHandler;

    @SuppressWarnings("unchecked")
    public <T> Optional<T> query(Object query, Class<T> resultType) {
        return switch (query) {
            case null -> throw new IllegalArgumentException("Null Query");
            // Sprite
            case GetSpriteById getSpriteById -> (Optional<T>) spriteQueryHandler.get(getSpriteById);

            default -> throw new IllegalArgumentException("Unexpected Query: " + query);
        };
    }
}
