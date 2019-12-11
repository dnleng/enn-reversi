import java.awt.*;

public class Display
{
	boolean hint;
	boolean endGame;
	String status;
	Logic logic;
	int xFieldSize;
	int yFieldSize;
	int pinSize;
	int player;
	int [][] grid;
	Boolean [][] legal;
	int [] playerScore;
	
	// Preset constants
	final private static int xPos = 50;
	final private static int yPos = 200;
	final private static int yScorePos = 20;
	final private static int playerCap = 2;
	
	public Display(int x, int y, int[][] board, Logic log)
	{
		// Set values
		pinSize = 50;
		xFieldSize = x;
		yFieldSize = y;
		grid = board;
		logic = log;
		endGame = false;
		
		// Set array
		playerScore = new int[playerCap];
		
		// Set default player
		player = 1;
		
		// Turn off hints by default
		hint = false;
	}
	
	public void draw(Graphics gr)
	{
		if(logic.display)
		{
			int count, topScore;
			String winner;
		
			// Clear Display
			gr.setColor(Color.WHITE);
			gr.fillRect(0, 0, 400, 600);
			gr.setColor(Color.BLACK);
		
			// Fetch memory and draw field
			this.drawScore(gr);
			this.drawField(gr);
			if (endGame)
			{
				// Game is deadlocked, declare winner
				topScore = 0;
				for(count = 0 ; count < playerCap ; count++)
					if(playerScore[count] > topScore)
						topScore = playerScore[count];
			
				winner = "";
				for (count = 0 ; count < playerCap ; count++)
					if(playerScore[count] == topScore)
						if(winner == "")
							winner = ("Winner: Player "+(count+1));
						else
							winner = "Tie";
			
				gr.drawString("Game Over | "+winner, xPos+(xFieldSize*pinSize/2)-(10*winner.length()/2), yPos-10);
			}
		}
	}
	
	public void drawScore(Graphics score)
	{
		int playerCount, xCount, yCount, xPlayer, yPlayer;

		// Reset player scores
		for(playerCount = 0 ; playerCount < playerCap ; playerCount++)
			playerScore[playerCount] = 0;
		
		// Count points for each pin
		for(xCount = 0 ; xCount < xFieldSize ; xCount++)
			for(yCount = 0 ; yCount < yFieldSize ; yCount++)
			{
				switch(grid[xCount][yCount])
				{
				case 1:
					playerScore[0]++;
					break;
				case 2:
					playerScore[1]++;
					break;
				}
			}
		
		// Player One score
		score.setColor(Color.BLUE);
		score.fillOval(50, yScorePos+pinSize, pinSize, pinSize);
		score.setColor(Color.BLACK);
		score.drawString("Score: "+playerScore[0]+" points", 60+pinSize, (int)(yScorePos+(1.5*pinSize)));
			
		// Player Two score
		score.setColor(Color.RED);
		score.fillOval(50, yScorePos+(2*pinSize+5), pinSize, pinSize);
		score.setColor(Color.BLACK);
		score.drawString("Score: "+playerScore[1]+" points", 60+pinSize, (int)(yScorePos+(2.5*pinSize)+5));

		// Turn marker
		switch(player)
		{
		case 1:
			xPlayer = 50+pinSize/4;
			yPlayer = pinSize+pinSize/4;
			break;
		case 2:
			xPlayer = 50+pinSize/4;
			yPlayer = 2*pinSize+5+pinSize/4;
			break;
		default:
			xPlayer = 50+pinSize/4;
			yPlayer = pinSize+pinSize/4;
			break;
		}
		score.setColor(Color.YELLOW);
		score.fillOval(xPlayer, yScorePos+(yPlayer), pinSize/2, pinSize/2);
		score.setColor(Color.BLACK);
		
	}
		
	public void drawField(Graphics field)
	{
		int xCount, yCount;
		
		// Draw pins for each field according to ownership
		for(xCount = 0 ; xCount < grid.length ; xCount++)
			for(yCount = 0 ; yCount < grid[xCount].length ; yCount++)
			{
				switch(grid[xCount][yCount])
				{
				case 1:
					field.setColor(Color.BLUE);
					field.fillOval(xCount*pinSize+xPos, yCount*pinSize+yPos, pinSize, pinSize);
					break;
				case 2:
					field.setColor(Color.RED);
					field.fillOval(xCount*pinSize+xPos, yCount*pinSize+yPos, pinSize, pinSize);
					break;
				default:
					if(logic.run)
						if(hint && legal[xCount][yCount])
						{
							field.setColor(Color.YELLOW);
							field.fillOval(xCount*pinSize+xPos, yCount*pinSize+yPos, pinSize, pinSize);
						}
					break;
				}
			}	
		field.setColor(Color.BLACK);
		
		// Draw field lines
		for(xCount = 0 ; xCount <= xFieldSize ; xCount++)
			field.drawLine(xCount*pinSize+xPos, yPos, xCount*pinSize+xPos, yFieldSize*pinSize+yPos);
		
		for(yCount = 0 ; yCount <= yFieldSize ; yCount++)
			field.drawLine(xPos, yCount*pinSize+yPos, xFieldSize*pinSize+xPos, yCount*pinSize+yPos);
	}
}
