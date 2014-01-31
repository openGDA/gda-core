/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.part.ViewPart;
@Deprecated
public class MCView extends ViewPart {

	enum PaintMode {
		DRAW, TOGGLE, ERASE
	}

	private MaskCreator mc;
	protected Label maindsmax;
	protected Label maindsmin;
	private PaintMode paintMode = PaintMode.DRAW;
	private boolean square = false;
	private int pensize = 1;
	protected Button btnClear;
	protected Button btnUndo;
	protected Button btnMaskAbove;
	protected Button btnMaskBelow;
	private Spinner spinnerabove;
	private Spinner spinnerbelow;
	protected ScrolledComposite container;
	protected Label statusLabel;
	protected boolean othercolor = false;
	protected boolean keepVisible = false;

	/**
	 * @wbp.parser.constructor
	 */
	public MCView() {
		mc = new MaskCreator();
	}

	MCView(MaskCreator mc) {
		this.mc = mc;
	}

	@Override
	public void createPartControl(Composite parent) {

		SelectionListener threschanged = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer max = null;
				Integer min = null;
				if (btnMaskAbove.getSelection()) {
					max = spinnerabove.getSelection();
				}
				if (btnMaskBelow.getSelection()) {
					min = spinnerbelow.getSelection();
				}
				mc.updateThreshold(min, max);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		container = new ScrolledComposite(parent, SWT.VERTICAL);
		final Composite c = new Composite(container, SWT.NONE);
		GridLayout grid = new GridLayout(2, true);
		c.setLayout(grid);

		Group g = new Group(c, SWT.BORDER);
		g.setText("Masking");
		GridLayout gl = new GridLayout(4, false);
		g.setLayout(gl);
		g.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		{
			btnMaskAbove = new Button(g, SWT.CHECK);
			btnMaskAbove.setAlignment(SWT.CENTER);
			btnMaskAbove.setText("mask above");
			btnMaskAbove.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
			btnMaskAbove.addSelectionListener(threschanged);

			spinnerabove = new Spinner(g, SWT.BORDER);
			spinnerabove.setMaximum(1000000);
			spinnerabove.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
			spinnerabove.addSelectionListener(threschanged);

			btnMaskBelow = new Button(g, SWT.CHECK);
			btnMaskBelow.setAlignment(SWT.CENTER);
			btnMaskBelow.setText("mask below");
			btnMaskBelow.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
			btnMaskBelow.addSelectionListener(threschanged);

			spinnerbelow = new Spinner(g, SWT.BORDER);
			spinnerbelow.setMaximum(1000000);
			spinnerbelow.setMinimum(-1);
			spinnerbelow.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
			spinnerbelow.addSelectionListener(threschanged);

			Label lblMax = new Label(g, SWT.RIGHT);
			lblMax.setText("Max: ");
			maindsmax = new Label(g, SWT.RIGHT);

			Label lblMin = new Label(g, SWT.RIGHT);
			lblMin.setText("Min: ");
			maindsmin = new Label(g, SWT.RIGHT);

			final Button changeColor = new Button(g, SWT.CHECK);
			changeColor.setText("toggle mask color");
			changeColor.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
			changeColor.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					othercolor = changeColor.getSelection();
					mc.drawOverlay();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			final Button keepVisibleButton = new Button(g, SWT.CHECK);
			keepVisibleButton.setText("keep mask visible");
			keepVisibleButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
			keepVisibleButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					keepVisible  = keepVisibleButton.getSelection();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		g = new Group(c, SWT.BORDER);
		g.setText("Pen properties");
		gl = new GridLayout(2, false);
		g.setLayout(gl);
		g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		{
			Label lblPenSize = new Label(g, SWT.NONE);
			lblPenSize.setText("Pen size");

			final Spinner penSpinner = new Spinner(g, SWT.BORDER);
			penSpinner.setMaximum(200);
			penSpinner.setMinimum(1);
			penSpinner.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					pensize = penSpinner.getSelection();
				}
			});

			Composite gp = new Composite(g, SWT.NONE);
			gl = new GridLayout(1, true);
			gp.setLayout(gl);
			gp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			{
				final Button btnSquare = new Button(gp, SWT.RADIO);
				btnSquare.setText("square");
				btnSquare.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						square = btnSquare.getSelection();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

				Button btnRound = new Button(gp, SWT.RADIO);
				btnRound.setText("round");
				btnRound.setSelection(true);
			}

			gp = new Composite(g, SWT.NONE);
			gl = new GridLayout(1, true);
			gp.setLayout(gl);
			gp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			{
				final Button btnDraw = new Button(gp, SWT.RADIO);
				btnDraw.setText("draw");
				btnDraw.setSelection(true);
				btnDraw.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						if (btnDraw.getSelection()) {
							paintMode = PaintMode.DRAW;
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

				final Button btnErase = new Button(gp, SWT.RADIO);
				btnErase.setText("erase");

				btnErase.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (btnErase.getSelection()) {
							paintMode = PaintMode.ERASE;
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
			btnClear = new Button(g, SWT.NONE);
			btnClear.setText("Clear");
			btnClear.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			btnUndo = new Button(g, SWT.NONE);
			btnUndo.setText("Undo");
			btnUndo.setEnabled(false);
			btnUndo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		}

		statusLabel = new Label(c, SWT.CENTER);
		statusLabel.setText("1000000000 pixels masked (100.00% of the detector area).");
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

		Composite e = new Composite(c, SWT.NONE);
		e.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		e = new Composite(c, SWT.NONE);
		e.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		container.setContent(c);
		container.setExpandVertical(true);
		container.setExpandHorizontal(true);
		container.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = container.getClientArea();
				container.setMinSize(c.computeSize(r.width, SWT.DEFAULT));
			}
		});

		mc.processPlotUpdate();
	}

	protected PaintMode getPaintMode() {
		return paintMode;
	}

	protected boolean isSquarePen() {
		return square;
	}

	protected int getPenSize() {
		return pensize;
	}

	@Override
	public void setFocus() {
	}

	void setMinMax(final int min, final int max) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				maindsmax.setText(String.format("%d", max));
				maindsmin.setText(String.format("%d", min));
				if (!btnMaskAbove.getSelection()) {
					spinnerabove.setMaximum(max + 1);
					spinnerabove.setMinimum(min);
				}
				if (!btnMaskBelow.getSelection()) {
					spinnerbelow.setMinimum(min - 1);
					spinnerbelow.setMaximum(max);
				}
			}
		});
	}
}
