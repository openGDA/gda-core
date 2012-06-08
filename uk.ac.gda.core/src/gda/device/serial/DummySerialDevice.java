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

package gda.device.serial;

import gda.device.DeviceException;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows you to pretend to be a serial device
 */
public class DummySerialDevice extends SerialBase implements ActionListener, KeyListener {
	
	private static final Logger logger = LoggerFactory.getLogger(DummySerialDevice.class);
	
	// private SerialPort serialPort;
	// private OutputStream outputStream;
	// private InputStream inputStream;
	// private boolean initialized = false;
	private JFrame jframe = new JFrame();

	private JPanel jpanel = new JPanel(new FlowLayout());

	private JTextField requestTextField;

	private JTextField replyTextField;

	// private String name;
	// private int baudRate;
	// private int byteSize;
	// private int parity;
	// private int stopBits;
	// private int flowControlIn;
	// private int flowControlOut;
	// private int flowControlMode;
	private StringBuffer inputBuffer = new StringBuffer();

	private StringBuffer outputBuffer = new StringBuffer();

	private int outputBufferLength = 0;

	/**
	 * Constructor
	 */
	public DummySerialDevice() {
		logger.info("Creating DummySerialDevice");
		requestTextField = new JTextField(20);
		replyTextField = new JTextField(20);
		// replyTextField.addActionListener(this);
		replyTextField.addKeyListener(this);
		jpanel.add(requestTextField);
		jpanel.add(replyTextField);
		jframe.getContentPane().add(jpanel);
		jframe.pack();
		jframe.setVisible(true);
	}

	@Override
	public void configure(){
		// no configuration required
	}

	@Override
	public void setBaudRate(int baudRate) throws DeviceException {
	}

	@Override
	public void setByteSize(int byteSize) throws DeviceException {
	}

	@Override
	public void setParity(String parity) throws DeviceException {
	}

	@Override
	public void setStopBits(int stopBits) throws DeviceException {
	}

	@Override
	public void setFlowControl(String flowControl) throws DeviceException {
	}

	@Override
	public int getReadTimeout() throws DeviceException {
		// FIXME should return some random value in milliseconds instead of zero
		return 0;
	}

	// Method for closing the port.

	@Override
	public void close() {
	}

	// CharReadableDev methods ...

	@Override
	public void setReadTimeout(int time) throws DeviceException {
	}

	@Override
	public char readChar() throws DeviceException {
		char toReturn;

		// return next char in buffer

		synchronized (this) {
			try {
				while (outputBufferLength == 0)
					wait();
			} catch (InterruptedException ie) {
			}
		}

		toReturn = outputBuffer.charAt(0);
		outputBuffer.deleteCharAt(0);
		outputBufferLength = outputBuffer.length();

		if (toReturn == '\r')
			logger.debug("readChar returning: <CR>");
		else if (toReturn == '\n')
			logger.debug("readChar returning: <LF>");
		else
			logger.debug("readChar returning: " + toReturn);
		return toReturn;
	}

	@Override
	public void flush() throws DeviceException {
		// empty buffer
	}

	// CharWritableDev methods ...

	@Override
	public void writeChar(char c) throws DeviceException {
		// FIXME display this char somehow, replace System.out
		logger.debug("DummySerialDevice given " + c);
		addToInputBuffer(c);
		requestTextField.setText(inputBuffer.toString());
	}

	/**
	 * Add EOL char to the end of the input buffer
	 * 
	 * @param toBeAdded
	 *            char
	 */
	private void addToInputBuffer(char toBeAdded) {
		if (toBeAdded == '\n')
			inputBuffer.append("<LF>");
		else if (toBeAdded == '\r')
			inputBuffer.append("<CR>");
		else
			inputBuffer.append(toBeAdded);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		outputBuffer.append(replyTextField.getText());
		outputBuffer.append('\r');
		outputBufferLength = outputBuffer.length();
		replyTextField.setText("");
		synchronized (this) {
			notify();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c;
		if (e.getModifiers() == InputEvent.CTRL_MASK) {
			c = (char) (e.getKeyChar() - 96);
			replyTextField.setText(replyTextField.getText() + "<ctrl-" + e.getKeyChar() + ">");
		} else if (e.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)) {
			c = (char) (e.getKeyChar() - 64);
			replyTextField.setText(replyTextField.getText() + "<ctrl-" + e.getKeyChar() + ">");
		} else {
			c = e.getKeyChar();
		}
		outputBuffer.append(c);
		outputBufferLength = outputBuffer.length();

		synchronized (this) {
			notify();
		}
	}
}
