package org.eclipse.scanning.points;

import java.io.IOException;

import org.eclipse.scanning.api.points.models.JythonGeneratorModel;
import org.eclipse.scanning.jython.JythonInterpreterManager;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JythonGenerator extends AbstractScanPointGenerator<JythonGeneratorModel> {

	private static Logger logger = LoggerFactory.getLogger(JythonGenerator.class);

	JythonGenerator(JythonGeneratorModel model) {
		super(model);
	}

	@Override
	protected PPointGenerator createPythonPointGenerator() {
		try {
			// Ensure that the module path is on the path
			JythonInterpreterManager.addPath(model.getPath());
		} catch (IOException e) {
			logger.error("Unable to add '{}' to path", model.getPath());
		}
		final JythonObjectFactory<PPointGenerator> jythonObject = new JythonObjectFactory<>(PPointGenerator.class,
				model.getModuleName(), model.getClassName());
		final PPointGenerator pPointGenerator;
		if (model.getJythonArguments() == null || model.getJythonArguments().isEmpty()) {
			pPointGenerator = jythonObject.createObject();
		} else {
			final Object[] args = model.getJythonArguments().values().toArray();
			final String[] keywords = model.getJythonArguments().keySet().toArray(new String[0]);
			pPointGenerator = jythonObject.createObject(args, keywords);
		}
		return pPointGenerator;
	}
}