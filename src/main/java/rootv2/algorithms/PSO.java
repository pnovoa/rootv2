package rootv2.algorithms;

import java.util.ArrayList;

import rootv2.Main;
import smile.math.random.UniversalGenerator;

public class PSO extends Algorithm{

	final double psoC = 1.496;
	final double psoW = .729;
	
	public class Particle {

		
		double[] x;
		double fitness;

		double[] v;

		double[] px;
		double pfitness = Double.NEGATIVE_INFINITY;

		public Particle() {
			
			x = new double[problem.dimension];
			px = new double[problem.dimension];
			v = new double[problem.dimension];
			fitness = Double.NEGATIVE_INFINITY;
			pfitness = Double.NEGATIVE_INFINITY;
			
		}
		
		public void copy(Particle p) {

			for (int i = 0; i < x.length; i++) {
				x[i] = p.x[i];
				px[i] = p.px[i];
			}

			fitness = p.fitness;
			pfitness = p.pfitness;
		}

		public void updatePBest() {

			for (int i = 0; i < x.length; i++) {
				px[i] = x[i];
			}

			pfitness = fitness;
		}

	}

	int currEval;
	ArrayList<Particle> swarm;
	Particle gBest;
	
	public PSO() {
		super();
		pSize= 50;
		seed = 112332;
	}
	
	
	@Override
	public void init() {
		
		swarm = new ArrayList<Particle>();
		gBest = new Particle();
		rand = new UniversalGenerator(seed);
		currEval=0;

		for (int i = 0; i < pSize; i++) {
			Particle p = new Particle();
			initParticle(p);
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

			p.fitness = problem.eval(p.x);
			p.pfitness = p.fitness;
			if (p.fitness > gBest.fitness) {
				gBest.copy(p);
			}

		}
		
		return p;
	}
	
	@Override
	public void initWithMemory() {
		
		gBest.fitness = problem.eval(gBest.x);
		gBest.updatePBest();
		
		swarm.get(0).copy(gBest);
		swarm.get(0).fitness = problem.eval(swarm.get(0).x);
		swarm.get(0).updatePBest();
		
		
		for (int i = 1; i < pSize; i++) {

			Particle p = swarm.get(i);

			initParticle(p);
		}
	}
	
	
	
	@Override
	public void iterate() {

		for (int i = 0; i < pSize; i++) {

			Particle p = swarm.get(i);
			iterateParticle(p);
		}
	}
	
	
	
	@Override
	public String instanceName() {
		
		return "PSO";
	}
	
	


	@Override
	public String factors() {
		return "\"PSO\"";
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
		p.fitness = problem.eval(p.x);
		
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
	
	protected double eval(double[] x) {
		currEval++;
		return problem.eval(x);
	}
	
	

}
