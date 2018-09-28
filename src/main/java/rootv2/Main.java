/**
 * 
 */
package rootv2;

/**
 * @author pavel
 *
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import rootv2.algorithms.Algorithm;
import rootv2.algorithms.IDEAL_ALG;
import rootv2.algorithms.PSO;
import rootv2.algorithms.PSOHE;
import rootv2.algorithms.PSORBF_AR;
import rootv2.algorithms.PSOAR_KPAST;
import rootv2.algorithms.PSO_KFUTURE;

public class Main {

public static int TIME_WINDOWS[] = { 2, 6, 10 };
public static int CHANGE_TYPE[] = { 1, 2, 3, 4, 5, 6 };
	
	
//	public static int TIME_WINDOWS[] = { 2, 6, 10};
//	public static int CHANGE_TYPE[] = {2};

	static int numRuns = 20;

	static double robutness[] = new double[numRuns];

	static final String change_type[] = {
		"small_step", "large_step", "random", "chaotic", "recurrent", "recurrent_with_noise"
	};
	
	public static void main(String[] args) {

		// System.out.println(args[0]);
		Algorithm alg;
		if (args[0].trim().compareTo("PSO") == 0) {
			alg = new PSO();

		} else if (args[0].trim().compareTo("PSORBF_AR") == 0) {
			PSORBF_AR algpso = new PSORBF_AR();
			
			algpso.rbfName = args[1];
			algpso.K = Integer.parseInt(args[2]);
			algpso.scalingFactor = Double.parseDouble(args[3]);
			algpso.arOrder = Integer.parseInt(args[4]);
			
			alg = algpso;
			
		} else if(args[0].trim().compareTo("PSOHE") == 0) {
			
			PSOHE algpso = new PSOHE();
			
			algpso.K = Integer.parseInt(args[1]);
			algpso.scalingFactor = Double.parseDouble(args[2]);
			algpso.arOrder = Integer.parseInt(args[3]);
			
			alg = algpso;
		} else if(args[0].trim().compareTo("PSO_AR_KPAST") == 0) {
		
			PSOAR_KPAST algpso = new PSOAR_KPAST();
			
			algpso.arOrder = Integer.parseInt(args[1]);
			
			alg = algpso;
		} else if(args[0].trim().compareTo("IDEAL_ALG") == 0) {

			alg = new IDEAL_ALG();
	
		} else {

			alg = new PSO_KFUTURE();
		}

		BufferedWriter writer = null;
		try {
			String timeLog = alg.instanceName() + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			//PrintStream out = new PrintStream(new FileOutputStream(timeLog+".err"));
			//System.setErr(out);
			
			
			File logFile = new File(timeLog + ".out");

			writer = new BufferedWriter(new FileWriter(logFile));

			RMPBI problem = new RMPBI();
			alg.problem = problem;

			for (int tw = 0; tw < TIME_WINDOWS.length; tw++) {

				for (int ct = 0; ct < CHANGE_TYPE.length; ct++) {

					for (int r = 0; r < numRuns; r++) {
						// alg = new PSORBF_AR();

						problem.timeWindows = TIME_WINDOWS[tw];
						problem.changeType = CHANGE_TYPE[ct];

						alg.seed += r;
						problem.seed += r;

						robutness[r] = run(alg);
						String print = String.format(Locale.US, "%s,%d,\"%s\",%.6e%n", alg.factors(), TIME_WINDOWS[tw], change_type[CHANGE_TYPE[ct]-1], robutness[r]);
						writer.write(print);
						writer.flush();
						//System.out.println(print);
					}

					

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}

	}

	private static double run(Algorithm alg) {

		double runRobutness = 0.;

		alg.problem.init();
		alg.init();

		int maxIterPerChange = alg.problem.computationalBudget / alg.pSize;
		
		for (int iter = 1; iter < maxIterPerChange; iter++) {

			alg.iterate();
		}
		//runRobutness += alg.problem.trueEval(alg.bestSolution());

		for (int timeStep = 1; timeStep < alg.problem.learningPeriod; timeStep++) {
			
			alg.problem.change();
			alg.initWithMemory();
			for (int iter = 1; iter < maxIterPerChange; iter++) {
				alg.iterate();
			}			
		}
		
		int end = alg.problem.numChanges - (alg.problem.timeWindows-1);
		int iii = 0;
		for (int timeStep = alg.problem.learningPeriod; timeStep < end ; timeStep++) {

			alg.problem.change();
			
			alg.initWithMemory();

			for (int iter = 1; iter < maxIterPerChange; iter++) {

				alg.iterate();
			}
			
			double robust = alg.problem.trueEval(alg.bestSolution());
			iii++;
			runRobutness += robust; 
		}

		return runRobutness / (double) iii;
	}

	public void testRMPBI() {

		RMPBI prob = new RMPBI();
		prob.init();
		Random rand = new Random(112);
		int pSize = 100;

		double pop[][] = new double[pSize][prob.dimension];

		for (int c = 0; c < 100; c++) {

			for (int i = 0; i < pop.length; i++) {

				for (int j = 0; j < prob.dimension; j++) {

					pop[i][j] = prob.minCoord - (prob.maxCoord - prob.minCoord) * rand.nextDouble();

				}

				System.out.println(prob.currEnvironment + "\t" + prob.eval(pop[i]) + "\t" + prob.trueEval(pop[i]));
			}
			prob.change();
		}
	}

	public static double clampValue(double val, double minV, double maxV) {

		double result = Math.max(val, minV);
		result = Math.min(result, maxV);

		return result;
	}

}
