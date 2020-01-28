package org.eclipse.scanning.points;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.points.models.JythonGeneratorModel;
import org.eclipse.scanning.jython.JythonInterpreterManager;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JythonGenerator extends AbstractScanPointGenerator<JythonGeneratorModel> {

	private static Logger logger = LoggerFactory.getLogger(JythonGenerator.class);

	private static final String CONTINUOUS = "continuous";
	private static final String ALTERNATE = "alternate";
	private static final String AXES = "axes";
	private static final String UNITS = "units";
	private static final String SIZE = "size";
	private static final List<String> MANDATORY_ARGS = Arrays.asList(CONTINUOUS, ALTERNATE, AXES, UNITS, SIZE);

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

	@Override
	public void validate(JythonGeneratorModel model) throws ValidationException {
		if (model.getPath() == null)
			throw new ModelValidationException("No module directory is set!", model, "path");
		final File file = new File(model.getPath());
		if (!file.exists())
			throw new ModelValidationException(String.format("The module directory '%s' does not exist!", file), model,
					"path");
		if (!file.isDirectory())
			throw new ModelValidationException(String.format("The module directory path '%s' is not a folder!", file),
					model, "path");
		if (!Optional.ofNullable(model.getModuleName()).isPresent())
			throw new ModelValidationException("The module name must be set!", model, "moduleName");
		if (!Optional.ofNullable(model.getClassName()).isPresent())
			throw new ModelValidationException("The class name must be set!", model, "className");
		Map<String, Object> kwargs = model.getJythonArguments();
		for (String arg : MANDATORY_ARGS) {
			if (!kwargs.containsKey(arg)) throw new ModelValidationException(
					String.format("Not all mandatory arguments set for JythonGeneratorModel, missing %s", arg), model, "jythonArguments");
		}
	}

}