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

package gda.device;

import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoDs.TangoConst;

public class DummyDbDatum {

	private DbDatum dbDatum = null;
	private String propertyName;
	private String propertyValue;
	private int propertyType;
	
	public DummyDbDatum() {
	}
	
	public DummyDbDatum(DbDatum dbDatum) {
		this.dbDatum = dbDatum;
	}
	
	public DbDatum getDbDatum() {
		if (dbDatum == null) {
			if (propertyType == TangoConst.Tango_DEV_SHORT) {
				dbDatum = new DbDatum(propertyName, Short.parseShort(propertyValue));
			} else if (propertyType == TangoConst.Tango_DEV_LONG) {
				dbDatum = new DbDatum(propertyName, Integer.parseInt(propertyValue));
			} else if (propertyType == TangoConst.Tango_DEV_LONG64) {
				dbDatum = new DbDatum(propertyName, Long.parseLong(propertyValue));
			} else if (propertyType == TangoConst.Tango_DEV_DOUBLE) {
				dbDatum = new DbDatum(propertyName, Double.parseDouble(propertyValue));
			} else if (propertyType == TangoConst.Tango_DEV_FLOAT) {
				dbDatum = new DbDatum(propertyName, Float.parseFloat(propertyValue));
			} else if (propertyType == TangoConst.Tango_DEV_USHORT) {
				dbDatum = new DbDatum(propertyName, Short.parseShort(propertyValue));
			} else if (propertyType == TangoConst.Tango_DEV_ULONG) {
				dbDatum = new DbDatum(propertyName, Integer.parseInt(propertyValue));
			} else if (propertyType == TangoConst.Tango_DEV_BOOLEAN) {
				dbDatum = new DbDatum(propertyName, Boolean.parseBoolean(propertyValue));
			} else {
				dbDatum = new DbDatum(propertyName, propertyValue);
			}
			
		}
		return dbDatum;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public int getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(int propertyType) {
		this.propertyType = propertyType;
	}

}
