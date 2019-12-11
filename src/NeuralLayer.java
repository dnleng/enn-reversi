import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;


public class NeuralLayer
{
	String id;
	NeuralNetwork parent;
	File layerFile;
	LinkedList<Neuron> neuronList;
	ArrayList<Collection<Double>> weightSuperList;
	ArrayList<Collection<Double>> weightVarSuperList;
	ArrayList<Double> biasList;
	ArrayList<Double> biasVarList;
	int width, inputSize;
	
	NeuralLayer(NeuralNetwork neuralNet, String id, int width, int inputSize)
	{
		int count;
		parent = neuralNet;
		biasList = new ArrayList<Double>();
		biasVarList = new ArrayList<Double>();
		weightSuperList = new ArrayList<Collection<Double>>();
		weightVarSuperList = new ArrayList<Collection<Double>>();
		neuronList = new LinkedList<Neuron>();
		this.width = width;
		this.inputSize = inputSize;
		this.id = id;
		layerFile = new File(id+".enn");
		if(neuralNet.type == NeuralNetwork.DEFAULT)
			System.out.println("Building new layer '"+id+"'");
		
		// Check if layer file is present and attempt to fix if required
		// otherwise attempt to load the construction files
		if(parent.type == NeuralNetwork.EVOLUTION)
			fixRandLayer(inputSize);
		else if(!layerFile.exists())
		{
			fixRandLayer(inputSize);
			writeLayer(id, true);
		}
		else
			loadLayer(id);
		
		// Construct a list of neurons acting as a layer
		for(count = 0 ; count < width ; count++)
			neuronList.add(new Neuron(Neuron.HIDDEN, weightSuperList.get(count), 
					weightVarSuperList.get(count), biasList.get(count), 
					biasVarList.get(count)));
		
	}
	
	public LinkedList<Double> compute(LinkedList<Double> inputList)
	{
		LinkedList<Double> result;
		result = new LinkedList<Double>();
		
		// Construct a list of neuron outputs using this layer
		for(Neuron neuron : neuronList)
			result.add(neuron.compute(inputList));
		
		return result;
	}
	
	public void writeLayer(String id, Boolean errorHandle)
	{
    	FileWriter writer;
    	ArrayList<Double> weightList, weightVarList;
    	int count, subCount;
		
   		// Set collections
    	weightList = new ArrayList<Double>();
    	weightVarList = new ArrayList<Double>();
		
		// Pass through collections while attempting to write them to files
		try
		{
			writer = new FileWriter(id+".enn");
			
			// Save layer identifier
			writer.write("neuralLayer "+id+"\n");		
			
			// Save biasList
			for(count = 0 ; count < width ; count++)
				writer.write("biasListElem "+biasList.get(count)+"\n");
			
			// Save biasVarList
			for(count = 0 ; count < width ; count++)
				writer.write("biasVarListElem "+biasVarList.get(count)+"\n");
			
			// Save weightSuperList
			for(count = 0 ; count < width ; count++)
			{
				// Initiate a new subset
				writer.write("weightSuperListElem start\n");
				weightList.addAll(weightSuperList.get(count));
				
				// Write subset
				for(subCount = 0 ; subCount < inputSize ; subCount++)
					writer.write("weightListElem "+weightList.get(subCount)+"\n");
				
				// Clean subset
				weightList.clear();
				writer.write("weightSuperListElem end\n");
			}
			
			// Save weightVarSuperList
			for(count = 0 ; count < width ; count++)
			{
				// Initiate a new subset
				writer.write("weightVarSuperListElem start\n");
				weightVarList.addAll(weightVarSuperList.get(count));
				
				// Write subset
				for(subCount = 0 ; subCount < inputSize ; subCount++)
					writer.write("weightVarListElem "+weightVarList.get(subCount)+"\n");
				
				// Clean subset
				weightVarList.clear();
				writer.write("weightVarSuperListElem end\n");
			}
			
			// Close file
			writer.close();
			
			// Return warning informing user of any error
			if(errorHandle)
				JOptionPane.showMessageDialog( null
						, "Unable locate '"+id+".enn'!\n\nSuccesfully generated replacement file.\n" +
							"Please consider recalibrating the AI."
						, "Missing File"
						, JOptionPane.WARNING_MESSAGE
                   		);
			System.out.println("Saved Neural Layer as "+id+".enn");
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
	
	private void loadLayer(String id)
    {
    	BufferedReader reader;
    	ArrayList<Double> weightList, weightVarList;
		String line, task, store;
		Scanner sc;
		
		// Set collections
		weightList = new ArrayList<Double>();
		weightVarList = new ArrayList<Double>();
		
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
				if(task.equals("neuralLayer"))
					System.out.println("Attempting to load "+sc.next()+".enn");
			
				// Generate bias list
				if(task.equals("biasListElem"))
					biasList.add(Double.parseDouble(sc.next()));
				
				// Generate bias variance list
				if(task.equals("biasVarListElem"))
					biasVarList.add(Double.parseDouble(sc.next()));
				
				// Start or end a new weight super-list
				if(task.equals("weightSuperListElem"))
				{
					store = sc.next();
					if(store.equals("start"))
						weightList.clear();
					else if(store.equals("end"))
						weightSuperList.add(weightList);
				}
				
				// Start or end a new weight variance super-list
				if(task.equals("weightVarSuperListElem"))
				{
					store = sc.next();
					if(store.equals("start"))
						weightVarList.clear();
					else if(store.equals("end"))
						weightVarSuperList.add(weightVarList);
				}
				
				// Generate weight list
				if(task.equals("weightListElem"))
					weightList.add(Double.parseDouble(sc.next()));
				
				// Generate weight variance list
				if(task.equals("weightVarListElem"))
					weightList.add(Double.parseDouble(sc.next()));		
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
				loadLayer(id);
				break;
			case 1:
				// Exit the program
				System.exit(0);
				break;
			case 2:
				// Replace files
				fixRandLayer(inputSize);
				writeLayer(id, false);
				break;
			default:
				// Exit the program by default
				System.exit(0);
				break;
			}
		}
    }
	
	private void fixRandLayer(int inputSize)
	{
		int count;
		
		// Construct biasList
		for(count = 0 ; count < width ; count++)
			biasList.add(3*Math.random());
		
		// Construct biasVarList
		for(count = 0 ; count < width ; count++)
			biasVarList.add(2*Math.random());
		
		// Construct weightSuperList containing weightLists
		for(count = 0 ; count < width ; count++)
			weightSuperList.add(generateRandList(inputSize, -1.0));
		
		// Construct weightVarSuperList containing weightVarLists
		for(count = 0 ; count < width ; count++)
			weightVarSuperList.add(generateRandList(inputSize, 0.0));
			
	}
	
	private Collection<Double> generateRandList(int inputSize, double manip)
	{
		int count;
		ArrayList<Double> list;
		list = new ArrayList<Double>();
		
		// Generate random doubles
		for(count = 0 ; count < inputSize ; count++)
			list.add(2*Math.random()+manip);
		
		return list;
	}
}
