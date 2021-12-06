/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs.i18;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.ScannableConfiguration;

public class I18SampleParameters implements ISampleParameters {

	public static final URL mappingURL = I18SampleParameters.class.getResource("I18SampleParametersMapping.xml");
	public static final URL schemaURL = I18SampleParameters.class.getResource("I18SampleParametersMapping.xsd");

	private String name = "";
	private String description = "";
	private List<ScannableConfiguration> scannableConfigurations = new LinkedList<>();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public List<String> getDescriptions() {
		return Arrays.asList(description);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ScannableConfiguration> getScannableConfigurations() {
		return scannableConfigurations;
	}

	/**
	 * For individual scannable configurations in XML.
	 * If a configuration for the same scannable already exists,
	 * it is replaced by the new one.
	 */
	public void addScannableConfiguration(ScannableConfiguration configuration) {
		scannableConfigurations.stream()
			.filter(scannable -> scannable.getScannableName().equals(configuration.getScannableName()))
			.findFirst().ifPresentOrElse(old -> old.setPosition(configuration.getPosition()),
					() -> scannableConfigurations.add(configuration));
	}

	/**
	 * For updating from UI
	 */
	public void setScannableConfigurations(List<ScannableConfiguration> configuration) {
		scannableConfigurations = configuration;
	}


	@Override
	public int hashCode() {
		final var prime = 31;
		var result = 1;
		result = prime * result + ((scannableConfigurations == null) ? 0 : scannableConfigurations.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		I18SampleParameters other = (I18SampleParameters) obj;
		if (scannableConfigurations == null) {
			if (other.scannableConfigurations != null)
				return false;
		} else if (!scannableConfigurations.equals(other.scannableConfigurations))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
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

}
