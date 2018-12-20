package org.opengda.lde.ui.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringUtils {
	/**
	 * returns the largest integer number at the end of a string in a list of strings with the same prefix string.
	 * @param strings
	 * @param prefix
	 * @return
	 */
	public static int largestIntAtEndStringsWithPrefix(List<String> strings,String prefix) {
		List<String> names = stringsStartWithPrefix(strings,prefix);
		List<Integer> numbers = new ArrayList<Integer>();
		if (!names.isEmpty()) {
			for (String name : names) {
				if (intAtEnd(name) != -1) {
					numbers.add(intAtEnd(name));
				}
			}
			if (numbers.isEmpty()) {
				return 0;
			}
			return Collections.max(numbers);
		}
		return -1;
	}
	/**
	 * returns a sub-list of strings that start with the specified prefix.
	 * @param strings
	 * @param prefix
	 * @return
	 */
	public static List<String> stringsStartWithPrefix(List<String> strings,String prefix) {
		List<String> names = new ArrayList<String>();
		for (String string : strings) {
			if (string.startsWith(prefix)) {
				names.add(string);
			}
		}
		return names;
	}
	/**
	 * returns the integer number at the end of a string.
	 * @param string
	 * @return integer number
	 */
	public static int intAtEnd(String string) {
		int i, j;
		i = j = string.length();
		while (--i > 0) {
			if (Character.isDigit(string.charAt(i)))
				continue;
			i++;
			break;
		}
		if (j - i >= 1)
			return Integer.parseInt(string.substring(i));
		return -1;
	}
	/**
	 * returns the prefix string before the integer number at the end of this string.
	 * @param name
	 * @return prefix as string
	 */
	public static String prefixBeforeInt(String name) {
		int i, j;
		i = j = name.length();
		while (--i > 0) {
			if (Character.isDigit(name.charAt(i)))
				continue;
			i++;
			break;
		}
		if (j - i >= 1)
			return name.substring(0, i);
		return name;
	}
}
