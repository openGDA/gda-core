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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Container for possible text completions<br>
 * <br>
 * Provides strings {@code before} and {@code after} that are the parts of the original line that
 * should remain either side of the cursor.
 * <br><br>
 * {@code options} is a list of {@link AutoCompleteOption}s that are possible
 * completions of the original string.<br><br>
 *
 * The final line should be<br>
 * {@literal >>> <before>|<chosen completion><after>}<br>
 */
public class AutoCompletion implements Serializable {
	private final String before;
	private final String after;
	private final Collection<AutoCompleteOption> options;
	private final int position;

	public AutoCompletion(String before, String after, Collection<AutoCompleteOption> options, int position) {
		this.before = before;
		this.after = after;
		this.options = new ArrayList<>(options);
		this.position = position;
	}

	/**
	 * Return the an empty AutoCompletion from a line and position
	 * @param line Current line
	 * @param posn Position of cursor in current line
	 * @return An AutoCompletion as if there were no completions available
	 */
	public static AutoCompletion noCompletions(String line, int posn) {
		return new AutoCompletion(line.substring(0, posn), line.substring(posn), new ArrayList<>(), posn);
	}

	public Collection<AutoCompleteOption> getOptions() {
		return Collections.unmodifiableCollection(options);
	}

	/**
	 * @return List of strings that could replace the current string section
	 */
	public Collection<String> getStrings() {
		return options.stream().map(o -> o.text).collect(Collectors.toList());
	}

	/**
	 * @return Cursor position that completion should be inserted a<br>
	 * 	This should be {@code before.length()}
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * The line up to the point where the completion is being inserted.<br>
	 * This is not always the cursor position where the completion was requested
	 * @return line up to completion position
	 */
	public String getBefore() {
		return before;
	}

	/**
	 * The text from the original line that was after the cursor position
	 * and should not be replaced by the completion
	 * @return String to be added after the completion
	 */
	public String getAfter() {
		return after;
	}
}