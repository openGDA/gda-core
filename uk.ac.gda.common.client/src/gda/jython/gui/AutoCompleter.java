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

package gda.jython.gui;

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
 * Adds a autocompletion capabilities to a JTextComponent. Responds to CTRL-SPACE keystroke Requires the completer
 * object to exist in the namespace
 */
public class AutoCompleter {
	private JPopupMenu popupMenu;
	private final JTextComponent textField;
	private JythonServerFacade commandserver;
	private String wordsBefore = ""; // words up to the word on which the prompt is made
	private String prefixOfLastWord=""; // string that is to be replaced by the users selection
	private String postFix=""; // string beyond the caret position at which the CTRL-ENTER was pressed
	/**
	 * @param commandserver
	 */
	public void setJythonServerFacade(JythonServerFacade commandserver) {
		this.commandserver = commandserver;
	}
	private boolean addArgs;
	/**
	 * @param textField
	 * @param parentFrame
	 */
	public AutoCompleter(final JTextComponent textField, final JPanel parentFrame) {
		this.textField = textField;
		this.commandserver = null;
		popupMenu = new JPopupMenu();
		parentFrame.add(popupMenu);// this is needed for the space and return keys to cause a selection

		// We need to make this an option
		// ActionListener actionListenerPeriod = new ActionListener() {
		// public void actionPerformed(ActionEvent actionEvent) {
		// listAutoCompletions(".");
		// }
		// };
		// KeyStroke keystrokePeriod = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0, false);
		// textField.registerKeyboardAction(actionListenerPeriod, keystrokePeriod, JComponent.WHEN_FOCUSED);

		ActionListener actionListenerSpace = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				listAutoCompletions("",false);
			}
		};
		KeyStroke keystrokeSpace = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK, false);
		textField.registerKeyboardAction(actionListenerSpace, keystrokeSpace, JComponent.WHEN_FOCUSED);

		ActionListener actionListenerSpaceShift = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				listAutoCompletions("", true);
			}
		};
		keystrokeSpace = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, false);
		textField.registerKeyboardAction(actionListenerSpaceShift, keystrokeSpace, JComponent.WHEN_FOCUSED);
		
		FocusListener focusListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// we need to set the caret after getting the focus
				textField.setCaretPosition(textField.getText().length());
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		};
		textField.addFocusListener(focusListener);
	}

	void listAutoCompletions(String keyPress, boolean addArgs) {
		this.addArgs = addArgs;
		postFix="";
		if (commandserver == null)
			return;
		// get last word on which to prompt for completion
		String lastWord ="";
		{
			String currentInput = textField.getText();
			int caretPos = textField.getCaretPosition();
			if( caretPos < currentInput.length()){
				postFix = currentInput.substring(caretPos);
				currentInput = currentInput.substring(0,caretPos) + keyPress;
			}
			String[] parts = currentInput.split(" ");
			if (parts.length == 0) {
				return;
			}
			// set the string of words up to the last word
			wordsBefore = "";
			if (parts.length > 1) {
				for (int i = 0; i < parts.length - 1; i++) {
					wordsBefore += parts[i] + " ";
				}
			}
			lastWord = parts[parts.length - 1];
		}
		{
			/* split the last word if it contains open parenthesis i.e. '(' */
			if( lastWord.endsWith("(")){
				wordsBefore +=lastWord;
				lastWord="";
			} else {
				String[] parts = lastWord.split("\\(");
				if (parts.length > 1 ) {
					for (int i = 0; i < parts.length - 1; i++) {
						wordsBefore += parts[i] + "(";
					}
					lastWord = parts[parts.length - 1];
				}
			}
		}
		if(lastWord.isEmpty())
			return;

		/*
		 * set the string that is to be replaced by the users selection - this is substring after the last period
		 */
		prefixOfLastWord = "";
		
		
		
		int lastDot = lastWord.lastIndexOf(".");
		if (lastDot >= 0) {
			prefixOfLastWord = lastWord.substring(0, lastDot + 1);
		}

		List<AutoCompletionParts> options = (new AutoCompletionGetter(commandserver, lastWord)).getOptions();
		if (options != null && options.size() > 0) {
			if (options.size() == 1) {
				finishAutoCompletion(options.get(0));
			} else {
				popupMenu.removeAll();
				for (AutoCompletionParts option : options) {
					Action action = new CompletionAction(option, this);
					JMenuItem menuItem = new JMenuItem(action);
//					menuItem.addActionListener(this);
					popupMenu.add(menuItem);
				}
				popupMenu.show(textField, 0, 0);
			}
		}
	}

	/*
	 * called by either the popup menu or directly
	 */
	void finishAutoCompletion(AutoCompletionParts optionChosen) {
		if (optionChosen != null) {
			String replacement = optionChosen.name;
			if( addArgs && optionChosen.type == 2 && postFix.isEmpty()){
				replacement += optionChosen.args;
			}
			final String newtext = wordsBefore + prefixOfLastWord + replacement + postFix;
			textField.setText(newtext);
			textField.setCaretPosition(newtext.length());
			textField.requestFocusInWindow();
			if(addArgs){
				String help = optionChosen.helpDoc;
				if(help.isEmpty()){
					help = optionChosen.name + "\n" + commandserver.evaluateCommand(optionChosen.name + ".__doc__");
				}
				InterfaceProvider.getTerminalPrinter().print(help);
			}
		}
		popupMenu.setVisible(false);
	}

}

class CompletionAction extends AbstractAction{
	AutoCompleter completer;
	AutoCompletionParts part;
	CompletionAction(AutoCompletionParts part, AutoCompleter completer){
		this.completer = completer;
		this.part = part;
		putValue(Action.NAME, part.name);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		completer.finishAutoCompletion(part);
	}
}