package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.function.Consumer;

import org.dawnsci.datavis.model.ILiveLoadedFileListener;
import org.dawnsci.datavis.model.LiveServiceManager;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.daq.experiment.api.ExperimentException;

/**
 * Updates a plot with the latest detector frame of the ongoing scan (at maximum 0.5 Hz)
 */
public class DetectorFramePeekView extends ViewPart {
	
	private IPlottingSystem<Composite> plot;
	
	@Override
	public void createPartControl(Composite parent) {
		
		Composite plotComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(plotComposite);
		GridDataFactory.fillDefaults().applyTo(plotComposite);
		
		try {
			plot = PlottingFactory.createPlottingSystem();
			plot.createPlotPart(plotComposite, "LatestDetectorFrame", null, PlotType.XY, null);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(plot.getPlotComposite());
		} catch (Exception e) {
			throw new ExperimentException(e);
		}
		
		Consumer<IDataset> framePlotter = latestFrame -> MetadataPlotUtils.plotDataWithMetadata(latestFrame, plot);
		final ILiveLoadedFileListener loadedFileListener = new LatestSwmrFrameFinder(framePlotter, 0.5);
		LiveServiceManager.getILiveFileService().addLiveFileListener(loadedFileListener);
		
		plotComposite.addDisposeListener(disposeEvent ->
			LiveServiceManager.getILiveFileService().removeLiveFileListener(loadedFileListener));
	}

	@Override
	public void setFocus() {
		plot.getPlotComposite().setFocus();
	}
	
}
