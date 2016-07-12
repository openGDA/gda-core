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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * @author Matthew Gerring Matthew Gerring
 */
public class XanesScanParameters implements Serializable, IScanParameters {

	private String scannableName;
	private String element;
	private Double finalEnergy;
	private boolean shouldValidate = true;

	/**
	 * @return the stopEnergy
	 */
	public Double getFinalEnergy() {
		return finalEnergy;
	}

	public Double getInitialEnergy(){
		return regions.get(0).getEnergy();
	}

	/**
	 * @param stopEnergy
	 *            the stopEnergy to set
	 */
	public void setFinalEnergy(Double stopEnergy) {
		this.finalEnergy = stopEnergy;
	}

	private String edge;
	private List<Region> regions;

	/**
	 * URL of mapping file for EXAFS parameters.
	 */
	static public final URL mappingURL = XanesScanParameters.class.getResource("ExafsParameterMapping.xml");

	/**
	 * URL of schema for EXAFS parameters.
	 */
	static public final URL schemaUrl = XanesScanParameters.class.getResource("ExafsParameterMapping.xsd");

	/**
	 * @param filename
	 * @return scan parameters
	 * @throws Exception
	 */
	public static XanesScanParameters createFromXML(String filename) throws Exception {
		return (XanesScanParameters) XMLHelpers.createFromXML(mappingURL, XanesScanParameters.class, schemaUrl,
				filename);
	}

	/**
	 * @param xanesScanParameters
	 * @param filename
	 * @throws Exception
	 */
	public static void writeToXML(XanesScanParameters xanesScanParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, xanesScanParameters, filename);
	}

	/**
	 * constructor
	 */
	public XanesScanParameters() {
		regions = new ArrayList<Region>(11);
	}

	/**
	 * @return return element name
	 */
	public String getElement() {
		return element;
	}

	/**
	 * @param element
	 */
	public void setElement(String element) {
		if (element != null)
			this.element = element;
	}

	/**
	 * @return element edge type
	 */
	public String getEdge() {
		return edge;
	}

	/**
	 * @param edge
	 */
	public void setEdge(String edge) {
		if (edge != null)
			this.edge = edge;
	}

	/**
	 * @return list of regions
	 */
	public List<Region> getRegions() {
		return regions;
	}

	/**
	 * @param regions
	 *            the regions to set
	 */
	public void setRegions(List<Region> regions) {
		this.regions = regions;
	}

	/**
	 * @param region
	 */
	public void addRegion(Region region) {
		regions.add(region);
	}

	/**
	 * @throws Exception
	 */
	public void checkRegions() throws Exception {

		if (regions.isEmpty())
			throw new RegionException("No regions added to scan.");

		if (regions.size() > 1) {
			for (int i = 1; i < regions.size(); ++i) {

				final Region last = regions.get(i - 1);
				final Region cur = regions.get(i);
				checkEngergyIncrease(last.getEnergy(), cur.getEnergy());

				// Last Region
				if (i == (regions.size() - 1)) {
					checkEngergyIncrease(cur.getEnergy(), finalEnergy);
				}
			}
		} else {
			checkEngergyIncrease(regions.get(0).getEnergy(), finalEnergy);
		}
	}

	/**
	 * @throws Exception
	 */
	public void checkEngergyIncrease(final double startEnergy, final double stopEnergy) throws Exception {
		if (startEnergy >= stopEnergy)
			throw new RegionEnergyException("The start energy " + startEnergy
					+ " is greater than or equal to the stop energy " + stopEnergy);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edge == null) ? 0 : edge.hashCode());
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result + ((finalEnergy == null) ? 0 : finalEnergy.hashCode());
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
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
		XanesScanParameters other = (XanesScanParameters) obj;
		if (edge == null) {
			if (other.edge != null) {
				return false;
			}
		} else if (!edge.equals(other.edge)) {
			return false;
		}
		if (element == null) {
			if (other.element != null) {
				return false;
			}
		} else if (!element.equals(other.element)) {
			return false;
		}
		if (finalEnergy == null) {
			if (other.finalEnergy != null) {
				return false;
			}
		} else if (!finalEnergy.equals(other.finalEnergy)) {
			return false;
		}
		if (regions == null) {
			if (other.regions != null) {
				return false;
			}
		} else if (!regions.equals(other.regions)) {
			return false;
		}
		if (scannableName == null) {
			if (other.scannableName != null) {
				return false;
			}
		} else if (!scannableName.equals(other.scannableName)) {
			return false;
		}
		if (shouldValidate != other.shouldValidate) {
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * @return Returns the scannableName.
	 */
	@Override
	public String getScannableName() {
		return scannableName;
	}

	/**
	 * @param scannableName
	 *            The scannableName to set.
	 */
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	/**
	 * @return Returns the shouldValidate.
	 */
	public boolean isShouldValidate() {
		return shouldValidate;
	}

	/**
	 * @param shouldValidate
	 *            The shouldValidate to set.
	 */
	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	/**
	 * Method required to use with BeanUI. Called using reflection.
	 */
	public void clear() {
		if (regions != null)
			regions.clear();
	}

}
