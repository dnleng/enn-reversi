import java.awt.*;
import javax.swing.*;

public class Logic extends JPanel
{
	private static final long serialVersionUID = 1L;
	public Display board;
	public int [][] layout;
	public Boolean [][] legal;
	public Boolean [] bot;
	Boolean run, display;
	int [] playerScore;
	int player, xMouse, yMouse;
	AI artIntel;
	NeuralNetwork [] botNet;
	long startTime, startMove;
	
	final private static int playerCap = 2;
	final private static int pinSize = 50;
	final private static int xPos = 50;
	final private static int yPos = 200;
	
	final public static int DEFAULTX = 6;
	final public static int DEFAULTY = 6;
	final private static int NORTH = -1;
	final private static int SOUTH = 1;
	final private static int EAST = 1;
	final private static int WEST = -1;
	final private static int CENTRAL = 0;

	
	public Logic()
	{
		// Create a vector of not owned field spots
		playerScore = new int [playerCap];
		layout = new int[DEFAULTX][DEFAULTY];
		legal = new Boolean[DEFAULTX][DEFAULTY];
		bot = new Boolean[2];
		botNet = new NeuralNetwork[2];
		artIntel = new AI(this, AI.BOTPLY);
		bot[0] = false;
		bot[1] = false;
		botNet[0] = artIntel.neuralNet;
		botNet[1] = artIntel.neuralNet;
		player = 1;
		run = false;
		display = true;
		
		// Build game board using the layout vector
		board = new Display(DEFAULTX, DEFAULTY, layout, this);
	}
	
	public void paint(Graphics g)
	{	
		int xCount, yCount;
		Boolean skip;
		
		if(run)
		{
			// Initiate timer
			startMove = System.currentTimeMillis();
			
			// Generate game board for a human player
			board.player = player;
			board.legal = legal;
			board.draw(g);
			
			// Generate an array of legal moves
			legal = generateMoveList(layout, player);
		
			// Check for game deadlock
			if(checkEndGame(layout))
			{
				board.endGame = true;
				run = false;
				System.out.println("Game over");
				System.out.println("Game time: "+(System.currentTimeMillis()-startTime)/1000F+"sec");
				this.repaint();
			}
			else 
			{
				// Check if we have to skip our move
				skip = true;
				for(yCount = 0 ; yCount < DEFAULTY ; yCount++)
					for(xCount = 0 ; xCount < DEFAULTY ; xCount++)
						if(legal[xCount][yCount])
							skip = false;
				
				if(skip)
				{
					System.out.println("Player "+player+" skipped move");
					player = swapPlayer(player);
					this.repaint();
				}
			}
		
			// Determine Neural Network valuation;
			if(bot[player-1] && !board.endGame)
			{
				// Set the correct player and start determining next move
				artIntel.player = player;
				artIntel.reason(botNet[player-1], this.layout, this.player);
			
				// If we aren't skipping our move, execute the move instead
				if(!artIntel.reason.skip)
				{
					System.out.println("Bot "+player+" claimed ("+artIntel.reason.move[0]+";"+artIntel.reason.move[1]+") - ("+(System.currentTimeMillis()-startMove)+" ms.)");
					layout = makeMove(layout, player, artIntel.reason.move[0], artIntel.reason.move[1]);
				} 
				else
					System.out.println("Bot "+player+" skipped move - ("+(System.currentTimeMillis()-startMove)+" ms.)");
			
				// Swap player
				player = swapPlayer(player);
			
				this.repaint();
			}
		} 
		else
		{
			// Draw a preset opening screen that will also be displayed when the game ends
			if(this.display)
			{
				splashScreen(g);
				this.repaint();
			}
		}
	}
	
	public static int[][] clear(int[][] layout)
	{
		int xCount, yCount;
		
		for(yCount = 0; yCount < DEFAULTY; yCount++)
			for(xCount = 0; xCount < DEFAULTX; xCount++)
				layout[xCount][yCount] = 0;
		
		return layout;
	}
	
	public void newGame(int difficulty)
	{
		// Clear board
		layout = clear(layout);
		
		// Set default positions
		layout[DEFAULTX/2-1][DEFAULTY/2-1] = 2;
		layout[DEFAULTX/2][DEFAULTY/2-1] = 1;
		layout[DEFAULTX/2-1][DEFAULTY/2] = 1;
		layout[DEFAULTX/2][DEFAULTY/2] = 2;
		
		// Reset base values
		player = 1;
		
		// Set board display values
		board.player = player;
		board.endGame = false;
		board.grid = layout;
		
		// Set difficulty
		artIntel.difficulty = difficulty;
		
		// Initiate display
		System.out.println("Started new game at level "+difficulty);
		this.run = true;
		startTime = System.currentTimeMillis();
		this.repaint();
	}
	
	public static Boolean[][] generateMoveList(int[][] board, int player)
	{
		Boolean[][] result;
		int xCount, yCount;
		result = new Boolean[DEFAULTX][DEFAULTY];
		
		// Fetch move validity for every field
		for(yCount = 0; yCount < DEFAULTY; yCount++)
			for(xCount = 0; xCount < DEFAULTX; xCount++)
				result[xCount][yCount] = fetchMoves(board, player, xCount, yCount);
		
		return result;
	}
	
	private static Boolean legalMove(int[][] board, int player, int x, int y, int xModifier, int yModifier)
	{
		int xStorage, yStorage;
		Boolean result;
		result = false;
		
		// First check if the requested field is neutral
		if(board[x][y] == 0)
		{
			// Check for array boundaries
			if(x+xModifier >= 0 && x+xModifier < DEFAULTX && y+yModifier >= 0 && y+yModifier < DEFAULTY)
			{
				// Check if adjacent field is taken by an opponent 
				if(board[x+xModifier][y+yModifier] != player && board[x+xModifier][y+yModifier] != 0)
				{
					// Store that field's vector
					xStorage = xModifier;
					yStorage = yModifier;
				
					// Go on checking field ownership until we're going out of field bounds or if the located field is not owned by an opponent
					while(x+xStorage >= 0 && x+xStorage < DEFAULTX && y+yStorage >= 0 && y+yStorage < DEFAULTY && board[x+xStorage][y+yStorage] != player && board[x+xStorage][y+yStorage] != 0)
					{
						xStorage += xModifier;
						yStorage += yModifier;
					}
				
					// Again check our array boundaries in case we ran off field
					if(x+xStorage >= 0 && x+xStorage < DEFAULTX && y+yStorage >= 0 && y+yStorage < DEFAULTY)
					{
						// If we own the last adjacent field, the move is valid
						if(board[x+xStorage][y+yStorage] == player)
							result = true;
					}
				}
			}
		}
		// Return move validity
		return result;
	}
	
	public static Boolean checkDeadlock(Boolean[][] legal)
	{
		int xCount, yCount;
		
		// Check if there is any legal move available
		for(yCount = 0; yCount < DEFAULTY; yCount++)
			for(xCount = 0; xCount < DEFAULTX; xCount++)
				if(legal[xCount][yCount])
					return false;
		
		// If no moves are found, return deadlock
		return true;
	}
	
	public static Boolean checkEndGame(int[][] board)
	{
		int xCount, yCount;
		Boolean[][] player1, player2;
		player1 = generateMoveList(board, 1);
		player2 = generateMoveList(board, 2);
		
		// Check if there is any legal move available
		for(yCount = 0; yCount < DEFAULTY; yCount++)
			for(xCount = 0; xCount < DEFAULTX; xCount++)
				if(player1[xCount][yCount] || player2[xCount][yCount])
					return false;
		
		// If no moves are found, return deadlock
		return true;
	}
	
	private static int[][] generateCaptureList(int[][] layout, int player, int x, int y)
	{
		int[][] memory;
		
		// Check the surroundings for the given field and capture if allowed
		memory = layout;
		memory = capture(layout, player, x, y, WEST, CENTRAL);
		memory = capture(layout, player, x, y, EAST, CENTRAL);
		memory = capture(layout, player, x, y, CENTRAL, NORTH);
		memory = capture(layout, player, x, y, CENTRAL, SOUTH);
		memory = capture(layout, player, x, y, WEST, SOUTH);
		memory = capture(layout, player, x, y, WEST, NORTH);
		memory = capture(layout, player, x, y, EAST, SOUTH);
		memory = capture(layout, player, x, y, EAST, NORTH);
		
		return memory;
	}
	
	private static int[][] capture(int[][] layout, int player, int x, int y, int xVector, int yVector)
	{
		int[][] memory;
		int xStore, yStore;
		Boolean capturable;
		
		memory = layout;
		capturable = false;
		xStore = xVector;
		yStore = yVector;
		
		// Set starting field ownership
		layout[x][y] = player;
		memory[x][y] = player;
		
		// Check for array boundaries and initiate search loop
		if(x+xStore >= 0 && x+xStore < DEFAULTX && y+yStore >= 0 && y+yStore < DEFAULTY)
			while(layout[x+xStore][y+yStore] != player && layout[x+xStore][y+yStore] != 0)
			{
				// Go to adjacent field following given vector
				xStore += xVector;
				yStore += yVector;
				
				// If this field is owned by the player, the start field is capturable
				if(x+xStore >= 0 && x+xStore < DEFAULTX && y+yStore >= 0 && y+yStore < DEFAULTY)
					if(layout[x+xStore][y+yStore] == player)
					{
						capturable = true;
						break;
					}
				
				// Again check if new field lies within array boundaries
				if(!(x+xStore >= 0 && x+xStore < DEFAULTX && y+yStore >= 0 && y+yStore < DEFAULTY))
					break;
			}
		
			if(capturable)
			{
				xStore -= xVector;
				yStore -= yVector;
				
				if(x+xStore >= 0 && x+xStore < DEFAULTX && y+yStore >= 0 && y+yStore < DEFAULTY)
					while(layout[x+xStore][y+yStore] != player && layout[x+xStore][y+yStore] != 0)
					{
						// Set ownership while tracing back
						memory[x+xStore][y+yStore] = player;
						
						// Back-trace vector to starting field
						xStore -= xVector;
						yStore -= yVector;
						
						// Be sure that the field lies within array boundaries
						if(!(x+xStore >= 0 && x+xStore < DEFAULTX && y+yStore >= 0 && y+yStore < DEFAULTY))
							break;
					}
			}
		return memory;
	}
	
	public static Boolean fetchMoves(int[][] board, int player, int x, int y)
	{
		// Check for allowed moves around a specific point
		if(legalMove(board, player, x, y, WEST, CENTRAL))
			return true;
		if(legalMove(board, player, x, y, EAST, CENTRAL))
			return true;
		if(legalMove(board, player, x, y, CENTRAL, NORTH))
			return true;
		if(legalMove(board, player, x, y, CENTRAL, SOUTH))
			return true;
		if(legalMove(board, player, x, y, WEST, SOUTH))
			return true;
		if(legalMove(board, player, x, y, WEST, NORTH))
			return true;
		if(legalMove(board, player, x, y, EAST, SOUTH))
			return true;
		if(legalMove(board, player, x, y, EAST, NORTH))
			return true;
		
		// No links found
		return false;
	}
	
	public static int swapPlayer(int player)
	{
		// Prepare next turn by swapping players
		if(player == 1)
			player = 2;
		else
			player = 1;
		
		return player;
	}
	
	public static int[][] makeMove(int[][] layout, int player, int x, int y)
	{
		layout[x][y] = player;
		layout = generateCaptureList(layout, player, x, y);

		return layout;
	}
	
	public static int[][] deepClone(int[][] sourceArray) 
	{
		int count;
		int [][]targetArray = new int[sourceArray.length][sourceArray[0].length];
		
		for(count = 0 ; count < sourceArray.length ; count++)
			System.arraycopy(sourceArray[count], 0, targetArray[count], 0, sourceArray[count].length);
		
		return targetArray;
	}
	
	public static void tempPrintArray(int[][] source)
	{
		int xCount, yCount;
		
		for(yCount = 0 ; yCount < Logic.DEFAULTY ; yCount++)
			for(xCount = 0 ; xCount < Logic.DEFAULTX ; xCount++)
				System.out.println("Source returns "+source[xCount][yCount]+" at ["+xCount+"]["+yCount+"]");
	}
	
	public void mousePressed(Point p) 
	{
		int xReal, yReal;

		// Convert real coordinates to field coordinates
		xReal = p.x - xPos;
		yReal = p.y - yPos;
		
		// Check for boundaries
		if(xReal > 0 && xReal < DEFAULTX*pinSize && yReal > 0 && yReal < DEFAULTY*pinSize)
		{
			xMouse = xReal / pinSize;
			yMouse = yReal / pinSize;
			
			// Set ownership when clicked within boundaries
			if(legal[xMouse][yMouse])
			{
				System.out.println("Player "+player+" claimed ("+(xMouse)+";"+(yMouse)+")");
				layout = makeMove(layout, player, xMouse, yMouse);
				player = swapPlayer(player);
				board.grid = layout;
				this.repaint();
			}
		}
	}
	
	private void splashScreen(Graphics g)
	{
		board.draw(g);
		g.drawString("Please start a new game...", 50, 50);
	}
}
