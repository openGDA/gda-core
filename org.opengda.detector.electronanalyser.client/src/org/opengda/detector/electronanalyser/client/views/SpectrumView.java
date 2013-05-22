package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.SequenceViewExtensionFactory;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectrumView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(SpectrumView.class);
	private IVGScientaAnalyser analyser;
	private String arrayPV;

	public SpectrumView() {
		setTitleToolTip("live display of integrated spectrum");
		// setContentDescription("A view for displaying integrated spectrum.");
		setPartName("Spectrum");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		try {
			spectrumPlotComposite = new SpectrumPlotComposite(this, rootComposite, SWT.None);
			spectrumPlotComposite.setAnalyser(getAnalyser());
			spectrumPlotComposite.setArrayPV(getArrayPV());
			spectrumPlotComposite.initialise();
		} catch (Exception e) {
			logger.error("Cannot create spectrum plot composite.", e);
		}
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(SequenceViewExtensionFactory.ID, selectionListener);
	}

	private ISelectionListener selectionListener = new INullSelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof RegionRunCompletedSelection) {
				spectrumPlotComposite.setNewRegion(true);
				spectrumPlotComposite.updateStat();
			} 
		}
	};


	private SpectrumPlotComposite spectrumPlotComposite;	@Override
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
