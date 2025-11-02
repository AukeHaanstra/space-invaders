package nl.pancompany.spaceinvaders.sprite.get;

import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.Optional;

public record GetSpriteById(SpriteId spriteId) {
    public record Result(Optional<SpriteReadModel> spriteReadModel) {
        public static Result of(Optional<SpriteReadModel> spriteReadModel) {
            return new Result(spriteReadModel);
        }

        public boolean isPresent() {
            return spriteReadModel.isPresent();
        }
        public SpriteReadModel get() {
            return spriteReadModel.get();
        }
        public SpriteReadModel orElseThrow() {
            return spriteReadModel.orElseThrow();
        }
        public SpriteReadModel orElse(SpriteReadModel other) {
            return spriteReadModel.orElse(other);
        }
    }
}
