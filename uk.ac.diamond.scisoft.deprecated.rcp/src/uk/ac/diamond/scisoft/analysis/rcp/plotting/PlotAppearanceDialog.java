/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DStyles;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

/**
 *
 */
public class PlotAppearanceDialog extends Dialog implements SelectionListener {
	private Button btnOk;
	private List lstGraphs;
	private Label lblColour;
	private Button btnColour;
	private Button btnCancel;
	private Label lblStyle;
	private Combo cmbStyle;
	private Combo cmbWidth;
	private Label lblWidth;
	private Plot1DGraphTable graphTable  = null;
	private Plot1DGraphTable copyGraphTable = null;
	private Shell shell;
	private boolean update = false;
	
	/**
	 * Create a PlotAppearanceDialog
	 * @param parent
	 * @wbp.parser.constructor
	 */
	public PlotAppearanceDialog(Shell parent) {
		super(parent);
	}

	/**
	 * Create a PlotAppearanceDialog
	 * @param parent
	 * @param table
	 */
	public PlotAppearanceDialog(Shell parent, Plot1DGraphTable table)
	{
		this(parent);
		graphTable = table;
		if (graphTable != null)
		{
			// first make a copy of the Plot1DGraphTable
			// so that all operations are done on the copy
			// and only the changes are submitted to the 
			// the real table when the user presses the
			// update button
			
			copyGraphTable = new Plot1DGraphTable();
			for (int i = 0; i < graphTable.getLegendSize(); i++)
			{
				Plot1DAppearance plotApp = graphTable.getLegendEntry(i);
				Plot1DAppearance copyPlotApp = new Plot1DAppearance(plotApp.getColour(),
																	plotApp.getStyle(),
																	plotApp.getLineWidth(),
																	plotApp.getName(),plotApp.isVisible());
				copyGraphTable.addEntryOnLegend(copyPlotApp);
			}
		}
	}
	
	/**
	 * Open the dialog
	 * @return true if the appearance have been updated otherwise false
	 */
	@SuppressWarnings("unused")
	public boolean open() {
		Shell parent = getParent();
		shell = new Shell(parent,SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setSize(418, 280);
		shell.setText("Set appearance of a graph");
		shell.setImage(AnalysisRCPActivator.getImageDescriptor("icons/color_wheel.png").createImage());
		shell.setLayout(new GridLayout(6, false));
		{
			lstGraphs = new List(shell, SWT.BORDER | SWT.V_SCROLL);
			{
				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
				gridData.heightHint = 173;
				gridData.widthHint = 378;
				lstGraphs.setLayoutData(gridData);
			}
		}
		lstGraphs.addSelectionListener(this);
		{
			lblColour = new Label(shell, SWT.NONE);
			lblColour.setText("Colour:");
		}
		{
			btnColour = new Button(shell, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
				gridData.widthHint = 41;
				btnColour.setLayoutData(gridData);
			}
		}
		btnColour.addSelectionListener(this);
		{
			lblStyle = new Label(shell, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
				gridData.widthHint = 43;
				lblStyle.setLayoutData(gridData);
			}
			lblStyle.setText("Style:");
		}
		{
			cmbStyle = new Combo(shell, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
				gridData.widthHint = 69;
				cmbStyle.setLayoutData(gridData);
			}
		}
		cmbStyle.add("Solid");
		cmbStyle.add("Dashed");
		cmbStyle.add("Points");
		cmbStyle.add("Solid with points");
		cmbStyle.add("Dashed with points");
		cmbStyle.select(0);
		cmbStyle.addSelectionListener(this);
		{
			lblWidth = new Label(shell, SWT.WRAP);
			{
				GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
				gridData.widthHint = 60;
				lblWidth.setLayoutData(gridData);
			}
			lblWidth.setText("Width:");
		}
		{
			cmbWidth = new Combo(shell, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
				gridData.widthHint = 69;
				cmbWidth.setLayoutData(gridData);
			}
		}
		cmbWidth.add("0.5");
		cmbWidth.add("1.0");
		cmbWidth.add("2.0");
		cmbWidth.add("4.0");
		cmbWidth.add("8.0");
		cmbWidth.select(1);
		cmbWidth.addSelectionListener(this);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		{
			btnOk = new Button(shell, SWT.NONE);
			btnOk.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
			btnOk.setText("Update");
		}
		btnOk.addSelectionListener(this);
		new Label(shell, SWT.NONE);
		{
			btnCancel = new Button(shell, SWT.NONE);
			btnCancel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
			btnCancel.setText("Cancel");
		}
		btnCancel.addSelectionListener(this);
		new Label(shell, SWT.NONE);
		if (copyGraphTable != null)
		{
			for (int i = 0; i < copyGraphTable.getLegendSize(); i++)
			{
				Plot1DAppearance plotApp = copyGraphTable.getLegendEntry(i);
				lstGraphs.add(plotApp.getName());
			}
		}
		
		
		shell.open();
		Display display = parent.getDisplay();

		if (copyGraphTable != null && 
				copyGraphTable.getLegendSize() > 0) 
		{
				lstGraphs.select(0);
				updateWidgets();
		}
	
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return update;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// Nothing to do
		
	}
	
	private void updateWidgets() {
		Plot1DAppearance plotApp = copyGraphTable.getLegendEntry(lstGraphs.getSelectionIndex());
		if (plotApp.getStyle() == Plot1DStyles.SOLID)
			cmbStyle.select(0);
		else if (plotApp.getStyle() == Plot1DStyles.DASHED)
			cmbStyle.select(1);
		else if (plotApp.getStyle() == Plot1DStyles.POINT)
			cmbStyle.select(2);
		else if (plotApp.getStyle() == Plot1DStyles.SOLID_POINT)
			cmbStyle.select(3);
		else if (plotApp.getStyle() == Plot1DStyles.DASHED_POINT)
			cmbStyle.select(4);
		
		java.awt.Color colour = plotApp.getColour();
		org.eclipse.swt.graphics.RGB rgb = 
			new org.eclipse.swt.graphics.RGB(colour.getRed(),
											 colour.getGreen(),
											 colour.getBlue());
		
		org.eclipse.swt.graphics.Color color = new
			org.eclipse.swt.graphics.Color(btnColour.getDisplay(),rgb);
		btnColour.setForeground(color);
		btnColour.setBackground(color);
		cmbWidth.select((plotApp.getLineWidth() >> 1));		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource().equals(lstGraphs))
		{
			updateWidgets();
		}
		if (e.getSource().equals(cmbStyle))
		{
			Plot1DAppearance plotApp = 
				copyGraphTable.getLegendEntry(lstGraphs.getSelectionIndex());
			if (cmbStyle.getSelectionIndex() == 0)
				plotApp.setStyle(Plot1DStyles.SOLID);
			else if (cmbStyle.getSelectionIndex() == 1)
				plotApp.setStyle(Plot1DStyles.DASHED);
			else if (cmbStyle.getSelectionIndex() == 2)
				plotApp.setStyle(Plot1DStyles.POINT);
			else if (cmbStyle.getSelectionIndex() == 3)
				plotApp.setStyle(Plot1DStyles.SOLID_POINT);
			else if (cmbStyle.getSelectionIndex() == 4)
				plotApp.setStyle(Plot1DStyles.DASHED_POINT);
		}
		if (e.getSource().equals(cmbWidth))
		{
			Plot1DAppearance plotApp =
				copyGraphTable.getLegendEntry(lstGraphs.getSelectionIndex());
			plotApp.setLineWidth((cmbWidth.getSelectionIndex() << 1));
		}
		if (e.getSource().equals(btnColour))
		{
			if (lstGraphs.getSelectionIndex() >= 0)
			{
				Plot1DAppearance plotApp = copyGraphTable.getLegendEntry(lstGraphs.getSelectionIndex());
				ColorDialog cd = new ColorDialog(getParent().getShell());
				cd.setText("Line colour");
				cd.setRGB(new RGB(255,0,0));
				RGB selectedColour = cd.open();
				if (selectedColour != null)
				{
					plotApp.setColour(new java.awt.Color(selectedColour.red,
														 selectedColour.green,
														 selectedColour.blue));
					org.eclipse.swt.graphics.Color color = new
						org.eclipse.swt.graphics.Color(btnColour.getDisplay(),selectedColour);
					btnColour.setForeground(color);
					btnColour.setBackground(color);
				}
			}
		}
		if (e.getSource().equals(btnCancel))
		{
			shell.close();
		}
		if (e.getSource().equals(btnOk))
		{
			if (copyGraphTable != null)
			{
				update = true;
				for (int i = 0; i < copyGraphTable.getLegendSize(); i++)
				{
					Plot1DAppearance newApp = copyGraphTable.getLegendEntry(i);
					Plot1DAppearance oldApp = graphTable.getLegendEntry(i);
					oldApp.setColour(newApp.getColour());
					oldApp.setStyle(newApp.getStyle());
					oldApp.setLineWidth(newApp.getLineWidth());
				}
				copyGraphTable.clearLegend();
			}
			shell.close();
		}
	}
}
