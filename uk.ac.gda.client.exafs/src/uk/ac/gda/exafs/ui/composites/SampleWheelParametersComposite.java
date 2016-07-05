/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.widgets.FieldBeanComposite;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gda.jython.JythonServerFacade;
import uk.ac.gda.beans.exafs.b18.B18SampleParameters;

/**
 *
 */
public final class SampleWheelParametersComposite extends FieldBeanComposite {

	private FieldComposite demand;
	private ComboWrapper filter;
	private BooleanWrapper manual;
	boolean showManual;
	private Button btnUpdateFilters;
	private Button btnGetCurrentValue;
	private Composite composite;
	private BooleanWrapper wheelEnabled;
	private boolean show;
	private Label lblDemand;
	private Label lblFilter;

	public SampleWheelParametersComposite(Composite parent, int style, B18SampleParameters bean) {
		super(parent, style);

		setLayout(new GridLayout(1, true));

		wheelEnabled = new BooleanWrapper(this, SWT.NONE);
		wheelEnabled.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		wheelEnabled.setText("Enabled");
		show = bean.getSampleWheelParameters().isWheelEnabled();

		manual = new BooleanWrapper(this, SWT.NONE);
		manual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		manual.setText("Manual Control");
		showManual = bean.getSampleWheelParameters().isManual();

		composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		lblDemand = new Label(composite, SWT.NONE);
		lblDemand.setText("Demand");
		demand = new ScaleBox(composite, SWT.NONE);
		((GridData) demand.getControl().getLayoutData()).widthHint = 60;
		demand.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		createEmptyLabel(demand);
		btnGetCurrentValue = new Button(composite, SWT.NONE);
		btnGetCurrentValue.setText("Get Current Value");

		lblFilter = new Label(composite, SWT.NONE);
		lblFilter.setText("Filter");

		filter = new ComboWrapper(composite, SWT.NONE);
		filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		updateFilters();

		filter.select(findFilterIndex(bean.getSampleWheelParameters().getFilter()));

		btnUpdateFilters = new Button(composite, SWT.NONE);
		btnUpdateFilters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnUpdateFilters.setText("Update Filters");

		btnGetCurrentValue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String demandValue = JythonServerFacade.getInstance().evaluateCommand("samplewheel()");
				demand.setValue(demandValue);
			}
		});

		btnUpdateFilters.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFilters();
			}
		});

		manual.setEnabled(show);
		demand.setEnabled(show);
		btnGetCurrentValue.setEnabled(show);
		filter.setEnabled(show);
		btnUpdateFilters.setEnabled(show);

		wheelEnabled.addValueListener(new ValueListener() {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				show = !show;
				manual.setEnabled(show);
				demand.setEnabled(show);
				btnGetCurrentValue.setEnabled(show);
				filter.setEnabled(show);
				btnUpdateFilters.setEnabled(show);
				lblFilter.setEnabled(show);
				lblDemand.setEnabled(show);
			}

			@Override
			public String getValueListenerName() {
				return null;
			}
		});

		if (show) {
			demand.setEnabled(showManual);
			btnGetCurrentValue.setEnabled(showManual);
			filter.setEnabled(!showManual);
			btnUpdateFilters.setEnabled(!showManual);
		}

		manual.addValueListener(new ValueListener() {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				showManual = !showManual;
				demand.setEnabled(showManual);
				btnGetCurrentValue.setEnabled(showManual);
				filter.setEnabled(!showManual);
				btnUpdateFilters.setEnabled(!showManual);
			}

			@Override
			public String getValueListenerName() {
				return null;
			}
		});
	}

	private String[] getFilters() {
		String filters = JythonServerFacade.getInstance().evaluateCommand("samplewheel.getFilterNames()");
		if (filters != null) {
			String[] filterList = filters.substring(filters.indexOf("[") + 1, filters.indexOf("]")).split(", ");
			for (int i = 0; i < filterList.length; i++) {
				filterList[i] = filterList[i].substring(2, filterList[i].length() - 1);
			}
			return filterList;
		}
		return new String[]{};
	}

	private int findFilterIndex(String name) {
		// Store filterlist at start to improve efficiency... imh 3/6/2016
		String [] filterList = getFilters();
		for (int i = 0; i < filterList.length; i++) {
			if (filterList[i].equals(name))
				return i;
	        }
		return -1;
	}

	private void updateFilters() {
		String filters = JythonServerFacade.getInstance().evaluateCommand("samplewheel.getFilterNames()");
		if (filters != null) {
			String[] filterList = filters.substring(filters.indexOf("[") + 1, filters.indexOf("]")).split(", ");
			for (int i = 0; i < filterList.length; i++) {
				filterList[i] = filterList[i].substring(2, filterList[i].length() - 1);
			}
			filter.setItems(getFilters());
		}
	}

	public FieldComposite getDemand() {
		return demand;
	}

	public FieldComposite getFilter() {
		return filter;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public FieldComposite getManual() {
		return manual;
	}

	public BooleanWrapper getWheelEnabled() {
		return wheelEnabled;
	}

	@SuppressWarnings("unused")
	private void createEmptyLabel(Composite composite){
		new Label(composite, SWT.NONE);
	}
}
