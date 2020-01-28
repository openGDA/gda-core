/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.ConcurrentMultiModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
/**
* A Generator for {@link ConcurrentMultiModel}s
*/
public class ConcurrentMultiGenerator extends AbstractMultiGenerator<ConcurrentMultiModel> {

	public ConcurrentMultiGenerator(ConcurrentMultiModel model, IPointGeneratorService service) {
		super(model, service);
	}

	@Override
	protected JythonObjectFactory<PPointGenerator> getFactory() {
		return ScanPointGeneratorFactory.JZipGeneratorFactory();
	}

	@Override
	public void validate(ConcurrentMultiModel model) {
		super.validate(model);
		List<String> dimensions = new ArrayList<>();
		int size = getGenerators().get(0).size();
		for (IPointGenerator<?> gen : getGenerators()) {
			if (gen.size() != size) {
				throw new ModelValidationException("All models must be the same length in ConcurrentMultiGenerator!", model, "models");
			}
			for (String axis : gen.getNames()) {
				if (dimensions.contains(axis)) throw new ModelValidationException("All models in ConcurrentModel must"
						+ " be in mutually exclusive axes!", model, "models");
				dimensions.add(axis);
			}
		}
	}

}
