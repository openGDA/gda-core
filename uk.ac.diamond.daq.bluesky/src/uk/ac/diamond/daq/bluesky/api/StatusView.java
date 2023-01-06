/*-
 * Copyright © 2022 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY, without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.bluesky.api;

/**
 * Information about an active or previously active status in a Bluesky plan.
 * For example: The status of a motor moving to a destination.
 */
public record StatusView(
		/* Name of the status to display */
		String displayName,
		/* Current value of the status' progress if applicable, can be null */
		double current,
		/* Initial value of the status' progress if applicable, can be null */
		double initial,
		/* Target value of the status' progress if applicable, can be null */
		double target,
		/* Unit of the above values */
		String unit,
		/* Sensible precision to display the status with */
		int precision,
		/* Whether the status is complete */
		boolean done,
		/* Percentage value of the status' progress if applicable, can be null */
		double percentage,
		/* Time since the status started if applicable, can be null */
		double timeElapsed,
		/* Time remaining for the status if applicable, can be null */
		double timeRemaining) {}
