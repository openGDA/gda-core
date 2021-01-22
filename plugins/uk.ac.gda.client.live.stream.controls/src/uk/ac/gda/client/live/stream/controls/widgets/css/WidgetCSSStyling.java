package uk.ac.gda.client.live.stream.controls.widgets.css;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class WidgetCSSStyling {
	/**
	 * CSS ID for GTK native widgets defined in SWT, can be used to override default GTK Widget style
	 */
	public static final String SWT_INTERNAL_GTK_CSS="org.eclipse.swt.internal.gtk.css";

	private static final Logger logger = LoggerFactory.getLogger(WidgetCSSStyling.class);

	private WidgetCSSStyling() {
	}

	public static void applyStyling(Control control, String cssID, String platformUrlForCSSFile) {
		String cssStyle = loadCSSStyle(platformUrlForCSSFile);
		control.setData(cssID, cssStyle);
	}

	public static void applyStyling(Composite composite, String cssID, String platformUrlForCSSFile) {
		String cssStyle = loadCSSStyle(platformUrlForCSSFile);
		Arrays.stream(composite.getChildren()).forEach(e -> e.setData(cssID, cssStyle));
	}

	/**
	 * read CSS style file into string
	 * 
	 * @param platform_url_for_css_file format example
	 *                                  "platform:/plugin/plugin.is.where.css.file.is/css/test.css"
	 * @return
	 */
	private static String loadCSSStyle(String platformUrlForCSSFile) {

		try (InputStream inputStream = new URL(platformUrlForCSSFile).openConnection().getInputStream()) {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

			return in.lines().collect(Collectors.joining("\n;"));

		} catch (IOException e) {
			logger.error("IO exception while reading CSS file");
			return null;
		}
	}
}
