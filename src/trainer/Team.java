package trainer;

import java.util.List;

import pokemon.ActivePokemon;
import battle.effect.TeamEffect;

public interface Team // THIS IS A DUMB NAME SOMEONE HELP ME RENAME IT 
{
	public ActivePokemon front();
	public List<TeamEffect> getEffects();
	public boolean hasEffect(String effect);
	public void addEffect(TeamEffect e);
	public List<ActivePokemon> getTeam();
	public boolean blackout();
	public void resetEffects();
	public void resetUsed();
}
