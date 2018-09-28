package rootv2.rootexp;

import java.util.Vector;

import rootv2.problems.SearchSpace;

public class RealSearchSpace implements SearchSpace<Vector<Double>> {
	
	
	private Vector<Double> lowerBound;
	private Vector<Double> upperBound;
	private Integer dimension;
	
	public static RealSearchSpace buildDummyRealSearchSpace(Integer dimension, Double lowerB, Double uppBound) {
		
		RealSearchSpace searchSpace = new RealSearchSpace();
		searchSpace.lowerBound = new Vector<Double>();
		searchSpace.upperBound = new Vector<Double>();
		searchSpace.dimension = dimension.intValue();
		
		for(int i=0; i<dimension; i++) {
			searchSpace.lowerBound.addElement(lowerB.doubleValue());
			searchSpace.upperBound.addElement(uppBound.doubleValue());
		}
		
		return searchSpace;
	}
	
	
	@Override
	public Vector<Double> getLowerBound() {
		return this.lowerBound;
	}

	@Override
	public Vector<Double> getUpperBound() {
		
		return this.upperBound;
	}

	@Override
	public Integer getDimension() {
		
		return this.dimension;
	}

	
	
}
