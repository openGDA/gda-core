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

package uk.ac.gda.beans.exafs.i20;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import uk.ac.gda.beans.exafs.IDetectorConfigurationParameters;

public class MedipixParameters implements IDetectorConfigurationParameters, Serializable  {

	static public final URL mappingURL = MedipixParameters.class.getResource("MedipixParametersMapping.xml");

	static public final URL schemaURL = MedipixParameters.class.getResource("MedipixParametersMapping.xsd");

	private List<ROIRegion> regionList;

	public void clear() {
		regionList.clear();
	}

	public MedipixParameters() {
		regionList = new ArrayList<ROIRegion>();
	}

	public void addRegion(ROIRegion roiRegion) {
		regionList.add( roiRegion );
	}

	public List<ROIRegion> getRegionList() {
		return regionList;
	}

	public void setRegionList(List<ROIRegion> regionList) {
		this.regionList = regionList;
	}

}
