import java.awt.event.*;
import javax.swing.*;

public class DeveloperAction extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	private Logic logic;
	String name;
	
    public DeveloperAction(Logic logic, String name, String tip)
    {   
    	super(name);
        this.logic = logic;
        putValue(Action.SHORT_DESCRIPTION, tip);
    }

    public void actionPerformed(ActionEvent event)
    {   
    	name = (String) this.getValue(Action.NAME);
		
		// Return system output
		System.out.println("DeveloperAction: " + name);
		
		// Determine action
		if(name.equals("Swap Computer/Human"))
		{
			logic.bot[0] = !logic.bot[0];
			logic.repaint();
		}
		
		if(name.equals("Display On/Off"))
		{
			logic.display = !logic.display;
			logic.repaint();
		}
		
		if(name.equals("Recalibrate AI"))
		{
			int choice;
			
			// Warn user of failure to load files
			Object[] options = {"Execute",
			                    "Cancel"};
			choice = JOptionPane.showOptionDialog(null,
				"This progress might take several minuted depending on your system.\n" +
				"Are you sure you wish to continue?",
			    "Confirmation Request",
			    JOptionPane.YES_NO_CANCEL_OPTION,
			    JOptionPane.WARNING_MESSAGE,
			    null,
			    options,
			    options[1]);
			
			switch(choice)
			{
			case 0:
				// Disable display and run
				System.out.println("Starting Recalibration");
				logic.display = false;
				logic.artIntel.spawn();
				logic.display = true;
				break;
			default:
				// Don't do anything
				System.out.println("User Cancelled Request");
				break;
			}
		}
    }
}
