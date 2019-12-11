import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

public class NeuralNetwork
{
	Logic logic;
	File neuronFile;
	Neuron outputNeuron;
	NeuralLayer hidden1, hidden2;
	ArrayList<Double> weightList, weightVarList;
	ArrayList<String> layers;
	String id;
	double bias, biasVar;
	int inputSize, type;
	
	final private static int DEFAULTX = 6;
	final private static int DEFAULTY = 6;
	final public static int DEFAULT = 1;
	final public static int EVOLUTION = 2;
	
	NeuralNetwork(Logic logic, int inputSize, int type)
	{
		this.logic = logic;
		this.inputSize = inputSize;
		this.type = type;
		
		this.id = "outputNeuron";
		this.neuronFile = new File(id+".enn");
		weightList = new ArrayList<Double>();
		weightVarList = new ArrayList<Double>();
		
		// Construct hidden layers
		if(this.type == DEFAULT)
			System.out.println("---\nAttempting to construct Neural Network");
		hidden2 = new NeuralLayer(this, "hiddenLayer2", 40, inputSize);
		hidden1 = new NeuralLayer(this, "hiddenLayer1", 10, hidden2.width);
		
		// Set output neuron
		if(this.type == DEFAULT)
			System.out.println("Constructing output neuron.");
		if(this.type == EVOLUTION)
			fixRandNeuron(hidden1.width + inputSize);
		else if(!neuronFile.exists() || this.type == EVOLUTION)
		{
			fixRandNeuron(hidden1.width + inputSize);
			writeNeuron(id, true);
		}
		else
			loadNeuron(id);
		
		// Construct output neuron
		outputNeuron = new Neuron(Neuron.OUTPUT, weightList, weightVarList, bias, biasVar);
		if(this.type == DEFAULT)
			System.out.println("Neural Network succesfully constructed!\n---");
	}
	
	public double compute(int[][] board)
	{
		LinkedList<Double> input, inputLayer1, inputLayer2, dual;
		double output;
		
		// Move board value through the Neural Network passing through each layer
		input = evaluate(board);
		inputLayer2 = hidden2.compute(input);
		inputLayer1 = hidden1.compute(inputLayer2);
		
		// Construct dual-connected output neuron
		dual = fuse(inputLayer1, input);
		output = outputNeuron.compute(dual);
		
		// Return final board valuation
		return output;
	}
	
	private LinkedList<Double> evaluate(int[][] input)
	{
		int xCount, yCount;
		LinkedList<Double> result;
		result = new LinkedList<Double>();
		
		// Evaluate board to current player
		for(yCount = 0; yCount < DEFAULTY ; yCount++)
			for(xCount = 0; xCount < DEFAULTX ; xCount++)
				if(input[xCount][yCount] == logic.player)
					result.add(1.0);
				else if(input[xCount][yCount] == 0)
					result.add(0.0);
				else
					result.add(-1.0);
		
		return result;
	}
	
	private LinkedList<Double> fuse(LinkedList<Double> a, LinkedList<Double> b)
	{
		LinkedList<Double> result;
		result = new LinkedList<Double>();
		
		for(Double element : a)
			result.add(element);
		
		for(Double element : b)
			result.add(element);
		
		return result;
	}
	
	private void fixRandNeuron(int inputSize)
	{
		int count;
		double temp;
		bias = 3*Math.random();
		
		temp = Math.random();
		if(temp < 0.1)
			temp = 0.1;
		biasVar = temp;
		
		// Construct weightSuperList containing weightLists
		for(count = 0 ; count < inputSize ; count++)
			weightList.add(2*Math.random()-1);
		
		// Construct weightVarSuperList containing weightVarLists
		for(count = 0 ; count < inputSize ; count++)
		{
			temp = 2*Math.random();
			if(temp < 0.1)
				temp = 0.1;
			weightVarList.add(temp);
		}
			
	}
	
	public void writeNeuron(String id, Boolean error)
	{
    	FileWriter writer;
    	int count;
		
		// Pass through collections while attempting to write them to files
		try
		{
			writer = new FileWriter(id+".enn");
			
			// Save layer identifier
			writer.write("neuron "+id+"\n");		
			
			// Save bias
			writer.write("bias "+bias+"\n");
			
			// Save biasVar
			writer.write("biasVar "+biasVar+"\n");
			
			// Save weightList
			for(count = 0 ; count < hidden1.width ; count++)
				writer.write("hiddenWeightListElem "+weightList.get(count)+"\n");
			
			for(count = 0 ; count < inputSize ; count++)
				writer.write("inputWeightListElem "+weightList.get(hidden1.width+count)+"\n");
			
			// Save weightVarList
			for(count = 0 ; count < hidden1.width ; count++)
				writer.write("hiddenWeightVarListElem "+weightVarList.get(count)+"\n");
			
			for(count = 0 ; count < inputSize ; count++)
				writer.write("inputWeightVarListElem "+weightVarList.get(hidden1.width+count)+"\n");
			
			// Close file
			writer.close();
			
			// Return warning informing user of any error
			if(error)
				JOptionPane.showMessageDialog( null
						, "Unable locate '"+id+".enn'!\n\nSuccesfully generated replacement file.\n" +
							"Please consider recalibrating the AI."
						, "Missing File"
						, JOptionPane.WARNING_MESSAGE
                   		);
			System.out.println("Saved Neuron as "+id+".enn");
		}
		catch(IOException e)
		{
			// Failed to generate replacement file, inform user and shut down
			JOptionPane.showMessageDialog( null
					, "Unable locate '"+id+".enn'!\n\nFailed to generate replacement file.\n" +
							"Please check your harddrive space / access rights and try again."
					, "Fatal Error"
					, JOptionPane.ERROR_MESSAGE
                   	);
			System.out.println(e.toString());
			System.out.println("Fatal Error; Shutting down");
			System.exit(0);
		}
	}
	
	private void loadNeuron(String id)
    {
    	BufferedReader reader;
		String line, task;
		Scanner sc;
		
		// Attempt to load file
		try
		{
			// Prepare file and clear game field
			reader = new BufferedReader(new FileReader(id+".enn"));
			
			while((line = reader.readLine()) != null)
			{
				sc = new Scanner(line);
				task = sc.next();
			
				// Determine current file
				if(task.equals("neuron"))
					System.out.println("Attempting to load "+sc.next()+".enn");
			
				// Generate bias element
				if(task.equals("bias"))
					bias = Double.parseDouble(sc.next());
				
				// Generate bias variance element
				if(task.equals("biasVar"))
					biasVar = Double.parseDouble(sc.next());
				
				// Generate weight list
				if(task.equals("hiddenWeightListElem") || task.equals("inputWeightListElem"))
					weightList.add(Double.parseDouble(sc.next()));
				
				// Generate weight variance list
				if(task.equals("hiddenWeightVarListElem") || task.equals("inputWeightVarListElem"))
					weightVarList.add(Double.parseDouble(sc.next()));	
			}
			
			// Close file
			reader.close();
		}
		catch(IOException e)
		{
			int choice;
			
			// Warn user of failure to load files
			Object[] options = {"Retry",
			                    "Abort",
			                    "Reset Files"};
			choice = JOptionPane.showOptionDialog(null,
				"Unable properly load '"+id+".enn'!\n\nPlease choose next action.",
			    "Loading Failure",
			    JOptionPane.YES_NO_CANCEL_OPTION,
			    JOptionPane.WARNING_MESSAGE,
			    null,
			    options,
			    options[2]);
			
			System.out.println("Error while attempting to load "+id+".enn");
			
			switch(choice)
			{
			case 0:
				// Attempt to reload files
				loadNeuron(id);
				break;
			case 1:
				// Exit the program
				System.exit(0);
				break;
			case 2:
				// Replace files
				fixRandNeuron(inputSize);
				writeNeuron(id, false);
				break;
			default:
				// Exit the program by default
				System.exit(0);
				break;
			}
		}
    }
}
