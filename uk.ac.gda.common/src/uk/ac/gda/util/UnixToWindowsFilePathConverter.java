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

public class UnixToWindowsFilePathConverter implements FilePathConverter, InitializingBean {

	private String windowsSubString;
	private String unixSubString;

	
	@Override
	public String converttoInternal(String filepath) {
		
		String replace = StringUtils.replace(filepath, unixSubString, windowsSubString);
		return StringUtils.replace(replace, "/", "\\");
	}

	@Override
	public String converttoExternal(String filepath) {
		String replace = StringUtils.replace(filepath, windowsSubString, unixSubString);
		return StringUtils.replace(replace, "\\", "/");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (windowsSubString == null || unixSubString==null )
			throw new IllegalArgumentException("windowsSubString == null || unixSubString==null");
		
	}

	public String getWindowsSubString() {
		return windowsSubString;
	}

	public void setWindowsSubString(String windowsSubString) {
		this.windowsSubString = windowsSubString;
	}

	public String getUnixSubString() {
		return unixSubString;
	}

	public void setUnixSubString(String unixSubString) {
		this.unixSubString = unixSubString;
	}



}
