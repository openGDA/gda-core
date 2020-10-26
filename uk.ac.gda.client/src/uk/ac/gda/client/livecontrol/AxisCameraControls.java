/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LiveControl implementation to control the pan, tilt, zoom, focus, iris of Axis webcams.
 * Commands are sent to ptz.cgi on the camera webserver.
 */
public class AxisCameraControls extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(AxisCameraControls.class);

	private String fullCgiUrl = ""; /** Full URL ptz.cgi file on server */
	private String baseUrl = ""; /** Server URL including any authentication. e.g. http://i20:!20@i20-1-webcam2/ */
	private String cgiFilePath = "axis-cgi/com/ptz.cgi"; /** Path to ptz.cgi file on server */

	private double zoomStep = 1200;
	private double focusStep = 1200;
	private double irisStep = 250;
	private int buttonWidth = 50;
	private int labelWidth = 50;

	private int cameraNumber = 1;

	private boolean showFocusControls = false;
	private boolean showIrisControls = false;

	private GridDataFactory buttonGridFactory = GridDataFactory.fillDefaults().grab(false, false).hint(buttonWidth, SWT.DEFAULT);
	private GridDataFactory labelGridFactory = GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(false, false).hint(labelWidth, SWT.DEFAULT);

	@Override
	public void createControl(Composite composite) {
		getCgiUrl().ifPresent(newBase -> fullCgiUrl = newBase);

		if (fullCgiUrl.isEmpty()) {
			final Label infoLabel = new Label(composite, SWT.NONE);
			infoLabel.setText("Warning! Camera URL has not been set - could not setup camera controls!");
			return;
		}
		composite.setToolTipText("Camera controls for "+fullCgiUrl.replace(cgiFilePath, ""));

		addControls(composite, "Pan", "Left", getUrlString(Command.MOVE, Direction.LEFT), "Right", getUrlString(Command.MOVE, Direction.RIGHT));
		addControls(composite, "Tilt", "Up", getUrlString(Command.MOVE, Direction.UP), "Down", getUrlString(Command.MOVE, Direction.DOWN));
		addControls(composite, "Zoom", "In", getUrlString(Command.ZOOM, zoomStep), "Out", getUrlString(Command.ZOOM, -zoomStep));

		if (showFocusControls) {
			addControls(composite, "Focus", "Near", getUrlString(Command.FOCUS, -focusStep), "Far", getUrlString(Command.FOCUS, focusStep));
		}
		if (showIrisControls) {
			addControls(composite, "Iris", "Close", getUrlString(Command.IRIS, -irisStep), "Open", getUrlString(Command.IRIS, irisStep));
		}
	}

	/**
	 * @return full URL to cgi file on server by appending {@link #cgiFilePath} to {@link #baseUrl}
	 */
	private Optional<String> getCgiUrl() {
		if (baseUrl != null) {
			return Optional.of(baseUrl + "/" + cgiFilePath);
		}
		return Optional.empty();
	}

	private void addControls(Composite parent, String label, String b1Label, String b1Command, String b2Label, String b2Command) {
		Group g = new Group(parent, SWT.NONE);
		GridLayout gl = new GridLayout(3,  false);
		g.setLayout(gl);
		addLabel(g, label);
		addButton(g, b1Label, b1Command);
		addButton(g, b2Label, b2Command);
	}

	private void addLabel(Composite parent, String label) {
		final Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		labelGridFactory.applyTo(l);
	}

	private void addButton(Composite parent, String label, String urlString) {
		final Button button = new Button(parent, SWT.NONE);
		button.setText(label);
		button.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> sendRequest(urlString)));
		buttonGridFactory.applyTo(button);
	}

	private String getUrlString(Command comm, double value) {
		return String.format("%s?camera=%d&%s=%.1f", fullCgiUrl, cameraNumber, comm, value);
	}

	private String getUrlString(Command comm, Direction moveDirection) {
		return String.format("%s?camera=%d&%s=%s", fullCgiUrl, cameraNumber, comm, moveDirection);
	}

	public void sendRequest(String urlString) {
		try {
			executeRequest(urlString);
		} catch (IOException e) {
			logger.error("Problem sending HTTP request to camera", e);
		}
	}

	/**
	 * Send HTTP request to the server.
	 * @param urlString
	 * @return
	 * @throws IOException
	 */
	private HttpEntity executeRequest(String urlString) throws IOException {
		logger.debug("Trying to connect to {}", urlString);
		authenticate(urlString);

		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(new HttpGet(urlString));
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() != HttpStatus.SC_OK) {
			logger.warn("Possible problem with connection - {} (code = {})", status.getReasonPhrase(),
					status.getStatusCode());
		} else {
			logger.debug("Connected ok");
		}
		return response.getEntity();
	}

	/**
	 * Set authentication (username and password) to use for the host
	 * @param urlString
	 * @throws MalformedURLException
	 */
	private void authenticate(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);
		if (url.getUserInfo() != null) {
			logger.debug("Setting username and password : {}", url.getUserInfo());

			// Get username and password from the url
			String[] userPasswd = url.getUserInfo().split(":");

			// Setup credentials for authenticating connection
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(url.getHost(), AuthScope.ANY_PORT), new UsernamePasswordCredentials(userPasswd[0], userPasswd[1]));
		}
	}

	private enum Command {
		ZOOM("rzoom"),
		PAN("rpan"),
		FOCUS("rfocus"),
		IRIS("riris"),
		MOVE("move");

		private final String commandString;

		private Command(String comm) {
			commandString = comm;
		}

		@Override
		public String toString() {
			return commandString;
		}
	}

	private enum Direction {
		UP("up"),
		DOWN("down"),
		LEFT("left"),
		RIGHT("right");

		private final String dirString;

		private Direction(String dirString) {
			this.dirString = dirString;
		}

		@Override
		public String toString() {
			return dirString;
		}
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setFullCgiUrl(String fullCgiUrl) {
		this.fullCgiUrl = fullCgiUrl;
	}

	public void setCameraNumber(int cameraNumber) {
		this.cameraNumber = cameraNumber;
	}

	public void setZoomStep(double zoomStep) {
		this.zoomStep = zoomStep;
	}

	public void setFocusStep(double focusStep) {
		this.focusStep = focusStep;
	}

	public void setShowFocusControls(boolean showFocusControls) {
		this.showFocusControls = showFocusControls;
	}

	public void setShowIrisControls(boolean showIrisControls) {
		this.showIrisControls = showIrisControls;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
		result = prime * result + buttonWidth;
		result = prime * result + cameraNumber;
		result = prime * result + ((cgiFilePath == null) ? 0 : cgiFilePath.hashCode());
		long temp;
		temp = Double.doubleToLongBits(focusStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((fullCgiUrl == null) ? 0 : fullCgiUrl.hashCode());
		temp = Double.doubleToLongBits(irisStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (showFocusControls ? 1231 : 1237);
		result = prime * result + (showIrisControls ? 1231 : 1237);
		temp = Double.doubleToLongBits(zoomStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AxisCameraControls other = (AxisCameraControls) obj;
		if (baseUrl == null) {
			if (other.baseUrl != null)
				return false;
		} else if (!baseUrl.equals(other.baseUrl))
			return false;
		if (buttonWidth != other.buttonWidth)
			return false;
		if (cameraNumber != other.cameraNumber)
			return false;
		if (cgiFilePath == null) {
			if (other.cgiFilePath != null)
				return false;
		} else if (!cgiFilePath.equals(other.cgiFilePath))
			return false;
		if (Double.doubleToLongBits(focusStep) != Double.doubleToLongBits(other.focusStep))
			return false;
		if (fullCgiUrl == null) {
			if (other.fullCgiUrl != null)
				return false;
		} else if (!fullCgiUrl.equals(other.fullCgiUrl))
			return false;
		if (Double.doubleToLongBits(irisStep) != Double.doubleToLongBits(other.irisStep))
			return false;
		if (showFocusControls != other.showFocusControls)
			return false;
		if (showIrisControls != other.showIrisControls)
			return false;
		if (Double.doubleToLongBits(zoomStep) != Double.doubleToLongBits(other.zoomStep))
			return false;
		return true;
	}
}
