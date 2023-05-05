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

package gda.data.metadata;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 *
 * @deprecated use {@link NXMetaDataProvider#add(String, Object, String)} instead
 */
@Deprecated(forRemoval = true, since = "GDA 9.30")
class MetaDataUserSuppliedItem {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(MetaDataUserSuppliedItem.class);

	private String key;
	private Object value;
	private String units;

	@Deprecated
	public MetaDataUserSuppliedItem(String key, Object value, String units) {
		super();

		logger.deprecatedClass();
		this.setKey(key);
		this.setValue(value);
		this.setUnits(units);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}
}