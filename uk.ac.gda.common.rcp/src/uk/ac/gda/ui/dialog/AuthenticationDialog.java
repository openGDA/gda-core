/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.dialog;

//use to open dialog box from Application~Workbench


import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
	 *A dialog box to log in to the GDA by either the current OS login or by specifying their username and password.
	 */
public final class AuthenticationDialog extends GenericDialog {

	private Text passwordText;
	private Text usernameText;
	protected Object result;
    private  Group group;
	private String userText;
    private String passText;
    private Button automaticButton = null;
    private Boolean automatic = true;
    private Label incorrectUserPassLabel;
    private String userID;
	private String workspacePath;
    
	/**
	 * @param disp 
	 * @param style
	 * @param title 
	 * @param user 
	 */
	public AuthenticationDialog(Display disp, int style, String title, String user, final String workspacePath) {
		super(new Shell(disp), style);
		this.setText(title);
		currentSelection = new Object();
		userID = user;
		this.workspacePath = workspacePath;
	}

	/**
	 * @param shell
	 * @param userObject
	 */
	@Override
	public void createContents(final Shell shell, final Object userObject) {

		final Composite main = new Composite(shell, SWT.NONE);
		main.setLayout(new GridLayout());
		main.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		automaticButton = new Button(main, SWT.CHECK);
		automaticButton.setSelection(true);
		automaticButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				automatic = !automatic;
				group.setVisible(!automaticButton.getSelection());
				((GridData)group.getLayoutData()).exclude = automaticButton.getSelection();
				if(automatic)
					incorrectUserPassLabel.setText("");
				else
					incorrectUserPassLabel.setVisible(true);
				shell.layout();
			}
		});
		automaticButton.setToolTipText("Check to use the same logon as your OS");
		automaticButton.setText("Log in as "+userID);

		this.group = new Group(main, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		group.setLayout(gridLayout);
		final GridData gd_group = new GridData(SWT.FILL, SWT.CENTER, true, false);
		group.setLayoutData(gd_group);
		gd_group.minimumWidth = 310;
		gd_group.exclude = true;
		
		final Label usernameLabel = new Label(group, SWT.NONE);
		usernameLabel.setText("Username:");
		
		usernameText = new Text(group, SWT.BORDER);
		usernameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				userText = usernameText.getText();
			}
		});
		usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Label passwordLabel = new Label(group, SWT.NONE);
		passwordLabel.setText("Password:");
		
		passwordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
		passwordText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				passText = passwordText.getText();
			}
		});
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		group.setVisible(false);

		incorrectUserPassLabel = new Label(main, SWT.SHADOW_NONE);
		incorrectUserPassLabel.setVisible(true);
		incorrectUserPassLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		incorrectUserPassLabel.setRedraw(true);
		
		incorrectUserPassLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		incorrectUserPassLabel.setText("");
		
		if (workspacePath!=null) {
			final Button setWorkspace = new Button(main, SWT.CHECK);
			setWorkspace.setText("Use default workspace");
			setWorkspace.setSelection(true);
		
			final Composite dirComp = new Composite(main, SWT.NONE);
			dirComp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
			
			final DirectoryFieldEditor fileChoice = new DirectoryFieldEditor("workspace.folder", "Workspace", dirComp);
			fileChoice.setStringValue(workspacePath);
			dirComp.setVisible(false);
			((GridData)dirComp.getLayoutData()).exclude = true;
		
			setWorkspace.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					dirComp.setVisible(!setWorkspace.getSelection());
					((GridData)dirComp.getLayoutData()).exclude = setWorkspace.getSelection();
					shell.layout();
				}
			});
	
			fileChoice.setPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals("field_editor_value")) {
						workspacePath = (String)event.getNewValue();
					}
				}
			});
		}
		
		// We use the golden ratio here to make the form
		// appear a nice size.
		shell.setSize((int)(220*1.6180339887), 220);
	}
	
	/**
	 * @return String
	 */
	public String getUsername(){
		return userText;
	}
	
	/**
	 * @return String
	 */
	public String getPassword(){
		return passText;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isAutomatic(){
		return automatic;
	}
	
	/**
	 * @param message
	 */
	public void setErrorMessage(String message){
		incorrectUserPassLabel.setText(message);
		incorrectUserPassLabel.getParent().layout();
		
	}
	
	/**
	 * @return boolean
	 */
	@Override
	public boolean shouldPack() {
		return false;
	}

	public String getWorkspacePath() {
		return workspacePath;
	}

}
