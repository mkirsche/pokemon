package map.overworld;

public enum OverworldTool {
    FLY("canFly"),
    SURF("canSurf"),
    FISH("canFish");

    private final String globalName;

    OverworldTool(String globalName) {
        this.globalName = globalName;
    }

    public String getGlobalName() {
        return this.globalName;
    }
}
