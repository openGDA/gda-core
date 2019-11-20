package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.Set;
import java.util.function.Consumer;

import org.dawnsci.datavis.model.LiveServiceManager;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.daq.experiment.api.ExperimentException;

/**
 * Updates a plot with the latest detector frame of the ongoing scan (at maximum 0.5 Hz)
 */
public class DetectorFramePeekView extends ViewPart {
	
	private ComboViewer detectorSelector;
	private IPlottingSystem<Composite> plot;
	
	private LatestSwmrFrameFinder frameFinder;
	private String selectedDetector;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite viewComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(viewComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(viewComposite);
		
		createDetectorSelector(viewComposite);
		createPlot(viewComposite);
		
		attachSwmrListener(viewComposite);
	}

	private void createDetectorSelector(Composite parent) {
		Composite datasetSelectorComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(datasetSelectorComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(datasetSelectorComposite);
		
		new Label(datasetSelectorComposite, SWT.NONE).setText("Detector:");
		
		detectorSelector = new ComboViewer(datasetSelectorComposite, SWT.READ_ONLY);
		detectorSelector.setContentProvider(ArrayContentProvider.getInstance());
		detectorSelector.addSelectionChangedListener(this::datasetSelectionChanged);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(detectorSelector.getControl());
	}

	private void createPlot(Composite parent) {
		Composite plotComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(plotComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(plotComposite);
		
		try {
			plot = PlottingFactory.createPlottingSystem();
			plot.createPlotPart(plotComposite, "LatestDetectorFrame", null, PlotType.XY, null);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(plot.getPlotComposite());
		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}
	
	/**
	 * Need a composite to attach a dispose listener
	 */
	private void attachSwmrListener(Composite parent) {
		Consumer<IDataset> framePlotter = latestFrame -> MetadataPlotUtils.plotDataWithMetadata(latestFrame, plot);
		frameFinder = new LatestSwmrFrameFinder(this::updateDetectors, framePlotter, 0.5);
		
		LiveServiceManager.getILiveFileService().addLiveFileListener(frameFinder);
		
		parent.addDisposeListener(disposeEvent ->
			LiveServiceManager.getILiveFileService().removeLiveFileListener(frameFinder));
	}

	/**
	 * Populate the combo box with the detector names available in the current scan,
	 * select previous selection if it's still available, otherwise the first element
	 */
	private void updateDetectors(Set<String> detectors) {
		Display.getDefault().asyncExec(() -> {
			detectorSelector.setInput(detectors);
			if (selectedDetector != null && detectors.contains(selectedDetector)) {
				detectorSelector.setSelection(new StructuredSelection(selectedDetector));
			} else {
				detectorSelector.setSelection(new StructuredSelection(detectors.iterator().next()));
			}
		});
	}
	
	private void datasetSelectionChanged(SelectionChangedEvent event) {
		selectedDetector = (String) ((StructuredSelection) event.getSelection()).getFirstElement();
		frameFinder.selectDetector(selectedDetector);
	}

	@Override
	public void setFocus() {
		plot.getPlotComposite().setFocus();
	}
	
}
