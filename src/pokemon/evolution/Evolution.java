package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

import java.io.Serializable;

public abstract class Evolution implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use);
	public abstract PokemonNamesies[] getEvolutions();
	public abstract String getString();

	public boolean canEvolve() {
		return true;
	}
}
