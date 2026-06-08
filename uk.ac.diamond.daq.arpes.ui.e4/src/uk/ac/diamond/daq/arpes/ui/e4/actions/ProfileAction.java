package uk.ac.diamond.daq.arpes.ui.e4.actions;

import org.dawnsci.plotting.tools.profile.ProfileTool;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProfileAction extends Action implements IAction{
	private static final Logger logger = LoggerFactory.getLogger(ProfileAction.class);
	private final IPlottingSystem<Composite> plottingSystem;
	private final Composite parent;
	private Shell profileShell;
	private Class<? extends ProfileTool> profileToolClass;

	public ProfileAction(IPlottingSystem<Composite> plottingSystem,
								Composite parent, Class<? extends ProfileTool> clazz) {
		super("", IAction.AS_CHECK_BOX);
		this.profileToolClass = clazz;
		setToolTipText("Box Profile");
		setImageDescriptor(
			AbstractUIPlugin.imageDescriptorFromPlugin(
				"uk.ac.diamond.daq.arpes.ui.e4",
				"icons/ProfileBox.png"));
		this.plottingSystem = plottingSystem;
		this.parent = parent;
	}

	@Override
	public void run() {
		if (plottingSystem.getRegions(RegionType.PERIMETERBOX).isEmpty()) {
			createDefaultRegion(plottingSystem);
		}
		if (isChecked()) {
			openBoxProfilePopup(parent);
		} else {

			if (profileShell != null) {
				profileShell.close();
			}
		}
	}

	private void createDefaultRegion(IPlottingSystem<Composite> pts) {
		IRegion newRegion;
		try {
			newRegion = pts.createRegion("AutoBoxProfile_" + System.currentTimeMillis(), RegionType.PERIMETERBOX);
			IROI roi = new PerimeterBoxROI(10, 10, 500, 500, 0);
			newRegion.setROI(roi);
			pts.addRegion(newRegion);
		} catch (Exception e) {
			logger.error("Failed to create region", e);
		}
	}

	private void openBoxProfilePopup(Composite parent) {

		if (profileShell != null && !profileShell.isDisposed()) {
			profileShell.setVisible(true);
			profileShell.setActive();
			return;
		}

		Shell parentShell = parent.getShell();

		profileShell = new Shell(parentShell, SWT.SHELL_TRIM | SWT.RESIZE);
		profileShell.setText("Profile Tool");
		profileShell.setLayout(new FillLayout());

		try {
			IPageSite site = new EmbeddedPageSite(plottingSystem.getActionBars());

			ProfileTool profileTool = profileToolClass.getDeclaredConstructor().newInstance();
			profileTool.init(site);
			profileTool.setPlottingSystem(plottingSystem);
			profileTool.createControl(profileShell);
			profileTool.activate();

			profileShell.addDisposeListener(e -> {
					profileTool.dispose();
					if (isChecked()) setChecked(false);
			});
			profileShell.setSize(400, 500);
			profileShell.open();
		} catch (Exception e) {
			logger.error("Failed to create profile tool ",e);
		}

	}

	private class EmbeddedPageSite implements IPageSite {

		private final IActionBars actionBars;
		private final IWorkbenchPartSite partSite;

		public EmbeddedPageSite(IActionBars actionBars) {
			this.actionBars = actionBars;
			this.partSite = null;
		}

		@Override
		public IActionBars getActionBars() {
			return actionBars;
		}

		@Override
		public Shell getShell() {
			return partSite.getShell();
		}

		@Override
		public IWorkbenchWindow getWorkbenchWindow() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectionProvider getSelectionProvider() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IWorkbenchPage getPage() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setSelectionProvider(ISelectionProvider provider) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T getService(Class<T> api) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasService(Class<?> api) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider) {
			throw new UnsupportedOperationException();
		}
	}
}
