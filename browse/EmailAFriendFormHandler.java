package com.bbb.browse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import nl.captcha.Captcha;
import atg.core.util.StringUtils;
import atg.droplet.DropletException;
import atg.json.JSONException;
import atg.json.JSONObject;
import atg.multisite.SiteContext;
import atg.multisite.SiteContextManager;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.userprofiling.email.TemplateEmailException;

import com.bbb.cms.manager.LblTxtTemplateManager;
import com.bbb.commerce.browse.manager.ProductManager;
import com.bbb.commerce.catalog.BBBCatalogErrorCodes;
import com.bbb.commerce.catalog.vo.ProductVO;
import com.bbb.constants.BBBCmsConstants;
import com.bbb.constants.BBBCoreConstants;
import com.bbb.constants.BBBCoreErrorConstants;
import com.bbb.constants.BBBGiftRegistryConstants;
import com.bbb.email.BBBEmailHelper;
import com.bbb.exception.BBBBusinessException;
import com.bbb.exception.BBBSystemException;
import com.bbb.logging.LogMessageFormatter;
import com.bbb.profile.session.BBBSessionBean;
import com.bbb.utils.BBBUtility;


// Email from whom?
/**
 * Form handler for sending email from the ATG Store website. <br/> The JSP form
 * that accepts the email can directly set the From, Subject and To fields or
 * use the defaults as named in the configuration of the DefaultEmailInfo. <br/>
 * When the form submits the parameters the template is used to format the email
 * and then the EmailSender sends the email. The names for the From, Subject, To
 * and Profile parameters that are sent to the Email Template are set in the
 * configuration and must match the email template. <br/>
 */
public class EmailAFriendFormHandler extends BBBEmailSenderFormHandler {

	private static final String EMAIL_A_FRIEND_PDP = "EmailAFriendPDP";
	private static final String EMAIL_SENDING_EXCEPTION = "err_email_sending_exception";
    private static final String RESPONSE_TYPE_JSON = "application/json"; //$NON-NLS-1$
	private static final String SCHEME_APPEND = "://"; //$NON-NLS-1$

	private boolean validateCaptcha;
	private boolean mEmailForSender;

	private LblTxtTemplateManager lblTxtTemplateManager;
	
	//MessageCC to sender flag
	private boolean mCcFlag;

	/**
	 * Product id parameter name.
	 */
	private String mProductIdParamName;
	private String captchaAnswer;
	private String mEmailType;
	private String mCurrentPageURL;
	private String mCurrentSite;
	private String mTemplateUrl;
	private SiteContext mSiteContext;
	private String mCurrentCatalogURL;
	private String mTempEmail;
	private String siteId = null;
	private Map<String,String> errorUrlMap;
	private String fromPage;// Page Name set from JSP
	private Map<String,String> successUrlMap;
	private String queryParam;// queryParam set from jsp
	private ProductManager productManager;
	
	public ProductManager getProductManager() {
		return productManager;
	}

	public void setProductManager(ProductManager productManager) {
		this.productManager = productManager;
	}

	/**
	 * @return the queryParam
	 */
	public String getQueryParam() {
		return queryParam;
	}

	/**
	 * @param queryParam the queryParam to set
	 */
	public void setQueryParam(String queryParam) {
		this.queryParam = queryParam;
	}

	/**
	 * @return the errorUrlMap
	 */
	public Map<String, String> getErrorUrlMap() {
		return errorUrlMap;
	}

	/**
	 * @param errorUrlMap the errorUrlMap to set
	 */
	public void setErrorUrlMap(Map<String, String> errorUrlMap) {
		this.errorUrlMap = errorUrlMap;
	}

	/**
	 * @return the fromPage
	 */
	public String getFromPage() {
		return fromPage;
	}

	/**
	 * @param fromPage the fromPage to set
	 */
	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}

	/**
	 * @return the successUrlMap
	 */
	public Map<String, String> getSuccessUrlMap() {
		return successUrlMap;
	}

	/**
	 * @param successUrlMap the successUrlMap to set
	 */
	public void setSuccessUrlMap(Map<String, String> successUrlMap) {
		this.successUrlMap = successUrlMap;
	}

	/**
	 * @return Catalog URL
	 */
	public final String getCurrentCatalogURL() {
		return this.mCurrentCatalogURL;
	}

	/**
	 * @param mCurrentCatalogURL
	 */
	public final void setCurrentCatalogURL(final String mCurrentCatalogURL) {
		this.mCurrentCatalogURL = mCurrentCatalogURL;
	}

	/**
	 * @return the mTempEmail
	 */
	public final String getTempEmail() {
		return this.mTempEmail;
	}
	
	/**
	 * @param pTempEmail the pTempEmail to set
	 */
	public final void setTempEmail(final String pTempEmail) {
		this.mTempEmail = pTempEmail;
	}

	/**
	 * @return the emailForSender
	 */
	public final boolean isEmailForSender() {
		return this.mEmailForSender;
	}
	
	/**
	 * @param pEmailForSender the mEmailForSender to set
	 */
	public final void setEmailForSender(final boolean pEmailForSender) {
		this.mEmailForSender = pEmailForSender;
	}

	/**
	 * @return the siteContext
	 */
	public final SiteContext getSiteContext() {
		return this.mSiteContext;
	}

	/**
	 * @param pSiteContext the siteContext to set
	 */
	public final void setSiteContext(final SiteContext pSiteContext) {
		this.mSiteContext = pSiteContext;
	}


	/**
	 * @return the templateUrl
	 */
	public final String getTemplateUrl() {
		return this.mTemplateUrl;
	}


	/**
	 * @param pTemplateUrl the templateUrl to set
	 */
	public final void setTemplateUrl(final String pTemplateUrl) {
		this.mTemplateUrl = pTemplateUrl;
	}

	/**
	 * @return the mCurrentSite
	 */
	public final String getCurrentSite() {
		return this.mCurrentSite;
	}

	/**
	 * @param pCurrentSite the pCurrentSite to set
	 */
	public final void setCurrentSite(final String pCurrentSite) {
		this.mCurrentSite = pCurrentSite;
	}

	/**
	 * @return the mCurrentPageURL
	 */
	public final String getCurrentPageURL() {
		return this.mCurrentPageURL;
	}

	/**
	 * @param pCurrentPageURL the pCurrentPageURL to set
	 */
	public final void setCurrentPageURL(final String pCurrentPageURL) {
		this.mCurrentPageURL = pCurrentPageURL;
	}

	/**
	 * @return the mEmailType
	 */
	public final String getEmailType() {
		return this.mEmailType;
	}

	/**
	 * @param pEmailType the mEmailType to set
	 */
	public final void setEmailType(final String pEmailType) {
		this.mEmailType = pEmailType;
	}


	/**
	 * Subject parameter name.
	 */
	private String mSubjectParamName;

	/**
	 * Locale parameter name - it represents the name of locale parameter to be
	 * used in Email template.
	 */
	private String mLocaleParamName = "locale"; //$NON-NLS-1$



	/**
	 * Gets the name of the parameter used for the ProductId: field. This is
	 * configured in the component property file.
	 * 
	 * @return the name of the parameter used for the ProductId: field.
	 */
	public final String getProductIdParamName() {
		return this.mProductIdParamName;
	}

	/**
	 * Sets the name of the parameter used for the ProductId: field. This is
	 * configured in the component property file.
	 * 
	 * @param pProductIdParamName -
	 *          the name of the parameter used for the ProductId: field.
	 */
	public final void setProductIdParamName(final String pProductIdParamName) {
		this.mProductIdParamName = pProductIdParamName;
	}

	/**
	 * Gets the name of the parameter used for the Subject: field. This is
	 * configured in the component property file.
	 * 
	 * @return the name of the parameter used for the Subject: field.
	 */
	public final String getSubjectParamName() {
		return this.mSubjectParamName;
	}

	/**
	 * Sets the name of the parameter used for the Subject: field. This is
	 * configured in the component property file.
	 * 
	 * @param pSubjectParamName -
	 *          the name of the parameter used for the Subject: field.
	 */
	public final void setSubjectParamName(final String pSubjectParamName) {
		this.mSubjectParamName = pSubjectParamName;
	}

	/**
	 * @param pLocaleParamName -
	 *          locale parameter name.
	 */
	public final void setLocaleParamName(final String pLocaleParamName) {
		this.mLocaleParamName = pLocaleParamName;
	}

	/**
	 * @return the value of property getEmailParamName.
	 */
	public final String getLocaleParamName() {
		return this.mLocaleParamName;
	}

	/* (non-Javadoc)
	 * @see com.bbb.browse.BBBEmailSenderFormHandler#collectParams(atg.servlet.DynamoHttpServletRequest)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected final Map collectParams(final DynamoHttpServletRequest pRequest) {
		// collect params from form handler to map and pass them into tools class
		final Map emailParams = super.collectParams(pRequest);
		final HashMap placeHolderValues = new HashMap();
		final Calendar currentDate = Calendar.getInstance();				
		final long uniqueKeyDate = currentDate.getTimeInMillis();
		final String emailPersistId = getProfile().getRepositoryId() + uniqueKeyDate;				
		
		placeHolderValues.put(BBBGiftRegistryConstants.WEBSITE_NAME, getCurrentSite());
	    String pageUrl=	getCurrentPageURL();
		if(pageUrl.contains("?"))
		{
		pageUrl=	pageUrl.concat("&");
		}
		else
		{
			pageUrl=	pageUrl.concat("?");
		}
		placeHolderValues.put(BBBGiftRegistryConstants.PAGE_URL, pageUrl);
		placeHolderValues.put(BBBGiftRegistryConstants.PAGE_URL_TEXT, BBBGiftRegistryConstants.PRODUCT_DETAIL_PAGE);
		placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_SENDERS_EMAIL, getSenderEmail());

		if (null == this.siteId) {
		    emailParams.put(BBBCoreConstants.SITE_ID, getSiteContext().getSite().getId());
		} else {
		    emailParams.put(BBBCoreConstants.SITE_ID, this.siteId);
		}

		placeHolderValues.put(BBBGiftRegistryConstants.EMAIL_TYPE, getEmailType());
		
		//Below fields are required by EmailTemplateDroplet
		if (!BBBUtility.isEmpty(getCurrentCatalogURL())) {
			try {
					 pageUrl  =	URLDecoder.decode(getCurrentCatalogURL(), "UTF-8");
					if(pageUrl.contains("?"))
					{
					pageUrl=	pageUrl.concat("&");
					}
					else
					{
						pageUrl=	pageUrl.concat("?");
					}
					placeHolderValues.put(BBBCmsConstants.FRM_DATA_PAGE_TITLE, pageUrl);
			} catch (UnsupportedEncodingException e) {
				
					logError("Unsupported Encoding Exception" + e); //$NON-NLS-1$
				
			}	
		}
		
		if (null == this.siteId) {
		    placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_SITEID, getSiteContext().getSite().getId());
		} else {
		    placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_SITEID, this.siteId);
		}
		placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_COMMENT_MESSAGE, getMessage());
		
		//CM-3077  - Check condition for sending different email content for Sender and Recipient 
		if (isEmailForSender()) {
			final Map<String, String> placeHolderMap = new HashMap<String, String>();
			placeHolderMap.put(BBBCoreConstants.RECIPIENT_EMAIL, getTempEmail());
			final String senderMessageHeader = getLblTxtTemplateManager().getPageTextArea(
			        "txt_sender_email_message_header", pRequest.getLocale().getLanguage(), 
			        placeHolderMap, null); //$NON-NLS-1$
			placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_SENDER_EMAIL_MESSAGE_HEADER, senderMessageHeader);
		} else {
			placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_SENDER_EMAIL_MESSAGE_HEADER, BBBCoreConstants.BLANK);
		}
		placeHolderValues.put(BBBCoreConstants.EMAIL_PERSIST_ID, emailPersistId);		

		if (getEmailType().equalsIgnoreCase(EmailAFriendFormHandler.EMAIL_A_FRIEND_PDP))
		{
			String pSiteId = this.siteId;
			String productId = pRequest.getParameter(BBBCoreConstants.PRODUCTID);
			if (null == this.siteId) {
				pSiteId = getSiteContext().getSite().getId();
			}
			ProductVO productVO=null;
			try {
				
				List<String> requestDomainName = getCatalogTools().getAllValuesForKey(BBBCoreConstants.MOBILEWEB_CONFIG_TYPE,BBBCoreConstants.REQUESTDOMAIN_CONFIGURE);
				productVO = getProductManager().getMainProductDetails(pSiteId,productId);
				
				String longDescription=productVO.getLongDescription();
				String productInfoThreePoint;
				int length = longDescription.length();
				int index = length;
				if (length >= 352){
					String subString = longDescription.substring(342, 352).toUpperCase();
					if (subString.indexOf("</LI>") != -1){
						index = longDescription.indexOf("</LI>", 342);
					} else if(subString.indexOf("<UL>") != -1){
						index = longDescription.indexOf("<UL>", 342);
					} else {
						index = 347;
					}
					productInfoThreePoint =longDescription.substring(4, index).concat("...");
				} else{
					productInfoThreePoint =longDescription.substring(4, index);
				}
				placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_PRODUCT_TITLE, productVO.getName());
				placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_PRODUCT_IMAGE, productVO.getProductImages().getLargeImage());
				placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_PRODUCT_INFO, productInfoThreePoint);
				placeHolderValues.put(BBBGiftRegistryConstants.FRMDATA_REQUEST_DOMAIN, requestDomainName.get(0));
				
				
			}
			catch (BBBBusinessException bbbbEx) {
				if(BBBCatalogErrorCodes.PRODUCT_IS_DISABLED_NO_LONGER_AVAILABLE_REPOSITORY.equalsIgnoreCase(bbbbEx.getErrorCode()) || BBBCatalogErrorCodes.PRODUCT_NOT_AVAILABLE_IN_REPOSITORY.equalsIgnoreCase(bbbbEx.getErrorCode())) {
					logError(LogMessageFormatter.formatMessage(pRequest, "Business Exception while getting product detail for productId=" +productId +" |SiteId="+pSiteId,BBBCoreErrorConstants.BROWSE_ERROR_1032));
				} else {
					logError(LogMessageFormatter.formatMessage(pRequest, "Business Exception while getting product detail for productId=" +productId +" |SiteId="+pSiteId,BBBCoreErrorConstants.BROWSE_ERROR_1032),bbbbEx);
				}
			} catch (BBBSystemException bbbsEx) {
				if(BBBCatalogErrorCodes.PRODUCT_IS_DISABLED_NO_LONGER_AVAILABLE_REPOSITORY.equalsIgnoreCase(bbbsEx.getErrorCode()) || BBBCatalogErrorCodes.PRODUCT_NOT_AVAILABLE_IN_REPOSITORY.equalsIgnoreCase(bbbsEx.getErrorCode())) {
					logError(LogMessageFormatter.formatMessage(pRequest, "System Exception while getting product detail for productI=" +productId +" |SiteId="+pSiteId,BBBCoreErrorConstants.BROWSE_ERROR_1032));
				} else {
					logError(LogMessageFormatter.formatMessage(pRequest, "System Exception while getting product detail for productI=" +productId +" |SiteId="+pSiteId,BBBCoreErrorConstants.BROWSE_ERROR_1033),bbbsEx);
				}
			}
		}
	    
		emailParams.put(BBBGiftRegistryConstants.PLACE_HOLDER_VALUES, placeHolderValues);
		if (getSiteContext() != null
		        && getSiteContext().getSite() != null 
		        && !(getSiteContext().getSite().getId().isEmpty())) {
		    this.siteId = getSiteContext().getSite().getId();
		}

		return emailParams;
	}
	
	/**Handle method to send request to email a friend for REST API.
	 * @param pRequest
	 * @param pResponse
	 * @return success/failure of the operation
	 * @throws ServletException
	 * @throws IOException
	 */
	public final boolean handleEmailAFriendRequest(final DynamoHttpServletRequest pRequest,
			final DynamoHttpServletResponse pResponse) throws ServletException, IOException {
		setSuccessURL(""); //$NON-NLS-1$
		setErrorURL(""); //$NON-NLS-1$

		boolean isError = false;

		setTemplateUrl(getTemplateUrl());
		
		if (this.getCurrentPageURL() == null || "".equals(this.getCurrentPageURL())) { //$NON-NLS-1$
			
			addFormException(new DropletException(getLblTxtTemplateManager()
					.getErrMsg(BBBGiftRegistryConstants.CURRENT_PAGE_URL_INVALID,
							pRequest.getLocale().getLanguage(), null, null),
							BBBGiftRegistryConstants.CURRENT_PAGE_URL_INVALID));
			isError = true;
			
		} else {
			setCurrentPageURL(StringUtils.escapeHtmlString(this.getCurrentPageURL()));
			final java.net.URL url =  new java.net.URL(this.getCurrentPageURL());
			setCurrentSite(url.getProtocol() + SCHEME_APPEND + url.getHost());
			setMessage(StringUtils.escapeHtmlString(this.getMessage()));
		}
		
		String[] emails = null;
		if (BBBUtility.isEmpty(getRecipientEmail())) {
			addFormException(new DropletException(BBBGiftRegistryConstants.RECIPIENT_EMAIL_INVALID_MSG,
					BBBGiftRegistryConstants.RECIPIENT_EMAIL_INVALID));
			isError = true;
			
		} else {
			emails = getRecipientEmail().split(BBBCoreConstants.SEMICOLON);
			for (final String email : emails) {
				if (!BBBUtility.isValidEmail(email)) {
					addFormException(new DropletException(BBBGiftRegistryConstants.RECIPIENT_EMAIL_INVALID_MSG,
			                BBBGiftRegistryConstants.RECIPIENT_EMAIL_INVALID));
					isError = true;
					break;
				}
			}
			

		}
			
		if (!BBBUtility.isValidEmail(getSenderEmail())) {

			addFormException(new DropletException(BBBGiftRegistryConstants.SENDER_EMAIL_INVALID_MSG,
			        BBBGiftRegistryConstants.SENDER_EMAIL_INVALID));
			isError = true;
		}
		if (!isError) {
				try {
					invokeSendEmail(pRequest);

					final Map<String, String> placeHolderMap = new HashMap<String, String>();
					placeHolderMap.put(BBBCoreConstants.RECIPIENT_EMAIL,
							getRecipientEmail());
				} catch (TemplateEmailException e) {

					addFormException(new DropletException(
							BBBGiftRegistryConstants.EMAIL_SEND_FAIL_MSG, BBBGiftRegistryConstants.EMAIL_SEND_FAIL));
				}
//Fix added for defect BSL-4435 | MOB- Sender is not receiving an email for "Email Product to a friend" from PDP

			}
		if (isCcFlag()) {
			setEmailForSender(true);
			setTempEmail(getRecipientEmail());
			setRecipientEmail(getSenderEmail());

			final boolean isSenderEmailSuccess = invokeHandleSend(pRequest, pResponse);
			if (!isSenderEmailSuccess && isLoggingError()) {
				logError("EmailAFriendFormHandler::Error sending email to sender"
						+ getSenderEmail()); //$NON-NLS-1$						
			}
		}
		return true;
	}

	protected boolean invokeHandleSend(final DynamoHttpServletRequest pRequest,
			final DynamoHttpServletResponse pResponse) throws ServletException, IOException {
		return super.handleSend(
				pRequest, pResponse);
	}

	protected void invokeSendEmail(final DynamoHttpServletRequest pRequest) throws TemplateEmailException {
		BBBEmailHelper.sendEmail(collectParams(pRequest),
				getEmailSender(), getEmailInfo());
	}


	/**
	 * This method is overridden to validate the Captcha entered by the user.
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @return boolean
	 */
	@Override
	public final boolean handleSend(final DynamoHttpServletRequest pRequest,
			final DynamoHttpServletResponse pResponse) throws ServletException, IOException {
		String siteId = getSiteIdFromContextManager();
		if ( StringUtils.isNotEmpty(getFromPage())) {
			StringBuffer appendData = new StringBuffer(BBBCoreConstants.BLANK);
			if(StringUtils.isNotEmpty(getQueryParam())){
				appendData.append(BBBCoreConstants.QUESTION_MARK).append(getQueryParam());
			}
			StringBuffer successURL = new StringBuffer(BBBCoreConstants.BLANK);
			StringBuffer errorURL = new StringBuffer(BBBCoreConstants.BLANK);
			successURL
					.append(pRequest.getContextPath())
					.append(getSuccessUrlMap().get(getFromPage()))
					.append(appendData);
			errorURL.append(pRequest.getContextPath())
					.append(getErrorUrlMap().get(getFromPage()))
					.append(appendData);

			setSuccessURL(successURL.toString());
			setErrorURL(errorURL.toString());
		}
		final JSONObject responseJson = new JSONObject();
		pResponse.setContentType(RESPONSE_TYPE_JSON);
		try {
			siteId = getSiteIdFromContextManager();
			if ((null != siteId && siteId.contains(BBBCoreConstants.TBS)) || validatingCaptcha(pRequest, pResponse)) {
				setTemplateUrl(getTemplateUrl());
				//setCurrentCatalogURL(getCurrentCatalogURL());
				setMessage(StringUtils.escapeHtmlString(this.getMessage()));
				setCurrentPageURL(pRequest.getHeader(BBBCoreConstants.REFERRER)); 
				setCurrentSite(pRequest.getScheme() + SCHEME_APPEND + pRequest.getServerName());
				final boolean isRecipientEmailSuccess = invokeHandleSend(pRequest, pResponse);
				
				if (isRecipientEmailSuccess) {
					final Map<String, String> placeHolderMap = new HashMap<String, String>();
					placeHolderMap.put(BBBCoreConstants.RECIPIENT_EMAIL,
							getRecipientEmail());
					populateSuccessInfoJSON(pRequest, responseJson, placeHolderMap);
					//Following code is to send email to Sender as well.
					// For BBSL-3817, Added this condition, so that when check
					// box is checked then only email goes.
					if (isCcFlag()) {
						setEmailForSender(true);
						setTempEmail(getRecipientEmail());
						setRecipientEmail(getSenderEmail());

						final boolean isSenderEmailSuccess = invokeHandleSend(pRequest, pResponse);
						if (!isSenderEmailSuccess && isLoggingError()) {
							logError("EmailAFriendFormHandler::Error sending email to sender"
									+ getSenderEmail()); //$NON-NLS-1$						
						}
					}

					
				} else {
					responseJson.put(BBBGiftRegistryConstants.ERROR, BBBGiftRegistryConstants.SERVER_ERROR); 
					responseJson.put(BBBGiftRegistryConstants.ERROR_MESSAGES,
							getLblTxtTemplateManager().getErrMsg("err_email_internal_error", 
							        pRequest.getLocale().getLanguage(), null, null));

					final Map<String, String> placeHolderMap = new HashMap<String, String>();
					placeHolderMap.put(BBBCoreConstants.RECIPIENT_EMAIL, getRecipientEmail());
					populateSuccessInfoJSON(pRequest, responseJson, placeHolderMap);
					
					//Following code is to send email to Sender as well.
					setEmailForSender(true);
					setTempEmail(getRecipientEmail());
					setRecipientEmail(getSenderEmail());
					
					final boolean isSenderEmailSuccess = invokeHandleSend(pRequest, pResponse);
					if (!isSenderEmailSuccess && isLoggingError()) { 
						logError("EmailAFriendFormHandler::Error sending email to sender" + getSenderEmail()); 
					}
				}
			} else {
				// when CAPTCHA is incorrect
				responseJson.put(BBBGiftRegistryConstants.ERROR, BBBGiftRegistryConstants.GENERAL_ERROR);
				responseJson.put(BBBGiftRegistryConstants.ERROR_MESSAGES, getLblTxtTemplateManager().getErrMsg(
								"err_email_incorrect_captcha", pRequest.getLocale().getLanguage(), null, null));
			}
			final PrintWriter out = pResponse.getWriter();
			out.print(responseJson.toString());
			out.flush();
			out.close();

		} catch (JSONException e) {
			
				logError("JSONException" + e); //$NON-NLS-1$
			
			addFormException(new DropletException(getLblTxtTemplateManager().getErrMsg(EMAIL_SENDING_EXCEPTION,
							pRequest.getLocale().getLanguage(), null, null), EMAIL_SENDING_EXCEPTION));
		}

		return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest, pResponse);
	}

	protected JSONObject populateSuccessInfoJSON(final DynamoHttpServletRequest pRequest, final JSONObject responseJson,
			final Map<String, String> placeHolderMap) throws JSONException {
		return responseJson.put(
				BBBGiftRegistryConstants.SUCCESS,
				getLblTxtTemplateManager().getPageTextArea(
						"txt_email_sent_msg", //$NON-NLS-1$
						pRequest.getLocale().getLanguage(),
						placeHolderMap, null));
	}

	protected String getSiteIdFromContextManager() {
		return SiteContextManager.getCurrentSiteId();
	}

	/**
	 * This Method validates the value of the captcha entered by the user and sends back the appropriate response.
	 * if captcha check needs to be disabled then turn validateCaptcha falg to false
	 * @param pRequest
	 * @param pResponse
	 * @return success / failure of the operation
	 * @throws ServletException
	 * @throws IOException
	 */
	public boolean validatingCaptcha(final DynamoHttpServletRequest pRequest,
			final DynamoHttpServletResponse pResponse) throws ServletException, IOException{
		if (this.validateCaptcha){
			boolean success = true;
			final BBBSessionBean sessionBean 
			    = (BBBSessionBean) pRequest.resolveName(BBBGiftRegistryConstants.SESSION_BEAN);
			final Captcha captcha = sessionBean.getCaptcha();
			pRequest.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			logDebug("SessionId = "+pRequest.getSession().getId() + "\nCaptcha in Session = " + captcha.toString() 
			        + "\nCaptcha User entered = " + getCaptchaAnswer());
			//if CAPTCHA validation is enabled 
			if (isValidateCaptcha() && !captcha.isCorrect(getCaptchaAnswer())) {
				success = false;
			}
			logDebug("Captcha Matched = " + success); //$NON-NLS-1$
			return success;
		}
		logDebug("Captcha check is turned off  "); //$NON-NLS-1$
		return true;
	}

	/**
	 * @return the URL of the success page.
	 */
	@Override
	public final String getSuccessURL() {
		return super.getSuccessURL() + "&recipientEmail=" //$NON-NLS-1$
				+ getRecipientEmail().trim();
	}

	/**
	 * @return the captchaAnswer
	 */
	public final String getCaptchaAnswer() {
		return this.captchaAnswer;
	}

	/**
	 * @param captchaAnswer the captchaAnswer to set
	 */
	public final void setCaptchaAnswer(final String captchaAnswer) {
		this.captchaAnswer = captchaAnswer;
	}

	/**
	 * @return the validateCaptcha
	 */
	public final boolean isValidateCaptcha() {
		return this.validateCaptcha;
	}

	/**
	 * @param validateCaptcha the validateCaptcha to set
	 */
	public final void setValidateCaptcha(final boolean validateCaptcha) {
		this.validateCaptcha = validateCaptcha;
	}

	/**
	 * @return the lblTxtTemplateManager
	 */
	public final LblTxtTemplateManager getLblTxtTemplateManager() {
		return this.lblTxtTemplateManager;
	}

	/**
	 * @param lblTxtTemplateManager the lblTxtTemplateManager to set
	 */
	public final void setLblTxtTemplateManager(final LblTxtTemplateManager lblTxtTemplateManager) {
		this.lblTxtTemplateManager = lblTxtTemplateManager;
	}
	
	public boolean isCcFlag() {
		return mCcFlag;
	}

	public void setCcFlag(boolean mCcFlag) {
		this.mCcFlag = mCcFlag;
	}
	


}
