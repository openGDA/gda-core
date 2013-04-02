package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalIOView extends ViewPart {

	private static final Logger logger=LoggerFactory .getLogger(ExternalIOView.class);
	private VGScientaAnalyser analyser;

	public ExternalIOView() {
		setTitleToolTip("live display of external IO data");
		// setContentDescription("A view for external IO data.");
		setPartName("ExternalIO");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new GridLayout());

		try {
			ExtIOPlotComposite externalIOPlotComposite = new ExtIOPlotComposite(this, rootComposite, SWT.None);
			externalIOPlotComposite.setAnalyser(getAnalyser());
		} catch (Exception e) {
			logger.error("Cannot create external IO plot composite.", e);
		}
	}

	@Override
	public void setFocus() {

	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);

	}

}
