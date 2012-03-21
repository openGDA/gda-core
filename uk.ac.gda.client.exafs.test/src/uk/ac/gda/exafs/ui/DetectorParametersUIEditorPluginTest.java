/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;


import java.io.File;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.ClientManager;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.composites.IonChamberComposite;
import uk.ac.gda.exafs.ui.composites.TransmissionComposite;
import uk.ac.gda.exafs.ui.describers.DetectorDescriber;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.util.PackageUtils;

/**
 * Run as junit plugin test.
 */
public class DetectorParametersUIEditorPluginTest {
	
	/**
	 * Force into testing mode.
	 */
	static {
		ClientManager.setTestingMode(true);
	}
	
	private DetectorParametersEditor   editor;
	private DetectorParametersUIEditor uiEd;
	/**
	 * @throws Throwable
	 */
	@Before
	public void setUp() throws Throwable {
		ClientManager.setTestingMode(true);
		
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		final File xml = new File(PackageUtils.getTestPath(DetectorParametersUIEditorPluginTest.class)+"/Detector_Parameters.xml");	
		final FileStoreEditorInput fileInput = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(xml));
	
		this.editor = (DetectorParametersEditor)window.getActivePage().openEditor(fileInput, DetectorDescriber.ID);
		this.uiEd = (DetectorParametersUIEditor)editor.getRichBeanEditor();
	}
	
	/**
	 * Bounds working
	 * @throws Throwable
	 */ 
	@SuppressWarnings("unchecked")
	@Test
	public final void testTransmission() throws Throwable {
		
		if (!uiEd.getExperimentType().getValue().equals("Transmission")) {
			throw new Exception("The XML file is configured for transmisson but the UI did not pick this up.");
		}
		
		final TransmissionComposite trans = uiEd.getTransmissionParameters();
		if (!trans.getWorkingEnergy().getValue().equals(7111.14d)) {
			throw new Exception("The working energy is not 7111.14 as expected but is "+uiEd.getTransmissionParameters().getWorkingEnergy().getValue());
		}
		
		// Ion Chambers
		final VerticalListEditor            ionEd = trans.getIonChamberParameters();
		final List<IonChamberParameters> vals = (List<IonChamberParameters>)ionEd.getValue();
		if (!vals.get(0).getName().equals("I0"))   throw new Exception("The first ion chamber must be I0!");
		if (!vals.get(1).getName().equals("It"))   throw new Exception("The first ion chamber must be It!");
		if (!vals.get(2).getName().equals("Iref")) throw new Exception("The first ion chamber must be Iref!");
		
		// NOTE if this is true the mucal code was run.
		Object press = ((IonChamberComposite)ionEd.getEditorUI()).getPressure().getValue();
		// Pressure should be calculated from the value in the XML 986.6494125122085 to 6.161255
		if (Double.parseDouble((String)press)!=6.161255d) {
			throw new Exception("Pressure for I0 was "+vals.get(0).getPressure()+" and should be 6.161255");
		}
		
		ionEd.setSelectedIndex(1);
		press = ((IonChamberComposite)ionEd.getEditorUI()).getPressure().getValue();
		if (Double.parseDouble((String)press)!=12.385063d) {
			throw new Exception("Pressure for I0 was "+vals.get(0).getPressure()+" and should be 6.161255");
		}
		
		final IonChamberComposite comp = (IonChamberComposite)ionEd.getEditorUI();
		comp.getGasType().setValue("N");
		if (!comp._testIsPressureOk()) throw new Exception("Pressure should be ok for the He+N2 gas combination.");
		
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public final void testFluorescence() throws Throwable {
		
		uiEd.getExperimentType().setValue("Fluorescence");
		if (!uiEd.getExperimentType().getValue().equals("Fluorescence")) {
			throw new Exception("The XML file is configured for Fluorescence but the UI did not pick this up.");
		}
		
		final FluorescenceComposite fluoro  = uiEd.getFluorescenceParameters();
		if (!fluoro.getConfigFileName().getText().equals("Xspress_Parameters.xml")) {
			throw new Exception("Expected that the Germanium file name would be Xspress_Parameters.xml and was "+fluoro.getConfigFileName().getText());
		}
		
		final IonChamberComposite comp = (IonChamberComposite)(fluoro.getIonChamberParameters().getEditorUI());
		if (!comp.getDeviceName().getValue().equals("counterTimer01")) {
			throw new Exception("Device name expected counterTimer01 and was "+comp.getDeviceName().getValue());
		}
		if (!comp.getChannel().getValue().equals(1)) {
			throw new Exception("Channel expected 1 and was "+comp.getChannel().getValue());
		}

	}
}
