/*-
 * Copyright © 2017 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tschoonj.xraylib.Xraylib;
import com.github.tschoonj.xraylib.XraylibException;

import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;
import uk.ac.gda.exafs.ui.data.ScanObject;

/**
 * Composite with controls to select an element and transition resonance line from combo boxes
 * (like in QExafs, Xas scan settings window). The line position can also be displayed in the plot of MCA data in
 * ({@link FluorescenceDetectorComposite}. Xraylib is used to get the necessary transition data for each element.
 * @since 2/11/2017
 */
public class FluoDetectorTransitionLineComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(FluoDetectorTransitionLineComposite.class);

	private Label elementNameLabel;
	private Label elementLineLabel;
	private Combo elementNameCombo;
	private Combo elementLineCombo;
	private Label lineEnergyLabel;
	private Label lineEnergyValueLabel;
	private Button showLineButton;
	private Button setLineFromScanSettingsButton;

	// Convert energy from eV to MCA channel
	// May need to customize this; XSpress2 uses 0.1; Xspress3, Vortex might be different
	private double energyToMcaChannelFactor = 0.1;

	// Arrays of transition names and indices for Xraylib (copied from XraylibComposite in master branch).
	private static final String[] transitionNames = new String[] {
			"KL2 (K\u03B12)", "KL3 (K\u03B11)", "KM2 (K\u03B23)", "KM3 (K\u03B21)", "KN2", "KN3 (K\u03B22)", "KO2", "KO3",
			"L1M2 (L\u03B24)", "L1M3 (L\u03B23)", "L1M4 (L\u03B210)", "L1M5 (L\u03B29)", "L1N2 (L\u03B32)", "L1N3 (L\u03B33)", "L1N4", "L1N5", "L1O2", "L1O3 (L\u03B34)",
			"L2M1 (L\u03B7)", "L2M3 (L\u03B217)", "L2M4 (L\u03B21)", "L2M5", "L2N1 (L\u03B35)", "L2N3", "L2N4 (L\u03B31)", "L2N5", "L2N6", "L2O1 (L\u03B38)", "L2O3", "L2O4 (L\u03B36)",
			"L3M1 (Ll)", "L3M2 (Lt)", "L3M4 (L\u03B12)", "L3M5 (L\u03B11)", "L3N1 (L\u03B26)", "L3N3", "L3N4 (L\u03B215)", "L3N5 (L\u03B22)", "L3N6", "L3N7", "L3O1 (L\u03B27)", "L3O2",
			"M1N2", "M1N3", "M1N4", "M1N5", "M1N6", "M1N7", "M1O2", "M1O3", "M1O4", "M1O5",
			"M2O1", "M2N3", "M2N4", "M2N5", "M2N6", "M2N7", "M2O1", "M2O3", "M2O4", "M2P1",
			"M3N1", "M3N2", "M3N4", "M3N5 (M\u03B3)", "M3N6", "M3N7", "M3O1", "M3O2", "M3O4", "M3O5", "M3P1", "M3P2",
			"M4N1", "M4N2", "M4N3", "M4N5", "M4N6 (M\u03B2)", "M4N7", "M4O1", "M4O2", "M4O3", "M4O5", "M4P1", "M2P2",
			"M5N1", "M5N2", "M5N3", "M5N4", "M5N6 (M\u03B12)", "M5N7 (M\u03B11)", "M5O1", "M5O2", "M5O3", "M5O4", "M5P1", "M5P2"
		};

	private static final int[] transitionMacros = new int[] {
			Xraylib.KL2_LINE, Xraylib.KL3_LINE, Xraylib.KM2_LINE, Xraylib.KM3_LINE, Xraylib.KN2_LINE, Xraylib.KN3_LINE, Xraylib.KO2_LINE, Xraylib.KO3_LINE,
			Xraylib.L1M2_LINE, Xraylib.L1M3_LINE, Xraylib.L1M4_LINE, Xraylib.L1M5_LINE, Xraylib.L1N2_LINE, Xraylib.L1N3_LINE, Xraylib.L1N4_LINE, Xraylib.L1N5_LINE, Xraylib.L1O2_LINE, Xraylib.L1O3_LINE,
			Xraylib.L2M1_LINE, Xraylib.L2M3_LINE, Xraylib.L2M4_LINE, Xraylib.L2M5_LINE, Xraylib.L2N1_LINE, Xraylib.L2N3_LINE, Xraylib.L2N4_LINE, Xraylib.L2N5_LINE, Xraylib.L2N6_LINE, Xraylib.L2O1_LINE, Xraylib.L2O3_LINE, Xraylib.L2O4_LINE,
			Xraylib.L3M1_LINE, Xraylib.L3M2_LINE, Xraylib.L3M4_LINE, Xraylib.L3M5_LINE, Xraylib.L3N1_LINE, Xraylib.L3N3_LINE, Xraylib.L3N4_LINE, Xraylib.L3N5_LINE, Xraylib.L3N6_LINE, Xraylib.L3N7_LINE, Xraylib.L3O1_LINE, Xraylib.L3O2_LINE,
			Xraylib.M1N2_LINE, Xraylib.M1N3_LINE, Xraylib.M1N4_LINE, Xraylib.M1N5_LINE, Xraylib.M1N6_LINE, Xraylib.M1N7_LINE, Xraylib.M1O2_LINE, Xraylib.M1O3_LINE, Xraylib.M1O4_LINE, Xraylib.M1O5_LINE,
			Xraylib.M2O1_LINE, Xraylib.M2N3_LINE, Xraylib.M2N4_LINE, Xraylib.M2N5_LINE, Xraylib.M2N6_LINE, Xraylib.M2N7_LINE, Xraylib.M2O1_LINE, Xraylib.M2O3_LINE, Xraylib.M2O4_LINE, Xraylib.M2P1_LINE,
			Xraylib.M3N1_LINE, Xraylib.M3N2_LINE, Xraylib.M3N4_LINE, Xraylib.M3N5_LINE, Xraylib.M3N6_LINE, Xraylib.M3N7_LINE, Xraylib.M3O1_LINE, Xraylib.M3O2_LINE, Xraylib.M3O4_LINE, Xraylib.M3O5_LINE, Xraylib.M3P1_LINE, Xraylib.M3P2_LINE,
			Xraylib.M4N1_LINE, Xraylib.M4N2_LINE, Xraylib.M4N3_LINE, Xraylib.M4N5_LINE, Xraylib.M4N6_LINE, Xraylib.M4N7_LINE, Xraylib.M4O1_LINE, Xraylib.M4O2_LINE, Xraylib.M4O3_LINE, Xraylib.M4O5_LINE, Xraylib.M4P1_LINE, Xraylib.M2P2_LINE,
			Xraylib.M5N1_LINE, Xraylib.M5N2_LINE, Xraylib.M5N3_LINE, Xraylib.M5N4_LINE, Xraylib.M5N6_LINE, Xraylib.M5N7_LINE, Xraylib.M5O1_LINE, Xraylib.M5O2_LINE, Xraylib.M5O3_LINE, Xraylib.M5O4_LINE, Xraylib.M5P1_LINE, Xraylib.M5P2_LINE
		};

	public FluoDetectorTransitionLineComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new FillLayout());

		Group elementEdgeGroup = new Group(this, SWT.NONE);
		elementEdgeGroup.setText("Element name and line selection");
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(elementEdgeGroup);

		showLineButton = new Button(elementEdgeGroup, SWT.CHECK);
		GridDataFactory.swtDefaults().span(3,1).applyTo(showLineButton);
		showLineButton.setText("Show line in plot");
		showLineButton.setSelection(false);

		setLineFromScanSettingsButton = new Button(elementEdgeGroup, SWT.PUSH);
		setLineFromScanSettingsButton.setText("Set line from scan");
		GridDataFactory.swtDefaults().span(1,1).applyTo(setLineFromScanSettingsButton);
		setLineFromScanSettingsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setElementAndLineFromScan();
			}
		});

		elementNameLabel = new Label(elementEdgeGroup, SWT.NONE);
		elementNameLabel.setText("Element name : ");

		elementNameCombo = new Combo(elementEdgeGroup, SWT.READ_ONLY);
		elementNameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		elementNameCombo.setItems( getElements(5, 93) );
		elementNameCombo.setSize(100, SWT.DEFAULT);
		elementNameCombo.select(0);

		elementLineLabel = new Label(elementEdgeGroup, SWT.NONE);
		elementLineLabel.setText("Line : ");

		elementLineCombo = new Combo(elementEdgeGroup, SWT.READ_ONLY);
		elementLineCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		elementLineCombo.setSize(100, SWT.DEFAULT);

		lineEnergyLabel = new Label(elementEdgeGroup, SWT.NONE);
		lineEnergyLabel.setText("Line energy (keV) : ");

		lineEnergyValueLabel = new Label(elementEdgeGroup, SWT.NONE);
		lineEnergyValueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		updateLineComboItems();
		updateLineEnergyValueLabel();
		setElementAndLineFromScan();

		// When selected element changes : Update 'line' combo contents, select first item, and update the transition energy label
		elementNameCombo.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLineComboItems();
				updateLineEnergyValueLabel();
			}
		});

		// When selected line changes : Update line energy label
		elementLineCombo.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLineEnergyValueLabel();
			}
		});
	}

	/**
	 * Return array of element names in atomic number, for elements with atomic numbers between minZ and maxZ (inclusive)
	 * @param minZ
	 * @param maxZ
	 * @return
	 */
	private String[] getElements(int minZ, int maxZ) {
		List<String> elementNameList = new ArrayList<>();
		for(Element el : Element.getElements(minZ, maxZ) ) {
			elementNameList.add(el.getSymbol());
		}
		return elementNameList.toArray(new String[]{});
	}

	/**
	 * Try to set the element and line from QExafs, Xas scan parameters of currently selected scan
	 */
	public void setElementAndLineFromScan() {
		// Return immediately if can't get required extension point (i.e. client not using Experiment explorer and xml files to setup experiments)
		try {
			ExperimentFactory.getExperimentObjectManagerClass();
		} catch (Exception e1) {
			logger.info("Element and edge information not available from scan settings - using default values.");
		}

		final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
		try {
			final IScanParameters params = ob.getScanParameters();

			// XasScanParameters, XanesScanParameters, QExafsScanParameters etc. all contain this info and have
			// getters of same name but don't share a common interface...
			// Use reflection to access methods to get element name and edge values, rather than lots of instanceof and casting...
			Method elemMeth = params.getClass().getDeclaredMethod("getElement");
			Method edgeMeth = params.getClass().getDeclaredMethod("getEdge");
			String elemVal = elemMeth.invoke(params).toString();
			String edgeVal = edgeMeth.invoke(params).toString();

			setSelectedElementName(elemVal);
			setSelectedLineFromEdge(edgeVal);
			showLineButton.setSelection(true);
		} catch (Exception e) {
			logger.warn("Problem getting element and edge information from scan settings", e);
		}
	}

	/**
	 * Make list of transition line names for selected energy using Xraylib.
	 * @param element
	 * @return
	 */
	private List<String> getLinesForElement(Element element) {
		List<String> lines = new ArrayList<>();
		int atomicNumber = element.getAtomicNumber();
		for (int line = 0; line < transitionMacros.length; line++) {
			try {
				// get line energy - this throws an exception if the specified line doesn't exist for the element
				Xraylib.LineEnergy(atomicNumber, transitionMacros[line]);

				// add line name to the list
				lines.add(transitionNames[line]);
			} catch (XraylibException e) {
				// go to next line
			}
		}
		return lines;
	}

	private int getTransitionMacroFromName(String name) {
		int ind =  ArrayUtils.indexOf(transitionNames, name);
		return transitionMacros[ind];
	}

	/**
	 *
	 * Update the line combo box with new list of available lines for currently selected element and select the first one.
	 */
	private void updateLineComboItems() {
		if ( getElement() != null) {
			List<String> lines = getLinesForElement(getElement());
			elementLineCombo.setItems(lines.toArray(new String[]{}));
			elementLineCombo.select(0);
		}
	}

	private void updateLineEnergyValueLabel() {
		double energyKev = getLineEnergy();
		lineEnergyValueLabel.setText(String.format("%.4f", energyKev));
	}

	/**
	 * @return {@link Element} object for currently selected element
	 */
	private Element getElement() {
		String selectedElement = getSelectedElementName();
		if (StringUtils.isNotEmpty(selectedElement)) {
			return Element.getElement(selectedElement);
		} else {
			return null;
		}
	}

	/**
	 * @return Energy of currently selected element and line (in keV)
	 */
	public double getLineEnergy() {
		Element element = getElement();
		if (element !=null) {
			int transMacro = getTransitionMacroFromName(getSelectedLineName());
			return Xraylib.LineEnergy(element.getAtomicNumber(), transMacro);
		} else {
			return 0.0;
		}
	}

	/**
	 * @return MCA channel corresponding to currently selected element and line (i.e {@link #getLineEnergy()}*energyToMcaChannelFactor)
	 */
	public double getEdgeMcaChannel() {
		return getLineEnergy()*1000*energyToMcaChannelFactor;
	}
	/**
	 * @return Name of selected element
	 */
	public String getSelectedElementName() {
		return elementNameCombo.getText();
	}

	/**
	 * Select named element in 'element name' combo box.
	 * @param element
	 */
	public void setSelectedElementName(String element) {
		if (selectInComboBox(elementNameCombo, element)) {
			updateLineComboItems();
			updateLineEnergyValueLabel();
		}
	}

	/**
	 * @return Name of selected line
	 */
	public String getSelectedLineName() {
		return elementLineCombo.getText();
	}

	/**
	 * Select line with maximum radiative transition rate belonging to edge in 'line' combo box.
	 * @param edgeName
	 */
	public void setSelectedLineFromEdge(String edgeName) {
		int Z = getElement().getAtomicNumber();
		String selectedLine = "";
		double maxRadRate = 0;
		for(String lineName : elementLineCombo.getItems()) {
			if (lineName.startsWith(edgeName)){
				int transMacro = getTransitionMacroFromName(lineName);
				double radRate = Xraylib.RadRate(Z, transMacro);
				if (radRate > maxRadRate) {
					maxRadRate = radRate;
					selectedLine = lineName;
				}
			}
		}
		selectInComboBox(elementLineCombo, selectedLine);
		updateLineEnergyValueLabel();
	}

	/**
	 * Select an item in a combo box
	 * @param comboBox
	 * @param item to try and select in combo
	 * @return true if selected item in combo box changed
	 */
	private boolean selectInComboBox(Combo comboBox, String item) {
		int index = ArrayUtils.indexOf(comboBox.getItems(), item);
		if (index != -1 && index != comboBox.getSelectionIndex()) {
			comboBox.select(index);
			return true;
		}
		return false;
	}

	public boolean getShowLineInPlot() {
		return showLineButton.getSelection();
	}

	public void setShowLineInPlot(boolean selected) {
		showLineButton.setSelection(selected);
	}

	public void addSelectionListener(SelectionListener listener) {
		elementNameCombo.addSelectionListener(listener);
		elementLineCombo.addSelectionListener(listener);
		showLineButton.addSelectionListener(listener);
		setLineFromScanSettingsButton.addSelectionListener(listener);
	}
}