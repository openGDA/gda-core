package uk.ac.gda.client.closeactions;

import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.gtk.GdkColor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;
import uk.ac.gda.client.closeactions.ClientCloseOption;

/**
 *  Wrapped by UserOptionsOnCloseDialog
 *
 *  Creates a popup menu which asks why the user is closing the client, then executes any required actions. 
 *  It encourages feedback by asking users to explain why they're restarting the client (maybe something
 *  is running slowly, or hanging, or has gotten stuck and we might be able to fix it)
 *  Also reminds users to call the EHC when done!
 *
 *  It is currently called from preShutdown, which runs whenever the client is closed
 */
public class UserOptionsMenuOnClose extends Composite {

	public final int niceWidth;

	private Text feedback;
	private Text name;
	private Label nameError;
	private ClientCloseOption selectedOption = ClientCloseOption.FINISHED;
	private static final String EHC_PHONE_NUMBER = LocalProperties.get("gda.facility.EHC.phone");
	private static final String URGENT_HELP_MESSAGE =
			"If you require urgent assistance during working hours call your local contact.\n" +
			"If you require urgent out of hours GDA assistance call the EHC on " + EHC_PHONE_NUMBER;

	public UserOptionsMenuOnClose(Composite parent, int style, int niceWidth) {
		super(parent, style);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
		this.niceWidth = niceWidth;

		GridLayoutFactory.fillDefaults().applyTo(this);

		// radial button group
		Composite selectionGroup = new Composite(this, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(15, 0).applyTo(selectionGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(selectionGroup);

		Composite buttonComposite = new Composite(selectionGroup, SWT.NONE);
		GridLayoutFactory.swtDefaults().spacing(5, 10).numColumns(2).applyTo(buttonComposite);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(buttonComposite);

		final Button finishedOption = optionButton(buttonComposite,
				"I'm finished using GDA",
				SWT.COLOR_WIDGET_FOREGROUND);
		finishedOption.addSelectionListener(createListener(ClientCloseOption.FINISHED, false));

		// If an automated client is present, give user the option to pass the baton to it on close
		// Only do this if user has the baton, or if baton is not held.
		boolean allowPassToUDC = false;
		if (InterfaceProvider.getBatonStateProvider().amIBatonHolder() ||
				!InterfaceProvider.getBatonStateProvider().isBatonHeld()) {
			ClientDetails[] runningClients = InterfaceProvider.getBatonStateProvider().getOtherClientInformation();
			allowPassToUDC = Arrays.stream(runningClients).anyMatch(ClientDetails::isAutomatedUser);
		}

		Button finishedPassToUDCOption = null;
		if (allowPassToUDC) {
			finishedPassToUDCOption = optionButton(buttonComposite,
					"I'm finished using GDA and will pass the baton to the UDC client",
					SWT.COLOR_RED);
			finishedPassToUDCOption.addSelectionListener(createListener(ClientCloseOption.FINISHED_UDC, false));
		}

		final Button restartOption = optionButton(buttonComposite,
				"I need to restart GDA because:",
				SWT.COLOR_WIDGET_FOREGROUND);
		restartOption.addSelectionListener(createListener(ClientCloseOption.RESTART_CLIENT, true));

		feedback = new Text(selectionGroup, SWT.MULTI | SWT.BORDER);
		GridDataFactory.fillDefaults().hint(niceWidth, 200).grab(true, true).applyTo(feedback);

		Composite nameGroup = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(15, 10).applyTo(nameGroup);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameGroup);

		Label nameLabel = new Label(nameGroup, SWT.WRAP);
		nameLabel.setText(
				"Please tell us who you are in case we need to contact you for more details."
				+ "\nThank you for helping us improve our software!");
		GridDataFactory.fillDefaults().hint(niceWidth, SWT.DEFAULT).grab(true, false).applyTo(nameLabel);

		name = new Text(nameGroup, SWT.MULTI | SWT.BORDER);
		GridDataFactory.fillDefaults().hint(niceWidth, SWT.DEFAULT).grab(true, false).applyTo(name);

		Label helpLabel = new Label(nameGroup, SWT.WRAP);
		helpLabel.setText(URGENT_HELP_MESSAGE);
		helpLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
		GridDataFactory.fillDefaults().hint(niceWidth, SWT.DEFAULT).grab(true, false).applyTo(helpLabel);

		finishedOption.setSelection(true);

		feedback.setEnabled(false);
		name.setEnabled(false);

		nameError = new Label(this, SWT.NONE);
		nameError.setText("Please fill in the name box if you're leaving feedback!");
		nameError.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		nameError.setVisible(false);
		
		name.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (nameError.isVisible() && !name.getText().isEmpty()) {
					nameError.setVisible(false);
				}
			}
		});
		
		addTabListener(feedback);
		addTabListener(name);

	}

	private void addTabListener(Text textbox) {
		textbox.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		});
	}

	private Button optionButton(Composite parent, String text, int labelColor) {
		Button button = new Button(parent, SWT.RADIO);
		GridDataFactory.swtDefaults().applyTo(button);
		
		Label label = new Label(parent, SWT.WRAP);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(label);
		label.setText(text);
		label.setForeground(getDisplay().getSystemColor(labelColor));
		
		return button;
	}

	private SelectionAdapter createListener(final ClientCloseOption option, final boolean activateFeedback) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedOption = option;
				feedback.setEnabled(activateFeedback);
				name.setEnabled(activateFeedback);
				if (activateFeedback) {
					feedback.setFocus();
				}
			}
		};
	}

	public boolean validate() {
		boolean isValid = feedback.isEnabled() ? !(getRestartReason().isEmpty() ^ getNameField().isEmpty()) : true;
		if (!isValid) {
			nameError.setVisible(true);
		}
		return isValid;
	}

	public String getRestartReason() {
		return feedback.getText();
	}

	public String getNameField() {
		return name.getText();
	}

	public ClientCloseOption selectedOption() {
		return selectedOption;
	}
}