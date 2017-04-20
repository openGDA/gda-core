package uk.ac.gda.client.closeactions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import uk.ac.gda.client.closeactions.ClientCloseOption;
import uk.ac.gda.client.closeactions.UserOptionsMenuOnClose;

/**
 *  Wraps UserOptionsMenuOnClose
 *
 *  Creates a popup menu which asks why the user is closing the client, then executes any required actions,
 *  mainly sending notificaition emails to various staff.
 *  It encourages feedback by asking users to explain why they're restarting the client (maybe something
 *  is running slowly, or hanging, or has gotten stuck and we might be able to fix it)
 *  Also reminds users to call the EHC when done!
 *
 *  It is currently called from preShutdown, which runs whenever the client is closed
 */
public class UserOptionsOnCloseDialog extends TitleAreaDialog {

	public final static int RESTART = -1;
	public final static int OK = 0;
	public final static int CANCEL = 1;

	private static final String TITLE = "Close Actions";
	private static final String BLURB = "Please tell us why you're closing the client, so we can take appropriate action.";

	private final int niceWidth = 480;

	private UserOptionsMenuOnClose menu;
	UserSelectedActionOnClose closeAction = new UserSelectedActionOnClose();

	public UserOptionsOnCloseDialog(Shell parent) {
		super(parent);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	public void create() {
		super.create();
		setTitle(TITLE);
		setMessage(BLURB);
		setBlockOnOpen(true); // make modal so dialog can't get lost
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		menu = new UserOptionsMenuOnClose(parent, SWT.NONE, niceWidth);
		return menu;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Close Menu");
	}

	@Override
	protected Point getInitialSize() {
		final Point original = super.getInitialSize();
		return new Point(niceWidth + 20, original.y);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (menu.selectedOption() == ClientCloseOption.RESTART_CLIENT){
					closeAction.doCloseAction(menu.selectedOption(), menu.restartReason());
					System.setProperty("requestedRestart", "true");
					setReturnCode(RESTART);
				} else if (menu.selectedOption() == ClientCloseOption.RESTART_CLIENT_AND_SERVER){
					closeAction.doCloseAction(menu.selectedOption(), menu.restartReason());
					setReturnCode(OK);
				} else{
					closeAction.doCloseAction(menu.selectedOption(), "");
					setReturnCode(OK);
				}
			}
		});
		button = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				//in case someone selects restart, then changes their mind
				System.setProperty("requestedRestart", "false");
			}
		});
	}

	public static void main(String[] args) {
		final Display display = Display.getDefault();
		Shell shell = new Shell(display);
		GridLayoutFactory.fillDefaults().applyTo(shell);
		shell.setText("Close Menu");

		// position shell in centre of screen
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);

		UserOptionsOnCloseDialog close = new UserOptionsOnCloseDialog(shell);

		shell.pack();
		close.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}