package uk.ac.gda.client.logpanel.view;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class MinLogLevelCombo extends WorkbenchWindowControlContribution {
	
	public static final String ID = "uk.ac.gda.client.logpanel.view.MinLogLevelCombo";
	
	@Override
	protected Control createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		
		Label label = new Label(container, SWT.NONE);
		label.setText("Minimum log level:");
		
		final Combo control = new Combo(container, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
		control.setItems(Logpanel.LOG_LEVELS.values().toArray(new String[] {}));
		
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
		GridDataFactory.swtDefaults().applyTo(label);
		GridDataFactory.swtDefaults().applyTo(control);
		
		getWorkbenchWindow().getPartService().addPartListener(new IPartListener2() {
			@Override public void partOpened(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(LogpanelView.ID)) {
					LogpanelView logpanelView = (LogpanelView) partRef.getPart(false);
					final Logpanel logpanel = logpanelView.getLogpanel();
					control.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							logpanel.setMinLogLevel(Logpanel.getLogLevel(control.getText()));
						}
					});
					control.setText(Logpanel.getLogLevelText(logpanel.getMinLogLevel()));
				}
			}
			@Override public void partActivated(IWorkbenchPartReference partRef) {}
			@Override public void partClosed(IWorkbenchPartReference partRef) {}
			@Override public void partDeactivated(IWorkbenchPartReference partRef) {}
			@Override public void partHidden(IWorkbenchPartReference partRef) {}
			@Override public void partInputChanged(IWorkbenchPartReference partRef) {}
			@Override public void partBroughtToTop(IWorkbenchPartReference partRef) {}
			@Override public void partVisible(IWorkbenchPartReference partRef) {}
		});
		
		return container;
	}
	
}
