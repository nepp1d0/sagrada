package it.polimi.ingsw.model.effect;

import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Die;

public class MoveDieWithSameColorAsDieFromRoundTrackEffect extends Effect {

    public MoveDieWithSameColorAsDieFromRoundTrackEffect(String name) {
        this.name = name;
    }

    @Override
    public void perform(Object... args) {
        Die die = (Die) args[0];
        int turn = (int) args[1];
        int numberOfDie = (int) args[2];
        Board board = (Board) args[3];
        if (toolCard.getBoard().getRoundTrack().getDieAt(turn, numberOfDie) == null) {
            toolCard.setResponse(false);
        } else {
            if (die.getColor() == board.getRoundTrack().getDieAt(turn, numberOfDie).getColor()) {
                toolCard.processMoveWithoutConstraints(true, true, true, false);
            }
        }
    }
}
