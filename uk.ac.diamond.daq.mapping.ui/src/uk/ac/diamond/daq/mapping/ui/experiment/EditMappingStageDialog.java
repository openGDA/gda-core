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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.Objects;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.impl.MappingStageOptions;

/**
 * This dialog lets us choose which motors are used for fast, slow and (optionally) associated axis in a Mapping experiment.
 * It works better when a {@link MappingStageOptions} is configured.
 */
public class EditMappingStageDialog extends Dialog {

	private MappingStageInfo stageInfo;
	private StageManagerControl stageManagerControl;
	private MappingStageOptions options;

	protected EditMappingStageDialog(Shell parentShell, MappingStageInfo stageInfo) {
		super(parentShell);
		setShellStyle(SWT.RESIZE);
		this.stageInfo = stageInfo;
		findMappingStageOptions();
	}

	private void findMappingStageOptions() {
		BundleContext context = FrameworkUtil.getBundle(EditMappingStageDialog.class).getBundleContext();
		ServiceReference<MappingStageOptions> serviceReference = context.getServiceReference(MappingStageOptions.class);
		if (Objects.nonNull(serviceReference)) {
			options = context.getService(serviceReference);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite mappingStageComposite = (Composite) super.createDialogArea(parent);

		if (Objects.nonNull(options)) {
			stageManagerControl = new ComboStageManagerControl(mappingStageComposite);
		} else {
			stageManagerControl = new FreeTextStageManagerControl(mappingStageComposite);
		}

		return mappingStageComposite;
	}

	/**
	 * Data binding only on pressing OK
	 */
	@Override
	protected void okPressed() {
		stageInfo.setActiveFastScanAxis(stageManagerControl.getFastAxisName());
		stageInfo.setActiveSlowScanAxis(stageManagerControl.getSlowAxisName());
		stageInfo.setAssociatedAxis(stageManagerControl.getAssociatedAxisName());
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure mapping stage");
	}

	private interface StageManagerControl {
		String getFastAxisName();
		String getSlowAxisName();
		String getAssociatedAxisName();
	}

	/**
	 * Preferred StageManagerControl implementation.<p>
	 * Requires a configured {@link MappingStageOptions}.
	 */
	private class ComboStageManagerControl implements StageManagerControl {
		private Combo fastAxis;
		private Combo slowAxis;
		private Combo associatedAxis;

		public ComboStageManagerControl(Composite parent) {
			Composite mainComposite = new Composite(parent, SWT.NONE);

			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(mainComposite);
			GridDataFactory horizGrab = GridDataFactory.fillDefaults().grab(true, false);
			horizGrab.applyTo(mainComposite);

			new Label(mainComposite, SWT.NONE).setText("Fast axis");
			fastAxis = new Combo(mainComposite, SWT.READ_ONLY);
			fastAxis.setItems(options.getFastAxes().toArray(new String[0]));
			fastAxis.select(options.getFastAxes().indexOf(stageInfo.getActiveFastScanAxis()));
			horizGrab.applyTo(fastAxis);

			new Label(mainComposite, SWT.NONE).setText("Slow axis");
			slowAxis = new Combo(mainComposite, SWT.READ_ONLY);
			slowAxis.setItems(options.getSlowAxes().toArray(new String[0]));
			slowAxis.select(options.getSlowAxes().indexOf(stageInfo.getActiveSlowScanAxis()));
			horizGrab.applyTo(slowAxis);

			if (Objects.nonNull(options.getAssociatedAxes())) {
				new Label(mainComposite, SWT.NONE).setText("Associated axis");
				associatedAxis = new Combo(mainComposite, SWT.READ_ONLY);
				associatedAxis.setItems(options.getAssociatedAxes().toArray(new String[0]));
				associatedAxis.select(options.getAssociatedAxes().indexOf(stageInfo.getAssociatedAxis()));
				horizGrab.applyTo(associatedAxis);
			}
		}

		@Override
		public String getFastAxisName() {
			return fastAxis.getText();
		}

		@Override
		public String getSlowAxisName() {
			return slowAxis.getText();
		}

		@Override
		public String getAssociatedAxisName() {
			if (Objects.nonNull(associatedAxis)) {
				return associatedAxis.getText();
			} else {
				return null;
			}
		}

	}

	/**
	 * This composite is drawn in the dialog if no {@link MappingStageOptions} configuration is found.<p>
	 * There is no validation in place - typos will cause errors!
	 */
	private class FreeTextStageManagerControl implements StageManagerControl {
		private Text fastAxis;
		private Text slowAxis;
		private Text associatedAxis;

		public FreeTextStageManagerControl(Composite parent) {
			Composite mainComposite = new Composite(parent, SWT.NONE);
			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(mainComposite);
			GridDataFactory horizGrab = GridDataFactory.fillDefaults().grab(true, false);
			horizGrab.applyTo(mainComposite);

			new Label(mainComposite, SWT.NONE).setText("Fast axis");
			fastAxis = new Text(mainComposite, SWT.BORDER);
			fastAxis.setText(stageInfo.getActiveFastScanAxis());
			horizGrab.applyTo(fastAxis);

			new Label(mainComposite, SWT.NONE).setText("Slow axis");
			slowAxis = new Text(mainComposite, SWT.BORDER);
			slowAxis.setText(stageInfo.getActiveSlowScanAxis());
			horizGrab.applyTo(slowAxis);

			new Label(mainComposite, SWT.NONE).setText("Associated axis");
			associatedAxis = new Text(mainComposite, SWT.BORDER);
			if (Objects.nonNull(stageInfo.getAssociatedAxis())) {
				associatedAxis.setText(stageInfo.getAssociatedAxis());
			}
			horizGrab.applyTo(associatedAxis);
		}

		@Override
		public String getFastAxisName() {
			return fastAxis.getText();
		}

		@Override
		public String getSlowAxisName() {
			return slowAxis.getText();
		}

		@Override
		public String getAssociatedAxisName() {
			if (Objects.isNull(associatedAxis.getText()) || associatedAxis.getText().isEmpty()) {
				return null;
			} else {
				return associatedAxis.getText();
			}
		}
	}

}
