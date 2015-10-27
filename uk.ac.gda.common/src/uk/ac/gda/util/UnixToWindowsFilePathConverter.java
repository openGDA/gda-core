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

	/**
	 * See {@link #setBackSlash(boolean)}
	 */
	@Deprecated
	private boolean backSlash = true; //some plugins use forward slash for both windows and Unix but previous GDA version uses only different slash 


	@Override
	public String converttoInternal(String filepath) {
		
		String replace = StringUtils.replace(filepath, unixSubString, windowsSubString);
		if (backSlash) replace = StringUtils.replace(replace, "/", "\\");   
		return replace;
	}

	@Override
	public String converttoExternal(String filepath) {
		String replace = StringUtils.replace(filepath, windowsSubString, unixSubString);
		if (backSlash) replace = StringUtils.replace(replace, "\\", "/");
		return replace;
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

	/**
	 * See {@link #setBackSlash(boolean)}
	 */
	@Deprecated
	public boolean isBackSlash() {
		return backSlash;
	}

	/**
	 * If backslash conversion is not required, see {@link uk.ac.gda.util.SimpleFilePathConverter}
	 * 
	 * Instead of this class with unixSubString=X, windowsSubString=Y and backSlash=false
	 * use SimpleFilePathConverter with userSubString=X and internalSubString=Y
	 * @param backSlash
	 */
	@Deprecated
	public void setBackSlash(boolean backSlash) {
		this.backSlash = backSlash;
	}

}
