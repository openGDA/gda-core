package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageView extends ViewPart {

	private static final Logger logger=LoggerFactory .getLogger(ImageView.class);
	private VGScientaAnalyser analyser;

	public ImageView() {
		setTitleToolTip("live display of 2D matrix as image");
		// setContentDescription("A view for image.");
		setPartName("Image");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new GridLayout());

		try {
			ImagePlotComposite imagePlotComposite = new ImagePlotComposite(this, rootComposite, SWT.None);
			imagePlotComposite.setAnalyser(getAnalyser());
		} catch (Exception e) {
			logger.error("Cannot create image plot composite.", e);
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
