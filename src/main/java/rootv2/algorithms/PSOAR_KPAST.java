package rootv2.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;

import rootv2.Main;
import rootv2.RMPBI;
import smile.math.random.UniversalGenerator;

public class PSOAR_KPAST extends Algorithm {

	//Study parameters
	
	
	public int arOrder = 4;
	
	final double psoC = 1.496;
	final double psoW = .729;
	
	public class Particle {

		
		double[] x;
		double fitness;
		double currFitness;

		double[] v;

		double[] px;
		double pfitness = Double.NEGATIVE_INFINITY;

		public Particle() {
			
			x = new double[problem.dimension];
			px = new double[problem.dimension];
			v = new double[problem.dimension];
			fitness = Double.NEGATIVE_INFINITY;
			pfitness = Double.NEGATIVE_INFINITY;
			currFitness = Double.NEGATIVE_INFINITY;
			
		}
		
		public void copy(Particle p) {

			for (int i = 0; i < x.length; i++) {
				x[i] = p.x[i];
				px[i] = p.px[i];
			}

			fitness = p.fitness;
			pfitness = p.pfitness;
			currFitness = p.currFitness;
		}

		public void updatePBest() {

			for (int i = 0; i < x.length; i++) {
				px[i] = x[i];
			}

			pfitness = fitness;
		}

	}
	
	//RBFNetwork.Trainer<double[]> trainer = new RBFNetwork.Trainer<double[]>(new EuclideanDistance());
	
	
	int dataIndex=0;
	
	
	int currEnviroment=0;
	ArrayList<Particle> swarm;
	Particle gBest;
	public String rbfName;
	
	public PSOAR_KPAST() {
		
		super();
		pSize= 50;
		seed = 112332;
	}
	
	
	
	
	@Override
	public String instanceName() {
		return String.format(Locale.US, "PSO_AR-%d_KPAST",arOrder);
	}

	


	@Override
	public String factors() {
		return String.format(Locale.US, "\"%s\",%d,%.3f,%d","pso_ar_kp",0,0.,arOrder);
	}




	@Override
	public void init() {
		
		
		
		currEnviroment = 0;
		dataIndex = 0;
		swarm = new ArrayList<Particle>();
		gBest = new Particle();
		rand = new UniversalGenerator(seed);

		for (int i = 0; i < pSize; i++) {
			Particle p = new Particle();
			initParticle(p);
			
			p.fitness = problem.eval(p.x);
			p.currFitness = p.fitness;
			p.pfitness = p.fitness;
			if (p.fitness > gBest.fitness) {
				gBest.copy(p);
			}
			
			swarm.add(p);
			dataIndex++;
		}
		
		
	}
	
	
	
	protected Particle initParticle(Particle p) {
		
		for (int i = 0; i < pSize; i++) {

			p.x = new double[problem.dimension];
			p.v = new double[problem.dimension];
			p.px = new double[problem.dimension];

			for (int d = 0; d < problem.dimension; d++) {

				p.x[d] = problem.minCoord + (problem.maxCoord - problem.minCoord) * rand.nextDouble();
				p.px[d] = p.x[d];
				p.v[d] = problem.minCoord + (problem.maxCoord - problem.minCoord) * rand.nextDouble();
				p.v[d] = 0.5 * (p.x[d] - p.v[d]);
			}

		}
		
		return p;
	}
	
	@Override
	public void initWithMemory() {
		
		currEnviroment++;
		dataIndex = 0;
		
		
		gBest.currFitness = problem.eval(gBest.x);
		gBest.fitness = eval(swarm.get(0));
		gBest.updatePBest();
		
		swarm.get(0).copy(gBest);
		
		for (int i = 1; i < pSize; i++) {

			Particle p = swarm.get(i);
			initParticle(p);
			
			p.currFitness = problem.eval(p.x);
			p.fitness = eval(p);
			p.updatePBest();
			
			if (p.fitness > gBest.fitness) {
				gBest.copy(p);
			}
			
			dataIndex++;
		}
		
	}
	
	
	
	@Override
	public void iterate() {

		for (int i = 0; i < pSize; i++) {

			Particle p = swarm.get(i);
			iterateParticle(p);
			
			//data[dataIndex] = Arrays.copyOf(p.x, p.x.length);
			//dataY[dataIndex] = p.fitness;
			//dataIndex++;
		}
	}
	
	protected void iterateParticle(Particle p) {
		
		for (int d = 0; d < problem.dimension; d++) {

			double social = this.rand.nextDouble() * psoC * (gBest.px[d] - p.x[d]);

			double cognition = this.rand.nextDouble() * psoC * (p.px[d] - p.x[d]);

			double v = psoW * p.v[d] + social + cognition;

			double x = p.x[d] + v;

			x = Main.clampValue(x, problem.minCoord, problem.maxCoord);

			if (x == problem.minCoord || x == problem.maxCoord) {
				v = 0.;
			}

			p.x[d] = x;
			p.v[d] = v;
		}
		
		// evaluation
		p.currFitness = problem.eval(p.x);
		p.fitness = eval(p);
		
		if(p.fitness>p.pfitness) {
			p.updatePBest();
			
			if (p.pfitness > gBest.fitness) {
				gBest.copy(p);
			}
		}
	}
	
	
	@Override
	public double[] bestSolution() {
		return gBest.x;
	}
	
	protected double eval(Particle p) {
		
		double[] x = p.x;
		
		if (currEnviroment < problem.learningPeriod) {
			
			return p.currFitness;
		}
		
		double currentFitness = p.currFitness;
		
		double pastDada[] = evalInThePast(x);
	
		double[] predictedValues = predictValues(pastDada, currentFitness);
		
		double result = currentFitness;
		
		
		for(int i=0; i<predictedValues.length; i++) {
			
			result += predictedValues[i];
			
			//RMPBI.Environment env = problem.environments.get(problem.currEnviroment+i+1);
			
			//double trueValue = problem.evalEnv(env, x);
			
			
			//double error = Math.abs(trueValue - predictedValues[i]);
			
//			if(Double.isNaN(error))
//				System.out.println("FUTURE ERROR:\t"+ trueValue + "\t" + predictedValues[i]);
//			System.out.println("FUTURE ERROR:\t"+error);
			
		}
		
		result = result/(double)problem.timeWindows;
		
		
		
		return result;
	}
	
	protected double[] evalInThePast(double x[]) {
		
		double result[] = new double[problem.learningPeriod];
		int past = currEnviroment-problem.learningPeriod;
		
		for (int i = 0; i < problem.learningPeriod; i++) {
			
//			result[i] = rbfNetworks.get(i).predict(x);
			
			RMPBI.Environment env = problem.environments.get(past+i);
//			
			result[i] = problem.evalEnv(env, x);
			
//			double trueValue = problem.evalEnv(env, x);
//			
//			System.out.println("PAST ERROR:\t"+Math.abs(trueValue - result[i]));
			
		}
		
		return result;
	}
	
	
	protected double[] predictValues(double[] pastFitness, double currFitness) {
		
		
		double dataModel[] = Arrays.copyOf(pastFitness, pastFitness.length + 1);
		dataModel[dataModel.length-1] = currFitness;
		
		ArimaParams arp = new ArimaParams(arOrder, 0,0,0,0,0,0);
	    
		ForecastResult forecastResult = Arima.forecast_arima(dataModel, problem.timeWindows-1, arp);
		
		return forecastResult.getForecast();
		
	}
	
	

}
