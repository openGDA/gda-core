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

package uk.ac.gda.server.exafs.scan.preparers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import gda.TestHelpers;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.device.DeviceException;
import gda.device.scannable.ScannableMotor;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.beans.exafs.OutputParameters;

public class OutputPreparerTest {

	private AsciiDataWriterConfiguration datawriterconfig;
	private NXMetaDataProvider metashop;
	private ScannableMotor mocked_sxcryo_scannable;

	@Before
	public void setupTests(){
		datawriterconfig = new AsciiDataWriterConfiguration();
		metashop = new NXMetaDataProvider();

		mocked_sxcryo_scannable = PowerMockito.mock(ScannableMotor.class);
		Mockito.when(mocked_sxcryo_scannable.getName()).thenReturn("mocked_sxcryo_scannable");


		Factory finderfactory = TestHelpers.createTestFactory("OutputPreparerTest");
		finderfactory.addFindable(mocked_sxcryo_scannable);
		Finder.getInstance().addFactory(finderfactory);

		JythonServerFacade jythonserverfacade = Mockito.mock(JythonServerFacade.class);
		InterfaceProvider.setJythonNamespaceForTesting(jythonserverfacade);
		Mockito.when(jythonserverfacade.getFromJythonNamespace("mocked_sxcryo_scannable")).thenReturn(mocked_sxcryo_scannable);
	}


	@Test
	public void testNXMetaDataProviderUsage(){

		try {
//			SignalParameters signal = new SignalParameters();
//			signal.setScannableName("mocked_sxcryo_scannable");
//			signal.setDecimalPlaces(1);
//			signal.setLabel("mocked_sxcryo_scannable");
//
//			List<SignalParameters> signalList = new ArrayList<SignalParameters>();
//			signalList.add(signal);

			MetadataParameters metadata = new MetadataParameters();
			metadata.setScannableName("mocked_sxcryo_scannable");

			OutputParameters outputParameters = new OutputParameters();
//			outputParameters.setSignalList(signalList);
			outputParameters.addMetadata(metadata);
			outputParameters.setMetadataActive(true);

			OutputPreparerTestImpl impl = new OutputPreparerTestImpl(datawriterconfig,metashop);
			impl.configure(outputParameters, null,null, null);

			assertTrue(metashop.getMetaScannables().contains(mocked_sxcryo_scannable));

			impl.resetStaticMetadataList();

			assertTrue(!metashop.getMetaScannables().contains(mocked_sxcryo_scannable));

		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}
}
