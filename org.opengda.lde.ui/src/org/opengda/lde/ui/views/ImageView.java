package org.opengda.lde.ui.views;

import gda.device.Detector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageView extends ViewPart {

	private static final Logger logger=LoggerFactory .getLogger(ImageView.class);
	private Detector detector;
	private String arrayPV;

	public ImageView() {
		setTitleToolTip("live display of 2D matrix as image");
		// setContentDescription("A view for image.");
		setPartName("Image");
	}

	ImagePlotComposite imagePlotComposite;
	private RetargetAction energyMode;
	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		try {
			imagePlotComposite = new ImagePlotComposite(this, rootComposite, SWT.None);
			imagePlotComposite.setDetector(detector);
			imagePlotComposite.setArrayPV(arrayPV);
			imagePlotComposite.initialise();
		} catch (Exception e) {
			logger.error("Cannot create image plot composite.", e);
		}
	}

	@Override
	public void setFocus() {

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

	public Detector getDetector() {
		return detector;
	}

	public void setDetector(Detector detector) {
		this.detector = detector;
	}

}
