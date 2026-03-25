package uk.ac.diamond.daq.arpes.ui.e4.views;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.dawnsci.multidimensional.ui.imagecuts.PerpendicularCutsHelper;
import org.dawnsci.multidimensional.ui.imagecuts.PerpendicularImageCutsComposite;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

/**
 * Window for a plot view containing two additional plot views showing
 * perpendicular cuts of the image from the main view.
 * <p>
 * Also has the option to incrementally sum the incoming data.
 */
public class ArpesLivePerpCutsViewE4 extends BaseLivePlotViewE4{
	private static final Logger logger = LoggerFactory.getLogger(ArpesLivePerpCutsViewE4.class);

	private final ReadWriteLock sumLock = new ReentrantReadWriteLock();
	private volatile boolean isSum = false;

	private ImageWithAxes sum; // Guarded by sumLock

	private final IEclipseContext context;

	@Inject
	public ArpesLivePerpCutsViewE4(IEclipseContext context) {
		this.context = context;
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		try {
			createUIComponents(parent);
		} catch (Exception e) {
			logger.error("Failed to create composite", e);
			throw new RuntimeException("Failed to initialize view", e);
		}
	}

	private void createUIComponents(Composite parent) throws Exception {
		SashForm inner = new SashForm(parent, SWT.HORIZONTAL);
		inner.setLayout(new GridLayout(2, true));
		Composite leftComposite = new Composite(inner, SWT.NONE);
		leftComposite.setLayout(new GridLayout());
		// Create button panel
		Composite buttonPanel = createButtonPanel(leftComposite);
		// Create perpendicular cuts composite
		PerpendicularImageCutsComposite cutsComposite = createCutsComposite(inner);
		// Create plotting system
		createPlottingSystem(leftComposite, cutsComposite);
	}

	private Composite createButtonPanel(Composite parent) {
		Composite buttonPanel = new Composite(parent, SWT.NONE);
		buttonPanel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		buttonPanel.setLayout(new GridLayout(3, false));
		Button liveButton = new Button(buttonPanel, SWT.RADIO);
		liveButton.setText("Live");
		liveButton.setSelection(!isSum);
		liveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isSum = !liveButton.getSelection();
				clearSum();
			}
		});
		Button sumButton = new Button(buttonPanel, SWT.RADIO);
		sumButton.setText("Sum");
		sumButton.setSelection(isSum);
		Button clearButton = new Button(buttonPanel, SWT.PUSH);
		clearButton.setText("Clear");
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearSum();
			}
		});
		return buttonPanel;
	}

	private PerpendicularImageCutsComposite createCutsComposite(Composite parent) throws Exception {
		IPlottingService plottingService = context.get(IPlottingService.class);
		if (plottingService == null) {
			throw new IllegalStateException("IPlottingService not available in context");
		}
		PerpendicularImageCutsComposite composite = new PerpendicularImageCutsComposite(
			parent, SWT.NONE, plottingService);
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());
		return composite;
	}

	private void createPlottingSystem(Composite parent, PerpendicularImageCutsComposite cutsComposite) throws Exception {
		IPlottingService plottingService = context.get(IPlottingService.class);
		plottingSystem = plottingService.createPlottingSystem();
		plottingSystem.setColorOption(ColorOption.NONE);
		plottingSystem.createPlotPart(parent, "Detector", null, PlotType.XY, null);
		plottingSystem.setKeepAspect(false);
		plottingSystem.repaint();
		plottingSystem.getPlotComposite().setLayoutData(
			GridDataFactory.fillDefaults().grab(true, true).create());
		PerpendicularCutsHelper helper = new PerpendicularCutsHelper(plottingSystem);
		helper.activate(cutsComposite);
	}

	private void clearSum() {
		sumLock.writeLock().lock();
		try {
			sum = null;
		} finally {
			sumLock.writeLock().unlock();
		}
	}

	private Dataset updateSum(IDataset data, IDataset xAxisValues, IDataset yAxisValues) {
		if (data == null) {
			return null;
		}
		sumLock.writeLock().lock();
		try {
			if (sum == null) {
				sum = new ImageWithAxes(data, xAxisValues, yAxisValues);
				return (Dataset) data;
			}
			if (sum.hasSameAxes(xAxisValues, yAxisValues)) {
				return sum.addToSum(data);
			} else {
				// Axes changed, start new sum
				sum = new ImageWithAxes(data, xAxisValues, yAxisValues);
				return (Dataset) data;
			}
		} finally {
			sumLock.writeLock().unlock();
		}
	}

	@Override
	protected void updatePlot(LiveDataPlotUpdate dataUpdate) {
		try {
			final IDataset xAxisValues = dataUpdate.getxAxis();
			final IDataset yAxisValues = dataUpdate.getyAxis();
			final IDataset plotData;
			if (isSum) {
				plotData = updateSum(dataUpdate.getData(), xAxisValues, yAxisValues);
			} else {
				plotData = dataUpdate.getData();
			}
			if (plotData != null && plottingSystem != null) {
				plottingSystem.updatePlot2D(plotData, Arrays.asList(xAxisValues, yAxisValues), null);
				plottingSystem.setKeepAspect(false);
				plottingSystem.repaint();
			}
		} catch (Exception e) {
			logger.error("Error handling plot update", e);
		}
	}

	/**
	 * Inner class to hold image data with its axes.
	 * Thread-safe when used with external synchronization.
	 */
	private static class ImageWithAxes {
		private final Dataset image;
		private final IDataset xAxis;
		private final IDataset yAxis;
		public ImageWithAxes(IDataset data, IDataset xAxisValues, IDataset yAxisValues) {
			this.image = DatasetUtils.cast(DoubleDataset.class, data);
			this.xAxis = xAxisValues;
			this.yAxis = yAxisValues;
		}
		public boolean hasSameAxes(IDataset xAxisValues, IDataset yAxisValues) {
			return Objects.equals(this.xAxis, xAxisValues) && Objects.equals(this.yAxis, yAxisValues);
		}
		public Dataset addToSum(IDataset data) {
			return this.image.iadd(data);
		}
	}

	@Focus
	public void setFocus() {
		if (plottingSystem != null) {
			plottingSystem.setFocus();
		}
	}

	@PreDestroy
	public void dispose() {
		// Clear sum data
		clearSum();
	}
}
