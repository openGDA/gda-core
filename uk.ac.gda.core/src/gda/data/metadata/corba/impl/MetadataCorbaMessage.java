/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.data.metadata.corba.impl;

import gda.data.metadata.corba.CorbaMetadataEntry;

import java.io.Serializable;

public class MetadataCorbaMessage implements Serializable{
	
	CorbaMetadataEntry metadataObj;
	Serializable arg;
	
	public MetadataCorbaMessage(CorbaMetadataEntry metadataObj, Serializable arg) {
		super();
		this.metadataObj = metadataObj;
		this.arg = arg;
	}

	public CorbaMetadataEntry getMetadataObj() {
		return metadataObj;
	}

	public void setMetadataObj(CorbaMetadataEntry metadataObj) {
		this.metadataObj = metadataObj;
	}

	public Serializable getArg() {
		return arg;
	}

	public void setArg(Serializable arg) {
		this.arg = arg;
	}


}
