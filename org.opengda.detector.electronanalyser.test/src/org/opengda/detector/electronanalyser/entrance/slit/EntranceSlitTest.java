package org.opengda.detector.electronanalyser.entrance.slit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opengda.detector.electronanalyser.entrance.slit.AnalyserEntranceSlit.EntranceSlit;


class EntranceSlitTest {

	private EntranceSlit slit;

	@BeforeEach
	void setUp() {
		slit = new EntranceSlit(100, 0.1, "curved", "vertical");
	}

	@Test
	void constructorSetsRawValue() {
		assertEquals(100, slit.getRawValue());
	}

	@Test
	void constructorSetsSize() {
		assertEquals(0.1, slit.getSize(), 0.0001);
	}

	@Test
	void constructorSetsShape() {
		assertEquals("curved", slit.getShape());
	}

	@Test
	void constructorSetsDirection() {
		assertEquals("vertical", slit.getDirection());
	}

	@Test
	void toStringContainsAllFields() {
		String result = slit.toString();
		assertTrue(result.contains("rawValue=100"));
		assertTrue(result.contains("size=0.1"));
		assertTrue(result.contains("direction=vertical"));
		assertTrue(result.contains("shape=curved"));
	}

	@Test
	void toStringMatchesExactFormat() {
		String expected = "EntranceSlit [rawValue=100, size=0.1, direction=vertical, shape=curved]";
		assertEquals(expected, slit.toString());
	}

	// --- Edge cases ---

	@Test
	void acceptsZeroRawValue() {
		EntranceSlit s = new EntranceSlit(0, 0.0, "straight", "horizontal");
		assertEquals(0, s.getRawValue());
		assertEquals(0.0, s.getSize());
	}

	@Test
	void acceptsNegativeRawValue() {
		// document current behavior: no validation is performed by the constructor
		EntranceSlit s = new EntranceSlit(-100, 0.1, "aperture", "vertical");
		assertEquals(-100, s.getRawValue());
	}

	@Test
	void acceptsNullShapeAndDirection() {
		// again documents current behavior — no null-checks in constructor
		EntranceSlit s = new EntranceSlit(200, 0.2, null, null);
		assertNull(s.getShape());
		assertNull(s.getDirection());
	}

	@ParameterizedTest
	@CsvSource({
		"100, 0.1, curved, vertical",
		"200, 0.2, straight, horizontal",
		"300, 0.3, aperture, vertical"
	})
	void multipleValidCombinations(int rawValue, double size, String shape, String direction) {
		EntranceSlit s = new EntranceSlit(rawValue, size, shape, direction);
		assertEquals(rawValue, s.getRawValue());
		assertEquals(size, s.getSize(), 0.0001);
		assertEquals(shape, s.getShape());
		assertEquals(direction, s.getDirection());
	}
}