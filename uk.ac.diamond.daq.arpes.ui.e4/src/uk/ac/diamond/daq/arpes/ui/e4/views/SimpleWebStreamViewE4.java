package uk.ac.diamond.daq.arpes.ui.e4.views;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleWebStreamViewE4 {
	private static final Logger logger = LoggerFactory.getLogger(SimpleWebStreamViewE4.class);
	private Browser browser;

	@Inject
	@Named("cameraUrl")
	@Active
	@Optional
	String cameraUrl;

	@PostConstruct
	public void createControls(Composite parent) {
		parent.setLayout(new FillLayout());

		browser = new Browser(parent, SWT.NONE);
		browser.setMenu(null);

		loadStream();
	}


	private void loadStream() {
		if (browser == null || browser.isDisposed()) {
			return;
		}

		// Fallback if not provided
		String url = (cameraUrl != null && !cameraUrl.isBlank())
				? cameraUrl
				: "about:blank";

		// Use HTML wrapper for better MJPEG compatibility
		String html = """
			<html>
			<body style="margin:0;padding:0;overflow:hidden;background:black;">
				<img src=\"%s\" style="width:100%%;height:100%%;object-fit:contain;" />
			</body>
			</html>
			""".formatted(url);

		browser.setText(html);
		logger.debug("Created simple view based on camera URL {}", cameraUrl);
	}

	@PreDestroy
	public void dispose() {
		if (browser != null && !browser.isDisposed()) {
			browser.dispose();
		}
	}

}
