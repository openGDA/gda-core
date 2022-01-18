package uk.ac.gda.sisa.ui;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.view.LiveStreamView;

public class LineProfilePart {
	
	private static final Logger logger = LoggerFactory.getLogger(LineProfilePart.class);
	private IPlottingSystem<Composite> sourcePlottingSystem;
	private IPlottingSystem<Composite> xAxisProfilePlot;
	private IPlottingSystem<Composite> yAxisProfilePlot;
	
	private static final String X_REGION_NAME = "x_profile_region";
	private static final String Y_REGION_NAME = "y_profile_region";
	private IImageTrace sourceTrace;
	private IRegion xregion;
	private IRegion yregion;
	
	private ITraceListener traceListener;
	private IROIListener xroiListener;
	private IROIListener yroiListener;
	private IRegionListener regionListener;

	
	@Inject
	public LineProfilePart(@Named("stream_id") @Active String streamID,
			@Named("stream_type") @Active String streamType) {
		
		IViewReference viewReference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findViewReference(LiveStreamView.ID, String.join("#", streamID, streamType));
		
		if (viewReference != null) {
			LiveStreamView liveStreamView = (LiveStreamView)viewReference.getView(false);
			sourcePlottingSystem = liveStreamView.getPlottingSystem();
		} else {
			String liveStreamError = "Could not find live stream view and therefore could not get plotting system";
			logger.error(liveStreamError);
			throw new IllegalStateException(liveStreamError);
		}
		
		
		// Create new plotting systems for data to go on
		IPlottingService plottingService = PlatformUI.getWorkbench().getService(IPlottingService.class);
		try {
			xAxisProfilePlot = plottingService.createPlottingSystem();
			yAxisProfilePlot = plottingService.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Could not create new plotting systems");
		}
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		sourceTrace = (IImageTrace)sourcePlottingSystem.getTraces().iterator().next();
		
		// Draw and initialise plots on parent composite
		xAxisProfilePlot.createPlotPart(parent, "x_profile_plot", null, PlotType.XY, null);
		yAxisProfilePlot.createPlotPart(parent, "y_profile_plot", null, PlotType.XY, null);
		yAxisProfilePlot.setShowLegend(false);
		xAxisProfilePlot.setShowLegend(false);
		xAxisProfilePlot.getAxes().get(0).setTitle("Y Profile");
		yAxisProfilePlot.getAxes().get(0).setTitle("X Profile");
		
		xroiListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				double pointX = evt.getROI().getPointX();
				double widthX = ((RectangularROI)(evt.getROI())).getLengths()[0];
				IDataset sliceXSum = generateXAxisDataset((int)pointX, widthX);
				updateProfilePlot(xAxisProfilePlot, sliceXSum);
			}
		};
		
		yroiListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				double pointY = evt.getROI().getPointY();
				double widthY = ((RectangularROI)(evt.getROI())).getLengths()[1];
				IDataset sliceYSum = generateYAxisDataset((int)pointY, widthY);
				updateProfilePlot(yAxisProfilePlot, sliceYSum);
			}
		};
		
		traceListener = new ITraceListener.Stub() {

			@Override
			public void traceUpdated(TraceEvent event) {
				if (event.getSource() instanceof IImageTrace) {
					sourceTrace = (IImageTrace) event.getSource();
					double pointX = xregion.getROI().getPointX();
					double pointY = yregion.getROI().getPointY();
					double widthX = ((RectangularROI)(xregion.getROI())).getLengths()[0];
					double widthY = ((RectangularROI)(yregion.getROI())).getLengths()[1];
					IDataset sliceXSum = generateXAxisDataset((int)pointX, widthX);
					IDataset sliceYSum = generateYAxisDataset((int)pointY, widthY);
					updateProfilePlot(xAxisProfilePlot, sliceXSum);
					updateProfilePlot(yAxisProfilePlot, sliceYSum);
				}
			}
		};
		
		regionListener = new IRegionListener.Stub() {

			@Override
			public void regionCreated(RegionEvent evt) {
				if (evt.getRegion().getName().equals(X_REGION_NAME)) {
					xregion = evt.getRegion();
					evt.getRegion().addROIListener(xroiListener);
				} else if(evt.getRegion().getName().equals(Y_REGION_NAME)) {
					yregion = evt.getRegion();
					evt.getRegion().addROIListener(yroiListener);
				}
				if (evt.getRegion().getName().equals(X_REGION_NAME)) {
					sourcePlottingSystem.addTraceListener(traceListener);
				}
				
			}
						
			@Override
			public void regionRemoved(RegionEvent evt) {
				if (evt.getRegion().getName().equals(X_REGION_NAME)) {
					evt.getRegion().removeROIListener(xroiListener);
					sourcePlottingSystem.removeTraceListener(traceListener);
				} else if(evt.getRegion().getName().equals(Y_REGION_NAME)) {
					evt.getRegion().removeROIListener(yroiListener);
				}
			}
		};
		
		sourcePlottingSystem.addRegionListener(regionListener);
	}
	
	private IDataset generateXAxisDataset(int point, double width) {
		Slice currentSlice = new Slice(point, point + (int)width);
		return DatasetUtils.convertToDataset(((Dataset)sourceTrace.getData().getSliceView((Slice) null, currentSlice)).sum(1, true)).squeeze();
	}
	
	private IDataset generateYAxisDataset(int point, double width) {
		Slice currentSlice = new Slice(point, point + (int)width);
		return DatasetUtils.convertToDataset(((Dataset)sourceTrace.getData().getSliceView(currentSlice, (Slice) null)).sum(0, true)).squeeze();
	}
	
	private void updateProfilePlot(IPlottingSystem<Composite> plot, IDataset dataset) {
		plot.clear();
		ILineTrace trace = plot.createLineTrace("trace");
		trace.setData(null, dataset);
		plot.addTrace(trace);
		plot.repaint();
	}
	
	@PreDestroy
	void dispose() {
		if (regionListener != null) {
			sourcePlottingSystem.removeRegionListener(regionListener);
		}
		if (traceListener != null) {
			sourcePlottingSystem.removeTraceListener(traceListener);
		}
		if (xroiListener != null) {
			xregion.removeROIListener(xroiListener);
		}
		if (yroiListener != null) {
			yregion.removeROIListener(yroiListener);
		}	
	}

}
