package uk.ac.diamond.daq.experiment.plan.payload.jython;

import org.python.core.PyFunction;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.experiment.api.plan.PayloadHandler;

@Component
public class JythonPayloadHandler implements PayloadHandler<JythonPayload> {

	@Override
	public Class<?> getSourceClass() {
		return PyFunction.class;
	}

	@Override
	public Class<JythonPayload> getTargetClass() {
		return JythonPayload.class;
	}

	@Override
	public JythonPayload wrap(Object rawPayload) {
		return new JythonPayload((PyFunction) rawPayload);
	}

	@Override
	public Object handle(JythonPayload payload) {
		return payload.getFunction().__call__();
	}

}
