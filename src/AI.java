import java.util.*;

public class AI
{
	public static int ROOKIE = 1;
	public static int NOVICE = 2;
	public static int INTERMEDIATE = 3;
	public static int PROFESSIONAL = 4;
	public static int EXPERT = 5;
	public static int GODLIKE = 6;
	public static int BOTPLY = NOVICE;
	private static int WIN = 3;
	private static int DRAW = 1;
	private static int LOSE = -1;
	private static int POPULATION = 20;
	private static int SURVIVAL = POPULATION/2;
	private static int MAXGEN = 5;
	
	NeuralNetwork neuralNet;
	ArrayList<NeuralNetwork> neuralList, winners;
	ArrayList<Integer> neuralScore;
	Logic logic;
	GameState reason;
	int[][] board;
	int player, difficulty, genMax;
	
	public AI(Logic logic, int difficulty)
	{
		// Set up a new board layout and attach the Neural Network
		this.logic = logic;
		this.difficulty = difficulty;
		board = Logic.deepClone(logic.layout);
		player = logic.player;
		neuralNet = new NeuralNetwork(logic, (Logic.DEFAULTX*Logic.DEFAULTY), NeuralNetwork.DEFAULT);
		neuralList = new ArrayList<NeuralNetwork>();
		winners = new ArrayList<NeuralNetwork>();
		neuralScore = new ArrayList<Integer>();
	}
	
	public void reason(NeuralNetwork bot, int[][] layout, int player)
	{
		// Build a new search tree and apply alphaBeta
		reason = new GameState(this, bot, layout, player, difficulty);
		reason.alphaBeta();
	}
	
	public void spawn()
	{
		int count, generation;
		NeuralNetwork winner;
		ArrayList<NeuralNetwork> survivers;
		survivers = new ArrayList<NeuralNetwork>();
		
		System.out.println("Spawning new neural networks");
		for(count = 0 ; count < POPULATION ; count++)
		{
			// Generate a new batch of neural networks and set default scores
			neuralList.add(new NeuralNetwork(logic, (Logic.DEFAULTX*Logic.DEFAULTY), NeuralNetwork.EVOLUTION));
			neuralScore.add(0);
		}
		
		System.out.println("Starting fitness selection now");
		for(generation = 0 ; generation < MAXGEN ; generation++)
		{
			System.out.println("Starting generation "+generation+" survival competition");
			survivers.addAll(this.compete(neuralList, neuralScore, POPULATION));
			winners.add(this.fetchFittest(neuralList, neuralScore));
			
			if(generation < MAXGEN)
			{
				System.out.println("Mutating "+survivers.size()+" networks");
				survivers.addAll(this.mutate(survivers));
				System.out.println("Network total; "+survivers.size());
			
				neuralList.clear();
				neuralScore.clear();
				neuralList.addAll(survivers);
				for(count = 0 ; count < POPULATION ; count++)
					neuralScore.add(0);
				survivers.clear();
			}
		}
		
		// Final selection
		neuralList.clear();
		neuralScore.clear();
		for(count = 0 ; count < MAXGEN ; count++)
			neuralScore.add(0);
		survivers.clear();
		System.out.println("Starting final winner selection");
		neuralList.addAll(this.compete(winners, neuralScore, MAXGEN));
		
		System.out.println("Fitness selection has been successfully completed");
		winner = fetchFittest(neuralList, neuralScore);
		winner.type = NeuralNetwork.DEFAULT;

		winner.hidden2.writeLayer(winner.hidden2.id, false);
		winner.hidden1.writeLayer(winner.hidden1.id, false);
		winner.writeNeuron(winner.id, false);
		System.out.println("Neural Network has been saved.");
		
		this.neuralNet = winner;
		System.out.println("Applied Neural Network to current game.\nJob's done!");
	}
	
	private Collection<NeuralNetwork> compete(ArrayList<NeuralNetwork> neuralList, ArrayList<Integer> neuralScore, int max)
	{
		int xCount, yCount, yCountMax, counter, minScore, maxScore, best;
		int[] playerScore;
		Collection<NeuralNetwork> survivers;
		Collection<Integer> surviverScores;
		survivers = new ArrayList<NeuralNetwork>();
		surviverScores = new ArrayList<Integer>();
		yCountMax = max;
		
		for(xCount = 0 ; xCount < max ; xCount++)
		{	
			for(yCount = 0 ; yCount < yCountMax ; yCount++)
				if(yCount != xCount)
				{
					// Play a match between 2 neural networks
					playerScore = this.simulate(neuralList.get(xCount), neuralList.get(yCount));
					System.out.println("Bot "+xCount+" ("+neuralScore.get(xCount)+") vs Bot "+yCount+" ("+neuralScore.get(yCount)+") result: "+playerScore[0]+"-"+playerScore[1]);
					
					// Give winner 3 points and take one point from loser
					// Alternatively, give both players 1 point in case of draw
					if(playerScore[0] > playerScore[1])
					{
						neuralScore.set(xCount, (neuralScore.get(xCount) + WIN));
						neuralScore.set(yCount, (neuralScore.get(yCount) + LOSE));
					}
					else if(playerScore[0] < playerScore[1])
					{
						neuralScore.set(yCount, (neuralScore.get(yCount) + WIN));
						neuralScore.set(xCount, (neuralScore.get(xCount) + LOSE));
					}
					else
					{
						neuralScore.set(xCount, (neuralScore.get(xCount) + DRAW));
						neuralScore.set(yCount, (neuralScore.get(yCount) + DRAW));
					}
				}
			
			// Deduct the max count to avoid double matches
			//yCountMax--;
		}
		
		// Determine best batch
		minScore = -999999999;
		maxScore = 999999999;
		best = 0;
		counter = 0;
		

		for(xCount = 0 ; xCount < (max/2) ; xCount++)
		{	
			minScore = -999999999;
			for(yCount = 0 ; yCount < (max-counter) ; yCount++)
			{
				if(neuralScore.get(yCount) > minScore && neuralScore.get(yCount) <= maxScore)
				{
					minScore = neuralScore.get(yCount);
					best = yCount;
				}
			}
			survivers.add(neuralList.get(best));
			surviverScores.add(neuralScore.get(best));
			neuralList.remove(best);
			neuralScore.remove(best);
			counter++;
		}
		System.out.println("Surviving neural network scores: "+surviverScores.toString());
		return survivers;
	}
	
	private int[] simulate(NeuralNetwork xBot, NeuralNetwork yBot)
	{
		NeuralNetwork[] botNet;
		Boolean[][] legal;
		int[][] layout;
		Boolean run, skip;
		int xCount, yCount, player;
		int score[];
		
		botNet = new NeuralNetwork[2];
		score = new int[2];
		run = true;
		skip = false;
		player = 1;
		
		botNet[0] = xBot;
		botNet[1] = yBot;
		
		layout = new int[Logic.DEFAULTX][Logic.DEFAULTY];
		layout = Logic.clear(layout);
		layout[Logic.DEFAULTX/2-1][Logic.DEFAULTY/2-1] = 2;
		layout[Logic.DEFAULTX/2][Logic.DEFAULTY/2-1] = 1;
		layout[Logic.DEFAULTX/2-1][Logic.DEFAULTY/2] = 1;
		layout[Logic.DEFAULTX/2][Logic.DEFAULTY/2] = 2;
		
		while(run)
		{
			// Reset skipping
			skip = false;
			
			// Generate an array of legal moves
			legal = Logic.generateMoveList(layout, player);
	
			// Check for game deadlock
			if(Logic.checkEndGame(layout))
				run = false;
			else 
			{
				// Check if we have to skip our move
				skip = true;
				for(yCount = 0 ; yCount < Logic.DEFAULTY ; yCount++)
					for(xCount = 0 ; xCount < Logic.DEFAULTX ; xCount++)
					{
						if(legal[xCount][yCount])
						{
							skip = false;
							break;
						}
					}
				
				if(skip)
					player = Logic.swapPlayer(player);	
			}	
	
			// Determine Neural Network valuation;
			if(run && !skip)
			{
				// Set the correct player and start determining next move
				this.reason(botNet[player-1], layout, player);
				
				// If we aren't skipping our move, execute the move instead
				if(!this.reason.skip)
					layout = Logic.makeMove(layout, player, this.reason.move[0], this.reason.move[1]);	
				
				// Swap player
				player = Logic.swapPlayer(player);
			}	
		}	
		
		// Set starting points
		score[0] = 0;
		score[1] = 0;
		
		// Determine total score
		for(yCount = 0 ; yCount < Logic.DEFAULTY ; yCount++)
			for(xCount = 0 ; xCount < Logic.DEFAULTX ; xCount++)
				if(layout[xCount][yCount] == 1)
					score[0]++;
				else if(layout[xCount][yCount] == 2)
					score[1]++;
		
		return score;
	}
	
	private ArrayList<NeuralNetwork> mutate(ArrayList<NeuralNetwork> neuralNets)
	{
		Random r = new Random();
		int networkCount, neuronCount, elemCount, count;
		double newVal;
		networkCount = 0;
		neuronCount = 0;
		elemCount = 0;
		ArrayList<Double> weightVars;
		ArrayList<NeuralNetwork> input, result;

		weightVars = new ArrayList<Double>();
		input = neuralNets;
		result = new ArrayList<NeuralNetwork>();
		

		//for(count = SURVIVAL ; count < POPULATION ; count+=SURVIVAL)
		//{
			for(NeuralNetwork net : neuralNets)
			{
				// Mutate layer 2 bias variance
				for(Double biasVar : net.hidden2.biasVarList)
				{
					newVal = biasVar*Math.exp(r.nextGaussian()/(Math.sqrt(2*Logic.DEFAULTX*Logic.DEFAULTY)))+(r.nextGaussian()/Math.sqrt(2*Math.sqrt(Logic.DEFAULTX*Logic.DEFAULTY)));
					if(newVal < 0.1)
						newVal = 0.1;
					
					net.hidden2.biasVarList.set(neuronCount, newVal);
					neuronCount++;
				}
				neuronCount = 0;
				
				// Mutate layer 1 bias variance
				for(Double biasVar : net.hidden1.biasVarList)
				{
					newVal = biasVar*Math.exp(r.nextGaussian()/(Math.sqrt(2*Logic.DEFAULTX*Logic.DEFAULTY)))+(r.nextGaussian()/Math.sqrt(2*Math.sqrt(Logic.DEFAULTX*Logic.DEFAULTY)));
					if(newVal < 0.1)
						newVal = 0.1;
					
					net.hidden1.biasVarList.set(neuronCount, newVal);
					neuronCount++;
				}
				neuronCount = 0;
				
				// Mutate output neuron bias variance
				newVal = net.outputNeuron.biasVar*Math.exp(r.nextGaussian()/(Math.sqrt(2*Logic.DEFAULTX*Logic.DEFAULTY)))+(r.nextGaussian()/Math.sqrt(2*Math.sqrt(Logic.DEFAULTX*Logic.DEFAULTY)));
				if(newVal < 0.1)
					newVal = 0.1;
				
				net.outputNeuron.biasVar = newVal;
				
				
				
				// Mutate layer 2 bias
				for(Double bias : net.hidden2.biasList)
				{
					newVal = bias+net.hidden2.biasVarList.get(neuronCount)*r.nextGaussian();
					net.hidden2.biasList.set(neuronCount, newVal);
					neuronCount++;
				}
				neuronCount = 0;
				
				// Mutate layer 1 bias
				for(Double bias : net.hidden1.biasList)
				{
					newVal = bias+net.hidden1.biasVarList.get(neuronCount)*r.nextGaussian();
					net.hidden1.biasList.set(neuronCount, newVal);
					neuronCount++;
				}
				neuronCount = 0;
				
				// Mutate output neuron bias
				net.outputNeuron.bias += net.outputNeuron.biasVar*r.nextGaussian();
				
				
				
				// Mutate layer 2 weight variance
				for(Collection<Double> weightVarList : net.hidden2.weightVarSuperList)
					for(double weightVar : weightVarList)
					{
						newVal = weightVar*Math.exp(r.nextGaussian()/(Math.sqrt(2*Logic.DEFAULTX*Logic.DEFAULTY)))+(r.nextGaussian()/Math.sqrt(2*Math.sqrt(Logic.DEFAULTX*Logic.DEFAULTY)));	
						if(newVal < 0.1)
							newVal = 0.1;
						weightVar = newVal;
					}
				
				// Mutate layer 1 weight variance
				for(Collection<Double> weightVarList : net.hidden1.weightVarSuperList)
					for(double weightVar : weightVarList)
					{
						newVal = weightVar*Math.exp(r.nextGaussian()/(Math.sqrt(2*Logic.DEFAULTX*Logic.DEFAULTY)))+(r.nextGaussian()/Math.sqrt(2*Math.sqrt(Logic.DEFAULTX*Logic.DEFAULTY)));	
						if(newVal < 0.1)
							newVal = 0.1;
						weightVar = newVal;
					}
				
				// Mutate output neuron weight variance
				for(Double weightVar : net.outputNeuron.weightVarList)
				{
					newVal = weightVar*Math.exp(r.nextGaussian()/(Math.sqrt(2*Logic.DEFAULTX*Logic.DEFAULTY)))+(r.nextGaussian()/Math.sqrt(2*Math.sqrt(Logic.DEFAULTX*Logic.DEFAULTY)));
					if(newVal < 0.1)
						newVal = 0.1;
					
					net.outputNeuron.weightVarList.set(elemCount, newVal);
					elemCount++;
				}
				elemCount = 0;
				
				
				// Mutate layer 2 weights
				for(Collection<Double> weights : net.hidden2.weightSuperList)
				{
					weightVars.addAll(net.hidden2.weightVarSuperList.get(neuronCount));
					for(double weight : weights)
					{
						// Determine new weight value
						weight = weight + weightVars.get(elemCount)*r.nextGaussian();
						elemCount++;
					}
					weightVars.clear();
					elemCount = 0;
					neuronCount++;
				}
				neuronCount = 0;
				
				// Mutate layer 1 weights
				for(Collection<Double> weights : net.hidden1.weightSuperList)
				{
					weightVars.addAll(net.hidden1.weightVarSuperList.get(neuronCount));
					for(double weight : weights)
					{
						// Determine new weight value
						weight = weight + weightVars.get(elemCount)*r.nextGaussian();
						elemCount++;
					}
					weightVars.clear();
					elemCount = 0;
					neuronCount++;
				}
				neuronCount = 0;
				
				// Mutate output neuron weights
				for(double weightVar : net.outputNeuron.weightVarList)
				{
					// Determine new weight value
					newVal = net.outputNeuron.weightList.get(elemCount) + weightVar*r.nextGaussian();
					net.outputNeuron.weightList.set(elemCount, newVal);
					elemCount++;
				}
				elemCount = 0;
				
				// Finalize
				System.out.println("Mutated network "+networkCount+" ("+((networkCount+1.0)/SURVIVAL)*100+"%)");
				networkCount++;
			//}
			result.addAll(input);
			//input = neuralNets;
		}
		
		networkCount = 0;
		return neuralNets;
	}
	
	private NeuralNetwork fetchFittest(Collection<NeuralNetwork> neuralNets, ArrayList<Integer> neuralScores)
	{
		int bestScore, count;
		NeuralNetwork result;
		result = new NeuralNetwork(logic, (Logic.DEFAULTX*Logic.DEFAULTY), NeuralNetwork.EVOLUTION);
		bestScore = -999999999;
		count = 0;
		
		for(NeuralNetwork net : neuralNets)
			if(neuralScores.get(count) > bestScore)
			{
				bestScore = neuralScores.get(count);
				result = net;
			} 
		
		return result;
	}
}
