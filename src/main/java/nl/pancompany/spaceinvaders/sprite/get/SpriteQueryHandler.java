package nl.pancompany.spaceinvaders.sprite.get;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class SpriteQueryHandler {

    private final SpriteRepository spriteRepository;

    public Optional<SpriteReadModel> get(GetSpriteById getSpriteById) {
        return spriteRepository.findById(getSpriteById.spriteId());
    }
}
