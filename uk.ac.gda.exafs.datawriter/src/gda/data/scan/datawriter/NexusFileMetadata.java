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

package gda.data.scan.datawriter;

/**
 * For use by the NexusExtraMetadataDateWriter to record the extra metadata and their location in a nexus file.
 */
public class NexusFileMetadata {
	
	public EntryTypes nxEntryType = null;
	public NXinstrumentSubTypes nxEntrySubType = null;
	public String name = null;
	public String value= null;
	public String typeLabel = null;
	
	public enum EntryTypes {
		NXinstrument, NXsample, NXmonitor, NXdata, NXevent_data, NXuser, 
		NXprocess, NXcharacterization
	}
	
	public enum NXinstrumentSubTypes {
		NXaperture, NXattenuator, NXbeam_stop, NXbending_magnet, NXcollimator, NXcrystal, NXdetector, NXdisk_chopper, 
		NXfermi_chopper, NXfilter, NXflipper, NXguide, NXinsertion_device, NXmirror, NXmoderator, NXmonochromator, 
		NXpolarizer, NXpositioner, NXsource, NXvelocity_selector, NXsample_stage
	}

	public NexusFileMetadata(String name, String value,EntryTypes nxEntryType, NXinstrumentSubTypes nxEntrySubType, String typeLabel) {
		super();
		this.nxEntryType = nxEntryType;
		this.nxEntrySubType = nxEntrySubType;
		this.typeLabel = typeLabel;
		this.name = name;
		this.value = value;
	}
	
	public String getUniqueCategory(){
		return this.typeLabel;
	}
	
	public EntryTypes getNxEntryType() {
		return nxEntryType;
	}
	public void setNxEntryType(EntryTypes nxEntryType) {
		this.nxEntryType = nxEntryType;
	}
	
	public NXinstrumentSubTypes getNxEntrySubType() {
		return nxEntrySubType;
	}
	
	public void setNxEntrySubType(NXinstrumentSubTypes nxEntrySubType) {
		this.nxEntrySubType = nxEntrySubType;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public String getTypeLabel() {
		return typeLabel;
	}

	public void setTypeLabel(String typeLabel) {
		this.typeLabel = typeLabel;
	}
}
