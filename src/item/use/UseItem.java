package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;

public interface UseItem {
	boolean use(Battle b, ActivePokemon p, Move m);
}
