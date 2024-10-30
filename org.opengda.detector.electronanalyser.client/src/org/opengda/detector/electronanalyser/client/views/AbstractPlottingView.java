package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPlottingView extends LivePlotView {

	private static final Logger logger = LoggerFactory .getLogger(AbstractPlottingView.class);
	EpicsArrayPlotComposite plotComposite;

	public AbstractPlottingView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		final Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());
		try {
			plotComposite = createPlotComposite(this, rootComposite, SWT.None);
			configureAndInitialisePlotComposite(plotComposite);
			makeActions(getViewSite(), plotComposite);
			logger.info("Created \"{}\" plot view.", getPartName());
		} catch (Exception e) {
			logger.error("Error creating \"" + getPartName() + "\" plot view.", e);
		}
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(SequenceViewLive.ID, selectionListener);
	}

	private ISelectionListener selectionListener = (part, selection) -> {
		if (selection instanceof RegionRunCompletedSelection regionRunCompleted) {
			doRegionRunCompletedSelection(regionRunCompleted);
		} else if (selection instanceof IStructuredSelection sel && sel.getFirstElement() instanceof SESRegion region) {
			updateEnergyAxisActions(part, region.isEnergyModeBinding(), plotComposite);
		}
	};

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow()
			.getSelectionService()
			.removeSelectionListener(SequenceViewLive.ID, selectionListener);
		super.dispose();
	}

	@SuppressWarnings("unused")
	protected void doRegionRunCompletedSelection(RegionRunCompletedSelection regionRunCompleted ) {
		plotComposite.setNewRegion(true);
	}

	abstract EpicsArrayPlotComposite createPlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception;

	protected EpicsArrayPlotComposite getPlotComposite() {
		return plotComposite;
	}
}
