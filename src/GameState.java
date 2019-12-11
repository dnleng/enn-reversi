import java.util.*;

public class GameState
{
	AI artIntel;
	GameState parent;
	NeuralNetwork botNet;
	boolean type, evaluated, top, skip, abBlock;
	Collection<GameState> children;
	int[][] game;
	int player, ply, maxPly;
	double valuation, alpha, beta;
	int[] move;
	
	final static boolean MIN = false;
	final static boolean MAX = true;
	
	public GameState(AI artIntel, NeuralNetwork botNet, GameState parent, int[][] board, int[] move)
	{
		// Set GameState settings for a subtree
		this.artIntel = artIntel;
		this.game = Logic.deepClone(board);
		this.type = !parent.type;
		this.evaluated = false;
		this.parent = parent;
		this.top = false;
		this.maxPly = parent.maxPly;
		this.ply = parent.ply + 1;
		this.move = move;
		this.skip = false;
		this.abBlock = false;
		this.alpha = -9999999999f;
		this.beta = 9999999999f;
		this.botNet = botNet;
		
		// Swap player since previous GameState
		if(parent.player == 1)
			this.player = 2;
		else
			this.player = 1;
		
		if(type == MAX)
			this.valuation = -9999999999f;
		else
			this.valuation = 9999999999f;
		
		
		// Determine if root has been reached
		if(ply < this.maxPly)
			seed();
		else
		{
			// Evaluate the current board
			valuation = botNet.compute(this.game);
			if(type == MAX)
				this.alpha = this.valuation;
			else
				this.beta = this.valuation;
			
			this.evaluated = true;
		}
	}
	
	public GameState(AI artIntel, NeuralNetwork botNet, int[][] board, int player, int maxPly)
	{
		// Set GameState settings for main tree
		this.artIntel = artIntel;
		this.game = Logic.deepClone(board);
		this.type = MAX;
		this.evaluated = false;
		this.parent = null;
		this.top = true;
		this.maxPly = maxPly;
		this.ply = 0;
		this.player = player;
		this.skip = false;
		this.abBlock = false;
		this.alpha = -9999999999f;
		this.beta = 9999999999f;
		this.botNet = botNet;
		

		this.valuation = -9999999999f;

		
		
		// Determine if root has been reached
		if(ply < this.maxPly)
			seed();
		else
		{
			// Evaluate the current board
			this.valuation = botNet.compute(this.game);
			if(type == MAX)
				this.alpha = this.valuation;
			else
				this.beta = this.valuation;
			
			this.evaluated = true;
		}
	}
	
	private void seed()
	{
		GameState next;
		int xCount, yCount;
		Boolean[][] legal;
		Boolean locatedLegal, deadlock;
		int[][] storage, nextState;
		
		// Generate branches
		children = new LinkedList<GameState>();
		storage = Logic.deepClone(game);
		legal = Logic.generateMoveList(game, player);
		
		locatedLegal = false;
		for(yCount = 0 ; yCount < Logic.DEFAULTY ; yCount++)
			for(xCount = 0 ; xCount < Logic.DEFAULTX ; xCount++)	
				if(legal[xCount][yCount])
				{
					// Add next possible move to the tree
					nextState = Logic.deepClone(Logic.makeMove(Logic.deepClone(storage), player, xCount, yCount));
					int[] nextMove = {xCount, yCount};
					next = new GameState(artIntel, botNet, this, nextState, nextMove);
					children.add(next);
					
					// Determine a move has been found
					if(!locatedLegal)
						locatedLegal = true;
				}
		
		// If no legal move has been found, copy current GameState instead
		if(!locatedLegal)
		{
			deadlock = Logic.checkEndGame(storage);
			if(deadlock)
			{
				// End game situation
				evaluated = true;
				valuation = -100.0;
				skip = true;
			}
			else
			{
				// Skip move
				next = new GameState(artIntel, botNet, this, storage, move);
				children.add(next);
			}
		}
	}
	
	public void alphaBeta()
	{	
		for(GameState state : children)
		{
			// Check alpha-beta match
			if(this.alpha > this.beta && !abBlock)
			{
				abBlock = true;
				break;
			}
			
			if(!abBlock)
			{	
				if(!state.evaluated)
				{
					// We need to go further down the tree first
					if(type == MAX)
						state.alpha = this.alpha;
					else
						state.beta = this.beta;
						
					state.alphaBeta();			
				}	

				// Since we're evaluated we can pass on our value
				if(type == MAX)
				{
					// Parent requests maximizing values
					if(valuation < state.valuation)
					{
						valuation = state.valuation;
						if(this.alpha < state.beta)
							this.alpha = state.beta;
					}
				}
				else
				{
					// Parent requests minimizing values
					if(valuation > state.valuation)
					{
						valuation = state.valuation;
						if(this.beta > state.alpha)
							this.beta = state.alpha;
					}
				}
			}
		}
		
		// Evaluate this branch
		this.evaluated = true;
			
		// Check for top tree
		if(top)
			getTopMove();
	}
	
	private void getTopMove()
	{
		// Since we are at the top now we can finally fetch the best move
		// by comparing child valuations to match this GameState's valuation
		for(GameState state : children)
			if(state.valuation == this.valuation)
				this.move = state.move;
		
		// In case this valuation has a null-move enable skipping
		if(this.move == null)
			this.skip = true;
	}
}
