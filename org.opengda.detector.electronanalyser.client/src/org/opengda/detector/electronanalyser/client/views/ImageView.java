package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

public class ImageView extends AbstractPlottingView {

	public static final String ID = "org.opengda.detector.electronanalyser.client.views.imageview";

	public ImageView() {
		setTitleToolTip("live display of 2D matrix as image");
		setPartName("Image");
	}

	@Override
	EpicsArrayPlotComposite createPlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		return new ImagePlotComposite(this, parent,	style);
	}
}
