/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.device.models;

public class ExpressionWatchdogModel extends AbstractDeviceWatchdogModel {

	/**
	 * This is an expression of scannables which are monitored and the expression
	 * reevaluated if they change during a scan. Each variable in the scan *must*
	 * be a scannable name. The expression will be evaluated using JEXL which is
	 * available through the IExpressionService. Any expression
	 */
	private String expression; // e.g. 'beamcurrent >= 1.0 && !portshutter.equalsIgnoreCase("Closed")'

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionWatchdogModel other = (ExpressionWatchdogModel) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExpressionWatchdogModel [expression=" + expression + "]";
	}
}
