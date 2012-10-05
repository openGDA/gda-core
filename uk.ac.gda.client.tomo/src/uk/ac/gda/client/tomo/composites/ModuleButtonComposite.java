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

package uk.ac.gda.client.tomo.composites;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite that handles the modules for the PCO camera.
 */
public class ModuleButtonComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(ModuleButtonComposite.class);
	private FontRegistry fontRegistry;
	private List<IModuleChangeListener> moduleChangeListeners = new ArrayList<IModuleChangeListener>();
	private static final String MODULE_4 = "4";
	private static final String MODULE_3 = "3";
	private static final String MODULE_2 = "2";
	private static final String MODULE_1 = "1";
	private static final String MODULES_HEADER = "Modules";
	private static final String NORMAL_TEXT_10 = "normal_10";
	/* Module buttons */
	private final Button btnModule1;
	private final Button btnModule2;
	private final Button btnModule3;
	private final Button btnModule4;

	private CAMERA_MODULE selectedModule;

	/**
	 * Module numbers
	 */
	public enum CAMERA_MODULE {
		ONE {
			@Override
			public Integer getValue() {
				return 1;
			}

			@Override
			public void select(ModuleButtonComposite mB) {
				selectControl(mB.btnModule1);
				deSelectControl(mB.btnModule2);
				deSelectControl(mB.btnModule3);
				deSelectControl(mB.btnModule4);
			}

		},
		TWO {
			@Override
			public Integer getValue() {
				return 2;
			}

			@Override
			public void select(ModuleButtonComposite mB) {
				deSelectControl(mB.btnModule1);
				selectControl(mB.btnModule2);
				deSelectControl(mB.btnModule3);
				deSelectControl(mB.btnModule4);
			}

		},
		THREE {
			@Override
			public Integer getValue() {
				return 3;
			}

			@Override
			public void select(ModuleButtonComposite mB) {
				deSelectControl(mB.btnModule1);
				deSelectControl(mB.btnModule2);
				selectControl(mB.btnModule3);
				deSelectControl(mB.btnModule4);
			}

		},
		FOUR {
			@Override
			public Integer getValue() {
				return 4;
			}

			@Override
			public void select(ModuleButtonComposite mB) {
				deSelectControl(mB.btnModule1);
				deSelectControl(mB.btnModule2);
				deSelectControl(mB.btnModule3);
				selectControl(mB.btnModule4);
			}
		},
		NO_MODULE {
			@Override
			public Integer getValue() {
				return -1;
			}

			@Override
			public void select(ModuleButtonComposite moduleButtonComposite) {
				// do nothing
			}
		}

		;

		public abstract Integer getValue();

		public abstract void select(ModuleButtonComposite moduleButtonComposite);

		public static CAMERA_MODULE getEnum(Integer val) {
			for (CAMERA_MODULE mod : values()) {
				if (val == mod.getValue()) {
					return mod;
				}
			}
			return null;
		}

	}

	private void showError(final String dialogTitle, final Exception ex) {
		if (!this.isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(getShell(), dialogTitle, ex.getMessage());
				}
			});
		}
	}

	private void initializeFontRegistry() {
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(NORMAL_TEXT_10, new FontData[] { new FontData(fontName, 8, SWT.NORMAL) });
		}
	}

	public ModuleButtonComposite(Composite parent, FormToolkit toolkit) {
		super(parent, SWT.None);
		initializeFontRegistry();
		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		this.setLayout(layout);

		lblModulesHeader = toolkit.createLabel(this, MODULES_HEADER, SWT.CENTER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		lblModulesHeader.setLayoutData(gd);
		/**/
		btnModule1 = toolkit.createButton(this, MODULE_1, SWT.PUSH);
		btnModule1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnModule1.addListener(SWT.MouseDown, ctrlMouseListener);
		btnModule1.addListener(SWT.MouseHover, mouseHoverListener);
		btnModule1.addListener(SWT.MouseExit, mouseExitListener);
		btnModule1.setFont(fontRegistry.get(NORMAL_TEXT_10));
		/**/
		btnModule2 = toolkit.createButton(this, MODULE_2, SWT.PUSH);
		btnModule2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnModule2.addListener(SWT.MouseDown, ctrlMouseListener);
		btnModule2.addListener(SWT.MouseHover, mouseHoverListener);
		btnModule2.addListener(SWT.MouseExit, mouseExitListener);
		btnModule2.setFont(fontRegistry.get(NORMAL_TEXT_10));
		/**/
		btnModule3 = toolkit.createButton(this, MODULE_3, SWT.PUSH);
		btnModule3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnModule3.addListener(SWT.MouseDown, ctrlMouseListener);
		btnModule3.addListener(SWT.MouseHover, mouseHoverListener);
		btnModule3.addListener(SWT.MouseExit, mouseExitListener);
		btnModule3.setFont(fontRegistry.get(NORMAL_TEXT_10));
		/**/
		btnModule4 = toolkit.createButton(this, MODULE_4, SWT.PUSH);
		btnModule4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnModule4.addListener(SWT.MouseDown, ctrlMouseListener);
		btnModule4.addListener(SWT.MouseHover, mouseHoverListener);
		btnModule4.addListener(SWT.MouseExit, mouseExitListener);
		btnModule4.setFont(fontRegistry.get(NORMAL_TEXT_10));
	}

	/**
	 * Method to set the button texts on the camera module buttons
	 * 
	 * @param units
	 * @param btn1Text
	 * @param btn2Text
	 * @param btn3Text
	 * @param btn4Text
	 */
	public void setModuleButtonText(String units, String btn1Text, String btn2Text, String btn3Text, String btn4Text) {
		lblModulesHeader.setText(String.format("%1$s (%2$s)", MODULES_HEADER, units));
		btnModule1.setText(btn1Text);
		btnModule2.setText(btn2Text);
		btnModule3.setText(btn3Text);
		btnModule4.setText(btn4Text);
	}

	private Listener mouseExitListener = new Listener() {
		@Override
		public void handleEvent(Event event) {

			Button btn = (Button) event.widget;
			if (!isSelected(btn)) {
				btn.setForeground(ColorConstants.black);
			}
		}

	};

	private Listener mouseHoverListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			if (event.stateMask == SWT.CTRL) {
				Button btn = (Button) event.widget;
				if (!isSelected(btn)) {
					btn.setForeground(ColorConstants.red);
				}
			}
		}
	};

	/**
	 * listener for buttons and slider - but this is control masked - the "Ctrl" key needs to be pressed
	 */
	private Listener ctrlMouseListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			if (event.stateMask == SWT.CTRL) {
				Object sourceObj = event.widget;
				CAMERA_MODULE oldModule = getModuleSelected();

				if (sourceObj == btnModule1) {
					if (!isSelected(btnModule1)) {
						selectControl(btnModule1);

						/**/
						deSelectControl(btnModule2);
						deSelectControl(btnModule3);
						deSelectControl(btnModule4);
						selectedModule = CAMERA_MODULE.ONE;
						updateModuleChangeListener(oldModule, CAMERA_MODULE.ONE);
					}
				} else if (sourceObj == btnModule2) {
					if (!isSelected(btnModule2)) {
						selectControl(btnModule2);
						/**/
						deSelectControl(btnModule1);
						deSelectControl(btnModule3);
						deSelectControl(btnModule4);
						selectedModule = CAMERA_MODULE.TWO;
						updateModuleChangeListener(oldModule, CAMERA_MODULE.TWO);
					}
				} else if (sourceObj == btnModule3) {
					if (!isSelected(btnModule3)) {
						selectControl(btnModule3);
						/**/
						deSelectControl(btnModule2);
						deSelectControl(btnModule1);
						deSelectControl(btnModule4);
						selectedModule = CAMERA_MODULE.THREE;
						updateModuleChangeListener(oldModule, CAMERA_MODULE.THREE);
					}
				} else if (sourceObj == btnModule4) {
					if (!isSelected(btnModule4)) {
						selectControl(btnModule4);
						/**/
						deSelectControl(btnModule2);
						deSelectControl(btnModule3);
						deSelectControl(btnModule1);
						selectedModule = CAMERA_MODULE.FOUR;
						updateModuleChangeListener(oldModule, CAMERA_MODULE.FOUR);
					}
				}
			}
		}
	};

	private Label lblModulesHeader;

	public CAMERA_MODULE getModuleSelected() {
		logger.debug("getModuleSelected#{}", selectedModule);
		return selectedModule;
	}

	private void updateModuleChangeListener(CAMERA_MODULE oldModule, CAMERA_MODULE newModule) {
		logger.debug(String.format("Module change from %1$s to %2$s", oldModule.toString(), newModule.toString()));
		try {
			for (IModuleChangeListener moduleChangeListener : moduleChangeListeners) {
				moduleChangeListener.moduleChanged(oldModule, newModule);
			}
		} catch (InterruptedException e) {
			showError("Error updating module change", e);
		} catch (InvocationTargetException e) {
			showError("Error updating module change", e);
		}
	}

	private static void selectControl(Button btnCntrl) {
		btnCntrl.setForeground(ColorConstants.red);
		btnCntrl.setBackground(ColorConstants.lightGray);
	}

	private static void deSelectControl(Button btnCntrl) {
		btnCntrl.setForeground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		btnCntrl.setBackground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	}

	public boolean addModuleChangeListener(IModuleChangeListener moduleChangeListener) {
		return moduleChangeListeners.add(moduleChangeListener);
	}

	public boolean removeModuleChangeListener(IModuleChangeListener moduleChangeListener) {
		return moduleChangeListeners.remove(moduleChangeListener);
	}

	/**
	 * Returns <code>true</code> if the colors as set when selected.
	 * 
	 * @param button
	 * @return true when background color is lightgray and foreground color is red - this is what was set when the
	 *         widget was selected.
	 */
	private boolean isSelected(Button button) {
		if (ColorConstants.red.equals(button.getForeground())
				&& ColorConstants.lightGray.equals(button.getBackground())) {
			return true;
		}
		return false;
	}

	// TODO
	@Override
	public void setEnabled(boolean enabled) {
		btnModule1.setEnabled(enabled);
		btnModule2.setEnabled(enabled);
		btnModule3.setEnabled(enabled);
		btnModule4.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	public void switchOn(final CAMERA_MODULE cameraModule) {
		selectedModule = cameraModule;
		if (!this.isDisposed()) {
			this.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					cameraModule.select(ModuleButtonComposite.this);
				}
			});
		}
	}

	public void deselectAll() {
		selectedModule = CAMERA_MODULE.NO_MODULE;
		if (!this.isDisposed()) {
			this.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					deSelectControl(btnModule1);
					deSelectControl(btnModule2);
					deSelectControl(btnModule3);
					deSelectControl(btnModule4);
				}
			});
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 400, 400));
		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		ModuleButtonComposite motionControlComposite = new ModuleButtonComposite(shell, new FormToolkit(display));

		motionControlComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

}
