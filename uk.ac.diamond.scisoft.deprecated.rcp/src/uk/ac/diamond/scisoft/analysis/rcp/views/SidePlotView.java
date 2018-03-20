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

package uk.ac.diamond.scisoft.analysis.rcp.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay2DConsumer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.plotserver.IBeanScriptingManager;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.ISidePlot;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.ISidePlotView;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.SidePlotManager;

/**
 * Side plot view provides a holding area for any analysis tools that implements ISidePlot. After creating this view,
 * call setPlotView and setMainPlotter to link the main plot view and its data to the tools
 */
@Deprecated
public class SidePlotView extends ViewPart implements ISidePlotView {
	
	private static final Logger logger = LoggerFactory.getLogger(SidePlotView.class);

	/**
	 * 
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView"; //$NON-NLS-1$
	
	private CTabFolder      sidePlotsFolder;
	private Composite       parent;
	private DataSetPlotter  mainPlotter = null;
	private List<ISidePlot> sidePlots   = null;
	private ISidePlot       sidePlot;
	private String          sId;
	private String          id;
	private GuiPlotMode     mode = null;
	private int             currentTabIndex = -1;
	boolean needToDispose = false;
	
	private List<Action> switchActions = null;

	private HistogramUpdate histoUpdate = null;

	static final long updateInterval = 50; // time between updates in milliseconds

	/**
	 * 
	 */
	public SidePlotView() {
		super();
		switchActions = new ArrayList<Action>();
		logger.debug("Using deprecated class");
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		id = site.getId();
		sId = site.getSecondaryId();
		setPartName("Side: " + sId);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
        init(site);
        loadState(memento);
    }


    public void loadState(@SuppressWarnings("unused") IMemento memento) {
       // TODO read rois so that they can be sent to side plots later.
    }
  
    
    @Override
    public void saveState(IMemento memento) {
       // TODO Store rois to memento?
    }

	/**
	 * Create contents of the view part
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;

		//createActions();
		//initializeToolBar();
		//initializeMenu();

		createSidePlotsFolder();
	}
	
	private void createSidePlotsFolder() {
		// new side plot control
		sidePlotsFolder = new CTabFolder(parent, SWT.CLOSE | SWT.BOTTOM);
		sidePlotsFolder.setLayout(new FillLayout());

		sidePlotsFolder.setTabHeight(0); // hide tab bar
	}


	public void disposeOverlays() {
		if (mainPlotter!=null && !mainPlotter.isDisposed()) {
			if (sidePlot != null)
				mainPlotter.unRegisterOverlay(sidePlot);
		}
	}

	/**
	 * Method called to deactivate part, it will then be closed.
	 */
	public void deactivate(boolean leaveSidePlotOpen) {
		
		for (Action a : switchActions) {
			a.setChecked(false);
		}
		
		disposeOverlays();
		
		// We just changed mode potentially.
		sidePlot = null;
		
		// May want to release more resources as 
		// sometimes view can take a lot of memory.
		if (!leaveSidePlotOpen) {
			try {
				this.getSite().getPage().hideView(this);
			} catch (IllegalArgumentException e) {
				// do nothing when perspective is vanishing
			}

			// DO NOT REMOVE OR COMMENT THESE LINES BELOW!!!!!!
			// SIDEPLOTS NEED TO BE SHUTDOWN OTHERWISE THEIR
			// EVENT THREADS DO NOT TERMINATE! THIS CAUSES
			// A THREAD, AND PROBABLY MEMORY, LEAKAGE!

			if (sidePlots != null)
				for (ISidePlot side : sidePlots) {
					if (side != null && !side.isDisposed())
						side.dispose();
				}
		}
	}

	/**
	 * Dispose destroys the part and cannot be
	 * reassigned for doing other things without
	 * bad things happening.
	 */
	@Override
	public void dispose() {

		try {
			deactivate(false);
	
			mainPlotter = null;
			
			// Once did other things here. These could still be
			// done but are not really needed. 
			// SWT should use the widget hierarchy to dispose
			// all that was added, normally.
			super.dispose();
		} finally {
			sidePlots = null;
			histoUpdate = null;
		}
	}
	
	@Override
	public void setFocus() {
		// do nothing special
	}

	@Override
	public void switchSidePlot(final IPlotUI plotUI, final int i) {
				
		if (sidePlot != null) {
			deactivateAllOverlays();
			sidePlot = null;
			if (i == currentTabIndex) {
				currentTabIndex = -1;
				if (switchActions.size()>i)
					switchActions.get(i).setChecked(false);
				if (sidePlotsFolder != null)
					sidePlotsFolder.setVisible(false);

				for (IContributionItem item : getViewSite().getActionBars().getToolBarManager().getItems()) {
					item.dispose();
				}

				getViewSite().getActionBars().getToolBarManager().removeAll();
				getViewSite().getActionBars().getMenuManager().removeAll();
				getViewSite().getActionBars().updateActionBars();

				return;
			}
		}

		if (sidePlots == null && plotUI != null) { // this is necessary when this view was closed by user
			plotUI.initSidePlotView();
		}

		if (parent.isDisposed()) return;

		if (sidePlots.size() == 0) {
			logger.warn("Number of sideplots is zero");
			return;
		}

		try {
			sidePlot = sidePlots.get(i);
			if (sidePlot.isDisposed()) {
				sidePlot.createPartControl(sidePlotsFolder);
				sidePlot.setDisposed(false);
				int nTabs = sidePlotsFolder.getItemCount();
				if (i >= nTabs) {
					logger.error("Selected tab number {} is greater than maximum {}", i, nTabs);
				}
				CTabItem tab = sidePlotsFolder.getItem(i);
				tab.setControl(sidePlot.getControl());
			}
			if (histoUpdate != null) // send cached histogram
				sidePlot.processHistogramUpdate(histoUpdate);
			currentTabIndex = i;
		} catch (Exception e) {
			//FIXME once we were in here it all goes wrong
			logger.error("Error opening chosen side plot", e);
			sidePlot = sidePlots.get(0);
			currentTabIndex = 0;
		}

		parent.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (IContributionItem item : getViewSite().getActionBars().getToolBarManager().getItems()) {
					item.dispose();
				}
				
				getViewSite().getActionBars().getToolBarManager().removeAll();
				getViewSite().getActionBars().getMenuManager().removeAll();
				
				for (Action a : switchActions) {
					a.setChecked(false);
				}
				if (sidePlotsFolder!=null) {
					sidePlotsFolder.setVisible(true);
					sidePlotsFolder.setSelection(i);
				}
				if (switchActions.size()>i) switchActions.get(i).setChecked(true);
				
				sidePlot.generateToolActions(getViewSite().getActionBars().getToolBarManager());
				sidePlot.generateMenuActions(getViewSite().getActionBars().getMenuManager(), getSite());
				getViewSite().getActionBars().updateActionBars();
			
			}
		});

		mainPlotter.registerOverlay(sidePlot);
		sidePlot.setMainPlotUI(plotUI);

		sidePlot.showSidePlot();
	}

	/**
	 * @param plotter DataSetPlotter object
	 * @param manager a GuiInfoManager should be possible to set null (none)
	 */
	public void setPlotView(DataSetPlotter plotter, IBeanScriptingManager manager) {
		
		mainPlotter = plotter;
		if (manager == null) needToDispose = true;
		
		PlottingMode pmode = mainPlotter.getMode();
		GuiPlotMode gmode = pmode.getGuiPlotMode();
		if (gmode == null) {
			logger.error("Not supported!");
			throw new IllegalStateException("Not supported");
		}

		if (mode == gmode) {
			// Since the SidePlot does not get disposed,
			// if the same file is opened again, we need to 
			// tell that plot the mainPlotter, their old copy
			// may be from an editor which is now disposed.
			if (sidePlots != null) for (ISidePlot side : sidePlots) {
				side.setGuiInfoManager(manager);
				side.setMainPlotter(mainPlotter);
			}
            return;
		}

		mode = gmode;

		// discover side plots using extension point
		sidePlots = SidePlotManager.getDefault().getSidePlots(mode,getPartName());

		if (sidePlots == null) { // remove view if there are no side plots configured
			// try {
			// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(this);
			// } catch (NullPointerException e) {
			// // active workbench can be null
			// }
			return;
		}

		for (CTabItem t : sidePlotsFolder.getItems()) t.dispose();
		for (ISidePlot side : sidePlots) {
			side.setUpdateInterval(updateInterval);
			side.setGuiInfoManager(manager);
			side.setMainPlotter(mainPlotter);
			CTabItem tab = new CTabItem(sidePlotsFolder, SWT.NONE); // put empty tabs in folder
			if (!side.isDisposed()) {
				Control c = side.getControl();
				if (c != null) {
					tab.setControl(side.getControl());
				} else {
					logger.warn("Side plot disposal in inconsistent state (control wasn't disposed)");
				}
			}
		}
		sidePlotsFolder.update();
	}

	/**
	 * Update current side plot with info from bean
	 * 
	 * @param guiBean
	 */
	public void updateGUI(GuiBean guiBean) {
		if (sidePlot == null) return;

		sidePlot.updateGUI(guiBean);
	}

	/**
	 * @param plotUI
	 * @return list of actions for toolbar
	 */
	@Override
	public List<Action> createSwitchActions(IPlotUI plotUI) {
		switchActions.clear();

		if (sidePlots == null)
			return switchActions;

		for (int i = 0; i < sidePlots.size(); i++) {
			ISidePlot side = sidePlots.get(i);
			switchActions.add(side.createSwitchAction(i, plotUI));
		}

		return switchActions;
	}

	/**
	 * Notify side plot that main plotter has changed
	 */
	public void processPlotUpdate() {
		if (sidePlot == null) return;

		sidePlot.processPlotUpdate();
	}

	/**
	 * Removes all the overlays and deactivates them, they should be reactivated when one of the buttons is pressed.
	 */
	public void deactivateAllOverlays() {
		if (sidePlot == null)
			return;

		if (sidePlot instanceof Overlay2DConsumer)
			((Overlay2DConsumer) sidePlot).hideOverlays();
		sidePlot.removePrimitives();
		mainPlotter.unRegisterOverlay(sidePlot);
	}

	/**
	 * Send histogram update information to all side plots
	 * 
	 * @param update
	 */
	public void sendHistogramUpdate(HistogramUpdate update) {
		histoUpdate  = update;
		if (sidePlot == null) return;

		sidePlot.processHistogramUpdate(update);
	}

	@Override
	public void setSwitchActions(List<Action> actions) {
		if (actions == null)
			return;
		if (switchActions == null)
			switchActions = new ArrayList<Action>();
		else
			switchActions.clear();
		switchActions.addAll(actions);
	}

	@Override
	public ISidePlot getActivePlot() {
		return sidePlot;
	}
	
}
