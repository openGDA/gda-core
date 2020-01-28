/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;

/**
 * A Generator with no model, for situations where a PPointGenerator is given to GDA without
 * model information (e.g. a PPointGenerator is handed to Malcolm, which does additional validation
 * and modifies the PPointGenerator, meaning that the model GDA was using is now invalid.
 *
 * This object is therefore just a wrapper for the PPointGenerator for situations where we want to treat it like an
 * IPointGenerator: passing it to a CompoundGenerator for example
 */

@SuppressWarnings("rawtypes")
public class NoModelGenerator extends AbstractScanPointGenerator {

	@SuppressWarnings("unchecked")
	public NoModelGenerator(PPointGenerator generator){
		model = null;
		this.pointGenerator = generator;
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		return pointGenerator;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [PPointGenerator=" + pointGenerator.toDict().toString() + "]";
	}

	@Override
	public void validate(AbstractPointsModel model) {
		throw new ModelValidationException("Generator is not intended for validating models!", model);
	}

	@Override
	public AbstractPointsModel getModel() {
		throw new ModelValidationException("NoModelGenerator does not contain information about model!");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		NoModelGenerator other = (NoModelGenerator) obj;
		if (pointGenerator == null) {
			return other.pointGenerator == null;
		}
		return pointGenerator.toDict().equals(other.pointGenerator.toDict());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pointGenerator == null) ? 0 : pointGenerator.hashCode());
		return result;
	}
}
