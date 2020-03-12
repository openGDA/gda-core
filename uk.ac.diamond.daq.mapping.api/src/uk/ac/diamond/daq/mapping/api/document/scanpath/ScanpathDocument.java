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

package uk.ac.diamond.daq.mapping.api.document.scanpath;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Describes a generic acquisition model. Classes extending this realise specific acquisition configuration as, one line scanning or raster scanning
 *
 * @author Maurizio Nagni
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class ScanpathDocument {

	// Constants to reference the available parameters for a {@link RandomOffsetTwoAxisGridPointsModel}
	protected static final int OFFSET = 0;
	protected static final int SEED = 1;

	protected IScanPointGeneratorModel pathModel;
	protected IROI roi;

	public ScanpathDocument() {
		super();
	}

	protected final void setPathModel(IScanPointGeneratorModel pathModel) {
		this.pathModel = pathModel;
	}

	protected final void setRoi(IROI roi) {
		this.roi = roi;
	}

	@JsonIgnore
	public abstract IScanPointGeneratorModel getIScanPointGeneratorModel();
	@JsonIgnore
	public abstract IROI getROI();
}
