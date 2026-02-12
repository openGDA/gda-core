/*-
 * Copyright © 2026 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.camera;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import gda.factory.FindableBase;

public final class PresetMjpegUrlSupplier extends FindableBase implements InitializingBean {

	private String urlSpec = "";

	@Override
	public void afterPropertiesSet() throws IllegalStateException {
		if (!isUrlSet())
			throw new IllegalStateException("URL has not been specified");
	}

	@Override
	public String toString() {
		return urlSpec;
	}

	/**
	 * Identify that the url has been set on the MotionJpegReceiver
	 *
	 * @return {@code true} if the urlspec is not null
	 */
	public boolean isUrlSet() {
		return StringUtils.hasText(urlSpec);
	}

	public void setUrl(String url) {
		urlSpec = url;
	}
}
