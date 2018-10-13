package root.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;

import root.Main;
import root.RMPBI;
import smile.clustering.KMeans;
import smile.interpolation.KrigingInterpolation;
import smile.interpolation.ShepardInterpolation;
import smile.math.distance.EuclideanDistance;
import smile.math.kernel.GaussianKernel;
import smile.math.kernel.LaplacianKernel;
import smile.math.kernel.ThinPlateSplineKernel;
import smile.math.random.UniversalGenerator;
import smile.math.rbf.GaussianRadialBasis;
import smile.math.rbf.ThinPlateRadialBasis;
import smile.regression.RBFNetwork;
import smile.regression.SVR;

public class PSOINT_AR extends Algorithm {

	public abstract class Interpolator {

		public Interpolator(double x[][], double y[]) {

		}

		public abstract double eval(double x[]);

	}

	public class KrigingModel extends Interpolator {

		KrigingInterpolation model;

		public KrigingModel(double _x[][], double _y[]) {

			super(_x, _y);

			model = new KrigingInterpolation(_x, _y);
		}

		@Override
		public double eval(double[] x) {
			return model.interpolate(x);
		}
	}

	public class RBFNGaussianModel extends Interpolator {

		RBFNetwork<double[]> model;

		public RBFNGaussianModel(double _x[][], double _y[]) {

			super(_x, _y);

			KMeans kmodel = new KMeans(_x, K);

			RBFNetwork.Trainer<double[]> trainer = new RBFNetwork.Trainer<double[]>(new EuclideanDistance());
			trainer.setRBF(new GaussianRadialBasis(5.0), K);
			model = trainer.train(_x, _y, kmodel.centroids());
		}

		@Override
		public double eval(double[] x) {
			return model.predict(x);
		}
	}

	public class RBFNThinPlateModel extends Interpolator {

		RBFNetwork<double[]> model;

		public RBFNThinPlateModel(double _x[][], double _y[]) {

			super(_x, _y);

			KMeans kmodel = new KMeans(_x, K);

			RBFNetwork.Trainer<double[]> trainer = new RBFNetwork.Trainer<double[]>(new EuclideanDistance());
			trainer.setRBF(new ThinPlateRadialBasis(5.0), K);
			model = trainer.train(_x, _y, kmodel.centroids());
		}

		@Override
		public double eval(double[] x) {
			return model.predict(x);
		}
	}

	public class ShepardModel extends Interpolator {

		ShepardInterpolation model;

		public ShepardModel(double _x[][], double _y[]) {

			super(_x, _y);

			model = new ShepardInterpolation(_x, _y);
		}

		@Override
		public double eval(double[] x) {
			return model.interpolate(x);
		}
	}

	public class SVRGaussianKernelModel extends Interpolator {

		SVR<double[]> model;

		public SVRGaussianKernelModel(double _x[][], double _y[]) {

			super(_x, _y);

			// KMeans kmodel = new KMeans(_x, K);

			SVR.Trainer<double[]> trainer = new SVR.Trainer<double[]>(new GaussianKernel(3.0), 0.05, 1.0);
			trainer.setTolerance(0.5);
			model = trainer.train(_x, _y);
		}

		@Override
		public double eval(double[] x) {
			return model.predict(x);
		}
	}

	public class SVRThinPlateSplineKernelModel extends Interpolator {

		SVR<double[]> model;

		public SVRThinPlateSplineKernelModel(double _x[][], double _y[]) {

			super(_x, _y);

			// KMeans kmodel = new KMeans(_x, K);

			SVR.Trainer<double[]> trainer = new SVR.Trainer<double[]>(new ThinPlateSplineKernel(3.0), 0.05, 1.0);
			trainer.setTolerance(0.5);
			model = trainer.train(_x, _y);
		}

		@Override
		public double eval(double[] x) {
			return model.predict(x);
		}
	}

	public class SVRLaplacianKernelModel extends Interpolator {

		SVR<double[]> model;

		public SVRLaplacianKernelModel(double _x[][], double _y[]) {

			super(_x, _y);

			// KMeans kmodel = new KMeans(_x, K);

			SVR.Trainer<double[]> trainer = new SVR.Trainer<double[]>(new LaplacianKernel(3.0), 0.05, 1.0);
			trainer.setTolerance(0.5);
			model = trainer.train(_x, _y);
		}

		@Override
		public double eval(double[] x) {
			return model.predict(x);
		}
	}

	// Study parameters

	public int K = 50;
	public int arOrder = 4;
	public String modelName;

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

	double iterAppError = 0.;
	double iterPredError = 0.;
	int iterEval = 0;

	double envBuildingTime;
	double iterEvalPastTime;
	// int iterEvalPT;

	ArrayList<Interpolator> models;
	double data[][];
	double dataY[];
	int dataIndex = 0;

	int currEnviroment = 0;
	ArrayList<Particle> swarm;
	Particle gBest;

	public PSOINT_AR() {

		super();
		pSize = 50;
		seed = 112332;
	}

	@Override
	public String instanceName() {
		return String.format(Locale.US, "PSOINT_%s_AR", modelName);
	}

	@Override
	public String factors() {
		return String.format(Locale.US, "\"%s\"", modelName);
	}

	@Override
	public void init() {

		models = new ArrayList<Interpolator>();

		// data = new double[problem.computationalBudget][problem.dimension];
		data = new double[pSize][problem.dimension];
		dataY = new double[data.length];
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
			data[dataIndex] = Arrays.copyOf(p.x, p.x.length);
			dataY[dataIndex] = p.currFitness;
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

		buildApproximationModel();

		currEnviroment++;
		dataIndex = 0;
		iterEval = 0;

		gBest.currFitness = problem.eval(gBest.x);
		gBest.fitness = eval(swarm.get(0));
		gBest.updatePBest();

		swarm.get(0).copy(gBest);

		data[dataIndex] = Arrays.copyOf(swarm.get(0).x, swarm.get(0).x.length);
		dataY[dataIndex] = swarm.get(0).currFitness;

		for (int i = 1; i < pSize; i++) {

			Particle p = swarm.get(i);
			initParticle(p);

			p.currFitness = problem.eval(p.x);
			p.fitness = eval(p);
			p.updatePBest();

			if (p.fitness > gBest.fitness) {
				gBest.copy(p);
			}

			data[dataIndex] = Arrays.copyOf(p.x, p.x.length);
			dataY[dataIndex] = p.currFitness;
			dataIndex++;
		}

	}

	@Override
	public void iterate() {

		for (int i = 0; i < pSize; i++) {

			Particle p = swarm.get(i);
			iterateParticle(p);

			// data[dataIndex] = Arrays.copyOf(p.x, p.x.length);
			// dataY[dataIndex] = p.fitness;
			// dataIndex++;
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

		if (p.fitness > p.pfitness) {
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

		double perror = 0.;
		for (int i = 0; i < predictedValues.length; i++) {

			result += predictedValues[i];

			RMPBI.Environment env = problem.environments.get(problem.currEnvironment + i + 1);

			double trueValue = problem.evalEnv(env, x);

			perror += predictedValues[i] - trueValue;

			// if(Double.isNaN(error))
			// System.out.println("FUTURE ERROR:\t"+ trueValue + "\t" + predictedValues[i]);
			// System.out.println("FUTURE ERROR:\t"+error);

		}

		result = result / (double) problem.timeWindows;

		iterPredError += perror / (double) predictedValues.length;

		return result;
	}

	protected double[] evalInThePast(double x[]) {

		double result[] = new double[problem.learningPeriod];
		int past = currEnviroment - problem.learningPeriod;

		double indError = 0.;
		double indEvalTime = 0.;
		iterEval = iterEval + 1;

		for (int i = 0; i < problem.learningPeriod; i++) {

			long startTime = System.currentTimeMillis();

			result[i] = models.get(i).eval(x);

			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			indEvalTime += elapsedTime;

			RMPBI.Environment env = problem.environments.get(past + i);
			//
			double trueValue = problem.evalEnv(env, x);

			double error = result[i] - trueValue;

			if (Double.isNaN(error)) {
				System.out.println(result[i] + "%t" + trueValue);
			}

			indError += result[i] - trueValue;
			//
			// System.out.println("PAST ERROR:\t"+Math.abs(trueValue - result[i]));

		}

		iterAppError += indError / (double) problem.learningPeriod;
		iterEvalPastTime += indEvalTime / (double) problem.learningPeriod;

		return result;
	}

	protected double[] predictValues(double[] pastFitness, double currFitness) {

		double dataModel[] = Arrays.copyOf(pastFitness, pastFitness.length + 1);
		dataModel[dataModel.length - 1] = currFitness;

		ArimaParams arp = new ArimaParams(arOrder, 0, 0, 0, 0, 0, 0);

		ForecastResult forecastResult = Arima.forecast_arima(dataModel, problem.timeWindows - 1, arp);

		return forecastResult.getForecast();

	}

	@Override
	public double bestFitness() {

		return gBest.fitness;
	}

	@Override
	public double getApproximationError() {

		double result = iterAppError / (double) iterEval;
		iterAppError = 0.;
		return result;
	}

	@Override
	public double getEvalPastTime() {

		double result = iterEvalPastTime / (double) iterEval;
		iterEvalPastTime = 0.;
		return result;
	}

	@Override
	public double getPredictionError() {

		double result = iterPredError / (double) iterEval;
		iterAppError = 0.;
		return result;
	}

	@Override
	public double getBuildingModelTime() {
		return this.envBuildingTime;
	}

	protected void buildApproximationModel() {

		if (currEnviroment > problem.learningPeriod) {

			models.remove(0);
		}

		// KMeans kmModel = new KMeans(data, K);

		Interpolator interModel = null;

		long startTime = System.currentTimeMillis();

		if (modelName.compareTo("kriging") == 0) {
			startTime = System.currentTimeMillis();
			interModel = new KrigingModel(data, dataY);
		} else if (modelName.compareTo("rbf_gaussian") == 0) {
			startTime = System.currentTimeMillis();
			interModel = new RBFNGaussianModel(data, dataY);

		} else if (modelName.compareTo("rbf_thinplate") == 0) {
			startTime = System.currentTimeMillis();
			interModel = new RBFNThinPlateModel(data, dataY);
		} else if (modelName.compareTo("shepard") == 0) {
			startTime = System.currentTimeMillis();
			interModel = new ShepardModel(data, dataY);
		} else if (modelName.compareTo("svr_gaussian_kernel") == 0) {
			startTime = System.currentTimeMillis();
			interModel = new SVRGaussianKernelModel(data, dataY);
		} else if (modelName.compareTo("svr_thinplatespline_kernel") == 0) {
			startTime = System.currentTimeMillis();
			interModel = new SVRThinPlateSplineKernelModel(data, dataY);
		} else if (modelName.compareTo("svr_laplacian_kernel") == 0) {
			startTime = System.currentTimeMillis();
			interModel = new SVRLaplacianKernelModel(data, dataY);
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		envBuildingTime = elapsedTime;

		models.add(interModel);
	}

}
