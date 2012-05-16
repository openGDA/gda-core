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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum that caters to the motion control composite
 */
public enum MotionControlCentring {
	TILT {
		@Override
		protected void switchOn(MotionControlComposite mCC) throws Exception {
			logger.debug("'Tilt' - is selected");
			selectControl(mCC.btnTilt);
			/**/
			deSelectControl(mCC.btnVertical);
			deSelectControl(mCC.btnHorizontal);
			deSelectControl(mCC.btnMoveAxisOfRotation);

			disable(mCC.txtXrayEnergy);
			disable(mCC.btnSampleWeight10to50);
			disable(mCC.btnSampleWeight1to10);
			disable(mCC.btnSampleWeightLessThan1);
			disable(mCC.txtCameraDistance);
			if (mCC.isRotationAxisFound()) {
				enable(mCC.btnMoveAxisOfRotation);
			}

			try {
				for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
					mcl.tilt(true);
				}
			} catch (Exception ex) {
				mCC.showErrorDialog(ex);
				throw ex;
			}

			/**/

		}

		@Override
		protected void switchOff(MotionControlComposite mCC) throws Exception {
			logger.debug("'Tilt' - is de-selected");
			deSelectControl(mCC.btnTilt);
			enable(mCC.moduleButtonComposite);
			enable(mCC.coarseRotation);
			enable(mCC.fineRotation);
			enable(mCC.btnLeftRotate);
			enable(mCC.btnRightRotate);
			enable(mCC.btnVertical);
			enable(mCC.btnFindAxisOfRotation);
			enable(mCC.btnTilt);

			enable(mCC.txtXrayEnergy);
			enable(mCC.btnSampleWeight10to50);
			enable(mCC.btnSampleWeight1to10);
			enable(mCC.btnSampleWeightLessThan1);
			enable(mCC.txtCameraDistance);
			/**/
			for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
				mcl.tilt(false);
			}
		}
	},
	HORIZONTAL {
		@Override
		protected void switchOn(MotionControlComposite mCC) throws Exception {
			/**/
			/**/
			selectControl(mCC.btnHorizontal);
			/**/
			deSelectControl(mCC.btnTilt);
			deSelectControl(mCC.btnMoveAxisOfRotation);
			deSelectControl(mCC.btnVertical);

			//
			disable(mCC.moduleButtonComposite);
			disable(mCC.moduleButtonComposite);
			disable(mCC.coarseRotation);
			disable(mCC.fineRotation);
			disable(mCC.btnLeftRotate);
			disable(mCC.btnRightRotate);
			disable(mCC.btnVertical);
			disable(mCC.btnMoveAxisOfRotation);
			disable(mCC.btnFindAxisOfRotation);
			disable(mCC.btnTilt);

			disable(mCC.txtXrayEnergy);
			disable(mCC.btnSampleWeight10to50);
			disable(mCC.btnSampleWeight1to10);
			disable(mCC.btnSampleWeightLessThan1);
			disable(mCC.txtCameraDistance);
			try {
				for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
					mcl.horizontal(true);
				}
			} catch (Exception ex) {
				mCC.showErrorDialog(ex);
				throw ex;
			}

			//

		}

		@Override
		protected void switchOff(MotionControlComposite mCC) throws Exception {
			deSelectControl(mCC.btnHorizontal);
			/**/
			enable(mCC.moduleButtonComposite);
			enable(mCC.coarseRotation);
			enable(mCC.fineRotation);
			enable(mCC.btnLeftRotate);
			enable(mCC.btnRightRotate);
			enable(mCC.btnVertical);
			enable(mCC.btnFindAxisOfRotation);
			enable(mCC.btnTilt);

			enable(mCC.txtXrayEnergy);
			enable(mCC.btnSampleWeight10to50);
			enable(mCC.btnSampleWeight1to10);
			enable(mCC.btnSampleWeightLessThan1);
			enable(mCC.txtCameraDistance);
			if (mCC.isRotationAxisFound()) {
				enable(mCC.btnMoveAxisOfRotation);
			}

			for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
				mcl.horizontal(false);
			}
		}
	},

	FIND_AXIS_ROTATION {
		@Override
		protected void switchOn(MotionControlComposite mCC) throws Exception {
			logger.debug("Half Rotation Tool - is selected");
			selectControl(mCC.btnFindAxisOfRotation);
			/**/
			deSelectControl(mCC.btnTilt);
			deSelectControl(mCC.btnHorizontal);
			deSelectControl(mCC.btnVertical);
			//
			disable(mCC.moduleButtonComposite);
			disable(mCC.moduleButtonComposite);
			disable(mCC.coarseRotation);
			disable(mCC.fineRotation);
			disable(mCC.btnLeftRotate);
			disable(mCC.btnRightRotate);
			disable(mCC.btnVertical);
			disable(mCC.btnHorizontal);
			disable(mCC.btnMoveAxisOfRotation);
			disable(mCC.btnTilt);

			disable(mCC.txtXrayEnergy);
			disable(mCC.btnSampleWeight10to50);
			disable(mCC.btnSampleWeight1to10);
			disable(mCC.btnSampleWeightLessThan1);
			disable(mCC.txtCameraDistance);
			try {
				for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
					mcl.findRotationAxis(true);
				}
			} catch (Exception ex) {
				mCC.showErrorDialog(ex);
				throw ex;
			}
		}

		@Override
		protected void switchOff(MotionControlComposite mCC) throws Exception {
			deSelectControl(mCC.btnFindAxisOfRotation);
			logger.debug("Half Rotation Tool - is de-selected");
			enable(mCC.moduleButtonComposite);
			enable(mCC.coarseRotation);
			enable(mCC.fineRotation);
			enable(mCC.btnLeftRotate);
			enable(mCC.btnRightRotate);
			enable(mCC.btnVertical);
			enable(mCC.btnHorizontal);
			enable(mCC.btnTilt);

			enable(mCC.txtXrayEnergy);
			enable(mCC.btnSampleWeight10to50);
			enable(mCC.btnSampleWeight1to10);
			enable(mCC.btnSampleWeightLessThan1);
			enable(mCC.txtCameraDistance);
			if (mCC.isRotationAxisFound()) {
				enable(mCC.btnMoveAxisOfRotation);
			}

			for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
				mcl.findRotationAxis(false);
			}

		}
	},
	VERTICAL {
		@Override
		protected void switchOn(MotionControlComposite mCC) throws Exception {
			logger.debug("'Vertical' - is selected");
			/**/
			/**/
			selectControl(mCC.btnVertical);
			/**/
			deSelectControl(mCC.btnTilt);
			deSelectControl(mCC.btnHorizontal);
			deSelectControl(mCC.btnMoveAxisOfRotation);
			/**/
			disable(mCC.moduleButtonComposite);
			disable(mCC.coarseRotation);
			disable(mCC.fineRotation);
			disable(mCC.btnLeftRotate);
			disable(mCC.btnRightRotate);
			disable(mCC.btnFindAxisOfRotation);
			disable(mCC.btnHorizontal);
			disable(mCC.btnMoveAxisOfRotation);

			disable(mCC.btnTilt);

			disable(mCC.txtXrayEnergy);
			disable(mCC.btnSampleWeightLessThan1);
			disable(mCC.btnSampleWeight10to50);
			disable(mCC.btnSampleWeight1to10);
			disable(mCC.txtCameraDistance);
			//
			try {
				for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
					mcl.vertical(true);
				}
			} catch (Exception ex) {
				mCC.showErrorDialog(ex);
				throw ex;
			}

		}

		@Override
		protected void switchOff(MotionControlComposite mCC) throws Exception {
			logger.debug("'Vertical' - is de-selected");
			deSelectControl(mCC.btnVertical);
			/**/
			enable(mCC.moduleButtonComposite);
			enable(mCC.coarseRotation);
			enable(mCC.fineRotation);
			enable(mCC.btnLeftRotate);
			enable(mCC.btnRightRotate);
			enable(mCC.btnHorizontal);
			enable(mCC.btnTilt);
			enable(mCC.btnFindAxisOfRotation);

			enable(mCC.txtXrayEnergy);
			enable(mCC.btnSampleWeight10to50);
			enable(mCC.btnSampleWeight1to10);
			enable(mCC.btnSampleWeightLessThan1);
			enable(mCC.txtCameraDistance);
			if (mCC.isRotationAxisFound()) {
				enable(mCC.btnMoveAxisOfRotation);
			}

			/**/
			for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
				mcl.vertical(false);
			}
		}
	},
	MOVE_AXIS_OF_ROTATION {

		@Override
		protected void switchOff(MotionControlComposite mCC) throws Exception {
			logger.debug("'Center Axis of Rotation' - is de-selected");
			deSelectControl(mCC.btnMoveAxisOfRotation);
			/**/
			enable(mCC.moduleButtonComposite);
			enable(mCC.coarseRotation);
			enable(mCC.fineRotation);
			enable(mCC.btnLeftRotate);
			enable(mCC.btnRightRotate);

			enable(mCC.btnHorizontal);
			enable(mCC.btnMoveAxisOfRotation);
			enable(mCC.btnFindAxisOfRotation);
			enable(mCC.btnVertical);
			enable(mCC.btnTilt);

			enable(mCC.txtXrayEnergy);
			enable(mCC.btnSampleWeight10to50);
			enable(mCC.btnSampleWeight1to10);
			enable(mCC.btnSampleWeightLessThan1);
			enable(mCC.txtCameraDistance);

			/**/
			for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
				mcl.moveAxisOfRotation(false);
			}
		}

		@Override
		protected void switchOn(MotionControlComposite mCC) throws Exception {
			logger.debug("'Move Axis Of Rotation' - is selected");
			/**/
			/**/
			selectControl(mCC.btnMoveAxisOfRotation);
			/**/
			deSelectControl(mCC.btnTilt);
			deSelectControl(mCC.btnHorizontal);
			/**/
			disable(mCC.moduleButtonComposite);
			disable(mCC.coarseRotation);
			disable(mCC.fineRotation);
			disable(mCC.btnLeftRotate);
			disable(mCC.btnRightRotate);
			disable(mCC.btnFindAxisOfRotation);
			disable(mCC.btnHorizontal);
			disable(mCC.btnVertical);

			disable(mCC.btnTilt);

			disable(mCC.txtXrayEnergy);
			disable(mCC.btnSampleWeight10to50);
			disable(mCC.btnSampleWeight1to10);
			disable(mCC.btnSampleWeightLessThan1);
			disable(mCC.txtCameraDistance);
			try {
				for (IMotionControlListener mcl : mCC.getMotionControlListeners()) {
					mcl.moveAxisOfRotation(true);
				}
			} catch (Exception ex) {
				mCC.showErrorDialog(ex);
				switchOff(mCC);
				throw ex;
			}

		}
	};

	private static void enable(Control control) {
		control.setEnabled(true);
	}

	private static void disable(Control control) {
		control.setEnabled(false);
	}

	protected abstract void switchOff(MotionControlComposite motionControlComposite) throws Exception;

	protected abstract void switchOn(MotionControlComposite motionControlComposite) throws Exception;

	private static final Logger logger = LoggerFactory.getLogger(MotionControlCentring.class);

	/**
	 * Method to run in the UI thread to set the colors to show the button selected
	 * 
	 * @param btnCntrl
	 */
	public static void selectControl(final Button btnCntrl) {
		btnCntrl.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				btnCntrl.setForeground(ColorConstants.red);
				btnCntrl.setBackground(ColorConstants.lightGray);

			}
		});
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button de-selected
	 * 
	 * @param btnCntrl
	 */
	public static void deSelectControl(final Button btnCntrl) {
		if (btnCntrl != null && !btnCntrl.isDisposed()) {
			btnCntrl.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					btnCntrl.setForeground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
					btnCntrl.setBackground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				}
			});
		}
	}
}