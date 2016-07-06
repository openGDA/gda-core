/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.jreality.overlay.Overlay1DConsumer;
import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DUIAdapter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.AbstractPlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotBean;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.StaticScanPlotView;

import com.swtdesigner.SWTResourceManager;

/**
 * Simple class which simply plots the derivatives of anything plotted in the plotter.
 * 
 * This class so far tested with 1D editors rather than plot views.
 * 
 * First derivative in 1D is needed by some science.
 * 
 * Replaced by org.dawnsci.plotting.tool.DerivativeTool
 * 
 * TODO - change to arbitrary function plotter?
 *      - allow regions? derivative of region rather than whole function.
 *      
 */
@Deprecated
public class DerivativeViewer extends SidePlot implements Overlay1DConsumer, PlotView {

    private static final Logger logger = LoggerFactory.getLogger(DerivativeViewer.class);	
	
	private DataSetPlotter    plotter;
	private IPlotUI           plotUI;

	private Composite parent;
	private Action first;
	private Action second;
	private Action third;
	private IAction showLeg;
	private Action sendToPlotView;
	private Action saveGraph;
	private Action printGraph;
	private Action copyGraph;
	private String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	private String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	private String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	private String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	private String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	private String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	private String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	private String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	private String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");

	public DerivativeViewer() {
		super();
	}

	@Override
	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
		this.plotUI = plotUI;
		Action action = super.createSwitchAction(index, plotUI);
		action.setText("Derivative Viewer");
		action.setToolTipText("Show the derivative of the data set(s) plotted in a side plot.");
		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/SidePlot-Derivative.png"));

		return action;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		this.parent.setLayout(new FillLayout());
		
		final Composite main = new Composite(parent, SWT.NONE);
		GridLayout gl_main = new GridLayout(1, false);
		gl_main.marginWidth = 0;
		gl_main.marginHeight = 0;
		gl_main.horizontalSpacing = 0;
		main.setLayout(gl_main);
		this.container = main;
		
		Composite peakPlotter = new Composite(main, SWT.NONE);
		peakPlotter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true)); 
		peakPlotter.setLayout(new FillLayout());
		
		plotter = new DataSetPlotter(PlottingMode.ONED, peakPlotter);
		plotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
		
	}
	
	
	// Not too happy about keeping state in fields, perhaps move to bean.
    private int     derivative = 1;
	
	@Override
	public void generateToolActions(IToolBarManager manager) {
				
 		createDerivativeActions();
 		createExportActions();
 		createExtraActions();
 		manager.add(first);
		manager.add(second);
		manager.add(third);
		manager.add(new Separator(getClass().getName()+printButtonText));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		manager.add(new Separator(getClass().getName()+"extraActions"));
		manager.add(sendToPlotView);
		manager.add(showLeg);
 		
		//manager.add(StaticScanPlotView.getOpenStaticPlotAction(this));

	}

	@Override
	public void dispose() {
		super.dispose();
		if (plotter != null)
			plotter.cleanUp();
		plotter = null;
	}

	public Dataset getXValues() {
		
		final List<AxisValues> xValues = mainPlotter.getXAxisValues();
		
		return xValues.get(0).toDataset();

	}
	
	public Collection<Dataset> getYValues() {
		
		final List<AxisValues> xValues = mainPlotter.getXAxisValues();
		
		/**
		 * These are the y data sets.
		 */
		final List<IDataset> ys = mainPlotter.getCurrentDataSets();

		final Collection<Dataset> dervs = new ArrayList<Dataset>(ys.size());
		for (int i = 0; i < ys.size(); i++) {
			final Dataset x    = xValues.get(i).toDataset();
			final Dataset y    = DatasetUtils.convertToDataset(ys.get(i));
			Dataset derv = y;
			for (int di = 0; di < derivative; di++) { 
				derv = Maths.derivative(x, derv, derivative);
			}
			derv.setName("f"+getTicksFor(derivative)+" {" +y.getName()+" }");
			dervs.add(derv);
		}
		
		return dervs;
	}
	
	@Override
	public void processPlotUpdate() {
		
		if (mainPlotter==null) {
		    clearPeakPlotter();
		    return;
		}
		
		final List<AxisValues> xValues = mainPlotter.getXAxisValues();
		
		/**
		 * These are the y data sets.
		 */
		final List<IDataset> ys = mainPlotter.getCurrentDataSets();
		if (ys==null||ys.isEmpty()) {
		    clearPeakPlotter();
		    return;
		}
		
		final Collection<Dataset> dervs = new ArrayList<Dataset>(ys.size());
		for (int i = 0; i < ys.size(); i++) {
			final Dataset x    = xValues.get(i).toDataset();
			final Dataset y    = DatasetUtils.convertToDataset(ys.get(i));
			Dataset derv = y;
			for (int di = 0; di < derivative; di++) { 
				derv = Maths.derivative(x, derv, derivative);
			}
			derv.setName("f"+getTicksFor(derivative)+" {" +y.getName()+" }");
			dervs.add(derv);
		}
		
		try {
			AbstractPlotView.createMultipleLegend(plotter, dervs);
			plotter.replaceAllPlots(dervs, xValues);
		} catch (PlotException e) {
			logger.error("Cannot update plot with "+dervs);
		}
		plotter.refresh(false);
	}

	private String getTicksFor(int size) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < size; i++) buf.append("'");
        return buf.toString();
	}

	@Override
	public void showSidePlot() {
		processPlotUpdate();
	}
	
	@Override
	public void registerProvider(OverlayProvider provider) {
	}

	@Override
	public void removePrimitives() {
//		int primNumMax = primitiveIDs.size();
//		if (primNumMax > 0)
//			oProvider.unregisterPrimitive(primitiveIDs);
	}
	
	private void clearPeakPlotter() {
		if (plotter != null) {
			try {
				plotter.replaceAllPlots(new ArrayList<IDataset>());
				removePrimitives();
			} catch (PlotException e) {
				logger.warn("The plot could not be cleared in "+getClass().getName());
			}
			plotter.refresh(false);
		}
	}

	@Override
	public void unregisterProvider() {
	}

	@Override
	public void areaSelected(AreaSelectEvent event) {

		
	}


	///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Not currently needed methods:
	 */
	@Override
	public void addToHistory() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void generateMenuActions(IMenuManager manager, IWorkbenchPartSite site) {
		createExportActions();
 		createExtraActions();
		manager.add(new Separator(getClass().getName()+printButtonText));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		manager.add(new Separator(getClass().getName()+"extraActions"));
		manager.add(sendToPlotView);
	}
	@Override
	public void removeFromHistory() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int updateGUI(GuiBean bean) {
		// TODO Auto-generated method stub
		return 0;
	}
	/**
	 * End not currently needed
	 */
	///////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public String getPartName() {
		return getSite().getPart().getTitle();
	}

	@Override
	public PlotBean getPlotBean() {
		final PlotBean bean = new PlotBean();
		// TODO add data
		return bean;
	}

	@Override
	public IWorkbenchPartSite getSite() {
		return ((ViewPart)plotUI.getSidePlotView()).getSite();
	}
	
	

	private void createDerivativeActions() {
		
		final CheckableActionGroup group = new CheckableActionGroup();
		first = new Action("First Derivative", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				updatePlotInternal(1);
			}
		};
		group.add(first);
		first.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/First-Derivative.png"));
		first.setChecked(true);
		
		second = new Action("Second Derivative", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				updatePlotInternal(2);
			}
		};
		second.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/Second-Derivative.png"));
		group.add(second);
		
		third = new Action("Third Derivative", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				updatePlotInternal(3);
			}
		};
		third.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/Third-Derivative.png"));
		group.add(third);
	}
	
	private void createExportActions(){
		saveGraph = new Action() {
			
			// Cache file name otherwise they have to keep
			// choosing the folder.
			private String filename;
			
			@Override
			public void run() {
				
				FileDialog dialog = new FileDialog (parent.getShell(), SWT.SAVE);
				
				String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps","*.svg;*.SVG"};
				if (filename!=null) {
					dialog.setFilterPath((new File(filename)).getParent());
				} else {
					String filterPath = "/";
					String platform = SWT.getPlatform();
					if (platform.equals("win32") || platform.equals("wpf")) {
						filterPath = "c:\\";
					}
					dialog.setFilterPath (filterPath);
				}
				dialog.setFilterNames (PlotExportUtil.FILE_TYPES);
				dialog.setFilterExtensions (filterExtensions);
				filename = dialog.open();
				if (filename == null)
					return;

				plotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
			}
		};
		saveGraph.setText(saveButtonText);
		saveGraph.setToolTipText(saveToolTipText);
		saveGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(saveImagePath));
		
		copyGraph = new Action() {
			@Override
			public void run() {
				plotter.copyGraph();
			}
		};
		copyGraph.setText(copyButtonText);
		copyGraph.setToolTipText(copyToolTipText);
		copyGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(copyImagePath));
		
		printGraph = new Action() {
			@Override
			public void run() {
				plotter.printGraph();
			}
		};
		
		printGraph.setText(printButtonText);
		printGraph.setToolTipText(printToolTipText);
		printGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(printImagePath));
	}

	private void createExtraActions() {
		showLeg = Plot1DUIAdapter.createShowLegend(plotter);
		showLeg.setChecked(true);
		
		final DerivativeViewer viewer = this;
 		
 		sendToPlotView = new Action() {
			@Override
			public void run() {
				try {
					EclipseUtils.getActivePage().showView("uk.ac.diamond.scisoft.analysis.rcp.plotView2");
					SDAPlotter.plot("Plot 2", viewer.getXValues(), viewer.getYValues().toArray(new Dataset[0]));
				} catch (Exception e) {
					logger.error("Error plotting to DerivitiveViewer", e);
				}
			}

		};
		sendToPlotView.setToolTipText("Send the results of the derivative to a separate plot view");
		final org.eclipse.swt.graphics.Image icon = SWTResourceManager.getImage(StaticScanPlotView.class,"/icons/chart_curve_add.png");
		final ImageDescriptor d = ImageDescriptor.createFromImage(icon);
		sendToPlotView.setImageDescriptor(d);
		sendToPlotView.setText("Send derivative to Plot 2");
	}

	protected void updatePlotInternal(final int derivative) {
		this.derivative    = derivative;
		processPlotUpdate();
	}
}
