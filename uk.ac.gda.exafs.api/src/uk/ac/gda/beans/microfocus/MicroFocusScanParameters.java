/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.microfocus;

import java.io.Serializable;
import java.net.URL;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Class for the Microfocus Scan parameters
 */

public class MicroFocusScanParameters implements Serializable, IScanParameters {

	static public final URL mappingURL = MicroFocusScanParameters.class.getResource("MicroFocusParameterMapping.xml");

	static public final URL schemaUrl = MicroFocusScanParameters.class.getResource("MicroFocusParameterMapping.xsd");

	public static MicroFocusScanParameters createFromXML(String filename) throws Exception {
		return (MicroFocusScanParameters) XMLHelpers.createFromXML(mappingURL, MicroFocusScanParameters.class,
				schemaUrl, filename);
	}

	public static void writeToXML(MicroFocusScanParameters scanParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, scanParameters, filename);
	}

	private Double xStart;
	private Double yStart;
	private Double xEnd;
	private Double yEnd;
	private Double xStepSize;
	private Double yStepSize;
	private Double collectionTime;
	private Double energy;
	private Double zValue;
	private Double rowTime;

	public MicroFocusScanParameters() {
	}

	public Double getZValue() {
		return zValue;
	}

	public void setZValue(Double zValue) {
		this.zValue = zValue;
	}

	public Double getEnergy() {
		return energy;
	}

	public void setEnergy(Double e) {
		this.energy = e;
	}

	public Double getCollectionTime() {
		return collectionTime;
	}

	public void setCollectionTime(Double collectionTime) {
		this.collectionTime = collectionTime;
	}

	public Double getXStart() {
		return xStart;
	}

	public void setXStart(Double xStart) {
		this.xStart = xStart;
	}

	public Double getYStart() {
		return yStart;
	}

	public void setYStart(Double yStart) {
		this.yStart = yStart;
	}

	public Double getXEnd() {
		return xEnd;
	}

	public void setXEnd(Double xEnd) {
		this.xEnd = xEnd;
	}

	public Double getYEnd() {
		return yEnd;
	}

	public void setYEnd(Double yEnd) {
		this.yEnd = yEnd;
	}

	public Double getXStepSize() {
		return xStepSize;
	}

	public void setXStepSize(Double xStepSize) {
		this.xStepSize = xStepSize;
	}

	public Double getYStepSize() {
		return yStepSize;
	}

	public void setYStepSize(Double yStepSize) {
		this.yStepSize = yStepSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((xStart == null) ? 0 : xStart.hashCode());
		result = prime * result + ((xEnd == null) ? 0 : xEnd.hashCode());
		result = prime * result + ((xStepSize == null) ? 0 : xStepSize.hashCode());
		result = prime * result + ((yStart == null) ? 0 : yStart.hashCode());
		result = prime * result + ((yEnd == null) ? 0 : yEnd.hashCode());
		result = prime * result + ((yStepSize == null) ? 0 : yStepSize.hashCode());
		result = prime * result + ((collectionTime == null) ? 0 : collectionTime.hashCode());
		result = prime * result + ((zValue == null) ? 0 : zValue.hashCode());
		result = prime * result + ((rowTime == null) ? 0 : rowTime.hashCode());
		result = prime * result + ((energy == null) ? 0 : energy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MicroFocusScanParameters other = (MicroFocusScanParameters) obj;
		if (xStart == null) {
			if (other.xStart != null) {
				return false;
			}
		} else if (!xStart.equals(other.xStart)) {
			return false;
		}
		if (xEnd == null) {
			if (other.xEnd != null) {
				return false;
			}
		} else if (!xEnd.equals(other.xEnd)) {
			return false;
		}
		if (xStepSize == null) {
			if (other.xStepSize != null) {
				return false;
			}
		} else if (!xStepSize.equals(other.xStepSize)) {
			return false;
		}
		if (yStart == null) {
			if (other.yStart != null) {
				return false;
			}
		} else if (!yStart.equals(other.yStart)) {
			return false;
		}
		if (yEnd == null) {
			if (other.yEnd != null) {
				return false;
			}
		} else if (!yEnd.equals(other.yEnd)) {
			return false;
		}
		if (yStepSize == null) {
			if (other.yStepSize != null) {
				return false;
			}
		} else if (!yStepSize.equals(other.yStepSize)) {
			return false;
		}
		if (collectionTime == null) {
			if (other.collectionTime != null) {
				return false;
			}
		} else if (!collectionTime.equals(other.collectionTime)) {
			return false;
		}
		if (zValue == null) {
			if (other.zValue != null) {
				return false;
			}
		} else if (!zValue.equals(other.zValue)) {
			return false;
		}
		if (energy == null) {
			if (other.energy != null) {
				return false;
			}
		} else if (!energy.equals(other.energy)) {
			return false;
		}
		if (rowTime == null) {
			if (other.rowTime != null) {
				return false;
			}
		} else if (!rowTime.equals(other.rowTime)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public String getScannableName() {
		// TODO: Which scannable should be used here?
		return null;
	}

	public void setRowTime(Double rowTime) {
		this.rowTime = rowTime;
	}

	public Double getRowTime() {
		return rowTime;
	}

}
