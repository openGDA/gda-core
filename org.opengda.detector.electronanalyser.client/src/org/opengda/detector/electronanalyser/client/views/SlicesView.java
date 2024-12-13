package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

public class SlicesView extends AbstractPlottingView {

	public static final String ID = "org.opengda.detector.electronanalyser.client.views.slicesview";
	SlicesPlotComposite plotComposite;

	public SlicesView() {
		setTitleToolTip("live display of integrated slices");
		setPartName("Slices");
	}

	@Override
	EpicsArrayPlotComposite createPlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		return new SlicesPlotComposite(this, parent, style);
	}
}
