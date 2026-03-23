package ca.dauqui.spacecolonygame.gamedata;

public class Colony {
    private String colonyName;
    private String commanderName;
    private String aiName;
    private boolean saved;

    public Colony(){
        saved = false;
    }




    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setCommanderName(String commanderName) {
        this.commanderName = commanderName;
    }

    public void setAIName(String aiName) {
        this.aiName = aiName;
    }
}