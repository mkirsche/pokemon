package trainer;

import battle.Battle;
import battle.effect.SwitchOutEffect;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.TeamEffect;
import battle.effect.status.StatusCondition;
import item.ItemNamesies;
import item.bag.Bag;
import main.Global;
import pokemon.ActivePokemon;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Trainer implements Team, Serializable {
	private static final long serialVersionUID = -7797121866082399148L;

	public static final int MAX_POKEMON = 6;

	protected String name;
	protected TrainerAction action;
	protected int cashMoney;

	protected List<ActivePokemon> team;
	protected List<TeamEffect> effects;
	private int frontIndex;

//	protected boolean isBeTryingToSwitchRunOrUseItem;
//	protected boolean isBTTSROUI;
	
	protected Bag bag;
	
	public Trainer(String name, int cashMoney) {
		team = new ArrayList<>();
		effects = new ArrayList<>();
		frontIndex = 0;

		this.name = name; 
		this.cashMoney = cashMoney;
		
		bag = new Bag();
	}

	@Override
	public ActivePokemon front() {
		return team.get(frontIndex);
	}

	@Override
	public int getTeamIndex(ActivePokemon teamMember) {
		for (int i = 0; i < team.size(); i++) {
			if (teamMember == team.get(i)) {
				return i;
			}
		}

		Global.error("Team member is not apart of team.");
		return -1;
	}

	@Override
	public List<ActivePokemon> getTeam() {
		return team;
	}

	@Override
	public List<TeamEffect> getEffects() {
		return effects;
	}

	@Override
	public void resetEffects() {
		effects = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setFront(int index) {
		if (frontIndex == index) {
			return;
		}
		
		// Apply any effects that take place when switching out
		if (front().getAbility() instanceof SwitchOutEffect) {
			((SwitchOutEffect)front().getAbility()).switchOut(front());
		}
		
		frontIndex = index;
	}
	
	// Sets the front Pokemon to be the first Pokemon capable of battling
	protected void setFront() {
		for (int i = 0; i < team.size(); i++) {
			if (team.get(i).canFight()) {
				setFront(i);
				return;
			}
		}

		Global.error("None of your Pokemon are capable of battling!");
	}
	
	// Should be called when the trainer enters a new battle -- Sets all Pokemon to not be used yet and sets the front Pokemon
	public void enterBattle() {
		team.forEach(ActivePokemon::resetAttributes);
		setFront();
	}

	@Override
	public void resetUsed() {
		for (int i = 0; i < team.size(); i++) {
			team.get(i).getAttributes().setUsed(i == frontIndex);
		}
	}
	
	public abstract void addPokemon(ActivePokemon p);

	public void addItem(ItemNamesies item, int amount) {
		bag.addItem(item, amount);
	}
	
	public Bag getBag() {
		return bag;
	}
	
	public int getDatCashMoney() {
		return cashMoney;
	}
	
	// Money in da bank
	public void getDatCashMoney(int datCash) {
		cashMoney += datCash;
	}
	
	// I don't know why I can't take this seriously, sorry guys
	public int sucksToSuck(int datCash) {
		int prev = cashMoney;
		cashMoney = Math.max(0, cashMoney - datCash);
		return prev - cashMoney;
	}

	@Override
	public boolean hasEffect(EffectNamesies effect) {
		return Effect.hasEffect(effects, effect);
	}

	@Override
	public void addEffect(TeamEffect e) {
		effects.add(e);
	}

	@Override
	public boolean blackout() {
		for (ActivePokemon p : team) {
			if (p.canFight()) {
				return false;
			}
		}

		return true;
	}
	
	public void healAll() {
		team.forEach(ActivePokemon::fullyHeal);
	}
	
	public void switchToRandom(Battle b) {

		boolean maxUsed = maxPokemonUsed(b);
		List<Integer> valid = new ArrayList<>();
		for (int i = 0; i < team.size(); i++) {
			if (i == frontIndex || !team.get(i).canFight() || (maxUsed && !team.get(i).getAttributes().isBattleUsed())) {
				continue;
			}

			valid.add(i);
		}
		
		if (valid.size() == 0) {
			Global.error("You shouldn't be switching when you have nothing to switch to!");
		}

		setFront(RandomUtils.getRandomValue(valid));
	}
	
	public boolean canSwitch(Battle b, int switchIndex) {

		// This Pokemon is already out!!
		if (switchIndex == frontIndex) {
			return false;
		}
		
		ActivePokemon curr = front();
		ActivePokemon toSwitch = team.get(switchIndex);
		
		// Cannot switch to a fainted Pokemon
		if (!toSwitch.canFight()) {
			return false;
		}

		// Cannot switch to an unused Pokemon if you have already used the maximum number of Pokemon
		if (maxPokemonUsed(b) && !toSwitch.getAttributes().isBattleUsed()) {
			return false;
		}

		// If current Pokemon is dead then you must switch!
		if (curr.hasStatus(StatusCondition.FAINTED)) {
			return true;
		}
		
		// Front Pokemon is alive -- Check if they are able to switch out, if not, display the appropriate message
		return curr.canEscape(b);
	}

	public boolean maxPokemonUsed(Battle b) {
		return numPokemonUsed() == b.getOpponent().maxPokemonAllowed();
	}

	private int numPokemonUsed() {
		return (int)team.stream().filter(p -> p.getAttributes().isBattleUsed()).count();
	}
	
	// Returns true if the trainer has Pokemon (other than the one that is currently fighting) that is able to fight
	public boolean hasRemainingPokemon(Battle b) {
		boolean maxUsed = maxPokemonUsed(b);
		for (int i = 0; i < team.size(); i++) {
			if (i == frontIndex) {
				continue;
			}
			
			if (team.get(i).canFight() && (!maxUsed || team.get(i).getAttributes().isBattleUsed())) {
				return true;
			}
		}
		
		return false;
	}
	
	public void performAction(Battle b, TrainerAction a) {
		setAction(a);
		b.fight();
	}
	
	public void setAction(TrainerAction a) {
		action = a;
	}
	
	public TrainerAction getAction() {
		return action;
	}
	
	public void swapPokemon(int i, int j) {
		ActivePokemon tmp = team.get(i);
		team.set(i, team.get(j));
		team.set(j, tmp);
	}
}
