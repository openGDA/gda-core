/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.beans;


import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uk.ac.gda.beans.exafs.i20.FurnaceParameters;
import uk.ac.gda.beans.exafs.i20.I20SampleParameters;
import uk.ac.gda.doe.DOEUtils;
import uk.ac.gda.doe.RangeInfo;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class ExafsDOETest {

	@Test
	public void testNullRangeInfo() throws Throwable {
		final FurnaceParameters p = new FurnaceParameters();
		p.setTemperature(null);
		p.setTime(2);
		p.setTolerance(1);
		p.setX("1");
		p.setY("2");
		p.setZ("3");
		final List<RangeInfo> os = DOEUtils.getInfo(p);
		if (os.size()!=1) throw new Exception();
	}

	@Test
	public void testNoRangeInfo() throws Throwable {
		final FurnaceParameters p = new FurnaceParameters();
		p.setTemperature("310");
		p.setTime(2);
		p.setTolerance(1);
		p.setX("1");
		p.setY("2");
		p.setZ("3");
		final List<RangeInfo> os = DOEUtils.getInfo(p);
		if (os.size()!=1) throw new Exception();
	}
	
	
	@Test
	public void testGetInfoFromList() throws Throwable {
		// Test furnce expansion
		final FurnaceParameters p1 = new FurnaceParameters();
		p1.setTemperature("310; 315; 1");
		p1.setTime(2);
		p1.setTolerance(1);
		p1.setX("1");
		p1.setY("2");
		p1.setZ("3");
		
		final FurnaceParameters p2 = new FurnaceParameters();
		p2.setTemperature("320; 325; 1");
		p2.setTime(2);
		p2.setTolerance(1);
		p2.setX("4");
		p2.setY("5");
		p2.setZ("6");

		final List<RangeInfo> os = DOEUtils.getInfoFromList(Arrays.asList(new Object[]{p1,p2}));
		// This should be 6*6 +  6 - NOTE not the same as if two beans with these ranges were
		// in the same bean.
		if (os.size()!=42) throw new Exception();
		
		System.out.println(RangeInfo.format(os));
	}


	@Test
	public void testExpandInfo1() throws Throwable {
		// Test furnce expansion
		final FurnaceParameters p = new FurnaceParameters();
		p.setTemperature("310; 315; 1");
		p.setTime(2);
		p.setTolerance(1);
		p.setX("1");
		p.setY("2");
		p.setZ("3");
		final List<RangeInfo> os = DOEUtils.getInfo(p);
		if (os.size()!=6) throw new Exception();
	}
	
	@Test
	public void testExpandInfo2() throws Throwable {
		// Test furnce expansion
		final FurnaceParameters p = new FurnaceParameters();
		p.setTemperature("310; 315; 1");
		p.setTime(2);
		p.setTolerance(1);
		p.setX("1,1.1,1.2,1.3");
		p.setY("2,2.1");
		p.setZ("3,3.1");
		final List<RangeInfo> os = DOEUtils.getInfo(p);
		if (os.size()!=96) throw new Exception();
		
		System.out.println(RangeInfo.format(os));
	}


	@Test
	public void testNullRange() throws Throwable {
		final FurnaceParameters p = new FurnaceParameters();
		p.setTemperature(null);
		p.setTime(2);
		p.setTolerance(1);
		p.setX("1");
		p.setY("2");
		p.setZ("3");
		final List<?> os = DOEUtils.expand(p);
		if (os.size()!=1) throw new Exception();
	}

	@Test
	public void testNoRange() throws Throwable {
		final FurnaceParameters p = new FurnaceParameters();
		p.setTemperature("310");
		p.setTime(2);
		p.setTolerance(1);
		p.setX("1");
		p.setY("2");
		p.setZ("3");
		final List<?> os = DOEUtils.expand(p);
		if (os.size()!=1) throw new Exception();
	}

	@Test
	public void testExpand1() throws Throwable {
		// Test furnce expansion
		final FurnaceParameters p = new FurnaceParameters();
		p.setTemperature("310; 315; 1");
		p.setTime(2);
		p.setTolerance(1);
		p.setX("1");
		p.setY("2");
		p.setZ("3");
		final List<?> os = DOEUtils.expand(p);
		if (os.size()!=6) throw new Exception();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExpand2() throws Throwable {
		// Test furnce expansion
		final FurnaceParameters p = new FurnaceParameters();
		p.setTemperature("310; 315; 1");
		p.setTime(2);
		p.setTolerance(1);
		p.setX("1,1.1,1.2,1.3");
		p.setY("2,2.1");
		p.setZ("3,3.1");
		final List<FurnaceParameters> os = (List<FurnaceParameters>)DOEUtils.expand(p);
		if (os.size()!=96) throw new Exception();
		
		// Test that the temperature is on the outside loop as it has
		// a higher weighting factor.
		if (os.get(0).getTemperature().equals("310")) throw new Exception("First temp should be 310");
		if (os.get(10).getTemperature().equals("310")) throw new Exception("Temp should be 310");
		if (os.get(14).getTemperature().equals("310")) throw new Exception("Temp should be 310");
		if (os.get(15).getTemperature().equals("311")) throw new Exception("Temp should be 311");
		if (os.get(30).getTemperature().equals("311")) throw new Exception("Temp should be 311");
		if (os.get(31).getTemperature().equals("312")) throw new Exception("Temp should be 312");
	}
	
	@Test
	public void testDOEControl() throws Exception {
		
		final I20SampleParameters s = getTestSampleParams();
		
		s.setSampleEnvironment(I20SampleParameters.SAMPLE_ENV[3]);// Furnace
		List<?> os = DOEUtils.expand(s);
		assertEquals(24,os.size());
		
		s.setSampleEnvironment(I20SampleParameters.SAMPLE_ENV[2]);// Cryo
		os = DOEUtils.expand(s);
		assertEquals(2,os.size());
	}

	public I20SampleParameters getTestSampleParams() throws Exception {
		final String xml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
			"<I20SampleParameters>\n"+
			"<shouldValidate>true</shouldValidate>\n"+
			"<name>Please set a sample name...</name>\n"+
			"<description>Please add a description...</description>\n"+
			"<sampleWheelPosition>1</sampleWheelPosition>\n"+
			"<sampleEnvironment>Furnace</sampleEnvironment>\n"+
			"<roomTemperature>\n"+
			"<x>0.0</x>\n"+
			"<y>0.0</y>\n"+
			"<z>0.0</z>\n"+
			"<rotation>0.0</rotation>\n"+
			"<roll>0.0</roll>\n"+
			"<yaw>0.0</yaw>\n"+
			"</roomTemperature>\n"+
			"<cryostat>\n"+
			"<temperature>80.0, 90</temperature>\n"+
			"<tolerance>1.0</tolerance>\n"+
			"<time>60.0</time>\n"+
			"<heaterRange>1</heaterRange>\n"+
			"<profileType>PID</profileType>\n"+
			"<p>1.0</p>\n"+
			"<i>2.0</i>\n"+
			"<d>3.0</d>\n"+
			"<sampleHolder>2 Samples</sampleHolder>\n"+
			"<sampleNumber>2</sampleNumber>\n"+
			"<position>1.0</position>\n"+
			"<finePosition>0.1</finePosition>\n"+
			"</cryostat>\n"+
			"<furnace>\n"+
			"<temperature>312.0; 319.0; 1</temperature>\n"+
			"<tolerance>1.2</tolerance>\n"+
			"<time>5.0</time>\n"+
			"<x>6.9, 7.1, 8.0</x>\n"+
			"<y>4.7</y>\n"+
			"<z>2.5</z>\n"+
			"</furnace>\n"+
			"</I20SampleParameters>";

		final I20SampleParameters sampP = new I20SampleParameters();
		XMLHelpers.setFromXML(sampP, I20SampleParameters.mappingURL, I20SampleParameters.schemaURL, xml);

		return sampP;
	}

}
