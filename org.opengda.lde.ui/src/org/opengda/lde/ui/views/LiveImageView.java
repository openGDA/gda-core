package org.opengda.lde.ui.views;

import gda.device.Detector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveImageView extends ViewPart {
	public static final String ID = "org.opengda.lde.ui.views.liveimageview";
	private static final Logger logger=LoggerFactory .getLogger(LiveImageView.class);
	private Detector detector;
	private String arrayPV;

	public LiveImageView() {
		setTitleToolTip("live display of 2D matrix as image");
		// setContentDescription("A view for image.");
		setPartName("Image");
	}

	LiveImagePlotComposite imagePlotComposite;
	private int xDimension;
	private int yDimension;
	private String arrayEnablePV;
	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		try {
			imagePlotComposite = new LiveImagePlotComposite(this, rootComposite, SWT.None);
			imagePlotComposite.setPlotName(getPartName());
			imagePlotComposite.setxDimension(getxDimension());
			imagePlotComposite.setyDimension(getyDimension());
			imagePlotComposite.setArrayPV(arrayPV);
			imagePlotComposite.setArrayEnablePV(arrayEnablePV);
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

	public int getxDimension() {
		return xDimension;
	}

	public void setxDimension(int xDimension) {
		this.xDimension = xDimension;
	}

	public int getyDimension() {
		return yDimension;
	}

	public void setyDimension(int yDimension) {
		this.yDimension = yDimension;
	}

	public void setArrayEnablePV(String arrayEnablePV) {
		this.arrayEnablePV=arrayEnablePV;
		
	}
}
