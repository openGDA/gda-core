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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private List<ParameterConfig> parameterConfig;

	/** These maps store the selection status of each of the displayed parameters (key provided by {@link #makeMapKey(String, String)} */
	private Map<String, Boolean> paramSelectionMap = new HashMap<>();
	private Map<String, Button> paramSelectionButtonMap = new HashMap<>();

//	private List<ParameterValuesForBean> paramsToSet;

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
//		this.paramsToSet = paramsToSet;

		for(ParameterValuesForBean paramForBean : paramsToSet) {
			for(ParameterValue p : paramForBean.getParameterValues()) {
				String keyval = makeMapKey(paramForBean.getBeanType(), p.getFullPathToGetter());
				Button button = paramSelectionButtonMap.get(keyval);
				if (button != null) {
					button.setSelection(true);
				}
			}
		}
	}

	public List<ParameterValuesForBean> getOverrides() {
		Map<String, ParameterValuesForBean> paramForBeanType = new HashMap<>();

		for(ParameterConfig editableParam : parameterConfig) {
			Boolean selected = paramSelectionMap.get(makeBeanTypeAndGetterString(editableParam));
			if (selected != null && selected) {
				String beanType = editableParam.getBeanType();
				ParameterValuesForBean overrideParams = paramForBeanType.get(beanType);
				if (overrideParams == null) {
					overrideParams = new ParameterValuesForBean();
					overrideParams.setBeanType(beanType);
					paramForBeanType.put(beanType, overrideParams);
				}
				overrideParams.addParameterValue(editableParam.getFullPathToGetter(), "");
			}
		}
		return new ArrayList<ParameterValuesForBean>(paramForBeanType.values());
	}

	/**
	 * Make string for map key, using bean type and getter strings.
	 * @param beanType
	 * @param getter
	 * @return map key
	 */
	public String makeMapKey(String beanType, String getter) {
		return beanType + ":" + getter;
	}

	public String makeBeanTypeAndGetterString(ParameterConfig param) {
		return makeMapKey(param.getBeanType(), param.getFullPathToGetter());
	}

	private void createComposite(Composite parent) {
		Label infoLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(infoLabel);
		infoLabel.setText("Select the measurement conditions to set the values for : ");

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2,false));

		paramSelectionButtonMap = new HashMap<>();
		for(ParameterConfig param : parameterConfig) {
			if (param.getShowInParameterSelectionDialog()) {
				Label label = new Label(comp, SWT.NONE);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

				label.setText(param.getDescription()+"     ");
				label.setToolTipText(param.getBeanType()+" : "+param.getFullPathToGetter());

				final Button button = new Button(comp, SWT.CHECK);
				GridDataFactory.fillDefaults().grab(true, false).hint(15,SWT.NONE).applyTo(button); // sizehint to remove border around empty text next to checkbox (swt bug)
				button.setSelection(false);
				final String keyString = makeMapKey(param.getBeanType(), param.getFullPathToGetter());
				paramSelectionButtonMap.put(keyString, button);
			}
		}
	}

	@Override
	public boolean close() {
		// store the selection status of the checkbox for each parameter before widgets are disposed
		paramSelectionMap = new HashMap<>();
		for(String key : paramSelectionButtonMap.keySet()) {
			paramSelectionMap.put(key, paramSelectionButtonMap.get(key).getSelection());
		}
		return super.close();
	}

	public List<ParameterConfig> getParameterConfig() {
		return parameterConfig;
	}

	public void setParameterConfig(List<ParameterConfig> parameterConfig) {
		this.parameterConfig = parameterConfig;
	}
}
