/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.rcp.views;

import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;
import gda.jython.gui.AutoCompletionGetter;
import gda.jython.gui.AutoCompletionParts;
import gda.rcp.GDAClientActivator;
import gda.rcp.ImageConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoCompleter {

	private ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();

	private final Text txtInput;

	private static final Logger logger = LoggerFactory.getLogger(AutoCompleter.class);

	private JythonTerminalContentProposalProvider contentProposalProvider;

	private String postFix;

	private String wordsBefore;

	private String prefixOfLastWord;

	private ContentProposalAdapter adapter;

	public AutoCompleter(final Text txtInput) {
		this.txtInput = txtInput;

		txtInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final org.eclipse.swt.events.KeyEvent e) {
				if (((e.stateMask & SWT.CTRL) != 0) && e.keyCode == ' ') {
					List<IContentProposal> proposals = getProposals();
					if (proposals != null && proposals.size() == 1) {
						if (!txtInput.getText().trim().equals(proposals.get(0).getContent())) {
							contentProposalListener.proposalAccepted(proposals.get(0));
						}
					} else {
						handleModify();
					}
				} else if (e.keyCode == ' ' && adapter.isProposalPopupOpen()) {
					// If the "space" key is pressed when the proposal popup is open then autocomplete it with the top
					// most entry.
					List<IContentProposal> proposals = getProposals();
					if (!proposals.isEmpty()) {
						contentProposalListener.proposalAccepted(proposals.get(0));
						adapter.closeProposalPopup();
					}
				}
			}
		});
		
		try {
			installContentProposalAdapter(txtInput, new TextContentAdapter());
		} catch (ParseException e) {
			logger.error("Problem installing content proposal adapter", e);
		}
	}

	void installContentProposalAdapter(Control control, IControlContentAdapter contentAdapter) throws ParseException {
		boolean propagate = true;
		char[] autoActivationCharacters = null;
		int autoActivationDelay = 0;

		KeyStroke ctrlSpaceKeyStroke = KeyStroke.getInstance("Ctrl+Space");

		adapter = new ContentProposalAdapter(control, contentAdapter, getContentProposalProvider(), ctrlSpaceKeyStroke,
				autoActivationCharacters);
		adapter.setInfoPopupRequired(false);
		adapter.addContentProposalListener(contentProposalListener);
		adapter.setLabelProvider(prv);
		adapter.setAutoActivationDelay(autoActivationDelay);
		adapter.setPropagateKeys(propagate);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
	}

	private IContentProposalListener contentProposalListener = new IContentProposalListener() {

		@Override
		public void proposalAccepted(IContentProposal proposal) {
			finishAutoCompletion(proposal.getLabel());
		}
	};

	private ILabelProvider prv = new LabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof IContentProposal) {
				return ((IContentProposal) element).getLabel();
			} else if (element instanceof String) {
				return element.toString();
			}
			return null;
		}

		@Override
		public org.eclipse.swt.graphics.Image getImage(Object element) {
			if (element instanceof ResponseContentProposal) {
				int type = ((ResponseContentProposal) element).getType();
				switch (type) {
				case 0:
					return GDAClientActivator.getDefault().getImageRegistry()
							.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_0);
				case 1:
					return GDAClientActivator.getDefault().getImageRegistry()
							.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_1);
				case 2:
					return GDAClientActivator.getDefault().getImageRegistry()
							.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_2);
				case 3:
					return GDAClientActivator.getDefault().getImageRegistry()
							.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_3);
				case 4:
					return GDAClientActivator.getDefault().getImageRegistry()
							.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_4);
				}
			}
			return null;
		}
	};

	private IContentProposalProvider getContentProposalProvider() {
		if (contentProposalProvider == null) {
			contentProposalProvider = new JythonTerminalContentProposalProvider(getProposals());
		}
		return contentProposalProvider;
	}

	protected void handleModify() {
		List<IContentProposal> proposals = getProposals();
		contentProposalProvider.setProposals(proposals);
	}

	private List<IContentProposal> getProposals() {
		postFix = "";
		// get last word on which to prompt for completion
		String lastWord = getLastWord();
		List<AutoCompletionParts> options = (new AutoCompletionGetter(commandRunner, lastWord)).getOptions();

		List<IContentProposal> proposals = new ArrayList<IContentProposal>();
		for (AutoCompletionParts autoCompletionParts : options) {
			proposals.add(new ResponseContentProposal(prefixOfLastWord + autoCompletionParts.name,
					autoCompletionParts.name, autoCompletionParts.helpDoc, autoCompletionParts.type));
		}
		Collections.sort(proposals, contentComparator);
		return proposals;
	}

	private Comparator<IContentProposal> contentComparator = new Comparator<IContentProposal>() {

		@Override
		public int compare(IContentProposal o1, IContentProposal o2) {
			String lbl1 = StringUtils.swapCase(o1.getLabel());
			String lbl2 = StringUtils.swapCase(o2.getLabel());
			return lbl1.compareTo(lbl2);
		}
	};

	public String getLastWord() {
		String lastWord = "";
		{
			String currentInput = txtInput.getText();
			int caretPos = txtInput.getCaretPosition();
			if (caretPos < currentInput.length()) {
				postFix = currentInput.substring(caretPos);
				currentInput = currentInput.substring(0, caretPos);
			}
			String[] parts = currentInput.split(" ");
			if (parts.length == 0) {
				return null;
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

		/* split the last word if it contains open parenthesis i.e. '(' */
		if (lastWord.endsWith("(")) {
			wordsBefore += lastWord;
			lastWord = "";
		} else {
			/* split the last word if it contains open parenthesis i.e. '(' */
			String[] parts = lastWord.split("\\(");
			if (parts.length > 1) {
				for (int i = 0; i < parts.length - 1; i++) {
					wordsBefore += parts[i] + "(";
				}
				lastWord = parts[parts.length - 1];
			}
			/* split the last word if it contains = sign */
			parts = lastWord.split("=");
			if (parts.length > 1) {
				for (int i = 0; i < parts.length - 1; i++) {
					wordsBefore += parts[i] + "=";
				}
				lastWord = parts[parts.length - 1];
			}
		}

		if (lastWord.isEmpty()) {
			return null;
		}

		prefixOfLastWord = "";

		int lastDot = lastWord.lastIndexOf(".");
		if (lastDot >= 0) {
			prefixOfLastWord = lastWord.substring(0, lastDot + 1);
		}
		return lastWord;
	}

	/*
	 * called by either the popup menu or directly
	 */
	private void finishAutoCompletion(String name) {
		if (name != null) {
			String replacement = name;
			final String newtext = wordsBefore + prefixOfLastWord + replacement + postFix;
			txtInput.setText(newtext);
			txtInput.setFocus();
			txtInput.setSelection(txtInput.getCharCount() + 1, txtInput.getCharCount() + 1);
		}
	}

	private class JythonTerminalContentProposalProvider implements IContentProposalProvider {

		/*
		 * The proposals mapped to IContentProposal. Cached for speed in the case where filtering is not used.
		 */
		private List<IContentProposal> contentProposals;

		/**
		 * Construct a SimpleContentProposalProvider whose content proposals are always the specified array of Objects.
		 */
		public JythonTerminalContentProposalProvider(List<IContentProposal> proposals) {
			super();
			this.contentProposals = proposals;
		}

		/**
		 * Return an array of Objects representing the valid content proposals for a field.
		 * 
		 * @param contents
		 *            the current contents of the field (only consulted if filtering is set to <code>true</code>)
		 * @param position
		 *            the current cursor position within the field (ignored)
		 * @return the array of Objects that represent valid proposals for the field given its current content.
		 */
		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			contents = getLastWord();
			ArrayList<IContentProposal> list = new ArrayList<IContentProposal>();
			if (contentProposals != null && contents != null) {
				for (IContentProposal contentProposal : contentProposals) {
					if (contentProposal.getContent().length() >= contents.length()
							&& contentProposal.getContent().substring(0, contents.length()).equalsIgnoreCase(contents)) {
						list.add(contentProposal);
					}
				}
			}
			return list.toArray(new IContentProposal[list.size()]);
		}

		/**
		 * Set the Strings to be used as content proposals.
		 * 
		 * @param items
		 *            the array of Strings to be used as proposals.
		 */
		public void setProposals(List<IContentProposal> items) {
			contentProposals = items;
		}
	}

	public void dispose() {
		if (adapter != null) {
			adapter.removeContentProposalListener(contentProposalListener);
		}
	}
}