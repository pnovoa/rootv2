/**
 * 
 */
package rootv2.algorithms;

import rootv2.problems.ProblemDefinition;

/**
 * @author pavel
 *
 */
public interface AlgorithmState<IndividualType, FitnessType> {

	public void setProblemDefinition(ProblemDefinition<IndividualType, FitnessType> problemDefinition);
	
    public ProblemDefinition<IndividualType, FitnessType> getProblemDefinition();
}
