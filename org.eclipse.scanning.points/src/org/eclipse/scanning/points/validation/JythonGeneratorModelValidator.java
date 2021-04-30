/*-
 * Copyright © 2021 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.points.validation;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.JythonGeneratorModel;

class JythonGeneratorModelValidator extends AbstractPointsModelValidator<JythonGeneratorModel> {
	private static final String CONTINUOUS = "continuous";
	private static final String ALTERNATE = "alternate";
	private static final String AXES = "axes";
	private static final String UNITS = "units";
	private static final String SIZE = "size";
	private static final List<String> MANDATORY_ARGS = Arrays.asList(CONTINUOUS, ALTERNATE, AXES, UNITS, SIZE);

	@Override
	public JythonGeneratorModel validate(JythonGeneratorModel model) {
		if (model.getPath() == null) {
			throw new ModelValidationException("No module directory is set!", model, "path");
		}
		final File file = new File(model.getPath());
		if (!file.exists()) {
			throw new ModelValidationException(String.format("The module directory '%s' does not exist!", file), model, "path");
		}
		if (!file.isDirectory()) {
			throw new ModelValidationException(String.format("The module directory path '%s' is not a folder!", file), model, "path");
		}
		if (!Optional.ofNullable(model.getModuleName()).isPresent()) {
			throw new ModelValidationException("The module name must be set!", model, "moduleName");
		}
		if (!Optional.ofNullable(model.getClassName()).isPresent()) {
			throw new ModelValidationException("The class name must be set!", model, "className");
		}
		final Map<String, Object> kwargs = model.getJythonArguments();
		for (String arg : MANDATORY_ARGS) {
			if (!kwargs.containsKey(arg)) throw new ModelValidationException(
					String.format("Not all mandatory arguments set for JythonGeneratorModel, missing %s", arg), model, "jythonArguments");
		}
		return model;
	}
}
