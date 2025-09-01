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

package uk.ac.gda.tomography.scan.editor.view.configuration.tomography;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionSubType;
import uk.ac.gda.api.acquisition.TrajectoryShape;
import uk.ac.gda.ui.tool.ClientMessages;

public class LaminographyComposite extends TomographyComposite {

	public LaminographyComposite(Supplier<Composite> buttonsCompositeSupplier) {
		super(buttonsCompositeSupplier);
	}

	@Override
	public ClientMessages getName() {
		return ClientMessages.LAMINOGRAPHY;
	}

	@Override
	protected AcquisitionKeys getKey() {
		return new AcquisitionKeys(AcquisitionPropertyType.TOMOGRAPHY, AcquisitionSubType.LAMINOGRAPHY, TrajectoryShape.ONE_DIMENSION_LINE);
	}


}