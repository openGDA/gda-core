package org.opengda.detector.electronanalyser.entrance.slit;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengda.detector.electronanalyser.entrance.slit.AnalyserEntranceSlit.EntranceSlit;

import gda.device.enumpositioner.DummyEnumPositioner;
import gda.factory.FactoryException;


class AnalyserEntranceSlitTest {
	private static final String SLIT_SHAPE = "Straight";
	private static final String SLIT_DIRECTION = "Vertical";
	private AnalyserEntranceSlit slit;
	private DummyEnumPositioner slitScannable;

	@BeforeEach
	void setUp() {
		slit = new AnalyserEntranceSlit();
		slit.setSlitShapePosition(0);
		slit.setSlitSizePosition(1);
		slit.setSplitSeparator(", ");
		slitScannable = new DummyEnumPositioner();
		slitScannable.setPositions(getSlitStringsList());
		slit.setSlitScannable(slitScannable);
	}

	List<String> getSlitStringsList(){
		return Arrays.asList(
			"Straight, S0.05, A0.5",
			"Straight, S0.1, A1.0",
			"Straight, S0.1, A1.3",
			"Straight, S0.1, A1.9",
			"Straight, S0.2, A1.0",
			"Straight, S0.2, A1.9",
			"Straight, S0.4, A1.9",
			"Straight, S3.2, A5.4",
			"Curved, S0.2, A1.0"
		);
	}

	@Test
	void defaultConstructorDoesNotSetSlits() {
		assertNull(slit.getCurrentSlit());
		assertNull(slit.getDefaultSlit());
	}


	@Test
	void testSetEntranceSlitsSet() {
		slit.setEntranceSlitsSet();
		assertNotNull(slit.entranceSlitsSet);
	}


	@Test
	void testParseEntranceSlitString() {
		int i = 5;
		EntranceSlit newSlit = slit.parseEntranceSlitString(getSlitStringsList().get(i), i);
		assertEquals(0.2, newSlit.getSize(),0.0001);
		assertEquals(SLIT_SHAPE, newSlit.getShape());
		assertEquals(SLIT_DIRECTION, newSlit.getDirection());
		assertEquals(i, newSlit.getRawValue(),0.0001);
	}

	@Test
	void getSizeByRawValueTest() throws FactoryException {
		slit.configure();
		assertEquals(0.05, slit.getSizeByRawValue(0), 0.0001);
	}

	@Test
	void testConfigureIsAlreadyConfigured() throws FactoryException {
		AnalyserEntranceSlit mockedSlit = spy(slit);
		when(mockedSlit.isConfigured()).thenReturn(true);
		assertTrue(mockedSlit.isConfigured());
		mockedSlit.configure();
		verify(mockedSlit,never()).setDefaultSlit(new EntranceSlit(0,0.0,"",""));
	}

	@Test
	void testConfigureWithNoSlitScannable() throws FactoryException {
		AnalyserEntranceSlit mockedSlit = spy(slit);
		when(mockedSlit.getSlitScannable()).thenReturn(null);
		mockedSlit.configure();
		assertNull(mockedSlit.getSlitScannable());
		assertNotNull(mockedSlit.defaultSlit);
		assertNull(mockedSlit.currentSlit);
		assertEquals(0.05, mockedSlit.getDefaultSlit().getSize(),0.0001);
		assertEquals(SLIT_SHAPE, mockedSlit.getDefaultSlit().getShape());
		assertEquals(SLIT_DIRECTION, mockedSlit.getDefaultSlit().getDirection());
		assertEquals(0, mockedSlit.getDefaultSlit().getRawValue(),0.0001);

		assertFalse(mockedSlit.isConfigured());
	}

	@Test
	void testConfigureWithSlitScannable() throws FactoryException {
		AnalyserEntranceSlit mockedSlit = spy(slit);
		assertNotNull(mockedSlit.getSlitScannable());
		mockedSlit.configure();
		assertNotNull(mockedSlit.defaultSlit);
		assertEquals(0.05, mockedSlit.getDefaultSlit().getSize(),0.0001);
		assertEquals(SLIT_SHAPE, mockedSlit.getDefaultSlit().getShape());
		assertEquals(SLIT_DIRECTION, mockedSlit.getDefaultSlit().getDirection());
		assertEquals(0, mockedSlit.getDefaultSlit().getRawValue(),0.0001);
		assertNotNull(mockedSlit.entranceSlitsSet);
		assertTrue(mockedSlit.isConfigured());
		verify(mockedSlit).update(null, null);
	}

	@Test
	void testReconfigure() throws FactoryException {
		AnalyserEntranceSlit mockedSlit = spy(slit);
		when(mockedSlit.isConfigured()).thenReturn(true);
		assertTrue(mockedSlit.isConfigured());
		mockedSlit.reconfigure();
		verify(mockedSlit).dispose();
		verify(mockedSlit).configure();
	}

	@Test
	void testUpdate() throws NumberFormatException {
		AnalyserEntranceSlit mockedSlit = spy(slit);
		mockedSlit.setEntranceSlitsSet();
		assertNotNull(mockedSlit.getSlitsRawValueList());
		mockedSlit.update(null, null);
		assertNotNull(mockedSlit.getCurrentSlit());
	}

	@Test
	void testSetCurrentSlitByRawValue() throws FactoryException {
		slit.configure();
		slit.setCurrentSlitByRawValue(0);
		assertNotNull(slit.getCurrentSlit());
		assertEquals(0.05, slit.getCurrentSlit().getSize(),0.0001);
		assertEquals(SLIT_SHAPE, slit.getCurrentSlit().getShape());
		assertEquals(SLIT_DIRECTION, slit.getCurrentSlit().getDirection());
		assertEquals(0, slit.getCurrentSlit().getRawValue(),0.0001);
	}

	@Test
	void testSetCurrentSlitByRawValueWrongValue() throws FactoryException {
		slit.configure();
		slit.setCurrentSlitByRawValue(1000);
		assertNotNull(slit.getCurrentSlit());
		assertEquals(slit.getCurrentSlit(),slit.getDefaultSlit());
	}

	@Test
	void testGetRawValue() throws FactoryException {
		slit.configure();
		assertEquals(0, slit.getRawValue());
	}

	@Test
	void testGetSizeInMM() throws FactoryException{
		slit.configure();
		assertEquals(0.05, slit.getSizeInMM(),0.001);
	}

	@Test
	void testGetShape() throws FactoryException {
		slit.configure();
		assertEquals(SLIT_SHAPE, slit.getShape());
		slit.setCurrentSlitByRawValue(getSlitStringsList().size()-1);
		assertEquals("Curved", slit.getShape());
	}

	@Test
	void testGetDirection() throws FactoryException {
		slit.configure();
		assertEquals(SLIT_DIRECTION, slit.getDirection());
	}

	@Test
	void testGetCurrentSlit() throws FactoryException{
		slit.configure();
		assertNotNull(slit.getCurrentSlit());
	}

	@Test
	void testSetDefaultSlitString() {
		int i = 1;
		slit.setDefaultSlitString(getSlitStringsList().get(i));
		assertEquals(slit.getDefaultSlitString(),getSlitStringsList().get(i));
	}

	@Test
	void testSetDefaultSlitStringEmpty() {
		slit.setDefaultSlitString("");
		assertEquals(slit.getDefaultSlitString(),AnalyserEntranceSlit.DEFAULT_SLIT_STRING);
	}

	@Test
	void testSetSlitScannable() {
		slit.setSlitScannable(slitScannable);
		assertNotNull(slit.getSlitScannable());
	}

	@Test
	void testGetDefaultSlit() throws FactoryException {
		slit.configure();
		assertNotNull(slit.getDefaultSlit());
	}

	@Test
	void testSetDefaultSlit() {
		EntranceSlit newSlit = new EntranceSlit(0,0.0,"","");
		slit.setDefaultSlit(newSlit);
		assertNotNull(slit.getDefaultSlit());
		assertEquals(newSlit, slit.getDefaultSlit());
	}

	@Test
	void testGetSlitsRawValueList() throws FactoryException {
		slit.configure();
		assertEquals(slit.getSlitsRawValueList().size(),getSlitStringsList().size());
	}

	@Test
	void testSetName() {
		slit.setName("mbs_slits");
		assertEquals(slit.getName(),"mbs_slits");
	}

	@Test
	void testSetSplitSeparator() {
		slit.setSplitSeparator(", ");
		assertEquals(slit.getSplitSeparator(),", ");
	}

	@Test
	void testSetSlitShapePosition() {
		int i = 1;
		slit.setSlitShapePosition(i);
		assertEquals(i,slit.getSlitShapePosition());
	}

	@Test
	void testSetSlitSizePosition() {
		int i = 2;
		slit.setSlitSizePosition(i);
		assertEquals(i,slit.getSlitSizePosition());
	}

	@Test
	void testSetSlitDirectionPosition() {
		int i = 3;
		slit.setSlitDirectionPosition(i);
		assertEquals(i,slit.getSlitDirectionPosition());
	}
}