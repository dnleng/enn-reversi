import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class FileAction extends AbstractAction
{   
	private static final long serialVersionUID = 1L;
	private Logic logic;
	String request;
	
    public FileAction(Logic logic, String name, String tip)
    {   
    	super(name);
        this.logic = logic;
        putValue(Action.SHORT_DESCRIPTION, tip);
    }

    public void actionPerformed(ActionEvent event)
    {   
		String file;
    	request = (String) this.getValue(Action.NAME);
		file = "default.save";
		
		// Print request
		System.out.println("FileAction: "+request);
		
		// Determine action
    	if(request.equals("Save Game"))
    		saveGame(file);
    	
    	if(request.equals("Load Game"))
    		loadGame(file);
    }
    
    private void saveGame(String file)
    {
    	FileWriter writer;
    	int xSize, ySize, xCount, yCount, player;
    	int[][] storage;
		
   		// Fetch game logic
		storage = logic.layout;
		player = logic.player;
		ySize = storage.length;
		xSize = storage[0].length;
		
		// Attempt to save the game to a default file while skipping unclaimed fields
		try
		{
			writer = new FileWriter(file);
			
			// Set current player
			writer.write("Player "+player+"\n");
			
			// Set current difficulty
			writer.write("Difficulty "+logic.artIntel.difficulty+"\n");
			
			// Set field ownership
			for(yCount = 0 ; yCount < ySize ; yCount++)
				for(xCount = 0 ; xCount < xSize ; xCount++)
					if(storage[xCount][yCount] != 0)
						writer.write("Set "+xCount+" "+yCount+" "+storage[xCount][yCount]+"\n");

			// Close file
			writer.close();
			
			// Return success message
			JOptionPane.showMessageDialog( null
					, "Game saved succesfully!"
					, "Save Game"
					, JOptionPane.INFORMATION_MESSAGE
					);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog( null
					, "Error writing to "+file
					, "Error"
					, JOptionPane.ERROR_MESSAGE
                   	);
			System.out.println(e.toString());
		}
    }
    
    private void loadGame(String file)
    {
    	BufferedReader reader;
    	int xField, yField, ownership;
		String line, task;
		Scanner sc;
		
		// Attempt to load file
		try
		{
			// Prepare file and clear game field
			reader = new BufferedReader(new FileReader(file));
			logic.layout = Logic.clear(logic.layout);
			
			while((line = reader.readLine()) != null)
			{
				sc = new Scanner(line);
				task = sc.next();
			
				// Set current player
				if(task.equals("Player"))
					logic.player = Integer.parseInt(sc.next());
				
				if(task.equals("Difficulty"))
					logic.artIntel.difficulty = Integer.parseInt(sc.next());
			
				// Set field ownership
				if(task.equals("Set"))
				{
					xField = Integer.parseInt(sc.next());
					yField = Integer.parseInt(sc.next());
					ownership = Integer.parseInt(sc.next());
					logic.layout[xField][yField] = ownership;
				}
			}
			
			// Return the loaded game and close file
			logic.repaint();
			reader.close();
		
			// Return success message
			JOptionPane.showMessageDialog( null
					, "Game loaded succesfully!"
					, "Load Game"
					, JOptionPane.INFORMATION_MESSAGE
					);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog( null
					, "Error reading from "+file+"\nFile does not exist?"
					, "Error"
					, JOptionPane.ERROR_MESSAGE
               		);
			System.out.println(e.toString());
		}
    }
}