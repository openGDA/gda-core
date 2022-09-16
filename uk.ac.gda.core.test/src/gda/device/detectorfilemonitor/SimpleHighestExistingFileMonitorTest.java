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

package gda.device.detectorfilemonitor;

import java.io.File;

import org.junit.Assert;

import gda.device.detectorfilemonitor.impl.SimpleHighestExistingFileMonitor;
import gda.observable.IObserver;
import gda.util.TestUtils;
import org.junit.jupiter.api.Test;

public class SimpleHighestExistingFileMonitorTest {

	private String scratchFolder;
	Integer foundIndex;

	@Test
	public void testSetRunAfterPropertiesSet() throws Exception{
		scratchFolder = TestUtils.setUpTest(SimpleHighestExistingFileMonitorTest.class, "testSetRunAfterPropertiesSet", true);
		SimpleHighestExistingFileMonitor simpleDetectorFileMonitor = new SimpleHighestExistingFileMonitor();
		int startNumber = 5;
		String fileTemplate = "/file_%04d.tif";
		simpleDetectorFileMonitor.setHighestExistingFileMonitorSettings(new HighestExistingFileMonitorSettings(scratchFolder, fileTemplate,startNumber));
		int delay = 1000;
		simpleDetectorFileMonitor.setDelayInMS(delay);
		simpleDetectorFileMonitor.afterPropertiesSet();
		Assert.assertFalse(simpleDetectorFileMonitor.isRunning());
		simpleDetectorFileMonitor.setRunning(true);
		testRunningMonitor(simpleDetectorFileMonitor, startNumber, fileTemplate, delay);
	}

	@Test
	public void testSetRunBeforePropertiesSet() throws Exception{
		scratchFolder = TestUtils.setUpTest(SimpleHighestExistingFileMonitorTest.class, "testSetRunBeforePropertiesSet", true);
		SimpleHighestExistingFileMonitor simpleDetectorFileMonitor = new SimpleHighestExistingFileMonitor();
		int startNumber = 5;
		String fileTemplate = "/file_%04d.tif";
		simpleDetectorFileMonitor.setHighestExistingFileMonitorSettings(new HighestExistingFileMonitorSettings(scratchFolder, fileTemplate,startNumber));
		int delay = 1000;
		simpleDetectorFileMonitor.setDelayInMS(delay);
		simpleDetectorFileMonitor.setRunning(true);
		simpleDetectorFileMonitor.afterPropertiesSet();
		testRunningMonitor(simpleDetectorFileMonitor, startNumber, fileTemplate, delay);
	}

	@Test
	public void testConfigureAndRun() throws Exception{
		scratchFolder = TestUtils.setUpTest(SimpleHighestExistingFileMonitorTest.class, "testConfigureAndRun", true);
		SimpleHighestExistingFileMonitor simpleDetectorFileMonitor = new SimpleHighestExistingFileMonitor();
		int startNumber = 5;
		String fileTemplate = "/file_%04d.tif";
		Assert.assertFalse(simpleDetectorFileMonitor.isRunning());
		int delay = 1000;
		simpleDetectorFileMonitor.setDelayInMS(delay);
		simpleDetectorFileMonitor.afterPropertiesSet();
		Assert.assertFalse(simpleDetectorFileMonitor.isRunning());
		simpleDetectorFileMonitor.configureAndRun(scratchFolder, fileTemplate, startNumber);
		testRunningMonitor(simpleDetectorFileMonitor, startNumber, fileTemplate, delay);
	}

	private void testRunningMonitor(SimpleHighestExistingFileMonitor simpleDetectorFileMonitor, int startNumber, String fileTemplate, int delay) throws Exception {

		simpleDetectorFileMonitor.addIObserver(new IObserver(){
			@Override
			public void update(Object source, Object arg) {
				foundIndex = ((HighestExistingFileMonitorData)arg).getFoundIndex();
			}
		});

		Assert.assertTrue(simpleDetectorFileMonitor.isRunning());

		Assert.assertNull(foundIndex);
		Assert.assertNull(simpleDetectorFileMonitor.getHighestExistingFileMonitorData().getFoundIndex());

		File f=null;
		int filenumber=startNumber;
		for( ; filenumber < startNumber+10; filenumber++){
			f = new File(String.format(scratchFolder+fileTemplate, filenumber));
			if(!f.exists()){
				f.createNewFile();
			}
		}

		Thread.sleep(2*delay);

		Assert.assertEquals(new Integer(filenumber-1), foundIndex);
		Assert.assertEquals(foundIndex, simpleDetectorFileMonitor.getHighestExistingFileMonitorData().getFoundIndex());

		simpleDetectorFileMonitor.setRunning(false);
		Assert.assertFalse(simpleDetectorFileMonitor.isRunning());
	}

}
