package it.polimi.ingsw.model.effect;

public class ChooseDieFromDraftPoolEffect extends Effect {
    @Override
    public void perform(Object... args) {
        toolCard.chooseDieFromDraftPool();
    }
}
