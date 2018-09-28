/**
 * 
 */
package rootv2.problems;

/**
 * @author pavel
 *
 */
public interface SearchSpace<SolutionType> {

	public SolutionType getLowerBound();
	
	public SolutionType getUpperBound();
	
	public Integer getDimension();
}
