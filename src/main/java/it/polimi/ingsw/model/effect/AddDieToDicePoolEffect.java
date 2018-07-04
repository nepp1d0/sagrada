package it.polimi.ingsw.model.effect;

import it.polimi.ingsw.model.DiceBag;
import it.polimi.ingsw.model.Die;

public class AddDieToDicePoolEffect extends Effect {

    public AddDieToDicePoolEffect(String name){
        this.name = name;
    }

    @Override
    public void perform(Object... args) {
        Die die = (Die) args[0];
        for(Die die2 : toolCard.getBoard().getDraftPool()){
            if (die.getColor()==die2.getColor() & die.getNumber()==die2.getNumber()){
                toolCard.getBoard().getDraftPool().remove(die);
                break;
            }
        }
        toolCard.setResponse(toolCard.addDieToDiceBag(die));
    }
}
