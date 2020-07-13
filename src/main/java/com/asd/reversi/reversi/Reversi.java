package com.asd.reversi.reversi;

import com.asd.reversi.reversi.command.Command;
import com.asd.reversi.reversi.command.HumanMove;
import com.asd.reversi.reversi.factory.StrategyFactory;
import com.asd.reversi.reversi.factory.StrategyImplFactory;
import com.asd.reversi.reversi.model.MoveDetails;
import com.asd.reversi.reversi.model.ReversiBoard;
import com.asd.reversi.reversi.strategy.Strategy;
import com.asd.reversi.reversi.strategy.StrategyImplementation;
import com.asd.reversi.reversi.strategy.StratgyContext;
import com.asd.reversi.reversi.player.Player;
import com.asd.reversi.reversi.util.Helper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Reversi {
    private static ReversiBoard reversiBoard = ReversiBoard.getInstance();

    public ReversiBoard registerPlayer(String username) {
        if (reversiBoard.getPlayerFactory().getPlayers().size() == 0) {
            reversiBoard.getPlayerFactory().createPlayer("human",username,1);
        } else if (reversiBoard.getPlayerFactory().getPlayers().size() == 1) {
            reversiBoard.getPlayerFactory().createPlayer("computer",username,-1);
        }
        return reversiBoard;
    }

    public List<Player> registerPlayerAsResult(String username) {
        if (reversiBoard.getPlayerFactory().getPlayers().size() == 0) {
            reversiBoard.getPlayerFactory().createPlayer("remote",username,-1);
        } else if (reversiBoard.getPlayerFactory().getPlayers().size() == 1) {
            reversiBoard.getPlayerFactory().createPlayer("computer",username,1);
        }
        return reversiBoard.getPlayerFactory().getPlayers();
    }

    public void startGame() {
        if (reversiBoard.isFinished()) {
            reversiBoard.reset();
        }
        reversiBoard.setNext(Helper.calcNextMoves(reversiBoard.getBoard() ,reversiBoard.getTurn()));
    }

    public ReversiBoard move(MoveDetails details) throws Exception {
        if (details.getX() != -5 && details.getY() != -5) { // Error Check Temporarily
            if (!isItPlayersTurn(details)) {
                throw new Exception("It's not your turn");
            }
            if (!isMoveValid(details)) {
                throw new Exception("It's not a valid movement");
            }
            Helper.doMove(reversiBoard.getBoard(), details);
            if (!Helper.isGameFinished(reversiBoard.getBoard())) {
                Helper.setTurn(details);
                reversiBoard.setNext(Helper.calcNextMoves(reversiBoard.getBoard(), reversiBoard.getTurn()));
            } else {
                Helper.checkState(reversiBoard.getBoard());
                reversiBoard.setFinished(true);
                System.out.println("game is over"); // for game  is over state
            }
            if (reversiBoard.getPlayerFactory().getPlayers().get(1).getName().equalsIgnoreCase("computer") && reversiBoard.getTurn() == reversiBoard.getPlayerFactory().getPlayers().get(1).getFlag()) {
                move(Helper.generateComputerMove());
            }
        }
        return reversiBoard;
    }

    public boolean doMove(int[][] board, MoveDetails details) {
        StratgyContext context = new StratgyContext(new StrategyImplementation());
        return context.execute(board, details);
    }


    public void playHumanMove(int[][] board,MoveDetails details){
        StrategyFactory factory= new StrategyImplFactory();
        Strategy strategy=factory.createStrategy() ;
        StratgyContext stratgyContext=new StratgyContext(strategy);
        Command command=new HumanMove(stratgyContext,board,details);
        submit(command);
    }

    private boolean isMoveValid(MoveDetails details) {
        int[][] nextMoves = Helper.calcNextMoves(reversiBoard.getBoard(), details.getPlayer());
        return nextMoves[details.getX()][details.getY()] == 2*details.getPlayer();
    }

    private boolean isItPlayersTurn(MoveDetails details) {
        return reversiBoard.getTurn() == details.getPlayer();
    }

    public MoveDetails generateComputerMove() {
       return Helper.generateComputerMove();
    }

    public void submit(Command command){
       command.execute();
    }


}
