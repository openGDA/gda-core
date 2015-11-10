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

package uk.ac.gda.exafs.ui.composites;

import gda.util.Element;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldBeanComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class WorkingEnergyComposite extends FieldBeanComposite {

	protected VerticalListEditor diodeParameters;

	private final static Logger logger = LoggerFactory.getLogger(WorkingEnergyComposite.class);

	private DetectorParameters provider;

	public WorkingEnergyComposite(Composite parent, int style, DetectorParameters abean) {
		super(parent, style);
		provider = abean;
	}

	protected ScaleBox workingEnergy;
	private Button workingEnergyBtn;
	Button selectDefaultsBtn;
	private SelectionAdapter workingEnergyListener;
	SelectionAdapter selectDefaultsListener;
	private Group tabFolder;
	QEXAFSParameters qexafsBean;
	XasScanParameters xasBean;
	XanesScanParameters xanesBean;



	protected void createEdgeEnergy(Composite comp, IBeanController control) {

		Label workingELbl = new Label(comp, SWT.NONE);
		workingELbl.setText("Calculate ion chamber gas filling for energy:");

		Composite workingEComp = new Composite(comp, SWT.NONE);
		GridLayout glWorkingE = new GridLayout(3, false);
		glWorkingE.marginHeight = 0;
		glWorkingE.marginWidth = 0;
		workingEComp.setLayout(glWorkingE);
		GridData gdWorkingE = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdWorkingE.widthHint = 490;
		workingEComp.setLayoutData(gdWorkingE);

		workingEnergy = new ScaleBox(workingEComp, SWT.NONE);
		workingEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		workingEnergy.setMaximum(20000.0);
		workingEnergy.setUnit("eV");
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.widthHint = 120;
		workingEnergy.setLayoutData(gd);

		this.workingEnergyBtn = new Button(workingEComp, SWT.NONE);
		workingEnergyBtn.setText("Get Energy From Scan");
		workingEnergyBtn.setToolTipText("Click to take edge energy from currently open scan parameters.");
		this.workingEnergyListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setWorkingFromEdge();
			}
		};
		workingEnergyBtn.addSelectionListener(workingEnergyListener);

		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.HIDE_DEFAULT_GAS_MIXTURES_BUTTON)) {
			this.selectDefaultsBtn = new Button(workingEComp, SWT.NONE);
			selectDefaultsBtn.setText("Set Default Gas Mixtures");
			selectDefaultsBtn.setToolTipText("Click to set ion chamber gas types to defaults.");
		}


		IExperimentObject selectedScan = ExperimentFactory.getExperimentEditorManager().getSelectedScan();

		// no point making any connections if no scan has been selected
		if (selectedScan == null) {
			return;
		}

		if (selectedScan instanceof ScanObject) {
			final ScanObject runOb = (ScanObject) selectedScan;
			String location = runOb.getScanFile().getLocation().toString();
			try {
				if (runOb.isQexafs() && provider != null) {
					qexafsBean = QEXAFSParameters.createFromXML(location);
					workingEnergy.setMaximum(qexafsBean.getFinalEnergy());
					workingEnergy.setMinimum(qexafsBean.getInitialEnergy());
				} else if (runOb.isXes() && provider != null) {
					workingEnergy.setMaximum(runOb.getFinalEnergy(), "FinalEnergy", XesScanParameters.class);
					workingEnergy.setMinimum(runOb.getInitialEnergy(), "InitialEnergy", XesScanParameters.class);
				}

				else if (runOb.isXas()) {
					xasBean = XasScanParameters.createFromXML(location);
					workingEnergy.setMaximum(xasBean.getFinalEnergy());
					workingEnergy.setMinimum(xasBean.getInitialEnergy());
				}

				else if (runOb.isXanes()) {
					xanesBean = XanesScanParameters.createFromXML(location);
					workingEnergy.setMaximum(xanesBean.getFinalEnergy());
					workingEnergy.setMinimum(xanesBean.getRegions().get(0).getEnergy());
				}

				else if (!runOb.isMicroFocus() && provider != null) {
					workingEnergy.setMaximum(runOb.getFinalEnergy(), "FinalEnergy", XasScanParameters.class);
					workingEnergy.setMinimum(runOb.getInitialEnergy(), "InitialEnergy", XasScanParameters.class);
				} else if (!runOb.isMicroFocus() && provider != null) {
					workingEnergy.setMaximum(runOb.getFinalEnergy(), "FinalEnergy", XanesScanParameters.class);
					workingEnergy.setMinimum(runOb.getInitialEnergy(), "InitialEnergy", XanesScanParameters.class);
				}

			} catch (Exception ne) {
				// If you get this exception, please file details in GDA-3404
				logger.error("Cannot connect bounds for working energy (see GDA-3404)", ne);
			}
		}

		try {
			control.addBeanFieldValueListener("EdgeEnergy", new ValueAdapter("EdgeEnergyListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateWorkingEnergy(e);
				}

			});

		} catch (Exception ne) {
			logger.error("Cannot add EdgeEnergy listeners.", ne);
		}
	}

	private void updateWorkingEnergy(ValueEvent e) {
		if(!workingEnergy.isDisposed()){
			workingEnergy.off();
			workingEnergy.setValue(e.getValue());
			workingEnergy.on();
		}
	}


	protected Group getTabFolder() {
		if (tabFolder == null) {
			tabFolder = new Group(this, SWT.NONE);
			tabFolder.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
			final GridLayout gridLayout_1 = new GridLayout();
			gridLayout_1.numColumns = 1;
			tabFolder.setLayout(gridLayout_1);
		}
		return tabFolder;
	}

	protected void setWorkingFromEdge() {
		try {
			IEditorPart[] dirtyEditors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getDirtyEditors();



			if(dirtyEditors.length>0){
				MessageBox dialog =
						  new MessageBox(this.getShell(), SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
						dialog.setText("Save Editor");
						dialog.setMessage("The editors need to be saved to get the edge energy. Would you like to save?");
						int returnCode = dialog.open();
						if(returnCode==32){
							for(int i=0;i<dirtyEditors.length;i++){
								dirtyEditors[i].doSave(new NullProgressMonitor());
							}
						}
			}

			final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();

			if (ob != null) {
				final IScanParameters params = ob.getScanParameters();
				if (ob.isXanes()) {
					setWorkingEmergyUsingXanes((XanesScanParameters) params);
				} else if (ob.isXas()) {
					setWorkingEnergyUsingXas((XasScanParameters) params);
				} else if (ob.isXes()) {
					setWorkingEnergyUsingXes(ob, (XesScanParameters) params);
				} else if (ob.isMicroFocus()) {
					MicroFocusScanParameters mfParams = (MicroFocusScanParameters) params;
					workingEnergy.setValue(mfParams.getEnergy());
				} else {  //QEXAFS
					double init = ((QEXAFSParameters) params).getInitialEnergy();
					double fin = ((QEXAFSParameters) params).getFinalEnergy();
					double energy = init + ((fin - init) / 2.0);
					workingEnergy.setMinimum(init);
					workingEnergy.setMaximum(fin);
					workingEnergy.setValue(energy);
				}
			}
		} catch (Exception ne) {
			logger.error("Cannot get edge energy for element.", ne);
		}
	}

	private void setWorkingEnergyUsingXes(ScanObject ob, final XesScanParameters params) throws Exception {
		XesScanParameters xesparams = params;

		if (xesparams.getScanType() == XesScanParameters.SCAN_XES_FIXED_MONO) {
			double energy = xesparams.getMonoEnergy();
			workingEnergy.setMinimum(energy);
			workingEnergy.setMaximum(energy);
			workingEnergy.setValue(energy);
		} else if (xesparams.getScanType() == XesScanParameters.SCAN_XES_SCAN_MONO) {
			double init = xesparams.getMonoInitialEnergy();
			double fin = xesparams.getMonoFinalEnergy();
			String energy = String.valueOf(init + ((fin - init) / 2.0));
			workingEnergy.setMinimum(init);
			workingEnergy.setMaximum(fin);
			workingEnergy.setValue(energy);
		} else {
			String subscanFileName = xesparams.getScanFileName();
			IFile subscanFile = ob.getFolder().getFile(subscanFileName);
			XMLRichBean bean = XMLHelpers.getBean(subscanFile.getLocation().toFile());

			if (bean instanceof XasScanParameters) {
				setWorkingEnergyUsingXas((XasScanParameters) bean);
			} else if (bean instanceof XanesScanParameters) {
				setWorkingEmergyUsingXanes((XanesScanParameters) bean);
			}
		}
	}

	private void setWorkingEnergyUsingXas(final XasScanParameters params) {
		String element = params.getElement();
		String edge = params.getEdge();
		final Element ele = Element.getElement(element);
		workingEnergy.setMinimum(ele.getInitialEnergy(edge));
		workingEnergy.setMaximum(ele.getFinalEnergy(edge));
		if (Math.floor(ele.getEdgeEnergy(edge)) == ele.getEdgeEnergy(edge))
			workingEnergy.setValue((int) ele.getEdgeEnergy(edge));
		else
			workingEnergy.setValue(ele.getEdgeEnergy(edge));
	}

	private void setWorkingEmergyUsingXanes(final XanesScanParameters params) {
		String element = params.getElement();
		String edge = params.getEdge();
		final Element ele = Element.getElement(element);
		workingEnergy.setMinimum(params.getRegions().get(0).getEnergy());
		workingEnergy.setMaximum(ele.getFinalEnergy(edge));
		if (Math.floor(ele.getEdgeEnergy(edge)) == ele.getEdgeEnergy(edge))
			workingEnergy.setValue((int) ele.getEdgeEnergy(edge));
		else
			workingEnergy.setValue(ele.getEdgeEnergy(edge));
	}

	public ScaleBox getWorkingEnergy() {
		return workingEnergy;
	}

	public VerticalListEditor getDiodeParameters() {
		return diodeParameters;
	}

	@Override
	public void dispose() {
		if (workingEnergyBtn != null && !workingEnergyBtn.isDisposed()) {
			workingEnergyBtn.removeSelectionListener(workingEnergyListener);
		}
		super.dispose();
	}
}
