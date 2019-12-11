import java.awt.event.*;
import javax.swing.*;

public class GameAction extends AbstractAction
{   
	/**
	 * GameAction.class
	 * Connects to the game tab in the GUI, determining which function has been called.
	 */
	private static final long serialVersionUID = 1L;
	private Logic logic;
	String request;
	
    public GameAction(Logic logic, String name, String tip)
    {   
    	super(name);
        this.logic = logic;
        putValue(Action.SHORT_DESCRIPTION, tip);
    }

    public void actionPerformed(ActionEvent event)
    {   
    	request = (String) this.getValue(Action.NAME);
		
		// Print request
		System.out.println("GameAction: "+request);
		
		// Determine action
		if(request.equals("Player vs Player"))
		{
			logic.bot[0] = false;
			logic.bot[1] = false;
			logic.newGame(AI.BOTPLY);
		}
		
		if(request.equals("Rookie"))
		{
			logic.bot[1] = true;
			logic.newGame(AI.ROOKIE);
		}
		
		if(request.equals("Novice"))
		{
			logic.bot[1] = true;
			logic.newGame(AI.NOVICE);
		}
		
		if(request.equals("Intermediate"))
		{
			logic.bot[1] = true;
			logic.newGame(AI.INTERMEDIATE);
		}
		
		if(request.equals("Professional"))
		{
			logic.bot[1] = true;
			logic.newGame(AI.PROFESSIONAL);
		}
		
		if(request.equals("Expert"))
		{
			logic.bot[1] = true;
			logic.newGame(AI.EXPERT);
		}
		
		if(request.equals("Godlike"))
		{
			logic.bot[1] = true;
			logic.newGame(AI.GODLIKE);
		}
    }
}