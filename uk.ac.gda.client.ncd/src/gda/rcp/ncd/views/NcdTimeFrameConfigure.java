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

package gda.rcp.ncd.views;

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.beans.FrameSetParameters;
import uk.ac.gda.server.ncd.beans.NcdParameters;
import uk.ac.gda.server.ncd.beans.TimeProfileParameters;

public class NcdTimeFrameConfigure extends ViewPart {
	public static final String ID = "gda.rcp.ncd.views.NcdTimeFrameConfigure"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(NcdTfgConfigure.class);
	private static int duplicateNumber = 1;
	private TabFolder tabFolder;
	private ScrolledComposite container;
	private Composite composite;
	
	@Override
	public void createPartControl(Composite parent) {

		container = new ScrolledComposite(parent, SWT.VERTICAL | SWT.HORIZONTAL);
		composite = new Composite(container, SWT.NONE);

		GridLayout layout = new GridLayout(1, false);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		composite.setLayoutData(gridData);
		composite.setLayout(layout);

		tabFolder = new TabFolder(composite, SWT.NONE);

		createDefaultProfile();
		createActions();

		container.setContent(composite);
		container.setExpandVertical(true);
		container.setExpandHorizontal(true);
		container.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				container.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});
	}
	
	private void createDefaultProfile() {
		List<TimeProfileParameters> timeProfileParameters = TimeFrameProfile.getHardwareConfig();
		if (timeProfileParameters == null) {
		String path = LocalProperties.getVarDir() + "default-timeframes.xml";
		final File toOpen = new File(path);
		if (toOpen.exists()) {
			try {
				timeProfileParameters = NcdParameters.createFromXML(path).getTimeProfileParameters();
			} catch (Exception ne) {
				// ignore as we'll use the hardcoded defaults;
			}
		}
		}
		if (timeProfileParameters == null) {
			TimeProfileParameters tpp = new TimeProfileParameters();
			FrameSetParameters fp = new FrameSetParameters();
			tpp.addFrameSetParameter(fp);
			timeProfileParameters = new ArrayList<TimeProfileParameters>();
			timeProfileParameters.add(tpp);
		}
		for (TimeProfileParameters tpp : timeProfileParameters) {
			TabItem tabItem = new TimeFrameProfile(tabFolder, SWT.NONE, tpp);
			tabItem.setText(tpp.getName());
		}
		tabFolder.pack();
		tabFolder.getParent().pack();

	}

	@Override
	public void setFocus() {
	}

	/**
	 * Returns basic actions with an load and a save.
	 */
	private void createActions() {
		try {
			IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
			manager.add(getAddFrameSetAction(this));
			manager.add(getDeleteFrameSetAction(this));
			manager.add(new Separator());
			manager.add(getInsertProfileAction(this));
			manager.add(getDeleteProfileAction(this));
			manager.add(getCopyProfileAction(this));
			manager.add(new Separator());
			manager.add(getOpenXMLAction(this));
			manager.add(getSaveXMLAction(this));
			manager.add(getConfigureAction(this));
		} catch (Exception e) {
			// if this occurs we're created by main()
		}
	}

	public IAction getSaveXMLAction(final ViewPart view) {
		IAction saveXMLAction = new Action() {
			FileDialog dialog;

			@Override
			public void run() {

				if (dialog == null) {
					dialog = new FileDialog(view.getSite().getShell(), SWT.SAVE);
					dialog.setText("Save time frame parameters");
					dialog.setFilterExtensions(new String[] { "*.xml" });
					dialog.setFilterPath(LocalProperties.getBaseDataDir());
				}

				String path = dialog.open();
				if (path == null)
					return;
				if (!path.toLowerCase().endsWith(".xml"))
					path = path + ".xml";

				final File toSave = new File(path);
				if (toSave.exists()) {
					final boolean ok = MessageDialog.openConfirm(view.getSite().getShell(), "Confirm Overwrite File",
							"The file '" + toSave.getName() + "' already exists.\n\nWould you like to overwrite?");
					if (!ok)
						return;
				}
				try {
					ArrayList<TimeProfileParameters> list = new ArrayList<TimeProfileParameters>();
					for (TabItem item : tabFolder.getItems()) {
						list.add(((TimeFrameProfile) item).getTimeProfileParameters());
					}
					NcdParameters params = new NcdParameters();
					params.setTimeProfileParameters(list);
					NcdParameters.writeToXML(params, path);
				} catch (Exception ne) {
					logger.error("Cannot save time frame parameters", ne);
				}
			}
		};

		saveXMLAction.setText("Save");
		saveXMLAction.setToolTipText("Save Time frame parameters to XML");
		saveXMLAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/save.png"));
		return saveXMLAction;
	}

	public IAction getOpenXMLAction(final ViewPart view) {
		IAction openXMLAction = new Action() {
			FileDialog dialog;

			@Override
			public void run() {
				if (dialog == null) {
					dialog = new FileDialog(view.getSite().getShell(), SWT.OPEN);
					dialog.setText("Load time frame parameters");
					dialog.setFilterExtensions(new String[] { "*.xml" });
					dialog.setFilterPath(LocalProperties.getBaseDataDir());
				}

				String path = dialog.open();
				if (path == null)
					return;

				final File toOpen = new File(path);
				if (!toOpen.exists())
					return;

				try {
					List<TimeProfileParameters> timeProfileParameters = NcdParameters.createFromXML(path).getTimeProfileParameters();

					for (TabItem item : tabFolder.getItems()) {
						item.dispose();
					}
					for (TimeProfileParameters tpp : timeProfileParameters) {
						TabItem tabItem = new TimeFrameProfile(tabFolder, SWT.NONE, tpp);
						tabItem.setText(tpp.getName());
						tpp.resetProfileNumber();
					}
					tabFolder.pack();
					tabFolder.getParent().pack();

				} catch (Exception ne) {
					logger.error("Cannot open time frame parameters", ne);
				}
			}
		};

		openXMLAction.setText("Load");
		openXMLAction.setToolTipText("Load Time frame parameters from XML");
		openXMLAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/open.png"));
		return openXMLAction;
	}

	public IAction getConfigureAction(final ViewPart view) {
		IAction configureAction = new Action() {
			@Override
			public void run() {
				try {
					int index = tabFolder.getSelectionIndex();
					TimeFrameProfile tfp = (TimeFrameProfile) tabFolder.getItem(index);
					tfp.configureHardware();
				} catch (Exception ne) {
					logger.error("Cannot open time frame parameters", ne);
				}
			}
		};

		configureAction.setText("Configure HW");
		configureAction.setToolTipText("Configure Time frame parameters");
		configureAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/configure.png"));
		return configureAction;
	}

	public IAction getCopyProfileAction(final ViewPart view) {
		IAction copyProfileAction = new Action() {
			@Override
			public void run() {
				try {
					int index = tabFolder.getSelectionIndex();
					TabItem item = tabFolder.getItem(index);
					TimeProfileParameters tpp = ((TimeFrameProfile)item).getTimeProfileParameters();
					TabItem newItem = new TimeFrameProfile(tabFolder, SWT.NONE, tpp);
					String duplicateName = tpp.getName() + "-" + duplicateNumber++;
					newItem.setText(duplicateName);
					tpp.setName(duplicateName);
					tabFolder.pack();
					tabFolder.getParent().pack();
				} catch (Exception ne) {
					logger.error("Cannot copy the profile", ne);
				}
			}
		};

		copyProfileAction.setText("Copy Profile");
		copyProfileAction.setToolTipText("Copy the currently selected profile");
		copyProfileAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/copy.png"));
		return copyProfileAction;
	}

	public IAction getInsertProfileAction(final ViewPart view) {
		IAction insertProfileAction = new Action() {
			@Override
			public void run() {
				try {
					createDefaultProfile();
				} catch (Exception ne) {
					logger.error("Cannot insert new profile", ne);
				}
			}
		};

		insertProfileAction.setText("Insert Profile");
		insertProfileAction.setToolTipText("Insert a new profile");
		insertProfileAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/insert.png"));
		return insertProfileAction;
	}

	public IAction getDeleteProfileAction(final ViewPart view) {
		IAction deleteProfileAction = new Action() {
			@Override
			public void run() {
				try {
					int index = tabFolder.getSelectionIndex();
					if (index > 0) {
						TabItem item = tabFolder.getItem(index);
						item.dispose();
					}
				} catch (Exception ne) {
					logger.error("Cannot delete the selected profile", ne);
				}
			}
		};

		deleteProfileAction.setText("Delete Profile");
		deleteProfileAction.setToolTipText("Delete the currently selected profile");
		deleteProfileAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/delete.png"));
		return deleteProfileAction;
	}

	public IAction getAddFrameSetAction(final ViewPart view) {
		IAction addFrameSetAction = new Action() {
			@Override
			public void run() {
				try {
					int index = tabFolder.getSelectionIndex();
					TabItem item = tabFolder.getItem(index);
					((TimeFrameProfile)item).addFrameSet();
				} catch (Exception ne) {
					logger.error("Cannot add group to profile", ne);
				}
			}
		};

		addFrameSetAction.setText("Add group to Profile");
		addFrameSetAction.setToolTipText("Insert default group after the current selection");
		addFrameSetAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/table_add.png"));
		return addFrameSetAction;
	}

	public IAction getDeleteFrameSetAction(final ViewPart view) {
		IAction deleteFrameSetAction = new Action() {
			@Override
			public void run() {
				try {
					int index = tabFolder.getSelectionIndex();
					TabItem item = tabFolder.getItem(index);
					((TimeFrameProfile)item).deleteFrameSet();
				} catch (Exception ne) {
					logger.error("Cannot delete group from profile", ne);
				}
			}
		};

		deleteFrameSetAction.setText("Delete group from Profile");
		deleteFrameSetAction.setToolTipText("Delete the currently selected group");
		deleteFrameSetAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/table_delete.png"));
		return deleteFrameSetAction;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("TFG Configure");
		shell.setLayout(new FillLayout());
		NcdTimeFrameConfigure sd = new NcdTimeFrameConfigure();
		sd.createPartControl(shell);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
