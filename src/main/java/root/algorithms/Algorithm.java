package root.algorithms;

import root.RMPBI;
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
	
	public double getApproximationError() {
		return 0.;
	}
	
	public double getPredictionError() {
		return 0.;
	}
	
	public double getBuildingModelTime() {
		return 0.;
	}
	
	public double getEvalPastTime() {
		return 0.;
	}
	
	public double bestFitness() {
		return 0.;
	}
}
