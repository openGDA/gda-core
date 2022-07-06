/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.data.fileregistrar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IScanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import gda.TestHelpers;
import gda.data.ServiceHolder;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.batoncontrol.ClientDetails;
import gda.scan.IScanDataPoint;
import gda.util.Version;

public class FileRegistrarUnitTest {

	private class ScanObserver extends ClientFileAnnouncer {

		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void notifyIObservers(Object source, Object arg) {
			latch.countDown();
		}

		// The ICAT XML creator is called on a separate thread: wait until it calls back
		public void waitUntilNotified() throws InterruptedException, TimeoutException {
			if (!latch.await(1, TimeUnit.SECONDS)) {
				throw new TimeoutException("Timeout waiting for notification of file registration");
			}
		}
	}

	private static final String SCAN_FILE = "/dls/ixx/2017/cm-12345-1/ixx-444.nxs";
	private static final String[] SCAN_FILES = { "/dls/ixx/2017/cm-12345-1/ixx-444.nxs",
			"/dls/ixx/2017/cm-12345-1/ixx-555-EXCALIBUR.h5", "/dls/ixx/2017/cm-12345-1/ixx-666-PANDA.h5" };

	private IcatXMLCreator icatXmlCreator;
	private ScanObserver scanObserver;
	private IFilePathService filePathService;
	private ArgumentCaptor<String> scanIdCaptor;
	private ArgumentCaptor<String[]> fileArrayCaptor;

	@BeforeEach
	public void setUp() {
		icatXmlCreator = mock(IcatXMLCreator.class);
		scanObserver = new ScanObserver();
		scanIdCaptor = ArgumentCaptor.forClass(String.class);
		fileArrayCaptor = ArgumentCaptor.forClass(String[].class);

		filePathService = mock(IFilePathService.class);
		new ServiceHolder().setFilePathService(filePathService);
	}

	@BeforeEach
	public void addMockJythonToFinder() throws FactoryException {
		Jython jython = mock(Jython.class);
		when(jython.getRelease(anyString())).thenReturn(Version.getRelease());
		when(jython.getClientInformation(anyString())).thenReturn(mock(ClientDetails.class));
		Factory factory = mock(Factory.class);
		when(factory.getFindables()).thenReturn(Arrays.asList(jython));
		when(factory.getFindable(Jython.SERVER_NAME)).thenReturn(jython);
		Finder.addFactory(factory);
	}

	@AfterEach
	public void cleanUpFinder() {
		Finder.removeAllFactories();
	}

	@SuppressWarnings("unused")
	@Test
	public void testIcatXMLCreatorRequired() throws Exception {
		assertThrows(FactoryException.class, () -> new FileRegistrar(null));
	}

	@Test
	public void testRegisterFile() throws Exception {
		when(filePathService.getScanNumber()).thenReturn(123);

		final FileRegistrar fileRegistrar = new FileRegistrar(icatXmlCreator);
		fileRegistrar.setClientFileAnnouncer(scanObserver);

		// registerFile() will throw an exception because there is no JythonServerFacade
		try {
			fileRegistrar.registerFile(SCAN_FILE);
		} catch (NullPointerException e) {
			// expected
		}
		fileRegistrar.scanEnd();
		scanObserver.waitUntilNotified();

		// Dataset id will be constructed from the value returned by the file path service
		verify(icatXmlCreator).registerFiles(scanIdCaptor.capture(), fileArrayCaptor.capture());
		assertEquals("scan-123", scanIdCaptor.getValue());
		final String[] fileArray = fileArrayCaptor.getValue();
		assertEquals(1, fileArray.length);
		assertEquals(SCAN_FILE, fileArray[0]);
	}

	@Test
	public void testRegisterFiles() throws Exception {

		when(filePathService.getScanNumber()).thenReturn(123);

		final FileRegistrar fileRegistrar = new FileRegistrar(icatXmlCreator);
		fileRegistrar.setClientFileAnnouncer(scanObserver);
		try {
			fileRegistrar.registerFiles(SCAN_FILES);
		} catch (NullPointerException e) {
			// expected
		}
		fileRegistrar.scanEnd();
		scanObserver.waitUntilNotified();

		verify(icatXmlCreator).registerFiles(scanIdCaptor.capture(), fileArrayCaptor.capture());
		assertEquals("scan-123", scanIdCaptor.getValue());
		final List<String> fileArray = Arrays.asList(fileArrayCaptor.getValue());
		assertEquals(3, fileArray.size());
		for (int i = 0; i < SCAN_FILES.length; i++) {
			assertTrue(fileArray.contains(SCAN_FILES[i]));
		}
	}

	@Test
	public void testAddScanFile() throws Exception {
		when(filePathService.getScanNumber()).thenReturn(123);

		final FileRegistrar fileRegistrar = new FileRegistrar(icatXmlCreator);
		fileRegistrar.setClientFileAnnouncer(scanObserver);
		fileRegistrar.addScanFile(SCAN_FILE);
		fileRegistrar.scanEnd();
		scanObserver.waitUntilNotified();

		// Dataset id will be constructed from the value returned by the file path service
		verify(icatXmlCreator).registerFiles(scanIdCaptor.capture(), fileArrayCaptor.capture());
		assertEquals("scan-123", scanIdCaptor.getValue());
		final String[] fileArray = fileArrayCaptor.getValue();
		assertEquals(1, fileArray.length);
		assertEquals(SCAN_FILE, fileArray[0]);
	}

	@Test
	public void testAddScanFiles() throws Exception {
		when(filePathService.getScanNumber()).thenReturn(123);

		final FileRegistrar fileRegistrar = new FileRegistrar(icatXmlCreator);
		fileRegistrar.setClientFileAnnouncer(scanObserver);
		for (int i = 0; i < SCAN_FILES.length; i++) {
			fileRegistrar.addScanFile(SCAN_FILES[i]);
		}
		fileRegistrar.scanEnd();
		scanObserver.waitUntilNotified();

		// Dataset id will be constructed from the value returned by the file path service
		verify(icatXmlCreator).registerFiles(scanIdCaptor.capture(), fileArrayCaptor.capture());
		assertEquals("scan-123", scanIdCaptor.getValue());
		final String[] fileArray = fileArrayCaptor.getValue();
		assertEquals(3, fileArray.length);
		for (int i = 0; i < SCAN_FILES.length; i++) {
			assertEquals(SCAN_FILES[i], fileArray[i]);
		}
	}

	@Test
	public void testAddScanFilesPathServiceFails() throws Exception {
		when(filePathService.getScanNumber()).thenThrow(new Exception("Cannot get scan number"));

		final FileRegistrar fileRegistrar = new FileRegistrar(icatXmlCreator);
		fileRegistrar.setClientFileAnnouncer(scanObserver);
		for (int i = 0; i < SCAN_FILES.length; i++) {
			fileRegistrar.addScanFile(SCAN_FILES[i]);
		}
		fileRegistrar.scanEnd();
		scanObserver.waitUntilNotified();

		// If file path service cannot provide scan number, get dataset id from the first file name
		verify(icatXmlCreator).registerFiles(scanIdCaptor.capture(), fileArrayCaptor.capture());
		assertEquals("ixx-444.nxs", scanIdCaptor.getValue());
		final String[] fileArray = fileArrayCaptor.getValue();
		assertEquals(3, fileArray.length);
		for (int i = 0; i < SCAN_FILES.length; i++) {
			assertEquals(SCAN_FILES[i], fileArray[i]);
		}
	}

	@Test
	public void testAddData() throws Exception {
		final int[] dims1 = new int[] { 10 };
		final Detector simpleDetector = TestHelpers.createTestDetector("SimpleDetector", 0.0,
				new String[] { "simpleDetector1" }, new String[] {}, 0,
				new String[] { "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g",
						"%5.2g" },
				TestHelpers.createTestNexusGroupData(DoubleDataset.class, dims1, true), SCAN_FILE, "description1",
				"detectorID1", "detectorType1");

		final IScanDataPoint dataPoint1 = mock(IScanDataPoint.class);
		when(dataPoint1.getDetectors()).thenReturn(new Vector<Detector>(Arrays.asList(simpleDetector)));
		when(dataPoint1.getDetectorNames()).thenReturn(new Vector<String>(Arrays.asList("SimpleDetector")));
		when(dataPoint1.getDetectorData()).thenReturn(new Vector<Object>(Arrays.asList(SCAN_FILE)));
		when(dataPoint1.getCurrentFilename()).thenReturn(SCAN_FILE);
		when(dataPoint1.getScanIdentifier()).thenReturn(321);

		final IScanDataPoint dataPoint2 = mock(IScanDataPoint.class);
		when(dataPoint2.getDetectors()).thenReturn(new Vector<Detector>(Arrays.asList(simpleDetector)));
		when(dataPoint2.getDetectorNames()).thenReturn(new Vector<String>(Arrays.asList("SimpleDetector")));
		when(dataPoint2.getDetectorData()).thenReturn(new Vector<Object>(Arrays.asList(SCAN_FILE)));
		when(dataPoint1.getCurrentFilename()).thenReturn(SCAN_FILE);
		when(dataPoint2.getScanIdentifier()).thenReturn(654);

		final IDataWriterExtender dataWriterExtender = mock(IDataWriterExtender.class);

		final FileRegistrar fileRegistrar = new FileRegistrar(icatXmlCreator);
		fileRegistrar.setClientFileAnnouncer(scanObserver);
		fileRegistrar.addData(dataWriterExtender, dataPoint1);
		fileRegistrar.addData(dataWriterExtender, dataPoint2);
		fileRegistrar.completeCollection(dataWriterExtender);
		scanObserver.waitUntilNotified();

		// Dataset id will be constructed from the last data point
		verify(icatXmlCreator).registerFiles(scanIdCaptor.capture(), fileArrayCaptor.capture());
		assertEquals("scan-654", scanIdCaptor.getValue());
		final String[] fileArray = fileArrayCaptor.getValue();
		assertEquals(1, fileArray.length);
		assertEquals(SCAN_FILE, fileArray[0]);
	}

	@Test
	public void testRegister() throws Exception {
		final IScanService scanService = mock(IScanService.class);
		new ServiceHolder().setRunnableDeviceService(scanService);
		final ArgumentCaptor<FileRegistrar> fileRegistrarCaptor = ArgumentCaptor.forClass(FileRegistrar.class);

		final FileRegistrar fileRegistrar = new FileRegistrar(icatXmlCreator);
		fileRegistrar.register();

		verify(scanService).addScanParticipant(fileRegistrarCaptor.capture());
		assertEquals(fileRegistrar, fileRegistrarCaptor.getValue());
	}

}
