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

import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.ConsecutiveMultiModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

/**
 * A Generator for {@link ConsecutiveMultiModel}s
 */
public class ConsecutiveMultiGenerator extends AbstractMultiGenerator<ConsecutiveMultiModel> {

	public ConsecutiveMultiGenerator(ConsecutiveMultiModel model, IPointGeneratorService service) {
		super(model, service);
	}

	public ConsecutiveMultiGenerator(InterpolatedMultiScanModel model, IPointGeneratorService service) {
		// this constructor is required for InterpolatedConsecutiveModel due to how the point gen service uses
		// reflection to invoke the constructor
		super(model, service);
	}

	@Override
	protected JythonObjectFactory<PPointGenerator> getFactory() {
		return ScanPointGeneratorFactory.JConcatGeneratorFactory();
	}
}
