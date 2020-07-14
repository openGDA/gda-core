package uk.ac.diamond.daq.client.gui.camera.liveview;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;

import java.util.Collection;

import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.ui.HistogramViewer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.live.stream.event.PlottingSystemUpdateEvent;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Listens to {@link PlottingSystemUpdateEvent}s
 * 
 * @author Mattew Webber
 * @author Maurizio Nagni
 */
public class HistogramComposite implements CompositeFactory {

	private HistogramViewer histogram;

	private static final Logger logger = LoggerFactory.getLogger(HistogramComposite.class);

	private IPlottingSystem<Composite> plottingSystem;
	private Composite histogramArea;
	private Composite container;

	public HistogramComposite(IPlottingSystem<Composite> plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		SpringApplicationContextProxy.addDisposableApplicationListener(this, plottingSystemUpdateListener);

		container = createClientCompositeWithGridLayout(parent, SWT.None, 8);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);
		
		histogramArea = createClientCompositeWithGridLayout(container, SWT.None, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).span(8, 1).grab(true, true).applyTo(histogramArea);

		Label label = createClientLabel(container, SWT.LEFT, ClientMessages.MAX);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(label);

		label = createClientLabel(container, SWT.LEFT, ClientMessages.NOT_AVAILABLE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(label);

		label = createClientLabel(container, SWT.LEFT, ClientMessages.MIN);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(label);

		label = createClientLabel(container, SWT.LEFT, ClientMessages.NOT_AVAILABLE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(label);

		label = createClientLabel(container, SWT.LEFT, ClientMessages.MEAN);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(label);

		label = createClientLabel(container, SWT.LEFT, ClientMessages.NOT_AVAILABLE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(label);

		label = createClientLabel(container, SWT.LEFT, ClientMessages.STD_DEV);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(label);

		label = createClientLabel(container, SWT.LEFT, ClientMessages.NOT_AVAILABLE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, false).applyTo(label);

		return container;
	}

	ApplicationListener<PlottingSystemUpdateEvent> plottingSystemUpdateListener = new ApplicationListener<PlottingSystemUpdateEvent>() {
		@Override
		public void onApplicationEvent(PlottingSystemUpdateEvent event) {
			if (!event.haveSameParent(histogramArea)) {
				return;
			}
			if (LivePlottingComposite.class.isAssignableFrom(event.getSource().getClass())) {
				refreshData();
			}
		}

		private void refreshData() {
			try {
				if (histogram == null) {
					histogram = new HistogramViewer(histogramArea);
					histogram.setContentProvider(new ImageHistogramProvider());
				}
				Collection<ITrace> traces = plottingSystem.getTraces(IPaletteTrace.class);
				if (!traces.isEmpty()) {
					IImageTrace trace = (IImageTrace) traces.iterator().next();
					trace.getImageServiceBean().setMax(260.0);
					histogram.setInput(trace);
				}
				createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(histogram.getComposite());
				container.layout(true, true);
				histogram.refresh();
			} catch (Exception e) {
				UIHelper.showError("Cannot create CameraConfiguration", e, logger);
			}
		}
	};
}