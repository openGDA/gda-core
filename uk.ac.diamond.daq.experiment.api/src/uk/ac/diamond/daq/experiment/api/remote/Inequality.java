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

package uk.ac.diamond.daq.experiment.api.remote;

import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;

/**
 * This type specifies the comparison that should be applied
 * between a signal provider and a reference target
 */
public enum Inequality {

	LESS_THAN("<"),
	LESS_THAN_OR_EQUAL_TO("<="),
	EQUAL_TO("="),
	GREATER_THAN_OR_EQUAL_TO(">="),
	GREATER_THAN(">");

	private String symbol;

	private Inequality(String symbol) {
		this.symbol = symbol;
	}

	public LimitCondition getLimitCondition(double argument) {
		switch (this) {
		case EQUAL_TO:
			return signal -> signal == argument;
		case GREATER_THAN:
			return signal -> signal > argument;
		case GREATER_THAN_OR_EQUAL_TO:
			return signal -> signal >= argument;
		case LESS_THAN:
			return signal -> signal < argument;
		case LESS_THAN_OR_EQUAL_TO:
			return signal -> signal <= argument;
		default:
			throw new IllegalStateException("Unknown inequality '" + this + '"');
		}
	}

	@Override
	public String toString() {
		return symbol;
	}
}