package battle.effect.status;

import battle.Battle;
import battle.effect.generic.Effect;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.PokemonEffect;
import item.Item;
import item.berry.GainableEffectBerry;
import item.berry.StatusBerry;
import main.Global;
import message.Messages;
import pokemon.ability.AbilityNamesies;
import battle.effect.generic.EffectNamesies;
import pokemon.ActivePokemon;

import java.io.Serializable;

public abstract class Status implements Serializable {
	private static final long serialVersionUID = 1L;

	private final StatusCondition statusCondition;
	
	protected Status(StatusCondition statusCondition) {
		this.statusCondition = statusCondition;
	}
	
	protected abstract String getCastMessage(ActivePokemon p);
	protected abstract String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim);

	protected abstract String getRemoveMessage(ActivePokemon victim);
	protected abstract String getSourceRemoveMessage(ActivePokemon victim, String sourceName);
	
	// A method to be overidden if anything related to conflicted victim is necessary to create this status
	protected void postCreateEffect(ActivePokemon victim) {}
	
	public static void removeStatus(Battle b, ActivePokemon victim, CastSource source) {
		Messages.addMessage(getRemoveStatus(b, victim, source), b, victim);
	}
	
	public static String getRemoveStatus(Battle b, ActivePokemon victim, CastSource source) {
		StatusCondition status = victim.getStatus().getType();
		victim.removeStatus();
		
		switch (source) {
			case ABILITY:
				return getStatus(status, victim).getSourceRemoveMessage(victim, victim.getAbility().getName());
			case HELD_ITEM:
				return getStatus(status, victim).getSourceRemoveMessage(victim, victim.getHeldItem(b).getName());
			default:
				return getStatus(status, victim).getRemoveMessage(victim);
		}
	}
	
	public static String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim, StatusCondition status) {
		return getStatus(status, victim).getFailMessage(b, user, victim);
	}
	
	protected String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
		StatusPreventionEffect statusPrevent = StatusPreventionEffect.getPreventEffect(b, user, victim, this.statusCondition);
		if (statusPrevent != null) {
			return statusPrevent.statusPreventionMessage(victim);
		}
		
		return Effect.DEFAULT_FAIL_MESSAGE;
	}
	
	// Creates a new status like a motherfucking champ
	private static Status getStatus(StatusCondition s, ActivePokemon victim) {
		Status status = s.getStatus();
		status.postCreateEffect(victim);
		
		return status;
	}
	
	public static boolean applies(StatusCondition status, Battle b, ActivePokemon caster, ActivePokemon victim) {
		return getStatus(status, victim).applies(b, caster, victim);
	}
	
	protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
		if (victim.hasStatus()) {
			return false;
		}

		StatusPreventionEffect statusPrevent = StatusPreventionEffect.getPreventEffect(b, caster, victim, this.statusCondition);
		if (statusPrevent != null) {
			return false;
		}
		
		return true;
	}
	
	public StatusCondition getType() {
		return statusCondition;
	}
	
	public void setTurns(int turns) {}
	
	// Returns true if a status was successfully given, and false if it failed for any reason
	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
		return giveStatus(b, caster, victim, status, false);
	}
	
	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status, boolean abilityCast) {
		Status s = getStatus(status, victim);
		return giveStatus(b, caster, victim, status, abilityCast ? s.getAbilityCastMessage(caster, victim) : s.getCastMessage(victim));
	}
	
	public static boolean giveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status, String castMessage) {
		Status s = getStatus(status, victim);
		if (s.applies(b, caster, victim)) {
			victim.setStatus(s);
			Messages.addMessage(castMessage, b, victim);

			// TODO: There should be a StatusReceivedEffect interface or something
			synchronizeCheck(b, caster, victim, status);
			berryCheck(b, victim, status);
			
			return true;
		}

		return false;
	}

	// TODO: Check parameter
	private static void berryCheck(Battle b, ActivePokemon victim, StatusCondition status) {
		Item item = victim.getHeldItem(b);
		if (item instanceof StatusBerry) {
			GainableEffectBerry berry = ((GainableEffectBerry)item);
			
			if (berry.gainBerryEffect(b, victim, CastSource.HELD_ITEM)) {
				victim.consumeItem(b);				
			}
		}
	}
	
	private static void synchronizeCheck(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
		Status s = getStatus(status, caster);
		if (victim.hasAbility(AbilityNamesies.SYNCHRONIZE) && s.applies(b, victim, caster)
				&& (status == StatusCondition.BURNED || status == StatusCondition.POISONED || status == StatusCondition.PARALYZED)) {
			if (victim.hasEffect(EffectNamesies.BAD_POISON)) {
				caster.addEffect((PokemonEffect)EffectNamesies.BAD_POISON.getEffect());
			}
			
			caster.setStatus(s);
			Messages.addMessage(s.getAbilityCastMessage(victim, caster), b, caster);
			
			berryCheck(b, caster, status);
		}
	}
	
	public static void removeStatus(ActivePokemon p) {
		p.setStatus(new NoStatus());
	}
	
	public static void die(ActivePokemon p) {
		if (p.getHP() > 0) {
			Global.error("Only dead Pokemon can die.");
		}

		p.setStatus(new Fainted());
	}
}
