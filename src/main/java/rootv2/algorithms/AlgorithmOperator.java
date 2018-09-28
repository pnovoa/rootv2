/**
 * 
 */
package rootv2.algorithms;

/**
 * @author pavel
 *
 */
public interface AlgorithmOperator<AlgorithmStateType> {

	public AlgorithmStateType perform(AlgorithmStateType algorithmState);
}
