/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.scan.datawriter;

import gda.TestHelpers;
import gda.device.Analyser;
import gda.device.Detector;
import gda.device.detector.analyser.EpicsMCASimple;
import gda.device.detector.xmap.XmapDetectorFromEpicsMca;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.device.scannable.SimpleScannable;
import gda.device.timer.DummyTfg;
import gda.scan.ConcurrentScan;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.beans.vortex.VortexParameters;

public class MFWESetupScannableTest {

	@SuppressWarnings("unchecked")
	@Test
	public void test() throws InterruptedException, Exception {
		TestHelpers.setUpTest(MFWESetupScannableTest.class, "test", true);
		
		SimpleScannable ix = new SimpleScannable();
		ix.setName("ix");
		ix.setInputNames(new String[]{"ix"});
		ix.setCurrentPosition(new Double(0.0));
		ix.configure();
		SimpleScannable iy = new SimpleScannable();
		iy.setName("iy");
		iy.setInputNames(new String[]{"iy"});
		iy.setCurrentPosition(new Double(0.0));
		iy.configure();

		FindableEpicsDevice fed = new FindableEpicsDevice();
		HashMap<String, String> recordPVs = new HashMap<String, String>();
		recordPVs.put("Record", "MLL:DXP:mca");
		fed.setRecordPVs(recordPVs);
		fed.setDummy(true);
		fed.configure();

		EpicsMCASimple mcaSimple = new EpicsMCASimple();
		mcaSimple.setName("mcaSimple");
		mcaSimple.setEpicsDevice(fed);
		mcaSimple.configure();
		DummyTfg tfg = new DummyTfg();
		tfg.setName("tfg");
		tfg.configure();
		XmapDetectorFromEpicsMca xmap = new XmapDetectorFromEpicsMca();
		xmap.setName("xmap");
		String filename = this.getClass().getResource("Vortex_Parameters.xml").getFile();
		xmap.setConfigFileName(filename);
		xmap.setAnalysers(Arrays.asList(new Analyser[]{mcaSimple}));
		xmap.setPrefixExtraNameWithDetElement(false);
		xmap.configure();

		
		MFWESetupScannable mfwesetup = new MFWESetupScannable();
		mfwesetup.setName("test");
		mfwesetup.setDetectorBeanFileName(filename);
		mfwesetup.setDetectors(new Detector[]{ xmap});
//		mfwesetup.setInitialSelectedElement("roi1");
		mfwesetup.configure();
		
		BeansFactory.setClasses(((Class<? extends IRichBean>[]) new Class<?>[]{VortexParameters.class}));
		ConcurrentScan scan = new ConcurrentScan(new Object[]{ ix, 0, 2, 1, iy, 0, 2, 1, xmap, 1., mfwesetup});
		scan.runScan();
}

}
