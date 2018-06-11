/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.jython.server.shell;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.util.InteractiveInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to provide Highlighters for JythonShell.
 * <p>
 * Tries to create a syntax highlighter but defaults to an no-op implementation if the required
 * jython module (pygments) is not available.
 */
final class Highlighters {
	private static final Logger logger = LoggerFactory.getLogger(Highlighters.class);
	/** Default no-op Highlighter to use if Pygments is not available */
	private static final Highlighter NON_HIGHLIGHTER = (r, b) -> new AttributedString(b);
	/** Cache for Pygments based highlighters if they're available */
	private static final Map<String, Highlighter> HIGHLIGHTERS = new HashMap<>();
	static {
		resetCache();
	}

	protected static void resetCache() {
		HIGHLIGHTERS.clear();
		HIGHLIGHTERS.put(null, NON_HIGHLIGHTER);
		HIGHLIGHTERS.put("", NON_HIGHLIGHTER);
		HIGHLIGHTERS.put("none", NON_HIGHLIGHTER);
	}

	/** This class is not intended to be instantiated */
	private Highlighters() {}

	/** Try and create (and cache) pygments based highlighter */
	private static void initialiseHighlighter(String theme) {
		try {
			HIGHLIGHTERS.put(theme, new JythonPygmentsHighlighter(theme));
		} catch (InvalidThemeException ite) {
			logger.info("Unrecognised theme ({}). No highlighting will be used", theme);
			HIGHLIGHTERS.put(theme, NON_HIGHLIGHTER);
		} catch (Exception e) {
			logger.info("Highlighting is not available - pygments package is required");
		}
	}

	/**
	 * Get a syntax highlighter if available or a no-op highlighter if not.
	 * <p>
	 * If a highlighter is not available, subsequent calls will continue to check if the required packages
	 * become available.
	 * @return A {@link Highlighter}
	 */
	public static Highlighter getHighlighter(String theme) {
		if (!HIGHLIGHTERS.containsKey(theme)) {
			initialiseHighlighter(theme);
		}
		return HIGHLIGHTERS.getOrDefault(theme, NON_HIGHLIGHTER);
	}

	public static class InvalidThemeException extends RuntimeException {}

	/**
	 * A highlighter implementation providing Python syntax highlighting via the Pygments python library.
	 *
	 * @see <a href="http://pygments.org">pygments.org</a>
	 */
	private static class JythonPygmentsHighlighter implements Highlighter {
		private static final Logger logger = LoggerFactory.getLogger(JythonPygmentsHighlighter.class);
		private final PyObject highlight;
		private final String themeName;

		/**
		 * Create a highlighter and initialise the python environment used to highlight text
		 * @throws InvalidThemeException if the given theme is not valid
		 */
		public JythonPygmentsHighlighter(String theme) throws InvalidThemeException {
			requireNonNull(theme, "Theme must not be null");
			logger.debug("Creating highlighter for theme {}", theme);
			themeName = theme;
			try (InteractiveInterpreter console = new InteractiveInterpreter()) {
				console.exec("from pygments import highlight");
				console.exec("from pygments.lexers import PythonLexer");
				console.exec("from pygments.formatters import Terminal256Formatter");
				console.exec("_py_lexer = PythonLexer(ensurenl=False, stripnl=False, tabsize=4)");
				console.set("_GDA_THEME", theme);
				try {
					console.exec("_term_formatter = Terminal256Formatter(style=_GDA_THEME)");
				} catch (Exception e) {
					logger.debug("Error creating formatter with theme {}", theme, e);
					throw new InvalidThemeException();
				}
				console.exec(
						"def _highlight(src):\n" +
						"    return highlight(src, _py_lexer, _term_formatter)\n"
				);
				highlight = console.eval("_highlight");
			}
		}

		@Override
		public AttributedString highlight(LineReader reader, String buffer) {
			logger.trace("Highlighting: '{}'", buffer);
			PyObject py_ansi = highlight.__call__(new PyUnicode(buffer));
			logger.trace("highlighted to '{}'", py_ansi);
			return AttributedString.fromAnsi(py_ansi.toString());
		}

		@Override
		public String toString() {
			return String.format("PygmentsHighlighter(%s)", themeName);
		}
	}
}
