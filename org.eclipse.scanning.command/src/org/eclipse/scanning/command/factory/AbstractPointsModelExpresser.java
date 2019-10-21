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

package org.eclipse.scanning.command.factory;

import org.eclipse.scanning.api.points.models.AbstractPointsModel;

public abstract class AbstractPointsModelExpresser<T extends AbstractPointsModel> extends PyModelExpresser<T> {

	public void appendCommonProperties(StringBuilder sb, T model, boolean verbose) {
		sb.append(", ");
		sb.append(getBooleanPyExpression("alternating", model.isAlternating(), verbose));
		sb.append(", ");
		sb.append(getBooleanPyExpression("continuous", model.isContinuous(), verbose));
	}
}
