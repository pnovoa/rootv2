package rootv2.algorithms;

import rootv2.RMPBI;
import smile.math.random.UniversalGenerator;

public abstract class Algorithm{
	
	public abstract String instanceName();
	
	public abstract String factors();
	
	public RMPBI problem;
	
	public UniversalGenerator rand;
	
	public int seed;
	
	public int pSize;
	
	public abstract void init();
	
	public abstract void initWithMemory();
	
	public abstract void iterate();
	
	public abstract double[] bestSolution();
	
	
}
