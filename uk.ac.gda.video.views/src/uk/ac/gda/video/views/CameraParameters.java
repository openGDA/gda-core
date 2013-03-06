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

package uk.ac.gda.video.views;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

public class CameraParameters implements InitializingBean{
	
	private String name;
	private String mjpegURL;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMjpegURL() {
		return mjpegURL;
	}
	public void setMjpegURL(String mjpegURL) {
		this.mjpegURL = mjpegURL;
	}
	protected void checkStringNotNull(String stringtoTest, String labelForException ){
		if( stringtoTest == null  || !StringUtils.hasLength(stringtoTest))
			throw new IllegalArgumentException(labelForException + " is null or empty");
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		checkStringNotNull(name, "name");
		checkStringNotNull(mjpegURL, "mjpegURL");
	}
}
