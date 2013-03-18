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

import gda.data.nexus.NeXusUtils;

import java.util.HashMap;
import java.util.Vector;

import org.nexusformat.NexusException;

/**
 * Nexus Data Writer which has a custom list of metadata values about the sample to add to the nexus file.
 * <p>
 * Required because the NexusDataWriter does not write every piece of metadata to itself by default. 
 */
public class NexusExtraMetadataDataWriter extends NexusDataWriter {

	/**
	 * The pieces of metadata, grouped in categories. They will be written to the Nexus file under those categories.
	 */
	private static HashMap<String,Vector<NexusFileMetadata>> groupedMetadata = new HashMap<String,Vector<NexusFileMetadata>>();
	
	public static void addMetadataEntry(NexusFileMetadata metadata){
		String category = metadata.getUniqueCategory();
		if (groupedMetadata.containsKey(category)){
			Vector<NexusFileMetadata> group = groupedMetadata.get(category);
			for (NexusFileMetadata existingEntry : group){
				if (existingEntry.getName().equals(metadata.getName())){
					return;
				}
			}
			group.add(metadata);				
		} else {
			Vector<NexusFileMetadata> newgroup = new Vector<NexusFileMetadata>();
			newgroup.add(metadata);
			groupedMetadata.put(category, newgroup);
		}
	}

	public static void removeMetadataEntry(NexusFileMetadata metadata) {
		String category = metadata.getUniqueCategory();
		if (groupedMetadata.containsKey(category)){
			Vector<NexusFileMetadata> group = groupedMetadata.get(category);
			NexusFileMetadata match = null;
			for (NexusFileMetadata entry : group) {
				if (entry.getName().equals(metadata.getName())) {
					match = entry;
					continue;
				}
			}
			if (match != null){
				group.remove(match);
			}
		}
	}
	
	public static void removeAllMetadataEntries(){
		groupedMetadata.clear();
	}
	
	public static HashMap<String,Vector<NexusFileMetadata>> getMetadataEntries(){
		return groupedMetadata;
	}
	
	public NexusExtraMetadataDataWriter() {
		super();
	}
	
	public NexusExtraMetadataDataWriter(Long fileNumber) {
		super(fileNumber);
	}

	@Override
	protected void createCustomMetaData() throws NexusException {
		HashMap<String,Vector<NexusFileMetadata>> groups = getMetadataEntries();
		
		for (String groupName : groups.keySet()){
			Vector<NexusFileMetadata> group = groupedMetadata.get(groupName);

			if (group.size() > 0) {

				boolean madeSubGroup = false;
				// if its part of the instrument, then put it inside the existing 'instrument' section of the Nexus file.
				if (group.get(0).getNxEntryType() == NexusFileMetadata.EntryTypes.NXinstrument) {
					if (file.groupdir().get("instrument") == null) {
						file.makegroup("instrument", group.get(0).getNxEntryType().toString());
					}
					file.opengroup("instrument", group.get(0).getNxEntryType().toString());
					if (file.groupdir().get(group.get(0).getTypeLabel()) == null) {
							file.makegroup(group.get(0).getTypeLabel(), group.get(0).getNxEntrySubType().toString());
					}
					file.opengroup(group.get(0).getTypeLabel(), group.get(0).getNxEntrySubType().toString());
					madeSubGroup = true;
				} else {
					file.makegroup(group.get(0).getTypeLabel(), group.get(0).getNxEntryType().toString());
					file.opengroup(group.get(0).getTypeLabel(), group.get(0).getNxEntryType().toString());
				}

				try {
					for (NexusFileMetadata thisEntry : group) {
						NeXusUtils.writeNexusString(file, thisEntry.getName(), thisEntry.getValue());
					}
				} finally {
					if (madeSubGroup) {
						file.closegroup();
					}
					file.closegroup();
				}
			}
		}
	}

}
