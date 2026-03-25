# Twilio SMS Notifications Setup Guide

## **What We Added**

✅ **SMS Notifications for:**
1. **User Approval:** User receives SMS when access request is approved
2. **Admin Notification:** Admin can receive SMS notifications (optional)
3. **User Denial:** User receives SMS when access request is denied

---

## **Step 1: Sign Up for Twilio**

1. Go to https://www.twilio.com/try-twilio
2. Sign up (Free trial: $15 credit - enough for testing)
3. Verify your email and phone number
4. Create account

---

## **Step 2: Get Twilio Credentials**

### **In Twilio Console:**

1. Go to https://www.twilio.com/console
2. Find your **Account SID** - displayed on dashboard
3. Find your **Auth Token** - displayed on dashboard
4. Go to **Phone Numbers** → **Manage** → **Active Numbers**
5. Copy your **Twilio Phone Number** (e.g., +1234567890)

---

## **Step 3: Update Existing Users with Phone Numbers**

For your **existing admin and user**, update their phone numbers:

### **Option A: Via Database (Direct)**

Connect to your PostgreSQL database:

```sql
UPDATE users 
SET phone_number = '+1234567890' 
WHERE id = 1;  -- Admin user

UPDATE users 
SET phone_number = '+0987654321' 
WHERE id = 2;  -- Regular user
```

### **Option B: Via API (Frontend)**

Use the new endpoint to update phone numbers:

```
POST /api/user/{userId}/update-phone
Content-Type: application/json

{
  "phoneNumber": "+1234567890"
}
```

### **Option C: Update Registration Form (Going Forward)**

When users register, they'll be prompted to enter phone number.

---

## **Step 4: Configure Environment Variables**

### **Local Development (.env or system variables)**

```bash
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_PHONE_NUMBER=+your-twilio-phone-number
```

### **Render Deployment**

Go to **Render Dashboard** → Your Backend Service → **Environment**:

```
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_PHONE_NUMBER=+your-twilio-phone-number
```

---

## **Step 5: Test Locally**

### **Scenario 1: User Gets Access Approval**

1. **Admin** approves user's access request
2. Backend calls: `twilioService.sendAccessApprovalSMS()`
3. **User receives SMS:**
   ```
   Hi! Your access request for file 'Document.pdf' has been 
   approved. You can now download it from FileVault.
   ```

### **Scenario 2: User Gets Access Denied**

1. **Admin** denies user's access request
2. Backend calls: `twilioService.sendAccessDenialSMS()`
3. **User receives SMS:**
   ```
   Your access request for file 'Document.pdf' has been denied. 
   Please contact the file owner for more information.
   ```

---

## **API Endpoints**

### **1. Update User Phone Number**

```
POST /api/user/{userId}/update-phone
Content-Type: application/json

{
  "phoneNumber": "+1234567890"
}

Response:
{
  "message": "Phone number updated successfully",
  "phoneNumber": "+1234567890"
}
```

### **2. Approve Access Request (Sends SMS)**

```
POST /api/access-requests/approve
Content-Type: application/json
?adminId=1

{
  "requestId": 5
}

Response: AccessRequestDTO with SMS sent to user
```

### **3. Reject Access Request (Sends SMS)**

```
POST /api/access-requests/reject
Content-Type: application/json
?adminId=1

{
  "requestId": 5,
  "reason": "Sensitive information"
}

Response: AccessRequestDTO with SMS sent to user
```

---

## **For Existing Users: Update Phone Numbers**

### **User 1 (ID: 1 - Admin)**

```
POST /api/user/1/update-phone
{
  "phoneNumber": "+1234567890"
}
```

### **User 2 (ID: 2 - Regular User)**

```
POST /api/user/2/update-phone
{
  "phoneNumber": "+0987654321"
}
```

---

## **Testing with Twilio Free Trial**

**Without Verified Numbers:**
- Free trial limits SMS to verified phone numbers
- Go to Twilio Console → **Phone Numbers** → **Verified Caller IDs**
- Add your number there first, then SMS will be sent to that number

**Sandbox Mode (Optional):**
- You can test without real SMS using Twilio Sandbox
- All messages go to console instead of real phones

---

## **Production Setup**

When deploying to production:

1. **Upgrade Twilio Account** (costs per SMS, ~$0.01 per message)
2. **Add Billing:** Twilio Console → Billing
3. **Environment Variables:** Add to Render with real credentials
4. **Phone Number Format:** Must include country code (e.g., +1 for USA)

---

## **Troubleshooting**

### **SMS Not Sending**

1. Check Twilio credentials in environment variables
2. Verify user has phone number in database
3. Check Twilio Console logs for errors
4. Ensure phone number format: `+[country-code][number]`

### **Invalid Phone Number Error**

```
Error: Invalid 'To' parameter
```

**Solution:** Add country code prefix
```
❌ 1234567890
✅ +11234567890
```

### **No Credentials Error**

```
Error: Twilio not configured
```

**Solution:** Add environment variables to backend service

---

## **Implementation Files Created**

✅ `backend/src/main/java/com/filevault/service/TwilioService.java` - SMS sending service
✅ `backend/src/main/java/com/filevault/dto/PhoneNumberRequest.java` - DTO
✅ Updated `AccessRequestController.java` - SMS on approve/deny
✅ Updated `UserController.java` - Phone number update endpoint
✅ Updated `UserService.java` - Phone update method
✅ Updated `pom.xml` - Twilio dependency
✅ Updated `application.properties` - Twilio config

---

## **Next Steps**

1. Add Twilio credentials to Render environment variables
2. Update existing users' phone numbers via API
3. Test by approving/denying access requests
4. Redeploy backend with Twilio integration
5. Monitor Twilio console for delivery reports

---

**Twilio is now integrated and ready!** 🚀
