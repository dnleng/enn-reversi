import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;


public class Nexus extends JApplet implements MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;
	Logic logic;
	Boolean currentPlayer;
	int method;
	
	
	public Nexus(int method)
	{
		this.method = method;
		this.init();
	}
	
	public void init()
	{
		// Generate vars
		logic = new Logic();
		Collection<GameAction> games = constructGameActions();
		Collection<FileAction> files = constructFileActions();
		Collection<SettingAction> settings = constructSettingActions();
		Collection<DeveloperAction> devs = constructDeveloperActions();
		
		// Build layout
		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(logic, BorderLayout.CENTER);
		c.add(constructMenuBar(games, files, settings, devs), BorderLayout.NORTH);
		
		// Add listeners
		logic.addMouseListener(this);
		logic.addMouseMotionListener(this);
	}
	
	public void setCurrentPlayer(Boolean player)
	{
		// Determine current player
		currentPlayer = player;
	}
	
    private Collection<GameAction> constructGameActions()
    {   
    	// Construct list of game actions
    	LinkedList<GameAction> result;
    	result = new LinkedList<GameAction>();
    	result.add( new GameAction(logic, "Player vs Player" , "Play against a human opponent"));
    	result.add( new GameAction(logic, "Rookie" , "Play against an AI using look-ahead-1"));
    	result.add( new GameAction(logic, "Novice" , "Play against an AI using look-ahead-2"));
    	result.add( new GameAction(logic, "Intermediate" , "Play against an AI using look-ahead-3"));
    	result.add( new GameAction(logic, "Professional" , "Play against an AI using look-ahead-4"));
    	result.add( new GameAction(logic, "Expert" , "Play against an AI using look-ahead-5"));
    	result.add( new GameAction(logic, "Godlike" , "Play against an AI using look-ahead-6"));
    	return result;
    }
    
    private Collection<FileAction> constructFileActions()
    {   
    	// Construct list of file actions
    	LinkedList<FileAction> result;
    	result = new LinkedList<FileAction>();
    	result.add( new FileAction(logic, "Load Game", "Load a saved game"));
    	result.add( new FileAction(logic, "Save Game", "Save current game"));
    	return result;
    }
	
	private Collection<SettingAction> constructSettingActions()
	{
		// Construct list of settings
		LinkedList<SettingAction> result;
        result = new LinkedList<SettingAction>();
        result.add( new SettingAction(logic, "Enable/Disable Hints", "Show all legal moves"));
        return result;
	}
	
	private Collection<DeveloperAction> constructDeveloperActions()
	{
		// Construct list of settings
		LinkedList<DeveloperAction> result;
        result = new LinkedList<DeveloperAction>();
        result.add( new DeveloperAction(logic, "Display On/Off", "Enable or disable visual game display"));
        result.add( new DeveloperAction(logic, "Swap Computer/Human", "Change between computer or human opponent"));
        result.add( new DeveloperAction(logic, "Recalibrate AI" , "Reset the AI" ));
        return result;
	}
	
	private Component constructMenuBar(Collection<GameAction> games, Collection<FileAction> files, Collection<SettingAction> settings, Collection<DeveloperAction> devs)
	{
		// Set menu headers
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		
    	menu = new JMenu("Game");
        for (Action game : games)
            menu.add(game);
        menubar.add(menu);
        
    	menu = new JMenu("File");
        for (Action file : files)
            menu.add(file);
        menubar.add(menu);
        
		menu = new JMenu("Settings");
		for (Action setting : settings)
			menu.add(setting);
		menubar.add(menu);
		
		menu = new JMenu("About");
		menu.add(new About());
		menubar.add(menu);
		
		if(method == Reversi.DEVELOPER)
		{
			menu = new JMenu("Developer Access");
			for (Action dev : devs)
				menu.add(dev);
			menubar.add(menu);
		}
		
		return menubar;
	}
	
	public void mousePressed(MouseEvent e)
	{
		this.logic.mousePressed(e.getPoint());
	}
	
	public void mouseClicked(MouseEvent e)
	{
		logic.requestFocus();
	}

	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}
