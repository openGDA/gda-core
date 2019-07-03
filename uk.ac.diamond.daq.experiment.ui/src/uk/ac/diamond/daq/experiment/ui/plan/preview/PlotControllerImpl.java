package uk.ac.diamond.daq.experiment.ui.plan.preview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation.LineStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.experiment.api.ExperimentException;

public class PlotControllerImpl implements PlotController {
	
	private static final String PLOT_TITLE = "Predicted plan outcome"; 
	
	private IPlottingSystem<Composite> plot;
	private List<IAnnotation> annotations;
	
	public PlotControllerImpl(Composite parent) {
		
		Composite plotComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(plotComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(plotComposite);
		
		try {
			plot = PlottingFactory.createPlottingSystem();
			plot.createPlotPart(plotComposite, "Preview", null, PlotType.XY, null);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(plot.getPlotComposite());
			
			annotations = new ArrayList<>();
		} catch (Exception e) {
			new Label(plotComposite, SWT.NONE).setText("Preview cannot be displayed");
		}
	}

	@Override
	public void clear() {
		plot.clear();
		annotations.forEach(annotation -> Display.getDefault().syncExec(()->plot.removeAnnotation(annotation)));
		annotations.clear();
	}

	@Override
	public void createPlot(Dataset x, Dataset y) {
		plot.createPlot1D(x, Arrays.asList(y), null);
		plot.setTitle(PLOT_TITLE);
	}

	@Override
	public void createPlot(Dataset x) {
		Dataset pseudoSecondDimension = DatasetFactory.createFromObject(new double[] {0., 0.,});
		pseudoSecondDimension.setName("Time");
		plot.createPlot1D(x, Arrays.asList(pseudoSecondDimension), null);
		plot.getSelectedYAxis().setVisible(false);
		plot.setTitle(PLOT_TITLE);
	}

	@Override
	public void plotTriggerPoints(String triggerName, Object x, Object y) {
		ILineTrace trace = plot.createLineTrace(triggerName);
		trace.setData(DatasetFactory.createFromObject(x), DatasetFactory.createFromObject(y));
		trace.setLineWidth(-10);
		trace.setPointSize(8);
		trace.setPointStyle(PointStyle.FILLED_CIRCLE);
		plot.repaint();
		Display.getDefault().syncExec(() -> plot.addTrace(trace));
		plot.setTitle(PLOT_TITLE);
	}

	@Override
	public void markSegmentEnd(String segmentName, double x) {
		try {
			IAnnotation segmentAnnotation = plot.createAnnotation("Segment end: " + segmentName);
			segmentAnnotation.setLocation(x, 0);
			segmentAnnotation.setShowName(true);
			segmentAnnotation.setShowPosition(false);
			segmentAnnotation.setLineStyle(LineStyle.UP_DOWN);
			annotations.add(segmentAnnotation);
			Display.getDefault().syncExec(()->plot.addAnnotation(segmentAnnotation));
		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}
	
	@Override
	public void flag(String elementName, String message, double x) {
		flag(elementName, message, x, 0.0);
	}

	@Override
	public void flag(String elementName, String message, double x, double y) {
		try {
			
			IAnnotation flag = plot.createAnnotation(elementName + ": \n" + message);
			flag.setLocation(x, y);
			flag.setShowInfo(true);
			flag.setShowPosition(true);
			flag.setLineStyle(LineStyle.NONE);
			annotations.add(flag);
			Display.getDefault().syncExec(()->plot.addAnnotation(flag));
			
		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}

}
