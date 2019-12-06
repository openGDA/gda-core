package uk.ac.diamond.daq.client.gui.camera.liveview;

import java.util.Collection;
import java.util.UUID;

import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.ui.HistogramViewer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.event.PlottingSystemUpdateEvent;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Listens to {@link PlottingSystemUpdateEvent}s
 * 
 * @author Mattew Webber
 * @author Maurizio Nagni
 */
public class HistogramComposite extends Composite {

	private HistogramViewer histogram;	

	private static final Logger logger = LoggerFactory.getLogger(HistogramComposite.class);
	
	private IPlottingSystem plottingSystem;
	private Composite histogramArea;
	
	public HistogramComposite(Composite parent, @SuppressWarnings("rawtypes") IPlottingSystem plottingSystem, int style) {
		super(parent, style);
		this.plottingSystem = plottingSystem;
		
		try {
			SpringApplicationContextProxy.addApplicationListener(plottingSystemUpdateListener);
		} catch (GDAClientException e) {
			logger.error("Error", e);
		}
		
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		histogramArea = ClientSWTElements.createComposite(this, SWT.None);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(histogramArea);
		
		
		Composite statisticsPanel = ClientSWTElements.createComposite(this, SWT.None, 8, SWT.LEFT, SWT.BOTTOM);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statisticsPanel);

		ClientSWTElements.createLabel(statisticsPanel, SWT.LEFT, ClientMessages.MAX);
		ClientSWTElements.createLabel(statisticsPanel, SWT.LEFT, ClientMessages.NOT_AVAILABLE);
		
		ClientSWTElements.createLabel(statisticsPanel, SWT.LEFT, ClientMessages.MIN);
		ClientSWTElements.createLabel(statisticsPanel, SWT.LEFT, ClientMessages.NOT_AVAILABLE);
		
		ClientSWTElements.createLabel(statisticsPanel, SWT.LEFT, ClientMessages.MEAN);
		ClientSWTElements.createLabel(statisticsPanel, SWT.LEFT, ClientMessages.NOT_AVAILABLE);
		
		ClientSWTElements.createLabel(statisticsPanel, SWT.LEFT, ClientMessages.STD_DEV);
		ClientSWTElements.createLabel(statisticsPanel, SWT.LEFT, ClientMessages.NOT_AVAILABLE);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
	}

	private void refreshData() {
		try {
			if (histogram == null) {
				histogram = new HistogramViewer(histogramArea);	
				histogram.setContentProvider(new ImageHistogramProvider());
			} 			
			Collection<ITrace> traces = plottingSystem.getTraces(IPaletteTrace.class);			
			if (!traces.isEmpty()) {
				IImageTrace trace = (IImageTrace)traces.iterator().next();
				trace.getImageServiceBean().setMax(260.0);
				histogram.setInput(trace);
				histogram.refresh();
				layout(true, true);
			}
			GridDataFactory.fillDefaults().grab(true, true).applyTo(histogram.getComposite());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	ApplicationListener<PlottingSystemUpdateEvent> plottingSystemUpdateListener = new ApplicationListener<PlottingSystemUpdateEvent>() {
		@Override
		public void onApplicationEvent(PlottingSystemUpdateEvent event) {
			UUID uuid = ClientSWTElements.findParentUUID(getParent());
			if (!event.getRootComposite().equals(uuid)) {
				return;
			}
			if (LivePlottingComposite.class.isAssignableFrom(event.getSource().getClass())) {
				refreshData();				
			}
		}
	};
	
}
