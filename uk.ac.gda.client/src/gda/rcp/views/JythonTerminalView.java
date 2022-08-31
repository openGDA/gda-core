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

package gda.rcp.views;

import static gda.rcp.preferences.GdaRootPreferencePage.SHOW_ALL_INPUT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import gda.jython.IJythonContext;
import gda.jython.IScanDataPointObserver;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.Terminal;
import gda.jython.TerminalInput;
import gda.rcp.GDAClientActivator;
import gda.rcp.util.ScanDataPointFormatterUtils;
import gda.rcp.views.dashboard.DashboardView;
import gda.rcp.views.dashboard.JythonSnapshotProvider;
import gda.rcp.views.dashboard.ScannableObject;
import gda.scan.IScanDataPoint;
import gda.scan.ScanDataPointFormatter;
import gda.util.PropertyUtils;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.ClientManager;
import uk.ac.gda.client.HelpHandler;

/**
 * Design to look and act like a command terminal for the GDA Jython Interpreter. NOTE: Currently this class does not
 * work unless it is a top most view when the user goes to a perspective containing it. This is because eclipse only
 * makes a view when it is visible. To fix this UIScanDataPointService could be used to tell this view of previous scan
 * data points missed by the view not being present. For now always use this view as a default visible one and this
 * issue will not be as bad.
 */
public class JythonTerminalView extends ViewPart implements IScanDataPointObserver, Terminal {

	// The output panel is similar to the details panel at the bottom of the
	// "Variables" view used when debugging. See these classes:
	//
	// org.eclipse.debug.internal.ui.views.variables.details.DefaultDetailPane
	// org.eclipse.debug.internal.ui.actions.variables.details.DetailPaneWordWrapAction

	/**
	 * ID of this view. Referenced by string to reduce dependencies in places, refactor will not work.
	 */
	public static final String ID = "gda.rcp.jythonterminalview";

	private static final Logger logger = LoggerFactory.getLogger(JythonTerminalView.class);
	private static final String NORMAL_PROMPT = ">>> ";
	private static final String ADDITONAL_INPUT_PROMPT = "... ";
	private static final String RAW_INPUT_PROMPT = "--> ";
	private static final int MAX_COMMANDS_TO_SAVE = 100;

	private static final String TERMINALNAME = "JythonTerminal";

	private static boolean scrollLock = false;
	private static boolean moveToTopOnUpdate = false;

	private volatile String txtInputText = "";
	private volatile String txtPromptText = "";
	private volatile int caretPosition = 0;

	private Text txtInput;
	/** {@link Document} containing output text */
	private final JythonTerminalDocument outputDoc = new JythonTerminalDocument();

	/** {@link TextViewer} that displays the output document */
	private TextViewer outputTextViewer;

	private String txtOutputLast; //copy of string sent to outputDoc.set()
	private Label txtPrompt;

	private final Vector<String> cmdHistory = new Vector<>();
	private int cmdHistoryIndex = 0;
	private String commandFileName;
	private JythonServerFacade jsf;
	private boolean runFromHistory = false;
	private String currentCmd;
	private boolean printOutput = false;

	private final AtomicBoolean outputBufferUpdated = new AtomicBoolean(false);
	private final Pattern newLinePattern = Pattern.compile("\\r+\\n");
	private final StringBuffer outputBuffer = new StringBuffer();

	private FileWriter outputFile;
	private Object lastScanDataPointUniqueName;
	private ScanDataPointFormatter scanDataPointFormatter;
	private AutoCompleter autoCompleter;
	private HelpHandler helpHandler;
	private Composite root;
	private TextViewerWordWrapToggleAction wordWrapAction;
	private IJythonContext mockJythonContext;

	private Future<?> displayUpdate;

	private Font font;

	/***/
	public JythonTerminalView() {
		try {
			this.scanDataPointFormatter = ScanDataPointFormatterUtils.getDefinedFormatter();
		} catch (Exception ne) {
			logger.error("Cannot read formatter extension point", ne);
		}
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		if (!ClientManager.isTestingMode()) {
			jsf = JythonServerFacade.getInstance();
			jsf.addOutputTerminal(this);
			jsf.addIScanDataPointObserver(this);
		}

		helpHandler = GDAClientActivator.getNamedService(HelpHandler.class, null);

		fetchOldHistory();

		// Refresh the terminal output at 20 Hz
		displayUpdate = Async.scheduleAtFixedRate(new SimpleOutputUpdater(), 0, 50, TimeUnit.MILLISECONDS);
	}

	@Override
	public void createPartControl(final Composite parent) {

		font = new Font(parent.getDisplay(), "DejaVu Sans Mono", 10, SWT.NORMAL);
		final int tabSize = 4;

		{
			root = new Composite(parent, SWT.NONE);
			root.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridLayout gl = new GridLayout();
			gl.horizontalSpacing = 0;
			gl.verticalSpacing = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			root.setLayout(gl);
			{
				outputTextViewer = new TextViewer(root, SWT.H_SCROLL | SWT.V_SCROLL);
				outputTextViewer.getTextWidget().setLayoutData(new GridData(GridData.FILL_BOTH));
				outputTextViewer.setEditable(false);
				outputTextViewer.getTextWidget().setFont(font);
				outputTextViewer.getTextWidget().setTabs(tabSize);
				outputTextViewer.getTextWidget().setBottomMargin(5);
				outputTextViewer.setDocument(outputDoc);
				outputTextViewer.addTextListener(new TextUpdateListener());
				// If the height of the view is not an exact multiple of the line height,
				// the final line can be partially obscured when the text is scrolled to
				// the bottom. To avoid this, adjust the bottom margin to ensure the view
				// displays an exact number of lines.
				outputTextViewer.getTextWidget().addControlListener(ControlListener.controlResizedAdapter(e -> {
						var widget = outputTextViewer.getTextWidget();
						var height = widget.getSize().y;
						var line = widget.getLineHeight();
						widget.setBottomMargin(height % line);
				}));
				txtOutputLast = "";

				createContextMenuForOutputBox();
				wordWrapAction.run(); // to set initial word wrap state

				// Print the output from startup (localstaion.py) to the terminal
				if (!ClientManager.isTestingMode()) {
					String startupOutput = jsf.getStartupOutput();
					if (startupOutput != null) {
						appendOutput(startupOutput);
					}
				}
			}

			Composite inputHolder = new Composite(root, SWT.None);
			inputHolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			inputHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(inputHolder);

			{
				txtPrompt = new Label(inputHolder, SWT.None);
				txtPrompt.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).indent(2, 0).applyTo(txtPrompt);
				txtPrompt.setText(NORMAL_PROMPT);
				txtPrompt.setFont(font);
				txtPrompt.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent e) {
						// If you click the prompt put the cursor in the input box
						txtInput.setFocus();
					}
				});
			}
			{
				if(isGTK3()) {
					// For RHEL7
					txtInput = new Text(inputHolder, SWT.BORDER);
				} else {
					// for REHL6
					txtInput = new Text(inputHolder, SWT.NONE);
				}
				txtInput.setFont(font);
				txtInput.setTabs(tabSize);
				txtInput.addListener(SWT.DefaultSelection, e -> textInputActionPerformed());
				txtInput.addKeyListener(new JythonTextHandler());
				txtInput.addTraverseListener(e -> {
						e.doit = false;
						if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
							int caret = txtInput.getCaretPosition();
							if (txtInput.getText(caret-1, caret-1).equals(String.valueOf(SWT.TAB))) {
								txtInput.setText(txtInput.getText().replaceFirst("\t", ""));
								txtInput.setSelection(caret-1);
							}
						}
				});

				txtInput.setLayoutData(new GridData(GridData.FILL_BOTH));
				autoCompleter = new AutoCompleter(txtInput);
			}
		}
		setHelpContextIDS();
	}

	private boolean isGTK3() {
		String gtkVerProp = System.getProperty("org.eclipse.swt.internal.gtk.version");
		return gtkVerProp.regionMatches(0, "3", 0, 1);
	}

	private void setHelpContextIDS() {
		IWorkbenchHelpSystem helpSystem = getSite().getWorkbenchWindow().getWorkbench().getHelpSystem();
		helpSystem.setHelp(root, "uk.ac.gda.client.jython_console");
	}

	private void createContextMenuForOutputBox() {
		// We only want one instance of this action, that will hold the current
		// setting
		wordWrapAction = new TextViewerWordWrapToggleAction(outputTextViewer);

		// The copy and select all actions will get recreated each time the
		// context menu is opened, so that they reflect whether or not text is
		// currently selected

		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this::fillContextMenuForOutputBox);

		Menu menu = menuMgr.createContextMenu(outputTextViewer.getTextWidget());
		outputTextViewer.getTextWidget().setMenu(menu);
	}

	private void fillContextMenuForOutputBox(IMenuManager menuMgr) {
		TextViewerAction copyAction = new TextViewerAction(outputTextViewer, ITextOperationTarget.COPY);
		copyAction.setText("&Copy");
		copyAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);

		TextViewerAction selectAllAction = new TextViewerAction(outputTextViewer, ITextOperationTarget.SELECT_ALL);
		selectAllAction.setText("Select &All");
		selectAllAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);

		menuMgr.add(copyAction);
		menuMgr.add(selectAllAction);
		menuMgr.add(new Separator());
		menuMgr.add(wordWrapAction);
		menuMgr.add(new OtherClientInputToggleAction());
	}

	public String getName() {
		return TERMINALNAME;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		// if from scans then objects are in the format String, ScanDataPoint
		if (changeCode instanceof IScanDataPoint && theObserved instanceof JythonServerFacade) {
			IScanDataPoint sdpt = (IScanDataPoint) changeCode;
			// print headerString if last printed was different
			String uniqueName = sdpt.getUniqueName();
			if (uniqueName != null) {
				if (lastScanDataPointUniqueName == null || !lastScanDataPointUniqueName.equals(uniqueName)) {
					appendOutput(sdpt.getHeaderString(scanDataPointFormatter) + "\n");
					lastScanDataPointUniqueName = uniqueName;
				}
			}
			// always print the point to the terminal
			appendOutput(sdpt.toFormattedString(scanDataPointFormatter) + "\n");
		} else if (changeCode instanceof String) {
			String message = (String) changeCode;

			if (message.compareTo(Jython.RAW_INPUT_REQUESTED) == 0) {
				// change prompt and next input will go through a different
				// method call
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
					txtInput.setEnabled(true);
					txtPrompt.setText(RAW_INPUT_PROMPT);
					txtInput.setFocus();
					// clear the command-line
					txtInput.setText("");
				});
			} else if (message.compareTo(Jython.RAW_INPUT_RECEIVED) == 0) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
					// change prompt back to usual
					txtPrompt.setText(NORMAL_PROMPT);
				});
			}
		}
	}

	@Override
	public void writeInput(TerminalInput input) {
		if (GDAClientActivator.getDefault().getPreferenceStore().getBoolean(SHOW_ALL_INPUT)) {
			write(input.format(NORMAL_PROMPT, ADDITONAL_INPUT_PROMPT) + "\n");
		}
	}

	@Override
	public void write(String output) {
		appendOutput(output);
	}

	@Override
	public void setFocus() {
		if (txtInput != null) {
			txtInput.setFocus();
		}
	}

	/**
	 * Run the command currently in the {@link #txtInput}. If this is a continuation of a previous
	 * multiline command, it will be appended to the current command before being run.
	 */
	private void runCommand() {
		if (jsf == null) {
			jsf = JythonServerFacade.getInstance();
			jsf.addIObserver(this);
		}

		// print out what was typed

		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			txtPromptText = txtPrompt.getText();
			txtInputText = txtInput.getText();
		});

		appendOutput(String.format("%s%s\n", txtPromptText, txtInputText));
		try {
			// if this is the start of a new command
			if (txtPromptText.compareTo(NORMAL_PROMPT) == 0) {
				String typedCmd = txtInputText;
				// add the command to cmdHistory
				if (cmdHistory.isEmpty()) {
					addCommandToHistory(typedCmd);
				} else if ((typedCmd.compareTo("") != 0)
						&& (typedCmd.compareTo(cmdHistory.get(cmdHistory.size() - 1)) != 0)) {
					addCommandToHistory(typedCmd);
				}
				if (cmdHistoryIndex != cmdHistory.size() - 2) {
					runFromHistory = true;
				}
				// run the command
				boolean needMore = jsf.runsource(typedCmd);
				// if not a complete Jython command
				if (needMore) {
					// save the command so far
					currentCmd = typedCmd;
					PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
						// change the prompt
						txtPrompt.setText(ADDITONAL_INPUT_PROMPT);
						// clear the command-line
						txtInput.setText("");
					});
				} else {
					currentCmd = "";
					// reset the cmdHistory pointer if we just added a new line
					cmdHistoryIndex = cmdHistory.size();
					runFromHistory = false;

					PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
						// clear the command-line if it has not been changed
						if (typedCmd.equals(txtInput.getText())) {
							txtInput.setText("");
						}
						txtPrompt.setText(NORMAL_PROMPT);
					});
				}
			}
			// if we are part way through a multi-line command
			else if (txtPromptText.compareTo(ADDITONAL_INPUT_PROMPT) == 0) {
				// add to history if something was entered
				if (txtInputText.compareTo("") != 0) {
					if (cmdHistory.isEmpty() || ((txtInputText.compareTo("") != 0)
							&& (txtInputText.compareTo(cmdHistory.get(cmdHistory.size() - 1)) != 0))) {
						addCommandToHistory(txtInputText);
					}
					if (cmdHistoryIndex != cmdHistory.size() - 2) {
						runFromHistory = true;
					}
				}
				// append to whole command
				currentCmd += "\n" + txtInputText;
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> txtInput.setEnabled(false));
				// run the command
				boolean needMore = jsf.runsource(currentCmd);

				// if not a complete Jython command
				if (needMore) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {// change the prompt
						txtPrompt.setText(ADDITONAL_INPUT_PROMPT);
						// clear the command-line
						txtInput.setText("");
					});
				} else {
					currentCmd = "";
					PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {// change the prompt
						txtPrompt.setText(NORMAL_PROMPT);
						// clear the command-line
						txtInput.setText("");
					});
					// reset the cmdHistory pointer
					cmdHistoryIndex = cmdHistory.size();
					runFromHistory = false;
				}
			}
			// else a script has asked for input
			else if (txtPromptText.compareTo(RAW_INPUT_PROMPT) == 0) {
				// get the next input from the user
				jsf.setRawInput(txtInputText);
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> txtInput.setText(""));
			}
		} catch (Exception e) {
			logger.debug("caught ", e);
			throw e;
		} finally {
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				txtInput.setEnabled(true);
				txtInput.setFocus();
			});
		}
	}

	private void fetchOldHistory() {
		try {
			commandFileName = getCommandFilename();
			File commandFile = new File(commandFileName);

			// if the file exists, read its contents
			if (commandFile.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(commandFile));
				String str;
				while ((str = in.readLine()) != null) {
					if (!str.isEmpty()) {
						cmdHistory.add(str);
					}
				}
				in.close();

				// if we have read in more than 500 commands, then
				// reduce file
				// and array size down to 500. This is done here as
				// during
				// running
				// we want to write to file every command as quickly as
				// possible
				int numberToRemove = cmdHistory.size() - MAX_COMMANDS_TO_SAVE;
				if (numberToRemove > 0) {
					for (int i = 0; i < numberToRemove; i++) {
						cmdHistory.removeElementAt(0);
					}

					// then rebuild file
					BufferedWriter out = new BufferedWriter(new FileWriter(commandFile));

					for (int i = 0; i < cmdHistory.size(); i++) {
						out.write(cmdHistory.get(i) + "\n");
					}
					out.close();
				}
				this.cmdHistoryIndex = cmdHistory.size();
			}

			// else make a new file
			else {
				commandFile.createNewFile();
			}
		} catch (Exception e) {
			logger.warn("Error reading Jython terminal history from file '{}' during configure", commandFileName, e);
			commandFileName = null;
		}

	}

	private void textInputActionPerformed() {
		// first intercept to see if there's any command which this panel is
		// interested in rather than passing to the interpreter.
		String inputText = txtInput.getText();
		String[] parts = inputText.split(" ");
		if (parts.length < 1) {
			return;
		}
		// if its a watch
		if (parts[0].toLowerCase().compareTo("watch") == 0) {
			logger.debug("'watch' used in JythonTerminalView, input: {}", inputText);
			// print out what was typed
			appendOutput(this.txtPrompt.getText() + inputText + "\n");

			try {
				DashboardView dashboard = (DashboardView)PlatformUI.
					getWorkbench().getActiveWorkbenchWindow().getActivePage().
					showView(DashboardView.ID);
				if (parts.length > 1) {
					for (int i = 1; i < parts.length; ++i) {
						dashboard.addServerObject(new ScannableObject(parts[i], new JythonSnapshotProvider()));
					}
				}
				addCommandToHistory(inputText);
			} catch (PartInitException e) {
				logger.error("Failed to get DashboardView", e);
			}
			txtInput.setText("");
			txtInput.setFocus();
		}
		else if ((helpHandler != null) && (parts[0].toLowerCase().compareTo("help") == 0)) {
			logger.debug("helpHandler used in Jython terminal view, input: {}", inputText);
			boolean handled = false;
			StringBuffer buf = new StringBuffer();
			try {
				handled = helpHandler.handle(inputText, buf);
			} catch (Exception e) {
				logger.error("Error handling " + inputText, e);
			}
			if( handled){
				appendOutput(this.txtPrompt.getText() + inputText + "\n");
				if( buf.length()>0){
					appendOutput(buf.toString() + "\n");
				}
				addCommandToHistory(inputText);
				txtInput.setText("");
			} else {
				Async.execute(this::runCommand);
			}
		}
		// if its the history command
		else if (parts[0].toLowerCase().compareTo("history") == 0) {
			logger.debug("'history' used in Jython terminal view, input: {}", inputText);
			// print out what was typed
			appendOutput(this.txtPrompt.getText() + parts[0] + "\n");

			// print out the last 100 commands
			int i = cmdHistory.size() > 100 ? cmdHistory.size() - 100 : 0;

			for (; i < cmdHistory.size(); i++) {
				appendOutput(i + "\t" + cmdHistory.get(i) + "\n");
			}
			txtInput.setText("");

			addCommandToHistory("history");
		}
		// repeat old commands
		else if (parts[0].startsWith("!")) {
			logger.debug("repeat used in Jython terminal view, input: {}", inputText);
			String stringToMatch = inputText.substring(1);

			// if stringToMatch is a number, then use that command
			if (stringIsAnInteger(stringToMatch)) {
				txtInput.setText(cmdHistory.get(Integer.parseInt(stringToMatch)));
				Async.execute(this::runCommand);
				return;
			}
			// else search backwards through the history to find a match
			int i = cmdHistory.size() - 1;
			boolean foundOne = false;
			for (; i >= 0; i--) {
				String oldCommand = cmdHistory.get(i);

				if (oldCommand.length() >= stringToMatch.length()) {
					String oldCmd = cmdHistory.get(i).substring(0, stringToMatch.length());
					if (oldCmd.compareTo(stringToMatch) == 0) {
						txtInput.setText(cmdHistory.get(i));
						i = 0;
						foundOne = true;
					}
				}
			}
			if (foundOne) {
				Async.execute(this::runCommand);
			} else {
				appendOutput("" + "\n");
				txtInput.setText("");
			}

		}
		// record output to a text file
		else if (parts[0].toLowerCase().compareTo("record") == 0) {
			logger.debug("record used in Jython terminal view, input: {}", inputText);
			// print out what was typed
			appendOutput(this.txtPrompt.getText() + inputText + "\n");

			addCommandToHistory(inputText);

			if (parts.length == 2 && parts[1].toLowerCase().compareTo("on") == 0) {
				startNewOutputFile();
				txtInput.setText("");
			}
			else if (parts.length == 2 && parts[1].toLowerCase().compareTo("off") == 0) {
				closeOutputFile();
				txtInput.setText("");
			}
			else {
				appendOutput("Expected 'record on' or 'record off'\n");
			}
		}
		// everything else, pass to the Command Server in a separate thread to
		// stop the GUI freezing.
		else {
			Async.execute(this::runCommand);
		}
	}

	private void closeOutputFile() {
		if (outputFile == null) {
			logger.warn("Not stopping recording: not started");
			return;
		}
		try {
			logger.info("Stopped recording terminal output");
			printOutput = false;
			outputFile.close();
			outputFile = null;
		} catch (IOException e) {
			logger.error("Error closing output file", e);
			outputFile = null;
		}
	}

	public void startNewOutputFile() {
		String filename = determineOutputFileName();
		// open the file
		try {
			outputFile = new FileWriter(new File(filename));
			printOutput = true;
			logger.info("Recording terminal output to: " + filename);
		} catch (IOException e) {
			printOutput = false;
			logger.warn("JythonTerminal could not create the output file: {}", filename, e);
		}
	}

	private String determineOutputFileName() {
		String terminalOutputDirName = getTerminalOutputDirName();
		terminalOutputDirName = terminalOutputDirName.replaceAll("/$", ""); // strip leading / to avoid dir//file

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String timestamp = sdf.format(new Date());

		String filename = String.format("%s/terminal-output-%s.txt", terminalOutputDirName, timestamp);
		return filename;
	}

	String getTerminalOutputDirName() {
		// get the terminal output directory from java properties. If no property defined, use the user script directory
		String terminalOutputDirName;
		if (LocalProperties.get("gda.jython.terminalOutputDir") != null) {
			terminalOutputDirName = PropertyUtils.getExistingDirFromLocalProperties("gda.jython.terminalOutputDir");
		} else { // get the user script directory from jython context
			terminalOutputDirName = getJythonContext().getDefaultScriptProjectFolder();
		}

		if (!(terminalOutputDirName.endsWith("\\") || terminalOutputDirName.endsWith("/"))) {
			terminalOutputDirName += System.getProperty("file.separator");
		}
		return terminalOutputDirName;
	}

	private void addCommandToHistory(String newCommand) {
		// add command to the history
		cmdHistory.add(newCommand);

		if (getCommandFilename() != null) {
			// also save command to a file
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(getCommandFilename(), true));
				out.write(newCommand + "\n");
				out.close();
			} catch (IOException e) {
				logger.error("Could not save to history file: {}", getCommandFilename(), e);
			}
		}
	}

	String getCommandFilename() {
		if (commandFileName == null) {
			commandFileName = LocalProperties.get("gda.jythonTerminal.commandHistory.path", getJythonContext()
					.getDefaultScriptProjectFolder());
			if (!(commandFileName.endsWith("\\") || commandFileName.endsWith("/"))) {
				commandFileName += System.getProperty("file.separator");
			}
			commandFileName += ".cmdHistory.txt";
		}
		return commandFileName;
	}

	private boolean stringIsAnInteger(String stringToCheck) {
		// a bit of a hack, but works!
		try {
			Integer.parseInt(stringToCheck);
			return true; // Did not throw, must be a number
		} catch (NumberFormatException err) {
			return false; // Threw, So is not a number
		}
	}

	private synchronized void appendOutput(String text) {
		// if output being saved to a file (record command)
		if (printOutput && outputFile != null) {
			try {
				outputFile.append(text);
				outputFile.flush();
			} catch (IOException e) {
				closeOutputFile();
				logger.warn("Error writing terminal output to file", e);
			}
		}
		recalculateBuffer(text);
		outputBufferUpdated.set(true);
	}

	private void recalculateBuffer(String text) {
		newLinePattern.matcher(text).replaceAll("\n");

		// if a repeat of the command prompt
		if (text.startsWith(NORMAL_PROMPT)) {
			// make sure that the print out starts on a new line
			if (outputBuffer.length() > 0 && outputBuffer.charAt(outputBuffer.length() - 1) != '\n') {
				outputBuffer.append("\r\n");
				// update where new print out should start
				caretPosition = outputBuffer.length();
			}
			// print
			addToOutputBuffer(text);
			// update where new print out should start
			caretPosition = outputBuffer.length();
		}
		// if just regular output simply append
		else if (!text.contains("\r") && !text.startsWith(RAW_INPUT_PROMPT)) {
			// If text field has grown too long, trim off 10% from the beginning. Note: This is only performed for
			// this "regular output" case because changing the text field length for the other cases messes up the
			// caretPosition value.
			int currentLength = outputBuffer.length();
			if (currentLength > LocalProperties.getInt("gda.jython.jythonTerminal.textFieldCharactersCap", 100000)) {
				outputBuffer.delete(0, currentLength / 10);
			}
			// print
			addToOutputBuffer(text);
			// update where new print out should start
			caretPosition = outputBuffer.length();
			// if output starts with '-->' when user requested input mid-script
		} else if (text.startsWith(RAW_INPUT_PROMPT)) {
			// add this output to the end of the previous line
			caretPosition = outputBuffer.length();
			// print
			addToOutputBuffer(text);
			// update where new print out should start
			caretPosition = outputBuffer.length();
		}
		// Otherwise must contain a \r.
		// This should be handled properly so the caret is returned to the start of the last line rather than \r
		// being treated as a new line marker.
		else {
			try {
				// find out where the \r is
				int locOfCR = text.indexOf('\r');

				// remove any final \n
				if (text.endsWith("\n")) {
					text = text.substring(0, text.length() - 1);
				}

				// if \r at start of string, move caret to start of previous
				// line, unless that line started with '>>>'
				if (locOfCR == 0) {
					int locofLastEndofLine = outputBuffer.lastIndexOf("\n");
					caretPosition = locofLastEndofLine + 1;
				}
				// else add first part of text and then move the caret of
				// that line
				else {
					String substring = text.substring(0, locOfCR);
					addToOutputBuffer(substring);
					int locofLastEndofLine = outputBuffer.lastIndexOf("\n");
					caretPosition = locofLastEndofLine + 1;
				}

				// if anything after the /r in the text, append that
				if (text.length() > locOfCR + 1) {
					String stringToAppend = text.substring(locOfCR + 1);
					// print
					addToOutputBuffer(stringToAppend);
					// update where new print out should start
					caretPosition += stringToAppend.length();
				}

			}
			// any error, simply output everything and treat \r as a \n
			catch (Exception e) {
				logger.error("Error updating the console output", e);
				addToOutputBuffer(text);
				caretPosition = outputBuffer.length();
			}
		}
	}

	/**
	 * Overwrites the text in the JTextArea with the supplied string starting at the location defined by caretPosition.
	 *
	 * @param theString
	 */
	private void addToOutputBuffer(String theString) {

		// find location of end of last line
		int finalCharacter = outputBuffer.length();

		// if caret at the end of the last line
		if (caretPosition >= finalCharacter) {
			outputBuffer.append(theString);
		}
		// else if the output would only overwrite existing text
		else if (theString.length() + caretPosition < finalCharacter) {
			outputBuffer.replace(caretPosition, caretPosition + theString.length(), theString);
		}
		// else a mixture of overwriting and appending
		else {
			int firstPartLength = finalCharacter - caretPosition;
			String firstPart = theString.substring(0, firstPartLength);
			outputBuffer.replace(caretPosition, finalCharacter, firstPart);
			if (firstPartLength < theString.length()) {
				String lastPart = theString.substring(firstPartLength);
				outputBuffer.append(lastPart);
			}
		}
	}

	private void moveCaretToEnd() {
		txtInput.setSelection(txtInput.getCharCount(), txtInput.getCharCount());
	}


	private IJythonContext getJythonContext() {
		return (this.mockJythonContext != null) ? this.mockJythonContext : jsf;
	}

	void setJythonContextForTesting(IJythonContext mockJythonContext) {
		logger.warn("Overriding JythonServerFacade with test object");
		this.mockJythonContext = mockJythonContext;
	}

	public static boolean getScrollLock() {
		return scrollLock;
	}

	public static void setScrollLock(boolean scrollLock) {
		JythonTerminalView.scrollLock = scrollLock;
	}

	public static boolean getMoveToTopOnUpdate() {
		return moveToTopOnUpdate;
	}

	public static void setMoveToTopOnUpdate(boolean moveToTopOnUpdate) {
		JythonTerminalView.moveToTopOnUpdate = moveToTopOnUpdate;
	}

	public void clearConsole() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> outputDoc.set(""));
	}

	@Override
	public void dispose() {
		if(autoCompleter!=null){
			autoCompleter.dispose();
		}
		if (displayUpdate != null) {
			displayUpdate.cancel(true);
		}
		if (!ClientManager.isTestingMode()) {
			jsf.deleteOutputTerminal(this);
			jsf.deleteIScanDataPointObserver(this);
		}
		if (font != null) {
			font.dispose();
		}

		super.dispose();
	}

	private class JythonTextHandler extends KeyAdapter {
		private final Pattern WORD_SPLIT = Pattern.compile("(?=[A-Z\\(\\)\\[\\]_])|(?<=\\s|\\.)(?!\\.|\\s)");
		private String prefix = "";
		private String suffix = "";
		private String first = "";
		private String last = "";
		private int caretPosition = 0;

		private void updateStrings() {
			String current = txtInput.getText();
			caretPosition = txtInput.getCaretPosition();
			prefix = current.substring(0, caretPosition);
			suffix = current.substring(caretPosition);
			first = WORD_SPLIT.split(suffix,0)[0];
			String[] prefSplits = WORD_SPLIT.split(prefix, 0);
			last = prefSplits[prefSplits.length - 1];
			prefix = prefix.substring(0, prefix.length() - last.length());
			suffix = suffix.substring(first.length());
		}

		private int getSelectionAnchor() {
			Point selection = txtInput.getSelection();
			return selection.x == caretPosition ? selection.y : selection.x;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// when up or down arrows pressed scroll through vector of commands
			if (e.keyCode == SWT.ARROW_DOWN) {
				runFromHistory = false;
				if (cmdHistoryIndex < cmdHistory.size() - 1) {
					cmdHistoryIndex++;
					txtInput.setText(cmdHistory.get(cmdHistoryIndex));
					moveCaretToEnd();
				}
				// if at end of array then don't move index pointer but add a blank string
				else if (cmdHistoryIndex == cmdHistory.size() - 1) {
					cmdHistoryIndex++;
					txtInput.setText("");
				}
			} else if (e.keyCode == SWT.ARROW_UP) {
				if (runFromHistory) {
					runFromHistory = false;
				} else if (cmdHistoryIndex > 0) {
					cmdHistoryIndex--;
				}
				if (cmdHistory.size() != 0) {
					txtInput.setText(cmdHistory.get(cmdHistoryIndex));
					moveCaretToEnd();
				}
			} else if (e.stateMask == SWT.CTRL) {
				updateStrings();
				switch (e.keyCode) {
				case 'u':
					// Ctrl-u clears the text box
					txtInput.setText("");
					break;
				case 'd':
				case 'z':
					// Ideally Ctrl-c would cancel the current command but this is mapped to copy. Ctrl-z (sigstop) and Ctrl-d (EOF)
					// are nearly relevant enough
					txtInput.setText("");
					currentCmd = "";
					txtPrompt.setText(NORMAL_PROMPT);
					appendOutput("KeyboardInterrupt");
					appendOutput(NORMAL_PROMPT);
					break;
				case SWT.BS:
					e.doit = false;
					txtInput.setText(prefix + first + suffix);
					txtInput.setSelection(caretPosition - last.length());
					break;
				case SWT.DEL:
					e.doit = false;
					txtInput.setText(prefix + last + suffix);
					txtInput.setSelection(caretPosition);
					break;
				case SWT.ARROW_RIGHT:
					e.doit = false;
					txtInput.setSelection(caretPosition + first.length());
					break;
				case SWT.ARROW_LEFT:
					e.doit = false;
					txtInput.setSelection(caretPosition - last.length());
					break;
				}
			} else if (e.stateMask == (SWT.CTRL | SWT.SHIFT)) {
				updateStrings();
				int anchor;
				switch (e.keyCode) {
				case SWT.ARROW_RIGHT:
					e.doit = false;
					anchor = getSelectionAnchor();
					txtInput.setSelection(anchor, caretPosition + first.length());
					break;
				case SWT.ARROW_LEFT:
					e.doit = false;
					anchor = getSelectionAnchor();
					txtInput.setSelection(anchor, caretPosition - last.length());
					break;
				}
			}
		}
	}
	/**
	 * This is used to update the UI from the outputBuffer
	 */
	private class SimpleOutputUpdater implements Runnable {

		@Override
		public void run() {
			// If the buffer is not updated don't need to get in the UI thread so just return
			// If the flag is true do the update and reset the flag to false.
			if (!outputBufferUpdated.getAndSet(false)) {
				return;
			}

			// On Windows, newlines are \r\n terminated, when you call setText() or append()
			// \n is replaced with \r\n, so this sequence unexpectedly may return false:
			// txtOutput.setText(newOutput);
			// txtOutput.getText().equals(newOutput);
			// The effect of this is we need to keep a local copy of the last
			// string to be set, and we need to calculate the selection index on
			// what the text actually is to know what the selection index should be

			String newOutput = outputBuffer.toString().trim();
			// decide whether to call outputDoc.append or set
			if (newOutput.startsWith(txtOutputLast)) {
				String append = newOutput.substring(txtOutputLast.length());
				// Get in the UI thread and update the terminal output
				PlatformUI.getWorkbench().getDisplay().asyncExec(() ->
					outputDoc.append(append)
				);
			} else {
				// Get in the UI thread and update the terminal output
				PlatformUI.getWorkbench().getDisplay().asyncExec(() ->
					outputDoc.set(newOutput)
				);
			}
			txtOutputLast = newOutput;
		}
	}

	/**
	 * Extends the standard JFace {@link Document} class to add an {@code append}
	 * method.
	 */
	private class JythonTerminalDocument extends Document {

		/**
		 * Appends the given text to the end of this document.
		 */
		private void append(String text) {
			try {
				replace(getLength(), 0, text);
			} catch (BadLocationException e) {
				logger.error("Couldn't append text", e);
			}
		}
	}

	/**
	 * Listener to implement auto-scroll and bring-to-front settings
	 */
	private class TextUpdateListener implements ITextListener {
		@Override
		public void textChanged(TextEvent event) {
			if (!JythonTerminalView.getScrollLock()) {
				outputTextViewer.setTopIndex(outputTextViewer.getTextWidget().getLineCount());
			}
			if (JythonTerminalView.getMoveToTopOnUpdate()) {
				bringToFront();
			}
		}

		/**
		 * Bring view to front
		 * @see JythonTerminalView#setMoveToTopOnUpdate(boolean)
		 */
		private void bringToFront() {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			if (getSite().getPage().equals(page)) {
				page.bringToTop(JythonTerminalView.this);
			}
		}
	}
}
