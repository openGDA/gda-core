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

package gda.mscan.element;

import java.util.List;
import java.util.Map;

/**
 * Holder for transformations that can be applied to the output data (nexus file) of an mscan which maps the
 * transformation to type of object used to define its parameters
 */
public enum ScanDataConsumer implements IMScanElementEnum {
	TEMPLATE("temp", List.class),
	PROCESSOR("proc", Map.class);

	private final String text;
	private final Class<?> paramsType;

	private ScanDataConsumer(final String text, final Class<?> type) {
		this.text = text;
		this.paramsType = type;
		}

	public String getText() {
		return text;
	}

	public Class<?> getParamsType() {
		return paramsType;
	}
}
