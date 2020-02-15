/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * This class has been implemented to customise {@link NudgePositionerComposite} layout not to change how a motor is
 * controlled.
 *
 * @author Maurizio Nagni
 */
public class SlimPositionerComposite extends NudgePositionerComposite {

	public SlimPositionerComposite(Composite parent, int style) {
		super(parent, style);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this);
	}

	@Override
	protected void createPositionerControl() {
		setPositionText(new Text(this, SWT.BORDER));
		if (isReadOnlyPosition()) {
			getPositionText().setEditable(false);
		} else {
			getPositionText().addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent key) {
					// If enter was pressed move to new position
					if (key.character == SWT.CR) { // enter or numpad enter pressed
						// Get the new position from the text box
						double newPosition = Double.parseDouble(getPositionText().getText().split(" ")[0]);
						move(newPosition);
					}
					// If up was pressed increment position and move
					if (key.keyCode == SWT.ARROW_UP) { // up arrow pressed
						doIncrementValue();
					}
					// If down was pressed decrement position and move
					if (key.keyCode == SWT.ARROW_DOWN) { // down arrow pressed
						doDecrementValue();
					}
				}
			});
			getPositionText().addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					// Update to ensure current position is shown when focus is lost
					scheduleUpdateReadbackJob();
				}
			});
		}

		Composite nudgeAmountComposite = new Composite(this, SWT.VERTICAL);
		GridLayoutFactory.fillDefaults().spacing(1, 1).applyTo(nudgeAmountComposite);
		final GridDataFactory buttonDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		// Increment button
		setIncrementButton(ClientSWTElements.createButton(nudgeAmountComposite, SWT.FLAT, ClientMessages.EMPTY_MESSAGE,
				ClientMessages.EMPTY_MESSAGE, ClientImages.TRIANGLE_UP));
		buttonDataFactory.applyTo(getIncrementButton());
		getIncrementButton().addSelectionListener(widgetSelectedAdapter(e -> doIncrementValue()));

		// Decrement button
		setDecrementButton(ClientSWTElements.createButton(nudgeAmountComposite, SWT.FLAT, ClientMessages.EMPTY_MESSAGE,
				ClientMessages.EMPTY_MESSAGE, ClientImages.TRIANGLE_DOWN));
		buttonDataFactory.applyTo(getDecrementButton());
		getDecrementButton().addSelectionListener(widgetSelectedAdapter(e -> doDecrementValue()));

		// Increment text box
		setIncrementText(new Text(this, SWT.BORDER));
		getIncrementText().addListener(SWT.Modify, event -> setIncrement(getIncrementText().getText()));
	}
}