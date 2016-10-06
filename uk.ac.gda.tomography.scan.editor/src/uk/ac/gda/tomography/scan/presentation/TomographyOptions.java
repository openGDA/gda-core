/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.presentation;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;

/**
 * <p>
 * This class hold the options for the various drop-down lists in ParametersComposite<br>
 * e.g. available rotation & linear stages, pixel size units.
 * </p>
 * <p>
 * A single bean of this class should be defined in the client configuration.
 * </p>
 */

public class TomographyOptions implements Findable, Configurable {

	private String name;
	private String[] rotationStages;
	private String[] linearStages;
	private String[] detectorToSampleDistanceUnits;
	private String[] xPixelSizeUnits;
	private String[] yPixelSizeUnits;
	private boolean configured = false;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (rotationStages == null) {
				throw new FactoryException("No rotation stages set");
			} else if (linearStages == null) {
				throw new FactoryException("No rotation stages set");
			}
		}
	}

	public String[] getRotationStages() {
		return rotationStages;
	}

	public void setRotationStages(String[] rotationStages) {
		this.rotationStages = rotationStages;
	}

	public String[] getLinearStages() {
		return linearStages;
	}

	public void setLinearStages(String[] linearStages) {
		this.linearStages = linearStages;
	}

	public String[] getDetectorToSampleDistanceUnits() {
		return detectorToSampleDistanceUnits;
	}

	public void setDetectorToSampleDistanceUnits(String[] detectorSampleToDistanceUnits) {
		this.detectorToSampleDistanceUnits = detectorSampleToDistanceUnits;
	}

	public String[] getxPixelSizeUnits() {
		return xPixelSizeUnits;
	}

	public void setxPixelSizeUnits(String[] xPixelSizeUnits) {
		this.xPixelSizeUnits = xPixelSizeUnits;
	}

	public String[] getyPixelSizeUnits() {
		return yPixelSizeUnits;
	}

	public void setyPixelSizeUnits(String[] yPixelSizeUnits) {
		this.yPixelSizeUnits = yPixelSizeUnits;
	}
}
