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

package uk.ac.gda.client.tomo.composites;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.ui.components.PointInDouble;

/**
 * Zoom button composite
 */
public class ZoomButtonComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(ZoomButtonComposite.class);

	private static final String ZOOM_HALF = "0.5";
	private static final String ZOOM_HEADER_LBL = "ZOOM";
	private static final String ZOOM_4 = "4";
	private static final String ZOOM_2 = "2";
	private static final String ZOOM_1 = "1";

	/**/
	/* Zoom buttons */
	private Button btnZoom4;
	private Button btnZoom2;
	private Button btnZoom1;
	private Button btnZoomHalf;

	/**
	 * Zoom buttons specific enums
	 */
	public enum ZOOM_LEVEL {

		NO_ZOOM(0) {
			@Override
			public Dimension getRectSize() {
				// returning null size for no zoom
				return null;
			}

			@Override
			protected void toggleZoomButton(ZoomButtonComposite cmp) {
				deSelectControl(cmp.btnZoom1);
				deSelectControl(cmp.btnZoom2);
				deSelectControl(cmp.btnZoom4);
				deSelectControl(cmp.btnZoomHalf);
				for (ZoomButtonActionListener actionListener : cmp.actionListeners) {
					try {
						actionListener.zoomButtonClicked(ZOOM_LEVEL.NO_ZOOM);
					} catch (Exception e) {
						cmp.showError("Problem enabling zoom", e);
					}
				}
			}

			@Override
			public Point getBin() {
				// TODO-Ravi Auto-generated method stub
				return null;
			}

			@Override
			public Point getRoiSize() {
				// TODO-Ravi Auto-generated method stub
				return null;
			}

			@Override
			public PointInDouble getDemandRawScale() {
				// TODO-Ravi Auto-generated method stub
				return null;
			}
		},
		/**
		 * "1"
		 */
		ZOOM_ONE(1) {

			@Override
			public Dimension getRectSize() {
				return new Dimension(182, 182);
			}

			@Override
			protected void toggleZoomButton(ZoomButtonComposite cmp) {
				selectControl(cmp.btnZoom1);
				deSelectControl(cmp.btnZoom2);
				deSelectControl(cmp.btnZoom4);
				deSelectControl(cmp.btnZoomHalf);
				try {
					for (ZoomButtonActionListener actionListener : cmp.actionListeners) {
						actionListener.zoomButtonClicked(ZOOM_LEVEL.ZOOM_ONE);
					}
				} catch (Exception ex) {
					deSelectControl(cmp.btnZoom1);
				}
			}

			@Override
			public Point getBin() {
				return new Point(1, 1);
			}

			@Override
			public Point getRoiSize() {
				return new Point(728, 728);
			}

			@Override
			public PointInDouble getDemandRawScale() {
				return new PointInDouble(1, 1);
			}
		},
		/**
		 * "2"
		 */
		ZOOM_TWO(2) {

			@Override
			public Dimension getRectSize() {
				return new Dimension(92, 92);
			}

			@Override
			protected void toggleZoomButton(ZoomButtonComposite cmp) {
				selectControl(cmp.btnZoom2);
				deSelectControl(cmp.btnZoom1);
				deSelectControl(cmp.btnZoom4);
				deSelectControl(cmp.btnZoomHalf);
				try {
					for (ZoomButtonActionListener actionListener : cmp.actionListeners) {
						actionListener.zoomButtonClicked(ZOOM_LEVEL.ZOOM_TWO);
					}
				} catch (Exception ex) {
					deSelectControl(cmp.btnZoom2);
				}
			}

			@Override
			public Point getBin() {
				return new Point(1, 1);
			}

			@Override
			public Point getRoiSize() {
				return new Point(368, 368);
			}

			@Override
			public PointInDouble getDemandRawScale() {
				return new PointInDouble(2, 2);
			}
		},
		/**
		 * "4"
		 */
		ZOOM_FOUR(4) {

			@Override
			public Dimension getRectSize() {
				return new Dimension(46, 46);
			}

			@Override
			protected void toggleZoomButton(ZoomButtonComposite cmp) {
				selectControl(cmp.btnZoom4);
				deSelectControl(cmp.btnZoom2);
				deSelectControl(cmp.btnZoom1);
				deSelectControl(cmp.btnZoomHalf);
				try {
					for (ZoomButtonActionListener actionListener : cmp.actionListeners) {
						actionListener.zoomButtonClicked(ZOOM_LEVEL.ZOOM_FOUR);
					}
				} catch (Exception ex) {
					deSelectControl(cmp.btnZoom4);
				}
			}

			@Override
			public Point getBin() {
				return new Point(1, 1);
			}

			@Override
			public Point getRoiSize() {
				return new Point(184, 184);
			}

			@Override
			public PointInDouble getDemandRawScale() {
				return new PointInDouble(4, 4);
			}
		},
		/**
		 * "0.5"
		 */
		@SuppressWarnings("hiding")
		ZOOM_HALF(0.5) {
			@Override
			public Dimension getRectSize() {
				return new Dimension(363, 363);
			}

			@Override
			protected void toggleZoomButton(ZoomButtonComposite cmp) {
				selectControl(cmp.btnZoomHalf);
				deSelectControl(cmp.btnZoom4);
				deSelectControl(cmp.btnZoom2);
				deSelectControl(cmp.btnZoom1);
				try {
					for (ZoomButtonActionListener actionListener : cmp.actionListeners) {
						actionListener.zoomButtonClicked(ZOOM_LEVEL.ZOOM_HALF);
					}
				} catch (Exception ex) {
					deSelectControl(cmp.btnZoomHalf);
				}
			}

			@Override
			public Point getBin() {
				return new Point(1, 1);
			}

			@Override
			public Point getRoiSize() {
				return new Point(1452, 1452);
			}

			@Override
			public PointInDouble getDemandRawScale() {
				return new PointInDouble(0.5, 0.5);
			}
		};

		private final double val;

		public abstract Dimension getRectSize();

		protected abstract void toggleZoomButton(ZoomButtonComposite zoomButtonComposite);

		private static void selectControl(final Button btnCntrl) {
			if (btnCntrl != null && !btnCntrl.isDisposed()) {
				btnCntrl.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						btnCntrl.setForeground(ColorConstants.red);
						btnCntrl.setBackground(ColorConstants.lightGray);
					}
				});
			}
		}

		private static void deSelectControl(final Button btnCntrl) {
			if (btnCntrl != null && !btnCntrl.isDisposed()) {
				btnCntrl.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						btnCntrl.setForeground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
						btnCntrl.setBackground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

					}
				});
			}
		}

		public abstract Point getBin();

		public abstract Point getRoiSize();

		public abstract PointInDouble getDemandRawScale();

		public double getValue() {
			return val;
		}

		ZOOM_LEVEL(double val) {
			this.val = val;
		}
	}

	private List<ZoomButtonActionListener> actionListeners = new ArrayList<ZoomButtonComposite.ZoomButtonActionListener>();

	/**
	 * Action listener to propagate that the button has been clicked.
	 */
	public interface ZoomButtonActionListener {

		/**
		 * @param zoomLevel
		 * @throws IllegalStateException
		 * @throws Exception
		 */
		void zoomButtonClicked(ZOOM_LEVEL zoomLevel) throws Exception;
	}

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
	 * @param al
	 * @return <code>true</code> when the action listener is added successfully.
	 */
	public boolean addZoomButtonActionListener(ZoomButtonActionListener al) {
		return actionListeners.add(al);
	}

	public boolean removeZoomButtonActionListener(ZoomButtonActionListener al) {
		return actionListeners.remove(al);
	}

	private SelectionListener buttonSelectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
			Object sourceObj = e.getSource();
			if (sourceObj == btnZoom1) {
				if (!isSelected(btnZoom1)) {
					logger.info("'Zoom 1' is selected");
					setZoomLevel(ZOOM_LEVEL.ZOOM_ONE);
				} else {
					logger.info("'Zoom 1' is de-selected");
					setZoomLevel(ZOOM_LEVEL.NO_ZOOM);
				}
			} else if (sourceObj == btnZoom2) {
				if (!isSelected(btnZoom2)) {
					logger.info("'Zoom 2' is selected");
					setZoomLevel(ZOOM_LEVEL.ZOOM_TWO);
				} else {
					logger.info("'Zoom 2' is de-selected");
					setZoomLevel(ZOOM_LEVEL.NO_ZOOM);
				}
			} else if (sourceObj == btnZoom4) {
				if (!isSelected(btnZoom4)) {
					logger.info("'Zoom 4' is selected");
					setZoomLevel(ZOOM_LEVEL.ZOOM_FOUR);
				} else {
					logger.info("'btnZoom4' is de-selected");
					setZoomLevel(ZOOM_LEVEL.NO_ZOOM);
				}
			} else if (sourceObj == btnZoomHalf) {
				if (!isSelected(btnZoomHalf)) {
					logger.info("'Zoom 0.5' is selected");
					setZoomLevel(ZOOM_LEVEL.ZOOM_HALF);
				} else {
					logger.info("'Zoom 0.5' is de-selected");
					setZoomLevel(ZOOM_LEVEL.NO_ZOOM);
				}
			}
		}
	};

	private ZOOM_LEVEL zoomLevel = ZOOM_LEVEL.NO_ZOOM;
	
	private static final String BOLD_TEXT_10 = "bold_10";
	private FontRegistry fontRegistry;

	private void initializeFontRegistry() {
		if (getDisplay() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_10, new FontData[] { new FontData(fontName, 10, SWT.BOLD) });
		}
	}

	//
	/**
	 * @param parent
	 */
	public ZoomButtonComposite(Composite parent, FormToolkit toolkit) {
		super(parent, SWT.None);
		initializeFontRegistry();
		GridLayout layout = new GridLayout(2, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		this.setLayout(layout);

		Label lblZoom = toolkit.createLabel(this, ZOOM_HEADER_LBL, SWT.CENTER);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		lblZoom.setFont(fontRegistry.get(BOLD_TEXT_10));
		lblZoom.setLayoutData(layoutData);

		/**/
		btnZoom4 = toolkit.createButton(this, ZOOM_4, SWT.PUSH);
		GridData ld = new GridData(GridData.FILL_HORIZONTAL);
		btnZoom4.setLayoutData(ld);
		btnZoom4.addSelectionListener(buttonSelectionListener);
		/**/
		btnZoom2 = toolkit.createButton(this, ZOOM_2, SWT.PUSH);
		ld = new GridData(GridData.FILL_HORIZONTAL);
		btnZoom2.setLayoutData(ld);
		btnZoom2.addSelectionListener(buttonSelectionListener);
		/**/
		btnZoom1 = toolkit.createButton(this, ZOOM_1, SWT.PUSH);
		ld = new GridData(GridData.FILL_HORIZONTAL);
		btnZoom1.setLayoutData(ld);
		btnZoom1.addSelectionListener(buttonSelectionListener);
		/**/
		btnZoomHalf = toolkit.createButton(this, ZOOM_HALF, SWT.PUSH);
		ld = new GridData(GridData.FILL_HORIZONTAL);
		btnZoomHalf.setLayoutData(ld);
		btnZoomHalf.addSelectionListener(buttonSelectionListener);
	}

	/**
	 * @param button
	 * @return <code>true</code> when background color is lightgray and foreground color is red - this is what was set
	 *         when the widget was selected.
	 */
	private boolean isSelected(Button button) {
		if (button != null && !button.isDisposed()) {
			if (ColorConstants.red.equals(button.getForeground())
					&& ColorConstants.lightGray.equals(button.getBackground())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param zoomLevel
	 */
	public void setZoomLevel(ZOOM_LEVEL zoomLevel) {
		this.zoomLevel = zoomLevel;
		zoomLevel.toggleZoomButton(this);
	}

	/**
	 * @return {@link ZOOM_LEVEL} that is currently selected
	 */
	public ZOOM_LEVEL getSelectedZoom() {
		return zoomLevel;
	}

	@Override
	public void dispose() {
		btnZoom1.dispose();
		btnZoom2.dispose();
		btnZoom4.dispose();
		btnZoomHalf.dispose();
		//
		buttonSelectionListener = null;
		actionListeners.clear();
		super.dispose();
	}

	public void disableZoomButtons() {
		btnZoom1.setEnabled(false);
		btnZoom2.setEnabled(false);
		btnZoom4.setEnabled(false);
		btnZoomHalf.setEnabled(false);
	}

	public void enableZoomButtons() {
		btnZoom1.setEnabled(true);
		btnZoom2.setEnabled(true);
		btnZoom4.setEnabled(true);
		btnZoomHalf.setEnabled(true);
	}
}
