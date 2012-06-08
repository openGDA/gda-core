/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.structure;

import java.util.Iterator;
import java.util.Stack;

/**
 * Code originally written by SLAC TEAM (AIDA) Modified at Diamond A path is responsible for tokenizing string paths,
 * and dealing with special conventions such as "." and "..".
 */
class Path {
	private final static char separatorChar = '/';

	private final static String separatorString = new String(new char[] { separatorChar });

	private Stack<String> stack;

	/**
	 * Create an empty Path
	 */
	Path() {
		stack = new Stack<String>();
	}

	/**
	 * Create an new path from a start point and a relative or absolute path.
	 * 
	 * @param start
	 * @param path
	 */
	Path(Path start, String path) {
		int pos = 0;
		if (path == null)
			path = "";
		int l = path.length();
		if (l > 0 && path.charAt(0) == separatorChar) {
			stack = new Stack<String>();
			pos++;
		} else {
			stack = new Stack<String>();
			for (int i = 0; i < start.stack.size(); i++) {
				stack.add(start.stack.get(i));
			}
		}
		for (; pos < l;) {
			int next = path.indexOf(separatorChar, pos);
			if (next < 0)
				next = path.length();

			String token = path.substring(pos, next);
			pos = next + 1;
			if (token.equals("."))
				continue;
			else if (token.equals(""))
				continue;
			else if (token.equals("..")) {
				if (stack.isEmpty())
					throw new IllegalArgumentException("Hit rock bottom");
				stack.pop();
			} else
				stack.push(token);
		}
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(separatorChar);
		if (!stack.isEmpty()) {
			for (int i = 0;;) {
				b.append(stack.get(i));
				if (++i == stack.size())
					break;
				b.append(separatorChar);
			}
		}
		return b.toString();
	}

	/**
	 * To String method.
	 * 
	 * @param start
	 * @param stop
	 * @return string
	 */
	public String toString(int start, int stop) {
		if (start < 0 || start > stop || stop > size())
			throw new IllegalArgumentException("PATH: Wrong START or STOP points:  " + start + "   " + stop);
		StringBuffer b = new StringBuffer();
		b.append(separatorChar);
		if (!stack.isEmpty()) {
			for (int i = start; i < size();) {
				b.append(stack.get(i));
				if (++i >= stop)
					break;
				b.append(separatorChar);
			}
		}
		return b.toString();
	}

	/**
	 * @return parent folder
	 */
	Path parent() {
		return new Path(this, "..");
	}

	String getName() {
		return stack.isEmpty() ? separatorString : (String) stack.peek();
	}

	Iterator<String> iterator() {
		return stack.iterator();
	}

	int size() {
		return stack.size();
	}

	String[] toArray() {
		String[] result = new String[stack.size()];
		stack.copyInto(result);
		return result;
	}

	String[] toArray(int depth) {
		String[] result = new String[depth];
		for (int i = 0; i < depth; i++)
			result[i] = stack.get(i);
		return result;
	}

	String[] toArray(String item) {
		String[] result = new String[stack.size() + 1];
		stack.copyInto(result);
		result[stack.size()] = item;
		return result;
	}
}
