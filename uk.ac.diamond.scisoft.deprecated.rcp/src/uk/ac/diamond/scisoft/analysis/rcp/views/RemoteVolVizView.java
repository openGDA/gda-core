/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.views;


import gda.observable.IObserver;

import java.io.FileWriter;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.volimage.CommandClient;
import uk.ac.diamond.scisoft.analysis.rcp.volimage.ImageStreamReader;

/**
 *
 */
public class RemoteVolVizView extends ViewPart implements PaintListener, MouseListener, MouseMoveListener, MouseWheelListener, SelectionListener, KeyListener, IObserver {

	static private final String SCRIPTSTRUNIX = "#!/bin/bash\n" +
												"source /etc/profile.d/modules.sh # load up module settings for DLS system\n" +
       										    "module load gigacubeR3d\n" +
       									 	    "exec gigacubeR3d 9999 --offscreen\n";	 
	static private final String UNIXSCRIPTNAME = "runvolume.sh";
	
	private Canvas canvas;
	private Image testImage;
	private ImageData imgData;
	private GC testImageGC;
	private Thread imageStreamThread = null;
	ImageStreamReader streamReader = null;
	private CommandClient commandClient = null;
	private float oldMouseX;
	private float oldMouseY;
	private boolean mouseDown = false;
	private int whichMouseButton = 0;
	private TransferFunctionView transferFunctionView;
	private Action setDVRMode;
	private Action setISOMode;
	private Action setMIPMode;
	private Action setLightMode;
	private Action fitToOriginalSize;
	private Action fitToWindow;

//	private Composite parent;
	private Text  txtRoiStartX;
	private Text  txtRoiStartY;
	private Text  txtRoiStartZ;
	private Text  txtRoiEndX;
	private Text  txtRoiEndY;
	private Text  txtRoiEndZ;
	private Button btnSetROI;
	private Label lblIsoValue;
	private Scale sclIsoValue;
	private Button btnSetIsoValue;
	private Color white = null;
	private Color black = null;
	private Process serverProcess = null;
	private boolean fitToImage = false;
	private PlotServer plotServer = null;
	private String viewName;
	private java.io.File scriptFile = null;
	
	public RemoteVolVizView() {
		plotServer = PlotServerProvider.getPlotServer();
	}

	@Override
	public void createPartControl(Composite parent) {
		plotServer.addIObserver(this);
		viewName = getViewSite().getRegisteredName();
//		this.parent = parent;
		parent.setLayout(new GridLayout(4, false));
	
		IActionBars toolBar = getViewSite().getActionBars();
		setDVRMode = new Action("") {
			@Override
			public void run()
			{
				if (commandClient != null)
					commandClient.setDisplayMode(0);
			}
		};
		setISOMode = new Action("") {
			@Override
			public void run()
			{
				if (commandClient != null)
					commandClient.setDisplayMode(1);				
			}
		};
		
		setMIPMode = new Action("") {
			@Override
			public void run()
			{
				if (commandClient != null)
					commandClient.setDisplayMode(2);			
			}
		};
		
		setLightMode = new Action("") {
			@Override
			public void run() 
			{
				if (commandClient != null)
					commandClient.setDisplayMode(3);
			}
		};
		
		fitToOriginalSize = new Action("") {
			@Override
			public void run()
			{
				fitToImage = true;
				canvas.redraw();
			}
		};
		fitToOriginalSize.setText("Fit to Image size");
		fitToOriginalSize.setDescription("Fit the render window to the actual image size");
		fitToOriginalSize.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/arrow_in.png"));
		
		fitToWindow = new Action("") {
			@Override
			public void run()
			{
				fitToImage = false;
				canvas.redraw();
			}
		};
		
		fitToWindow.setText("Fit to window size");
		fitToWindow.setDescription("Fit to the display window");
		fitToWindow.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/arrow_out.png"));

		setDVRMode.setText("DVR");
		setDVRMode.setDescription("Switch to Direct Volume Rendering mode");
		setISOMode.setText("ISOSURFACE");
		setISOMode.setDescription("Switch to Isosurface Rendering mode");
		setMIPMode.setText("MIP");
		setMIPMode.setDescription("Switch to Maximum Intensity Projection mode");
		setLightMode.setDescription("Switch to Volume lighting Rendering mode");
		setLightMode.setText("VolumeShading");
		
		toolBar.getMenuManager().add(setDVRMode);
		toolBar.getMenuManager().add(setISOMode);
		toolBar.getMenuManager().add(setMIPMode);
		toolBar.getMenuManager().add(setLightMode);
		toolBar.getToolBarManager().add(fitToOriginalSize);
		toolBar.getToolBarManager().add(fitToWindow);
		
		imgData = new ImageData(640,480,24,new PaletteData(0xff0000, 0x00ff00, 0xf0000ff));
		for (int y = 0; y < 480; y++)
			for (int x = 0; x < 640; x++)
			{
				int red = x%256;
				int green = y%256;
				int blue = x%256;
				int pixelValue = (red << 16)+(green << 8)+blue;
				imgData.setPixel(x,y, pixelValue);
			}

		try {
			transferFunctionView = 
				(TransferFunctionView)getSite().getPage().showView("uk.ac.diamond.scisoft.analysis.rcp.views.TransferFunctionView");
		} catch (PartInitException e) {
			e.printStackTrace();
		}	
		

		lblIsoValue = new Label(parent,SWT.NONE);
		lblIsoValue.setText("Iso value: ");
		sclIsoValue = new Scale(parent,SWT.BORDER);
		sclIsoValue.setMaximum(256);
		sclIsoValue.setMinimum(0);
		sclIsoValue.setIncrement(1);
		sclIsoValue.setSelection((int)(0.02f * 256));
		GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,false,1,1);
		sclIsoValue.setLayoutData(gridData);
		sclIsoValue.addSelectionListener(this);
		btnSetIsoValue = new Button(parent,SWT.NONE);
		btnSetIsoValue.setText("Set iso value");
		btnSetIsoValue.addSelectionListener(this);
		canvas = new Canvas(parent,SWT.DOUBLE_BUFFERED);
		gridData = new GridData(SWT.FILL,SWT.FILL,true,true,4,3);
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addKeyListener(this);
	    testImage = new Image(canvas.getDisplay(),640,480);
		testImageGC = new GC(testImage);
		if (startExternalProcess()) {
			streamReader = new ImageStreamReader("localhost", 9999, testImageGC, canvas);
			imageStreamThread = new Thread(streamReader);
			imageStreamThread.start();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {}

			commandClient = new CommandClient("localhost", 10000);
			commandClient.addIObserver(transferFunctionView);
			transferFunctionView.addIObserver(commandClient);				
		}
	}

	private boolean startExternalProcess() {
		try {
			// build script file
			String workspaceDir = org.eclipse.core.runtime.Platform.getInstanceLocation().getURL().getPath();
			String scriptFilename = workspaceDir;
			
			if (System.getProperty("os.name").equals("Linux")) {
				scriptFilename = scriptFilename + UNIXSCRIPTNAME;
				scriptFile = new java.io.File(scriptFilename);
				FileWriter writer = new FileWriter(scriptFile);
		
				try {
						writer.write(SCRIPTSTRUNIX,0,SCRIPTSTRUNIX.length());
					writer.flush();
					writer.close();
				} catch (Exception ex) { return false; }
				
				scriptFile.setExecutable(true, false);
			
				// wait till file really exists
				
				while (scriptFile.length() == 0) {}
			}
			// launch script
			
			ProcessBuilder pb = null;
			
			if (System.getProperty("os.name").equals("Linux"))
				pb = new ProcessBuilder("./" + UNIXSCRIPTNAME);
			else if (System.getProperty("os.name").contains("Windows"))
				pb = new ProcessBuilder("gigacubeR3d","9999","--offscreen");
			if (pb != null) {
				pb.directory(new java.io.File(workspaceDir));
				serverProcess = pb.start();
			}
			
		    if (serverProcess == null)
		    	return false;
		    
		} catch (Exception ex) { return false; }
		// wait a bit to make sure process has started up!
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {}

		return true;
	}
	@Override
	public void setFocus() {
		// Nothing to do
	}

	@Override
	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		Rectangle client = canvas.getClientArea();
		if (white == null) {
			white = new Color(canvas.getDisplay(),new RGB(255,255,255));
		}
		if (black == null) {
			black = new Color(canvas.getDisplay(),new RGB(0,0,0));
		}
		gc.setForeground(white);
		gc.setBackground(black);
		gc.fillRectangle(client);
		if (!fitToImage)
			gc.drawImage(testImage,0, 0,640,480,0,0,client.width,client.height);
		else {
			int imageXPos = Math.max(0,(client.width - 640) >> 1);
			int imageYPos = Math.max(0,(client.height - 480) >> 1);
			gc.drawImage(testImage,imageXPos,imageYPos);
		}
	}
	
	@Override
	public void dispose()
	{
		streamReader.stop();
		serverProcess.destroy();
		System.err.println("Exit value "+serverProcess.exitValue());
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(transferFunctionView);
		testImage.dispose();
		if (scriptFile != null)
			scriptFile.delete();
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// Nothing to do
	}

	@Override
	public void mouseDown(MouseEvent e) {
		whichMouseButton = e.button;
		Rectangle client = canvas.getClientArea();
		mouseDown = true;
		oldMouseX = -1.0f + e.x / (client.width* 0.5f);
		oldMouseY = 1.0f - e.y / (client.height*0.5f);		
	}

	@Override
	public void mouseUp(MouseEvent e) {
		mouseDown = false;
	}

	@Override
	public void mouseMove(MouseEvent e) {
		if (mouseDown)
		{
			Rectangle client = canvas.getClientArea();
			float currentMouseX = -1.0f + e.x / (client.width*0.5f);
			float currentMouseY = 1.0f - e.y / (client.height*0.5f);
			switch (whichMouseButton)
			{
				case 1:
					commandClient.rotateVolume(currentMouseX, currentMouseY, oldMouseX, oldMouseY );
				break;
				case 2:
					commandClient.translate(currentMouseX-oldMouseX,currentMouseY-oldMouseY);
				break;
			}
			oldMouseX = currentMouseX;
			oldMouseY = currentMouseY;
		}
	}

	@Override
	public void mouseScrolled(MouseEvent e) {
		float zoomValue = -0.1f * e.count;
		commandClient.zoomVolume(zoomValue);		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// Nothing to do		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource().equals(btnSetROI)) {
			commandClient.setROI(txtRoiStartX.getText(), 
								 txtRoiStartY.getText(),
								 txtRoiStartZ.getText(),
								 txtRoiEndX.getText(),
								 txtRoiEndY.getText(),
								 txtRoiEndZ.getText());
		} else if (e.getSource().equals(sclIsoValue)) {
			transferFunctionView.setIsoValue(sclIsoValue.getSelection()/256.0f);
		} else if (e.getSource().equals(btnSetIsoValue)) {
			commandClient.setIsoValue(sclIsoValue.getSelection()/256.0f);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.keyCode == SWT.KEYPAD_ADD)
		{
			commandClient.zoomVolume(0.2f);					
		} else if (e.keyCode == SWT.KEYPAD_EQUAL)
		{
			commandClient.zoomVolume(-0.2f);
		}		
	}

	private void processGUIUpdate(GuiUpdate gu)
	{
		if (gu.getGuiName().contains(viewName)) {
			GuiBean bean = gu.getGuiData();
			if (bean.containsKey(GuiParameters.FILENAME)) {
				String rawfilename = (String)bean.get(GuiParameters.FILENAME);
				if (bean.containsKey(GuiParameters.VOLUMEHEADERSIZE)) {
					int headersize = (Integer)bean.get(GuiParameters.VOLUMEHEADERSIZE);
					int voxelType = (Integer)bean.get(GuiParameters.VOLUMEVOXELTYPE);
					int xDim = (Integer)bean.get(GuiParameters.VOLUMEXDIM);
					int yDim = (Integer)bean.get(GuiParameters.VOLUMEYDIM);
					int zDim = (Integer)bean.get(GuiParameters.VOLUMEZDIM);
					if (commandClient != null) 
						commandClient.loadRawVolume(rawfilename, headersize, voxelType, xDim, yDim, zDim);
				} else {
					if (commandClient != null)
						commandClient.loadDSRVolume(rawfilename);
				}
				if (commandClient != null) {
					canvas.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								commandClient.getHistogram();
							}
						});
				}
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// Nothing to do
		
	}
	@Override
	public void update(Object source, Object changeCode) {
		if (changeCode instanceof GuiUpdate)
		{
			GuiUpdate gu = (GuiUpdate) changeCode;
			processGUIUpdate(gu);
		}		
	}

}
