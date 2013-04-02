package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlicesView extends ViewPart {

	private static final Logger logger=LoggerFactory .getLogger(SlicesView.class);
	private VGScientaAnalyser analyser;

	public SlicesView() {
		setTitleToolTip("live display of integrated slices");
		// setContentDescription("A view for displaying integrated slices.");
		setPartName("Slices");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new GridLayout());

		try {
			SlicesPlotComposite slicesPlotComposite = new SlicesPlotComposite(this, rootComposite, SWT.None);
			slicesPlotComposite.setAnalyser(getAnalyser());
		} catch (Exception e) {
			logger.error("Cannot create slices plot composite.", e);
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
