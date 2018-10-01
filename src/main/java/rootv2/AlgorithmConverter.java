/**
 * 
 */
package rootv2;

import picocli.CommandLine.ITypeConverter;
import rootv2.algorithms.Algorithm;
import rootv2.algorithms.PSO;
import rootv2.algorithms.PSOARKPAST;
import rootv2.algorithms.PSORBF_AR;
import rootv2.algorithms.PSO_KFUTURE;

/**
 * @author pavelnovoa
 *
 */
public class AlgorithmConverter implements ITypeConverter<Algorithm> {

	
	@Override
	public Algorithm convert(String value) throws Exception {
		System.out.println(value);
		switch (value) {
			case "PSORBF_AR": return new PSORBF_AR();
			case "PSOARKPAST": return new PSOARKPAST();
			case "PSO_KFUTURE": return new PSO_KFUTURE();
			default: return new PSO();
		}
	}

}
