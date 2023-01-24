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

package uk.ac.gda.exafs.ui.dialogs;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.composites.TransmissionComposite;

/**
 * A wizard is used to monitor progress while gain calculation runs.
 */
public class GainWizard extends Wizard {
	private final static Logger logger = LoggerFactory.getLogger(GainWizard.class);
    private GainWizardPage gainPage;
	private IBeanController control;

	public GainWizard(IBeanController control) {
    	super();
    	setNeedsProgressMonitor(true);
		this.control = control;
    }

    @Override
	public void addPages() {
		this.gainPage = new GainWizardPage(control);
        addPage(gainPage);

        getAllValues();
 	}

    @SuppressWarnings("unchecked")
	@Override
	public boolean performFinish() {
		final VerticalListEditor ionChambers;
		final String type = (String) control.getBeanField("experimentType", DetectorParameters.class).getValue();
		if (type.equalsIgnoreCase("Transmission")) {
			TransmissionComposite tpc = (TransmissionComposite) control.getBeanField("transmissionParameters", DetectorParameters.class);
			ionChambers = tpc.getIonChamberParameters();
		}
		else if (type.equalsIgnoreCase("fluorescence")) {
			FluorescenceComposite fpc = (FluorescenceComposite) control.getBeanField("fluorescenceParameters", DetectorParameters.class);
			ionChambers = fpc.getIonChamberParameters();
		}
		else {
			logger.error("Cannot deal with experimentType = '"+type+"'");
			throw new RuntimeException("Cannot deal with experimentType = '"+type+"'");
		}
		try {
			ionChambers.setValue(0, "gain", gainPage.getI0_gain());
			ionChambers.setValue(1, "gain", gainPage.getIt_gain());
			ionChambers.setValue(2, "gain", gainPage.getIref_gain());
		} catch (Exception ne) {
			logger.error("Cannot set gain for ion chambers", ne);
		}
		return true;
	}

	protected void getAllValues() {
		gainPage.getFinalEnergyValue();
		gainPage.getSampleEdgeValue();
		gainPage.getReferenceEdgeSample();
	}

}