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

package org.diamond.tomo.highrestomo.beans;

import java.net.URL;

import org.apache.commons.beanutils.BeanUtils;

/**
 * This bean contains all the information about a high resolution tomography, so
 * that the script can then be run.
 * 
 */
public class HighResolutionTomographyParameters {

	private Double startAngle;
	private Double endAngle;
	private Double exposureTime;
	private Integer numberOfProjections;
	private Integer numberOfFlatFields;
	private Integer imagesPerFlat;
	private Integer imagesPerdark;	
	
	/**
	 * Static for getting the mapping file
	 */
	static public final URL mappingURL = HighResolutionTomographyParameters.class.getResource("ParameterMapping.xml");
	/**
	 * Static for getting the schema file
	 */
	static public final URL schemaURL  = HighResolutionTomographyParameters.class.getResource("ParameterMapping.xsd");
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((endAngle == null) ? 0 : endAngle.hashCode());
		result = prime * result
				+ ((exposureTime == null) ? 0 : exposureTime.hashCode());
		result = prime * result
				+ ((imagesPerFlat == null) ? 0 : imagesPerFlat.hashCode());
		result = prime * result
				+ ((imagesPerdark == null) ? 0 : imagesPerdark.hashCode());
		result = prime
				* result
				+ ((numberOfFlatFields == null) ? 0 : numberOfFlatFields
						.hashCode());
		result = prime
				* result
				+ ((numberOfProjections == null) ? 0 : numberOfProjections
						.hashCode());
		result = prime * result
				+ ((startAngle == null) ? 0 : startAngle.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HighResolutionTomographyParameters other = (HighResolutionTomographyParameters) obj;
		if (endAngle == null) {
			if (other.endAngle != null)
				return false;
		} else if (!endAngle.equals(other.endAngle))
			return false;
		if (exposureTime == null) {
			if (other.exposureTime != null)
				return false;
		} else if (!exposureTime.equals(other.exposureTime))
			return false;
		if (imagesPerFlat == null) {
			if (other.imagesPerFlat != null)
				return false;
		} else if (!imagesPerFlat.equals(other.imagesPerFlat))
			return false;
		if (imagesPerdark == null) {
			if (other.imagesPerdark != null)
				return false;
		} else if (!imagesPerdark.equals(other.imagesPerdark))
			return false;
		if (numberOfFlatFields == null) {
			if (other.numberOfFlatFields != null)
				return false;
		} else if (!numberOfFlatFields.equals(other.numberOfFlatFields))
			return false;
		if (numberOfProjections == null) {
			if (other.numberOfProjections != null)
				return false;
		} else if (!numberOfProjections.equals(other.numberOfProjections))
			return false;
		if (startAngle == null) {
			if (other.startAngle != null)
				return false;
		} else if (!startAngle.equals(other.startAngle))
			return false;
		return true;
	}


	/**
	 * @return the startAngle
	 */
	public Double getStartAngle() {
		return startAngle;
	}


	/**
	 * @param startAngle the startAngle to set
	 */
	public void setStartAngle(Double startAngle) {
		this.startAngle = startAngle;
	}


	/**
	 * @return the endAngle
	 */
	public Double getEndAngle() {
		return endAngle;
	}


	/**
	 * @param endAngle the endAngle to set
	 */
	public void setEndAngle(Double endAngle) {
		this.endAngle = endAngle;
	}


	/**
	 * @return the exposureTime
	 */
	public Double getExposureTime() {
		return exposureTime;
	}


	/**
	 * @param exposureTime the exposureTime to set
	 */
	public void setExposureTime(Double exposureTime) {
		this.exposureTime = exposureTime;
	}


	/**
	 * @return the numberOfProjections
	 */
	public Integer getNumberOfProjections() {
		return numberOfProjections;
	}


	/**
	 * @param numberOfProjections the numberOfProjections to set
	 */
	public void setNumberOfProjections(Integer numberOfProjections) {
		this.numberOfProjections = numberOfProjections;
	}


	/**
	 * @return the numberOfFlatFields
	 */
	public Integer getNumberOfFlatFields() {
		return numberOfFlatFields;
	}


	/**
	 * @param numberOfFlatFields the numberOfFlatFields to set
	 */
	public void setNumberOfFlatFields(Integer numberOfFlatFields) {
		this.numberOfFlatFields = numberOfFlatFields;
	}


	/**
	 * @return the imagesPerFlat
	 */
	public Integer getImagesPerFlat() {
		return imagesPerFlat;
	}


	/**
	 * @param imagesPerFlat the imagesPerFlat to set
	 */
	public void setImagesPerFlat(Integer imagesPerFlat) {
		this.imagesPerFlat = imagesPerFlat;
	}


	/**
	 * @return the imagesPerdark
	 */
	public Integer getImagesPerdark() {
		return imagesPerdark;
	}


	/**
	 * @param imagesPerdark the imagesPerdark to set
	 */
	public void setImagesPerdark(Integer imagesPerdark) {
		this.imagesPerdark = imagesPerdark;
	}


	/**
	 * @return the mappingURL
	 */
	public static URL getMappingURL() {
		return mappingURL;
	}


	/**
	 * @return the schemaURL
	 */
	public static URL getSchemaURL() {
		return schemaURL;
	}


	/**
	 * Uses the bean utils to generate a sensible toString.
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

}
