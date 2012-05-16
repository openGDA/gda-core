/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.components;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite that lists 128, 64, 32, 16, 8, 4, 2, 1 as buttons so that selecting those, the amplification of the
 * exposure, and the histogram settings can be controlled.
 */
public class AmplifierStepperComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(AmplifierStepperComposite.class);

	private STEPPER selectedStepper = STEPPER.ONE;

	private void showError(final String dialogTitle, final Exception ex) {
		if (!this.isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(getShell(), dialogTitle, ex.getMessage());
				}
			});
		}
	}

	/**
	 * Listeners to the amplifier Stepper composite
	 */
	public interface AmplifierStepperListener {
		/**
		 * @param stepper
		 * @throws Exception
		 */
		public void performAction(STEPPER stepper) throws Exception;
	}

	private List<AmplifierStepperListener> amplifierStepperListeners = new ArrayList<AmplifierStepperComposite.AmplifierStepperListener>();

	/**
	 * @param amplifierStepperListener
	 * @return <code>true</code> if the listener is added to the list successfully
	 */
	public boolean addAmplifierStepperListener(AmplifierStepperListener amplifierStepperListener) {
		return amplifierStepperListeners.add(amplifierStepperListener);
	}

	/**
	 * @param amplifierStepperListener
	 * @return <code>true</code> if the listener is removed from the list successfully.
	 */
	public boolean removeAmplifierStepperListener(AmplifierStepperListener amplifierStepperListener) {
		return amplifierStepperListeners.remove(amplifierStepperListener);
	}

	public void moveStepperTo(STEPPER stepper) {
		// Expect all the buttons to be present.
		for (Button btn : buttons) {
			if (!stepper.equals(btn.getModel().getUserData())) {
				deselectButton(btn);
			} else {
				selectButton(btn);
			}
		}
	}

	/**
	 * STEPPER enum
	 */
	public enum STEPPER {
		ONE_TWENTY_EIGHT(128) {
			@Override
			public String toString() {
				return "128";
			}
		},
		SIXTY_FOUR(64) {
			@Override
			public String toString() {
				return "64";
			}
		},
		THIRTY_TWO(32) {
			@Override
			public String toString() {
				return "32";
			}
		},
		SIXTEEN(16) {
			@Override
			public String toString() {
				return "16";
			}
		},
		EIGHT(8) {
			@Override
			public String toString() {
				return "8";
			}

		},
		FOUR(4) {
			@Override
			public String toString() {
				return "4";
			}
		},
		TWO(2) {
			@Override
			public String toString() {
				return "2";
			}
		},
		ONE(1) {
			@Override
			public String toString() {
				return "1";
			}
		};

		private final int val;

		/**
		 * @param text
		 * @return the {@link STEPPER} for the given text
		 */
		public static STEPPER getStepperEnum(String text) {
			for (STEPPER stepper : values()) {
				if (stepper.toString().equals(text)) {
					return stepper;
				}
			}
			return null;
		}

		STEPPER(int val) {
			this.val = val;
		}

		public int getValue() {
			return val;
		}

	}

	private FigureCanvas figCanvas;
	private FontRegistry fontRegistry;
	private static final String BOLD_TEXT_7 = "bold-text_7";
	private Button btn1;
	private Button btn2;
	private Button btn3;
	private Button btn4;
	private Button btn5;
	private Button btn6;
	private Button btn7;
	private Button btn8;
	private Button[] buttons = new Button[8];

	/**
	 * @param parent
	 * @param style
	 */
	public AmplifierStepperComposite(Composite parent, int style) {
		super(parent, style);
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_7, new FontData[] { new FontData(fontName, 8, SWT.BOLD) });
		}
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);
		figCanvas = new FigureCanvas(this);
		figCanvas.setContents(getContents());
		figCanvas.getViewport().setContentsTracksHeight(true);
		figCanvas.getViewport().setContentsTracksWidth(true);
		figCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	private ChangeListener changeListener = new ChangeListener() {

		@Override
		public void handleStateChanged(ChangeEvent event) {
			if (ButtonModel.PRESSED_PROPERTY.equals(event.getPropertyName())) {
				Button btn = (Button) event.getSource();
				if (btn.getModel().isPressed()) {
					if (!ColorConstants.darkGreen.equals(btn.getBackgroundColor())) {
						selectButton(btn);
						STEPPER stepper = (STEPPER) btn.getModel().getUserData();
						if (stepper != null) {
							try {
								for (AmplifierStepperListener aSl : amplifierStepperListeners) {
									aSl.performAction(stepper);
								}
							} catch (Exception e) {
								showError("Problem setting amplifier state", e);
							}
							for (Button b : buttons) {
								if (!b.equals(btn)) {
									deselectButton(b);
								}
							}
						}
					}
				}
			} else if (ButtonModel.MOUSEOVER_PROPERTY.equals(event.getPropertyName())) {
				Button button = (Button) event.getSource();
				button.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
			}
		}
	};

	protected void deselectButton(final Button btn) {
		if (!isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					btn.setBackgroundColor(ColorConstants.lightGray);
					btn.setForegroundColor(ColorConstants.black);
				}
			});
		}
	}

	protected void selectButton(final Button btn) {
		if (!isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					btn.setBackgroundColor(ColorConstants.darkGreen);
					btn.setForegroundColor(ColorConstants.white);
				}
			});
		}

		if (btn.equals(btn1)) {
			selectedStepper = STEPPER.ONE_TWENTY_EIGHT;
		} else if (btn.equals(btn2)) {
			selectedStepper = STEPPER.SIXTY_FOUR;
		} else if (btn.equals(btn3)) {
			selectedStepper = STEPPER.THIRTY_TWO;
		} else if (btn.equals(btn4)) {
			selectedStepper = STEPPER.SIXTEEN;
		} else if (btn.equals(btn5)) {
			selectedStepper = STEPPER.EIGHT;
		} else if (btn.equals(btn6)) {
			selectedStepper = STEPPER.FOUR;
		} else if (btn.equals(btn7)) {
			selectedStepper = STEPPER.TWO;
		} else if (btn.equals(btn8)) {
			selectedStepper = STEPPER.ONE;
		}
	}

	public STEPPER getSelectedStepper() {
		return selectedStepper;
	}

	private IFigure getContents() {
		RectangleFigure boundaryFigure = new RectangleFigure();
		boundaryFigure.setBackgroundColor(ColorConstants.white);
		boundaryFigure.setLayoutManager(new ZoomSliderCompositeLayout());
		int count = 0;
		btn1 = new Button(STEPPER.ONE_TWENTY_EIGHT.toString());
		btn1.setBorder(new LineBorder(1));

		getButtonLabel(btn1).setFont(fontRegistry.get(BOLD_TEXT_7));
		boundaryFigure.add(btn1);
		btn1.getModel().setUserData(STEPPER.ONE_TWENTY_EIGHT);
		btn1.addChangeListener(changeListener);
		buttons[count++] = btn1;
		//
		btn2 = new Button(STEPPER.SIXTY_FOUR.toString());
		btn2.setBorder(new LineBorder(1));
		getButtonLabel(btn2).setFont(fontRegistry.get(BOLD_TEXT_7));
		boundaryFigure.add(btn2);
		btn2.getModel().setUserData(STEPPER.SIXTY_FOUR);
		btn2.addChangeListener(changeListener);
		buttons[count++] = btn2;
		//
		btn3 = new Button(STEPPER.THIRTY_TWO.toString());
		btn3.setBorder(new LineBorder(1));
		getButtonLabel(btn3).setFont(fontRegistry.get(BOLD_TEXT_7));
		boundaryFigure.add(btn3);
		btn3.getModel().setUserData(STEPPER.THIRTY_TWO);
		btn3.addChangeListener(changeListener);
		buttons[count++] = btn3;
		//
		btn4 = new Button(STEPPER.SIXTEEN.toString());
		btn4.setBorder(new LineBorder(1));
		getButtonLabel(btn4).setFont(fontRegistry.get(BOLD_TEXT_7));
		boundaryFigure.add(btn4);
		btn4.getModel().setUserData(STEPPER.SIXTEEN);
		btn4.addChangeListener(changeListener);
		buttons[count++] = btn4;
		//
		btn5 = new Button(STEPPER.EIGHT.toString());
		btn5.setBorder(new LineBorder(1));
		getButtonLabel(btn5).setFont(fontRegistry.get(BOLD_TEXT_7));
		boundaryFigure.add(btn5);
		btn5.getModel().setUserData(STEPPER.EIGHT);
		btn5.addChangeListener(changeListener);
		buttons[count++] = btn5;
		//
		btn6 = new Button(STEPPER.FOUR.toString());
		btn6.setBorder(new LineBorder(1));
		getButtonLabel(btn6).setFont(fontRegistry.get(BOLD_TEXT_7));
		boundaryFigure.add(btn6);
		btn6.getModel().setUserData(STEPPER.FOUR);
		btn6.addChangeListener(changeListener);
		buttons[count++] = btn6;
		//
		btn7 = new Button(STEPPER.TWO.toString());
		btn7.setBorder(new LineBorder(1));
		getButtonLabel(btn7).setFont(fontRegistry.get(BOLD_TEXT_7));
		boundaryFigure.add(btn7);
		btn7.getModel().setUserData(STEPPER.TWO);
		btn7.addChangeListener(changeListener);
		buttons[count++] = btn7;
		//
		btn8 = new Button(STEPPER.ONE.toString());
		btn8.setBorder(new LineBorder(1));
		btn8.getModel().setUserData(STEPPER.ONE);
		getButtonLabel(btn8).setFont(fontRegistry.get(BOLD_TEXT_7));
		boundaryFigure.add(btn8);
		btn8.addChangeListener(changeListener);
		buttons[count++] = btn8;
		//
		return boundaryFigure;
	}

	private class ZoomSliderCompositeLayout extends XYLayout {
		@Override
		public void layout(IFigure parent) {
			super.layout(parent);
			Rectangle parentBounds = parent.getBounds();
			parent.setSize(35, parentBounds.height);
			Dimension textExtents = getButtonLabel(btn1).getTextUtilities().getTextExtents(
					getButtonLabel(btn1).getText(), getFont());
			Dimension buttonSize = new Dimension(textExtents.width + 5, textExtents.height + 5);
			getButtonLabel(btn1).setSize(textExtents);
			btn1.setSize(buttonSize);
			btn1.setLocation(new Point(3, 5));

			textExtents = getButtonLabel(btn2).getTextUtilities().getTextExtents(getButtonLabel(btn2).getText(),
					getFont());
			getButtonLabel(btn2).setSize(textExtents);
			btn2.setSize(buttonSize);
			btn2.setLocation(new Point(3, parentBounds.height / 8));

			textExtents = getButtonLabel(btn3).getTextUtilities().getTextExtents(getButtonLabel(btn3).getText(),
					getFont());
			getButtonLabel(btn3).setSize(textExtents);
			btn3.setSize(buttonSize);
			btn3.setLocation(new Point(3, parentBounds.height / 4));

			textExtents = getButtonLabel(btn4).getTextUtilities().getTextExtents(getButtonLabel(btn4).getText(),
					getFont());
			getButtonLabel(btn4).setSize(textExtents);
			btn4.setSize(buttonSize);
			btn4.setLocation(new Point(3, 3 * parentBounds.height / 8));

			textExtents = getButtonLabel(btn5).getTextUtilities().getTextExtents(getButtonLabel(btn5).getText(),
					getFont());
			getButtonLabel(btn5).setSize(textExtents);
			btn5.setSize(buttonSize);
			btn5.setLocation(new Point(3, parentBounds.height / 2));

			textExtents = getButtonLabel(btn6).getTextUtilities().getTextExtents(getButtonLabel(btn6).getText(),
					getFont());
			getButtonLabel(btn6).setSize(textExtents);
			btn6.setSize(buttonSize);
			btn6.setLocation(new Point(3, 5 * parentBounds.height / 8));

			textExtents = getButtonLabel(btn7).getTextUtilities().getTextExtents(getButtonLabel(btn7).getText(),
					getFont());
			getButtonLabel(btn7).setSize(textExtents);
			btn7.setSize(buttonSize);
			btn7.setLocation(new Point(3, 3 * parentBounds.height / 4));

			textExtents = getButtonLabel(btn8).getTextUtilities().getTextExtents(getButtonLabel(btn8).getText(),
					getFont());
			getButtonLabel(btn8).setSize(textExtents);
			btn8.setSize(buttonSize);
			btn8.setLocation(new Point(3, 7 * parentBounds.height / 8));

		}
	}

	@SuppressWarnings("rawtypes")
	private Label getButtonLabel(Button btn) {
		List children = btn.getChildren();
		for (Object child : children) {
			if (child instanceof Label) {
				return (Label) child;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 100, 400));
		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		AmplifierStepperComposite sliderComposite = new AmplifierStepperComposite(shell, SWT.None);
		shell.setText(sliderComposite.getClass().getName());
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 400;
		layoutData.widthHint = 100;
		sliderComposite.setLayoutData(layoutData);
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	@Override
	public void dispose() {
		logger.info("Disposing");
		try {
			for (Button btn : buttons) {
				btn.removeChangeListener(changeListener);
			}

			buttons = null;
			amplifierStepperListeners.clear();
			amplifierStepperListeners = null;
			fontRegistry = null;
			figCanvas.dispose();

		} catch (Exception ex) {
			logger.error("Problem disposing", ex);
		}
		super.dispose();
	}
}
