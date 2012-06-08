/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.GdaMetadata;
import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.StoredMetadataEntry;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ObservablePathConstructorTest {

	private ObservablePathConstructor opc;
	private GdaMetadata gdaMetadata;
	private StoredMetadataEntry meta1;
	private StoredMetadataEntry meta2;

	@Before
	public void setUp() throws FactoryException {
		meta1 = new StoredMetadataEntry("meta1", "value1");
		meta2 = new StoredMetadataEntry("meta2", "value2");
		gdaMetadata = new GdaMetadata();
		gdaMetadata.addMetadataEntry(meta1);
		gdaMetadata.addMetadataEntry(meta2);
		
		opc = new ObservablePathConstructor();
		opc.setName("pathname");
		opc.setTemplate("/root/$meta1$/$meta2$"); //e.g. /dls/b16/data/$year$/$visit$/$subdirectory$
		opc.setGdaMetadata(gdaMetadata);
		opc.configure();
		
		GDAMetadataProvider.setInstanceForTesting(gdaMetadata);
	}
	
	@Test
	public void testGetPath() {
		assertEquals("/root/value1/value2", opc.getPath());
		meta1.setValue("value1new");
		meta2.setValue("value2new");
		assertEquals("/root/value1new/value2new", opc.getPath());	
	}
	
	@Test
	public void testUpdatesOberversWhenMetadataUpdates() {
		IObserver mockObserver = mock(IObserver.class);
		opc.addIObserver(mockObserver);
		meta1.setValue("value1new");
		verify(mockObserver).update(opc, new PathChanged("/root/value1new/value2"));
	}
	
	@Test
	public void testGetReferedMetadataEntries() {
		List<IMetadataEntry> referedEntries = opc.getReferedMetadataEntries();
		assertEquals(meta1, referedEntries.get(0));
		assertEquals(meta2, referedEntries.get(1));
		
		StoredMetadataEntry meta3 = new StoredMetadataEntry("meta3", "value3");
		gdaMetadata.addMetadataEntry(meta3);
		opc.setTemplate("/root/$meta1$/$meta2$/$meta3$");
		referedEntries = opc.getReferedMetadataEntries();
		assertEquals(meta1, referedEntries.get(0));
		assertEquals(meta2, referedEntries.get(1));
		assertEquals(meta3, referedEntries.get(2));
	}
	
}
