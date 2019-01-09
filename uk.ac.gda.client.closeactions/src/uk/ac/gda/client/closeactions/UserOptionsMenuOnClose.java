package uk.ac.gda.client.closeactions;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

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
	private ClientCloseOption selectedOption = ClientCloseOption.TEMP_ABSENCE;

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

		final Button option1 = optionButton(buttonComposite, "I'm finished for now - but I or a colleague will be back soon (no action)");
		option1.addSelectionListener(createListener(ClientCloseOption.TEMP_ABSENCE, false));

		final Button option2 = optionButton(buttonComposite, "I need to restart the client (Please tell us why)");
		option2.addSelectionListener(createListener(ClientCloseOption.RESTART_CLIENT, true));

		final Button option3 = optionButton(buttonComposite, "I need to restart the client and the server (Please tell us why)");
		option3.addSelectionListener(createListener(ClientCloseOption.RESTART_CLIENT_AND_SERVER, true));

		final Button option4 = optionButton(buttonComposite,
				"I'm finished for this visit, the hutch is searched and locked (if on-site) and I have or am about to inform the EHC on +44 1235 77 87 87.");
		option4.addSelectionListener(createListener(ClientCloseOption.FINISHED, false));

		feedback = new Text(selectionGroup, SWT.MULTI | SWT.BORDER);
		GridDataFactory.fillDefaults().hint(niceWidth, 200).grab(true, true).applyTo(feedback);

		Composite nameGroup = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(15, 10).applyTo(nameGroup);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameGroup);

		Label nameLabel = new Label(nameGroup, SWT.WRAP);
		nameLabel.setText("Please tell us who you are in case we need to contact you for more details. This will help us improve our software.");
		GridDataFactory.fillDefaults().hint(niceWidth, SWT.DEFAULT).grab(true, false).applyTo(nameLabel);
		
		name = new Text(nameGroup, SWT.MULTI | SWT.BORDER);
		GridDataFactory.fillDefaults().hint(niceWidth, SWT.DEFAULT).grab(true, false).applyTo(name);

		option1.setSelection(true);
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
	}

	private Button optionButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.RADIO);
		GridDataFactory.swtDefaults().applyTo(button);
		
		Label label = new Label(parent, SWT.WRAP);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(label);
		label.setText(text);
		
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