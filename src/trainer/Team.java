package trainer;

import java.util.List;

import namesies.Namesies;
import pokemon.ActivePokemon;
import battle.effect.generic.TeamEffect;

// THIS IS A DUMB NAME SOMEONE HELP ME RENAME IT
public interface Team {
	ActivePokemon front();
	List<TeamEffect> getEffects();
	boolean hasEffect(Namesies effect);
	void addEffect(TeamEffect e);
	List<ActivePokemon> getTeam();
	boolean blackout();
	void resetEffects();
	void resetUsed();
}
