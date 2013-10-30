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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Vector;

import org.junit.Test;

public class NexusExtraMetadataDataWriterTest {

	@Test
	public void testAddingAndRemovingEntries(){
		NexusFileMetadata entry1 = new NexusFileMetadata("Pitch","10.0",NexusFileMetadata.EntryTypes.NXinstrument,NexusFileMetadata.NXinstrumentSubTypes.NXmirror,"M1");
		NexusFileMetadata entry2 = new NexusFileMetadata("Stripe","2",NexusFileMetadata.EntryTypes.NXinstrument,NexusFileMetadata.NXinstrumentSubTypes.NXmirror,"M1");
		NexusFileMetadata entry3 = new NexusFileMetadata("Xsize","5.5",NexusFileMetadata.EntryTypes.NXinstrument,NexusFileMetadata.NXinstrumentSubTypes.NXaperture,"S1");
		NexusFileMetadata entry4 = new NexusFileMetadata("x","12.34",NexusFileMetadata.EntryTypes.NXsample,null,"SAM1");
		
		NexusExtraMetadataDataWriter.addMetadataEntry(entry1);
		NexusExtraMetadataDataWriter.addMetadataEntry(entry2);
		NexusExtraMetadataDataWriter.addMetadataEntry(entry3);
		NexusExtraMetadataDataWriter.addMetadataEntry(entry4);
		
		HashMap<String,Vector<NexusFileMetadata>> storedEntries = NexusExtraMetadataDataWriter.getMetadataEntries();
		
		assertEquals("10.0",storedEntries.get("M1").get(0).getValue());
		assertEquals("2",storedEntries.get("M1").get(1).getValue());
		assertEquals("5.5",storedEntries.get("S1").get(0).getValue());
		assertEquals("12.34",storedEntries.get("SAM1").get(0).getValue());
		
		//remove one of the sample entries
		NexusExtraMetadataDataWriter.removeMetadataEntry(entry1);
		assertEquals(1,storedEntries.get("M1").size());
		
		// remove from the wrong category
		NexusExtraMetadataDataWriter.removeMetadataEntry(entry2);
		assertEquals(0,storedEntries.get("M1").size());
		assertEquals(1,storedEntries.get("S1").size());
		
		// remove from the right category
		NexusExtraMetadataDataWriter.removeMetadataEntry(entry3);
		assertEquals(0,storedEntries.get("S1").size());
	}
}
