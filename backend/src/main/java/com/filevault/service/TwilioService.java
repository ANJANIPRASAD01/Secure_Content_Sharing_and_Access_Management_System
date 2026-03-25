package com.filevault.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TwilioService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    private boolean isTwilioConfigured() {
        return accountSid != null && !accountSid.isEmpty() &&
               authToken != null && !authToken.isEmpty() &&
               twilioPhoneNumber != null && !twilioPhoneNumber.isEmpty();
    }

    public void sendAccessApprovalSMS(String userPhoneNumber, String userName, String fileName) {
        if (!isTwilioConfigured()) {
            logger.warn("Twilio not configured. Skipping SMS to " + userPhoneNumber);
            return;
        }

        try {
            Twilio.init(accountSid, authToken);
            String messageBody = "Hi! Your access request for file '" + fileName + 
                                "' has been approved. You can now download it from FileVault.";
            
            Message message = Message.creator(
                    new PhoneNumber(twilioPhoneNumber),
                    new PhoneNumber(userPhoneNumber),
                    messageBody
            ).create();
            
            logger.info("Access approval SMS sent to " + userPhoneNumber + " with SID: " + message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send access approval SMS to " + userPhoneNumber, e);
        }
    }

    public void sendAccessDenialSMS(String userPhoneNumber, String fileName) {
        if (!isTwilioConfigured()) {
            logger.warn("Twilio not configured. Skipping SMS to " + userPhoneNumber);
            return;
        }

        try {
            Twilio.init(accountSid, authToken);
            String messageBody = "Your access request for file '" + fileName + 
                                "' has been denied. Please contact the file owner for more information.";
            
            Message message = Message.creator(
                    new PhoneNumber(twilioPhoneNumber),
                    new PhoneNumber(userPhoneNumber),
                    messageBody
            ).create();
            
            logger.info("Access denial SMS sent to " + userPhoneNumber + " with SID: " + message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send access denial SMS to " + userPhoneNumber, e);
        }
    }

    public void sendAccessRequestNotificationToAdmin(String adminPhoneNumber, String requesterName, String fileName) {
        if (!isTwilioConfigured()) {
            logger.warn("Twilio not configured. Skipping SMS to admin " + adminPhoneNumber);
            return;
        }

        try {
            Twilio.init(accountSid, authToken);
            String messageBody = "Admin Alert: User '" + requesterName + 
                                "' has requested access to file '" + fileName + 
                                "'. Log in to FileVault to review and approve/deny.";
            
            Message message = Message.creator(
                    new PhoneNumber(twilioPhoneNumber),
                    new PhoneNumber(adminPhoneNumber),
                    messageBody
            ).create();
            
            logger.info("Access request notification SMS sent to admin " + adminPhoneNumber + " with SID: " + message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send access request notification SMS to admin " + adminPhoneNumber, e);
        }
    }

    public void sendAdminActionNotificationSMS(String adminPhoneNumber, String userName, String action, String fileName) {
        if (!isTwilioConfigured()) {
            logger.warn("Twilio not configured. Skipping SMS to admin " + adminPhoneNumber);
            return;
        }

        try {
            Twilio.init(accountSid, authToken);
            String messageBody = "Admin notification: You have " + action + " access for user '" + userName + 
                                "' to file '" + fileName + "'.";
            
            Message message = Message.creator(
                    new PhoneNumber(twilioPhoneNumber),
                    new PhoneNumber(adminPhoneNumber),
                    messageBody
            ).create();
            
            logger.info("Admin action notification SMS sent to " + adminPhoneNumber + " with SID: " + message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send admin action notification SMS to " + adminPhoneNumber, e);
        }
    }
}
