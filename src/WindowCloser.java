import java.awt.event.*;

public class WindowCloser extends WindowAdapter 
{
	public void windowClosing(WindowEvent e)
	{
		System.out.println("User exits game");
		System.exit(0);
	}
}
