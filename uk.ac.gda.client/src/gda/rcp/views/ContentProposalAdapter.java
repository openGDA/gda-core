/**
 * <pre>
 * *****************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Hannes Erven <hannes@erven.at> - Bug 293841 - [FieldAssist] NumLock keyDown event should not close the proposal popup [with patch]
 ******************************************************************************
 * </pre>
 **/

package gda.rcp.views;

import static java.util.Collections.emptySet;
import static org.eclipse.swt.events.SelectionListener.widgetDefaultSelectedAdapter;

import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * ContentProposalAdapter can be used to attach content proposal behavior to a control. This behavior includes obtaining
 * proposals, opening a popup dialog, managing the content of the control relative to the selections in the popup, and
 * optionally opening up a secondary popup to further describe proposals.
 * <p>
 * This class provides some overridable methods to allow clients to manually control the popup. However, most of the
 * implementation remains private.
 *
 * @since 3.2 * @author rsr31645 - R Somayaji - Had to copy this class from jface because there was no way to control
 *        the display of infopopup. Once GDA requirement have sorted out the need to use the info popup (the tooltip
 *        that appears next to the content assist) then GDA can delete this class and use the
 *        org.eclipse.jface.fieldassist.ContentProposalAdapter directly instead.
 */
public class ContentProposalAdapter {

	private static final IContentProposal[] NO_PROPOSALS = new IContentProposal[0];

	/*
	 * The lightweight popup used to show content proposals for a text field. If additional information exists for a
	 * proposal, then selecting that proposal will result in the information being displayed in a secondary popup.
	 */
	class ContentProposalPopup extends PopupDialog {
		/*
		 * The listener we install on the popup and related controls to determine when to close the popup. Some events
		 * (move, resize, close, deactivate) trigger closure as soon as they are received, simply because one of the
		 * registered listeners received them. Other events depend on additional circumstances.
		 */
		private final class PopupCloserListener implements Listener {
			private boolean scrollbarClicked = false;

			@Override
			public void handleEvent(final Event e) {

				// If focus is leaving an important widget or the field's
				// shell is deactivating
				if (e.type == SWT.FocusOut) {
					scrollbarClicked = false;
					/*
					 * Ignore this event if it's only happening because focus is moving between the popup shells, their
					 * controls, or a scrollbar. Do this in an async since the focus is not actually switched when this
					 * event is received.
					 */
					e.display.asyncExec(() -> {
						if (isValid()) {
							if (scrollbarClicked || hasFocus()) {
								return;
							}
							// Workaround a problem on X and Mac, whereby at
							// this point, the focus control is not known.
							// This can happen, for example, when resizing
							// the popup shell on the Mac.
							// Check the active shell.
							Shell activeShell = e.display.getActiveShell();
							if (activeShell == getShell()) {
								return;
							}
							close();
						}
					});
					return;
				}

				// Scroll bar has been clicked. Remember this for focus event
				// processing.
				if (e.type == SWT.Selection) {
					scrollbarClicked = true;
					return;
				}
				// For all other events, merely getting them dictates closure.
				close();
			}

			// Install the listeners for events that need to be monitored for
			// popup closure.
			void installListeners() {
				// Listeners on this popup's table and scroll bar
				proposalTable.addListener(SWT.FocusOut, this);
				ScrollBar scrollbar = proposalTable.getVerticalBar();
				if (scrollbar != null) {
					scrollbar.addListener(SWT.Selection, this);
				}

				// Listeners on this popup's shell
				getShell().addListener(SWT.Deactivate, this);
				getShell().addListener(SWT.Close, this);

				// Listeners on the target control
				control.addListener(SWT.MouseDoubleClick, this);
				control.addListener(SWT.MouseDown, this);
				control.addListener(SWT.Dispose, this);
				control.addListener(SWT.FocusOut, this);
				// Listeners on the target control's shell
				Shell controlShell = control.getShell();
				controlShell.addListener(SWT.Move, this);
				controlShell.addListener(SWT.Resize, this);

			}

			// Remove installed listeners
			void removeListeners() {
				if (isValid()) {
					proposalTable.removeListener(SWT.FocusOut, this);
					ScrollBar scrollbar = proposalTable.getVerticalBar();
					if (scrollbar != null) {
						scrollbar.removeListener(SWT.Selection, this);
					}

					getShell().removeListener(SWT.Deactivate, this);
					getShell().removeListener(SWT.Close, this);
				}

				if (control != null && !control.isDisposed()) {

					control.removeListener(SWT.MouseDoubleClick, this);
					control.removeListener(SWT.MouseDown, this);
					control.removeListener(SWT.Dispose, this);
					control.removeListener(SWT.FocusOut, this);

					Shell controlShell = control.getShell();
					controlShell.removeListener(SWT.Move, this);
					controlShell.removeListener(SWT.Resize, this);
				}
			}
		}

		/*
		 * The listener we will install on the target control.
		 */
		private final class TargetControlListener implements Listener {
			// Key events from the control
			@Override
			public void handleEvent(Event e) {
				if (!isValid()) {
					return;
				}

				char key = e.character;

				// Traverse events are handled depending on whether the
				// event has a character.
				if (e.type == SWT.Traverse) {
					// If the traverse event contains a legitimate character,
					// then we must set doit false so that the widget will
					// receive the key event. We return immediately so that
					// the character is handled only in the key event.
					// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=132101
					if (key != 0) {
						e.doit = false;
						return;
					}
					// Traversal does not contain a character. Set doit true
					// to indicate TRAVERSE_NONE will occur and that no key
					// event will be triggered. We will check for navigation
					// keys below.
					e.detail = SWT.TRAVERSE_NONE;
					e.doit = true;
				} else {
					// Default is to only propagate when configured that way.
					// Some keys will always set doit to false anyway.
					e.doit = true;
				}

				// No character. Check for navigation keys.

				if (key == 0) {
					int newSelection = proposalTable.getSelectionIndex();
					int visibleRows = (proposalTable.getSize().y / proposalTable.getItemHeight()) - 1;
					switch (e.keyCode) {
					case SWT.ARROW_UP:
						newSelection -= 1;
						if (newSelection < 0) {
							newSelection = proposalTable.getItemCount() - 1;
						}
						// Not typical - usually we get this as a Traverse and
						// therefore it never propagates. Added for consistency.
						if (e.type == SWT.KeyDown) {
							// don't propagate to control
							e.doit = false;
						}

						break;

					case SWT.ARROW_DOWN:
						newSelection += 1;
						if (newSelection > proposalTable.getItemCount() - 1) {
							newSelection = 0;
						}
						// Not typical - usually we get this as a Traverse and
						// therefore it never propagates. Added for consistency.
						if (e.type == SWT.KeyDown) {
							// don't propagate to control
							e.doit = false;
						}

						break;

					case SWT.PAGE_DOWN:
						newSelection += visibleRows;
						if (newSelection >= proposalTable.getItemCount()) {
							newSelection = proposalTable.getItemCount() - 1;
						}
						if (e.type == SWT.KeyDown) {
							// don't propagate to control
							e.doit = false;
						}
						break;

					case SWT.PAGE_UP:
						newSelection -= visibleRows;
						if (newSelection < 0) {
							newSelection = 0;
						}
						if (e.type == SWT.KeyDown) {
							// don't propagate to control
							e.doit = false;
						}
						break;

					case SWT.HOME:
						newSelection = 0;
						if (e.type == SWT.KeyDown) {
							// don't propagate to control
							e.doit = false;
						}
						break;

					case SWT.END:
						newSelection = proposalTable.getItemCount() - 1;
						if (e.type == SWT.KeyDown) {
							// don't propagate to control
							e.doit = false;
						}
						break;

					// If received as a Traverse, these should propagate
					// to the control as keydown. If received as a keydown,
					// proposals should be recomputed since the cursor
					// position has changed.
					case SWT.ARROW_LEFT,  SWT.ARROW_RIGHT:
						if (e.type == SWT.Traverse) {
							e.doit = false;
						} else {
							e.doit = true;
							String contents = getControlContentAdapter().getControlContents(getControl());
							// If there are no contents, changes in cursor
							// position have no effect.
							if (contents.length() > 0) {
								asyncRecomputeProposals();
							}
						}
						break;

					// Any unknown keycodes will cause the popup to close.
					// Modifier keys are explicitly checked and ignored because
					// they are not complete yet (no character).
					default:
						if (e.keyCode != SWT.CAPS_LOCK && e.keyCode != SWT.NUM_LOCK && e.keyCode != SWT.MOD1
								&& e.keyCode != SWT.MOD2 && e.keyCode != SWT.MOD3 && e.keyCode != SWT.MOD4) {
							close();
						}
						return;
					}

					// If any of these navigation events caused a new selection,
					// then handle that now and return.
					if (newSelection >= 0) {
						selectProposal(newSelection);
					}
					return;
				}

				// key != 0
				// Check for special keys involved in cancelling or accepting the proposals.
				switch (key) {
				case SWT.ESC:
					e.doit = false;
					close();
					break;
				case SWT.BS:
					// There is no filtering provided by us, but some
					// clients provide their own filtering based on content.
					// Recompute the proposals if the cursor position
					// will change (is not at 0).
					int pos = getControlContentAdapter().getCursorPosition(getControl());
					// We rely on the fact that the contents and pos do not yet
					// reflect the result of the BS. If the contents were
					// already empty, then BS should not cause
					// a recompute.
					if (pos > 0) {
						asyncRecomputeProposals();
					}
					break;

				default:
					if (acceptKeys.contains(key)) {
						e.doit = false;
						Object p = getSelectedProposal();
						if (p != null) {
							acceptCurrentProposal();
						} else {
							close();
						}
					} else if (Character.isDefined(key)) {
						// If the key is a defined unicode character, and not one of
						// the special cases processed above, update the proposals.
						// Recompute proposals after processing this event.
						asyncRecomputeProposals();
					}
				}
			}
		}

		/*
		 * The listener installed on the target control.
		 */
		private Listener targetControlListener;

		/*
		 * The listener installed in order to close the popup.
		 */
		private PopupCloserListener popupCloser;

		/*
		 * The table used to show the list of proposals.
		 */
		private Table proposalTable;

		/*
		 * The proposals to be shown (cached to avoid repeated requests).
		 */
		private IContentProposal[] proposals;

		/**
		 * Constructs a new instance of this popup, specifying the control for which this popup is showing content, and
		 * how the proposals should be obtained and displayed.
		 *
		 * @param infoText
		 *            Text to be shown in a lower info area, or <code>null</code> if there is no info area.
		 */
		ContentProposalPopup(String infoText, IContentProposal[] proposals) {
			// IMPORTANT: Use of SWT.ON_TOP is critical here for ensuring
			// that the target control retains focus on Mac and Linux. Without
			// it, the focus will disappear, keystrokes will not go to the
			// popup, and the popup closer will wrongly close the popup.
			// On platforms where SWT.ON_TOP overrides SWT.RESIZE, we will live
			// with this.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=126138
			super(control.getShell(), SWT.RESIZE | SWT.ON_TOP, false, false, false, false, false, null, infoText);
			this.proposals = proposals;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.PopupDialog#getForeground()
		 */
		@Override
		protected Color getForeground() {
			return JFaceResources.getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.PopupDialog#getBackground()
		 */
		@Override
		protected Color getBackground() {
			return JFaceResources.getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
		}

		/*
		 * Creates the content area for the proposal popup. This creates a table and places it inside the composite. The
		 * table will contain a list of all the proposals.
		 * @param parent The parent composite to contain the dialog area; must not be <code>null</code>.
		 */
		@Override
		protected final Control createDialogArea(final Composite parent) {
			// Use virtual where appropriate (see flag definition).
			if (USE_VIRTUAL) {
				proposalTable = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);

				Listener listener = this::handleSetData;
				proposalTable.addListener(SWT.SetData, listener);
			} else {
				proposalTable = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL);
			}

			// set the proposals to force population of the table.
			setProposals(proposals);

			proposalTable.setHeaderVisible(false);
			proposalTable.addSelectionListener(widgetDefaultSelectedAdapter(e -> acceptCurrentProposal()));
			return proposalTable;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.PopupDialog.adjustBounds()
		 */
		@Override
		protected void adjustBounds() {
			// Get our control's location in display coordinates.
			Point location = control.getDisplay().map(control.getParent(), null, control.getLocation());
			int initialX = location.x + POPUP_OFFSET;
			int initialY = location.y + control.getSize().y + POPUP_OFFSET;

			// If there is no specified size, force it by setting
			// up a layout on the table.
			if (popupSize == null) {
				GridData data = new GridData(GridData.FILL_BOTH);
				data.heightHint = proposalTable.getItemHeight() * POPUP_CHAR_HEIGHT;
				data.widthHint = Math.max(control.getSize().x, POPUP_MINIMUM_WIDTH);
				proposalTable.setLayoutData(data);
				getShell().pack();
				popupSize = getShell().getSize();
			}

			// Constrain to the display
			Rectangle constrainedBounds = getConstrainedShellBounds(new Rectangle(initialX, initialY, popupSize.x,
					popupSize.y));

			// If there has been an adjustment causing the popup to overlap
			// with the control, then put the popup above the control.
			if (constrainedBounds.y < initialY)
				getShell().setBounds(initialX, location.y - popupSize.y, popupSize.x, popupSize.y);
			else
				getShell().setBounds(initialX, initialY, popupSize.x, popupSize.y);

			// Now set up a listener to monitor any changes in size.
			getShell().addListener(SWT.Resize, e -> popupSize = getShell().getSize());
		}

		/*
		 * Handle the set data event. Set the item data of the requested item to the corresponding proposal in the
		 * proposal cache.
		 */
		private void handleSetData(Event event) {
			TableItem item = (TableItem) event.item;
			int index = proposalTable.indexOf(item);

			if (0 <= index && index < proposals.length) {
				IContentProposal current = proposals[index];
				item.setText(getString(current));
				item.setImage(getImage(current));
				item.setData(current);
			} else {
				// this should not happen, but does on win32
			}
		}

		/*
		 * Caches the specified proposals and repopulates the table if it has been created.
		 */
		private void setProposals(IContentProposal[] newProposals) {
			if (newProposals == null || newProposals.length == 0) {
				newProposals = NO_PROPOSALS;
			}
			this.proposals = newProposals;

			// If there is a table
			if (isValid()) {
				final int newSize = newProposals.length;
				if (USE_VIRTUAL) {
					// Set and clear the virtual table. Data will be
					// provided in the SWT.SetData event handler.
					proposalTable.setItemCount(newSize);
					proposalTable.clearAll();
				} else {
					// Populate the table manually
					proposalTable.setRedraw(false);
					proposalTable.setItemCount(newSize);
					TableItem[] items = proposalTable.getItems();
					for (int i = 0; i < items.length; i++) {
						TableItem item = items[i];
						IContentProposal proposal = newProposals[i];
						item.setText(getString(proposal));
						item.setImage(getImage(proposal));
						item.setData(proposal);
					}
					proposalTable.setRedraw(true);
				}
				// Default to the first selection if there is content.
				if (newProposals.length > 0) {
					selectProposal(0);
				}
			}
		}

		/*
		 * Get the string for the specified proposal. Always return a String of some kind.
		 */
		private String getString(IContentProposal proposal) {
			if (proposal == null) {
				return EMPTY;
			}
			if (labelProvider == null) {
				return proposal.getLabel() == null ? proposal.getContent() : proposal.getLabel();
			}
			return labelProvider.getText(proposal);
		}

		/*
		 * Get the image for the specified proposal. If there is no image available, return null.
		 */
		private Image getImage(IContentProposal proposal) {
			if (proposal == null || labelProvider == null) {
				return null;
			}
			return labelProvider.getImage(proposal);
		}

		/*
		 * Answer true if the popup is valid, which means the table has been created and not disposed.
		 */
		private boolean isValid() {
			return proposalTable != null && !proposalTable.isDisposed();
		}

		/*
		 * Return whether the receiver has focus. Since 3.4, this includes a check for whether the info popup has focus.
		 */
		private boolean hasFocus() {
			return isValid() && getShell().isFocusControl() || proposalTable.isFocusControl();
		}

		/*
		 * Return the current selected proposal.
		 */
		private IContentProposal getSelectedProposal() {
			if (isValid()) {
				int i = proposalTable.getSelectionIndex();
				if (proposals == null || i < 0 || i >= proposals.length) {
					return null;
				}
				return proposals[i];
			}
			return null;
		}

		/*
		 * Select the proposal at the given index.
		 */
		private void selectProposal(int index) {
			Assert.isTrue(index >= 0, "Proposal index should never be negative"); //$NON-NLS-1$
			if (!isValid() || proposals == null || index >= proposals.length) {
				return;
			}
			proposalTable.setSelection(index);
			proposalTable.showSelection();
		}

		/**
		 * Opens this ContentProposalPopup. This method is extended in order to add the control listener when the popup
		 * is opened and to invoke the secondary popup if applicable.
		 *
		 * @return the return code
		 * @see org.eclipse.jface.window.Window#open()
		 */
		@Override
		public int open() {
			int value = super.open();
			if (popupCloser == null) {
				popupCloser = new PopupCloserListener();
			}
			popupCloser.installListeners();
			return value;
		}

		/**
		 * Closes this popup. This method is extended to remove the control listener.
		 *
		 * @return <code>true</code> if the window is (or was already) closed, and <code>false</code> if it is still
		 *         open
		 */
		@Override
		public boolean close() {
			popupCloser.removeListeners();
			return super.close();
		}

		/*
		 * Accept the current proposal.
		 */
		private void acceptCurrentProposal() {
			// Close before accepting the proposal. This is important
			// so that the cursor position can be properly restored at
			// acceptance, which does not work without focus on some controls.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=127108
			IContentProposal proposal = getSelectedProposal();
			close();
			notifyProposalAccepted(proposal);
		}

		/*
		 * Request the proposals from the proposal provider, and recompute any caches. Repopulate the popup if it is
		 * open.
		 */
		private void recomputeProposals() {
			IContentProposal[] allProposals = getProposals();
			if (allProposals == null)
				allProposals = NO_PROPOSALS;
			if (allProposals.length == 0) {
				proposals = allProposals;
				close();
			} else {
				setProposals(allProposals);
			}
		}

		/*
		 * In an async block, request the proposals. This is used when clients are in the middle of processing an event
		 * that affects the widget content. By using an async, we ensure that the widget content is up to date with the
		 * event.
		 */
		private void asyncRecomputeProposals() {
			if (isValid()) {
				control.getDisplay().asyncExec(() -> {
					recordCursorPosition();
					recomputeProposals();
				});
			} else {
				recomputeProposals();
			}
		}

		Listener getTargetControlListener() {
			if (targetControlListener == null) {
				targetControlListener = new TargetControlListener();
			}
			return targetControlListener;
		}
	}

	/*
	 * Set to <code>true</code> to use a Table with SWT.VIRTUAL. This is a workaround for
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98585#c40 The corresponding SWT bug is
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=90321
	 */
	private static final boolean USE_VIRTUAL = !Util.isMotif();

	/*
	 * The character height hint for the popup. May be overridden by using setInitialPopupSize.
	 */
	private static final int POPUP_CHAR_HEIGHT = 10;

	/*
	 * The minimum pixel width for the popup. May be overridden by using setInitialPopupSize.
	 */
	private static final int POPUP_MINIMUM_WIDTH = 300;

	/*
	 * The pixel offset of the popup from the bottom corner of the control.
	 */
	private static final int POPUP_OFFSET = 3;

	/*
	 * Empty string.
	 */
	private static final String EMPTY = ""; //$NON-NLS-1$

	/*
	 * The object that provides content proposals.
	 */
	private IContentProposalProvider proposalProvider;

	/*
	 * A label provider used to display proposals in the popup, and to extract Strings from non-String proposals.
	 */
	private ILabelProvider labelProvider;

	/*
	 * The control for which content proposals are provided.
	 */
	private Control control;

	/*
	 * The adapter used to extract the String contents from an arbitrary control.
	 */
	private IControlContentAdapter controlContentAdapter;

	/*
	 * The popup used to show proposals.
	 */
	private ContentProposalPopup popup;

	/*
	 * The keystroke that signifies content proposals should be shown.
	 */
	private KeyStroke triggerKeyStroke;

	/*
	 * The listener we install on the control.
	 */
	private Listener controlListener;

	/*
	 * The list of IContentProposalListener listeners.
	 */
	private ListenerList<IContentProposalListener> proposalListeners = new ListenerList<>();

	/*
	 * The desired size in pixels of the proposal popup.
	 */
	private Point popupSize;

	/*
	 * The remembered position of the insertion position. Not all controls will restore the insertion position if the
	 * proposal popup gets focus, so we need to remember it.
	 */
	private int insertionPos = -1;

	/** Keys that will accept the selected proposal and close the popup */
	private Set<Character> acceptKeys = emptySet();

	/**
	 * Construct a content proposal adapter that can assist the user with choosing content for the field.
	 *
	 * @param control
	 *            the control for which the adapter is providing content assist. May not be <code>null</code>.
	 * @param controlContentAdapter
	 *            the <code>IControlContentAdapter</code> used to obtain and update the control's contents as proposals
	 *            are accepted. May not be <code>null</code>.
	 * @param proposalProvider
	 *            the <code>IContentProposalProvider</code> used to obtain content proposals for this control, or
	 *            <code>null</code> if no content proposal is available.
	 * @param keyStroke
	 *            the keystroke that will invoke the content proposal popup.
	 */
	public ContentProposalAdapter(Control control, IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider, KeyStroke keyStroke, Set<Character> acceptKeys) {
		super();
		// We always assume the control and content adapter are valid.
		Assert.isNotNull(control);
		Assert.isNotNull(controlContentAdapter);
		this.control = control;
		this.controlContentAdapter = controlContentAdapter;

		// The rest of these may be null
		this.proposalProvider = proposalProvider;
		this.triggerKeyStroke = keyStroke;
		this.acceptKeys = acceptKeys;
		addControlListener(control);
	}

	/**
	 * Get the control on which the content proposal adapter is installed.
	 *
	 * @return the control on which the proposal adapter is installed.
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Get the label provider that is used to show proposals.
	 *
	 * @return the {@link ILabelProvider} used to show proposals, or <code>null</code> if one has not been installed.
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Set the label provider that is used to show proposals. The lifecycle of the specified label provider is not
	 * managed by this adapter. Clients must dispose the label provider when it is no longer needed.
	 *
	 * @param labelProvider
	 *            the (@link ILabelProvider} used to show proposals.
	 */
	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Return the proposal provider that provides content proposals given the current content of the field. A value of
	 * <code>null</code> indicates that there are no content proposals available for the field.
	 *
	 * @return the {@link IContentProposalProvider} used to show proposals. May be <code>null</code>.
	 */
	public IContentProposalProvider getContentProposalProvider() {
		return proposalProvider;
	}

	/**
	 * Set the content proposal provider that is used to show proposals.
	 *
	 * @param proposalProvider
	 *            the {@link IContentProposalProvider} used to show proposals
	 */
	public void setContentProposalProvider(IContentProposalProvider proposalProvider) {
		this.proposalProvider = proposalProvider;
	}

	/**
	 * Return the size, in pixels, of the content proposal popup.
	 *
	 * @return a Point specifying the last width and height, in pixels, of the content proposal popup.
	 */
	public Point getPopupSize() {
		return popupSize;
	}

	/**
	 * Set the size, in pixels, of the content proposal popup. This size will be used the next time the content proposal
	 * popup is opened.
	 *
	 * @param size
	 *            a Point specifying the desired width and height, in pixels, of the content proposal popup.
	 */
	public void setPopupSize(Point size) {
		popupSize = size;
	}

	/**
	 * Return the content adapter that can get or retrieve the text contents from the adapter's control. This method is
	 * used when a client, such as a content proposal listener, needs to update the control's contents manually.
	 *
	 * @return the {@link IControlContentAdapter} which can update the control text.
	 */
	public IControlContentAdapter getControlContentAdapter() {
		return controlContentAdapter;
	}

	/**
	 * Add the specified listener to the list of content proposal listeners that are notified when content proposals are
	 * chosen. </p>
	 *
	 * @param listener
	 *            the IContentProposalListener to be added as a listener. Must not be <code>null</code>. If an attempt
	 *            is made to register an instance which is already registered with this instance, this method has no
	 *            effect.
	 * @see org.eclipse.jface.fieldassist.IContentProposalListener
	 */
	public void addContentProposalListener(IContentProposalListener listener) {
		proposalListeners.add(listener);
	}

	/**
	 * Removes the specified listener from the list of content proposal listeners that are notified when content
	 * proposals are chosen. </p>
	 *
	 * @param listener
	 *            the IContentProposalListener to be removed as a listener. Must not be <code>null</code>. If the
	 *            listener has not already been registered, this method has no effect.
	 * @since 3.3
	 * @see org.eclipse.jface.fieldassist.IContentProposalListener
	 */
	public void removeContentProposalListener(IContentProposalListener listener) {
		proposalListeners.remove(listener);
	}

	/*
	 * Add our listener to the control.
	 */
	private void addControlListener(Control control) {
		if (controlListener != null) {
			return;
		}
		controlListener = e -> {
			if (e.type == SWT.Traverse || e.type == SWT.KeyDown) {
				// If the popup is open, it gets first shot at the
				// keystroke and should set the doit flags appropriately.
				if (popup != null) {
					popup.getTargetControlListener().handleEvent(e);
					return;
				}

				// We were only listening to traverse events for the popup
				if (e.type == SWT.Traverse) {
					return;
				}

				// The popup is not open. We are looking at keydown events
				// for a trigger to open the popup.
				if (triggerKeyStroke != null && triggerKeyStroke.getModifierKeys() == e.stateMask && triggerKeyStroke.getNaturalKey() == e.character) {
					// We never propagate the keystroke for an explicit
					// keystroke invocation of the popup
					e.doit = false;
					openProposalPopup();
				}
			}
		};
		control.addListener(SWT.KeyDown, controlListener);
		control.addListener(SWT.Traverse, controlListener);
		control.addListener(SWT.Modify, controlListener);
	}

	/**
	 * Open the proposal popup and display the proposals provided by the proposal provider. If there are no proposals to
	 * be shown, do not show the popup. This method returns immediately. That is, it does not wait for the popup to open
	 * or a proposal to be selected.
	 *
	 */
	private void openProposalPopup() {
		if (isValid() && popup == null) {
			// Check whether there are any proposals to be shown.
			recordCursorPosition(); // must be done before getting proposals
			IContentProposal[] proposals = getProposals();

			if (proposals.length > 0) {
				recordCursorPosition();
				// Don't show the pop-up when there is only 1 proposal as the auto-completion is activated and the
				// user gets the text box filled with the only proposal.
				if (proposals.length > 1) {
					popup = new ContentProposalPopup(null, proposals);
					popup.open();
					popup.getShell().addDisposeListener(event -> popup = null);
					internalPopupOpened();
				} else {
					notifyProposalAccepted(proposals[0]);
				}
			}
		}
	}

	/**
	 * Close the proposal popup without accepting a proposal. This method returns immediately, and has no effect if the
	 * proposal popup was not open. This method is used by subclasses to explicitly close the popup based on additional
	 * logic.
	 *
	 * @since 3.3
	 */
	public void closeProposalPopup() {
		if (popup != null) {
			popup.close();
		}
	}

	/*
	 * Check that the control and content adapter are valid.
	 */
	private boolean isValid() {
		return control != null && !control.isDisposed() && controlContentAdapter != null;
	}

	/*
	 * Record the control's cursor position.
	 */
	private void recordCursorPosition() {
		if (isValid()) {
			IControlContentAdapter adapter = getControlContentAdapter();
			insertionPos = adapter.getCursorPosition(control);
		}
	}

	/*
	 * Get the proposals from the proposal provider. Gets all of the proposals without doing any filtering.
	 */
	private IContentProposal[] getProposals() {
		if (proposalProvider == null || !isValid()) {
			return NO_PROPOSALS;
		}
		int position = insertionPos;
		if (position == -1) {
			position = getControlContentAdapter().getCursorPosition(getControl());
		}
		String contents = getControlContentAdapter().getControlContents(getControl());
		return proposalProvider.getProposals(contents, position);
	}

	/*
	 * A proposal has been accepted. Notify interested listeners.
	 */
	private void notifyProposalAccepted(IContentProposal proposal) {
		final Object[] listenerArray = proposalListeners.getListeners();
		for (int i = 0; i < listenerArray.length; i++) {
			((IContentProposalListener) listenerArray[i]).proposalAccepted(proposal);
		}
	}

	/**
	 * Returns whether the content proposal popup has the focus. This includes both the primary popup and any secondary
	 * info popup that may have focus.
	 *
	 * @return <code>true</code> if the proposal popup or its secondary info popup has the focus
	 * @since 3.4
	 */
	public boolean hasProposalPopupFocus() {
		return popup != null && popup.hasFocus();
	}

	/*
	 * The popup has just opened, but listeners have not yet been notified. Perform any cleanup that is needed.
	 */
	private void internalPopupOpened() {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=243612
		if (control instanceof Combo combo) {
			combo.setListVisible(false);
		}
	}

	/**
	 * Sets focus to the proposal popup. If the proposal popup is not opened, this method is ignored. If the secondary
	 * popup has focus, focus is returned to the main proposal popup.
	 *
	 * @since 3.6
	 */
	public void setProposalPopupFocus() {
		if (isValid() && popup != null)
			popup.getShell().setFocus();
	}

	/**
	 * Answers a boolean indicating whether the main proposal popup is open.
	 *
	 * @return <code>true</code> if the proposal popup is open, and <code>false</code> if it is not.
	 * @since 3.6
	 */
	public boolean isProposalPopupOpen() {
		return isValid() && popup != null;
	}

}
