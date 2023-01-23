/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.mythen;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.exafs.scan.ScanObject;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.MythenParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.FauxRichBeansEditor;
import uk.ac.gda.ui.tool.ClientVerifyListener;

public class MythenParametersUIEditor extends FauxRichBeansEditor<MythenParameters> {
	private static final Logger logger = LoggerFactory.getLogger(MythenParametersUIEditor.class);

	private Text energyBox;
	private Text numFramesBox;
	private Text frameLengthBox;
	private Composite mainComposite;

	public MythenParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, MythenParameters editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	@Override
	protected String getRichEditorTabText() {
		return "Mythen";
	}

	@Override
	public void createPartControl(Composite parent) {
		MythenParameters params = getBean();

		ScrolledComposite	scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		mainComposite = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(5,15).applyTo(mainComposite);

		Label energy = new Label(mainComposite, SWT.NONE);
		energy.setText("Energy (eV)");

		energyBox = new Text(mainComposite, SWT.NONE);
		energyBox.addVerifyListener(ClientVerifyListener.verifyOnlyPositiveDoubleText);

		Label time = new Label(mainComposite, SWT.NONE);
		time.setText("Collection time (sec)");

		frameLengthBox = new Text(mainComposite, SWT.NONE);
		frameLengthBox.addVerifyListener(ClientVerifyListener.verifyOnlyPositiveDoubleText);

		Label numFrames = new Label(mainComposite, SWT.NONE);
		numFrames.setText("Number of frames");

		numFramesBox = new Text(mainComposite, SWT.NONE);
		numFramesBox.addVerifyListener(ClientVerifyListener.verifyOnlyIntegerText);


		Button grabEnergyButton = new Button(mainComposite, SWT.PUSH);
		grabEnergyButton.setText("Get energy from scan");
		grabEnergyButton.setToolTipText("Set the mythen energy to the initial energy of the scan settings");
		grabEnergyButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			updateMythenEnergyFromScan();
		}));

		GridDataFactory gdFactory = GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT);
		Stream.of(energyBox, frameLengthBox, numFramesBox).forEach(gdFactory::applyTo);
		Stream.of(energy, time, numFrames).forEach(gdFactory::applyTo);

		scrolledComposite.setContent(mainComposite);
		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		updateGuiFromBean();

		addTextboxListeners(t -> params.setMythenEnergy(Double.parseDouble(t)), params::getMythenEnergy, energyBox);
		addTextboxListeners(t -> params.setMythenTime(Double.parseDouble(t)), params::getMythenTime, frameLengthBox);
		addTextboxListeners(t -> params.setMythenFrames(Integer.parseInt(t)), params::getMythenFrames, numFramesBox);
	}

	/**
	 * Update the mythen energy using the initial energy from the scan settings
	 * @see #getEnergyFromScan()
	 */
	private void updateMythenEnergyFromScan() {
		getEnergyFromScan().ifPresent(energy ->
			// Update the energy text box; the listeners will take care of notifying that a change has been made
			Display.getDefault().asyncExec(() -> energyBox.setText(Double.toString(energy))) );
	}

	/**
	 * Try to get the initial scan energy from the scan parameter settings in the Experiment explorer perspective
	 * @return
	 */
	private Optional<Double> getEnergyFromScan() {
		// Return immediately if can't get required extension point (i.e. client not using Experiment explorer and xml files to setup experiments)
		try {
			ExperimentFactory.getExperimentObjectManagerClass();
		} catch (Exception e1) {
			logger.info("Unable to access scan settings in Experiment perspective");
			return Optional.empty();
		}

		try {
			final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
			final IScanParameters params = ob.getScanParameters();

			// Find the 'initial energy' getter method if one exists
			Optional<Method> initialEnergyMethod = Stream.of(params.getClass().getDeclaredMethods())
					.filter(meth -> meth.getName().equals("getInitialEnergy"))
					.findFirst();

			if (initialEnergyMethod.isPresent() ) {
				String str = initialEnergyMethod.get().invoke(params).toString();
				return Optional.of(Double.parseDouble(str));
			}
			return Optional.empty();
		} catch (Exception e) {
			logger.warn("Problem getting initial energy from scan settings", e);
			return Optional.empty();
		}
	}

	/**
	 * Update the GUI text boxes using latest values from the bean
	 */
	private void updateGuiFromBean() {
		Display.getDefault().asyncExec(() -> {
			energyBox.setText(Double.toString(getBean().getMythenEnergy()));
			frameLengthBox.setText(Double.toString(getBean().getMythenTime()));
			numFramesBox.setText(Integer.toString(getBean().getMythenFrames()));
		});
	}

	private <T extends Number> void addTextboxListeners(Consumer<String> setter, Supplier<T> getter, Text textbox) {

		Consumer<FocusEvent> updater = event -> {
			logger.debug("Textbox update : new value = {}", textbox.getText());
			if (StringUtils.defaultString(getter.get().toString()).equals(textbox.getText())) {
				logger.debug("Text not modified");
			} else {
				logger.debug("Text content modified");
				setter.accept(textbox.getText());
				beanChanged();
			}
		};

		textbox.addListener(SWT.Modify, e -> updater.accept(null));

		textbox.addFocusListener(FocusListener.focusLostAdapter(updater));

		textbox.addListener(SWT.Traverse, event ->  {
			if (event.detail == SWT.TRAVERSE_RETURN) {
				updater.accept(null);
			}
		});
	}

	@Override
	public void setFocus() {
		mainComposite.setFocus();
	}

	@Override
	public void dispose() {
		mainComposite.dispose();
	}

	@Override
	public void linkUI(boolean tf) {
		// update GUI from bean when it has been changed.
		updateGuiFromBean();
	}
}
