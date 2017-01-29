package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import pokemon.ActivePokemon;
import trainer.Trainer.Action;
import trainer.WildPokemon;

class TestBattle extends Battle {
    private TestBattle(ActivePokemon nahMahBoi) {
        super(new WildPokemon(nahMahBoi));
    }

    public double getDamageModifier(ActivePokemon attacking, ActivePokemon defending) {
        return super.getDamageModifier(attacking, defending);
    }

    boolean ableToAttack(AttackNamesies attack, TestPokemon attacking, ActivePokemon defending) {
        attacking.setupMove(attack, this, defending);
        return super.ableToAttack(attacking, defending);
    }


    void fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        getPlayer().setAction(Action.FIGHT);

        ((TestPokemon)getPlayer().front()).setupMove(attackingMove, this, getOpponent().front());
        ((TestPokemon)getOpponent().front()).setupMove(defendingMove, this, getPlayer().front());
        super.fight(getPlayer().front(), getOpponent().front());
    }

    void attackingFight(AttackNamesies attackNamesies) {
        fight(attackNamesies, AttackNamesies.SPLASH);
    }

    void defendingFight(AttackNamesies attackNamesies) {
        fight(AttackNamesies.SPLASH, attackNamesies);
    }

    static TestBattle create(TestPokemon mahBoiiiiiii, TestPokemon nahMahBoi) {
        new TestCharacter(mahBoiiiiiii);
        TestBattle testBattle = new TestBattle(nahMahBoi);

        mahBoiiiiiii.setupMove(AttackNamesies.SPLASH, testBattle, nahMahBoi);
        nahMahBoi.setupMove(AttackNamesies.SPLASH, testBattle, mahBoiiiiiii);

        return testBattle;
    }
}
