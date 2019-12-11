import java.awt.*;
import javax.swing.JFrame;

public class Reversi extends JFrame
{

	/**
	 * Reversi Evolved
	 * 
	 * Reversi Evolved is a project for the Introduction to Computational Intelligence 
	 * at Utrecht University, with the goal of creating an AI learning AI opponent using 
	 * a basic Evolutionary Neural Network along with the Alpha-Beta Algorithm to boost 
	 * performance.
	 * 
	 * Copyright © 2008, D.N. de Leng
	 */
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT = 0;
	public static final int DEVELOPER = 1;
	
	public Reversi(int method)
	{
		this.setSize(400, 600);
		this.setTitle("Reversi Evolved");
		this.setBackground(Color.LIGHT_GRAY);
		this.addWindowListener(new WindowCloser());
		this.getContentPane().add(new Nexus(method), BorderLayout.CENTER);
	}
	
	public static void main(String[] args) 
	{
		int count, method;
		method = DEFAULT;
		
		if(args.length > 0)
			for (count = 0; count < args.length; count++)
			{
				if(args[count].equals("help") || args[count].equals("?"))
				{
					System.out.println("Reversi Evolved\n---");
					System.out.println("help, ? :: Shows this message");
					System.out.println("dev     :: Starts the program with special features");
					System.exit(0);
				}
				else if(args[count].equals("dev"))
					method = DEVELOPER;
			}
		
		System.out.println("User enters game");
		new Reversi(method).setVisible(true);
	}
}
