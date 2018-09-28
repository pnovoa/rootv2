package rootv2.algorithms;

import java.util.Locale;

public class IDEAL_ALG extends Algorithm {

	
	
	public IDEAL_ALG() {
		super();
		pSize=50;
	}

	@Override
	public String instanceName() {
		return String.format(Locale.US, "IDEAL");
	}

	@Override
	public String factors() {
		return String.format(Locale.US, "\"%s\",%d,%.3f,%d","optimum",0,0.,0);
	}

	@Override
	public void init() {
		

	}

	@Override
	public void initWithMemory() {
		// TODO Auto-generated method stub

	}

	@Override
	public void iterate() {
		// TODO Auto-generated method stub

	}

	@Override
	public double[] bestSolution() {
		return problem.findBestSolution(problem.currEnvironment);
	}

}
