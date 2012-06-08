package gda.server.collisionAvoidance;

import java.lang.reflect.Array;

/**
 * Convenience method for producing a simple textual representation of an array.
 * <P>
 * The format of the returned <code>String</code> is the same as <code>AbstractCollection.toString</code>:
 * <ul>
 * <li>non-empty array: [blah, blah]
 * <li>empty array: []
 * <li>null array: null
 * </ul>
 * 
 * @author Jerome Lacoste
 * @author www.javapractices.com
 */
public final class ArrayToString {

	/**
	 * <code>aArray</code> is a possibly-null array whose elements are primitives or objects; arrays of arrays are
	 * also valid, in which case <code>aArray</code> is rendered in a nested, recursive fashion.
	 * 
	 * @param aArray
	 * @return Array if possible
	 */
	public static String get(Object aArray) {
		if (aArray == null)
			return fNULL;
		checkObjectIsArray(aArray);

		StringBuffer result = new StringBuffer(fSTART_CHAR);
		int length = Array.getLength(aArray);
		for (int idx = 0; idx < length; ++idx) {
			Object item = Array.get(aArray, idx);
			if (isNonNullArray(item)) {
				// recursive call!
				result.append(get(item));
			} else {
				result.append(item);
			}
			if (!isLastItem(idx, length)) {
				result.append(fSEPARATOR);
			}
		}
		result.append(fEND_CHAR);
		return result.toString();
	}

	// PRIVATE //
	private static final String fSTART_CHAR = "[";
	private static final String fEND_CHAR = "]";
	private static final String fSEPARATOR = ", ";
	private static final String fNULL = "null";

	private static void checkObjectIsArray(Object aArray) {
		if (!aArray.getClass().isArray()) {
			throw new IllegalArgumentException("Object is not an array.");
		}
	}

	private static boolean isNonNullArray(Object aItem) {
		return aItem != null && aItem.getClass().isArray();
	}

	private static boolean isLastItem(int aIdx, int aLength) {
		return (aIdx == aLength - 1);
	}

	/**
	 * Test harness.
	 * 
	 * @param args
	 */
	public static void main(String... args) {

		boolean[] booleans = { true, false, false };
		char[] chars = { 'B', 'P', 'H' };
		byte[] bytes = { 3 };
		short[] shorts = { 5, 6 };
		int[] ints = { 7, 8, 9, 10 };
		long[] longs = { 100, 101, 102 };
		float[] floats = { 99.9f, 63.2f };
		double[] doubles = { 212.2, 16.236, 42.2 };
		String[] strings = { "blah", "blah", "blah" };
		java.util.Date[] dates = { new java.util.Date(), new java.util.Date() };
		System.out.println("booleans: " + get(booleans));
		System.out.println("chars: " + get(chars));
		System.out.println("bytes: " + get(bytes));
		System.out.println("shorts: " + get(shorts));
		System.out.println("ints: " + get(ints));
		System.out.println("longs: " + get(longs));
		System.out.println("floats: " + get(floats));
		System.out.println("double: " + get(doubles));
		System.out.println("strings: " + get(strings));
		System.out.println("dates: " + get(dates));

		int[] nullInts = null;
		int[] emptyInts = {};
		String[] emptyStrings = { "", "" };
		String[] nullStrings = { null, null };
		System.out.println("null ints: " + get(nullInts));
		System.out.println("empty ints: " + get(emptyInts));
		System.out.println("empty Strings: " + get(emptyStrings));
		System.out.println("null Strings: " + get(nullStrings));

		String[] arrayA = { "A", "a" };
		String[] arrayB = { "B", "b" };
		String[][] arrayOfArrays = { arrayA, arrayB };
		System.out.println("array Of Arrays: " + get(arrayOfArrays));
	}
}