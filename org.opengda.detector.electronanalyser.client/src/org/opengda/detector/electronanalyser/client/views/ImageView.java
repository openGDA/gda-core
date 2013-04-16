package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageView extends ViewPart {

	private static final Logger logger=LoggerFactory .getLogger(ImageView.class);
	private IVGScientaAnalyser analyser;
	private String arrayPV;

	public ImageView() {
		setTitleToolTip("live display of 2D matrix as image");
		// setContentDescription("A view for image.");
		setPartName("Image");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		try {
			ImagePlotComposite imagePlotComposite = new ImagePlotComposite(this, rootComposite, SWT.None);
			imagePlotComposite.setAnalyser(getAnalyser());
			imagePlotComposite.setArrayPV(arrayPV);
			imagePlotComposite.initialise();
		} catch (Exception e) {
			logger.error("Cannot create image plot composite.", e);
		}
	}

	@Override
	public void setFocus() {

	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);

	}

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}

}
