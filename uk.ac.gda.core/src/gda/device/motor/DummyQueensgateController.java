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

package gda.device.motor;

import gda.device.DeviceBase;
import gda.device.MotorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy controller class for Queensgate piezos. It manages serial communication with individual piezo command modules
 * (there can be up to three in a box), ensuring that replies return to the correct module. The class also manages the
 * tasks common to all piezos - converting to and from module replies.
 */
public class DummyQueensgateController extends DeviceBase implements PiezoController {

	private static final Logger logger = LoggerFactory.getLogger(DummyQueensgateController.class);

	// Singleton instance of controller
	private static final DummyQueensgateController queensgateController = new DummyQueensgateController();

	private String serialDeviceName;

	private static final String TERMINATOR = "\r\n";

	// private static final String DISABLE = "M01";
	private static final String START_CONVERSION = "N0";

	private static final String ADC_RESULT = "N4";

	private static final String SEND_REPLY = "?";

	private static final String ZERO_MODULES = "#0";

	private static final String SET_FOR_INPUT = "!QT";

	// private static final String POSITION_ZERO = "I000";
	// private static final String WRITE_TO_ALL_MODULES = "M73";

	private static final QueensgateControllerEmulator QGCEmulator = queensgateController.new QueensgateControllerEmulator();

	/**
	 * @return queensgateController
	 */
	public static DummyQueensgateController getInstance() {
		return queensgateController;
	}

	/**
	 * Constructor
	 */
	public DummyQueensgateController() {
	}

	@Override
	public void configure() {
		initialiseModules();
	}

	/**
	 * This method is required to setup the initial condition of the piezo. First, module 0 is selected (ie no modules)
	 * then input from the command module is enabled on Ports Q through T. The positions are set to zero on all modules
	 * then the modules are deselected and the input/output gate is disabled.
	 */
	private void initialiseModules() {
		logger.debug("DummyQueensgate Controller: init cmds " + ZERO_MODULES + SET_FOR_INPUT /*
																							 * + POSITION_ZERO +
																							 * WRITE_TO_ALL_MODULES +
																							 * DISABLE
																							 */);

		write(ZERO_MODULES);
		write(SET_FOR_INPUT);
		// write(POSITION_ZERO);
		// write(WRITE_TO_ALL_MODULES);
		// write(DISABLE); //TODO - is this needed? (QG manual & emu MAY
		// say
		// not needed)

	}

	/**
	 * @param serialDeviceName
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * @return serial device name
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * Used to send a command to the piezo module that does not require a reply.
	 * 
	 * @param module
	 *            is the module number
	 * @param positionCommand
	 *            - the position to set in command
	 * @throws MotorException
	 */
	@Override
	public synchronized void setPosition(int module, String positionCommand) throws MotorException {
		// README The communication is in binary form, so module 3 must take the
		// binary value of 100, which is 4 in base 10.
		if (module == 3) {
			module = 4;
		}

		String moduleCommand = "M" + module + "1";
		logger.debug("DummyQueensgate Controller: setposition cmd " + moduleCommand + positionCommand + "N3N1");

		write(moduleCommand);
		write(positionCommand);
		write("N3"); // TODO - QG manual & emu say this is needed
		write("N1"); // TODO - QG manual & emu say this is needed
		// write(DISABLE); //TODO - QG manual & emu say not needed
	}

	/**
	 * Sends the commands required to return the current rawPosition (offset), converts this value to a position
	 * (offset) and compares with requested position (offset). Notifies when the process is complete.
	 * 
	 * @param module
	 *            the module nos.
	 * @return string representation of current position
	 * @throws MotorException
	 */
	@Override
	public synchronized String getPositionAndStatus(int module) throws MotorException {
		// README The communication is in binary form, so module 3 must take the
		// binary value of 100, which is 4 in base 10.
		if (module == 3) {
			module = 4;
		}

		String moduleCommand = "M" + module + "4";
		String reply = null;
		logger.debug("DummyQueensgate Controller: getposition cmd " + moduleCommand + START_CONVERSION + ADC_RESULT
				+ SEND_REPLY);

		write(moduleCommand);
		write(START_CONVERSION);
		write(ADC_RESULT);
		write(SEND_REPLY);
		reply = read().trim();

		logger.debug("DummyQueensgate Controller: getposition reply " + module + " " + reply);

		// Message.out("Dummy Queensgate Controller: setposition cmd " +
		// DISABLE);

		// write(DISABLE); //TODO - QG manual & emu say not needed
		return reply;
	}

	// =========================================================================
	// EMULATE BEHAVIOUR OF A SERIAL READER & WRITER
	// =========================================================================

	private void write(String s) {
		s = s.concat(TERMINATOR);

		for (int i = 0; i < s.length(); i++) {
			QGCEmulator.writeChar(s.charAt(i));
		}
	}

	private String read() {
		String s = "";

		char c = QGCEmulator.readChar();

		while (c != '\0') {
			s = s.concat(String.valueOf(c));
			c = QGCEmulator.readChar();
		}
		return s;
	}

	/**
	 * Legacy Class which emulates behaviour of a real Queensgate Controller with up to three Piezo Devices.
	 */
	private class QueensgateControllerEmulator {
		private int state = IDLE;

		private final static int IDLE = 0;

		private final static int SETONE = 1;

		private final static int SETTWO = 2;

		private final static int SETTHREE = 3;

		private final static int GETONE = 4;

		private final static int GETTWO = 5;

		private final static int GETTHREE = 6;

		private final static int GETFOUR = 7;

		private final static int GETFIVE = 8;

		private String comingIn = null;

		private String CRLF = "\r\n";

		private char[] goingOut = new char[6];

		private int lastCharSent = -1;

		private int currentModule = 0;

		private int status;

		// Positions and requested offsets for 3 modules - A, B & C
		private int position[] = new int[3];

		private int requestedOffset[] = new int[3];

		/**
		 * Calculates actualOffset, position, and voltage from a requested offset.
		 */
		private void calculateValues() {
			int actualOffset = 0;
			double voltage = 0.0;

			// could introduce random (or indeed non-random) errors here
			actualOffset = requestedOffset[currentModule];

			// Position calculation should give 2047 for -8192 not 2048
			// and -2048 for +8191 not -2047 so the calculation has to
			// be different for positive and negative offsets
			if (requestedOffset[currentModule] >= 0) {
				position[currentModule] = -(requestedOffset[currentModule] / 4) - 1;
			} else {
				position[currentModule] = -(requestedOffset[currentModule] + 1) / 4;
			}

			// Could add a random front panel offset here
			voltage = position[currentModule] * 5.3 / 2048.0;

			// Sometimes return an error status
			if (Math.random() > 0.95) {
				status |= 1 << currentModule;
			} else {
				status &= ~1 << currentModule;
			}

			logger.debug("QueensgateControllerEmulator: actualOffset " + actualOffset + " position "
					+ position[currentModule] + " voltage " + voltage + " status " + status);
		}

		/**
		 * Prints an error message for unexpected input.
		 * 
		 * @param s
		 *            the unexpected input
		 */
		private void erroneousInput(String s) {
			logger.warn("DummyQueensgateController received unexpected input (non-critical): " + s);
			state = IDLE;
		}

		/**
		 * Decodes an offset request string to extract the requested offset.
		 * 
		 * @param s
		 *            the incoming string
		 * @return the offset
		 */
		private int getRequestedOffsetFromString(String s) {
			int i = 9999;

			// the string is Ixxx, xxx is the encoded offset request
			logger.debug("DummyQueensgateController: Encoded string is " + s.substring(1));

			try {
				i = Integer.parseInt(s.substring(1), 16);
			} catch (NumberFormatException nfe) {
				logger.debug("DummyQueensgateController: cannot convert " + s + " to valid offset request");
			}

			logger.debug("Raw hexadecimal offset is " + i);

			if ((i & 0x2000) == 0x2000) {
				i = -((~i) + 1 & 0x3fff);
			}

			logger.debug("Hexadecimal offset after complementing " + i);

			return i;
		}

		/**
		 * Handles strings coming in from program, this is a simple but tedious finite state machine which expects to
		 * get the same command strings as are sent to the real Queensgate and puts itself into suitable states as it
		 * goes.
		 * 
		 * @param s
		 *            the incoming string
		 */
		private synchronized void HandleNextString(String s) {
			// TODO - This state machine will need a big rewrite if it is to
			// handle more than just get/set commands

			// Only two commands are understood, setting offset for which
			// the states begin SET, and getting position and status for
			// which the states begin GET. Any input which does not fit
			// in with expectations is rejected.

			// Map module bit pattern values (1,2,4) to (0,1,2) index
			// (where modules map to 0 = A, 1 = B, 2 = C)
			int selectModule[] = { 0, 0, 1, 0, 2 };

			logger.debug("DummyQueensgateController: command " + s);

			switch (state) {
			case IDLE:
				if (s.charAt(0) == 'M') {
					int moduleNumber = s.charAt(1) - '0';

					if (moduleNumber <= 4) {
						currentModule = selectModule[moduleNumber];
						logger.debug("QueensgateControllerEmulator: selected module is " + currentModule);

						if (s.length() > 2) {
							if (s.charAt(2) == '1') {
								state = SETONE;
							}

							// If N = 4, must be doing a GET read from ADC
							if (s.charAt(2) == '4') {
								state = GETTWO;
							}
						}
					} else {
						erroneousInput(s);
					}
				}
				/*
				 * if (s.equals("M11")) { state = SETONE; }
				 */
				// TODO - doesnt handle N1 then Mx as start of SET sequence
				// instead of Mx1 combined command
				else if (s.equals("N4")) {
					state = GETONE;
				} else {
					erroneousInput(s);
				}
				break;

			case SETONE:
				if (s.startsWith("I")) {
					// get required offset and store
					requestedOffset[currentModule] = getRequestedOffsetFromString(s);
					logger.debug("QueensgateControllerEmulator: requestedOffset is now "
							+ requestedOffset[currentModule]);
					state = SETTWO;
				} else {
					erroneousInput(s);
				}
				break;

			case SETTWO:
				if (s.equals("N3")) {
					state = SETTHREE;
				} else {
					erroneousInput(s);
				}
				break;

			case SETTHREE:
				if (s.equals("N1")) {
					calculateValues();
					state = IDLE;
				} else {
					erroneousInput(s);
				}
				break;

			case GETONE:
				if (s.charAt(0) == 'M') {
					int moduleNumber = s.charAt(1) - '0';

					if (moduleNumber <= 4) {
						currentModule = selectModule[moduleNumber];
						logger.debug("QueensgateControllerEmulator: selected module is " + currentModule);
						state = GETTWO;
					} else {
						erroneousInput(s);
					}
				}
				/*
				 * if (s.equals("M1")) { state = GETTWO; }
				 */
				else {
					erroneousInput(s);
				}
				break;

			case GETTWO:
				if (s.equals("N0")) {
					state = GETTHREE;
				} else {
					erroneousInput(s);
				}
				break;

			case GETTHREE:
				if (s.equals("N4")) {
					state = GETFOUR;
				} else {
					erroneousInput(s);
				}
				break;

			case GETFOUR:
				if (s.equals("?")) {
					constructGoingOut();
					state = GETFIVE;
				} else {
					erroneousInput(s);
				}
				break;
			}

		}

		/**
		 * Constructs the goingOut char array from the position and status
		 */
		private synchronized void constructGoingOut() {
			String stringPosition;

			// must send back exactly what a real Queensgate would send
			// position as 12 bit number in most significant 12 bits and
			// then status in lowest four bits

			logger.debug("QueensgateControllerEmulator: current position is " + position[currentModule]);
			stringPosition = threeDigitTwosComplementString(position[currentModule]);
			logger.debug("                         this encodes as " + stringPosition);

			goingOut[0] = stringPosition.charAt(0);
			goingOut[1] = stringPosition.charAt(1);
			goingOut[2] = stringPosition.charAt(2);
			goingOut[3] = Character.forDigit(status, 10);

			goingOut[4] = '\r';
			goingOut[5] = '\n';

			logger.debug("DummyQueensgateController: goingOut is " + String.valueOf(goingOut).substring(0, 4));
		}

		/**
		 * Converts a 12 bit integer into a three digit string, only used here to imitate how the real Queensgate would
		 * send back its position.
		 * 
		 * @param i
		 *            the integer to convert
		 * @return the three digit string
		 */
		private String threeDigitTwosComplementString(int i) {
			int j;

			if (i < 0) {
				j = (~(Math.abs(i)) + 1) & 0xfff;
			} else {
				j = i;
			}

			String rtrn = Integer.toHexString(j);

			while (rtrn.length() < 3) {
				rtrn = "0" + rtrn;
			}

			return (rtrn);
		}

		// These two methods override the DummySerial equivalents in order
		// to provide behaviour which mimics an actual Queensgate
		// NB read and write are from the point of view of the caller i.e.
		// readChar reads FROM here, writeChar writes TO here.

		/**
		 * Reads a character from the device
		 * 
		 * @return the character read
		 */
		public char readChar() {
			char rtrn = '\0';

			// char array goingOut is returned one char at a time

			if (state == GETFIVE) {
				if (lastCharSent < 5) {
					lastCharSent++;
					rtrn = goingOut[lastCharSent];
				} else if (lastCharSent == 5) {
					lastCharSent = -1;
					state = IDLE;
				}
			} else {
				state = IDLE;
				logger.debug("DummyQueensgateController: readChar when not ready");
			}
			return (rtrn);
		}

		/**
		 * Writes a character to the device
		 * 
		 * @param c
		 *            The character to write
		 */
		public void writeChar(char c) {
			if (comingIn == null) {
				comingIn = String.valueOf(c);
			} else {
				comingIn = comingIn + String.valueOf(c);
			}

			if (comingIn.endsWith(CRLF)) {
				HandleNextString(comingIn.substring(0, comingIn.length() - 2));
				comingIn = null;
			}
		}

	}
}
