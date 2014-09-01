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

package uk.ac.gda.util;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;


/**
 * Converts between an "internal" filepath and an "external" (user) filepath
 */

public class SimpleFilePathConverter implements FilePathConverter, InitializingBean {

	private String internalSubString;
	private String userSubString;

	@Override
	public String converttoInternal(String filepath) {
		return StringUtils.replace(filepath, userSubString, internalSubString);
	}

	@Override
	public String converttoExternal(String filepath) {
		return StringUtils.replace(filepath, internalSubString, userSubString);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (internalSubString == null || userSubString==null ) {
			throw new IllegalArgumentException("internalSubString == null || userSubString==null");
		}
	}

	public String getInternalSubString() {
		return internalSubString;
	}

	public void setInternalSubString(String internalSubString) {
		this.internalSubString = internalSubString;
	}

	public String getUserSubString() {
		return userSubString;
	}

	public void setUserSubString(String userSubString) {
		this.userSubString = userSubString;
	}
}
