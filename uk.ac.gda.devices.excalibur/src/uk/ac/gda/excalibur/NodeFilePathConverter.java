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

package uk.ac.gda.excalibur;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import uk.ac.gda.util.FilePathConverter;

public class NodeFilePathConverter implements FilePathConverter, InitializingBean {

	private String extSubString;
	private String intSubString;

	
	@Override
	public String converttoInternal(String filepath) {
		return filepath;
	}

	@Override
	public String converttoExternal(String filepath) {
		return StringUtils.replace(filepath, extSubString, intSubString);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (extSubString == null || intSubString==null )
			throw new IllegalArgumentException("extSubString == null || intSubString==null");
		
	}

	public String getExtSubString() {
		return extSubString;
	}

	public void setExtSubString(String extSubString) {
		this.extSubString = extSubString;
	}

	public String getIntSubString() {
		return intSubString;
	}

	public void setIntSubString(String intSubString) {
		this.intSubString = intSubString;
	}

}
