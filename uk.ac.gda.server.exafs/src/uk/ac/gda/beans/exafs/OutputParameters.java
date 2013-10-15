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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

public class OutputParameters implements Serializable, IOutputParameters {

	public static final URL mappingURL = OutputParameters.class.getResource("ExafsParameterMapping.xml");

	public static final URL schemaUrl = OutputParameters.class.getResource("ExafsParameterMapping.xsd");

	private String asciiFileName;
	private String asciiDirectory;
	private String nexusDirectory;
	private String beforeScriptName;
	private String afterScriptName;
	private List<SignalParameters> signalList;
	private List<MetadataParameters> metadataList;
	private boolean signalActive;
	private boolean metadataActive;
	private boolean shouldValidate = true;
	private boolean extraData = false;


	public boolean isSignalActive() {
		return signalActive;
	}

	public void setSignalActive(boolean signalActive) {
		this.signalActive = signalActive;
	}
	
	public boolean isMetadataActive() {
		return metadataActive;
	}

	public void setMetadataActive(boolean metadataActive) {
		this.metadataActive = metadataActive;
	}

	@Override
	public void clear() {
		signalList.clear();
		metadataList.clear();
	}

	public OutputParameters() {
		signalList = new ArrayList<SignalParameters>();
		metadataList = new ArrayList<MetadataParameters>();
	}

	@Override
	public String getAsciiFileName() {
		return asciiFileName;
	}

	public void setAsciiFileName(String asciiFileName) {
		this.asciiFileName = asciiFileName;
	}

	@Override
	public String getAsciiDirectory() {
		return asciiDirectory;
	}

	public void setAsciiDirectory(String asciiDirectory) {
		this.asciiDirectory = asciiDirectory;
	}

	public String getNexusDirectory() {
		return nexusDirectory;
	}

	public void setNexusDirectory(String nexusDirectory) {
		this.nexusDirectory = nexusDirectory;
	}

	public void addSignal(SignalParameters signal) {
		signalList.add(signal);
	}

	public List<SignalParameters> getSignalList() {
		return signalList;
	}

	@SuppressWarnings("unchecked")
	public List<SignalParameters> getCheckedSignalList() {
		return signalActive ? getSignalList() : Collections.EMPTY_LIST;
	}

	public void setSignalList(List<SignalParameters> signalList) {
		this.signalList = signalList;
	}

	public void addMetadata(MetadataParameters metadata) {
		metadataList.add(metadata);
	}

	public List<MetadataParameters> getMetadataList() {
		return metadataList;
	}

	@SuppressWarnings("unchecked")
	public List<SignalParameters> getCheckedMetadataList() {
		return metadataActive ? getMetadataList() : Collections.EMPTY_LIST;
	}

	public void setMetadataList(List<MetadataParameters> metadataList) {
		this.metadataList = metadataList;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((asciiDirectory == null) ? 0 : asciiDirectory.hashCode());
		result = prime * result + ((asciiFileName == null) ? 0 : asciiFileName.hashCode());
		result = prime * result + (extraData ? 1231 : 1237);
		result = prime * result + ((nexusDirectory == null) ? 0 : nexusDirectory.hashCode());
		result = prime * result + ((beforeScriptName == null) ? 0 : beforeScriptName.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
		result = prime * result + (signalActive ? 1231 : 1237);
		result = prime * result + ((signalList == null) ? 0 : signalList.hashCode());
		
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
		OutputParameters other = (OutputParameters) obj;
		if (asciiDirectory == null) {
			if (other.asciiDirectory != null)
				return false;
		} else if (!asciiDirectory.equals(other.asciiDirectory))
			return false;
		if (asciiFileName == null) {
			if (other.asciiFileName != null)
				return false;
		} else if (!asciiFileName.equals(other.asciiFileName))
			return false;
		if (extraData != other.extraData)
			return false;
		if (nexusDirectory == null) {
			if (other.nexusDirectory != null)
				return false;
		} else if (!nexusDirectory.equals(other.nexusDirectory))
			return false;
		if (beforeScriptName == null) {
			if (other.beforeScriptName != null)
				return false;
		} else if (!beforeScriptName.equals(other.beforeScriptName))
			return false;
		if (shouldValidate != other.shouldValidate)
			return false;
		if (signalActive != other.signalActive)
			return false;
		if (signalList == null) {
			if (other.signalList != null)
				return false;
		} else if (!signalList.equals(other.signalList))
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

	public boolean isShouldValidate() {
		return shouldValidate;
	}

	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	public boolean isExtraData() {
		return extraData;
	}

	public void setExtraData(boolean extraData) {
		this.extraData = extraData;
	}

	public void setBeforeScriptName(String scriptName) {
		this.beforeScriptName = scriptName;
	}

	public String getBeforeScriptName() {
		return beforeScriptName;
	}
	
	public void setAfterScriptName(String scriptName) {
		this.afterScriptName = scriptName;
	}

	public String getAfterScriptName() {
		return afterScriptName;
	}
}
