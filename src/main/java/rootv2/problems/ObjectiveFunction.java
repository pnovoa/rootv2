package rootv2.problems;

/**
 * @author pavel
 *
 */
public interface ObjectiveFunction<SolutionType, FunctionType> {

	public FunctionType evaluate(SolutionType solution);
}
