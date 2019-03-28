/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.util.userOptions;

import java.util.Iterator;
import java.util.Map;

public class UserOptionsMap extends java.util.LinkedHashMap<String, UserOption> {

	public static final String propTitle = "title";
	public static final String propDefValue = "defaultValue";
	public static final String propKeyName = "keyName";
	public static final String propValue = "value";
	public static final String propType = "type";
	public static final String typeBoolean = "typeBoolean";
	public static final String typeString = "typeString";
	public static final String typeDouble = "typeDouble";
	public static final String typeInteger = "typeInteger";
	public static final String propDesc = "description";

	private String title;
	private Boolean isDefault;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof UserOptionsMap))
			return false;
		UserOptionsMap options = (UserOptionsMap) o;
		if (!options.getTitle().equals(title))
			return false;
		if (options.size() != size())
			return false;
		Iterator<Map.Entry<String, UserOption>> iter = entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, UserOption> entry = iter.next();
			if (!options.get(entry.getKey()).equals(entry.getValue()))
				return false;
		}
		return true;
	}

	public String getTitle() {
		return title;
	}

	public Boolean isDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
