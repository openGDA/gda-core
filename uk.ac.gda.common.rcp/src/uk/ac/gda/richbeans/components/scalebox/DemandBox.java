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

package uk.ac.gda.richbeans.components.scalebox;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.beans.IExpressionManager;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;
import uk.ac.gda.ui.utils.SWTUtils;

/**
 * Demand box has the following event properties:
 * 1. Bounds are checked whenever a value is changed.
 * 2. ValueListeners are notified when the enter key is pressed or when focus is lost.
 * 3. Use the demandBegin, demandStep and demandComplete methods to control updating behaviour
 */
public class DemandBox extends NumberBox {

	boolean enabled = true;
	boolean desiredEnabledState = true; //remember what state the GUI should have if disabled from higher levels (MotorPositionViewer)
	
	protected boolean restoreValueWhenFocusLost;
	protected double previousValue;

	public DemandBox(Composite parent, int style) {
		this(parent, style, -1);
	}
	/**
	 * 
	 * @param parent
	 * @param style
	 * @param expressionWidthHint A hint for the width of the expression box, or -1 for no hint
	 */
	public DemandBox(Composite parent, int style, int expressionWidthHint) {
		super(parent, style);
		createExpressionLabel(expressionWidthHint);
	}

	/**
	 * Call when the move is complete, the text gets set to the finalPosition
	 * and the expression view of the move disappears 
	 */
	public void demandComplete(final Object finalPosition) {
		desiredEnabledState = true;
		setRealState();
		setValue(finalPosition);
		GridUtils.setVisibleAndLayout(expressionLabel, false);
	}

	/**
	 * Call when the move is complete when no final position is available.
	 * The expression view of the move disappears 
	 */
	public void demandComplete() {
		desiredEnabledState = true;
		setRealState();
		GridUtils.setVisibleAndLayout(expressionLabel, false);
	}

	/**
	 * Call when the move begins, the expression text gets to the startPosition
	 * and the text becomes disabled
	 */
	public void demandBegin(final double startPosition) {
		desiredEnabledState = false;
		setRealState();
		setExpressionValue(startPosition);
	}

	/**
	 * Call when a move is in progress with a new value
	 */
	public void demandStep(final double stepPosition) {
		desiredEnabledState = false;
		setRealState();
		setExpressionValue(stepPosition);
	}

	
	@Override
	public boolean isExpressionAllowed() {
		return true; // its for the demand value
	}
	
	/**
	 * Stops the setting of an expression manager being possible.
	 */
	@Override
	public void setExpressionManager(IExpressionManager man) {
		this.expressionManager = null;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setRealState();
	}
	
	public void setRealState() {
		super.setEnabled(desiredEnabledState & enabled);
	}
	
	/**
	 * Change listeners to only notify if focus lost or enter pressed.
	 */
	@Override
	protected void createTextListeners(final StyledText text) {
		createFocusListener(text);
		createModifyListener(text);
		createVerifyKeyListener(text);
	}
	
	@Override
	protected void createVerifyKeyListener(StyledText text) {
		
		verifyListener = new VerifyKeyListener() {	
			@Override
			public void verifyKey(VerifyEvent event) {
				if (event.character == SWT.CR) {// They hit enter
					textUpdateAndFireListeners();
				}
			}
		};
		text.addVerifyKeyListener(verifyListener);
	}

	@Override
	protected void createModifyListener(final StyledText text) {
	    this.modifyListener = new ModifyListener()  {
			@Override
			public void modifyText(final ModifyEvent e) {
				textUpdate(); // Checks the entry for bounds (and unit)
				              // Do not fire events.
			} 	
	    };
	    text.addModifyListener(modifyListener);
	}
	
	/**
	 * Change loss of focus to fire value.
	 */
	@Override
	protected void createFocusListener(final StyledText text) {
		this.focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (restoreValueWhenFocusLost) {
					restoreText();
				}
				else {
					textUpdateAndFireListeners();
				}
			}
	    };
	    text.addFocusListener(focusListener);
	}

	protected void restoreText() {
		setValue(previousValue);
	}
	
	@Override
	public void setValue(Object value) {
		if (value != null) {
			previousValue = (Double)value;
		}
		super.setValue(value);
	}
	
	public boolean isRestoreValueWhenFocusLost() {
		return restoreValueWhenFocusLost;
	}
	public void setRestoreValueWhenFocusLost(boolean restoreValueWhenFocusLost) {
		this.restoreValueWhenFocusLost = restoreValueWhenFocusLost;
	}
	/**
	 * Testing use only.
	 */
	public static void main(String... args) {
		
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new BorderLayout());
		
		final Composite comp = new Composite(shell,SWT.NONE);
		comp.setLayoutData(BorderLayout.NORTH);
		
		comp.setLayout(new GridLayout(1,false));

		final DemandBox x = new DemandBox(comp, SWT.NONE);
		x.setUnit("micron");
		x.setLabel("x");
		x.setLabelWidth(20);
		x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final DemandBox y = new DemandBox(comp, SWT.NONE, 30); // set narrower than default
		y.setUnit("micron");
		y.setLabel("y");
		y.setLabelWidth(20);
		y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final DemandBox z = new DemandBox(comp, SWT.NONE);
		z.setUnit("micron");
		z.setLabel("z");
		z.setLabelWidth(20);
		z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label value = new Label(comp, SWT.NONE);
		value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label moveError = new Label(comp, SWT.NONE);
		value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		moveError.setText("");
		
		final TestMover xMotor = new TestMover(1);
		final TestMover zMotor = new TestMover(1);
		
		// set different initial positions, these aren't motor moves
		xMotor.setInitialPosition(5);
		zMotor.setInitialPosition(5);
	
		x.setValue(xMotor.getPosition());
		y.setValue(xMotor.getPosition());
		z.setValue(zMotor.getPosition());


		xMotor.addObserver(new Observer() {	
			@Override
			public void update(Observable o, Object arg) {
				shell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (xMotor.isBusy()){
							x.demandStep(xMotor.getPosition());
							y.demandStep(xMotor.getPosition());
						} else {
							x.demandComplete(xMotor.getPosition());
							y.demandComplete(xMotor.getPosition());
						}
					}
				});
			}
		});
		
	
		zMotor.addObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				shell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (zMotor.isBusy()){
							z.demandStep(zMotor.getPosition());
						} else {
							// don't automatically set text box with final position
							z.demandComplete();
						}
					}
				});
			}
		});				
        
        x.addValueListener(new ValueAdapter("x value listener") {		
			@Override
			public void valueChangePerformed(ValueEvent e) {
				double destPosition = e.getDoubleValue();
				x.demandBegin(xMotor.getPosition());
				xMotor.moveTo(destPosition);			
				moveError.setText("");
				shell.layout(); // relayout for error message change
			}
		});

		y.addValueListener(new ValueAdapter("y value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				double destPosition = e.getDoubleValue();
				y.demandBegin(xMotor.getPosition());
				xMotor.moveTo(destPosition);
				moveError.setText("");
				shell.layout(); // relayout for error message change
			}
		});

		z.addValueListener(new ValueAdapter("z value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				double destPosition = e.getDoubleValue();
				z.demandBegin(zMotor.getPosition());
				zMotor.moveTo(destPosition);
				moveError.setText("");
				shell.layout(); // relayout for error message change
			}
		});
		
		
		
		final ValueListener l = new ValueAdapter("test") {		
			@Override
			public void valueChangePerformed(ValueEvent e) {
				final StringBuilder buf = new StringBuilder();
				addValue("x", x, buf);
				addValue("y", y, buf);
				addValue("z", z, buf);
				value.setText(buf.toString());
				shell.layout();
			}

			private void addValue(String label, DemandBox box, StringBuilder buf) {
				buf.append(label);
				buf.append("=");
				buf.append(box.getValue());
				buf.append(" ");
				buf.append(box.getUnit());
				buf.append("    ");
			}
		};

		x.addValueListener(l);
		y.addValueListener(l);
		z.addValueListener(l);
		
		shell.pack();
		shell.setSize(400,400);

		x.on();
		y.on();
		z.on();
		SWTUtils.showCenteredShell(shell);
	}


	/**
	 * Just a test class to display the move facility
	 * 
	 * Behaves a little like a motor and uses a timer to notify.
	 */
	private static class TestMover extends Observable {
				
		private boolean busy = false;
		private double value = 0d; 
		private double stepSize = 1d;
		
		TestMover(double stepSize) {
			this.stepSize = stepSize;
		}

		public void setInitialPosition(double d) {
			value = d;
		} 

		public void moveTo(final double target) {
			if (busy)
				return;
			
			busy = true;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					double step;
					if (value > target)
						step = -stepSize;
					else
						step = stepSize;

					while (true) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}

						if (step < 0 && value <= target)
							break;
						else if (step > 0 && value >= target)
							break;

						value += step;
						setChanged();
						notifyObservers(value);
					}
					setChanged();
					notifyObservers();
					busy = false;

				}
			};
			Thread t = new Thread(runnable);
			t.start();
		}

		public double getPosition() {
			return value;
		}
		
		public boolean isBusy(){
			return busy;
		}
	}		
}
