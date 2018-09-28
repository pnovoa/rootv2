/**
 * 
 */
package rootv2.problems;

/**
 * @author pavel
 *
 */
public interface ProblemDefinition<SolutionType, FunctionType> {

	public SearchSpace<SolutionType> getSearchSpace();
	
	public ObjectiveFunction<SolutionType, FunctionType> getObjectiveFunction();
	
}
