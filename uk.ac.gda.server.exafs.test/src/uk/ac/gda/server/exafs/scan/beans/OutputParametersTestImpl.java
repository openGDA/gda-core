/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan.beans;

import java.util.ArrayList;
import java.util.List;

import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.beans.exafs.SignalParameters;

/**
 * For unit tests.
 */
public class OutputParametersTestImpl implements IOutputParameters {

	@Override
	public void clear() {
		// 
	}

	@Override
	public String getAsciiFileName() {
		return "";
	}

	@Override
	public String getAsciiDirectory() {
		return "ascii";
	}

	@Override
	public String getNexusDirectory() {
		return "nexus";
	}

	@Override
	public List<MetadataParameters> getMetadataList() {
		return new ArrayList<MetadataParameters>();
	}

	@Override
	public void setAsciiFileName(String name) {
		//
	}

	@Override
	public String getAfterScriptName() {
		return "";
	}

	@Override
	public String getBeforeScriptName() {
		return "";
	}

	@Override
	public List<SignalParameters> getSignalList() {
		return new ArrayList<SignalParameters>();
	}

}
