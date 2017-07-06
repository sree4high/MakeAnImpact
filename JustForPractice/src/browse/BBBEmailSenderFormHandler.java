package com.bbb.browse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;


import atg.core.util.StringUtils;
import atg.droplet.DropletException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.userprofiling.Profile;
import atg.userprofiling.ProfileTools;
import atg.userprofiling.email.TemplateEmailException;
import atg.userprofiling.email.TemplateEmailInfoImpl;

import com.bbb.commerce.catalog.BBBCatalogTools;
import com.bbb.common.BBBGenericFormHandler;
import com.bbb.constants.BBBCoreConstants;
import com.bbb.constants.BBBGiftRegistryConstants;
import com.bbb.email.BBBEmailHelper;
import com.bbb.email.BBBTemplateEmailSender;
import com.bbb.exception.BBBBusinessException;
import com.bbb.exception.BBBSystemException;
import com.bbb.utils.BBBUtility;

/**
 * @author agoe21
 *
 */
public class BBBEmailSenderFormHandler extends BBBGenericFormHandler {

	
	public static final String MSG_ACTION_SUCCESS = "E-mail has been sent successfully. Check your inbox.";

	private TemplateEmailInfoImpl mEmailInfo;

	public TemplateEmailInfoImpl getEmailInfo() {
		return mEmailInfo;
	}

	public void setEmailInfo(final TemplateEmailInfoImpl mEmailInfo) {
		this.mEmailInfo = mEmailInfo;
	}

	
	/**
	 * Success URL.
	 */
	private String mSuccessURL;
	/**
	 * Error URL.
	 */
	private String mErrorURL;
	/**
	 * Template URL.
	 */
	private String mTemplateUrl;
	/**
	 * Recipient name.
	 */
	private String mRecipientName;
	/**
	 * Recipient e-mail.
	 */
	private String mRecipientEmail;
	/**
	 * Sender name.
	 */
	private String mSenderName;
	/**
	 * Sender e-mail.
	 */
	private String mSenderEmail;
	/**
	 * Message.
	 */
	private String mMessage;

	/**
	 * Site id.
	 */
	private String mSiteId;
	
	
	private String mServerName;
	private String mContextPath;	
	private String mStoreContextPath; // variable to get context path from properties file - REST Specific

	/**
	 * @return the mStoreContextPath
	 */
	public String getStoreContextPath() {
		return mStoreContextPath;
	}

	/**
	 * @param mStoreContextPath the mStoreContextPath to set
	 */
	public void setStoreContextPath(String mStoreContextPath) {
		this.mStoreContextPath = mStoreContextPath;
	}

	public String getServerName() {
		return mServerName;
	}

	public void setServerName(String pServerName) {
		this.mServerName = pServerName;
	}	

	public String getContextPath() {
		return mContextPath;
	}

	public void setContextPath(String pContextPath) {
		this.mContextPath = pContextPath;
	}

	public BBBEmailSenderFormHandler() {
		super();
	}

	// --------------------------------------------------
	// property: Subject
	private String mSubject;

	/**
	 * @return the String
	 */
	public String getSubject() {
		return mSubject;
	}

	/**
	 * @param Subject
	 *            the String to set
	 */
	public void setSubject(final String pSubject) {
		mSubject = pSubject;
	}

	/**
	 * @return the URL of the success page.
	 */
	public String getSuccessURL() {
		return mSuccessURL;
	}

	/**
	 * Sets the URL of the success page.
	 * 
	 * @param pSuccessURL
	 *            - the URL of the success page
	 */
	public void setSuccessURL(final String pSuccessURL) {
		if (StringUtils.isBlank(pSuccessURL)) {
			mSuccessURL = null;
		} else {
			mSuccessURL = pSuccessURL;
		}
	}

	/**
	 * @return the URL of the error page.
	 */
	public String getErrorURL() {
		return mErrorURL;
	}

	/**
	 * Sets the URL of the error page.
	 * 
	 * @param pErrorURL
	 *            - the URL of the error page
	 */
	public void setErrorURL(final String pErrorURL) {
		if (StringUtils.isBlank(pErrorURL)) {
			mErrorURL = null;
		} else {
			mErrorURL = pErrorURL;
		}
	}

	/**
	 * Gets the value of the RecipientName: field.
	 * 
	 * @return the value of the RecipientName: field.
	 */
	public String getRecipientName() {
		return mRecipientName;
	}

	/**
	 * Sets the value of the RecipientName: field.
	 * 
	 * @param pRecipientName
	 *            - the value of the RecipientName: field.
	 */
	public void setRecipientName(final String pRecipientName) {
		mRecipientName = pRecipientName;
	}

	/**
	 * Gets the value of the RecipientEmail: field.
	 * 
	 * @return the value of the RecipientEmail: field.
	 */
	public String getRecipientEmail() {
		return mRecipientEmail;
	}

	/**
	 * Sets the value of the RecipientEmail: field.
	 * 
	 * @param pRecipientEmail
	 *            - the value of the RecipientEmail: field.
	 */
	public void setRecipientEmail(final String pRecipientEmail) {
		mRecipientEmail = pRecipientEmail.trim();
	}

	/**
	 * Gets the value of the SenderName: field.
	 * 
	 * @return the value of the SenderName: field.
	 */
	public String getSenderName() {
		return mSenderName;
	}

	/**
	 * Sets the value of the SenderName: field.
	 * 
	 * @param pSenderName
	 *            - the value of the SenderName: field.
	 */
	public void setSenderName(final String pSenderName) {
		mSenderName = pSenderName;
	}

	/**
	 * Gets the value of the SenderEmail: field.
	 * 
	 * @return the value of the SenderEmail: field.
	 */
	public String getSenderEmail() {
		return mSenderEmail;
	}

	/**
	 * Sets the value of the SenderEmail: field.
	 * 
	 * @param pSenderEmail
	 *            - the value of the SenderEmail: field.
	 */
	public void setSenderEmail(final String pSenderEmail) {
		mSenderEmail = pSenderEmail.trim();
	}

	/**
	 * Gets the value of the SiteId: field.
	 * 
	 * @return the value of the SiteId: field.
	 */
	public String getSiteId() {
		return mSiteId;
	}

	/**
	 * Sets the value of the SiteId: field.
	 * 
	 * @param pSiteId
	 *            - the value of the SiteId: field.
	 */
	public void setSiteId(final String pSiteId) {
		mSiteId = pSiteId;
	}

	/**
	 * Gets the value of the Message: field.
	 * 
	 * @return the value of the Message: field.
	 */
	public String getMessage() {
		return mMessage;
	}

	/**
	 * Sets the value of the Message: field.
	 * 
	 * @param pMessage
	 *            - the value of the Message: field.
	 */
	public void setMessage(final String pMessage) {
		mMessage = pMessage;
	}

	/**
	 * Sets the URL for the email template used to send the email. This is
	 * configured in the component property file.
	 * 
	 * @param pTemplateUrl
	 *            - the URL
	 */
	public void setTemplateUrl(final String pTemplateUrl) {
		mTemplateUrl = pTemplateUrl;
	}

	/**
	 * Gets the URL for the email template used to send the email. This is
	 * configured in the component property file.
	 * 
	 * @return the URL
	 */
	public String getTemplateUrl() {
		return mTemplateUrl;
	}

	// --------------------------------------------------
	// property: mTemplateUrlName
	private String mTemplateUrlName;

	/**
	 * @return the String
	 */
	public String getTemplateUrlName() {
		return mTemplateUrlName;
	}

	/**
	 * @param mTemplateUrlName
	 *            the String to set
	 */
	public void setTemplateUrlName(final String pTemplateUrlName) {
		mTemplateUrlName = pTemplateUrlName;
	}

	/**
	 * Recipient name parameter name.
	 */
	private String mRecipientNameParamName;

	/**
	 * E-mail recipient parameter name.
	 */
	private String mRecipientEmailParamName;

	/**
	 * Sender parameter name.
	 */
	private String mSenderNameParamName;

	/**
	 * E-mail sender parameter name.
	 */
	private String mSenderEmailParamName;

	/**
	 * Gets the name of the Name value used for the To: field. This is
	 * configured in the component property file.
	 * 
	 * @return the name of the Name value used for the To: field.
	 */
	public String getRecipientNameParamName() {
		return mRecipientNameParamName;
	}

	/**
	 * Sets the name of the Name value used for the To: field. This is
	 * configured in the component property file.
	 * 
	 * @param pRecipientNameParamName
	 *            - the name of the Name value used for the To: field.
	 */
	public void setRecipientNameParamName(final String pRecipientNameParamName) {
		mRecipientNameParamName = pRecipientNameParamName;
	}

	/**
	 * Gets the name of the Email value used for the To: field. This is
	 * configured in the component property file.
	 * 
	 * @return the name of the Email value used for the To: field.
	 */
	public String getRecipientEmailParamName() {
		return mRecipientEmailParamName;
	}

	/**
	 * Sets the name of the Email value used for the To: field. This is
	 * configured in the component property file.
	 * 
	 * @param pRecipientEmailParamName
	 *            - the name of the Email value used for the To: field.
	 */
	public void setRecipientEmailParamName(final String pRecipientEmailParamName) {
		mRecipientEmailParamName = pRecipientEmailParamName;
	}

	/**
	 * Gets the name of the Name value used for the From: field. This is
	 * configured in the component property file.
	 * 
	 * @return the name of the Name value used for the from field.
	 */
	public String getSenderNameParamName() {
		return mSenderNameParamName;
	}

	/**
	 * Sets the name of the Name value used for the From: field. This is
	 * configured in the component property file.
	 * 
	 * @param pSenderNameParamName
	 *            - the name of the Name value used for the From: field.
	 */
	public void setSenderNameParamName(final String pSenderNameParamName) {
		mSenderNameParamName = pSenderNameParamName;
	}

	/**
	 * Gets the name of the Email value used for the From: field. This is
	 * configured in the component property file.
	 * 
	 * @return the name of the Email value used for the From: field.
	 */
	public String getSenderEmailParamName() {
		return mSenderEmailParamName;
	}

	/**
	 * Sets the name of the Email value used for the From: field. This is
	 * configured in the component property file.
	 * 
	 * @param pSenderEmailParamName
	 *            - the name of the Email value used for the From: field.
	 */
	public void setSenderEmailParamName(final String pSenderEmailParamName) {
		mSenderEmailParamName = pSenderEmailParamName;
	}

	/**
	 * Message parameter name.
	 */
	private String mMessageParamName;

	/**
	 * Gets the name of the parameter used for the Message: field. This is
	 * configured in the component property file.
	 * 
	 * @return the name of the parameter used for the Message: field.
	 */
	public String getMessageParamName() {
		return mMessageParamName;
	}

	/**
	 * Sets the name of the parameter used for the Message: field. This is
	 * configured in the component property file.
	 * 
	 * @param pMessageParamName
	 *            - the name of the parameter used for the Comment: field.
	 */
	public void setMessageParamName(final String pMessageParamName) {
		mMessageParamName = pMessageParamName;
	}

	/**
	 * SiteId parameter name.
	 */
	private String mSiteIdParamName;

	/**
	 * Gets the name of the parameter used for the SiteId: field. This is
	 * configured in the component property file.
	 * 
	 * @return the name of the parameter used for the SiteId: field.
	 */
	public String getSiteIdParamName() {
		return mSiteIdParamName;
	}

	/**
	 * Sets the name of the parameter used for the SiteId: field. This is
	 * configured in the component property file.
	 * 
	 * @param pSiteIdParamName
	 *            - the name of the parameter used for the Comment: field.
	 */
	public void setSiteIdParamName(final String pSiteIdParamName) {
		mSiteIdParamName = pSiteIdParamName;
	}

	/**
	 * @return the ProfileTools.
	 */
	public ProfileTools getProfileTools() {
		return getProfile().getProfileTools();
	}
	
	private BBBCatalogTools mCatalogTools;

	public BBBCatalogTools getCatalogTools() {
		return mCatalogTools;
	}

	public void setCatalogTools(BBBCatalogTools mCatalogTools) {
		this.mCatalogTools = mCatalogTools;
	}

	/**
	 * Profile.
	 */
	private Profile mProfile = null;

	/**
	 * Sets The user profile associated with the email. The default profile is
	 * used here. This is configured in the component property file.
	 * 
	 * @param pProfile
	 *            - the user profile of the logged in user.
	 */
	public void setProfile(Profile pProfile) {
		mProfile = pProfile;
	}

	/**
	 * Gets the user profile associated with the email. The default profile is
	 * used here. This is configured in the component property file.
	 * 
	 * @return the user profile of the logged in user.
	 */
	public Profile getProfile() {
		return mProfile;
	}

	// --------------------------------------------------
	// property: SubjectParamName
	private String mSubjectParamName;

	/**
	 * @return the String
	 */
	public String getSubjectParamName() {
		return mSubjectParamName;
	}

	/**
	 * @param SubjectParamName
	 *            the String to set
	 */
	public void setSubjectParamName(final String pSubjectParamName) {
		mSubjectParamName = pSubjectParamName;
	}

	// --------------------------------------------------
	// property: ActionResult
	private String mActionResult;

	/**
	 * @return the String
	 */
	public String getActionResult() {
		return mActionResult;
	}

	/**
	 * @param ActionResult
	 *            the String to set
	 */
	public void setActionResult(final String pActionResult) {
		mActionResult = pActionResult;
	}

	private BBBTemplateEmailSender mEmailSender;

	public BBBTemplateEmailSender getEmailSender() {
		return mEmailSender;
	}

	public void setEmailSender(BBBTemplateEmailSender mEmailSender) {
		this.mEmailSender = mEmailSender;
	}

	/**
	 * Handles the form submit and sends the email.
	 * 
	 * @param pRequest
	 *            - http request
	 * @param pResponse
	 *            - http response
	 * @return true on success, false - otherwise
	 * @throws java.lang.Exception
	 *             if error occurs
	 */
	public boolean handleSend(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException, IOException {
		
			logDebug("GenericEmailSenderFormHandler - [handleSend] = Entered in method \n");
		

		if ((!BBBUtility.isValidEmail(getSenderEmail()))
				&& (!BBBUtility.isValidEmail(getRecipientEmail()))) {
			
			addFormException(new DropletException(
					BBBGiftRegistryConstants.SENDER_RECEIPIENT_EMAIL_INVALID,BBBGiftRegistryConstants.SENDER_RECEIPIENT_EMAIL_INVALID));
			return checkFormRedirect(null, getErrorURL(), pRequest, pResponse);
		}

		if (!BBBUtility.isValidEmail(getSenderEmail())) {
			
			addFormException(new DropletException(BBBGiftRegistryConstants.SENDER_EMAIL_INVALID,BBBGiftRegistryConstants.SENDER_EMAIL_INVALID));
			return checkFormRedirect(null, getErrorURL(), pRequest, pResponse);
		}

		try {
			// send email
			BBBEmailHelper.sendEmail(collectParams(pRequest),
					getEmailSender(), getEmailInfo());

			// set action result message
			setActionResult(MSG_ACTION_SUCCESS);

		} catch (TemplateEmailException e) {
			processException(e, "emailFormHandler.send.TemplateEmailException",
					pRequest, pResponse);
		}

	
			logDebug("GenericEmailSenderFormHandler - [handleSend] = After sendEmail()");
		

		return true;
	}

	/**
	 * Collect parameters for e-mail templates
	 * 
	 * @return map of parameters
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map collectParams(DynamoHttpServletRequest pRequest) {
		// collect params from form handler to map and pass them into tools
		// class
		Map emailParams = new HashMap();
		String channel = pRequest.getHeader(BBBCoreConstants.CHANNEL);
		emailParams.put(getTemplateUrlName(), getTemplateUrl());
		emailParams.put(getSenderNameParamName(), getSenderName());
		emailParams.put(getSenderEmailParamName(), getSenderEmail());
		emailParams.put(getRecipientNameParamName(), getRecipientName());
		emailParams.put(getRecipientEmailParamName(), getRecipientEmail());
		emailParams.put(getMessageParamName(), getMessage());
		emailParams.put(getSubjectParamName(), getSubject());
		emailParams.put(getSiteIdParamName(), getSiteId());
		
		if(channel != null && (channel.equalsIgnoreCase(BBBCoreConstants.MOBILEWEB) || channel.equalsIgnoreCase(BBBCoreConstants.MOBILEAPP))){
			try {
				//set context path from properties file in case of Mobile Web and mobile App
				emailParams.put(getContextPath(), getStoreContextPath()); 
				List<String> configValue = getCatalogTools().getAllValuesForKey(BBBCoreConstants.MOBILEWEB_CONFIG_TYPE, BBBCoreConstants.REQUESTDOMAIN_CONFIGURE);
				if(configValue != null && configValue.size() > 0){
					 //set serverName from config key
					emailParams.put(getServerName(), configValue.get(0));
				}
			} catch (BBBSystemException e) {
				logError("BBBEmailSenderFormHandler.collectParams :: System Exception occured while fetching config value for config key " + BBBCoreConstants.REQUESTDOMAIN_CONFIGURE + "config type " + BBBCoreConstants.MOBILEWEB_CONFIG_TYPE + e);
			} catch (BBBBusinessException e) {
				logError("BBBEmailSenderFormHandler.collectParams :: Business Exception occured while fetching config value for config key " + BBBCoreConstants.REQUESTDOMAIN_CONFIGURE + "config type " + BBBCoreConstants.MOBILEWEB_CONFIG_TYPE + e);
			}
		}
		else{
			emailParams.put(getServerName(), pRequest.getServerName());
			emailParams.put(getContextPath(), pRequest.getContextPath());
		}
		return emailParams;
	}

	/**
	 * Add a user error message to the form exceptions.
	 * 
	 * @param pException
	 *            - exception to process
	 * @param pMsgId
	 *            - message id
	 * @param pRequest
	 *            - http request
	 * @param pResponse
	 *            - http response
	 */
	public void processException(Throwable pException, String pMsgId,
			DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) {
		String msg = "Some Process Exception";

		// If there is a message in the exception then add that
		if (pException != null) {
			String msg2 = pException.getLocalizedMessage();

			if (!StringUtils.isBlank(msg2)) {
				msg += (" " + msg2);
			}
		}

		DropletException de;

		if (pException == null) {
			de = new DropletException(msg,pMsgId);
		} else {
			de = new DropletException(msg, pException, pMsgId);
		}

		addFormException(de);

		
		logDebug(pException);
		
	}
}