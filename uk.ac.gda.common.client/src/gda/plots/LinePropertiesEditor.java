/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.plots;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;

/**
 * JDialog that provides utilities to enable editing of line properties
 */
class LinePropertiesEditor extends JDialog implements ActionListener, SeriesChangeListener {
	private SimpleXYSeries currentLine = null;

	private JTextField nameField;

	private JColorChooser colourChooser = new JColorChooser();

	private JDialog colourDialog = JColorChooser.createDialog(LinePropertiesEditor.this, null, true, colourChooser,
			this, null);

	private JButton lineColourButton;

	private boolean settingLineColour = false;

	private boolean settingMarkerColour = false;

	private EnumChooser typesCombo;

	private JButton markerColourButton;

	private EnumChooser patternsCombo;

	private EnumChooser markersCombo;

	private JSpinner markerSizeSpinner;

	private JSpinner lineThicknessSpinner;

	/**
	 * Constructor
	 */
	public LinePropertiesEditor() {
		JPanel overallPanel = new JPanel(new BorderLayout());

		overallPanel.add(createSettingsPanel(true), BorderLayout.CENTER);
		overallPanel.add(createButtonsPanel(false), BorderLayout.SOUTH);
		add(overallPanel);

		pack();
	}

	/**
	 * @param parent
	 * @param title
	 * @param message
	 * @param sxys
	 * @param allProperties
	 */
	LinePropertiesEditor(JFrame parent, String title, @SuppressWarnings("unused") String message,
			SimpleXYSeries sxys, boolean allProperties) {
		super(parent, title, true);
		if (parent != null) {
			Dimension parentSize = parent.getSize();
			Point p = parent.getLocation();
			setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
		}
		getContentPane().add(createSettingsPanel(allProperties), BorderLayout.CENTER);
		getContentPane().add(createButtonsPanel(true), BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setCurrentLine(sxys);
		setVisible(true);
	}

	private JPanel createSettingsPanel(boolean allProperties) {
		JPanel settingsPanel = new JPanel(new BorderLayout());

		JPanel labels = new JPanel(new GridLayout(8, 0, 0, 5));
		JPanel widgets = new JPanel(new GridLayout(8, 0, 0, 5));

		colourChooser.setPreviewPanel(new JPanel());
		setTitle("LinePropertiesEditor");

		labels.add(new JLabel("Line Name"));
		nameField = new JTextField();
		widgets.add(nameField);

		labels.add(new JLabel("Line Colour"));
		lineColourButton = new JButton();
		lineColourButton.setIcon(new SimpleIcon(30, lineColourButton.getFontMetrics(lineColourButton.getFont())
				.getHeight()));
		lineColourButton.setVerticalTextPosition(SwingConstants.BOTTOM);
		lineColourButton.setHorizontalTextPosition(SwingConstants.RIGHT);
		lineColourButton.setIconTextGap(10);
		lineColourButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				colourChooser.setColor((Color) currentLine.getPaint());
				settingLineColour = true;
				colourDialog.setVisible(true);
			}
		});
		widgets.add(lineColourButton);

		if (allProperties) {
			labels.add(new JLabel("Line Thickness "));
			lineThicknessSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
			widgets.add(lineThicknessSpinner);

			labels.add(new JLabel("Line Type"));
			typesCombo = new EnumChooser(gda.plots.Type.class);
			widgets.add(typesCombo);

			labels.add(new JLabel("Line Pattern"));
			patternsCombo = new EnumChooser(gda.plots.Pattern.class);
			widgets.add(patternsCombo);

			labels.add(new JLabel("MarkerColour"));
			markerColourButton = new JButton();
			markerColourButton.setIcon(new SimpleIcon(Marker.BOX, 18, markerColourButton.getFontMetrics(
					markerColourButton.getFont()).getHeight()));
			markerColourButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					colourChooser.setColor((Color) currentLine.getSymbolPaint());
					settingMarkerColour = true;
					colourDialog.setVisible(true);
				}
			});
			widgets.add(markerColourButton);
		}

		labels.add(new JLabel("Marker"));
		markersCombo = new EnumChooser(gda.plots.Marker.class);
		widgets.add(markersCombo);

		if (allProperties) {
			labels.add(new JLabel("Marker Size"));
			markerSizeSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
			widgets.add(markerSizeSpinner);
		}

		settingsPanel.add(labels, BorderLayout.WEST);
		settingsPanel.add(widgets, BorderLayout.CENTER);
		settingsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory
				.createEmptyBorder(5, 5, 5, 5)));
		return settingsPanel;
	}

	private JPanel createButtonsPanel(final boolean dispose) {
		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
				setVisible(false);
				if (dispose)
					dispose();
			}
		});

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				if (dispose)
					dispose();
			}
		});

		buttonsPanel.add(okButton);
		buttonsPanel.add(applyButton);
		buttonsPanel.add(cancelButton);

		return buttonsPanel;
	}

	/**
	 * Sets the line whose properties are being edited
	 *
	 * @param newCurrentLine
	 *            the line to be edited (a SimpleXYSeries)
	 */
	public void setCurrentLine(SimpleXYSeries newCurrentLine) {
		if (newCurrentLine != null) {
			if (currentLine != null) {
				currentLine.removeChangeListener(this);
			}
			currentLine = newCurrentLine;
			currentLine.addChangeListener(this);
		}
		displayCurrentProperties();

	}

	@Override
	public void seriesChanged(SeriesChangeEvent event) {
		// Temporarily commented out. If the program changes the line properties
		// while this is visible should redisplay. However if the program
		// changes
		// only the data and the user has already changed something in this but
		// not applied it then the redisplay causes the user's change to vanish.
		// Making it impossible, for example, to change the colour of a line
		// during a scan unless you catch it at exactly the right moment.
		// displayCurrentProperties();
	}

	private void displayCurrentProperties() {
		nameField.setText(currentLine.getName());
		lineColourButton.setForeground((Color) currentLine.getPaint());
		lineColourButton.setText(XColors.name((Color) currentLine.getPaint()));
		if (lineThicknessSpinner != null)
			lineThicknessSpinner.setValue(currentLine.getLineWidth());
		if (typesCombo != null)
			typesCombo.setSelectedItem(currentLine.getType());
		if (patternsCombo != null)
			patternsCombo.setSelectedItem(currentLine.getPattern());
		if (markerColourButton != null)
			markerColourButton.setForeground((Color) currentLine.getSymbolPaint());
		markersCombo.setSelectedItem(currentLine.getMarker());

		if (markerSizeSpinner != null)
			markerSizeSpinner.setValue(currentLine.getSymbolSize());
	}

	private void apply() {
		currentLine.removeChangeListener(this);
		currentLine.setName(nameField.getText());
		currentLine.setPaint(lineColourButton.getForeground());
		if (lineThicknessSpinner != null)
			currentLine.setLineWidth((Integer) lineThicknessSpinner.getValue());
		if (typesCombo != null)
			currentLine.setType((gda.plots.Type) typesCombo.getSelectedItem());
		if (patternsCombo != null)
			currentLine.setPattern((Pattern) patternsCombo.getSelectedItem());
		if (markerColourButton != null)
			currentLine.setSymbolPaint(markerColourButton.getForeground());
		currentLine.setMarker((Marker) markersCombo.getSelectedItem());
		if (markerSizeSpinner != null)
			currentLine.setMarkerSize((Integer) markerSizeSpinner.getValue());
		currentLine.addChangeListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (settingLineColour) {
			lineColourButton.setForeground(colourChooser.getColor());
			lineColourButton.setText(XColors.name(colourChooser.getColor()));
			settingLineColour = false;
		} else if (settingMarkerColour) {
			markerColourButton.setForeground(colourChooser.getColor());
			markerColourButton.setText(XColors.name(colourChooser.getColor()));
			settingMarkerColour = false;
		}
	}
}
