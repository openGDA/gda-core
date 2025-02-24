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

package uk.ac.gda.exafs.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;

public class ParameterSelectionDialog extends Dialog {

	//Parameters that will be displayed in view for selecting.
	private List<ParameterConfig> parameterConfigs;

	/** These maps store the selection status of each of the displayed parameters (key provided by {@link #makeMapKey(String, String)} */
	private List<ParameterConfig> selectedParameters = Collections.emptyList();
	private Map<ParameterConfig, Button> paramSelectionButtonMap = new HashMap<>();

	protected ParameterSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		createComposite(container);
		return parent;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Set measurement conditions");
	}

	public void setFromParameters(List<ParameterValuesForBean> paramsToSet) {

		for(ParameterValuesForBean paramForBean : paramsToSet) {
			for(ParameterValue paramValue : paramForBean.getParameterValues()) {

				// locate the ParameterConfig object corresponding to the selected ParameterValue
				String keyval = makeBeanTypeAndGetterString(paramForBean.getBeanType(), paramValue.getFullPathToGetter());
				Optional<ParameterConfig> matchingParam = parameterConfigs.stream()
							.filter(p -> makeBeanTypeAndGetterString(p).equals(keyval))
							.findFirst();

				// Lookup the corresponding Button for the ParameterConfig and set selection to true
				if (matchingParam.isPresent()) {
					paramSelectionButtonMap.get(matchingParam.get()).setSelection(true);
				}
			}
		}
	}

	/**
	 * Generate list of ParmeterValuesForBean from the selected parameters in the dialog
	 * box.
	 * i.e. Use beantype and fullPathToGetter from each selected ParameterConfig along with
	 * fullPathToGetter for any additionalConfig items it contains.
	 *
	 * @return List of ParameterValuesForBean
	 */
	public List<ParameterValuesForBean> getSelectedParameters() {
		Map<String, ParameterValuesForBean> paramForBeanType = new HashMap<>();

		for(ParameterConfig selectedParam : selectedParameters) {
			String beanType = selectedParam.getBeanType();
			ParameterValuesForBean paramsForBean = paramForBeanType.computeIfAbsent(beanType, bt -> {
				var newParamVals = new ParameterValuesForBean();
				newParamVals.setBeanType(beanType);
				return newParamVals;
			});
			paramsForBean.addParameterValue(selectedParam.getFullPathToGetter(), "");

			// Add parameter values from any additional config
			for(var additionalParam: selectedParam.getAdditionalConfig()) {
				paramsForBean.addParameterValue(additionalParam.getFullPathToGetter(), "");
			}
		}
		return new ArrayList<>(paramForBeanType.values());
	}

	/**
	 * Make string for map key, using bean type and getter strings.
	 *
	 * @param beanType
	 * @param getter
	 * @return map key
	 */
	public String makeBeanTypeAndGetterString(String beanType, String getter) {
		return beanType + ":" + getter;
	}

	public String makeBeanTypeAndGetterString(ParameterConfig param) {
		return makeBeanTypeAndGetterString(param.getBeanType(), param.getFullPathToGetter());
	}

	private void createComposite(Composite parent) {
		Label infoLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(infoLabel);
		infoLabel.setText("Select the measurement conditions to set the values for : ");

		Composite comp = new Composite(parent, SWT.NONE );
		comp.setLayout(new GridLayout(6,false));

		paramSelectionButtonMap = new HashMap<>();
		for(ParameterConfig param : parameterConfigs) {
			Label label = new Label(comp, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

			label.setText(param.getDescription()+"     ");
			label.setToolTipText(param.getBeanType()+" : "+param.getFullPathToGetter());

			final Button button = new Button(comp, SWT.CHECK);
			GridDataFactory.fillDefaults().grab(true, false).hint(20,SWT.NONE).applyTo(button); // sizehint to remove border around empty text next to checkbox (swt bug)
			button.setSelection(false);
			paramSelectionButtonMap.put(param, button);
		}
	}

	@Override
	public boolean close() {
		// Make list of selected parameters in the GUI before widgets are disposed
		selectedParameters = parameterConfigs.stream()
				.filter(p -> paramSelectionButtonMap.get(p).getSelection())
				.toList();

		return super.close();
	}

	public List<ParameterConfig> getParameterConfig() {
		return parameterConfigs;
	}

	public void setParameterConfig(List<ParameterConfig> parameterConfig) {
		this.parameterConfigs = parameterConfig;
	}
}
