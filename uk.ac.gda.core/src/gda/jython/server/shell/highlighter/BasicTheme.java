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

package gda.jython.server.shell.highlighter;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.server.shell.highlighter.TokenStreamHighlighter.SyntaxClass;
import gda.jython.server.shell.highlighter.TokenStreamHighlighter.Theme;

public class BasicTheme implements Theme {
	private static final Logger logger = LoggerFactory.getLogger(BasicTheme.class);
	private static final String THEME_DATA_RE = "(?<data>((\\d+)?(;(\\d+))?)(,((\\d+)?(;(\\d+))?)){6})"; // NOSONAR - group names are used in patterns
	private static final Pattern THEME_DATA_FORMAT = Pattern.compile(THEME_DATA_RE);
	private static final Pattern FILE_THEME_FORMAT = Pattern.compile("(?<name>[a-z_]+)\\(" + THEME_DATA_RE + "\\)");

	public static final int BOLD = 1 << 0;
	public static final int FAINT = 1 << 1;
	public static final int ITALIC = 1 << 2;
	public static final int UNDERLINE = 1 << 3;

	private final EnumMap<SyntaxClass, AttributedStyle> styleMap;

	public static Theme fromValue(String data) {
		if (THEME_DATA_FORMAT.matcher(data).matches()) {
			logger.debug("Using custom user theme: {}", data);
			return new BasicTheme(data);
		}
		return fromFile(data);
	}

	public static Theme fromFile(String name) {
		try (InputStream themeStream = TokenStreamHighlighter.class.getResourceAsStream("themes");
				InputStreamReader isr = new InputStreamReader(themeStream);
				BufferedReader reader = new BufferedReader(isr)) {
			Map<String, Theme> themes = reader.lines()
					.filter(line -> !line.isEmpty())
					.filter(line -> !line.startsWith("#"))
					.map(String::trim)
					.map(FILE_THEME_FORMAT::matcher)
					.filter(Matcher::matches)
					.collect(toMap(m -> m.group("name"), m -> new BasicTheme(m.group("data"))));
			var theme = themes.get(name);
			if (theme == null) {
				logger.warn("No theme named '{}' available", name);
				theme = Theme.NONE;
			} else {
				logger.debug("Using named theme '{}'", name);
			}
			return theme;
		} catch (IOException e) {
			logger.warn("Couldn't read themes file - using default", e);
			return Theme.NONE;
		}
	}

	/** Create theme from data - assume string is valid */
	private BasicTheme(String styleString) {
		String[] fields = styleString.split(",",-1);
		styleMap = parseStyle(fields);
	}

	@Override
	public AttributedStyle styleFor(SyntaxClass type) {
		return styleMap.getOrDefault(type, NO_STYLE);
	}

	private static EnumMap<SyntaxClass, AttributedStyle> parseStyle(String[] fields) {
		EnumMap<SyntaxClass, AttributedStyle> styles = new EnumMap<>(SyntaxClass.class);
		var iter = stream(fields).iterator();
		for (var syn: SyntaxClass.values()) {
			styles.put(syn, iter.hasNext() ? asStyle(iter.next()) : NO_STYLE);
		}
		return styles;
	}

	private static AttributedStyle asStyle(String styleString) {
		var parts = styleString.split(";");
		AttributedStyle style = fg(parts[0]);
		int mask;
		if (parts.length == 1) {
			mask = 0;
		} else {
			mask = Integer.valueOf(parts[1]);
		}
		if ((mask & BOLD) > 0) style = style.bold();
		if ((mask & FAINT) > 0) style = style.faint();
		if ((mask & ITALIC) > 0) style = style.italic();
		if ((mask & UNDERLINE) > 0) style = style.underline();
		return style;
	}

	private static AttributedStyle fg(String styleString) {
		if (styleString.isEmpty()) {
			return NO_STYLE;
		}
		return NO_STYLE.foreground(Integer.valueOf(styleString));
	}
}
