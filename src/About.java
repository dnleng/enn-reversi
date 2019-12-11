import java.awt.event.*;
import javax.swing.*;

public class About extends AbstractAction 
{
	private static final long serialVersionUID = 1L;

	public About()
	{
		super("About");
	}
	
	public void actionPerformed(ActionEvent event)
	{
		System.out.println("AboutAction: About");
		JOptionPane.showMessageDialog( null
									, "Reversi Evolved\nProject for Introduction to Computational Intelligence, " +
											"Utrecht University\n\nCopyright © 2008, D.N. de Leng"
									, "About"
									, JOptionPane.INFORMATION_MESSAGE
									);
	}
}
