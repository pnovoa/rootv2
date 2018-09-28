/**
 * 
 */
package rootv2.problems;

/**
 * @author pavel
 *
 */
public interface TestProblem<SolutionType, FunctionType>  {

	public void initialize();
	
	public Boolean endConditionHasMeet();
	
	public ProblemDefinition<SolutionType, FunctionType> getProblemDefinition();
	
	
	
}
