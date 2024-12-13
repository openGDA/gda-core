package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

public class ExternalIOView extends AbstractPlottingView {

	public static final String ID = "org.opengda.detector.electronanalyser.client.views.externalioview";

	public ExternalIOView() {
		setTitleToolTip("live display of external IO data");
		setPartName("ExternalIO");
	}

	@Override
	EpicsArrayPlotComposite createPlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		return new ExtIOPlotComposite(part, parent, style);
	}
}
