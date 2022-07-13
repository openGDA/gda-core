/*
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay2DConsumer;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay2DProvider2;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.objects.PointListObject;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.dawnsci.plotting.api.jreality.tool.IImagePositionEvent;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;

/**
 *
 */
@Deprecated
public class InfoSidePlot extends SidePlot implements Overlay2DConsumer, SelectionListener {

	private Text txtMaxValue;
	private Text txtMinValue;
	private Button btnShowMax;
	private Button btnShowMin;
	private List<Integer> xMaxPos = new ArrayList<Integer>();
	private List<Integer> yMaxPos = new ArrayList<Integer>();
	private List<Integer> xMinPos = new ArrayList<Integer>();
	private List<Integer> yMinPos = new ArrayList<Integer>();
	private Overlay2DProvider2 provider = null;
	private PointListObject maxPoints = null;
	private PointListObject minPoints = null;
	
	@Override
	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
		Action action = super.createSwitchAction(index, plotUI);
		action.setId("uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.InfoSidePlot");
		action.setText("Information");
		action.setToolTipText("Get some raw information");
		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/new.png"));
		return action;
	}
	
	@Override
	public void addToHistory() {
		// Nothing to do

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		Group grpValues = new Group(container, SWT.NONE);
		grpValues.setLayout(new GridLayout(3,false));
		grpValues.setText("Dataset information");
		Label lblMaxValue = new Label(grpValues,SWT.NONE);
		lblMaxValue.setText("Maximum data value: ");
		txtMaxValue = new Text(grpValues,SWT.NONE);
		txtMaxValue.setEditable(false);
		GridData grdData = new GridData(SWT.CENTER,SWT.CENTER,true,false);
		grdData.minimumWidth = 80;
		grdData.widthHint = 80;
		txtMaxValue.setLayoutData(grdData);
		txtMaxValue.setText(Integer.toString(0));
		btnShowMax = new Button(grpValues,SWT.TOGGLE);
		btnShowMax.setText("Show position(s) in plot");
		btnShowMax.addSelectionListener(this);
		Label lblMinValue = new Label(grpValues,SWT.NONE);
		lblMinValue.setText("Minimum data value: ");
		txtMinValue = new Text(grpValues,SWT.NONE);
		txtMinValue.setEditable(false);
		txtMinValue.setLayoutData(grdData);
		txtMinValue.setText(Integer.toString(0));
		btnShowMin = new Button(grpValues,SWT.TOGGLE);
		btnShowMin.setText("Show position(s) in plot");
		btnShowMin.addSelectionListener(this);

	}

	@Override
	public void generateMenuActions(IMenuManager manager, IWorkbenchPartSite site) {
		// TODO Auto-generated method stub

	}

	@Override
	public void generateToolActions(IToolBarManager manager) {
		// TODO Auto-generated method stub

	}

	private boolean determineMinMaxXYPos(IDataset data, float min, float max) {
		try {
			List<IntegerDataset> posns;
			posns = Comparisons.nonZero(Comparisons.equalTo(data, min));
			if (posns.size() == 0)
				return false;
			IndexIterator iter = posns.get(0).getIterator();
			if (posns.size() > 1) {
				IntegerDataset y = posns.get(0);
				IntegerDataset x = posns.get(1);
				while (iter.hasNext()) {
					yMinPos.add(y.getAbs(iter.index));
					xMinPos.add(x.getAbs(iter.index));
				}
			} else {
				IntegerDataset x = posns.get(0);
				while (iter.hasNext()) {
					xMinPos.add(x.getAbs(iter.index));
				}
			}
			posns = Comparisons.nonZero(Comparisons.equalTo(data, max));
			if (posns.size() == 0)
				return false;
			iter = posns.get(0).getIterator();
			if (posns.size() > 1) {
				IntegerDataset y = posns.get(0);
				IntegerDataset x = posns.get(1);
				while (iter.hasNext()) {
					yMaxPos.add(y.getAbs(iter.index));
					xMaxPos.add(x.getAbs(iter.index));
				}
			} else {
				IntegerDataset x = posns.get(0);
				while (iter.hasNext()) {
					xMaxPos.add(x.getAbs(iter.index));
				}
			}
			return true;
		} catch (Exception e) {
		}
		return false;
	}
	
	@Override
	public void processPlotUpdate() {
		if (mainPlotter == null) {
			return;
		}		
		List<IDataset> dataList = mainPlotter.getCurrentDataSets();
		
		xMaxPos.clear();
		yMaxPos.clear();
		xMinPos.clear();
		yMinPos.clear();
		if (minPoints != null)
			minPoints = null;
		if (maxPoints != null)
			maxPoints = null;
		
		if (dataList != null && dataList.size() > 0) {
			IDataset data = dataList.get(0);
			final float min = data.min().floatValue();
			final float max = data.max().floatValue();
			final boolean shouldDisableButtons = determineMinMaxXYPos(data,min,max);
			txtMaxValue.getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					txtMaxValue.setText(Float.toString(max));
					txtMinValue.setText(Float.toString(min));
					btnShowMax.setEnabled(!shouldDisableButtons);
					btnShowMin.setEnabled(!shouldDisableButtons);
					btnShowMax.setSelection(false);
					btnShowMin.setSelection(false);
				}
			});
		}
	}

	@Override
	public void removeFromHistory() {
		// Nothing to do
	}

	@Override
	public void showSidePlot() {
		processPlotUpdate();
	}

	@Override
	public int updateGUI(GuiBean bean) {
		// Nothing to do
		return 0;
	}

	@Override
	public void registerProvider(OverlayProvider provider) {
		if (provider instanceof Overlay2DProvider2)
			this.provider = (Overlay2DProvider2)provider; 
	}

	@Override
	public void removePrimitives() {
		if (maxPoints != null)
			maxPoints = null;
		if (minPoints != null)
			minPoints = null;

	}

	@Override
	public void unregisterProvider() {
		provider = null;

	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void hideOverlays() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showOverlays() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageDragged(IImagePositionEvent event) {
		// Nothing to do
		
	}

	@Override
	public void imageFinished(IImagePositionEvent event) {
		// Nothing to do
		
	}

	@Override
	public void imageStart(IImagePositionEvent event) {
		// Nothing to do
		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// Nothing to do
		
	}
	
	private void displayMax() {
		if (provider != null) {
			if (maxPoints == null)
				maxPoints = (PointListObject)provider.registerObject(PrimitiveType.POINTLIST);
			double[] xPos = new double[xMaxPos.size()];
			double[] yPos = new double[yMaxPos.size()];
			int index = 0;
			Iterator<Integer> xIter = xMaxPos.iterator();
			Iterator<Integer> yIter = yMaxPos.iterator();
			while (xIter.hasNext()) {
				xPos[index] = xIter.next();
				yPos[index++] = yIter.next();
			}
			maxPoints.setPointPositions(xPos,yPos);
			provider.begin(OverlayType.VECTOR2D);
			maxPoints.setColour(java.awt.Color.RED);
			maxPoints.setThick(true);
			maxPoints.draw();
			provider.end(OverlayType.VECTOR2D);
		}
	}

	private void displayMin() {
		if (provider != null) {
			if (minPoints == null)
				minPoints = (PointListObject)provider.registerObject(PrimitiveType.POINTLIST);
			double[] xPos = new double[xMinPos.size()];
			double[] yPos = new double[yMinPos.size()];
			int index = 0;
			Iterator<Integer> xIter = xMinPos.iterator();
			Iterator<Integer> yIter = yMinPos.iterator();
			while (xIter.hasNext()) {
				xPos[index] = xIter.next();
				yPos[index++] = yIter.next();
			}
			minPoints.setPointPositions(xPos,yPos);
			provider.begin(OverlayType.VECTOR2D);
			minPoints.setColour(java.awt.Color.BLUE);
			minPoints.setThick(true);
			minPoints.draw();
			provider.end(OverlayType.VECTOR2D);
		}
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource().equals(btnShowMax)) {
			if (btnShowMax.getSelection()) {
				if (maxPoints == null)
					displayMax();
				else {
					if (provider != null) {
						provider.begin(OverlayType.VECTOR2D);
						maxPoints.setVisible(true);
						provider.end(OverlayType.VECTOR2D);
					}
				}
			} else {
				if (maxPoints != null) {
					if (provider != null) {
						provider.begin(OverlayType.VECTOR2D);
						maxPoints.setVisible(false);
						provider.end(OverlayType.VECTOR2D);
					}
				}
			}
		} else if (e.getSource().equals(btnShowMin)) {
			if (btnShowMin.getSelection()) {
				if (minPoints == null)
					displayMin();
				else {
					if (provider != null) {
						provider.begin(OverlayType.VECTOR2D);
						minPoints.setVisible(true);
						provider.end(OverlayType.VECTOR2D);
					}
				}
			} else {
				if (minPoints != null) {
					if (provider != null) {
						provider.begin(OverlayType.VECTOR2D);
						minPoints.setVisible(false);
						provider.end(OverlayType.VECTOR2D);
					}
				}
			}
		}		
	}
	
}
