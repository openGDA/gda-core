package gda.device;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MotorStatusTest {

	@Test
	public void testValue() {
		assertEquals(0, MotorStatus.UPPER_LIMIT.value());
		assertEquals(1, MotorStatus.LOWER_LIMIT.value());
		assertEquals(2, MotorStatus.FAULT.value());
		assertEquals(3, MotorStatus.READY.value());
		assertEquals(4, MotorStatus.BUSY.value());
		assertEquals(5, MotorStatus.UNKNOWN.value());
		assertEquals(6, MotorStatus.SOFT_LIMIT_VIOLATION.value());
	}

	@Test
	public void testFromInt() {
		assertEquals(MotorStatus.UPPER_LIMIT, MotorStatus.fromInt(0));
		assertEquals(MotorStatus.LOWER_LIMIT, MotorStatus.fromInt(1));
		assertEquals(MotorStatus.FAULT, MotorStatus.fromInt(2));
		assertEquals(MotorStatus.READY, MotorStatus.fromInt(3));
		assertEquals(MotorStatus.BUSY, MotorStatus.fromInt(4));
		assertEquals(MotorStatus.UNKNOWN, MotorStatus.fromInt(5));
		assertEquals(MotorStatus.SOFT_LIMIT_VIOLATION, MotorStatus.fromInt(6));
	}

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testFromIntTooHigh() {
		MotorStatus.fromInt(7);
	}

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testFromIntTooLow() {
		MotorStatus.fromInt(-1);
	}
}
