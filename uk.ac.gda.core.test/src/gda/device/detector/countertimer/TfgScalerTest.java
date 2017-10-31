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

package gda.device.detector.countertimer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.detector.DummyDAServer;
import gda.device.detector.countertimer.ScalerOutputProcessor.OutputConfig;
import gda.device.memory.Scaler;
import gda.device.scannable.DummyScannable;
import gda.device.timer.Etfg;
import gda.factory.FactoryException;
import gda.scan.ConcurrentScan;

public class TfgScalerTest {

	private DummyDAServer daserver;
	private Etfg tfg;
	private Scaler memory;
	private TfgScaler tfgscaler;
	private TfgScalerWithLogValues tfgScalerWithLogValues;

	@Before
	public void setup() throws FactoryException, DeviceException{

		daserver = new DummyDAServer();
		daserver.setDataFile(TfgScalerTest.class.getResource("Mofoil2.dat").getFile());
		daserver.configure();

		tfg = new Etfg();
		tfg.setDaServer(daserver);
		tfg.configure();

		memory = new Scaler();
		memory.setDaServer(daserver);
		memory.setWidth(10);
		memory.setHeight(1);
		memory.setOpenCommand("tfg open-cc");
		memory.configure();

		tfgscaler = new TfgScaler();
		tfgscaler.setScaler(memory);
		tfgscaler.setTimer(tfg);

		setupAndConfigureScaler();

	}

	private void setupAndConfigureScaler() throws FactoryException {
		tfgScalerWithLogValues = new TfgScalerWithLogValues();
		tfgScalerWithLogValues.setName("tfgScalerWithLogValues");
		tfgScalerWithLogValues.setScaler(memory);
		tfgScalerWithLogValues.setTimer(tfg);
		tfgScalerWithLogValues.setTFGv2(true);
		tfgScalerWithLogValues.setOutputLogValues(true);
		tfgScalerWithLogValues.setTimeChannelRequired(true);
		tfgScalerWithLogValues.setExtraNames(new String[] { "I0", "It", "Iref", "lnI0It", "lnItIref" });
		tfgScalerWithLogValues.setFirstDataChannel(0);
		tfgScalerWithLogValues.setNumChannelsToRead(3);
		tfgScalerWithLogValues.setOutputFormat(new String[] { "%.6,5g", "%9d", "%9d", "%9d", "%.5g", "%.5g" });
		tfgScalerWithLogValues.setDarkCurrentRequired(false); // set to false, or otherwise need to also setup shutter
		tfgScalerWithLogValues.configure();
	}

	@Test
	public void testReadoutTFG1NoTimeAllAvailableChannels() throws FactoryException, DeviceException {
		tfgscaler.setTFGv2(false);
		tfgscaler.setNumChannelsToRead(10);
		tfgscaler.setFirstDataChannel(0);
		tfgscaler.setTimeChannelRequired(false);
		tfgscaler.setExtraNames(new String[] { "I0", "It", "Iref" });
		tfgscaler.setOutputFormat(new String[] { "%.4f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f",
				"%.2f", "%.2f", "%.2f" });
		tfgscaler.configure();

		tfgscaler.setCollectionTime(1.0);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(10, output.length);
	}

	@Test
	public void testReadoutTFG1NoTimeAllChannels() throws FactoryException, DeviceException {
		tfgscaler.setTFGv2(false);
		tfgscaler.setNumChannelsToRead(3);
		tfgscaler.setFirstDataChannel(0);
		tfgscaler.setTimeChannelRequired(false);
		tfgscaler.setExtraNames(new String[]{"I0","It","Iref"});
		tfgscaler.setOutputFormat(new String[]{"%.4f","%.2f","%.2f","%.2f"});
		tfgscaler.configure();

		tfgscaler.setCollectionTime(1.0);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(3,output.length);
		assertEquals(19.9600579,output[0],1);
		assertEquals(-2.7172,output[1],1);
		assertEquals(99380,output[2],1);
	}

	@Test
	public void testReadoutTFG1TimeAllChannels() throws FactoryException, DeviceException {
		tfgscaler.setTFGv2(false);
		tfgscaler.setNumChannelsToRead(3);
		tfgscaler.setFirstDataChannel(0);
		tfgscaler.setTimeChannelRequired(true);
		tfgscaler.setExtraNames(new String[]{"Time","I0","It","Iref"});
		tfgscaler.setOutputFormat(new String[]{"%.4f","%.2f","%.2f","%.2f","%.2f"});
		tfgscaler.configure();

		tfgscaler.setCollectionTime(0.5);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(4,output.length);
		assertEquals(0.5,output[0],1);
		assertEquals(19.9600579,output[1],1);
		assertEquals(-2.7172,output[2],1);
		assertEquals(99380,output[3],1);
	}

	@Test
	public void testReadoutTFG2NoTimeAllChannels() throws DeviceException, FactoryException {
		tfgscaler.setTFGv2(true);
		tfgscaler.setNumChannelsToRead(3);
		tfgscaler.setFirstDataChannel(0);
		tfgscaler.setTimeChannelRequired(false);
		tfgscaler.setExtraNames(new String[]{"I0","It","Iref"});
		tfgscaler.setOutputFormat(new String[]{"%.4f","%.2f","%.2f","%.2f"});
		tfgscaler.configure();

		tfgscaler.setCollectionTime(1.0);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(3,output.length);
		assertEquals(-2.7172,output[0],1);
		assertEquals(99380,output[1],1);
		assertEquals(1504410,output[2],1);
	}

	@Test
	public void testReadoutTFG2TimeAllChannels() throws FactoryException, DeviceException {
		tfgscaler.setTFGv2(true);
		tfgscaler.setNumChannelsToRead(3);
		tfgscaler.setFirstDataChannel(0);
		tfgscaler.setTimeChannelRequired(true);
		tfgscaler.setExtraNames(new String[]{"Time","I0","It","Iref"});
		tfgscaler.setOutputFormat(new String[]{"%.4f","%.2f","%.2f","%.2f","%.2f"});
		tfgscaler.configure();

		tfgscaler.setCollectionTime(0.5);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(4,output.length);
		assertEquals(0.00000020,output[0],0.00000001);
		assertEquals(-2.7172,output[1],1);
		assertEquals(99380,output[2],1);
		assertEquals(1504410,output[3],1);
	}

	@Test
	public void testReadoutTFG1NoTimeOtherChannels() throws FactoryException, DeviceException {
		tfgscaler.setTFGv2(false);
		tfgscaler.setNumChannelsToRead(2);
		tfgscaler.setFirstDataChannel(5);
		tfgscaler.setTimeChannelRequired(false);
		tfgscaler.setExtraNames(new String[]{"I1","I2"});
		tfgscaler.setOutputFormat(new String[]{"%.4f","%.2f","%.2f"});
		tfgscaler.configure();

		tfgscaler.setCollectionTime(1.0);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(2,output.length);
		assertEquals(-9,output[0],1);
		assertEquals(147,output[1],1);
	}

	@Test
	public void testReadoutTFG1TimeOtherChannels() throws FactoryException, DeviceException {
		tfgscaler.setTFGv2(false);
		tfgscaler.setNumChannelsToRead(2);
		tfgscaler.setFirstDataChannel(5);
		tfgscaler.setTimeChannelRequired(true);
		tfgscaler.setExtraNames(new String[]{"Time","I1","I2"});
		tfgscaler.setOutputFormat(new String[]{"%.4f","%.2f","%.2f","%.2f"});
		tfgscaler.configure();

		tfgscaler.setCollectionTime(0.5);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(3,output.length);
		assertEquals(0.5,output[0],1);
		assertEquals(-9,output[1],1);
		assertEquals(147,output[2],1);
	}

	@Test
	public void testReadoutTFG2NoTimeOtherChannels() throws FactoryException, DeviceException {
		tfgscaler.setTFGv2(true);
		tfgscaler.setNumChannelsToRead(2);
		tfgscaler.setFirstDataChannel(5);
		tfgscaler.setTimeChannelRequired(false);
		tfgscaler.setExtraNames(new String[]{"I1","I2"});
		tfgscaler.setOutputFormat(new String[]{"%.4f","%.2f","%.2f"});
		tfgscaler.configure();

		tfgscaler.setCollectionTime(1.0);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(2,output.length);
		assertEquals(147,output[0],1);
		assertEquals(2502,output[1],1);
	}

	@Test
	public void testReadoutTFG2TimeOtherChannels() throws FactoryException, DeviceException {
		tfgscaler.setTFGv2(true);
		tfgscaler.setNumChannelsToRead(2);
		tfgscaler.setFirstDataChannel(5);
		tfgscaler.setTimeChannelRequired(true);
		tfgscaler.setExtraNames(new String[]{"Time","I1","I2"});
		tfgscaler.setOutputFormat(new String[]{"%.4f","%.2f","%.2f","%.2f"});
		tfgscaler.configure();

		tfgscaler.setCollectionTime(0.5);
		tfgscaler.collectData();

		double[] output = tfgscaler.readout();

		assertEquals(3,output.length);
		assertEquals(0.000000020,output[0],1);
		assertEquals(147,output[1],1);
		assertEquals(2502,output[2],1);
	}

	private List<OutputConfig> getScalerOutputConfig() {
		List<OutputConfig> outputConfig = new ArrayList<OutputConfig>();
		outputConfig.add(new OutputConfig("channel0", "%.4f", 0));
		outputConfig.add(new OutputConfig("channel1", "%.4f", 1));
		outputConfig.add(new OutputConfig("channel01", "%.4f", 0, 1, OutputConfig.DIVIDE));
		outputConfig.add(new OutputConfig("channel01_log", "%.4f", 0, 1, OutputConfig.DIVIDE_LOG));
		outputConfig.add(new OutputConfig("channel2", "%.4f", 2));
		outputConfig.add(new OutputConfig("channel21", "%.4f", 2, 1, OutputConfig.DIVIDE));
		return outputConfig;
	}

	@Test
	public void testScalerOutputConfig() throws Exception {
		tfgScalerWithLogValues.saveExtraNamesFormats();

		tfgScalerWithLogValues.setUseCustomisedOutput(true);
		tfgScalerWithLogValues.setScalerOutputConfig(getScalerOutputConfig());
		tfgScalerWithLogValues.configureScalerOutputProcessor();

		double vals[] = tfgScalerWithLogValues.readout();
		assertEquals(vals.length, tfgScalerWithLogValues.getScalerOutputConfig().size()+1);
		assertEquals(vals[1]/vals[2], vals[3], 1e-6);
		assertEquals(Math.log(vals[1]/vals[2]), vals[4], 1e-6);
		assertEquals(vals[5]/vals[2], vals[6], 1e-6);
	}

	@Test
	public void testScalerOutputConfigRestoresOk() throws Exception {
		// Make reading with original format and output options
		double[] origVals = tfgScalerWithLogValues.readout();
		String[] origFormat = tfgScalerWithLogValues.getOutputFormat();
		String[] origExtraNames = tfgScalerWithLogValues.getExtraNames();

		tfgScalerWithLogValues.saveExtraNamesFormats();
		tfgScalerWithLogValues.setUseCustomisedOutput(true);
		tfgScalerWithLogValues.setScalerOutputConfig(getScalerOutputConfig());
		tfgScalerWithLogValues.configureScalerOutputProcessor();

		// Do basic sanity check (should be ok if 'testScalerOutputConfig()' passes)
		double vals[] = tfgScalerWithLogValues.readout();
		assertEquals(vals.length, tfgScalerWithLogValues.getScalerOutputConfig().size()+1);

		// this should restore the original outputFormat and extraNames
		tfgScalerWithLogValues.setUseCustomisedOutput(false);

		double[] finalVals = tfgScalerWithLogValues.readout();
		String[] finalFormat = tfgScalerWithLogValues.getOutputFormat();
		String[] finalExtraNames = tfgScalerWithLogValues.getExtraNames();

		assertEquals(origVals.length, finalVals.length);
		assertArrayEquals(origFormat, finalFormat);
		assertArrayEquals(origExtraNames, finalExtraNames);
	}
}
