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

package gda.util.userOptions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.configuration.ConfigurationException;

/**
 * UserOptionsDialog Class
 */
public class UserOptionsDialog extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JFrame frame;

	JLabel label;

	UserOptions options;

	boolean ok = false;

	/**
	 * @return boolean
	 */
	public boolean getOK() {
		return ok;
	}

	HashMap<String, Component> components = new HashMap<String, Component>();

	/**
	 * @param frame
	 * @param parent
	 * @param options
	 */
	public UserOptionsDialog(JFrame frame, Component parent, UserOptions options) {
		super(frame, options.title, true);
		this.frame = frame;
		this.options = options;
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		label = new JLabel(options.title != null ? options.title : "");
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel pane = makePane();
		// pane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		pane.setAlignmentX(Component.CENTER_ALIGNMENT);
		// pane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.add(label);
		panel.add(pane);

		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));

		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		JButton defButton = new JButton("Default");
		defButton.addActionListener(this);

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(this);

		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(okButton);
		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(cancelButton);
		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(defButton);
		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(resetButton);
		btnPanel.add(Box.createHorizontalGlue());

		btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(btnPanel);

		getContentPane().add(panel);
		getRootPane().setDefaultButton(cancelButton);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	@SuppressWarnings("rawtypes")
	private JPanel makePane() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		Iterator<Map.Entry<String, UserOption>> iter = options.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, UserOption> entry = iter.next();
			UserOption option = entry.getValue();
			if (option.defaultValue instanceof Boolean && option.description instanceof String) {
				JCheckBox box = new JCheckBox((String) option.description);
				box.setSelected((Boolean) option.value);
				box.setAlignmentX(Component.LEFT_ALIGNMENT);
				box.setAlignmentY(Component.LEFT_ALIGNMENT);
				components.put(entry.getKey(), box);
				pane.add(box);
			}
		}
		return pane;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Default")) {
			Iterator<Map.Entry<String, Component>> iter = components.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Component> entry = iter.next();
				UserOption option = options.get(entry.getKey());
				if (option.defaultValue instanceof Boolean) {
					((JCheckBox) entry.getValue()).setSelected((Boolean) option.defaultValue);
				}
			}
			return;
		}
		if (e.getActionCommand().equals("Reset")) {
			Iterator<Map.Entry<String, Component>> iter = components.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Component> entry = iter.next();
				UserOption option = options.get(entry.getKey());
				if (option.defaultValue instanceof Boolean) {
					((JCheckBox) entry.getValue()).setSelected((Boolean) option.value);
				}
			}
			return;
		}
		if (e.getActionCommand().equals("OK")) {
			Iterator<Map.Entry<String, Component>> iter = components.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Component> entry = iter.next();
				UserOption option = options.get(entry.getKey());
				if (option.defaultValue instanceof Boolean) {
					option.value = ((JCheckBox) entry.getValue()).isSelected();
				}
			}
		}
		ok = e.getActionCommand().equals("OK");
		setVisible(false);
		dispose();
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked from the event-dispatching thread.
	 * 
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private static void createAndShowGUI() throws ConfigurationException, IOException, Exception {
		String TestFileFolder = "test/gda/util/UserOptions/UserOptionsTestFiles";
		UserOptions options = new UserOptions();
		options.title = "Test options";
		options.put("key1", new UserOption<String, Boolean>("Option1", true, null));
		options.put("key2", new UserOption<String, Boolean>("Option2", true));
		options.put("key3", new UserOption<String, Boolean>("Option3", false, false));
		options.put("key4", new UserOption<String, Boolean>("Option4", false, true));
		options.put("key5", new UserOption<String, Boolean>("Option5", true, false));
		// UserOptionsDialog newContentPane = new UserOptionsDialog(null, null,
		// options);
		options.saveValuesToConfig(TestFileFolder, "userOptionsDialogValues");
		options.saveToTemplate(TestFileFolder, "userOptionsDialogConfig");
		UserOptions newOptions = UserOptions.createFromTemplate(TestFileFolder, "userOptionsDialogConfig");
		newOptions.setValuesFromConfig(TestFileFolder, "userOptionsDialogValues");
		new UserOptionsDialog(null, null, newOptions);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					createAndShowGUI();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
}
