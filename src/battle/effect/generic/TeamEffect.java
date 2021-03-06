package battle.effect.generic;

import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.SimpleStatModifyingEffect;
import battle.effect.generic.EffectInterfaces.BarrierEffect;
import battle.effect.generic.EffectInterfaces.CritBlockerEffect;
import battle.effect.generic.EffectInterfaces.DefogRelease;
import battle.effect.generic.EffectInterfaces.EndBattleEffect;
import battle.effect.generic.EffectInterfaces.EntryEffect;
import battle.effect.generic.EffectInterfaces.RapidSpinRelease;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.ItemNamesies;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import trainer.Trainer;
import type.Type;

import java.io.Serializable;

// Class to handle effects that are specific to one side of the battle
public abstract class TeamEffect extends Effect implements Serializable {
	private static final long serialVersionUID = 1L;

	public TeamEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		super(name, minTurns, maxTurns, nextTurnSubside);
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		if (printCast) {
			Messages.add(getCastMessage(b, caster, victim, source));
		}

		b.getTrainer(victim.isPlayer()).addEffect(this);
		
		Messages.add(new MessageUpdate().updatePokemon(b, caster));
		Messages.add(new MessageUpdate().updatePokemon(b, victim));
	}

	// EVERYTHING BELOW IS GENERATED ###
	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	static class Reflect extends TeamEffect implements BarrierEffect, SimpleStatModifyingEffect {
		private static final long serialVersionUID = 1L;

		Reflect() {
			super(EffectNamesies.REFLECT, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public void breakBarrier(Battle b, ActivePokemon breaker) {
			Messages.add(breaker.getName() + " broke the reflect barrier!");
			b.getEffects(!breaker.isPlayer()).remove(this);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " raised the " + Stat.DEFENSE.getName().toLowerCase() + " of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of reflect faded.";
		}

		public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
			return !opp.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE;
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add("The effects of reflect faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim).remove(this);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
				Effect.getEffect(b.getEffects(victim), this.namesies).setTurns(8);
			}
		}

		public double getModifier() {
			return 2;
		}
	}

	static class LightScreen extends TeamEffect implements BarrierEffect, SimpleStatModifyingEffect {
		private static final long serialVersionUID = 1L;

		LightScreen() {
			super(EffectNamesies.LIGHT_SCREEN, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public void breakBarrier(Battle b, ActivePokemon breaker) {
			Messages.add(breaker.getName() + " broke the light screen barrier!");
			b.getEffects(!breaker.isPlayer()).remove(this);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " raised the " + Stat.SP_DEFENSE.getName().toLowerCase() + " of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of light screen faded.";
		}

		public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
			return !opp.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SP_DEFENSE;
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add("The effects of light screen faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim).remove(this);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
				Effect.getEffect(b.getEffects(victim), this.namesies).setTurns(8);
			}
		}

		public double getModifier() {
			return 2;
		}
	}

	static class Tailwind extends TeamEffect implements SimpleStatModifyingEffect {
		private static final long serialVersionUID = 1L;

		Tailwind() {
			super(EffectNamesies.TAILWIND, 4, 4, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " raised the speed of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of tailwind faded.";
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public double getModifier() {
			return 2;
		}
	}

	static class AuroraVeil extends TeamEffect implements SimpleStatModifyingEffect {
		private static final long serialVersionUID = 1L;

		AuroraVeil() {
			super(EffectNamesies.AURORA_VEIL, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
				Effect.getEffect(b.getEffects(victim), this.namesies).setTurns(8);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " is covered by an aurora veil!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of aurora veil faded.";
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
		}

		public double getModifier() {
			return 2;
		}
	}

	static class StickyWeb extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;

		StickyWeb() {
			super(EffectNamesies.STICKY_WEB, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return "Sticky web covers everything!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add("The sticky web spun away!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.isLevitating(b)) {
				return;
			}
			
			enterer.getAttributes().modifyStage(b.getOtherPokemon(enterer), enterer, -1, Stat.SPEED, b, CastSource.EFFECT, "The sticky web {change} " + enterer.getName() + "'s {statName}!");
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add("The sticky web dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim).remove(this);
		}
	}

	static class StealthRock extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;

		StealthRock() {
			super(EffectNamesies.STEALTH_ROCK, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return "Floating rocks were scattered all around!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add("The floating rocks spun away!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(enterer.getName() + " was hurt by stealth rock!");
			enterer.reduceHealthFraction(b, Type.ROCK.getAdvantage().getAdvantage(enterer, b)/8.0);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add("The floating rocks dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim).remove(this);
		}
	}

	static class ToxicSpikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;
		private int layers;

		ToxicSpikes() {
			super(EffectNamesies.TOXIC_SPIKES, -1, -1, false);
			this.layers = 1;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect spikesies = Effect.getEffect(b.getEffects(victim), this.namesies);
			if (spikesies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			((ToxicSpikes)spikesies).layers++;
			Messages.add(getCastMessage(b, caster, victim, source));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return "Toxic spikes were scattered all around!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add("The toxic spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.isLevitating(b)) {
				return;
			}
			
			if (enterer.isType(b, Type.POISON)) {
				Messages.add(enterer.getName() + " absorbed the Toxic Spikes!");
				super.active = false;
				return;
			}
			
			ActivePokemon theOtherPokemon = b.getOtherPokemon(enterer);
			StatusCondition poisonCondition = layers >= 2 ? StatusCondition.BADLY_POISONED : StatusCondition.POISONED;
			Status.giveStatus(b, theOtherPokemon, enterer, poisonCondition);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add("The toxic spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim).remove(this);
		}
	}

	static class Spikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;
		private int layers;

		Spikes() {
			super(EffectNamesies.SPIKES, -1, -1, false);
			this.layers = 1;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect spikesies = Effect.getEffect(b.getEffects(victim), this.namesies);
			if (spikesies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			((Spikes)spikesies).layers++;
			Messages.add(getCastMessage(b, caster, victim, source));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return "Spikes were scattered all around!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add("The spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.isLevitating(b) || enterer.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(enterer.getName() + " was hurt by spikes!");
			if (layers == 1) {
				enterer.reduceHealthFraction(b, 1/8.0);
			}
			else if (layers == 2) {
				enterer.reduceHealthFraction(b, 1/6.0);
			}
			else {
				enterer.reduceHealthFraction(b, 1/4.0);
			}
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add("The spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim).remove(this);
		}
	}

	static class Wish extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private String casterName;

		Wish() {
			super(EffectNamesies.WISH, 1, 1, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			casterName = caster.getName();
			super.cast(b, caster, victim, source, printCast);
		}

		public void subside(Battle b, ActivePokemon p) {
			if (p.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			p.healHealthFraction(1/2.0);
			Messages.add(new MessageUpdate(casterName + "'s wish came true!").updatePokemon(b, p));
		}
	}

	static class LuckyChant extends TeamEffect implements CritBlockerEffect {
		private static final long serialVersionUID = 1L;

		LuckyChant() {
			super(EffectNamesies.LUCKY_CHANT, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return "The lucky chant shielded " + victim.getName() + "'s team from critical hits!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of lucky chant wore off.";
		}
	}

	static class FutureSight extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private ActivePokemon theSeeer;

		FutureSight() {
			super(EffectNamesies.FUTURE_SIGHT, 2, 2, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			theSeeer = caster;
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return theSeeer.getName() + " foresaw an attack!";
		}

		public void subside(Battle b, ActivePokemon p) {
			Messages.add(p.getName() + " took " + theSeeer.getName() + "'s attack!");
			
			Attack attack = AttackNamesies.FUTURE_SIGHT.getAttack();
			
			// Don't do anything for moves that are uneffective
			if (!attack.effective(b, theSeeer, p)) {
				return;
			}
			
			theSeeer.setMove(new Move(attack));
			theSeeer.getAttack().applyDamage(theSeeer, p, b);
		}
	}

	static class DoomDesire extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private ActivePokemon theSeeer;

		DoomDesire() {
			super(EffectNamesies.DOOM_DESIRE, 2, 2, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			theSeeer = caster;
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return theSeeer.getName() + " foresaw an attack!";
		}

		public void subside(Battle b, ActivePokemon p) {
			Messages.add(p.getName() + " took " + theSeeer.getName() + "'s attack!");
			
			Attack attack = AttackNamesies.DOOM_DESIRE.getAttack();
			
			// Don't do anything for moves that are uneffective
			if (!attack.effective(b, theSeeer, p)) {
				return;
			}
			
			theSeeer.setMove(new Move(attack));
			theSeeer.getAttack().applyDamage(theSeeer, p, b);
		}
	}

	static class HealSwitch extends TeamEffect implements EntryEffect {
		private static final long serialVersionUID = 1L;
		private String wish;

		HealSwitch() {
			super(EffectNamesies.HEAL_SWITCH, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			wish = caster.getAttack().namesies() == AttackNamesies.LUNAR_DANCE ? "lunar dance" : "healing wish";
			super.cast(b, caster, victim, source, printCast);
		}

		public void enter(Battle b, ActivePokemon enterer) {
			enterer.healHealthFraction(1);
			enterer.removeStatus();
			
			Messages.add(new MessageUpdate(enterer.getName() + " health was restored due to the " + wish + "!").updatePokemon(b, enterer));
			super.active = false;
		}
	}

	static class DeadAlly extends TeamEffect {
		private static final long serialVersionUID = 1L;

		DeadAlly() {
			super(EffectNamesies.DEAD_ALLY, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}
	}

	static class PayDay extends TeamEffect implements EndBattleEffect {
		private static final long serialVersionUID = 1L;
		private int coins;

		PayDay() {
			super(EffectNamesies.PAY_DAY, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			PayDay payday = (PayDay)Effect.getEffect(b.getEffects(true), this.namesies);
			Messages.add(getCastMessage(b, caster, victim, source));
			coins = 5*caster.getLevel();
			if (payday == null) {
				b.getPlayer().addEffect(this);
			}
			else {
				payday.coins += coins;
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return "Coins scattered everywhere!";
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
			Messages.add(player.getName() + " picked up " + coins + " pokedollars!");
			player.getDatCashMoney(coins);
		}
	}

	static class GetDatCashMoneyTwice extends TeamEffect {
		private static final long serialVersionUID = 1L;

		GetDatCashMoneyTwice() {
			super(EffectNamesies.GET_DAT_CASH_MONEY_TWICE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim), this.namesies));
		}
	}
}
