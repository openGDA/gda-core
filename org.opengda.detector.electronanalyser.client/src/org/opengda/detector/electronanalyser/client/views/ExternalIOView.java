package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalIOView extends LivePlotView {

	public static final String ID = "org.opengda.detector.electronanalyser.client.views.externalioview";
	private static final Logger logger=LoggerFactory .getLogger(ExternalIOView.class);
	ExtIOPlotComposite plotComposite;

	public ExternalIOView() {
		setTitleToolTip("live display of external IO data");
		// setContentDescription("A view for external IO data.");
		setPartName("ExternalIO");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		try {
			plotComposite = new ExtIOPlotComposite(this, rootComposite, SWT.None);
			configureAndInitialisePlotComposite(plotComposite);
			makeActions(getViewSite(), plotComposite);
		} catch (Exception e) {
			logger.error("Cannot create external IO plot composite.", e);
		}
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(SequenceView.ID, selectionListener);
	}

	private ISelectionListener selectionListener = new INullSelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof RegionRunCompletedSelection) {
				plotComposite.setNewRegion(true);
			} else if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof Region) {
					updateEnergyAxisActions(part, firstElement, plotComposite);
				}
			}
		}
	};
	@Override
	public void dispose() {
		getViewSite()
		.getWorkbenchWindow()
		.getSelectionService()
		.removeSelectionListener(SequenceView.ID,
				selectionListener);
		super.dispose();
	}
}
