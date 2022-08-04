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

package gda.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSCommandRunner implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(OSCommandRunner.class);

	public final Integer exitValue;

	public final Exception exception;

	public final Boolean succeeded;

	public final List<String> commands;

	public final boolean keepOutput;

	private final List<String> outputLines;

	public final List<String> getOutputLines() {
		return outputLines;
	}

	public enum LOGOPTION {
		NEVER,
		ALWAYS,
		ONLY_ON_ERROR
	}

	public static void runNoWait(String command, LOGOPTION logOption, String stdInFileName) {
		runNoWait(command.split("[\\s]"), logOption, stdInFileName);
	}

	public static void runNoWait(String[] _commands, LOGOPTION logOption, String stdInFileName) {
		runNoWait(Arrays.asList(_commands), logOption, stdInFileName);
	}

	public static void runNoWait(final List<String> _commands, final LOGOPTION logOption, final String stdInFileName){
		runNoWait(_commands, logOption, stdInFileName, null);
	}

	public static void runNoWait(final List<String> _commands, final LOGOPTION logOption, final String stdInFileName, ExecutorService executor) {
		_runNoWait(_commands, logOption, stdInFileName, null, null, executor);
	}

	public static void runNoWait(final List<String> _commands, final LOGOPTION logOption, final String stdInFileName,
			final Map<? extends String, ? extends String> envPutAll, final List<String> envRemove) {
		_runNoWait(_commands, logOption, stdInFileName, envPutAll, envRemove, null);
	}

	/**
	 * Starts process, returns immediately.
	 *
	 * @param _commands - program path and arguments
	 * @param logOption - controls the logging of the output
	 * @param stdInFileName - if not null stdin is set to this file for the program
	 */
	private static void _runNoWait(final List<String> _commands, final LOGOPTION logOption, final String stdInFileName,
			final Map<? extends String, ? extends String> envPutAll,
			final List<String> envRemove, ExecutorService executor) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				OSCommandRunner osCommandRunner = new OSCommandRunner(_commands, logOption != LOGOPTION.NEVER,
						stdInFileName, null, envPutAll, envRemove);
				if (osCommandRunner.exception != null) {
					String msg = "Exception seen trying to run command " + osCommandRunner.getCommandAsString();
					logger.error(msg);
					logger.error(osCommandRunner.exception.toString());
				} else if (osCommandRunner.exitValue != 0) {
					String msg = "Exit code = " + Integer.toString(osCommandRunner.exitValue)
							+ " returned from command " + osCommandRunner.getCommandAsString();
					logger.warn(msg);
					if (logOption != LOGOPTION.NEVER) {
						osCommandRunner.logOutput();
					}
				} else {
					if (logOption == LOGOPTION.ALWAYS) {
						osCommandRunner.logOutput();
					}
				}
			}
		};
		if (executor != null) {
			executor.submit(r);
		} else {
			new Thread(r, OSCommandRunner.class.getSimpleName()).start();
		}
	}

	public OSCommandRunner(String command, boolean _keepOutput, String stdInFileName, String stdOutFileName) {
		this(command.split("[\\s]"), _keepOutput, stdInFileName, stdOutFileName);
	}

	public OSCommandRunner(String[] _commands, boolean _keepOutput, String stdInFileName, String stdOutFileName) {
		this(Arrays.asList(_commands), _keepOutput, stdInFileName, stdOutFileName);
	}

	public OSCommandRunner(List<String> _commands, boolean _keepOutput, Object stdInFile, String stdOutFileName) {
		this(_commands, _keepOutput, stdInFile, stdOutFileName, null, null);
	}

	public OSCommandRunner(List<String> _commands, boolean _keepOutput, Object stdInFile, String stdOutFileName, Map<? extends String, ? extends String> envPutAll,
			List<String> envRemove) {
		this(_commands, _keepOutput, stdInFile, stdOutFileName, envPutAll, envRemove, null);
	}

	public OSCommandRunner(List<String> _commands, boolean _keepOutput, Object stdInFile, String stdOutFileName, Map<? extends String, ? extends String> envPutAll,
			List<String> envRemove, String directory) {
		this(_commands, _keepOutput, stdInFile, stdOutFileName, envPutAll, envRemove, directory, -1);
	}

	/**
	 * Starts process, waits for completion.
	 *
	 * @param _commands - this is the program and list of arguments
	 * @param _keepOutput - true if output is to be later accessed in outputLines
	 * @param stdInFile - if not null stdin is set to this file. Can be a string for the file path or a list of strings for the lines of the file.
	 * @param stdOutFileName - if not null stdout is set to this file.
	 */
	public OSCommandRunner(List<String> _commands, boolean _keepOutput, Object stdInFile, String stdOutFileName, Map<? extends String, ? extends String> envPutAll,
			List<String> envRemove, String directory, int timeoutInMs) {
		this.commands = _commands;
		this.keepOutput = _keepOutput;
		Integer _exitValue = 0;
		Exception _exception = null;
		Boolean _succeeded = false;
		Vector<String> _outputLines = null;
		try {
			ProcessBuilder pb = new ProcessBuilder();
			if( envRemove != null){
				for(String key : envRemove)
					pb.environment().remove(key);
			}
			if( envPutAll != null)
				pb.environment().putAll(envPutAll);
			pb.redirectErrorStream(true);
			pb.command(commands);
			if( directory != null && !directory.isEmpty())
				pb.directory(new File(directory));

			final Process p = pb.start();

			if (timeoutInMs != -1) {
				final ProcessKiller killer = new ProcessKiller(p, timeoutInMs);
				killer.start();
			}

			try {
				if (stdInFile != null) {
					// Copy the file to the process input
					final OutputStream ostream = p.getOutputStream();
					final InputStream istream = stdInFile instanceof String
					                          ? new FileInputStream((String)stdInFile)
					                          : null;
					try {
						final byte[] buffer = new byte[4096];
						if (istream!=null) {
							for (int count = 0; (count = istream.read(buffer)) >= 0;) {
								ostream.write(buffer, 0, count);
							}
						} else {
							final String [] lines = (String[])stdInFile;
							for (int i = 0; i < lines.length; i++) {
								ostream.write(lines[i].getBytes("UTF-8"));
								ostream.write('\n');
							}
						}
					} catch (IOException ex) {
						// TODO This is actually an error - do not do nothing here!
						// do nothing
					}
					ostream.close();
					if (istream!=null) istream.close();
				}
				if(!keepOutput && stdOutFileName == null)
					pipe(p.getInputStream(), System.out);
				_exitValue = p.waitFor();
				if (stdOutFileName != null) {
					// Copy the file to the process input
					InputStream istream = p.getInputStream();
					OutputStream ostream = new FileOutputStream(stdOutFileName);
					byte[] buffer = new byte[4096];
					for (int count = 0; (count = istream.read(buffer)) >= 0;) {
						ostream.write(buffer, 0, count);
					}
					ostream.close();
				}
				if (keepOutput) {
					BufferedReader output = null;
					InputStream istream = null;
					try{
						if (stdOutFileName != null) {
							istream = new FileInputStream(stdOutFileName);
						} else {
							istream = p.getInputStream();
						}
						output = new BufferedReader(new InputStreamReader(istream));
						String line;
						_outputLines = new Vector<String>();
						while ((line = output.readLine()) != null) {
							_outputLines.add(line);
						}
					}finally{
						if (stdOutFileName != null && istream != null) {
							istream.close();
						}

					}
				}
				_succeeded = _exitValue == 0;
			} catch (Exception ex) {
				throw ex;
			}

			closeStream(p.getInputStream(), "input");
			closeStream(p.getOutputStream(), "output");
			closeStream(p.getErrorStream(), "error");
			p.destroy();

		} catch (Exception ex) {
			_exception = ex;
		} finally {
			// TODO The exception has to be explicitly asked for currently.
			// Instead it should be thrown if this is an API to processes.
			exception = _exception;
			exitValue = _exitValue;
			succeeded = _succeeded;
			if (_outputLines != null) {
				outputLines = _outputLines;
			} else {
				outputLines = null;
			}
		}
	}

	private static void closeStream(Closeable stream, String name) {
		try {
			stream.close();
		} catch (IOException ioe) {
			logger.warn(String.format("Unable to close process %s stream", name), ioe);
		}
	}

	private static void pipe(final InputStream src, final PrintStream dest) {
	    new Thread(new Runnable() {
	        @Override
	        public void run() {
	            try {
	                byte[] buffer = new byte[1024];
	                for (int n = 0; n != -1; n = src.read(buffer)) {
	                    dest.write(buffer, 0, n);
	                }
	            } catch (IOException e) { // just exit
	            }
	        }
	    }).start();
	}

	public String getCommandAsString() {
		String msg = "";
		for (String s : commands) {
			msg += " " + s;
		}
		return msg;
	}

	/**
	 * Print the output to the logger.
	 */
	public void logOutput() {
		logger.info("Output from command " + getCommandAsString());
		if (outputLines != null) {
			for (String s : outputLines) {
				logger.info(s);
			}
		}
	}
}
