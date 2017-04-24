package uk.ac.gda.client.closeactions;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.client.closeactions.ClientCloseOption;
import uk.ac.gda.client.closeactions.contactinfo.ISPyBLocalContacts;
import uk.ac.gda.client.closeactions.contactinfo.LdapEmail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.internet.MimeMessage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.jdbc.core.JdbcTemplate;

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

	public void doCloseAction(ClientCloseOption selectedOption, String reason) {
		optionChoice = selectedOption;
		switch (selectedOption)
		{
		case RESTART_CLIENT:
			if (!reason.trim().isEmpty()){
				logToTextFile(reason);
				sendEmail(reason, "GDA Restart Reasoning/Feedback");
			}
			logger.info("User felt the need to restart client. Could we be having client issues?");
			break;
		case RESTART_CLIENT_AND_SERVER:
			if (!reason.trim().isEmpty()){
				logToTextFile(reason);
				sendEmail(reason, "GDA Restart Reasoning/Feedback");
			}
			logger.info("User felt the need to restart client and server. Could we be having server/control station issues?");
			if (!LocalProperties.isDummyModeEnabled()){
				ProcessBuilder pb = new ProcessBuilder("gdaservers_closemenurestart");
				try {
					pb.start();
				} catch (IOException e) {
					logger.error("There was a problem restarting servers from the Close Menu: " + e);
				}
			}
			break;
		case TEMP_ABSENCE:
			// do nothing, client will exit cleanly
			break;
		case FINISHED:
			sendEmail("", "Current user is finished");
			break;
		}
	}

	private void sendEmail(final String text, final String subject){
		if (!LocalProperties.isDummyModeEnabled()) {
			Job job = new Job("Send closeAction feedback email") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try{
						final String[] recipients = getEmailRecipients();
						final String from = String.format("%s <%s>", visit, beamlineName);
						final String mailSubject = subject + " " + beamlineName;
	
						final String smtpHost = LocalProperties.get("gda.feedback.smtp.host","localhost");
	
						JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
						mailSender.setHost(smtpHost);
	
						MimeMessage message = mailSender.createMimeMessage();
						final MimeMessageHelper helper = new MimeMessageHelper(message, false);
						helper.setFrom(from);
						helper.setTo(recipients);
						helper.setSubject(mailSubject);

						if (text != "") {
							helper.setText(text);
						} else {
							helper.setText("The current user (visit: " + visit + ") on " + beamlineName + " is finished.");
						}

						{//required to workaround class loader issue with "no object DCH..." error
							MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
							mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
							mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
							CommandMap.setDefaultCommandMap(mc);
						}
						mailSender.send(message);
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
		case RESTART_CLIENT_AND_SERVER:
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
				logger.debug("Connecting to ISPyB.");
				JdbcTemplate template = ISPyBLocalContacts.connectToDatabase();
				logger.debug("Retrieving local contact from ISPyB.");
				result = new ISPyBLocalContacts(template).forCurrentVisit(visit);
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
			logger.debug("Connecting to ISPyB.");
			JdbcTemplate template = ISPyBLocalContacts.connectToDatabase();
			logger.debug("Retrieving next visits from ISPyB.");
			result = new ISPyBLocalContacts(template).forFollowingVisit(beamline);
		} catch(Exception e) {
			logger.error("There was an error connecting to ISPyB while retrieving visits: ", e);
		}
		return result;
	}
}