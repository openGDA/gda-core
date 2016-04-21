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

package uk.ac.diamond.daq.mapping.impl;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AbstractBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.api.scan.process.ProcessingException;

import gda.factory.Findable;

/**
 * A preprocessor which sets the names of the currently-active stage axes.
 * <p>
 * This is intended to be created and configured using Spring, and then registered in the OSGi framework, probably by
 * using an OSGiServiceRegister instance (also in Spring).
 */
public class MappingStagePreprocessor implements IPreprocessor, Findable {

	private String name;
	private String activeFastScanAxis;
	private String activeSlowScanAxis;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getActiveFastScanAxis() {
		return activeFastScanAxis;
	}

	public void setActiveFastScanAxis(String activeFastScanAxis) {
		this.activeFastScanAxis = activeFastScanAxis;
	}

	public String getActiveSlowScanAxis() {
		return activeSlowScanAxis;
	}

	public void setActiveSlowScanAxis(String activeSlowScanAxis) {
		this.activeSlowScanAxis = activeSlowScanAxis;
	}

	@Override
	public <T> ScanRequest<T> preprocess(ScanRequest<T> req) throws ProcessingException {

		for (IScanPathModel model : req.getModels()) {
			if (model instanceof AbstractBoundingBoxModel) {
				AbstractBoundingBoxModel boxModel = (AbstractBoundingBoxModel) model;
				boxModel.setFastAxisName(activeFastScanAxis);
				boxModel.setSlowAxisName(activeSlowScanAxis);
			}
		}

		return req;
	}
}
