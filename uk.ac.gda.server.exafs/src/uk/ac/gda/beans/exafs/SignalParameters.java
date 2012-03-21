/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

/**
 *
 */
public class SignalParameters  implements Serializable{
	private String label;
	private String name;
	private String expression;
	private String dataFormat="6.4f";
	private String scannableName;

	/**
	 * 
	 */
	public SignalParameters() {
		
	}

	/**
	 * @param label 
	 * @param name 
	 * @param dataFormat
	 * @param expression 
	 * @param scannableName 
	 */
	public SignalParameters(String label, String name, String dataFormat, String expression, String scannableName) {
		this.label = label;
		this.name = name;
		this.dataFormat = dataFormat;
		this.expression = expression;
		this.scannableName = scannableName;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the deviceName
	 */
	public String getDataFormat() {
		return dataFormat;
	}

	/**
	 * @param dataFormat
	 *            the dataFormat to set
	 */
	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return Returns the expression.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression The expression to set.
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * @return Returns the scannableName.
	 */
	public String getScannableName() {
		return scannableName;
	}

	/**
	 * @param scannableName The scannableName to set.
	 */
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataFormat == null) ? 0 : dataFormat.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SignalParameters other = (SignalParameters) obj;
		if (dataFormat == null) {
			if (other.dataFormat != null) {
				return false;
			}
		} else if (!dataFormat.equals(other.dataFormat)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (dataFormat == null) {
			if (other.dataFormat != null) {
				return false;
			}
		} else if (!dataFormat.equals(other.dataFormat)) {
			return false;
		}
		if (expression == null) {
			if (other.expression != null) {
				return false;
			}
		} else if (!expression.equals(other.expression)) {
			return false;
		}
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		if (scannableName == null) {
			if (other.scannableName != null) {
				return false;
			}
		} else if (!scannableName.equals(other.scannableName)) {
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

}
