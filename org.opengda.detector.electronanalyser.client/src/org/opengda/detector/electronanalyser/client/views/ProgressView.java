package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressView extends ViewPart {

	private static final Logger logger=LoggerFactory .getLogger(ProgressView.class);
	private VGScientaAnalyser analyser;

	public ProgressView() {
		setTitleToolTip("display progress view");
		// setContentDescription("A view for displaying progresses.");
		setPartName("Progress");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new GridLayout());

		try {
			RegionProgressComposite regionProgressComposite = new RegionProgressComposite(rootComposite, SWT.None);
			regionProgressComposite.setAnalyser(getAnalyser());
		} catch (Exception e) {
			logger.error("Cannot create region progress composite.", e);
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
