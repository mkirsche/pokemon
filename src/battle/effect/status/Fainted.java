package battle.effect.status;

import pokemon.ActivePokemon;
import util.StringUtils;

class Fainted extends Status {
    private static final long serialVersionUID = 1L;

    public Fainted() {
        super(StatusCondition.FAINTED);
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " fainted!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " caused " + victim.getName() + " to faint!";
    }

    public String getRemoveMessage(ActivePokemon victim) {
        return StringUtils.empty();
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return StringUtils.empty();
    }
}