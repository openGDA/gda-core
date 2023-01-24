/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * So I20 can have the fluo detector output parameters in a different place to everyone else.
 */
public class I20OutputParameters extends OutputParameters {
	private boolean vortexSaveRawSpectrum = false;
	private boolean xspressOnlyShowFF = false;
	private boolean xspressShowDTRawValues = false;
	private boolean xspressSaveRawSpectrum = false;

//	public static final URL mappingURL = OutputParameters.class.getResource("I20SampleParametersMapping.xml");
//	public static final URL schemaUrl = OutputParameters.class.getResource("I20SampleParametersMapping.xsd");

	public static I20OutputParameters createFromXML(String filename) throws Exception {
		return XMLHelpers.createFromXML(mappingURL, I20OutputParameters.class, schemaUrl, filename);
	}

	public static void writeToXML(I20OutputParameters outputParams, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, outputParams, filename);
	}

	public boolean isVortexSaveRawSpectrum() {
		return vortexSaveRawSpectrum;
	}

	public void setVortexSaveRawSpectrum(boolean vortexSaveRawSpectrum) {
		this.vortexSaveRawSpectrum = vortexSaveRawSpectrum;
	}

	public boolean isXspressOnlyShowFF() {
		return xspressOnlyShowFF;
	}

	public void setXspressOnlyShowFF(boolean xspressOnlyShowFF) {
		this.xspressOnlyShowFF = xspressOnlyShowFF;
	}

	public boolean isXspressShowDTRawValues() {
		return xspressShowDTRawValues;
	}

	public void setXspressShowDTRawValues(boolean xspressShowDTRawValues) {
		this.xspressShowDTRawValues = xspressShowDTRawValues;
	}

	public boolean isXspressSaveRawSpectrum() {
		return xspressSaveRawSpectrum;
	}

	public void setXspressSaveRawSpectrum(boolean xspressSaveRawSpectrum) {
		this.xspressSaveRawSpectrum = xspressSaveRawSpectrum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (vortexSaveRawSpectrum ? 1231 : 1237);
		result = prime * result + (xspressOnlyShowFF ? 1231 : 1237);
		result = prime * result + (xspressSaveRawSpectrum ? 1231 : 1237);
		result = prime * result + (xspressShowDTRawValues ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		I20OutputParameters other = (I20OutputParameters) obj;
		if (vortexSaveRawSpectrum != other.vortexSaveRawSpectrum)
			return false;
		if (xspressOnlyShowFF != other.xspressOnlyShowFF)
			return false;
		if (xspressSaveRawSpectrum != other.xspressSaveRawSpectrum)
			return false;
		if (xspressShowDTRawValues != other.xspressShowDTRawValues)
			return false;
		return true;
	}

}