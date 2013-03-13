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

import gda.util.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.ElementPosition;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.i20.I20SampleParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.util.GainBean;
import uk.ac.gda.exafs.util.GainCalculation;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

import com.swtdesigner.SWTResourceManager;

/**
 *
 */
public class GainDialog extends Dialog {

	private static Logger logger = LoggerFactory.getLogger(GainDialog.class);

	private ScaleBox referenceEdgeEnergy;
	private ScaleBox sampleEdgeEnergy;
	private ScaleBox finalEnergy;
	private ScaleBox tolerance;

	private Object referenceEdgeEnergyValue = 0d;
	private Object sampleEdgeEnergyValue = 0d;
	private Object finalEnergyValue = 0d;

	private IProgressService progressService;

	/**
	 * Create the dialog
	 * 
	 * @param parentShell
	 */
	public GainDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());

		final Label theCalculationLooksLabel = new Label(container, SWT.WRAP);
		final GridData gd_theCalculationLooksLabel = new GridData(SWT.FILL, SWT.FILL, true, false);
		theCalculationLooksLabel.setLayoutData(gd_theCalculationLooksLabel);

		final Composite main = new Composite(container, SWT.BORDER);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		main.setLayout(new GridLayout());

		final Composite top = new Composite(main, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		top.setLayout(gridLayout);
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Label calculationExtentLabel = new Label(top, SWT.NONE);
		calculationExtentLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		calculationExtentLabel.setText("Calculation Extent");

		final ComboWrapper comboWrapper = new ComboWrapper(top, SWT.READ_ONLY);
		comboWrapper.setItems(new String[] { "All ion chambers" });
		comboWrapper.select(0);
		comboWrapper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final ExpandableComposite advancedExpandableComposite = new ExpandableComposite(main, SWT.NONE);
		advancedExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		advancedExpandableComposite.setText("Advanced");

		final Composite advanced = new Composite(advancedExpandableComposite, SWT.NONE);
		advanced.setLayout(gridLayout);
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Link finalEnergyLabel = new Link(advanced, SWT.NONE);
		finalEnergyLabel
				.setToolTipText("Click to take energy from scan parameters. This also happens automatically when the gain form is opened.");
		finalEnergyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		finalEnergyLabel.setText("<a>E1</a>");
		finalEnergyLabel.setToolTipText("Final energy");
		finalEnergyLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getFinalEnergyValue();
			}
		});

		this.finalEnergy = new ScaleBox(advanced, SWT.NONE);
		finalEnergy.setMaximum(120000.0);
		finalEnergy.setUnit("eV");
		final GridData gd_finalEnergy = new GridData(SWT.FILL, SWT.CENTER, true, false);
		finalEnergy.setLayoutData(gd_finalEnergy);
		finalEnergy.setValue(finalEnergyValue);

		final Link sampleEdgeEnergyLabel = new Link(advanced, SWT.NONE);
		sampleEdgeEnergyLabel
				.setToolTipText("Click to take energy from scan parameters.  This also happens automatically when the gain form is opened.");
		final GridData gd_sampleEdgeEnergyLabel = new GridData(SWT.FILL, SWT.CENTER, false, false);
		sampleEdgeEnergyLabel.setLayoutData(gd_sampleEdgeEnergyLabel);
		sampleEdgeEnergyLabel.setText("<a>E3</a>");
		sampleEdgeEnergyLabel.setToolTipText("This is 20ev below the sample edge energy.");
		sampleEdgeEnergyLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getSampleEdgeValue();
			}
		});

		this.sampleEdgeEnergy = new ScaleBox(advanced, SWT.NONE);
		final GridData gd_sampleEdgeEnergy = new GridData(SWT.FILL, SWT.CENTER, true, false);
		sampleEdgeEnergy.setLayoutData(gd_sampleEdgeEnergy);
		sampleEdgeEnergy.setUnit("eV");
		sampleEdgeEnergy.setMaximum(finalEnergy);
		sampleEdgeEnergy.setValue(sampleEdgeEnergyValue);

		final Link referenceEdgeEnergyLabel = new Link(advanced, SWT.NONE);
		referenceEdgeEnergyLabel
				.setToolTipText("Click to take energy from sample parameters. This also happens automatically when the gain form is opened.");
		final GridData gd_referenceEdgeEnergyLabel = new GridData(SWT.FILL, SWT.CENTER, true, false);
		referenceEdgeEnergyLabel.setLayoutData(gd_referenceEdgeEnergyLabel);
		referenceEdgeEnergyLabel.setText("<a>E3</a>");
		referenceEdgeEnergyLabel.setToolTipText("This is 20ev below the reference edge energy.");
		referenceEdgeEnergyLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getReferenceEdgeSample();
			}
		});

		this.referenceEdgeEnergy = new ScaleBox(advanced, SWT.NONE);
		final GridData gd_referenceEdgeEnergy = new GridData(SWT.FILL, SWT.CENTER, true, false);
		referenceEdgeEnergy.setLayoutData(gd_referenceEdgeEnergy);
		referenceEdgeEnergy.setUnit("eV");
		referenceEdgeEnergy.setMaximum(finalEnergy);
		referenceEdgeEnergy.setValue(referenceEdgeEnergyValue);

		final Label toleranceLabel = new Label(advanced, SWT.NONE);
		toleranceLabel
				.setToolTipText("This is how close to the maximum intensity that the algorithm should find the gain setting for.");
		toleranceLabel.setText("Tolerance");

		this.tolerance = new ScaleBox(advanced, SWT.NONE);
		tolerance.setNumericValue(90);
		tolerance.setUnit("%");
		tolerance.setMinimum(1.0);
		tolerance.setMaximum(100.0);
		tolerance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		tolerance.addValueListener(new ValueAdapter("Label updater") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				setCalculationLabelText(theCalculationLooksLabel);
			}
		});
		tolerance.on();

		advancedExpandableComposite.setClient(advanced);
		advancedExpandableComposite.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				container.layout();
			}
		});

		final Link resultsLabel = new Link(container, SWT.WRAP);
		resultsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		resultsLabel.setText("");

		setCalculationLabelText(theCalculationLooksLabel);
		return container;
	}

	private void setCalculationLabelText(Label label) {
		final double val = tolerance.getNumericValue();
		if (Double.isNaN(val)) {
			label.setText("Error, the tolerance is blank but should be between 1 and 100");
			if (getButton(IDialogConstants.OK_ID) != null)
				getButton(IDialogConstants.OK_ID).setEnabled(false);
		} else {
			label.setText("The calculation looks at the intensity for each ion chamber based on the scan parameters and attempts to adjust gain until intensity is ~"
					+ val + "% of the detector maximum.");
			if (getButton(IDialogConstants.OK_ID) != null)
				getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
	}

	/**
	 * Create contents of the button bar
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		final Button button = createButton(parent, IDialogConstants.OK_ID, "Calculate", true);
		button.setImage(SWTResourceManager.getImage(GainDialog.class, "/icons/calculator_edit.png"));

		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Calculate Gain");
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			okPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		} else if (IDialogConstants.CLOSE_ID == buttonId) {
			close();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {

		if (progressService == null) {
			progressService = (IProgressService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(IProgressService.class);
		}
		if (progressService == null)
			return;

		try {
			// Setup data
			final GainBean bean = new GainBean() {
				@Override
				public void updateMessage(String line) {
					// resultsLabel.setText(buffer.toString());
				}
			};
			BeanUI.uiToBean(this, bean);
			bean.setCollectionTime(1000L);
			bean.setTolerance(tolerance.getNumericValue());
			bean.setLogger(logger);

			// Name of scannable
			IExperimentEditorManager man = ExperimentFactory.getExperimentEditorManager();
			final IScanParameters scanParams = ((ScanObject) man.getSelectedScan()).getScanParameters();
			final String name = scanParams.getScannableName();
			bean.setScannableName(name);

			// Name of amplifiers
			final List<IonChamberParameters> ionChambers;
			final String type = (String) BeanUI.getBeanField("experimentType", DetectorParameters.class).getValue();
			if (type.equalsIgnoreCase("Transmission")) {
				ionChambers = ((TransmissionParameters) BeanUI.getBeanField("transmissionParameters",
						DetectorParameters.class).getValue()).getIonChamberParameters();
			} else if (type.equalsIgnoreCase("fluorescence")) {
				ionChambers = ((FluorescenceParameters) BeanUI.getBeanField("fluorescenceParameters",
						DetectorParameters.class).getValue()).getIonChamberParameters();
			} else {
				throw new Exception("Cannot deal with experimentType = '" + type + "'");
			}

			progressService.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					monitor.beginTask("Calculate Gain", 100);

					bean.setMonitor(monitor);

					// TODO Run this in event service
					try {
						monitor.worked(10);

						// Get the intensity of I0
						bean.setIonChamber(ionChambers.get(0));
						bean.setEnergy(bean.getFinalEnergy());

						final double i0_orig = GainCalculation.getIntensity(bean);
						System.out.println("I0 = " + i0_orig);

						final String i0_gain = GainCalculation.getSuggestedGain(bean);
						System.out.println(i0_gain);

						// bean.setIonChamber(ionChambers.get(1));
						// final String it_gain = GainCalculation.getSuggestedGain(bean);
						// System.out.println(it_gain);
						//
						//
						// bean.setIonChamber(ionChambers.get(2));
						// final String iref_gain = GainCalculation.getSuggestedGain(bean);
						// System.out.println(iref_gain);

					} catch (Exception ne) {
						logger.error("Cannot calculate gain automatically", ne);
					} finally {
						monitor.done();
					}

				}
			});
		} catch (Exception e) {
			logger.error("Cannot calculate gain.", e);
		}
	}

	/**
	 * 
	 */
	public void getAllValues() {
		getFinalEnergyValue();
		getSampleEdgeValue();
		getReferenceEdgeSample();
	}

	@SuppressWarnings("unchecked")
	private void getReferenceEdgeSample() {

		try {
			final int pos = (Integer) ExperimentFactory.getExperimentEditorManager().getValueFromUIOrBean(
					"sampleWheelPosition", I20SampleParameters.class);
			final List<ElementPosition> elePos = (List<ElementPosition>) ExperimentFactory.getExperimentEditorManager()
					.getValueFromUIOrBean("elementPositions", I20SampleParameters.class);
			for (ElementPosition elementPosition : elePos) {
				if (elementPosition.getWheelPosition() == pos) {
					final Element refElement = Element.getElement(elementPosition.getPrincipleElement());
					if (refElement != null) {
						this.referenceEdgeEnergyValue = (refElement.getEdgeEnergy("K")) - 20d;
						if (referenceEdgeEnergy != null)
							this.referenceEdgeEnergy.setValue(referenceEdgeEnergyValue);
					}
				}
			}
		} catch (Exception ne) {
			logger.error("Cannot get reference edge energy", ne);
		}
	}

	@SuppressWarnings("unchecked")
	private void getSampleEdgeValue() {
		try {
			final IFieldWidget ui = BeanUI.getBeanField("edgeEnergy", XasScanParameters.class,
					XanesScanParameters.class);
			if (ui != null) {
				this.sampleEdgeEnergyValue = ui.getValue();
			} else {
				try {
					final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
					if (ob != null) {
						final String element, edge;
						final Object params = ob.getScanParameters();
						if (params instanceof XanesScanParameters) {
							element = ((XanesScanParameters) params).getElement();
							edge = ((XanesScanParameters) params).getEdge();
						} else {
							element = ((XasScanParameters) params).getElement();
							edge = ((XasScanParameters) params).getEdge();
						}
						final Element ele = Element.getElement(element);
						this.sampleEdgeEnergyValue = new Double(ele.getEdgeEnergy(edge)) - 20d;
					}
				} catch (Exception ne) {
					logger.error("Cannot get edge energy for element.", ne);
				}

			}
			if (sampleEdgeEnergy != null)
				this.sampleEdgeEnergy.setValue(sampleEdgeEnergyValue);

		} catch (Exception ne) {
			logger.error("Cannot get sample edge energy", ne);
		}
	}

	@SuppressWarnings("unchecked")
	private void getFinalEnergyValue() {
		try {
			this.finalEnergyValue = ExperimentFactory.getExperimentEditorManager().getValueFromUIOrBean("finalEnergy",
					XasScanParameters.class.asSubclass(IRichBean.class),
					XanesScanParameters.class.asSubclass(IRichBean.class));
			if (finalEnergy != null)
				this.finalEnergy.setValue(finalEnergyValue);
		} catch (Exception ne) {
			logger.error("Cannot get final energy", ne);
		}
	}

	/**
	 * @return Returns the progressService.
	 */
	public IProgressService getProgressService() {
		return progressService;
	}

	/**
	 * @param progressService
	 *            The progressService to set.
	 */
	public void setProgressService(IProgressService progressService) {
		this.progressService = progressService;
	}

	/**
	 * @return Returns the referenceEdgeEnergy.
	 */
	public ScaleBox getReferenceEdgeEnergy() {
		return referenceEdgeEnergy;
	}

	/**
	 * @return Returns the sampleEdgeEnergy.
	 */
	public ScaleBox getSampleEdgeEnergy() {
		return sampleEdgeEnergy;
	}

	/**
	 * @return Returns the finalEnergy.
	 */
	public ScaleBox getFinalEnergy() {
		return finalEnergy;
	}

	/**
	 * @return Returns the tolerance.
	 */
	public ScaleBox getTolerance() {
		return tolerance;
	}
}
