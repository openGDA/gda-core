package uk.ac.diamond.daq.experiment.plan.payload.jython;

import org.python.core.PyFunction;

import uk.ac.diamond.daq.experiment.api.plan.Payload;

public class JythonPayload implements Payload {
	
	private final PyFunction function;
	
	public JythonPayload(PyFunction function) {
		this.function = function;
	}
	
	public PyFunction getFunction() {
		return function;
	}

}
