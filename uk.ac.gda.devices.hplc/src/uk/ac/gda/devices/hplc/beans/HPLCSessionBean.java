/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hplc.beans;

import java.net.URL;
import java.util.List;
import gda.factory.Finder;
import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.PlateConfig;

public class HPLCSessionBean implements IRichBean {

	private static final long serialVersionUID = 5075861349177543025L;
	static public final URL mappingURL = HPLCSessionBean.class.getResource("HPLCMapping.xml");
	static public final URL schemaURL  = HPLCSessionBean.class.getResource("HPLCMapping.xsd");
	public static final PlateConfig HPLC_PLATES = Finder.getInstance().find("hplcPlates");
	
	List<HPLCBean> measurements;
	
	public void setPlateSetup(PlateConfig plates) {
		if (!plates.equals(HPLC_PLATES)) {
			throw new IllegalArgumentException("HPLC plates do not match machine setup");
		}
	}

	public List<HPLCBean> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<HPLCBean> measurements) {
		this.measurements = measurements;
		for (HPLCBean tb : measurements) {
			LocationBean loc = tb.getLocation();
			if (loc.getConfig() == null) {
				loc.setConfig(HPLC_PLATES);
			}
		}
	}

	public static PlateConfig getHplcPlates() {
		return HPLC_PLATES;
	}

	@Override
	public void clear() {
		measurements = null;
	}

}
