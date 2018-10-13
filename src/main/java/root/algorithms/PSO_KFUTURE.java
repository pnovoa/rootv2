package root.algorithms;

import java.util.ArrayList;
import java.util.Locale;

import root.Main;
import root.RMPBI;
import smile.math.random.UniversalGenerator;

public class PSO_KFUTURE extends Algorithm {

	//Study parameters
	
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
	

	
	int currEnviroment=0;
	ArrayList<Particle> swarm;
	Particle gBest;
	public String rbfName;
	
	public PSO_KFUTURE() {
		
		super();
		pSize= 50;
		seed = 112332;
	}
	
	
	
	
	@Override
	public String instanceName() {
		return String.format(Locale.US, "PSO-KFUTURE");
	}

	


	@Override
	public String factors() {
		return String.format(Locale.US, "\"%s\",%d,%.3f,%d","kfuture",0,0.,0);
	}




	@Override
	public void init() {
		
		currEnviroment = 0;
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
			
		}
		
	}
	
	
	
	@Override
	public void iterate() {

		for (int i = 0; i < pSize; i++) {

			Particle p = swarm.get(i);
			iterateParticle(p);
			
		
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
		
		double result = problem.trueEval(x);
				
		return result;
	}
	
	

}
