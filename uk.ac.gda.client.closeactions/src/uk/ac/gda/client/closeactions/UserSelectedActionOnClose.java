package uk.ac.gda.client.closeactions;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;
import gda.util.Email;
import uk.ac.gda.client.closeactions.ClientCloseOption;
import uk.ac.gda.client.closeactions.contactinfo.ISPyBJdbcTemplate;
import uk.ac.gda.client.closeactions.contactinfo.ISPyBLocalContacts;
import uk.ac.gda.client.closeactions.contactinfo.ISPyBVisits;
import uk.ac.gda.client.closeactions.contactinfo.LdapEmail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * called by UserOptionsMenuOnClose
 * 
 * Performs various actions depending on the reason the user has closed the client. 
 * 
 * Mostly sends emails. Notifies Local Contacts, PBSs etc. 
 */

public class UserSelectedActionOnClose {

	private final static Logger logger = LoggerFactory.getLogger(UserSelectedActionOnClose.class);

	private ClientCloseOption optionChoice;
	private String visit;
	private String beamlineName;

	UserSelectedActionOnClose(){
		beamlineName = LocalProperties.get("gda.beamline.name","Beamline Unknown").toUpperCase();
		visit = LocalProperties.get(LocalProperties.RCP_APP_VISIT);
	}

	public void doCloseAction(ClientCloseOption selectedOption, String reason, String name) {
		ClientDetails[] clients = InterfaceProvider.getBatonStateProvider().getOtherClientInformation();
		optionChoice = selectedOption;
		switch (selectedOption)
		{
		case RESTART_CLIENT:
			trimAndEmail(reason, name);
			logger.info("User felt the need to restart GDA. Could we be having DAQ issues?");
			break;
		case FINISHED:
			break;
		case FINISHED_UDC:
			for (ClientDetails client : clients) {
				if (client.isAutomatedUser()) {
					int batonholderIndex = InterfaceProvider.getBatonStateProvider().getBatonHolder().getIndex();
					InterfaceProvider.getBatonStateProvider().assignBaton(client.getIndex(), batonholderIndex);
					break;
				}
			}
			break;

		default:
			break;
		}
	}

	private void trimAndEmail(String reason, String name) {
		if (!reason.trim().isEmpty()){
			String user = System.getProperty("user.name");
			String text = String.format("%s%n%nSent by: %s%nLogged in as: %s", reason, name.trim(), user);
			logToTextFile(text);
			sendEmail(text, "GDA Restart Reasoning/Feedback");
		}
	}

	private void sendEmail(final String text, final String subject){
		if (!LocalProperties.isDummyModeEnabled()) {
			Job job = new Job("Send closeAction feedback email") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try{
						new Email()
								.to(getEmailRecipients())
								.from(visit, beamlineName)
								.subject(subject + " " + beamlineName)
								.message(text.isEmpty()
										? "The current user (visit: " + visit + ") on " + beamlineName + " is finished."
										: text)
								.send();
						return Status.OK_STATUS;
					} catch(Exception e){
						logger.error("Could not send feedback", e);
						return new Status(IStatus.ERROR, "uk.ac.gda.client.closeactions", 1, "Error sending email", e);
					}
				}
			};
			job.schedule();
		}
	}

	private void logToTextFile(String text){
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		String date = df.format(new Date());

		String[] pid_host = ManagementFactory.getRuntimeMXBean().getName().split("@");
		String file = LocalProperties.get("gda.gui.closeMenuFeedbackFile", LocalProperties.get("gda.logs.dir") + "/user_restart_reasons.txt");

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));) {
			writer.write(date + " " + visit + " " + beamlineName + "@" + pid_host[1] + ": ");
			writer.write(text.replace('\n', ' '));
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			logger.error("Problem accessing local property: ", e);
		}
	}

	private String[] getEmailRecipients() {
		//using List because number of recipients is variable
		List<String> eRs = new ArrayList<>();

		switch (optionChoice)
		{
		case RESTART_CLIENT:
			for (String email : getFeedbackGroup()) {
				eRs.add(email);
			}
			break;
		case FINISHED:
			for (String LocalContact : getCurrentAndNextLocalContacts()) {
				eRs.add(getEmailAddress(LocalContact));
			}
			eRs.add(LocalProperties.get("gda.principalbeamlinescientist"));
			break;
		default:
			break;
		}

		logger.debug("eRs: {}", eRs);
		List<String> viableEmailRecipients = new ArrayList<>();
		for (String eR : eRs) {
			if (eR != null && eR.trim().length() > 0) {
				viableEmailRecipients.add(eR);
			}
		}
		logger.debug("viableEmailRecipients: {}", viableEmailRecipients);
		
		//convert to String[] for Mime
		String[] recipients = new String[viableEmailRecipients.size()];
		viableEmailRecipients.toArray(recipients);
		return recipients;
	}

	private List<String> getFeedbackGroup() {
		List<String> eRs = new ArrayList<>();
		addRecipients(eRs, LocalProperties.get("gda.feedback.recipients",""));
		addRecipients(eRs, LocalProperties.get("gda.developers", ""));
		return eRs;
	}

	private void addRecipients(List<String> eRs, String recipientsString) {
		if (!(recipientsString.equals(""))) {
			String[] recipients = recipientsString.split(" ");
			for (String recipient : recipients) {
				eRs.add(recipient.trim());
			}
		}
	}

	private String getEmailAddress(String fedID){
		return new LdapEmail().forFedID(fedID);
	}

	private Set<String> getCurrentAndNextLocalContacts(){
		Set<String> localContacts = new HashSet<>();
		localContacts.addAll(getLocalContact(LocalProperties.get(LocalProperties.RCP_APP_VISIT)));
		for (String visit: getNextVisits()){
			localContacts.addAll(getLocalContact(visit));
		}
		return localContacts;
	}

	private List<String> getLocalContact(String visit){
		List<String> result = new ArrayList<>();
		//filter out cm and nr so we're not inundating devs with emails during commissioning etc.
		if (!(visit.startsWith("cm") || visit.startsWith("nr"))) {
			try {
				logger.debug("Retrieving local contact from ISPyB.");
				result = new ISPyBLocalContacts(new ISPyBJdbcTemplate().template()).forCurrentVisit(visit);
			} catch(Exception e) {
				logger.error("There was an error connecting to ISPyB while retrieving local contact", e);
			}
		}
		return result;
	}

	private List<String> getNextVisits(){
		String beamline = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
		List<String> result = new ArrayList<>();
		try {
			logger.debug("Retrieving next visits from ISPyB.");
			result = new ISPyBVisits(new ISPyBJdbcTemplate().template()).followingVisits(beamline);
		} catch(Exception e) {
			logger.error("There was an error connecting to ISPyB while retrieving visits: ", e);
		}
		return result;
	}
}