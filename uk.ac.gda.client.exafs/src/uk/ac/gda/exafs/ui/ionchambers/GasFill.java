/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.ionchambers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.gda.beans.exafs.IonChamberParameters;

/**
 * Ion chamber gas filling functions
 */
public class GasFill {

	private static final Logger logger = LoggerFactory.getLogger(GasFill.class);

	/**
	 * Map to convert from ionchamber name to name of the gas injector scannable
	 */
	private static final Map<String, String> injectorNameMap;
	static {
		injectorNameMap = new HashMap<>();
		injectorNameMap.put("I0", "ionc1_gas_injector");
		injectorNameMap.put("It", "ionc2_gas_injector");
		injectorNameMap.put("Iref", "ionc3_gas_injector");
	}

	/**
	 * Run gas fill using specified ion chamber parameters.
	 *
	 * @param params {IonChamberParameters} - object specifying ion chamber be used, fill-purge parameters
	 */
	public static void runGasFill(IonChamberParameters params) {
		runGasFill(params, null);
	}

	/**
	 * Run gas fill using specified ion chamber parameters.
	 * This code is refactored from listener of 'fill gas' button in IonChamberComposite editor view;
	 *
	 * @param params {IonChamberParameters} - object specifying ion chamber be used, fill-purge parameters
	 * @param gasFillButton {Button} - button in gui used to start fill sequence (so it can be disabled whilst fill takes place)
	 */
	public static void runGasFill(IonChamberParameters params, final Button gasFillButton ) {
		runGasFill(Arrays.asList(params), Arrays.asList(gasFillButton));
	}

	public static void runGasFill(List<IonChamberParameters> params, final List<Button> gasFillButtons ) {
		String message = params.stream().map(GasFill::checkInjectorScannable).collect(Collectors.joining("\n")).trim();
		if (!message.isEmpty()) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Could not run gas fill",
					"Unable to run the gas fill - "+message);
			return;
		}

		//Make make Map from injector scannable to the ionchamber params
		Map<Scannable, IonChamberParameters> injectorParamMap = new HashMap<>();
		for(IonChamberParameters p : params) {
			String injectorName = injectorNameMap.get(p.getName());
			Scannable gasInjector = Finder.find(injectorName);
			injectorParamMap.put(gasInjector, p);
		}

		Job job = new Job("Gas fill sequence progress") {

			// update 'enabled' status of gas fill buttons (from gui thread)
			private void updateButtonEnabled(final boolean isEnabled) {
				Display.getDefault().asyncExec(() ->
					gasFillButtons.forEach(button -> {
						if (button != null) {
							button.setEnabled(isEnabled);
						}
					})
				);
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					updateButtonEnabled(false); // disable so buttons they can't be pressed during fill sequence
					monitor.beginTask("Filling gas rig ", injectorParamMap.size());
					// Fill each ion chamber in turn
					for(var injectorItem : injectorParamMap.entrySet()) {
						if (monitor.isCanceled()) {
							break;
						}
						monitor.subTask(injectorItem.getValue().getName());
						List<String> injectorArgs = getInjectorParameters(injectorItem.getValue());
						injectorItem.getKey().moveTo(injectorArgs);
						monitor.worked(1);
					}

				} catch (Exception e1) {
					logger.error("Exception operating a gas injector from the UI", e1);
					return Status.CANCEL_STATUS;
				} finally {
					updateButtonEnabled(true);
				}
				return Status.OK_STATUS;
			}

			@Override
			protected void canceling() {
				try {
					for(var injector : injectorParamMap.keySet()) {
						if (injector.isBusy()) {
							injector.stop();
						}
					}
				} catch (DeviceException e) {
					logger.error("Exception whilst aborting a gas injector", e);
				}
			}
		};

		job.setUser(true);
		job.schedule();

	}

	private static String checkInjectorScannable(IonChamberParameters params) {
		// Try to find correct gas injector to use for the ionchamber :
		String ionchamberName = params.getName(); //name of ionchamber; should be either I0, It or Iref
		String injectorName = injectorNameMap.get(ionchamberName);
		if (injectorName == null) {
			return String.format("Ion chamber name %s not recognised - was expecting name to be I0, It or Iref", ionchamberName);
		}

		final Scannable gasInjector = Finder.find(injectorName);
		if (gasInjector == null) {
			return String.format("Gas injector for %s not found - was expecting to find %s", ionchamberName, injectorName);
		}

		return "";
	}

	private static List<String> getInjectorParameters(IonChamberParameters params) {

		// Assemble all the params required for the gas injector scannable
		String purge_pressure = "25.0";
		String purge_period = "120.0";
		String gas_fill1_pressure_mbar = Double.toString(params.getPressure()*1000);
		String gas_fill1_period = Double.toString(params.getGas_fill1_period_box());

		String gas_fill2_pressure = Double.toString(params.getTotalPressure()*1000);
		String gas_fill2_period = Double.toString(params.getGas_fill2_period_box());

		String gas_select = params.getGasType();
		String gas_select_val = "-1";

		String flushString;
		if (params.getFlush())
			flushString = "True";
		else
			flushString = "False";

		if (gas_select.equals("Kr"))
			gas_select_val = "0";
		else if (gas_select.equals("N"))
			gas_select_val = "1";
		else if (gas_select.equals("Ar"))
			gas_select_val = "2";

		/* A GasInjectionScannable object (set up in spring config) actually performs the fill, purge sequence for the ionchamber.
		  Fill, purge sequence is run by setting the scannable 'position' using a list of strings with all the parameter values in a specific order.
		 ( Note that although the GasInjectionScannable classes for b18 and i20 are different, the format of the 'position' are the same
		   so initiating the fill-purge sequence can be done in the same way. )
		 */
		List<String> args = new ArrayList<>();
		args.add(purge_pressure);
		args.add(purge_period);
		args.add(gas_fill1_pressure_mbar);
		args.add(gas_fill1_period);
		args.add(gas_fill2_pressure);
		args.add(gas_fill2_period);
		args.add(gas_select_val);
		args.add(flushString);

		return args;
	}
}
