package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;

public class SpectrumView extends AbstractPlottingView {

	public static final String ID = "org.opengda.detector.electronanalyser.client.views.spectrumview";

	public SpectrumView() {
		setTitleToolTip("live display of integrated spectrum");
		setPartName("Spectrum");
	}
	@Override
	EpicsArrayPlotComposite createPlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		return new SpectrumPlotComposite(part, parent, style);
	}

	@Override
	protected void doRegionRunCompletedSelection(RegionRunCompletedSelection regionRunCompleted) {
		if (getPlotComposite() instanceof SpectrumPlotComposite spectrumComposite) spectrumComposite.updateStat();
		super.doRegionRunCompletedSelection(regionRunCompleted);
	}
}
