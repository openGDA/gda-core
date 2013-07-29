/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CrashTheVm {
	
	private CrashTheVm() {}

	public static void crashTheVm() {
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			Object unsafe = theUnsafeField.get(null);
			Method getByteMethod = unsafeClass.getMethod("getByte", long.class);
			getByteMethod.invoke(unsafe, 0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
