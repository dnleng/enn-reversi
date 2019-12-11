import java.util.*;


public class Neuron 
{
	ArrayList<Double> weightList, weightVarList;
	double bias, biasVar;
	int type;
	final static int HIDDEN = 0;
	final static int OUTPUT = 1;
	
	
	Neuron(int type, Collection<Double> weights, Collection<Double> weightVars, double bias, double biasVar)
	{	
		// Set neuron type
		this.type = type;
		
		// Construct list of weights
    	this.weightList = new ArrayList<Double>();
    	for(Double weight : weights)
    		weightList.add(weight);
    	
		// Construct list of weight variances
    	this.weightVarList = new ArrayList<Double>();
    	for(Double weightVar : weightVars)
    		weightVarList.add(weightVar);
    	
		// Set bias term and its variance
    	this.bias = bias;
    	this.biasVar = biasVar;
	}
	

	public double compute(Collection<Double> inputs)
	{
		ArrayList<Double> inputList;
		int count;
		double output, summation;
		
		// Set default values
		count = 0;
		summation = 0;
		
		// Construct list of input values
    	inputList = new ArrayList<Double>();
    	for(Double input : inputs)
    		inputList.add(input);
		
		// Run through the lists
		for(Double input : inputList)
		{
			summation += (input * weightList.get(count));
			count++;
		}
		
		// Determine final output value based on neuron type
		switch(type)
		{
		case HIDDEN:
			output = Math.tanh(summation + bias);
			break;
		case OUTPUT:
			output = summation + bias;
			break;
		default:
			output = Math.tanh(summation + bias);
			break;
		}
		return output;
	}
}
