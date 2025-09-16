/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.standards;

import static uk.ac.diamond.daq.mapping.ui.exafs.ExafsParametersSection.numericTextBox;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.getXanesElementsList;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.roundDouble;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.mapping.api.StandardsScanParams;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.LineToTrack;
import uk.ac.diamond.daq.mapping.ui.xanes.XanesEdgeCombo;

public class StandardsExafsScanView extends StandardsScanView {

	private static final String SCRIPT_FILE = "scanning/submit_standards_exafs_scan.py";
	private static final String BUTTON_NAME = "Submit EXAFS standards scan";
	private static final Color BUTTON_COLOUR = new Color(Display.getDefault(), new RGB(170, 204, 0));

	private Text edgeEnergyText;

	@Override
	protected Composite createEnergyComposite(Composite parent) {
		final Composite editorComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(editorComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(editorComposite);

		LabelFactory.newLabel(SWT.NONE).create(editorComposite).setText("Edge Energy");
		edgeEnergyText = numericTextBox(editorComposite);
		edgeEnergyText.setEnabled(false);
		edgeEnergyText.setText("0.0");

		final Composite edgeAndExposureComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(edgeAndExposureComposite);
	    GridLayoutFactory.swtDefaults().numColumns(4).margins(5, 5).spacing(5, 20).applyTo(edgeAndExposureComposite);

	    var elementList = getXanesElementsList();
	    if (elementList.isPresent()) {
	    	XanesEdgeCombo edgeCombo = new XanesEdgeCombo(edgeAndExposureComposite, elementList.get());
	    	edgeCombo.addSelectionChangedListener(e ->
	    		handleEdgeSelectionChanged(edgeCombo.getSelection()));
	    }

		return edgeAndExposureComposite;
	}

	private void handleEdgeSelectionChanged(IStructuredSelection selection) {
		final EdgeToEnergy selectedEdge = (EdgeToEnergy) selection.getFirstElement();
		if (selectedEdge == null) {
			return;
		}

		double edgeEnergyValue = roundDouble(selectedEdge.getEnergy());
		edgeEnergyText.setText(String.valueOf(edgeEnergyValue));

		String edge = selectedEdge.getEdge();
		String element = edge.split("-")[0];
		String line = edge.split("-")[1];
		lineToTrack = new LineToTrack(element, line);
	}

	@Override
	protected StandardsScanParams setScanParameters() {
		final StandardsScanParams scanParams = new StandardsScanParams();
		scanParams.setExposureTime(Double.parseDouble(exposureTimeText.getText()));
		scanParams.setReverseScan(reverseCheckBox.getSelection());
		scanParams.setLineToTrack(lineToTrack);
		scanParams.setXasPosition(selectedXasPosition.getPosition());
		return scanParams;
	}

	@Override
	@PostConstruct
	public void createView(Composite parent) {
		super.createView(parent);
	}

	@Override
	protected String getScriptFile() {
		return SCRIPT_FILE;
	}

	@Override
	protected String getButtonName() {
		return BUTTON_NAME;
	}

	@Override
	protected Color getButtonColour() {
		return BUTTON_COLOUR;
	}
}
