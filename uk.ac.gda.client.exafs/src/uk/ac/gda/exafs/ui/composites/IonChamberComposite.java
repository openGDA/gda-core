/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.binding.IBeanService;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldComposite.NOTIFY_TYPE;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.selector.ListEditor;
import org.eclipse.richbeans.widgets.selector.ListEditorUI;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.CurrentAmplifier;
import gda.exafs.mucal.PressureBean;
import gda.exafs.mucal.PressureCalculation;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.components.wrappers.FindableNameWrapper;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.dialogs.GainWizard;
import uk.ac.gda.exafs.ui.ionchambers.GasFill;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.exafs.util.GainCalculation;

public class IonChamberComposite extends Composite implements ListEditorUI {
	private final Logger logger = LoggerFactory.getLogger(IonChamberComposite.class);
	private LabelWrapper deviceName;
	private TextWrapper name;
	private TextWrapper currentAmplifierName;
	private LabelWrapper channel;
	private ComboWrapper gain;
	private ComboWrapper offset;
	private ScaleBox percentAbsorption;
	private ComboWrapper gasType;
	private Group gasPropertiesGroup;
	private LabelWrapper pressure;
	private ScaleBox totalPressure;
	private ScaleBox ionChamberLength;
	private ScaleBox gas_fill1_period_box;
	private ScaleBox gas_fill2_period_box;
	private BooleanWrapper autoFillGas;
	private BooleanWrapper flush;
	private CLabel errorMessage;
	private ExpandableComposite advancedExpandableComposite;
	private ExpansionAdapter expansionListener;
	private Link refreshLink;
	private SelectionAdapter refreshListener;
	private BooleanWrapper changeSensitivity;
	private String flushString = "False";
	private VerticalListEditor provider;
	private Button fillGasButton;
	private DetectorParameters detParams;
	private IonChamberParameters ionParams;
	private boolean useGasProperties = true;
	private Label gainLabel;
	private Label offsetLabel;

	public IonChamberComposite(Composite parent, int style, final VerticalListEditor provider,
 final DetectorParameters abean, IBeanController control) {
		super(parent, style);
		setLayout(new GridLayout());

		this.provider = provider;
		detParams = abean;

		final Composite main = new Composite(this, SWT.NONE);
		GridData gd_main = new GridData(SWT.FILL, SWT.CENTER, true, false);
		main.setLayoutData(gd_main);
		main.setLayout(new GridLayout(2, true));
		try {
			useGasProperties = detParams.getIonChambers().get(0).isUseGasProperties();
		} catch (Exception e) {
			logger.warn("Unable to get the gas properties");
			if (ExafsActivator.getDefault().getPreferenceStore()
					.getBoolean(ExafsPreferenceConstants.NEVER_DISPLAY_GAS_PROPERTIES)) {
				useGasProperties = false;
			}
		}
		createLeft(main, control);
		createRight(main);
	}

	private void createLeft(final Composite main, IBeanController control) {
		final Composite left = new Composite(main, SWT.NONE);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createLeftSensitivityProperties(left, control);
		if (useGasProperties)
			createLeftGasProperties(left);
	}

	private void createLeftGasProperties(final Composite left) {

		this.gasPropertiesGroup = new Group(left, SWT.NONE);
		gasPropertiesGroup.setLayout(new GridLayout(2, false));
		gasPropertiesGroup.setText("Gas Properties");
		gasPropertiesGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		autoFillGas = new BooleanWrapper(gasPropertiesGroup, SWT.NONE);
		autoFillGas.setText("Fill before starting data collection");
		autoFillGas
				.setToolTipText("If selected then the gas filling system will be operated during the data collection scripts.\nIf unticked then it will not be operated and you should press the 'Fill Gas' button on the GUI to change the gas.");

		flush = new BooleanWrapper(gasPropertiesGroup, SWT.NONE);
		flush.setText("Flush ion chamber before fill");
		flush.setToolTipText("If selected then the ion chamber will be flushed before a filled");

		final Label gasTypeLabel = new Label(gasPropertiesGroup, SWT.NONE);
		gasTypeLabel.setText("Gas Type");

		gasType = new ComboWrapper(gasPropertiesGroup, SWT.READ_ONLY);
		gasType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final Map<String, Object> gasMap = new LinkedHashMap<String, Object>();
		gasMap.put("He", "He");
		gasMap.put("He + N\u2082", "N");
		gasMap.put("He + Ar", "Ar");
		gasMap.put("He + Kr", "Kr");
		gasType.setItems(gasMap);
		gasType.addValueListener(new ValueAdapter("gasTypeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				calculatePressure();
			}
		});

		final Label percentAdsorptionLabel = new Label(gasPropertiesGroup, SWT.NONE);
		percentAdsorptionLabel.setText("Percent Absorption");

		percentAbsorption = new ScaleBox(gasPropertiesGroup, SWT.NONE);
		percentAbsorption.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		percentAbsorption.setMaximum(100);
		percentAbsorption.setUnit("%");
		percentAbsorption.addValueListener(new ValueAdapter("percentAbsorptionListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				calculatePressure();
			}
		});

		final Label pressureLabel = new Label(gasPropertiesGroup, SWT.NONE);
		pressureLabel.setText("Pressure");

		final Composite composite = new Composite(gasPropertiesGroup, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 3;
		composite.setLayout(gridLayout_1);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		this.pressure = new LabelWrapper(composite, SWT.NONE);
		pressure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pressure.setUnit("bar");
		pressure.setNotifyType(NOTIFY_TYPE.VALUE_CHANGED);
		pressure.setDecimalPlaces(6);


		if (!ExafsActivator.getDefault().getPreferenceStore()
				.getBoolean(ExafsPreferenceConstants.HIDE_GAS_FILL_CONTROLS)) {
			fillGasButton = new Button(composite, SWT.NONE);
			fillGasButton.setToolTipText("Click to fill the gas into the ion chamber.");
			fillGasButton.setText("Fill Gas");

			fillGasButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					//  Create new parameters object using currently set parameters from gui
					IonChamberParameters chamberParams = new IonChamberParameters();
					chamberParams.setName( (String) name.getValue() );
					chamberParams.setPressure( Double.parseDouble(pressure.getValue()) );
					chamberParams.setGas_fill1_period_box( Double.parseDouble(gas_fill1_period_box.getValue().toString()) );
					chamberParams.setTotalPressure( Double.parseDouble(totalPressure.getValue().toString()) );
					chamberParams.setGas_fill2_period_box( Double.parseDouble(gas_fill2_period_box.getValue().toString()) );
					chamberParams.setGasType( gasType.getValue().toString() );
					chamberParams.setFlush( flush.getValue() );
					// Run gas fill
					GasFill.runGasFill( chamberParams, fillGasButton);
				}
			} );

		}
	}

	@SuppressWarnings("unused")
	private void createLeftSensitivityProperties(final Composite left, final IBeanController control) {
		final Composite gainProperties = new Composite(left, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gainProperties.setLayout(gridLayout);
		gainProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite nameComposite = new Composite(left, SWT.NONE);
		final Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Name");
		nameLabel.setVisible(false);
		name = new TextWrapper(nameComposite, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		name.setVisible(false);
		nameComposite.setSize(0, 0);

		changeSensitivity = new BooleanWrapper(gainProperties, SWT.NONE);
		changeSensitivity.setText("Change the sensitivity during data collection");
		changeSensitivity
				.setToolTipText("Select for the amplifier sensitivity to be adjust during data collection.\nIf unselected then the current sensitivity will be left unchanged.");
		changeSensitivity.setValue(false);

		new Label(gainProperties, SWT.NONE);

		gainLabel = new Label(gainProperties, SWT.NONE);
		gainLabel.setText("Sensitivity");
		gainLabel
				.setToolTipText("The gain setting on the amplifier.\n(This cannot be linked to get the gain as the Stanford Amplifier does not have a get for the gain, only a set.)");

		gain = new ComboWrapper(gainProperties, SWT.READ_ONLY);
		gain.setLayout(new GridLayout(2, false));
		gain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		List<String> notches = null;
		if (!LocalProperties.get("gda.factory.factoryName").equals("I18")) {
			notches = GainCalculation.getGainNotches();
		} else {
			notches = new ArrayList<String>(8);
			notches.add("10^3 V/A");
			notches.add("10^4 V/A");
			notches.add("10^5 V/A");
			notches.add("10^6 V/A");
			notches.add("10^7 V/A");
			notches.add("10^8 V/A");
			notches.add("10^9 V/A");
			notches.add("10^10 V/A");
		}

		gain.setItems(notches.toArray(new String[notches.size()]));
		gain.addButtonListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WizardDialog dialog = new WizardDialog(getShell(), new GainWizard(control));
				dialog.setPageSize(new Point(780, 550));
				dialog.create();
				dialog.open();
			}
		});

		if (!LocalProperties.get("gda.factory.factoryName").equals("I18")) {
			offsetLabel = new Label(gainProperties, SWT.NONE);
			offsetLabel.setText("Offset");
			offsetLabel
					.setToolTipText("The offset setting on the amplifier.\n(This cannot be linked to get the offset as the Stanford Amplifier does not have an offset for the gain, only a set.)");

			offset = new ComboWrapper(gainProperties, SWT.READ_ONLY);
			offset.setLayout(new GridLayout(2, false));
			offset.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			final List<String> offsetNotches = GainCalculation.getOffsetNotches();
			offset.setItems(offsetNotches.toArray(new String[offsetNotches.size()]));
			offset.addButtonListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					WizardDialog dialog = new WizardDialog(getShell(), new GainWizard(control));
					dialog.setPageSize(new Point(780, 550));
					dialog.create();
					dialog.open();
				}
			});
		}
	}

	private void createRight(final Composite main) {
		final Composite right;
		right = new Composite(main, SWT.NONE);
		right.setLayout(new FillLayout());
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		this.advancedExpandableComposite = new ExpandableComposite(right, SWT.NONE);
		advancedExpandableComposite.setText("Advanced");

		final Composite advanced = new Composite(advancedExpandableComposite, SWT.NONE);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		advanced.setLayout(gridLayout_2);

		final Label deviceNameLabel = new Label(advanced, SWT.NONE);
		deviceNameLabel.setText("Device Name");

		deviceName = new LabelWrapper(advanced, SWT.BORDER);
		deviceName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		deviceName.setTextType(LabelWrapper.TEXT_TYPE.PLAIN_TEXT);

		final Label channelLabel = new Label(advanced, SWT.NONE);
		channelLabel.setText("Channel");

		channel = new LabelWrapper(advanced, SWT.BORDER);
		channel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		channel.setUnit(null);

		final Label currentAmplifierNameLabel = new Label(advanced, SWT.NONE);
		currentAmplifierNameLabel.setText("Current Amplifier Name");

		currentAmplifierName = new FindableNameWrapper(advanced, SWT.BORDER, CurrentAmplifier.class);
		currentAmplifierName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (useGasProperties) {
			final Label totalPressureLabel = new Label(advanced, SWT.NONE);
			totalPressureLabel.setText("Total Pressure");

			this.totalPressure = new ScaleBox(advanced, SWT.NONE);
			totalPressure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			totalPressure.setUnit("bar");
			totalPressure.addValueListener(new ValueAdapter("totalPressureListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					calculatePressure();
				}
			});

			final Label ionChamberLengthLabel = new Label(advanced, SWT.NONE);
			ionChamberLengthLabel.setText("Ion Chamber Length");

			this.ionChamberLength = new ScaleBox(advanced, SWT.NONE);
			ionChamberLength.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			ionChamberLength.setUnit("cm");
			ionChamberLength.setMinimum(1);
			ionChamberLength.setMaximum(1000);
			ionChamberLength.addValueListener(new ValueAdapter("ionChamberLengthListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					calculatePressure();
				}
			});

			final Label gas_fill1_period_lbl = new Label(advanced, SWT.NONE);
			gas_fill1_period_lbl.setText("Gas fill 1 period");

			gas_fill1_period_box = new ScaleBox(advanced, SWT.NONE);
			gas_fill1_period_box.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			gas_fill1_period_box.setUnit("seconds");

			final Label gas_fill2_period_lbl = new Label(advanced, SWT.NONE);
			gas_fill2_period_lbl.setText("Gas fill 2 period");

			gas_fill2_period_box = new ScaleBox(advanced, SWT.NONE);
			gas_fill2_period_box.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			gas_fill2_period_box.setUnit("seconds");
			if (ExafsActivator.getDefault().getPreferenceStore()
					.getBoolean(ExafsPreferenceConstants.DISPLAY_GAS_FILL_PERIOD)) {
				gas_fill1_period_lbl.setVisible(false);
				gas_fill1_period_lbl.setSize(0, 0);
				gas_fill1_period_box.setVisible(false);
				gas_fill1_period_box.setSize(0, 0);
				gas_fill2_period_lbl.setVisible(false);
				gas_fill2_period_lbl.setSize(0, 0);
				gas_fill2_period_box.setVisible(false);
				gas_fill2_period_box.setSize(0, 0);
			}
		}
		advancedExpandableComposite.setClient(advanced);
		this.expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.layoutFull(getParent());
			}
		};
		advancedExpandableComposite.addExpansionListener(expansionListener);
	}

	@Override
	public void dispose() {
		advancedExpandableComposite.removeExpansionListener(expansionListener);
		refreshLink.removeSelectionListener(refreshListener);
		super.dispose();
	}

	int findFilterIndex(String name, ComboWrapper combo) {
		for (int i = 0; i < combo.getItems().length; i++)
			if (combo.getItems()[i].equals(name))
				return i;
		return -1;
	}

	void calculateDefaultGasType(double workingE) {
		// i) I0: If working energy < 7 keV default to use N2 fill gas
		// ii) I0: If 7 <= Working energy <26 keV default to use Ar
		// iii) I0: If working energy >=26keV default to use Kr
		// iv) It/Iref: If working energy < 16keV use Ar
		// v) It/Iref: if working energy >= 16keV use Kr

		int N = 1;
		int Ar = 2;
		int Kr = 3;

		String chamber = ionParams.getName();
		List<IonChamberParameters> chambers = null;

		if (detParams.getExperimentType().toString().equals("Transmission")) {
			chambers = detParams.getTransmissionParameters().getIonChamberParameters();
		} else if (detParams.getExperimentType().toString().equals("Fluorescence")) {
			chambers = detParams.getFluorescenceParameters().getIonChamberParameters();
		} else if (detParams.getExperimentType().toString().equals("XES")) {
			chambers = detParams.getXesParameters().getIonChamberParameters();
		} else {
			return;
		}

		if (workingE < 7000) {
			chambers.get(0).setGasType("N");
		} else if (workingE >= 7000 && workingE < 26000) {
			chambers.get(0).setGasType("Ar");
		} else if (workingE >= 26000) {
			chambers.get(0).setGasType("Kr");
		}
		if (workingE < 16000) {
			chambers.get(1).setGasType("Ar");
			chambers.get(2).setGasType("Ar");
		} else if (workingE >= 16000) {
			chambers.get(1).setGasType("Kr");
			chambers.get(2).setGasType("Kr");
		}

		if (chamber.equals("I0") || chamber.equals("I1")) {
			this.percentAbsorption.setValue(15);
			if (workingE < 7000) {
				gasType.select(N);
			} else if (workingE >= 7000 && workingE < 26000) {
				gasType.select(Ar);
			} else if (workingE >= 26000) {
				gasType.select(Kr);
			}
		} else if (chamber.equals("It") || chamber.equals("Iref")) {
			if (workingE < 16000) {
				gasType.select(Ar);
			} else if (workingE >= 16000) {
				gasType.select(Kr);
			}
		}
	}

	/**
	 * We do not calculate pressure in this class. Instead we prepare the data and send it to the PressureCalculation
	 * class.
	 */
	public void calculatePressure() {

		if(this.isVisible()){

			if (!isUseGasProperties() || workingEnergy.getValue() == null)
				return;
			// We run the pressure code in a separate progressible task, just in case.
			final IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(
					IProgressService.class);
			try {

				// Get the working energy before starting the task.
				final double workingE = (Double) workingEnergy.getValue();

				service.run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						monitor.beginTask("Calculate Pressure", 100);

						try {
							final IonChamberParameters bean = (IonChamberParameters) provider.getInstance();

							getDisplay().syncExec(new Runnable() {

								@Override
								public void run() {
									try {
										final IBeanService service = ExafsActivator.getService(IBeanService.class);
										final IBeanController control = service.createController(IonChamberComposite.this, bean);
										control.uiToBean();
									} catch (Exception e) {

										e.printStackTrace();
									}
								}
							});

							bean.setWorkingEnergy(workingE);

							monitor.worked(10);
							final PressureBean ans = PressureCalculation.getPressure(bean);
							monitor.worked(80);

							getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									final double pressureVal = ans.getPressure();

									String experimentType = detParams.getExperimentType();

									int index = provider.getSelectedIndex();

									List<IonChamberParameters> ionParamsList = null;

									if (experimentType.equals("Transmission")) {
										TransmissionParameters params = detParams.getTransmissionParameters();
										ionParamsList = params.getIonChamberParameters();
									} else if (experimentType.equals("Fluorescence")) {
										FluorescenceParameters params = detParams.getFluorescenceParameters();
										ionParamsList = params.getIonChamberParameters();
									} else if (experimentType.equals("XES")) {
										FluorescenceParameters params = detParams.getXesParameters();
										ionParamsList = params.getIonChamberParameters();
									}

									if (ionParamsList != null) {
										ionParams = ionParamsList.get(index);
									}

									boolean originalAutoFillGas = ionParams.getAutoFillGas();
									boolean originalFlush = ionParams.getFlush();

									if (pressureVal > totalPressure.getNumericValue() || pressureVal < 0.003) {
										if (fillGasButton != null)
											fillGasButton.setEnabled(false);
										autoFillGas.setEnabled(false);
										flush.setEnabled(false);
										autoFillGas.setValue(false);
										flush.setValue(false);
										pressure.setLabelColor(new Color(null, 255, 0, 0));
									} else {
										if (fillGasButton != null)
											fillGasButton.setEnabled(true);
										autoFillGas.setEnabled(true);
										flush.setEnabled(true);
										autoFillGas.setValue(originalAutoFillGas);
										flush.setEnabled(true);
										autoFillGas.setValue(originalAutoFillGas);
										flush.setValue(originalFlush);
										pressure.setLabelColor(new Color(null, 0, 0, 0));
									}

									if (!Double.isNaN(pressureVal) && !Double.isInfinite(pressureVal)) {
										getPressure().setValue(pressureVal);
									}

									GridUtils.layoutFull(IonChamberComposite.this);
								}
							});
							monitor.worked(10);

						} catch (NullPointerException ne) {
							// Can legally happen when user types in no value for a box.
							logger.debug("Cannot run mucal code to find gas absorption.", ne);
						} catch (Throwable ne) {
							logger.error("Cannot run mucal code to find gas absorption.", ne);
						} finally {
							monitor.done();
						}
					}
				});
			} catch (Exception e) {
				logger.error("Cannot run mucal code to find gas absorption.", e);
			}
		}

	}

	public ScaleBox getGas_fill1_period_box() {
		return gas_fill1_period_box;
	}

	public ScaleBox getGas_fill2_period_box() {
		return gas_fill2_period_box;
	}

	public ScaleBox getIonChamberLength() {
		return ionChamberLength;
	}

	public ScaleBox getTotalPressure() {
		return totalPressure;
	}

	public LabelWrapper getPressure() {
		return pressure;
	}

	public ComboWrapper getGasType() {
		return gasType;
	}

	public ScaleBox getPercentAbsorption() {
		return percentAbsorption;
	}

	public ComboWrapper getGain() {
		return gain;
	}

	public ComboWrapper getOffset() {
		return offset;
	}

	public LabelWrapper getChannel() {
		return channel;
	}

	public TextWrapper getCurrentAmplifierName() {
		return currentAmplifierName;
	}

	// suppressing that getName is in the superclass hierarchy as a private
	@SuppressWarnings("all")
	public TextWrapper getName() {
		return name;
	}

	public LabelWrapper getDeviceName() {
		return deviceName;
	}

	public BooleanWrapper getAutoFillGas() {
		return autoFillGas;
	}

	public BooleanWrapper getFlush() {
		return flush;
	}

	public BooleanWrapper getChangeSensitivity() {
		return changeSensitivity;
	}

	@Override
	public boolean isAddAllowed(ListEditor listEditor) {
		int index = listEditor.getSelectedIndex();
		if (index == 0 || index == 1)
			return false; // Can add after 2
		return true;
	}

	@Override
	public boolean isDeleteAllowed(ListEditor listEditor) {
		int index = listEditor.getSelectedIndex();
		if (index == 0 || index == 1 || index == 2)
			return false;
		return true;
	}

	@Override
	public boolean isReorderAllowed(ListEditor listEditor) {
		int index = listEditor.getSelectedIndex();
		if (index == 0 || index == 1 || index == 2)
			return false;
		return true;
	}

	@Override
	public void notifySelected(ListEditor listEditor) {
		int index = listEditor.getSelectedIndex();
		if (index == 0 || index == 1 || index == 2) {
			if (useGasProperties)
				gasPropertiesGroup.setVisible(true);
		} else {
			if (useGasProperties)
				gasPropertiesGroup.setVisible(false);
			errorMessage.setVisible(false);
			GridUtils.layoutFull(this);
		}
	}

	// NOT UI Box on this composite.
	private ScaleBox workingEnergy;

	/**
	 * Sets the active working energy box used for pressure calculation.
	 *
	 * @param workingEnergy
	 */
	public void setWorkingEnergy(ScaleBox workingEnergy) {
		this.workingEnergy = workingEnergy;
	}

	public boolean _testIsPressureOk() {
		return "".equals(errorMessage.getText().trim());
	}

	public boolean isUseGasProperties() {
		return useGasProperties;
	}

	public void setGasType(String gasTypeVal) {
		this.gasType.select(findFilterIndex(gasTypeVal, gasType));
	}

	public void setExperimentType(String type) {
		detParams.setExperimentType(type);
	}
}
