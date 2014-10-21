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

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.devices.hatsaxs.beans.LocationBean;
import uk.ac.gda.devices.hatsaxs.beans.PlateConfig;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class HplcSessionBean implements IRichBean {

	private static final long serialVersionUID = 5075861349177543025L;
	static public final URL mappingURL = HplcSessionBean.class.getResource("HplcMapping.xml");
	static public final URL schemaURL  = HplcSessionBean.class.getResource("HplcMapping.xsd");
	public static PlateConfig HPLC_PLATES;
	
	List<HplcBean> measurements;

	public static HplcSessionBean createFromXML(String filename) throws Exception {
		return (HplcSessionBean) XMLHelpers.createFromXML(mappingURL, HplcSessionBean.class, schemaURL, filename);
	}

	public static void writeToXML(HplcSessionBean bean, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, bean, filename);
	}
	
	public static void setPlates(PlateConfig plates) {
		HPLC_PLATES = plates;
	}
	
	public void setPlateSetup(PlateConfig plates) {
		if (!plates.equals(HPLC_PLATES)) {
			throw new IllegalArgumentException("HPLC plates do not match machine setup");
		}
	}

	public List<HplcBean> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<HplcBean> measurements) {
		this.measurements = measurements;
		for (HplcBean tb : measurements) {
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
