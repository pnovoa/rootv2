/**
 * 
 */
package rootv2.measures;

/**
 * @author pavel
 *
 */
public interface Measurable<DataType> {

	public void registerMeasure(Measure<DataType> measure);
	public void removeMeasure(Measure<DataType> measure);
	public void notifyMeasures();
}
