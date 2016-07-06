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
package uk.ac.diamond.scisoft.analysis.rcp.views.plot;

import gda.configuration.properties.LocalProperties;

import java.awt.Color;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.dawb.common.util.list.PrimitiveArrayEncoder;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DStyles;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DUIAdapter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;

import com.swtdesigner.SWTResourceManager;

/**
 * A view that clones an AbstractPlotView and shows the data in it. The view saves state so that when the client is
 * restarted, the user can still see the plot.
 */
@Deprecated
public class StaticScanPlotView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(StaticScanPlotView.class);

	public static final String ID = "gda.rcp.views.scan.StaticScanPlotView";

	protected DataSetPlotter plotter;
	protected boolean plotted;
	protected PlotBean plotBean;

	@Override
	public final void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.plotter = new DataSetPlotter(PlottingMode.ONED, parent, false);

		final IPlotUI plotUI = createPlotActions(parent);
		plotter.registerUI(plotUI);

		createPlotFromBean();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		setSite(site);

		if (memento != null) {
			try {
				this.plotBean = getBeanFromXML(memento.getTextData());
				StaticScanPlotView.addSecondId(plotBean.getSecondId());
			} catch (Exception e) {
				logger.error("Cannot read plot bean", e);
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		try {
			memento.putTextData(getStringFromBean(plotBean));
		} catch (Exception e) {
			logger.error("Cannot save plot bean", e);
		}
	}

	private PlotBean getBeanFromXML(String textData) throws UnsupportedEncodingException {
		final ByteArrayInputStream stream = new ByteArrayInputStream(textData.getBytes("UTF-8"));
		XMLDecoder d = new XMLDecoder(new BufferedInputStream(stream));
		final PlotBean bean = (PlotBean) d.readObject();
		d.close();
		return bean;
	}

	private String getStringFromBean(final PlotBean plotBean) throws Exception {

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XMLEncoder e = new XMLEncoder(new BufferedOutputStream(stream));
		e.writeObject(plotBean);
		e.close();
		return stream.toString("UTF-8");
	}

	/**
	 * Call to copy data / configuration out of this plotter and display it.
	 * 
	 * @param toCopy
	 */
	public void setPlotter(final PlotView toCopy) {
		if (plotted) {
			logger.error("Cannot open " + StaticScanPlotView.class.getName() + " more than once.");
			return;
		}

		this.plotBean = toCopy.getPlotBean();
		plotBean.setPartName(toCopy.getPartName() + " (Snapshot)");
		createPlotFromBean();
	}

	/**
	 * Call to copy data / configuration out of this plotter and display it.
	 * 
	 * @param bean
	 */
	public void setPlotter(final PlotBean bean) {
		if (plotted) {
			logger.error("Cannot open " + StaticScanPlotView.class.getName() + " more than once.");
			return;
		}

		this.plotBean = bean;
		createPlotFromBean();
	}

	/**
	 * Takes bean and puts info into plot.
	 */
	protected void createPlotFromBean() {

		if (plotBean == null)
			return;
		plotter.setAxisModes(AxisMode.asEnum(plotBean.getXAxisMode()), AxisMode.asEnum(plotBean.getYAxisMode()),
				AxisMode.LINEAR);
		plotter.setXAxisLabel(plotBean.getXAxis());
		plotter.setYAxisLabel(plotBean.getYAxis());
		if (plotBean.getXAxisValues()!=null) {
			final double [] da = PrimitiveArrayEncoder.getDoubleArray(plotBean.getXAxisValues());
			plotter.setXAxisValues(new AxisValues(da), 1);
		}

		createPlotAndLegend(plotBean);
		setPartName(plotBean.getPartName());
		plotter.refresh(false);
		plotted = true;
	}

	protected void setSecondId(String secondId) {
		if (plotBean == null)
			return;
		plotBean.setSecondId(secondId);
	}

	/**
	 * Adds current data to another plotter, also create legends.
	 * 
	 * @param p
	 */
	protected void createPlotAndLegend(final PlotBean p) {

		final Map<String, ? extends Dataset> data = p.getDataSets();
		if (data.size() > 1) {
			try {
				plotter.replaceAllPlots(data.values());
			} catch (PlotException e) {
			}
		} else {
			final Plot1DAppearance app = new Plot1DAppearance(Color.BLACK, Plot1DStyles.SOLID, 1,
					p.getCurrentPlotName());
			plotter.getColourTable().addEntryOnLegend(app);
			try {
				plotter.replaceCurrentPlot(data.values().iterator().next());
			} catch (PlotException ex) {
			}
		}
	}

	/**
	 * Returns basic actions with an open and a save.
	 * 
	 * @param parent
	 * @return f
	 */
	protected IPlotUI createPlotActions(Composite parent) {
		return new Plot1DUIAdapter(getViewSite().getActionBars(), plotter, parent, getPartName()) {
			@Override
			public void buildToolActions(IToolBarManager manager) {
				manager.add(getOpenPlotAction(StaticScanPlotView.this));
				manager.add(getSavePlotAction(StaticScanPlotView.this));
				super.buildToolActions(manager);
			}
		};
	}

	@Override
	public void setFocus() {
		plotter.requestFocus();
	}

	/**
	 * Action designed to take an AbstractPlotView and open it in a static plot.
	 * 
	 * @param abs
	 * @return action to open static
	 */
	public static IAction getOpenStaticPlotAction(final PlotView abs) {
		final Action action = new Action() {
			@Override
			public void run() {
				try {
					final IWorkbenchPage page = abs.getSite().getPage();
					final String secondId = getUniqueSecondId();
					StaticScanPlotView view = (StaticScanPlotView) page.showView(StaticScanPlotView.ID, secondId,
							IWorkbenchPage.VIEW_ACTIVATE);
					view.setPlotter(abs);
					view.setSecondId(secondId);

				} catch (PartInitException e) {
					logger.error("Cannot find view " + StaticScanPlotView.ID, e);
				}

			}

		};
		action.setToolTipText("Save plot to separate window. Used for comparing and saving plots.");
		final Image icon = SWTResourceManager.getImage(StaticScanPlotView.class, "/icons/chart_curve_add.png");
		final ImageDescriptor d = ImageDescriptor.createFromImage(icon);
		action.setImageDescriptor(d);

		return action;
	}

	/**
	 * @param sv
	 * @return f
	 */
	public static IAction getSavePlotAction(final StaticScanPlotView sv) {
		return new Action("Save plot", AbstractUIPlugin.imageDescriptorFromPlugin(sv.getSite().getPluginId(),
				"icons/disk.png")) {
			FileDialog dialog;

			@Override
			public void run() {

				if (dialog == null) {
					dialog = new FileDialog(sv.getSite().getShell(), SWT.SAVE);
					dialog.setText("Save plot");
					dialog.setFilterExtensions(new String[] { "*.xml" });
					dialog.setFilterPath(System.getProperty(LocalProperties.GDA_DATA));
				}

				String path = dialog.open();
				if (path == null)
					return;
				if (!path.toLowerCase().endsWith(".xml"))
					path = path + ".xml";

				final File toSave = new File(path);
				if (toSave.exists()) {
					final boolean ok = MessageDialog.openConfirm(sv.getSite().getShell(), "Confirm Overwrite File",
							"The file '" + toSave.getName() + "' already exists.\n\nWould you like to overwrite?");
					if (!ok)
						return;
				}
				try {
					setPlotBeanToFile(toSave, sv.plotBean);
				} catch (Exception ne) {
					logger.error("Cannot save graph", ne);
				}
			}
		};
	}

	/**
	 * @param sv
	 * @return f
	 */
	public static IAction getOpenPlotAction(final StaticScanPlotView sv) {
		return new Action("Open saved plot", AbstractUIPlugin.imageDescriptorFromPlugin(sv.getSite().getPluginId(),
				"icons/folder_add.png")) {
			FileDialog dialog;

			@Override
			public void run() {

				if (dialog == null) {
					dialog = new FileDialog(sv.getSite().getShell(), SWT.OPEN);
					dialog.setText("Open a saved plot");
					dialog.setFilterExtensions(new String[] { "*.xml" });
					dialog.setFilterPath(System.getProperty(LocalProperties.GDA_DATA));
				}

				String path = dialog.open();
				if (path == null)
					return;

				final File toOpen = new File(path);
				if (!toOpen.exists())
					return;

				try {
					final IWorkbenchPage page = sv.getSite().getPage();
					final String secondId = getUniqueSecondId();
					StaticScanPlotView view = (StaticScanPlotView) page.showView(StaticScanPlotView.ID, secondId,
							IWorkbenchPage.VIEW_ACTIVATE);
					view.setPlotter(getPlotBeanFromFile(toOpen));
					view.setSecondId(secondId);

				} catch (Exception ne) {
					logger.error("Cannot open graph", ne);
				}
			}
		};
	}

	private static PlotBean getPlotBeanFromFile(final File toOpen) throws FileNotFoundException {

		XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(toOpen)));
		final PlotBean bean = (PlotBean) d.readObject();
		d.close();
		return bean;
	}

	private static void setPlotBeanToFile(final File toSave, final PlotBean plotBean) throws FileNotFoundException {

		XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(toSave)));
		e.writeObject(plotBean);
		e.close();
	}

	private static String TEMPLATE = "uk.ac.diamond.scisoft.analysis.rcp.views.staticplot";
	private static Collection<String> cachedNames = new HashSet<String>(31);

	private static String getUniqueSecondId() {
		int num = 1;
		while (cachedNames.contains(TEMPLATE + num)) {
			++num;
		}
		cachedNames.add(TEMPLATE + num);
		return TEMPLATE + num;
	}

	private static void addSecondId(final String id) {
		cachedNames.add(id);
	}

}
