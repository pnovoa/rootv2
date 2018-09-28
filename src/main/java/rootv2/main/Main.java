/**
 * 
 */
package rootv2.main;

import org.apache.commons.math3.linear.MatrixUtils;

import rootv2.rootexp.RealSearchSpace;
import smile.interpolation.ShepardInterpolation;

/**
 * @author pavel
 *
 */
public final class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		RealSearchSpace ss = RealSearchSpace.buildDummyRealSearchSpace(7, -5., 5.);

		for(Double v : ss.getLowerBound()) {
			System.out.println(v);
		}
		for(Double v : ss.getUpperBound()) {
			System.out.println(v);
		}
		System.out.println("Dimension " + ss.getDimension());
		
		ShepardInterpolation ssInterpolation;
		
	}

}
