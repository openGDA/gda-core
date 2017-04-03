package uk.ac.gda.client.closeactions;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.client.closeactions.ClientCloseOption;
import uk.ac.gda.client.closeactions.contactinfo.ISPyBLocalContacts;
import uk.ac.gda.client.closeactions.contactinfo.LdapEmail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

	public void doCloseAction(ClientCloseOption selectedOption, String reason) {
		optionChoice = selectedOption;
		switch (selectedOption)
		{
		case RESTART_CLIENT:
			sendEmail(reason, "GDA Restart Reasoning/Feedback");
			logger.info("User felt the need to restart client. Could we be having client issues?");
			break;
		case RESTART_CLIENT_AND_SERVER:
			sendEmail(reason, "GDA Restart Reasoning/Feedback");
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
						final String beamlineName = LocalProperties.get("gda.beamline.name","Beamline Unknown").toUpperCase();
						final String visit = LocalProperties.get(LocalProperties.RCP_APP_VISIT);
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

	private String[] getEmailRecipients() {
		//using List because number of recipients is variable
		List<String> eRs = new ArrayList<>();

		eRs.add("victoria.lawson@diamond.ac.uk");
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
			//should only ever return single emails
			eRs.add(getEmailAddress(getLocalContact()));
			eRs.add(getEmailAddress(getNextLocalContact()));
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
		String recipientsProperty = LocalProperties.get("gda.feedback.recipients","dag-group@diamond.ac.uk");
		String[] recipients = recipientsProperty.split(" ");
		for (String recipient : recipients) {
			recipient.trim();
			eRs.add(recipient);
		}
		return eRs;
	}

	private String getEmailAddress(String fedID){
		return new LdapEmail().forFedID(fedID);
	}

	private String getLocalContact(){
		return getLocalContact(LocalProperties.get(LocalProperties.RCP_APP_VISIT));
	}

	private String getLocalContact(String visit){
		//filter out cm and nr
		if (!(visit.startsWith("cm") || visit.startsWith("nr"))) {
			String result = null;
			try {
				logger.debug("Connecting to ISPyB.");
				JdbcTemplate template = ISPyBLocalContacts.connectToDatabase();
				logger.debug("Retrieving local contact from ISPyB.");
				result = new ISPyBLocalContacts(template).forCurrentVisit(visit);
			} catch(Exception e) {
				logger.error("There was an error connecting to ISPyB while retrieving local contact", e);
			}
			return result;
		}
		return "";
	}

	private String getNextLocalContact(){
		for (String result : getNextVisits()){
			//filter out cm and nr
			if (!(result.startsWith("cm") || result.startsWith("nr"))) {
				String[] parts = result.split(",");
				String startDate[] = parts[1].split(" ");

				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				int oneDayInMillis = 24*60*60*1000;
				String tomorrow = df.format(new Date(System.currentTimeMillis() + oneDayInMillis));

				if (startDate[0] == tomorrow){
					String currentLC = getLocalContact();
					String nextLC = getLocalContact(parts[0]);
					if (currentLC != nextLC) {
						return nextLC;
					}
				}
			}
		}
		return "";
	}

	private List<String> getNextVisits(){
		String beamline = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
		 List<String> result = null;
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