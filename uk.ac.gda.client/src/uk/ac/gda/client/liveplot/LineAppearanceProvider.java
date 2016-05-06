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

package uk.ac.gda.client.liveplot;

import java.awt.Color;
import java.util.Vector;

import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DStyles;
import org.eclipse.dawnsci.plotting.api.jreality.util.PlotColorUtility;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.rcp.GDAClientActivator;
import uk.ac.gda.preferences.PreferenceConstants;

public class LineAppearanceProvider {

	private static final Logger logger = LoggerFactory.getLogger(LineAppearanceProvider.class);
	private boolean showErrorBar = false;

	public LineAppearanceProvider() {
		GDAClientActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Object newValue = event.getNewValue();
				String sValue = newValue instanceof String ? (String) newValue : null;
				if (event.getProperty().equals(PreferenceConstants.GDA_CLIENT_PLOT_COLORS)) {
					refreshColors(sValue);
				} else if (event.getProperty().equals(PreferenceConstants.GDA_CLIENT_PLOT_LINESTYLES)) {
					refreshLineStyles(sValue);
				} else if (event.getProperty().equals(PreferenceConstants.GDA_CLIENT_PLOT_LINEWIDTH)) {
					if (newValue instanceof Integer) {
						refreshLineWidth((Integer) newValue);
					} else if (sValue != null && !sValue.isEmpty()) {
						try {
							refreshLineWidth(Integer.valueOf(sValue));
						} catch (NumberFormatException e) {
							logger.warn("Unable to get Integer from " + sValue, e);
						}
					}
				} else if (event.getProperty().equals(PreferenceConstants.GDA_CLIENT_PLOT_ERRORBAR)) {
					if (newValue instanceof Boolean)
						refreshLineErrorBar((Boolean) newValue);
				}
			}
		});
		IPreferenceStore preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();
		refreshLineStyles(preferenceStore.getString(PreferenceConstants.GDA_CLIENT_PLOT_LINESTYLES));
		refreshColors(preferenceStore.getString(PreferenceConstants.GDA_CLIENT_PLOT_COLORS));
		refreshLineWidth(preferenceStore.getInt(PreferenceConstants.GDA_CLIENT_PLOT_LINEWIDTH));
		refreshLineErrorBar(preferenceStore.getBoolean(PreferenceConstants.GDA_CLIENT_PLOT_ERRORBAR));

	}

	private void refreshLineErrorBar(boolean boolean1) {
		showErrorBar = boolean1;

	}

	protected void refreshLineWidth(Integer pref) {
		if (pref > 0) {
			lineWidth = pref;
		}
	}

	private String[] getStringArrayFromCSV(String pref) {
		if(!StringUtils.hasLength(pref))
			return new String[]{};
		pref = pref.replace(" ", "");
		return pref.split(",");
	}

	private Plot1DStyles getStyleFromInteger(int style) {
		switch (style) {
		case 0:
			return Plot1DStyles.SOLID;
		case 1:
			return Plot1DStyles.DASHED;
		case 2:
			return Plot1DStyles.POINT;
		case 3:
			return Plot1DStyles.SOLID_POINT;
		case 4:
			return Plot1DStyles.DASHED_POINT;
		}
		return Plot1DStyles.SOLID_POINT;
	}

	protected void refreshLineStyles(String pref) {
		String[] strings = getStringArrayFromCSV(pref);
		Vector<Plot1DStyles> vvals = new Vector<Plot1DStyles>();
		for (String s : strings) {
			try {
				if (!s.isEmpty()){
					vvals.add(getStyleFromInteger(Integer.valueOf(s)));
				}
			} catch (NumberFormatException e) {
				logger.warn("Unable to get style from " + s, e);
			}
		}
		if (vvals.size() > 0) {
			styles = vvals.toArray(new Plot1DStyles[] {});
		}
	}

	protected void refreshColors(String pref) {
		String[] strings = getStringArrayFromCSV(pref);
		Vector<Color> vvals = new Vector<Color>();
		for (String s : strings) {
			try {
				if (!s.isEmpty()){
					vvals.add(new Color(Integer.valueOf(s, 16)));
				}
			} catch (NumberFormatException e) {
				logger.warn("Unable to get color from " + s, e);
			}
		}
		if (vvals.size() > 0) {
			colors = vvals.toArray(new Color[] {});
		}
	}

	Color[] colors = null;

	public Color getColour(int nr) {
		return colors != null ? colors[nr % colors.length] : PlotColorUtility.getDefaultColour(nr);
	}

	int lineWidth = 0;

	public int getLineWidth() {
		return lineWidth > 0 ? lineWidth : PlotColorUtility.getDefaultLineWidth(0);

	}

	Plot1DStyles[] styles = null;

	public Plot1DStyles getStyle(int nr) {
		return styles != null ? styles[nr % styles.length] : PlotColorUtility.getDefaultStyle(nr);
	}

	public boolean showErrorBar() {
		return showErrorBar;
	}

}
