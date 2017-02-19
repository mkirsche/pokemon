package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;
import trainer.Trainer;

public interface PlayerUseItem extends UseItem {
	boolean use();

	default boolean use(Battle b, ActivePokemon p, Move m) {
		return this.use();
	}
}