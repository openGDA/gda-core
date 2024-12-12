/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.core.sampletransfer;

/**
 * Represents a message that includes the name of the sequence and the command to be executed.
 * @param sequence The name of the sequence
 * @param sample The sample selected by user
 * @param command The command to be executed on the sequence {@link SequenceCommand}
 *
 */
public record SequenceRequest (
		Sequence sequence,
		SampleSelection sample,
		SequenceCommand command) {}
