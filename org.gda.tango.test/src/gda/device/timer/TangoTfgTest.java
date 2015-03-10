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

package gda.device.timer;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import gda.device.TangoDeviceProxy;
import gda.device.impl.DummyTangoDeviceImpl;

public class TangoTfgTest {

	public enum tfout { 
		 mask_width(1<<0), enb_ext_veto(1<<1), ext_veto_high(1<<2), ttl_drive1(1<<3), ttl_drive2(1<<4), ttl_drive3(1<<5), 
		 veto2_inv(1<<6), xfer2_inv(1<<7), veto3_inv(1<<8), xfer3_inv(1<<9), s1_mux(1<<10), s2_mux(1<<11), enb_tf3(1<<12), 
		 lvds1_hotwaxs(1<<13), lvds2_hotwaxs(1<<14);
		
		private int value;
		
		private tfout(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}

	public enum cc_mode { 
		all (1<<0), scaler64 (1<<1), adcs6 (1<<2), shortmixed (1<<3), shortscalers (1<<4), adcs8 (1<<5); 
		
		private int value;
		
		private cc_mode(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
 	}

	public enum cc_chan { 
		level (1<<0), edge (1<<1), inv_level (1<<2), debounce (1<<3), vetoed_level (1<<4), vetoed_edge (1<<5),
		time_veto (1<<6), vetoed_debounce (1<<7), ignore_veto (1<<8), alternate(1<<9), extra_veto (1<<10),
		now (1<<11), silent (1<<12);
		
		private int value;
		
		private cc_chan(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
 	}

	public enum cc_extra_veto { 
		chan0_3 (1<<0), chan4_7 (1<<1), veto_scal (1<<2), veto_trig (1<<3), veto_tf3 (1<<4), inv_veto (1<<5),
		now (1<<6), silent (1<<7); 
		
		private int value;
		
		private cc_extra_veto(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
 	}
	
	public enum veto { 
		veto0_inv (1<<0), veto1_inv (1<<1), veto2_inv (1<<2), fzero0_inv (1<<3), fzero1_inv (1<<4),
		xfer0_inv (1<<5), xfer1_inv (1<<6), veto0_drive (1<<7), veto1_drive (1<<8), veto2_drive (1<<9),
		fzero0_drive (1<<10), fzero1_drive (1<<11), xfer0_drive (1<<12), xfer1_drive (1<<13),
		lvds_veto_inv (1<<14), lvds_fzero_inv (1<<15), lvds_xfer_inv (1<<16), leds_on_lemos (1<<17);
		
		private int value;
		
		private veto(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
 	}

	public enum group { 
		help(1<<0), ext_start(1<<1), ext_inh(1<<2), cycles(1<<3), file(1<<4), no_min_20us(1<<5), silent(1<<6),
		sequence(1<<7), auto_rearm(1<<8), ext_falling(1<<9);
		
		private int value;
		
		private group(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
 	}
	public enum trig {
		help(1<<0), start(1<<1), pause(1<<2), pause_dead(1<<3), falling(1<<4), debounce(1<<5), threshold(1<<6), now(1<<7),
		raw(1<<8), alternate(1<<9);

		private int value;
				
		private trig(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public int or(trig ... argin) {
			int result = value;
			for (int i=0; i<argin.length; i++) {
				result |= argin[i].getValue();
			}
			return result;
		}
	}

	private TangoDeviceProxy dev;
	
	public TangoTfgTest() {
		{ // dummy Tango proxy implementation
			dev = new TangoDeviceProxy( new DummyTangoDeviceImpl("tango::2345"));
			try {
				dev.write_attribute(new DeviceAttribute("Version", 2));
				dev.write_attribute(new DeviceAttribute("CurrentFrame", 0));
				dev.write_attribute(new DeviceAttribute("CurrentLap", 2));
				dev.write_attribute(new DeviceAttribute("AcqStatus", "IDLE"));
				dev.write_attribute(new DeviceAttribute("ArmedStatus", "IDLE"));
				dev.write_attribute(new DeviceAttribute("Progress", "IDLE"));
			} catch (DevFailed e) {
				fail("Exception: " + e.errors[0].desc);
			}
		}
//		{ // real tango device server
//			dev = new TangoDeviceProxy("tango://tcfippc3:2345/tfg/tango/1#dbase=no");
//		}
	}

	@Test
	public void testAttributes() {
		try {
			assertEquals(2, dev.getAttributeAsInt("Version"));
			assertEquals(0, dev.getAttributeAsInt("CurrentFrame"));
			assertEquals(2, dev.getAttributeAsInt("CurrentLap"));
			assertEquals("IDLE", dev.getAttributeAsString("AcqStatus"));
			assertEquals("IDLE", dev.getAttributeAsString("ArmedStatus"));
			assertEquals("IDLE", dev.getAttributeAsString("Progress"));
//			dev.getAttributeAsInt("FullStatus");
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testCommands() {
		try {
// TODO better tests here
			dev.command_inout("enable");
			dev.command_inout("disable");
			dev.command_inout("stopAtEOC");
			dev.command_inout("pause");
//			dev.command_inout("cont");
			dev.command_inout("stop");
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testSetupPort() {
		try {
			int[] argin = {0xF0, 0xF};
			DeviceData args = new DeviceData();
			args.insert(argin);
			dev.command_inout("setupPort", args);
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testSetupTrig() {
		try {
			double[] argin = new double[5];
			for (int i = 0; i < 16; i++) {
				argin[0] = trig.start.getValue();
				argin[1] = i; // Trigger input number 1..16
				argin[2] = 0; // Debounce value
				argin[3] = 0; // Threshold value
				DeviceData args = new DeviceData();
				args.insert(argin);
				dev.command_inout("setupTrig", args);
			}
			for (int i=1; i<16; i++) {
				argin[0] = trig.start.or(trig.falling);
				argin[1] = i; // Trigger input number 1..16
				argin[2] = 0; // Debounce value
				argin[3] = 0; // Threshold value
				DeviceData args = new DeviceData();
				args.insert(argin);
				dev.command_inout("setupTrig", args);
			}
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

// only with the real device
//	@Test
	public void testGenerate() {
		try {
			double[] argin = {0, 1, 2, 1.0, 5.0, 0};
			DeviceData args = new DeviceData();
			args.insert(argin);
			DeviceData argout = dev.command_inout("generate", args);
			assertEquals(2, argout.extractLong());

			dev.command_inout("start");
			assertEquals("RUNNING", dev.getAttributeAsString("AcqStatus"));
			assertEquals("RUNNING:", dev.getAttributeAsString("Progress").substring(0,8));
			while (dev.getAttributeAsString("AcqStatus").equals("RUNNING"))
				synchronized (this) {
					try {
						wait(1000, 0);
					} catch (InterruptedException e) {
					}
				}
			assertEquals("IDLE",dev.getAttributeAsString("Progress"));
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	//	Only with the real device
//	@Test
	public void testSetupGroups() {
		try {
			String[] argin = new String[7];
			argin[0] = "" + group.cycles.getValue();
			argin[1] = "4"; //cycles
			argin[2] = "tfgsetupgroups.dat"; //filename
			argin[3] = "seq_name"; //sequence name
			argin[4] = "10 0.1 2.0 0 0 0 255";
			argin[5] = "5 0.1 4.0 0 0 0 0";
			argin[6] = "-1 0 0 0 0 0 0";
			DeviceData args = new DeviceData();
			args.insert(argin);
			DeviceData argout = dev.command_inout("setupGroups", args);
			assertEquals(15, argout.extractLong());

			dev.command_inout("start");
			assertEquals("RUNNING", dev.getAttributeAsString("AcqStatus"));
			assertEquals("RUNNING:", dev.getAttributeAsString("Progress").substring(0,8));
			while (dev.getAttributeAsString("AcqStatus").equals("RUNNING"))
				synchronized (this) {
					try {
						wait(1000, 0);
					} catch (InterruptedException e) {
				}
			}
			assertEquals("IDLE",dev.getAttributeAsString("Progress"));
			dev.command_inout("start");
			for (int i=0; i<50; i++) {
				assertEquals("RUNNING", dev.getAttributeAsString("AcqStatus"));
			}
			dev.command_inout("stop");
			assertEquals("IDLE", dev.getAttributeAsString("AcqStatus"));
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testSetupVeto() {
		try {
			int[] argin = new int[19];
			for (int i=0; i<19; i++) {
				argin[i] = 0;
			}
			argin[0] = veto.veto0_inv.getValue() | veto.veto2_inv.getValue();
			argin[1] = 1; //veto value
			argin[3] = 1; //veto value
			DeviceData args = new DeviceData();
			args.insert(argin);
			dev.command_inout("setupVeto", args);
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testSetupCCExtraVeto() {
		try {
			int[] argin = new int[4];
			argin[0] = cc_extra_veto.chan0_3.getValue() | cc_extra_veto.veto_scal.getValue();
			argin[1] = 2; //extra-veto value
			argin[2] = 3; //extra-veto value
			argin[3] = 4; //extra-veto value
			DeviceData args = new DeviceData();
			args.insert(argin);
			dev.command_inout("setupCCExtraVeto", args);
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testSetupCCMode() {
		try {
			int argin = cc_mode.scaler64.getValue();
			DeviceData args = new DeviceData();
			args.insert(argin);
			dev.command_inout("setupCCMode", args);
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testSetupCCChan() {
		try {
			double[] argin = new double[5];
			argin[0] = cc_chan.debounce.getValue() | cc_chan.alternate.getValue();
			argin[1] = -1; // all channels
			argin[2] = 5.4; // debounce value
			argin[3] = 6.3; // vetoed debounce value
			argin[4] = 1;   // alternate value
			DeviceData args = new DeviceData();
			args.insert(argin);
			dev.command_inout("setupCCChan", args);
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testSetupTFout() {
		try {
			int[] argin = new int[16];
			for (int i=0; i<16; i++) {
				argin[i] = 0;
			}
			argin[0] = tfout.mask_width.getValue() | tfout.veto2_inv.getValue();
			argin[1] = 3; // mask_width
			argin[6] = 0; // veto2_inv
			DeviceData args = new DeviceData();
			args.insert(argin);
			dev.command_inout("setupTFout", args);
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testClear() {
		try {
			int[] argin = {0, 0, 0, 9, 1, 32767};
			DeviceData args = new DeviceData();
			args.insert(argin);
			dev.command_inout("clear", args);
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}

	@Test
	public void testRead() {
		try {
			int[] argin = {0, 0, 0, 9, 1, 5};
			DeviceData args = new DeviceData();
			args.insert(argin);
			dev.command_inout("read", args);
		} catch (DevFailed e) {
			fail("Exception: " + e.errors[0].desc);
		}
	}
}
