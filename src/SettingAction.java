import java.awt.event.*;
import javax.swing.*;

public class SettingAction extends AbstractAction
{
	/**
	 * SettingAction.class
	 * Connects to the settings tab in the GUI, determining which function has been called.
	 */
	private static final long serialVersionUID = 1L;
	private Logic logic;
	String name;
	
    public SettingAction(Logic logic, String name, String tip)
    {   
    	super(name);
        this.logic = logic;
        putValue(Action.SHORT_DESCRIPTION, tip);
    }

    public void actionPerformed(ActionEvent event)
    {   
    	name = (String) this.getValue(Action.NAME);
		
		// Return system output
		System.out.println("SettingAction: " + name);
		
		// Determine action
		if(name.equals("Enable/Disable Hints"))
		{
			logic.board.hint = !logic.board.hint;
			logic.repaint();
		}
    }
}