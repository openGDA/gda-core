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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.roi.data.IRowData;
import org.dawb.common.ui.plot.roi.data.ROIData;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.downsample.DownsampleMode;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.function.Downsample;
import org.eclipse.dawnsci.analysis.dataset.impl.BooleanDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.MaskingBean;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.ROIHandler;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay2DConsumer;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay2DProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.dawnsci.plotting.api.region.IRegionService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.deprecated.rcp.Activator;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBeanException;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROIDataList;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROITableViewer;
import uk.ac.diamond.scisoft.analysis.rcp.queue.InteractiveQueue;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;

/**
 * Abstract class to extend for any side plots that uses overlays and stuff on 2D images
 * <p>
 * roiClass and roiListClass are only used in updateGUI method so if you override that method,
 * you may not need to initialize those fields.
 */
@Deprecated
public abstract class SidePlotProfile extends SidePlot implements Overlay2DConsumer, SelectionListener, ICellEditorListener {
	private static Logger logger = LoggerFactory.getLogger(SidePlotProfile.class);

	protected Dataset data;
	protected Dataset subData;
	protected double subFactor; // linear reduction factor of down-sampled image
	private static final int DMAXDIM = 500; // maximum length of a side in image for down-sampled image

	protected IROI roi = null;
	protected ROIData roiData = null;
	protected ROIDataList roiDataList = null;
	protected Class<? extends IROI> roiClass = null;
	protected Class<?> roiListClass = null;

	protected Overlay2DProvider oProvider;
	protected boolean dragging = false;
	protected Color oColour; // current overlay colour
	protected Color dColour = new Color(255, 0, 0); // default colour: red
	protected Color cColour; // complement brightness colour
	protected double oThickness = 2.0;
	protected double oTransparency = 0.6;
	protected int[] cpt = new int[2];

	protected List<Integer> dragIDs;
	protected List<Integer> roiIDs;
	protected List<Integer> roisIDs;
	protected ROIHandler roiHandler;

	private static final int HSIZEMIN = 5;
	private static final int HSIZEMAX = 100;
	private static final double HSIZEFRACTION = 0.05; // fraction of smallest dimension to use for size of handle

	protected ROITableViewer tViewer;

	protected boolean isBulkUpdate = false; // this is set true when updating many widgets' selection

	protected BooleanDataset mask;
	protected BooleanDataset subMask;

	protected InteractiveQueue roiQueue = null;

	@Override
	public void dispose() {
		if (roiQueue != null)
			roiQueue.dispose();
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		roiQueue = new InteractiveQueue(parent);
	}

	public int calcHandleSize(final int[] shape) {
		int tsize;

		if (shape.length < 2) {
			tsize = shape[0];
		} else {
			tsize = Math.min(shape[0], shape[1]);
		}
		int hsize = (int) (tsize*HSIZEFRACTION); // make fraction of smallest dimension
		if (hsize < HSIZEMIN)
			hsize = HSIZEMIN;

		if (hsize > tsize*0.25) { // now check if handle is too big for image
			hsize = tsize/4;
			if (hsize < 1)
				hsize = 1; // absolute minimum size
		}
		if (hsize > HSIZEMAX)
			hsize = HSIZEMAX;
		return hsize;
	}

	@Override
	public void registerProvider(OverlayProvider provider) {
		oProvider = (Overlay2DProvider) provider;
	}

	@Override
	public void unregisterProvider() {
		hideOverlays();
		removePrimitives();
		oProvider = null;
	}

	@Override
	public void hideOverlays() {
		if (oProvider == null)
			return;

		hideIDs(dragIDs);
		hideCurrent();
		hideIDs(roisIDs);
	}

	@Override
	public void showOverlays() {
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				drawOverlays();
				drawCurrentOverlay();
			}
		});
	}

	@Override
	public void showSidePlot() {

		mask = null;
		if (guiUpdateManager != null) {
			GuiBean beanBag = guiUpdateManager.getGUIInfo();
			if (beanBag.containsKey(GuiParameters.MASKING)) {
				MaskingBean maskingBean = (MaskingBean) beanBag.get(GuiParameters.MASKING);
				mask = maskingBean.getMask();
			}
		}
	
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				tViewer.setInput(roiDataList);
				updatePlot();
			}
		});
		
		int state = 0;
		if (guiUpdateManager != null)
			state = updateGUI(guiUpdateManager.getGUIInfo(), true);

		// send ROIs on switch if bean has new info or has missing info
		if ((state & ROI) != 0)
			sendCurrentROI(roi);
		if ((state & ROILIST) != 0)
			sendROIs(roiClass);

		if (state != 0)
			updateDataList();
	
		if (roi == null && roiDataList.size() == 0) {
 			return;
 		}
		showOverlays();
	}

	protected void hideCurrent() {
		hideIDs(roiIDs);

		for (int h = 0, hmax = roiHandler.size(); h < hmax; h++) {
			if (roiHandler.get(h) != -1) {
				oProvider.setPrimitiveVisible(roiHandler.get(h), false);
			}
		}
	}

	/**
	 * Remove primitives listed in IDs
	 * 
	 * @param ids
	 */
	protected void hideIDs(List<Integer> ids) {
		for (int r = 0, rmax = ids.size(); r < rmax; r++) {
			int id = ids.get(r);
			if (id != -1) {
				oProvider.setPrimitiveVisible(id, false);
			}
		}
	}

	@Override
	public void removePrimitives() {
		if (oProvider == null)
			return;

		removeIDs(dragIDs);
		removeIDs(roiIDs);

		removeHandles();
		removeIDs(roisIDs);
	}

	/**
	 * Remove handles primitives
	 */
	protected void removeHandles() {
		int hmax = roiHandler.size();
		if (hmax > 0) {
			oProvider.unregisterPrimitive(roiHandler.getAll());
			for (int h = 0; h < hmax; h++) {
				roiHandler.set(h, -1);
			}
		}
	}

	/**
	 * Remove primitives listed in IDs
	 * 
	 * @param ids
	 */
	protected void removeIDs(List<Integer> ids) {
		int rmax = ids.size();
		if (rmax > 0) {
			oProvider.unregisterPrimitive(ids);
			for (int r = 0; r < rmax; r++) {
				ids.set(r, -1);
			}
		}
	}

	protected void updatePlot() {
		updatePlot(roi);
	}

	/**
	 * Update plot(s) with given region of interest
	 * @param roi
	 */
	abstract protected void updatePlot(IROI roi);

	/**
	 * Draw current overlay for given region of interest
	 */
	abstract protected void drawCurrentOverlay();

	/**
	 * Draw overlays from table for given regions of interest
	 */
	abstract protected void drawOverlays();

	/**
	 * Update all spinners with regions of interest
	 * @param roi
	 */
	abstract protected void updateAllSpinners(final IROI roi);

	/**
	 * Update data list from ROI list
	 */
	protected void updateDataList() {
		if (getDataset()) {
			for (int i = 0, imax = roiDataList.size(); i < imax; i++) {
				IRowData rdata = roiDataList.get(i);
				if (rdata instanceof ROIData)
					roiDataList.set(i, createNewROIData(((ROIData) rdata).getROI()));
			}
		}
	}

	// table menu selection
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		IStructuredSelection selection = (IStructuredSelection) tViewer.getSelection();
		if (selection != null) {
			if (e.widget instanceof MenuItem) {
				ROIData cRData = (ROIData) selection.getFirstElement();
				int onum = roiDataList.indexOf(cRData); // index of selected overlay
				switch (tViewer.getContextMenu().indexOf((MenuItem) e.widget)) {
				case ROITableViewer.ROITABLEMENU_EDIT:
					roi = cRData.getROI();
					roiHandler.setROI(roi);
					removeIDs(roiIDs); // get rid of current overlay
					roiIDs.set(0, roisIDs.get(onum)); // transfer selected ROI
					roisIDs.remove(onum);
					roiDataList.remove(cRData);
					sendCurrentROI(roi);
					sendROIs(null);

					updateAllSpinners(roi);
					getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							tViewer.refresh();
							removeHandles();
							drawOverlays();
							drawCurrentOverlay();
							updatePlot();
						}
					});
					break;
				case ROITableViewer.ROITABLEMENU_COPY:
					roi = cRData.getROI().copy();
					setROIName(roi);
					roiHandler.setROI(roi);
					sendCurrentROI(roi);

					updateAllSpinners(roi);
					getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							drawOverlays();
							drawCurrentOverlay();
							updatePlot();
						}
					});
					break;
				case ROITableViewer.ROITABLEMENU_DELETE:
					oProvider.unregisterPrimitive(roisIDs.get(onum));
					roisIDs.remove(onum);
					roiDataList.remove(cRData);
					sendROIs(null);

					getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							tViewer.refresh();
							drawOverlays();
							updatePlot();
						}
					});
					break;
				case ROITableViewer.ROITABLEMENU_DELETE_ALL:
					removeIDs(roisIDs);
					roisIDs.clear();
					roiDataList.clear();
					sendROIs(null);

					getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							tViewer.refresh();
							drawOverlays();
							updatePlot();
						}
					});
					break;
				}
			}
		}
	}

	// Cell editor for plot selection in table
	@Override
	public void applyEditorValue() {
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				sendROIs(null);
				updatePlot();
			}
		});
	}

	@Override
	public void cancelEditor() {
	}

	@Override
	public void editorValueChanged(boolean oldValidState, boolean newValidState) {
	}

	// more GUI listeners
	protected SelectionListener copyButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			roi = null;
			roiData.setPlot(false);
			roiDataList.add(roiData);
			roisIDs.add(-1);
			removeCurrentROI();
			sendROIs(null);

			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					tViewer.refresh();
					hideCurrent();
					removeIDs(roiIDs);
					removeHandles();
					drawOverlays();
					updatePlot();
				}
			});
		}
	};

	protected SelectionListener deleteButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			roi = null;
			removeCurrentROI();

			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					tViewer.refresh();
					hideCurrent();
					removeHandles();
					updatePlot();
				}
			});
		}
	};

	protected SelectionListener brightnessButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (((Button) e.widget).getSelection()) {
				oColour = cColour;
			} else {
				oColour = dColour;
			}
			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					drawOverlays();
					drawCurrentOverlay();
				}
			});
		}
	};

	protected void sendCurrentROI(IROI roib) {
		if (roib == null) {
			removeCurrentROI();
			return;
		}

		if (guiUpdateManager != null)
			guiUpdateManager.putGUIInfo(GuiParameters.ROIDATA, roib.copy());
	}

	protected void sendROIs(@SuppressWarnings("unused") Class<? extends IROI> clazz) {
		removeROIs();
		if (roiDataList.size() == 0) {
			return;
		}

		if (guiUpdateManager != null)
			guiUpdateManager.putGUIInfo(GuiParameters.ROIDATALIST, createROIList());
	}

	protected void removeCurrentROI() {
		if (guiUpdateManager != null)
			guiUpdateManager.removeGUIInfo(GuiParameters.ROIDATA);
	}

	protected void removeROIs() {
		if (guiUpdateManager != null)
			guiUpdateManager.removeGUIInfo(GuiParameters.ROIDATALIST);
	}

	/**
	 * Create new ROI data specific to profile
	 * @param roi
	 * @return ROI data
	 */
	public abstract ROIData createNewROIData(IROI roi);

	/**
	 * Create a list of ROIs
	 * @return list of ROIs
	 */
	public abstract ROIList<? extends IROI> createROIList();

	/**
	 * Update GUI based on information from bean
	 * @param bean
	 * @param onSwitch true if switching to this side plot
	 * @return state defined by flag with following bit masks: ROI, ROILIST, PREFS
	 */
	@SuppressWarnings("unchecked")
	public int updateGUI(GuiBean bean, boolean onSwitch) {
		int update = 0;

		if (bean == null)
			return update;

		logger.debug("Bean: {}", bean);

		Display display = getControl().getDisplay();

		// logic is for each GUI parameter
		//     if null and parameter exists
		//         if not onSwitch
		//             delete parameter
		//         signal updating of parameter
		//     else if same class
		//         replace parameter
		//         signal updating of parameter
		//     else if onSwitch
		//         signal updating of parameter
		if (bean.containsKey(GuiParameters.ROIDATA)) {
			Object obj = bean.get(GuiParameters.ROIDATA);

			if (obj == null) {
				if (roi != null) {
					if (!onSwitch) {
						hideCurrent();
						roi = null;
						roiHandler.setROI(roi);

						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								hideCurrent();
								updatePlot();
								drawOverlays();
							}
						});
						updateAllSpinners(roi);
					}

					update |= ROI;
				}
			} else if (obj.getClass().equals(roiClass)) {
				hideCurrent();
				roi = (IROI) obj;
				roiHandler.setROI(roi);
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						updatePlot();
						drawOverlays();
						drawCurrentOverlay();
					}
				});
				updateAllSpinners(roi);

				update |= ROI;
			} else if (onSwitch) {
				if (roi != null)
					update |= ROI;
			}
		} else if (onSwitch) {
			if (roi != null)
				update |= ROI;
		}

		if (data == null)
			return update;

		if (bean.containsKey(GuiParameters.ROIDATALIST)) {
			Object obj = bean.get(GuiParameters.ROIDATALIST);
			if (obj == null) {
				if (!onSwitch) {
					getDataset();
					removeIDs(roisIDs);
					roisIDs.clear();
					roiDataList.clear();

					// update display and tree view
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							updatePlot();
							tViewer.setInput(roiDataList);
							hideOverlays();
							removeIDs(roiIDs);
							removeHandles();
							drawCurrentOverlay();
						}
					});
				}

				update |= ROILIST;
			} else if (obj.getClass().equals(roiListClass)) {
				ArrayList<? extends IROI> list = (ArrayList<? extends IROI>) obj;

				// remove IDs first (primitives and data)
				getDataset();
				removeIDs(roisIDs);
				roisIDs.clear();
				roiDataList.clear();
				for (IROI roib : list) {

					roiDataList.add(createNewROIData(roib));
					roisIDs.add(-1);
				}

				// update display and tree view
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						updatePlot();
						tViewer.setInput(roiDataList);
						hideCurrent();
						removeIDs(roiIDs);
						removeHandles();
						drawOverlays();
						drawCurrentOverlay();
					}
				});

				update |= ROILIST;
			} else if (onSwitch) {
				update |= ROILIST;
			}
		} else if (onSwitch) {
			if (roiDataList.size() > 0)
				update |= ROILIST;
		}

		return update;
	}

	@Override
	public int updateGUI(GuiBean bean) {
		return updateGUI(bean, false);
	}

	/**
	 * Down-sample image dataset
	 */
	private void createDownsampledDataset() {
		int[] shape = data.getShape();
		int max = Math.max(shape[0], shape[1]);
		subFactor = Math.ceil((double) max / DMAXDIM);
		if (subFactor > 1) {
			shape[0] = (int) subFactor;
			shape[1] = (int) subFactor;
			Downsample map = new Downsample(DownsampleMode.MAXIMUM, shape);
			subData = (Dataset)map.value(data).get(0);
		} else {
			subData = null;
		}
	}

	/**
	 * Check for new dataset and use if it is new and calculate down-sampled version
	 * @return true if dataset has changed
	 */
	public boolean getDataset() {
		IDataset idata = mainPlotter.getCurrentDataSet();
		if (idata != null) {
			Dataset ldata = DatasetUtils.convertToDataset(idata);
			if (!ldata.equals(data)) { // make down-sampled image
				data = ldata;
				createDownsampledDataset();
				return true;
			} else if (data != null && subData == null) {
				createDownsampledDataset();
			}
		}
		return false;
	}


	@Override
	public void processPlotUpdate() {
		updateDataList();

		if (oProvider != null) {
			updatePlot();
			
			//sendCurrentROI(roi);
			//sendROIs(null);
			tViewer.setInput(roiDataList);
			drawOverlays();
			drawCurrentOverlay();
		}
	}

	/**
	 * Push plotting data to another plot view
	 *
	 * @param site
	 * @param fullPlotID
	 */
	public void pushPlottingData(IWorkbenchPartSite site, String fullPlotID, int profileNr) {
		if (roiData != null) {
			PlotView plotView = null;
			try {
				plotView = (PlotView) site.getPage().showView(fullPlotID);

			} catch (PartInitException e) {
				logger.error("All over now! Cannot find plotview: {} ", fullPlotID);
				logger.error(e.toString());
				return;
			}

			if (plotView == null)
				return;

			plotView.updatePlotMode(GuiPlotMode.ONED);

			DataBean dBean = getPlottingData(profileNr);

			if (dBean != null)
				plotView.processPlotUpdate(dBean);
		}
	}

	/**
	 * @return Returns all plotting data as data bean for plotting in another plot view
	 */
	DataBean getPlottingData(int profileNr) {
		DataBean dBean = null;

		if (roiData.getProfileData().length > profileNr) {
			dBean = new DataBean(GuiPlotMode.ONED);
			DatasetWithAxisInformation axisData = new DatasetWithAxisInformation();
			AxisMapBean axisMapBean = new AxisMapBean();

			dBean.addAxis(AxisMapBean.XAXIS, roiData.getXAxis(profileNr).toDataset());
			axisMapBean.setAxisID(new String[] { AxisMapBean.XAXIS });
			axisData.setData(roiData.getProfileData(profileNr));
			axisData.setAxisMap(axisMapBean);

			try {
				dBean.addData(axisData);
			} catch (DataBeanException e) {
				logger.debug("Could not add data to bean");
				e.printStackTrace();
				dBean = null;
			}
		}
		return dBean;
	}

	@Override
	public void generateMenuActions(IMenuManager manager, IWorkbenchPartSite site) {
	}

	@Override
	public void generateToolActions(IToolBarManager manager) {
		Action addtoHistory = new Action() {
			@Override
			public void run() {
				addToHistory();
			}
		};
		addtoHistory.setText("Add current profiles to history");
		addtoHistory.setToolTipText("Adds the current profiles to the plot history");
		addtoHistory.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/basket_put.png"));

		Action removefromHistory = new Action() {
			@Override
			public void run() {
				removeFromHistory();
			}
		};
		removefromHistory.setText("Remove last profiles from history");
		removefromHistory.setToolTipText("Remove the last profiles from the plot history");
		removefromHistory.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/basket_remove.png"));
		manager.add(addtoHistory);
		manager.add(removefromHistory);	
	}

	/**
	 * Set name of ROI according to its type and make it unique
	 * @param roi
	 */
	protected void setROIName(IROI roi) {
		setROIName(null, roi);
	}

	/**
	 * Set name of ROI according to its type and make it unique
	 * @param prefix string to add to front of name
	 * @param roi
	 */
	protected void setROIName(String prefix, IROI roi) {
		final IRegionService rservice = Activator.getService(IRegionService.class);
		String stub = rservice.forROI(roi).getName();
		if (prefix != null)
			stub = prefix + stub;

		List<String> names = new ArrayList<String>();
		for (ROIData r : roiDataList) {
			names.add(r.getROI().getName());
		}

		int i = 1;
		String name;
		do {
			name = stub + " " + i++;
			if (i > 1000)
				break;
		} while (names.contains(name));
		roi.setName(name);
	}
}
