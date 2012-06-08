/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.scan;

import gda.data.PlottableDetectorData;
import gda.data.PlottableDetectorDataClone;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.util.Serializer;

import java.io.Serializable;
import java.util.Vector;

/**
 * Token passed between instances of ScanDataPointServer and ScanDataPointClient
 */
public class ScanDataPointVar implements Serializable {
	private ScanDataPointToken token;
	private byte[] stepIds;

	private Integer currentPointNumber;
	private Vector<Object> positions;
	private Vector<Object> detectorData;

	ScanDataPointVar(IScanDataPoint point) {
		token = new ScanDataPointToken(point.getUniqueName());
		positions = point.getPositions();
		currentPointNumber = point.getCurrentPointNumber();
		detectorData = new Vector<Object>();
		if (point.getDetectorData() != null) {
			for (Object data : point.getDetectorData()) {
				// must ensure equal lengths of detectorData and point.getDectecorData
				// send all detectorData to client unless it is PlottableDetectorData
				Object detectorDataToAdd = data;
				if (data instanceof PlottableDetectorData) {
					detectorDataToAdd = new PlottableDetectorDataClone((PlottableDetectorData) data);
				} else if (data instanceof NexusTreeProvider) {
					// TODO warn because of data volume potentially send to the client uselessly,
					// could be for other non simple data types as well
				}
				detectorData.add(detectorDataToAdd);
			}
		}
		if (point.getStepIds() != null && point.getStepIds().size() > 0) {
			Object[] dt = point.getStepIds().toArray(new Object[0]);
			stepIds = Serializer.toByte(dt);
		}

	}

	public ScanDataPointToken getToken() {
		return token;
	}

	public byte[] getStepIds() {
		return stepIds;
	}

	public Integer getCurrentPointNumber() {
		return currentPointNumber;
	}

	public Vector<Object> getPositions() {
		return positions;
	}

	public Vector<Object> getDetectorData() {
		return detectorData;
	}
}
