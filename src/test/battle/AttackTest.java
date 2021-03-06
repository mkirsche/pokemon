package test.battle;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.generic.EffectNamesies;
import battle.effect.status.StatusCondition;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import test.GeneralTest;
import test.TestPokemon;
import trainer.Team;
import type.Type;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class AttackTest {
    @Test
    public void testPhysicalContact() {
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            Attack attack = attackNamesies.getAttack();
            Assert.assertFalse("Status moves cannot have physical contact. Move: " + attack.getName(),
                    attack.isStatusMove() && attack.isMoveType(MoveType.PHYSICAL_CONTACT));
        }
    }

    @Test
    public void recoilTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.ROCK_HEAD);
        TestPokemon defending = battle.getDefending();

        battle.attackingFight(AttackNamesies.TAKE_DOWN);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertFalse(defending.fullHealth());

        defending.fullyHeal();
        Assert.assertTrue(defending.fullHealth());

        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        Assert.assertFalse(attacking.fullHealth());
        Assert.assertFalse(defending.fullHealth());

        int damage = attacking.getMaxHP() - attacking.getHP();
        Assert.assertTrue(defending.getMaxHP() - defending.getHP() == (int)(Math.ceil(damage/3.0)));

        attacking.fullyHeal();
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.MAGIC_GUARD);
        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        Assert.assertTrue(defending.fullHealth());

        // Struggle should still cause recoil damage even if they have Rock Head/Magic Guard
        attacking.fullyHeal();
        battle.attackingFight(AttackNamesies.STRUGGLE);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.ROCK_HEAD));
        Assert.assertFalse(attacking.fullHealth());
    }

    @Test
    public void captivateTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withGender(Gender.MALE);
        TestPokemon defending = battle.getDefending().withGender(Gender.MALE);

        // TODO: Test genderless too
        attacking.apply(false, AttackNamesies.CAPTIVATE, battle);

        defending.withGender(Gender.FEMALE);
        attacking.apply(true, AttackNamesies.CAPTIVATE, battle);

        attacking.withAbility(AbilityNamesies.OBLIVIOUS);
        attacking.apply(true, AttackNamesies.CAPTIVATE, battle);

        attacking.withAbility(AbilityNamesies.NO_ABILITY);
        defending.withAbility(AbilityNamesies.OBLIVIOUS);
        attacking.apply(false, AttackNamesies.CAPTIVATE, battle);
    }

    @Test
    public void ohkoTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.MAGIKARP, PokemonNamesies.DRAGONITE);
        TestPokemon defending = battle.getDefending();

        // Ground type should not effect
        battle.attackingFight(AttackNamesies.FISSURE);
        Assert.assertTrue(defending.fullHealth());

        // OHKO,MF
        battle.attackingFight(AttackNamesies.HORN_DRILL);
        Assert.assertTrue(defending.isFainted(battle));

        // Sturdy prevents OHKO
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.STURDY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.fullHealth());

        defending.withAbility(AbilityNamesies.NO_ABILITY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.isFainted(battle));

        defending = new TestPokemon(PokemonNamesies.GLACEON);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.fullHealth());
    }

    @Test
    public void selfSwitchingMoves() {
        TestPokemon attacking1 = new TestPokemon(PokemonNamesies.CHANSEY);
        TestPokemon attacking2 = new TestPokemon(PokemonNamesies.HAPPINY);
        TestPokemon defending = new TestPokemon(PokemonNamesies.SHUCKLE);

        TestBattle battle = TestBattle.createTrainerBattle(attacking1, defending);
        battle.getPlayer().addPokemon(attacking2);

        Assert.assertTrue(battle.getPlayer().front() == attacking1);

        // Use U-Turn -- make sure they swap
        battle.attackingFight(AttackNamesies.U_TURN);
        Assert.assertTrue(battle.getPlayer().front() == attacking2);

        // TODO: Baton Pass

        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void swapOpponentMoves() {
        TestPokemon attacking1 = new TestPokemon(PokemonNamesies.STEELIX);
        TestPokemon attacking2 = new TestPokemon(PokemonNamesies.REGIROCK);
        TestPokemon defending = new TestPokemon(PokemonNamesies.SHUCKLE);

        TestBattle battle = TestBattle.createTrainerBattle(attacking1, defending);
        battle.getPlayer().addPokemon(attacking2);

        Assert.assertTrue(battle.getAttacking() == attacking1);

        // Use Dragon Tail -- make sure they swap
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.DRAGON_TAIL);
        Assert.assertTrue(battle.getAttacking() == attacking2);

        // Don't swap with Suction Cups
        attacking2.withAbility(AbilityNamesies.SUCTION_CUPS);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.CIRCLE_THROW);
        Assert.assertTrue(battle.getAttacking() == attacking2);

        attacking2.withAbility(AbilityNamesies.NO_ABILITY);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.ROAR);
        Assert.assertTrue(battle.getAttacking() == attacking1);

        // Don't swap when ingrained
        battle.fight(AttackNamesies.INGRAIN, AttackNamesies.WHIRLWIND);
        Assert.assertTrue(battle.getAttacking() == attacking1);

        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void curseTest() {
        // TODO: Protean
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Non-Ghost type curse -- apply stat changes
        battle.attackingFight(AttackNamesies.CURSE);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.ATTACK) == 1);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.DEFENSE) == 1);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.SPEED) == -1);
        Assert.assertFalse(attacking.hasEffect(EffectNamesies.CURSE));
        Assert.assertFalse(defending.hasEffect(EffectNamesies.CURSE));

        // Add Ghost Type
        battle.defendingFight(AttackNamesies.TRICK_OR_TREAT);
        Assert.assertTrue(attacking.isType(battle, Type.GHOST));
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertTrue(defending.fullHealth());

        // Make sure stat changes remain the same and target gets curse effect
        battle.attackingFight(AttackNamesies.CURSE);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.ATTACK) == 1);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.DEFENSE) == 1);
        Assert.assertTrue(attacking.getAttributes().getStage(Stat.SPEED) == -1);
        Assert.assertFalse(attacking.hasEffect(EffectNamesies.CURSE));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.CURSE));
        Assert.assertTrue(GeneralTest.healthRatioMatch(attacking, .5));
        Assert.assertTrue(GeneralTest.healthRatioMatch(defending, .75));
    }

    // Used for attacks that have a random element to them -- like Tri-Attack and Acupressure -- required running several times
    @Test
    public void randomEffectsTest() {
        boolean triAttackAlwaysSame = true;
        Map<StatusCondition, Boolean> triAttackStatusMap = new EnumMap<>(StatusCondition.class);
        triAttackStatusMap.put(StatusCondition.NO_STATUS, false);
        triAttackStatusMap.put(StatusCondition.PARALYZED, false);
        triAttackStatusMap.put(StatusCondition.BURNED, false);
        triAttackStatusMap.put(StatusCondition.FROZEN, false);

        boolean acupressureAlwaysSame = true;
        boolean[] acupressureStats = new boolean[Stat.NUM_BATTLE_STATS];

        for (int i = 0; i < 1000; i++) {
            TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
            TestPokemon attacking = battle.getAttacking();
            TestPokemon defending = battle.getDefending();

            // Tri-Attack
            battle.fight(AttackNamesies.TRI_ATTACK, AttackNamesies.TRI_ATTACK);

            StatusCondition attackingCondition = attacking.getStatus().getType();
            StatusCondition defendingCondition = defending.getStatus().getType();

            Assert.assertTrue(triAttackStatusMap.containsKey(attackingCondition));
            Assert.assertTrue(triAttackStatusMap.containsKey(defendingCondition));

            triAttackStatusMap.put(attackingCondition, true);
            triAttackStatusMap.put(defendingCondition, true);

            if (attackingCondition != defendingCondition) {
                triAttackAlwaysSame = false;
            }

            battle.emptyHeal();

            // Acupressure
            battle.fight(AttackNamesies.ACUPRESSURE, AttackNamesies.ACUPRESSURE);

            boolean foundAttacking = false;
            boolean foundDefending = false;
            for (Stat stat : Stat.BATTLE_STATS) {
                int attackingStage = attacking.getStage(stat);
                int defendingStage = defending.getStage(stat);
                if (attackingStage != defendingStage) {
                    acupressureAlwaysSame = false;
                }

                int index = stat.index();
                if (attackingStage > 0) {
                    Assert.assertFalse(foundAttacking);
                    acupressureStats[index] = true;
                    foundAttacking = true;
                }

                if (defendingStage > 0) {
                    Assert.assertFalse(foundDefending);
                    acupressureStats[index] = true;
                    foundDefending = true;
                }
            }
        }

        Assert.assertFalse(triAttackAlwaysSame);
        Assert.assertTrue(triAttackStatusMap.get(StatusCondition.NO_STATUS));
        Assert.assertTrue(triAttackStatusMap.get(StatusCondition.PARALYZED));
        Assert.assertTrue(triAttackStatusMap.get(StatusCondition.BURNED));
        Assert.assertTrue(triAttackStatusMap.get(StatusCondition.FROZEN));

        Assert.assertFalse(acupressureAlwaysSame);
        for (int j = 0; j < acupressureStats.length; j++) {
            Assert.assertTrue(Stat.getStat(j, true).getName(), acupressureStats[j]);
        }
    }

    @Test
    public void roostTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.DRAGONITE, PokemonNamesies.MAGIKARP);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Should fail with full hp
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));
        attacking.apply(false, AttackNamesies.ROOST, battle);
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));

        // Reduce health and apply again
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        Assert.assertFalse(attacking.fullHealth());
        attacking.apply(true, AttackNamesies.ROOST, battle);
        Assert.assertFalse(attacking.isType(battle, Type.FLYING));
        Assert.assertTrue(attacking.fullHealth());

        // Should fail because attack is already maxed -- flying type should come back at the end of the turn
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));

        // Clear stat changes and reduce again
        Assert.assertTrue(attacking.getStage(Stat.ATTACK) == Stat.MAX_STAT_CHANGES);
        battle.attackingFight(AttackNamesies.HAZE);
        Assert.assertTrue(attacking.getStage(Stat.ATTACK) == 0);
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        Assert.assertTrue(attacking.getStage(Stat.ATTACK) == Stat.MAX_STAT_CHANGES);

        // Using a full turn should bring the flying type back at the end
        Assert.assertFalse(attacking.fullHealth());
        battle.attackingFight(AttackNamesies.ROOST);
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));
        Assert.assertTrue(attacking.fullHealth());

        defending.apply(false, AttackNamesies.MUD_SLAP, battle);
        defending.apply(true, AttackNamesies.TACKLE, battle);
        Assert.assertFalse(attacking.fullHealth());
        attacking.apply(true, AttackNamesies.ROOST, battle);
        Assert.assertTrue(attacking.fullHealth());
        defending.apply(true, AttackNamesies.MUD_SLAP, battle);
        Assert.assertFalse(attacking.fullHealth());
    }

    @Test
    public void lastResortTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withMoves(AttackNamesies.LAST_RESORT);
        battle.getDefending().withMoves(AttackNamesies.SPLASH, AttackNamesies.ENDURE);

        // Should work if it is the only move the pokemon knows
        attacking.apply(true, AttackNamesies.LAST_RESORT, battle);

        // Should fail if multiple moves and hasn't used all of them yet
        battle.emptyHeal();
        attacking.withMoves(AttackNamesies.TACKLE, AttackNamesies.LAST_RESORT);
        attacking.apply(false, AttackNamesies.LAST_RESORT, battle);

        // Use the other move and then it should work
        Move tackle = attacking.getMoves(battle).get(0);
        attacking.setMove(tackle);
        Assert.assertFalse(tackle.used());
        battle.fight();
        Assert.assertTrue(tackle.used());
        attacking.apply(true, AttackNamesies.LAST_RESORT, battle);
    }

    @Test
    public void psychoShift() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.MAGIC_GUARD);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.MAGIC_GUARD);

        battle.fight(AttackNamesies.SOAK, AttackNamesies.SOAK);
        Assert.assertTrue(attacking.isType(battle, Type.WATER));
        Assert.assertTrue(defending.isType(battle, Type.WATER));

        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusCondition.BURNED));
        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BURNED));
        Assert.assertFalse(defending.hasStatus());

        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED));
        Assert.assertTrue(defending.hasStatus(StatusCondition.BADLY_POISONED));

        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BURNED));
        Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED));

        battle.attackingFight(AttackNamesies.REFRESH);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusCondition.BADLY_POISONED));

        attacking.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.POISONED));
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BADLY_POISONED));
        Assert.assertFalse(defending.hasStatus());

        attacking.withAbility(AbilityNamesies.PROTEAN);
        Assert.assertTrue(attacking.isType(battle, Type.WATER));
        battle.attackingFight(AttackNamesies.CLEAR_SMOG);
        Assert.assertTrue(attacking.isType(battle, Type.POISON));
        Assert.assertTrue(attacking.hasStatus(StatusCondition.POISONED));
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BADLY_POISONED));
        battle.attackingFight(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED));
        Assert.assertTrue(defending.hasStatus(StatusCondition.BADLY_POISONED));
        Assert.assertTrue(attacking.isType(battle, Type.PSYCHIC));
        battle.attackingFight(AttackNamesies.ACID_ARMOR);
        Assert.assertTrue(attacking.isType(battle, Type.POISON));
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);

        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.PROTEAN));
        battle.attackingFight(AttackNamesies.DRAGON_DANCE);
        Assert.assertTrue(Arrays.toString(attacking.getType(battle)), attacking.isType(battle, Type.DRAGON));
        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.POISONED));

        defending.withAbility(AbilityNamesies.IMMUNITY);
        defending.apply(false, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.POISONED));
        Assert.assertFalse(defending.hasStatus());

        battle.emptyHeal();
        attacking.withMoves(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
        battle.defendingFight(AttackNamesies.SPORE);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));
        Assert.assertFalse(defending.hasStatus());
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus(StatusCondition.ASLEEP));

        defending.apply(true, AttackNamesies.PSYCHO_SHIFT, battle);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));
        Assert.assertFalse(defending.hasStatus());

        defending.withAbility(AbilityNamesies.INSOMNIA);
        battle.attackingFight(AttackNamesies.SLEEP_TALK);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));
        Assert.assertFalse(defending.hasStatus());
    }

    @Test
    public void powderMoveTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Powder moves shouldn't work on Grass-type Pokemon
        Assert.assertTrue(attacking.isType(battle, Type.GRASS));
        defending.apply(false, AttackNamesies.SLEEP_POWDER, battle);
        attacking.apply(true, AttackNamesies.SLEEP_POWDER, battle);

        battle.emptyHeal();
        battle.defendingFight(AttackNamesies.SOAK);
        Assert.assertFalse(attacking.isType(battle, Type.GRASS));
        defending.apply(true, AttackNamesies.SLEEP_POWDER, battle);

        // Or Pokemon with Overcoat
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.OVERCOAT);
        attacking.apply(false, AttackNamesies.SLEEP_POWDER, battle);
        attacking.apply(false, AttackNamesies.COTTON_SPORE, battle);
        attacking.apply(true, AttackNamesies.LEECH_SEED, battle);

        // Or Pokemon holding Safety Goggles
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.NO_ABILITY);
        defending.giveItem(ItemNamesies.SAFETY_GOGGLES);
        attacking.apply(false, AttackNamesies.SPORE, battle);
        attacking.apply(false, AttackNamesies.POISON_POWDER, battle);
        attacking.apply(true, AttackNamesies.VINE_WHIP, battle);
    }

    @Test
    public void bugBiteTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.DRAGONITE, PokemonNamesies.DRAGONITE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        attacking.giveItem(ItemNamesies.RAWST_BERRY);
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertTrue(attacking.isHoldingItem(battle));
        Assert.assertTrue(defending.hasStatus(StatusCondition.BURNED));

        battle.fight(AttackNamesies.ENDURE, AttackNamesies.BUG_BITE);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(attacking.hasEffect(EffectNamesies.EATEN_BERRY));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.EATEN_BERRY));
        Assert.assertTrue(attacking.hasEffect(EffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(defending.hasEffect(EffectNamesies.CONSUMED_ITEM));

        battle.attackingFight(AttackNamesies.RECYCLE);
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.RAWST_BERRY));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(attacking.hasEffect(EffectNamesies.EATEN_BERRY));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.EATEN_BERRY));
        Assert.assertTrue(attacking.hasEffect(EffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(defending.hasEffect(EffectNamesies.CONSUMED_ITEM));

        battle.defendingFight(AttackNamesies.POISON_POWDER);
        Assert.assertTrue(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertTrue(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());

        battle.attackingFight(AttackNamesies.PSYCHO_SHIFT);
        Assert.assertTrue(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus());

        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertTrue(defending.hasStatus());
        Assert.assertTrue(attacking.hasEffect(EffectNamesies.EATEN_BERRY));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.EATEN_BERRY));
        Assert.assertTrue(attacking.hasEffect(EffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(defending.hasEffect(EffectNamesies.CONSUMED_ITEM));
    }

    @Test
    public void powerChangeTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Acrobatics has double power when not holding an item
        attacking.setupMove(AttackNamesies.ACROBATICS, battle);
        Assert.assertTrue(battle.getDamageModifier(attacking, defending) == 2);
        attacking.giveItem(ItemNamesies.POTION);
        Assert.assertTrue(battle.getDamageModifier(attacking, defending) == 1);

        // Body Slam -- doubles when the opponent uses Minimize
        attacking.setupMove(AttackNamesies.BODY_SLAM, battle);
        Assert.assertTrue(battle.getDamageModifier(attacking, defending) == 1);
        defending.apply(true, AttackNamesies.MINIMIZE, battle);
        Assert.assertTrue(defending.hasEffect(EffectNamesies.USED_MINIMIZE));
        Assert.assertTrue(battle.getDamageModifier(attacking, defending) == 2);
    }

    @Test
    public void evasionRemovalTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.fight(AttackNamesies.DOUBLE_TEAM, AttackNamesies.MINIMIZE);
        Assert.assertTrue(attacking.getStage(Stat.EVASION) == 1);
        Assert.assertTrue(defending.getStage(Stat.EVASION) == 2);

        battle.attackingFight(AttackNamesies.FORESIGHT);
        Assert.assertTrue(attacking.getStage(Stat.EVASION) == 1);
        Assert.assertTrue(defending.getStage(Stat.EVASION) == 0);

        battle.defendingFight(AttackNamesies.MIRACLE_EYE);
        Assert.assertTrue(attacking.getStage(Stat.EVASION) == 0);
        Assert.assertTrue(defending.getStage(Stat.EVASION) == 0);
    }

    @Test
    public void changeAbilityTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.OVERGROW);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.BLAZE);

        battle.attackingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.BLAZE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.OVERGROW));

        battle.attackingFight(AttackNamesies.WORRY_SEED);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.BLAZE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.INSOMNIA));

        battle.defendingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.INSOMNIA));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.BLAZE));

        battle.attackingFight(AttackNamesies.ENTRAINMENT);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.INSOMNIA));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.INSOMNIA));

        battle.attackingFight(AttackNamesies.SIMPLE_BEAM);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.INSOMNIA));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.SIMPLE));

        battle.attackingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.SIMPLE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.INSOMNIA));

        battle.defendingFight(AttackNamesies.ROLE_PLAY);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.SIMPLE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.SIMPLE));

        battle.defendingFight(AttackNamesies.GASTRO_ACID);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.NO_ABILITY));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.SIMPLE));

        battle.defendingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.SIMPLE));
        Assert.assertTrue(defending.hasAbility(AbilityNamesies.NO_ABILITY));
    }

    @Test
    public void rapidSpinTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        Team defendingTeam = battle.getOpponent();
        TestPokemon defending = battle.getDefending();

        battle.attackingFight(AttackNamesies.STEALTH_ROCK);
        battle.attackingFight(AttackNamesies.TOXIC_SPIKES);
        battle.attackingFight(AttackNamesies.SPIKES);
        battle.attackingFight(AttackNamesies.LEECH_SEED);
        battle.attackingFight(AttackNamesies.WRAP);

        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.STEALTH_ROCK));
        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.TOXIC_SPIKES));
        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.SPIKES));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.LEECH_SEED));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.WRAPPED));

        battle.emptyHeal();
        battle.defendingFight(AttackNamesies.CONSTRICT);

        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.STEALTH_ROCK));
        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.TOXIC_SPIKES));
        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.SPIKES));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.LEECH_SEED));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.WRAPPED));

        battle.defendingFight(AttackNamesies.RAPID_SPIN);

        Assert.assertFalse(defendingTeam.hasEffect(EffectNamesies.STEALTH_ROCK));
        Assert.assertFalse(defendingTeam.hasEffect(EffectNamesies.TOXIC_SPIKES));
        Assert.assertFalse(defendingTeam.hasEffect(EffectNamesies.SPIKES));
        Assert.assertFalse(defending.hasEffect(EffectNamesies.LEECH_SEED));
        Assert.assertFalse(defending.hasEffect(EffectNamesies.WRAPPED));

        battle.emptyHeal();

        battle.attackingFight(AttackNamesies.STEALTH_ROCK);
        battle.attackingFight(AttackNamesies.TOXIC_SPIKES);
        battle.attackingFight(AttackNamesies.SPIKES);
        battle.attackingFight(AttackNamesies.LEECH_SEED);
        battle.attackingFight(AttackNamesies.WRAP);

        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.STEALTH_ROCK));
        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.TOXIC_SPIKES));
        Assert.assertTrue(defendingTeam.hasEffect(EffectNamesies.SPIKES));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.LEECH_SEED));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.WRAPPED));
    }

    @Test
    public void restTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();

        battle.defendingFight(AttackNamesies.FALSE_SWIPE);
        battle.defendingFight(AttackNamesies.TOXIC);

        Assert.assertFalse(attacking.fullHealth());
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BADLY_POISONED));

        battle.attackingFight(AttackNamesies.REST);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));

        battle.attackingFight(AttackNamesies.SPLASH);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));

        battle.attackingFight(AttackNamesies.SPLASH);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertTrue(attacking.hasStatus(StatusCondition.ASLEEP));

        battle.attackingFight(AttackNamesies.SPLASH);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertFalse(attacking.hasStatus());
    }
}
