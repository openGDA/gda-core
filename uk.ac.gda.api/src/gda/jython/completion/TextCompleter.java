/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.jython.completion;


/**
 * Functional interface for anything that provides a list of completions ({@link AutoCompletion})
 * for a given string and cursor position
 * <br><br>
 * Takes {@code String} and {@code int} and returns {@link AutoCompletion}
 */
@FunctionalInterface
public interface TextCompleter {

	AutoCompletion getCompletionsFor(String line, int posn);

}