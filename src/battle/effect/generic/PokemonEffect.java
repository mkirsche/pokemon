package battle.effect.generic;

import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.MessageGetter;
import battle.effect.PassableEffect;
import battle.effect.SapHealthEffect;
import battle.effect.attack.ChangeAbilityMove;
import battle.effect.attack.ChangeTypeSource;
import battle.effect.generic.BattleEffect.FieldUproar;
import battle.effect.generic.EffectInterfaces.AbsorbDamageEffect;
import battle.effect.generic.EffectInterfaces.AccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.AlwaysCritEffect;
import battle.effect.generic.EffectInterfaces.AttackSelectionEffect;
import battle.effect.generic.EffectInterfaces.AttackSelectionSelfBlockerEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeAttackTypeEffect;
import battle.effect.generic.EffectInterfaces.ChangeMoveListEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.CritStageEffect;
import battle.effect.generic.EffectInterfaces.DamageTakenEffect;
import battle.effect.generic.EffectInterfaces.DefendingNoAdvantageChanger;
import battle.effect.generic.EffectInterfaces.DefogRelease;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.EffectBlockerEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.ForceMoveEffect;
import battle.effect.generic.EffectInterfaces.GroundedEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.PhysicalContactEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.ProtectingEffect;
import battle.effect.generic.EffectInterfaces.RapidSpinRelease;
import battle.effect.generic.EffectInterfaces.SelfAttackBlocker;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import battle.effect.generic.EffectInterfaces.StatSwitchingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.StatusReceivedEffect;
import battle.effect.generic.EffectInterfaces.TakeDamageEffect;
import battle.effect.generic.EffectInterfaces.TargetSwapperEffect;
import battle.effect.generic.EffectInterfaces.TrappingEffect;
import battle.effect.holder.AbilityHolder;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import type.Type;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Class to handle effects that are on a single Pokemon
public abstract class PokemonEffect extends Effect implements Serializable {
	private static final long serialVersionUID = 1L;

	public PokemonEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		super(name, minTurns, maxTurns, nextTurnSubside);
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		if (printCast) {
			Messages.add(getCastMessage(b, caster, victim, source));
		}

		victim.addEffect(this);
		
		Messages.add(new MessageUpdate().updatePokemon(b, caster));
		Messages.add(new MessageUpdate().updatePokemon(b, victim));
	}

	// EVERYTHING BELOW IS GENERATED ###
	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	static class LeechSeed extends PokemonEffect implements EndTurnEffect, RapidSpinRelease, PassableEffect, SapHealthEffect {
		private static final long serialVersionUID = 1L;

		LeechSeed() {
			super(EffectNamesies.LEECH_SEED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.isType(b, Type.GRASS) || victim.hasEffect(this.namesies));
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(this.getSapMessage(victim));
			this.sapHealth(b, b.getOtherPokemon(victim), victim, victim.reduceHealthFraction(b, 1/8.0), false);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " was seeded!";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from leech seed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.isType(b, Type.GRASS)) {
				return "It doesn't affect " + victim.getName() + "!";
			}
			else if (victim.hasEffect(this.namesies)) {
				return victim.getName() + " is already seeded!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			// Need to override this to not sap health when applying damage
		}
	}

	static class Flinch extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Flinch() {
			super(EffectNamesies.FLINCH, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.INNER_FOCUS) && !caster.breaksTheMold()) || !b.isFirstAttack() || victim.hasEffect(this.namesies));
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			return false;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (victim.hasAbility(AbilityNamesies.STEADFAST)) {
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " flinched!";
		}
	}

	static class FireSpin extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		FireSpin() {
			super(EffectNamesies.FIRE_SPIN, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " was trapped in the fiery vortex!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by fire spin.";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from fire spin!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " is hurt by fire spin!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to fire spin!";
		}
	}

	static class Infestation extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Infestation() {
			super(EffectNamesies.INFESTATION, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " has been afflicted with an infestation!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by infestation.";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from infestation!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " is hurt by infestation!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to infestation!";
		}
	}

	static class MagmaStorm extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		MagmaStorm() {
			super(EffectNamesies.MAGMA_STORM, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " was trapped by swirling magma!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by magma storm.";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from magma storm!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " is hurt by magma storm!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to magma storm!";
		}
	}

	static class Clamped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Clamped() {
			super(EffectNamesies.CLAMPED, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " clamped " + victim.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by clamp.";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from clamp!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " is hurt by clamp!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to clamp!";
		}
	}

	static class Whirlpooled extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Whirlpooled() {
			super(EffectNamesies.WHIRLPOOLED, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " was trapped in the vortex!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by whirlpool.";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from whirlpool!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " is hurt by whirlpool!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to whirlpool!";
		}
	}

	static class Wrapped extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Wrapped() {
			super(EffectNamesies.WRAPPED, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " was wrapped by " + user.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by wrap.";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from wrap!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " is hurt by wrap!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to wrap!";
		}
	}

	static class Binded extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		Binded() {
			super(EffectNamesies.BINDED, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " was binded by " + user.getName() + "!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by bind.";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from bind!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " is hurt by bind!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to bind!";
		}
	}

	static class SandTomb extends PokemonEffect implements EndTurnEffect, TrappingEffect, RapidSpinRelease {
		private static final long serialVersionUID = 1L;

		SandTomb() {
			super(EffectNamesies.SAND_TOMB, 4, 5, true);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.GRIP_CLAW)) setTurns(5);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " was trapped by sand tomb!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer trapped by sand tomb.";
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.add(releaser.getName() + " was released from sand tomb!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser).remove(this);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " is hurt by sand tomb!");
			
			// Reduce 1/8 of the victim's total health, or 1/6 if holding a binding band
			victim.reduceHealthFraction(b, b.getOtherPokemon(victim).isHoldingItem(b, ItemNamesies.BINDING_BAND) ? 1/6.0 : 1/8.0);
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to sand tomb!";
		}
	}

	static class KingsShield extends PokemonEffect implements ProtectingEffect {
		private static final long serialVersionUID = 1L;

		KingsShield() {
			super(EffectNamesies.KINGS_SHIELD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
			// Pokemon that make contact with the king's shield have their attack reduced
			if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT)) {
				p.getAttributes().modifyStage(opp, p, -2, Stat.ATTACK, b, CastSource.EFFECT, "The King's Shield {change} " + p.getName() + "'s attack!");
			}
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " protected itself!";
		}
	}

	static class SpikyShield extends PokemonEffect implements ProtectingEffect {
		private static final long serialVersionUID = 1L;

		SpikyShield() {
			super(EffectNamesies.SPIKY_SHIELD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
			// Pokemon that make contact with the spiky shield have their health reduced
			if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT)) {
				Messages.add(p.getName() + " was hurt by " + opp.getName() + "'s Spiky Shield!");
				p.reduceHealthFraction(b, 1/8.0);
			}
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " protected itself!";
		}
	}

	static class BanefulBunker extends PokemonEffect implements ProtectingEffect {
		private static final long serialVersionUID = 1L;

		BanefulBunker() {
			super(EffectNamesies.BANEFUL_BUNKER, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void protectingEffects(Battle b, ActivePokemon p, ActivePokemon opp) {
			// Pokemon that make contact with the baneful bunker are become poisoned
			if (p.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) && Status.applies(StatusCondition.POISONED, b, opp, p)) {
				Status.giveStatus(b, opp, p, StatusCondition.POISONED, p.getName() + " was poisoned by " + opp.getName() + "'s Baneful Bunker!");
			}
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " protected itself!";
		}
	}

	static class Protecting extends PokemonEffect implements ProtectingEffect {
		private static final long serialVersionUID = 1L;

		Protecting() {
			super(EffectNamesies.PROTECTING, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " protected itself!";
		}
	}

	static class QuickGuard extends PokemonEffect implements ProtectingEffect {
		private static final long serialVersionUID = 1L;

		QuickGuard() {
			super(EffectNamesies.QUICK_GUARD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return b.getPriority(attacking, attacking.getAttack()) > 0;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " protected itself!";
		}
	}

	static class CraftyShield extends PokemonEffect implements ProtectingEffect {
		private static final long serialVersionUID = 1L;

		CraftyShield() {
			super(EffectNamesies.CRAFTY_SHIELD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return attacking.getAttack().isStatusMove();
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " protected itself!";
		}
	}

	static class MatBlock extends PokemonEffect implements ProtectingEffect {
		private static final long serialVersionUID = 1L;

		MatBlock() {
			super(EffectNamesies.MAT_BLOCK, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean protectingCondition(Battle b, ActivePokemon attacking) {
			return !attacking.getAttack().isStatusMove();
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			// No successive decay for this move
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " protected itself!";
		}
	}

	static class Bracing extends PokemonEffect implements BracingEffect {
		private static final long serialVersionUID = 1L;

		Bracing() {
			super(EffectNamesies.BRACING, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " braced itself!";
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth) {
			return true;
		}

		public String braceMessage(ActivePokemon bracer) {
			return bracer.getName() + " endured the hit!";
		}
	}

	static class Confusion extends PokemonEffect implements PassableEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private int turns;

		Confusion() {
			super(EffectNamesies.CONFUSION, -1, -1, false);
			this.turns = RandomUtils.getRandomInt(1, 4); // Between 1 and 4 turns
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.OWN_TEMPO) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Snap it out!
			if (turns == 0) {
				Messages.add(p.getName() + " snapped out of its confusion!");
				super.active = false;
				return true;
			}
			
			turns--;
			Messages.add(p.getName() + " is confused!");
			
			// 50% chance to hurt yourself in confusion while confused
			if (RandomUtils.chanceTest(50)) {
				Messages.add("It hurt itself in confusion!");
				
				// Perform confusion damage
				Move temp = p.getMove();
				p.setMove(new Move(AttackNamesies.CONFUSION_DAMAGE.getAttack()));
				p.reduceHealth(b, b.calculateDamage(p, p));
				p.setMove(temp);
				
				return false;
			}
			
			return true;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (victim.isHoldingItem(b, ItemNamesies.PERSIM_BERRY)) {
				Messages.add(victim.getName() + "'s " + ItemNamesies.PERSIM_BERRY.getName() + " snapped it out of confusion!");
				victim.getAttributes().removeEffect(this.namesies);
				victim.consumeItem(b);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " became confused!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasEffect(this.namesies)) {
				return victim.getName() + " is already confused!";
			}
			else if (victim.hasAbility(AbilityNamesies.OWN_TEMPO)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents confusion!";
			}
			
			return super.getFailMessage(b, user, victim);
		}
	}

	static class SelfConfusion extends PokemonEffect implements ForceMoveEffect {
		private static final long serialVersionUID = 1L;
		private Move move;

		SelfConfusion() {
			super(EffectNamesies.SELF_CONFUSION, 2, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			move = caster.getMove();
			super.cast(b, caster, victim, source, printCast);
		}

		public Move getForcedMove() {
			return move;
		}

		public void subside(Battle b, ActivePokemon p) {
			Confusion c = new Confusion();
			if (c.applies(b, p, p, CastSource.EFFECT)) {
				Messages.add(p.getName() + " became confused due to fatigue!");
				p.addEffect(c);
			}
		}
	}

	static class Safeguard extends PokemonEffect implements DefogRelease, StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Safeguard() {
			super(EffectNamesies.SAFEGUARD, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " is covered by a veil!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of " + victim.getName() + "'s Safeguard faded.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return "Safeguard protects " + victim.getName() + " from status conditions!";
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add("The effects of " + victim.getName() + "'s Safeguard faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim).remove(this);
		}
	}

	static class GuardSpecial extends PokemonEffect implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		GuardSpecial() {
			super(EffectNamesies.GUARD_SPECIAL, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " is covered by a veil!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of " + victim.getName() + "'s Guard Special faded.";
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return "Guard Special protects " + victim.getName() + " from status conditions!";
		}
	}

	static class Encore extends PokemonEffect implements ForceMoveEffect, EndTurnEffect, AttackSelectionSelfBlockerEffect {
		private static final long serialVersionUID = 1L;
		private Move move;

		Encore() {
			super(EffectNamesies.ENCORE, 3, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.getAttributes().getLastMoveUsed() == null || victim.getAttributes().getLastMoveUsed().getPP() == 0 || victim.getAttributes().getLastMoveUsed().getAttack().isMoveType(MoveType.ENCORELESS) || victim.hasEffect(this.namesies));
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (move.getPP() == 0) active = false; // If the move runs out of PP, Encore immediately ends
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			move = victim.getAttributes().getLastMoveUsed();
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " got an encore!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of " + victim.getName() + "'s encore faded.";
		}

		public Move getForcedMove() {
			return move;
		}

		public boolean usable(Battle b, ActivePokemon p, Move m) {
			return move.getAttack().namesies() == m.getAttack().namesies();
		}

		public String getUnusableMessage(Battle b, ActivePokemon p) {
			return "Only " + move.getAttack().getName() + " can be used right now!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being encored!";
			}
			
			return super.getFailMessage(b, user, victim);
		}
	}

	static class Disable extends PokemonEffect implements AttackSelectionSelfBlockerEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move disabled;
		private int turns;

		Disable() {
			super(EffectNamesies.DISABLE, -1, -1, false);
			this.turns = RandomUtils.getRandomInt(4, 7); // Between 4 and 7 turns
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.getAttributes().getLastMoveUsed() == null || victim.getAttributes().getLastMoveUsed().getPP() == 0 || victim.hasEffect(this.namesies));
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// TODO: What is happening here?
			turns--;
			return true;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			disabled = victim.getAttributes().getLastMoveUsed();
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + "'s " + disabled.getAttack().getName() + " was disabled!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + disabled.getAttack().getName() + " is no longer disabled!";
		}

		public boolean usable(Battle b, ActivePokemon p, Move m) {
			return disabled.getAttack().namesies() != m.getAttack().namesies();
		}

		public String getUnusableMessage(Battle b, ActivePokemon p) {
			return disabled.getAttack().getName() + " is disabled!";
		}

		public boolean shouldSubside(Battle b, ActivePokemon victim) {
			return turns == 0;
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasEffect(this.namesies)) {
				return victim.getName() + " is already disabled!";
			}
			else if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being disabled!";
			}
			
			return super.getFailMessage(b, user, victim);
		}
	}

	static class RaiseCrits extends PokemonEffect implements CritStageEffect, PassableEffect, MessageGetter {
		private static final long serialVersionUID = 1L;
		private boolean focusEnergy;
		private boolean direHit;
		private boolean berrylicious;

		RaiseCrits() {
			super(EffectNamesies.RAISE_CRITS, -1, -1, false);
			this.focusEnergy = false;
			this.direHit = false;
			this.berrylicious = false;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(source == CastSource.USE_ITEM && victim.hasEffect(this.namesies) && ((RaiseCrits)victim.getEffect(this.namesies)).direHit);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			// Doesn't 'fail' if they already have the effect -- just display the message again
			else if (printCast) {
				Messages.add(getCastMessage(b, caster, victim, source));
			}
			
			RaiseCrits critsies = (RaiseCrits)victim.getEffect(this.namesies);
			switch (source) {
				case ATTACK:
					critsies.focusEnergy = true;
					break;
				case USE_ITEM:
					critsies.direHit = true;
					break;
				case HELD_ITEM:
					critsies.berrylicious = true;
					break;
				default:
					Global.error("Unknown source for RaiseCrits effect.");
				}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return this.getMessage(b, victim, source);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			int critStage = 0;
			
			// TODO: Should probably make an enum or something because this is stupid
			if (focusEnergy) {
				critStage++;
			}
			
			if (direHit) {
				critStage++;
			}
			
			if (berrylicious) {
				critStage++;
			}
			
			if (critStage == 0) {
				Global.error("RaiseCrits effect is not actually raising crits.");
			}
			
			return critStage + stage;
		}

		public String getGenericMessage(ActivePokemon p) {
			return p.getName() + " is getting pumped!";
		}

		public String getSourceMessage(ActivePokemon p, String sourceName) {
			return p.getName() + " is getting pumped due to its " + sourceName + "!";
		}
	}

	static class ChangeItem extends PokemonEffect implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		ChangeItem() {
			super(EffectNamesies.CHANGE_ITEM, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			item = ((ItemHolder)source.getSource(b, caster)).getItem();
			victim.getAttributes().removeEffect(this.namesies);
			super.cast(b, caster, victim, source, printCast);
		}

		public Item getItem() {
			return item;
		}
	}

	static class ChangeType extends PokemonEffect implements ChangeTypeEffect {
		private static final long serialVersionUID = 1L;
		private Type[] type;
		private ChangeTypeSource typeSource;
		private CastSource castSource;
		
		private String castMessage(ActivePokemon victim) {
			String changeType = type[0].getName() + (type[1] == Type.NO_TYPE ? "" : "/" + type[1].getName());
			
			switch (castSource) {
				case ATTACK:
					return victim.getName() + " was changed to " + changeType + " type!!";
				case ABILITY:
					return victim.getName() + "'s " + ((Ability)typeSource).getName() + " changed it to the " + changeType + " type!!";
				case HELD_ITEM:
					return victim.getName() + "'s " + ((Item)typeSource).getName() + " changed it to the " + changeType + " type!!";
				
				default:
					Global.error("Invalid cast source for ChangeType " + castSource);
					return null;
			}
		}

		ChangeType() {
			super(EffectNamesies.CHANGE_TYPE, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			castSource = source;
			typeSource = (ChangeTypeSource)source.getSource(b, caster);
			type = typeSource.getType(b, caster, victim);
			
			// Remove any other ChangeType effects that the victim may have
			while (victim.getAttributes().removeEffect(this.namesies));
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return castMessage(victim);
		}

		public void subside(Battle b, ActivePokemon p) {
			Messages.add(new MessageUpdate().updatePokemon(b, p));
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			return type;
		}
	}

	static class ChangeAbility extends PokemonEffect implements AbilityHolder {
		private static final long serialVersionUID = 1L;
		private Ability ability;
		private String message;

		ChangeAbility() {
			super(EffectNamesies.CHANGE_ABILITY, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Ability oldAbility = victim.getAbility();
			oldAbility.deactivate(b, victim);
			
			ChangeAbilityMove changey = (ChangeAbilityMove)source.getSource(b, caster);
			ability = changey.getAbility(b, caster, victim);
			message = changey.getMessage(b, caster, victim);
			
			// Remove any other ChangeAbility effects that the victim may have
			while (victim.getAttributes().removeEffect(this.namesies));
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return message;
		}

		public Ability getAbility() {
			return ability;
		}
	}

	static class Stockpile extends PokemonEffect implements StageChangingEffect {
		private static final long serialVersionUID = 1L;
		private int turns;

		Stockpile() {
			super(EffectNamesies.STOCKPILE, -1, -1, false);
			this.turns = 0;
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s) {
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE ? turns : 0;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			
			Stockpile stockpile = (Stockpile)victim.getEffect(this.namesies);
			if (stockpile.turns < 3) {
				Messages.add(victim.getName() + " Defense and Special Defense were raised!");
				stockpile.turns++;
				return;
			}
			
			Messages.add(this.getFailMessage(b, caster, victim));
		}

		public void subside(Battle b, ActivePokemon p) {
			Messages.add("The effects of " + p.getName() + "'s Stockpile ended!");
			Messages.add(p.getName() + "'s Defense and Special Defense decreased!");
		}

		public int getTurns() {
			return turns;
		}
	}

	static class UsedDefenseCurl extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		UsedDefenseCurl() {
			super(EffectNamesies.USED_DEFENSE_CURL, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(getCastMessage(b, caster, victim, source));
			}
		}
	}

	static class UsedMinimize extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		UsedMinimize() {
			super(EffectNamesies.USED_MINIMIZE, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(getCastMessage(b, caster, victim, source));
			}
		}
	}

	static class Mimic extends PokemonEffect implements ChangeMoveListEffect {
		private static final long serialVersionUID = 1L;
		private Move mimicMove;

		Mimic() {
			super(EffectNamesies.MIMIC, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			ActivePokemon other = b.getOtherPokemon(victim);
			final Move lastMoveUsed = other.getAttributes().getLastMoveUsed();
			Attack lastAttack = lastMoveUsed == null ? null : lastMoveUsed.getAttack();
			
			if (lastAttack == null || victim.hasMove(b, lastAttack.namesies()) || lastAttack.isMoveType(MoveType.MIMICLESS)) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			mimicMove = new Move(lastAttack);
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " learned " + mimicMove.getAttack().getName() + "!";
		}

		public List<Move> getMoveList(List<Move> actualMoves) {
			List<Move> list = new ArrayList<>();
			for (Move move : actualMoves) {
				if (move.getAttack().namesies() == AttackNamesies.MIMIC) {
					list.add(mimicMove);
				}
				else {
					list.add(move);
				}
			}
			
			return list;
		}
	}

	static class Imprison extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
		private static final long serialVersionUID = 1L;
		private List<AttackNamesies> unableMoves;

		Imprison() {
			super(EffectNamesies.IMPRISON, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			unableMoves = new ArrayList<>();
			for (Move m : caster.getMoves(b)) {
				unableMoves.add(m.getAttack().namesies());
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " sealed " + victim.getName() + "'s moves!";
		}

		public boolean usable(Battle b, ActivePokemon p, Move m) {
			return !unableMoves.contains(m.getAttack().namesies());
		}

		public String getUnusableMessage(Battle b, ActivePokemon p) {
			return "No!! You are imprisoned!!!";
		}
	}

	static class Trapped extends PokemonEffect implements TrappingEffect {
		private static final long serialVersionUID = 1L;

		Trapped() {
			super(EffectNamesies.TRAPPED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled at this time!";
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " can't escape!";
		}
	}

	static class Foresight extends PokemonEffect implements DefendingNoAdvantageChanger {
		private static final long serialVersionUID = 1L;

		Foresight() {
			super(EffectNamesies.FORESIGHT, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(getCastMessage(b, caster, victim, source));
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " identified " + victim.getName() + "!";
		}

		public boolean negateNoAdvantage(Type attacking, Type defending) {
			return defending == Type.GHOST && (attacking == Type.NORMAL || attacking == Type.FIGHTING);
		}
	}

	static class MiracleEye extends PokemonEffect implements DefendingNoAdvantageChanger {
		private static final long serialVersionUID = 1L;

		MiracleEye() {
			super(EffectNamesies.MIRACLE_EYE, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(getCastMessage(b, caster, victim, source));
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " identified " + victim.getName() + "!";
		}

		public boolean negateNoAdvantage(Type attacking, Type defending) {
			return defending == Type.DARK && (attacking == Type.PSYCHIC);
		}
	}

	static class Torment extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
		private static final long serialVersionUID = 1L;

		Torment() {
			super(EffectNamesies.TORMENT, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " tormented " + victim.getName() + "!";
		}

		public boolean usable(Battle b, ActivePokemon p, Move m) {
			Move lastMoveUsed = p.getAttributes().getLastMoveUsed();
			return (lastMoveUsed == null || lastMoveUsed.getAttack().namesies() != m.getAttack().namesies());
		}

		public String getUnusableMessage(Battle b, ActivePokemon p) {
			return p.getName() + " cannot use the same move twice in a row!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents torment!";
			}
			
			return super.getFailMessage(b, user, victim);
		}
	}

	static class SoundBlock extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
		private static final long serialVersionUID = 1L;

		SoundBlock() {
			super(EffectNamesies.SOUND_BLOCK, 3, 3, false);
		}

		public boolean usable(Battle b, ActivePokemon p, Move m) {
			return !m.getAttack().isMoveType(MoveType.SOUND_BASED);
		}

		public String getUnusableMessage(Battle b, ActivePokemon p) {
			return p.getName() + " cannot use sound-based moves!!";
		}
	}

	static class Taunt extends PokemonEffect implements AttackSelectionSelfBlockerEffect {
		private static final long serialVersionUID = 1L;

		Taunt() {
			super(EffectNamesies.TAUNT, 3, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || (victim.hasAbility(AbilityNamesies.OBLIVIOUS) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " fell for the taunt!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of the taunt wore off.";
		}

		public boolean usable(Battle b, ActivePokemon p, Move m) {
			return !m.getAttack().isStatusMove();
		}

		public String getUnusableMessage(Battle b, ActivePokemon p) {
			return "No!! Not while you're under the effects of taunt!!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.OBLIVIOUS) || victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being taunted!";
			}
			
			return super.getFailMessage(b, user, victim);
		}
	}

	static class LaserFocus extends PokemonEffect implements AlwaysCritEffect {
		private static final long serialVersionUID = 1L;

		LaserFocus() {
			super(EffectNamesies.LASER_FOCUS, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " began focusing!";
		}
	}

	static class LockOn extends PokemonEffect implements PassableEffect, AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		LockOn() {
			super(EffectNamesies.LOCK_ON, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			return true;
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " took aim!";
		}
	}

	static class Telekinesis extends PokemonEffect implements LevitationEffect, OpponentAccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Telekinesis() {
			super(EffectNamesies.TELEKINESIS, 4, 4, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.isGrounded(b) || victim.hasEffect(this.namesies));
		}

		public boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Opponent can always strike you unless they are using a OHKO move or you are semi-invulnerable
			return !attacking.getAttack().isMoveType(MoveType.ONE_HIT_KO) && !defending.isSemiInvulnerable();
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " was levitated due to " + user.getName() + "'s telekinesis!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer under the effects of telekinesis.";
		}

		public void fall(Battle b, ActivePokemon fallen) {
			Messages.add("The effects of telekinesis were cancelled!");
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}
	}

	static class Ingrain extends PokemonEffect implements TrappingEffect, EndTurnEffect, GroundedEffect, PassableEffect {
		private static final long serialVersionUID = 1L;

		Ingrain() {
			super(EffectNamesies.INGRAIN, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			int healAmount = victim.healHealthFraction(1/16.0);
			if (victim.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
				victim.heal((int)(healAmount*.3));
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to ingrain!").updatePokemon(b, victim));
		}

		public boolean trapped(Battle b, ActivePokemon escaper) {
			return true;
		}

		public String trappingMessage(ActivePokemon trapped) {
			return trapped.getName() + " cannot be recalled due to ingrain!";
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			removeLevitation(b, victim);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " planted its roots!";
		}
	}

	static class Grounded extends PokemonEffect implements GroundedEffect {
		private static final long serialVersionUID = 1L;

		Grounded() {
			super(EffectNamesies.GROUNDED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			removeLevitation(b, victim);
		}
	}

	static class Curse extends PokemonEffect implements EndTurnEffect, PassableEffect {
		private static final long serialVersionUID = 1L;

		Curse() {
			super(EffectNamesies.CURSE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " was hurt by the curse!");
			victim.reduceHealthFraction(b, 1/4.0);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			caster.reduceHealthFraction(b, 1/2.0);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " cut its own HP and put a curse on " + victim.getName() + "!";
		}
	}

	static class Yawn extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		Yawn() {
			super(EffectNamesies.YAWN, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(!Status.applies(StatusCondition.ASLEEP, b, caster, victim) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " grew drowsy!";
		}

		public void subside(Battle b, ActivePokemon p) {
			Status.giveStatus(b, b.getOtherPokemon(p), p, StatusCondition.ASLEEP);
		}
	}

	static class MagnetRise extends PokemonEffect implements LevitationEffect, PassableEffect {
		private static final long serialVersionUID = 1L;

		MagnetRise() {
			super(EffectNamesies.MAGNET_RISE, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.isGrounded(b) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " levitated with electromagnetism!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " is no longer under the effects of magnet rise.";
		}

		public void fall(Battle b, ActivePokemon fallen) {
			Messages.add("The effects of " + fallen.getName() + "'s magnet rise were cancelled!");
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}
	}

	static class Uproar extends PokemonEffect implements ForceMoveEffect, AttackSelectionEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move uproar;
		
		private static void wakeUp(Battle b, ActivePokemon wakey) {
			if (wakey.hasStatus(StatusCondition.ASLEEP)) {
				wakey.removeStatus();
				Messages.add(new MessageUpdate("The uproar woke up " + wakey.getName() + "!").updatePokemon(b, wakey));
			}
		}

		Uproar() {
			super(EffectNamesies.UPROAR, 3, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			// If uproar runs out of PP, the effect immediately ends
			if (uproar.getPP() == 0) {
				active = false;
			}
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			uproar = victim.getMove();
			super.cast(b, caster, victim, source, printCast);
			b.addEffect(new FieldUproar());
			
			wakeUp(b, victim);
			wakeUp(b, b.getOtherPokemon(victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " started an uproar!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + "'s uproar ended.";
		}

		public Move getForcedMove() {
			return uproar;
		}

		public boolean usable(Battle b, ActivePokemon p, Move m) {
			return m.getAttack().namesies() == AttackNamesies.UPROAR;
		}

		public String getUnusableMessage(Battle b, ActivePokemon p) {
			return "Only Uproar can be used right now!";
		}
	}

	static class AquaRing extends PokemonEffect implements PassableEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		AquaRing() {
			super(EffectNamesies.AQUA_RING, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) return;
			int healAmount = victim.healHealthFraction(1/16.0);
			if (victim.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
				victim.heal((int)(healAmount*.3));
			}
			
			Messages.add(new MessageUpdate(victim.getName() + " restored some HP due to aqua ring!").updatePokemon(b, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " surrounded itself with a veil of water!";
		}
	}

	static class Nightmare extends PokemonEffect implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Nightmare() {
			super(EffectNamesies.NIGHTMARE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(!victim.hasStatus(StatusCondition.ASLEEP) || victim.hasEffect(this.namesies));
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (!victim.hasStatus(StatusCondition.ASLEEP)) {
				this.active = false;
				return;
			}
			
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(victim.getName() + " was hurt by its nightmare!");
			victim.reduceHealthFraction(b, 1/4.0);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " began having a nightmare!";
		}

		public boolean shouldSubside(Battle b, ActivePokemon victim) {
			return !victim.hasStatus(StatusCondition.ASLEEP);
		}
	}

	static class Charge extends PokemonEffect implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Charge() {
			super(EffectNamesies.CHARGE, 2, 2, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.ELECTRIC ? 2 : 1;
		}
	}

	static class Focusing extends PokemonEffect implements DamageTakenEffect {
		private static final long serialVersionUID = 1L;

		Focusing() {
			super(EffectNamesies.FOCUSING, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " began tightening its focus!";
		}

		public void damageTaken(Battle b, ActivePokemon damageTaker) {
			Messages.add(damageTaker.getName() + " lost its focus and couldn't move!");
			damageTaker.getAttributes().removeEffect(this.namesies);
			damageTaker.addEffect((PokemonEffect)EffectNamesies.FLINCH.getEffect());
		}
	}

	static class ShellTrap extends PokemonEffect implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		ShellTrap() {
			super(EffectNamesies.SHELL_TRAP, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " set up a trap!";
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			Messages.add(user.getName() + " set off " + victim.getName() + "'s trap!!");
			victim.getAttributes().removeEffect(this.namesies);
		}
	}

	static class BeakBlast extends PokemonEffect implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		BeakBlast() {
			super(EffectNamesies.BEAK_BLAST, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " started heating up its beak!";
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			Status.giveStatus(b, victim, user, StatusCondition.BURNED);
		}
	}

	static class FiddyPercentStronger extends PokemonEffect implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		FiddyPercentStronger() {
			super(EffectNamesies.FIDDY_PERCENT_STRONGER, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return 1.5;
		}
	}

	static class Transformed extends PokemonEffect implements ChangeMoveListEffect, DifferentStatEffect, ChangeTypeEffect {
		private static final long serialVersionUID = 1L;
		private Move[] moveList; // TODO: Check if I can change this to a list -- not sure about the activate method in particular
		private int[] stats;
		private Type[] type;

		Transformed() {
			super(EffectNamesies.TRANSFORMED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(b.getOtherPokemon(victim).hasEffect(this.namesies) || ((caster.hasAbility(AbilityNamesies.ILLUSION) && caster.getAbility().isActive())) || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			// Pokemon to transform into
			ActivePokemon transformee = b.getOtherPokemon(victim);
			
			// Set the new stats
			stats = new int[Stat.NUM_STATS];
			for (int i = 0; i < stats.length; i++) {
				stats[i] = Stat.getStat(i, victim.getLevel(), transformee.getPokemonInfo().getStat(i), victim.getIV(i), victim.getEV(i), victim.getNature().getNatureVal(i));
			}
			stats[Stat.HP.index()] = victim.getMaxHP();
			
			// Copy the move list
			List<Move> transformeeMoves = transformee.getMoves(b);
			moveList = new Move[transformeeMoves.size()];
			for (int i = 0; i < transformeeMoves.size(); i++) {
				moveList[i] = new Move(transformeeMoves.get(i).getAttack(), 5);
			}
			
			// Copy all stages
			for (Stat stat : Stat.BATTLE_STATS) {
				victim.getAttributes().setStage(stat, transformee.getStage(stat));
			}
			
			// Copy the type
			type = transformee.getPokemonInfo().getType();
			
			// Castaway
			super.cast(b, caster, victim, source, printCast);
			Messages.add(new MessageUpdate().withNewPokemon(transformee.getPokemonInfo(), transformee.isShiny(), true, victim.isPlayer()));
			Messages.add(new MessageUpdate().updatePokemon(b, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " transformed into " + b.getOtherPokemon(victim).getPokemonInfo().getName() + "!";
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			return type;
		}

		public List<Move> getMoveList(List<Move> actualMoves) {
			return Arrays.asList(moveList);
		}

		public Integer getStat(ActivePokemon user, Stat stat) {
			return stats[stat.index()];
		}
	}

	static class Substitute extends PokemonEffect implements AbsorbDamageEffect, PassableEffect, EffectBlockerEffect {
		private static final long serialVersionUID = 1L;
		private int hp;

		Substitute() {
			super(EffectNamesies.SUBSTITUTE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.getHPRatio() <= .25 || victim.getMaxHP() <= 3 || victim.hasEffect(this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			hp = victim.reduceHealthFraction(b, .25) + 1;
			super.cast(b, caster, victim, source, printCast);
			
			String imageName = "substitute" + (victim.isPlayer() ? "-back" : "");
			Messages.add(new MessageUpdate().updatePokemon(b, victim).withImageName(imageName, victim.isPlayer()));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " put in a substitute!";
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Self-target and field moves are always successful
			if (user.getAttack().isSelfTarget() || user.getAttack().isMoveType(MoveType.FIELD)) {
				return true;
			}
			
			// Substitute-piercing moves, Sound-based moves, and Pokemon with the Infiltrator ability bypass Substitute
			if (user.getAttack().isMoveType(MoveType.SUBSTITUTE_PIERCING) || user.getAttack().isMoveType(MoveType.SOUND_BASED) || user.hasAbility(AbilityNamesies.INFILTRATOR)) {
				return true;
			}
			
			// Print the failure for status moves
			if (user.getAttack().isStatusMove()) {
				Messages.add(this.getFailMessage(b, user, victim));
			}
			
			return false;
		}

		public boolean absorbDamage(Battle b, ActivePokemon damageTaker, int damageAmount) {
			this.hp -= damageAmount;
			if (this.hp <= 0) {
				Messages.add(new MessageUpdate("The substitute broke!").withNewPokemon(damageTaker.getPokemonInfo(), damageTaker.isShiny(), true, damageTaker.isPlayer()));
				damageTaker.getAttributes().removeEffect(this.namesies());
			}
			else {
				Messages.add("The substitute absorbed the hit!");
			}
			
			// Substitute always blocks damage
			return true;
		}
	}

	static class Mist extends PokemonEffect implements StatProtectingEffect, DefogRelease {
		private static final long serialVersionUID = 1L;

		Mist() {
			super(EffectNamesies.MIST, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " shrouded itself in mist!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The mist faded.";
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return !caster.hasAbility(AbilityNamesies.INFILTRATOR);
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return "The mist prevents stat reductions!";
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.add("The mist faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim).remove(this);
		}
	}

	static class MagicCoat extends PokemonEffect implements TargetSwapperEffect {
		private static final long serialVersionUID = 1L;

		MagicCoat() {
			super(EffectNamesies.MAGIC_COAT, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " shrouded itself with a magic coat!";
		}

		public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
			Attack attack = user.getAttack();
			if (!attack.isSelfTarget() && attack.isStatusMove() && !attack.isMoveType(MoveType.NO_MAGIC_COAT)) {
				Messages.add(opponent.getName() + "'s " + "Magic Coat" + " reflected " + user.getName() + "'s move!");
				return true;
			}
			
			return false;
		}
	}

	static class Bide extends PokemonEffect implements ForceMoveEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;
		private Move move;
		private int turns;
		private int damage;

		Bide() {
			super(EffectNamesies.BIDE, -1, -1, false);
			this.turns = 1;
			this.damage = 0;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			damage += victim.getAttributes().getDamageTaken();
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Bide bidesies = (Bide)victim.getEffect(this.namesies);
			
			// If the victim is not already under the effects of Bide, cast it upon them
			if (bidesies == null) {
				move = caster.getMove();
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			// Already has the effect, but not ready for it to end yet -- store dat energy
			if (bidesies.turns > 0) {
				bidesies.turns--;
				Messages.add(getCastMessage(b, caster, victim, source));
				return;
			}
			
			// TIME'S UP -- RELEASE DAT STORED ENERGY
			Messages.add(victim.getName() + " released energy!");
			if (bidesies.damage == 0) {
				// Sucks to suck
				Messages.add(this.getFailMessage(b, caster, victim));
			}
			else {
				// RETALIATION STATION
				b.getOtherPokemon(victim).reduceHealth(b, 2*bidesies.damage);
			}
			
			// Bye Bye Bidesies
			victim.getAttributes().removeEffect(this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " is storing energy!";
		}

		public Move getForcedMove() {
			return move;
		}

		public int getTurns() {
			return turns;
		}
	}

	static class HalfWeight extends PokemonEffect implements HalfWeightEffect {
		private static final long serialVersionUID = 1L;
		private int layers;

		HalfWeight() {
			super(EffectNamesies.HALF_WEIGHT, -1, -1, false);
			this.layers = 1;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			HalfWeight halfWeight = (HalfWeight)victim.getEffect(this.namesies);
			if (halfWeight == null) super.cast(b, caster, victim, source, printCast);
			else halfWeight.layers++;
		}

		public int getHalfAmount(int halfAmount) {
			return halfAmount + layers;
		}
	}

	static class PowerTrick extends PokemonEffect implements PassableEffect, StatSwitchingEffect {
		private static final long serialVersionUID = 1L;

		PowerTrick() {
			super(EffectNamesies.POWER_TRICK, -1, -1, false);
		}

		public Stat switchStat(Stat s) {
			if (s == Stat.ATTACK) return Stat.DEFENSE;
			if (s == Stat.DEFENSE) return Stat.ATTACK;
			return s;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			PokemonEffect thaPowah = victim.getEffect(this.namesies);
			if (thaPowah == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			Messages.add(getCastMessage(b, caster, victim, source));
			victim.getAttributes().removeEffect(this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + "'s attack and defense were swapped!";
		}
	}

	static class PowerSplit extends PokemonEffect implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerSplit() {
			super(EffectNamesies.POWER_SPLIT, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " split the power!";
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			
			// If the stat is a splitting stat, return the average between the user and the opponent
			if (s == Stat.ATTACK || s == Stat.SP_ATTACK) {
				return (p.getStat(b, s) + opp.getStat(b, s))/2;
			}
			
			return stat;
		}
	}

	static class GuardSplit extends PokemonEffect implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		GuardSplit() {
			super(EffectNamesies.GUARD_SPLIT, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " split the defense!";
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			
			// If the stat is a splitting stat, return the average between the user and the opponent
			if (s == Stat.DEFENSE || s == Stat.SP_DEFENSE) {
				return (p.getStat(b, s) + opp.getStat(b, s))/2;
			}
			
			return stat;
		}
	}

	static class HealBlock extends PokemonEffect implements SelfAttackBlocker {
		private static final long serialVersionUID = 1L;

		HealBlock() {
			super(EffectNamesies.HEAL_BLOCK, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " blocked " + victim.getName() + " from healing!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of heal block wore off.";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.AROMA_VEIL)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents heal block!";
			}
			
			return super.getFailMessage(b, user, victim);
		}

		public boolean block(Battle b, ActivePokemon user) {
			// TODO: Test
			return user.getAttack() instanceof SapHealthEffect;
		}
	}

	static class Infatuated extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Infatuated() {
			super(EffectNamesies.INFATUATED, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.OBLIVIOUS) && !caster.breaksTheMold()) || (victim.hasAbility(AbilityNamesies.AROMA_VEIL) && !caster.breaksTheMold()) || !Gender.oppositeGenders(caster, victim) || victim.hasEffect(this.namesies));
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			Messages.add(p.getName() + " is in love with " + opp.getName() + "!");
			if (RandomUtils.chanceTest(50)) {
				return true;
			}
			
			Messages.add(p.getName() + "'s infatuation kept it from attacking!");
			return false;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (victim.isHoldingItem(b, ItemNamesies.DESTINY_KNOT) && this.applies(b, victim, caster, CastSource.HELD_ITEM)) {
				super.cast(b, victim, caster, CastSource.HELD_ITEM, false);
				Messages.add(victim.getName() + "'s " + ItemNamesies.DESTINY_KNOT.getName() + " caused " + caster.getName() + " to fall in love!");
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " fell in love!";
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (Gender.oppositeGenders(user, victim) && (victim.hasAbility(AbilityNamesies.OBLIVIOUS) || victim.hasAbility(AbilityNamesies.AROMA_VEIL))) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " prevents infatuation!";
			}
			
			return super.getFailMessage(b, user, victim);
		}
	}

	static class Snatch extends PokemonEffect implements TargetSwapperEffect {
		private static final long serialVersionUID = 1L;

		Snatch() {
			super(EffectNamesies.SNATCH, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
			Attack attack = user.getAttack();
			if (attack.isSelfTarget() && attack.isStatusMove() && !attack.isMoveType(MoveType.NON_SNATCHABLE)) {
				Messages.add(opponent.getName() + " snatched " + user.getName() + "'s move!");
				return true;
			}
			
			return false;
		}
	}

	static class Grudge extends PokemonEffect implements StatusReceivedEffect {
		private static final long serialVersionUID = 1L;

		Grudge() {
			super(EffectNamesies.GRUDGE, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " wants " + b.getOtherPokemon(victim).getName() + " to bear a grudge!";
		}

		private void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			Messages.add(murderer.getName() + "'s " + murderer.getAttack().getName() + " lost all its PP due to " + dead.getName() + "'s grudge!");
			murderer.getMove().reducePP(murderer.getMove().getPP());
		}

		public void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
			if (statusType == StatusCondition.FAINTED) {
				ActivePokemon murderer = b.getOtherPokemon(victim);
				
				// Only grant death wish if murdered through direct damage
				if (murderer.getAttributes().isAttacking()) {
					// DEATH WISH GRANTED
					deathWish(b, victim, murderer);
				}
			}
		}
	}

	static class DestinyBond extends PokemonEffect implements BeforeTurnEffect, StatusReceivedEffect {
		private static final long serialVersionUID = 1L;

		DestinyBond() {
			super(EffectNamesies.DESTINY_BOND, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			p.removeEffect(this);
			return true;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!RandomUtils.chanceTest((int)(100*caster.getAttributes().getSuccessionDecayRate()))) {
				Messages.add(this.getFailMessage(b, caster, victim));
				return;
			}
			
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " is trying to take " + b.getOtherPokemon(victim).getName() + " down with it!";
		}

		private void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			Messages.add(dead.getName() + " took " + murderer.getName() + " down with it!");
			murderer.killKillKillMurderMurderMurder(b);
		}

		public void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
			if (statusType == StatusCondition.FAINTED) {
				ActivePokemon murderer = b.getOtherPokemon(victim);
				
				// Only grant death wish if murdered through direct damage
				if (murderer.getAttributes().isAttacking()) {
					// DEATH WISH GRANTED
					deathWish(b, victim, murderer);
				}
			}
		}
	}

	static class PerishSong extends PokemonEffect implements PassableEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		PerishSong() {
			super(EffectNamesies.PERISH_SONG, 3, 3, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !((victim.hasAbility(AbilityNamesies.SOUNDPROOF) && !caster.breaksTheMold()) || victim.hasEffect(this.namesies));
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			Messages.add(victim.getName() + "'s Perish Song count fell to " + (super.numTurns - 1) + "!");
			if (super.numTurns == 1) {
				victim.killKillKillMurderMurderMurder(b);
			}
		}

		public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasAbility(AbilityNamesies.SOUNDPROOF)) {
				return victim.getName() + "'s " + victim.getAbility().getName() + " makes it immune to sound based moves!";
			}
			
			return super.getFailMessage(b, user, victim);
		}
	}

	static class Embargo extends PokemonEffect implements PassableEffect {
		private static final long serialVersionUID = 1L;

		Embargo() {
			super(EffectNamesies.EMBARGO, 5, 5, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return victim.getName() + " can't use items now!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return victim.getName() + " can use items again!";
		}
	}

	static class ConsumedItem extends PokemonEffect implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item consumed;

		ConsumedItem() {
			super(EffectNamesies.CONSUMED_ITEM, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			consumed = victim.getHeldItem(b);
			victim.removeItem();
			victim.getAttributes().removeEffect(this.namesies);
			super.cast(b, caster, victim, source, printCast);
		}

		public Item getItem() {
			return consumed;
		}
	}

	static class FairyLock extends PokemonEffect implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		FairyLock() {
			super(EffectNamesies.FAIRY_LOCK, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			// TODO: This isn't right
			return true;
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return escaper.getName() + " is trapped by the Fairy Lock!";
		}
	}

	static class Powder extends PokemonEffect implements BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Powder() {
			super(EffectNamesies.POWDER, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Fire-type moves makes the user explode
			if (p.getAttackType() == Type.FIRE) {
				Messages.add("The powder exploded!");
				p.reduceHealthFraction(b, 1/4.0);
				return false;
			}
			
			return true;
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " sprinkled powder on " + victim.getName() + "!";
		}
	}

	static class Electrified extends PokemonEffect implements ChangeAttackTypeEffect {
		private static final long serialVersionUID = 1L;

		Electrified() {
			super(EffectNamesies.ELECTRIFIED, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
			return user.getName() + " electrified " + victim.getName() + "!";
		}

		public Type changeAttackType(Attack attack, Type original) {
			return Type.ELECTRIC;
		}
	}

	// TODO: Why does this need the UsedProof field?
	static class EatenBerry extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		EatenBerry() {
			super(EffectNamesies.EATEN_BERRY, -1, -1, false);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			if (!victim.hasEffect(this.namesies)) {
				super.cast(b, caster, victim, source, printCast);
			}
			else {
				Messages.add(getCastMessage(b, caster, victim, source));
			}
		}
	}

	static class BreaksTheMold extends PokemonEffect {
		private static final long serialVersionUID = 1L;

		BreaksTheMold() {
			super(EffectNamesies.BREAKS_THE_MOLD, 1, 1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}
	}

	static class Raging extends PokemonEffect implements TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		Raging() {
			super(EffectNamesies.RAGING, -1, -1, false);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(victim.hasEffect(this.namesies));
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			Move lastMoveUsed = victim.getAttributes().getLastMoveUsed();
			if (lastMoveUsed == null || lastMoveUsed.getAttack().namesies() != AttackNamesies.RAGE) {
				victim.removeEffect(this);
				return;
			}
			
			victim.getAttributes().modifyStage(
				victim, victim, 1, Stat.ATTACK, b, CastSource.EFFECT,
				victim.getName() + "'s Rage increased its attack!"
			);
		}
	}
}
