package com.bbb.browse;
// new one: File
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import nl.captcha.Captcha;
import atg.droplet.DropletException;
import atg.multisite.SiteContext;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

import com.bbb.commerce.browse.manager.ActivateCouponManager;
import com.bbb.commerce.catalog.BBBCatalogTools;
import com.bbb.account.validatecoupon.ActivateCouponResponseVO;
import com.bbb.cms.manager.LblTxtTemplateManager;
import com.bbb.common.BBBGenericFormHandler;
import com.bbb.constants.BBBCoreConstants;
import com.bbb.constants.BBBCoreErrorConstants;
import com.bbb.constants.BBBGiftRegistryConstants;
//import com.bbb.constants.BBBWebServiceConstants;
//import com.bbb.exception.BBBBusinessException;
//import com.bbb.exception.BBBSystemException;
import com.bbb.framework.validation.BBBValidationRules;
import com.bbb.logging.LogMessageFormatter;
import com.bbb.profile.session.BBBSessionBean;
import com.bbb.account.validatecoupon.ActivateCouponRequestVO;
import com.bbb.utils.BBBUtility;

public class ActivateBigBlueFormHandler extends BBBGenericFormHandler {

	public static final String RESPONSE_TYPE_JSON = "application/json";
	public static final String URL = "url";
	private SiteContext mSiteContext;
	private boolean mSuccessMessage = false;
	private boolean mContextAdded = false;
	private String errorMessage=null;
	private String captchaAnswer;
	private boolean validateCaptcha;		
	private String mCurrentPageURL;	
	private String mCurrentSite;
	private String mTemplateUrl;
	private Map<String, String> mErrorMap;
	private LblTxtTemplateManager lblTxtTemplateManager;
	//private static final String SCHEME_APPEND ="://";	
	private ActivateCouponRequestVO activateBigBlueVO;
	BBBValidationRules rules;
	ActivateCouponManager activateCouponManager;
	ActivateCouponResponseVO activateCouponRespVO;
	private BBBCatalogTools mBbbCatalogTools;
	private String emailAddress;
	private String expiryDate;
	private String successURL;
	public String getSuccessURL() {
		return successURL;
	}

	private String errorURL;
	public String getErrorURL() {
		return errorURL;
	}

	public void setErrorURL(String errorURL) {
		this.errorURL = errorURL;
	}

	public void setSuccessURL(String successURL) {
		this.successURL = successURL;
	}

	
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}


	
	
	public Map<String, String> getmErrorMap() {
		return mErrorMap;
	}

	public void setmErrorMap(Map<String, String> mErrorMap) {
		this.mErrorMap = mErrorMap;
	}
	public ActivateCouponManager getActivateCouponManager() {
		return activateCouponManager;
	}

	public void setActivateCouponManager(ActivateCouponManager activateCouponManager) {
		this.activateCouponManager = activateCouponManager;
	}


	public BBBValidationRules getRules() {
		return rules;
	}

	public void setRules(BBBValidationRules rules) {
		this.rules = rules;
	}
	
	/**
	 * @return the siteContext
	 */
	public SiteContext getSiteContext() {
		return mSiteContext;
	}

	/**
	 * @param pSiteContext the siteContext to set
	 */
	public void setSiteContext(SiteContext pSiteContext) {
		mSiteContext = pSiteContext;
	}

	
	/**
	 * @return the mCurrentSite
	 */
	public String getCurrentSite() {
		return mCurrentSite;
	}

	/**
	 * @param mCurrentSite the mCurrentSite to set
	 */
	public void setCurrentSite(String pCurrentSite) {
		this.mCurrentSite = pCurrentSite;
	}

	/**
	 * @return the mCurrentPageURL
	 */
	public String getCurrentPageURL() {
		return mCurrentPageURL;
	}

	/**
	 * @param mCurrentPageURL the mCurrentPageURL to set
	 */
	public void setCurrentPageURL(String pCurrentPageURL) {
		this.mCurrentPageURL = pCurrentPageURL;
	}	
	
	/**
	 * @return the successMessage
	 */
	public boolean isSuccessMessage() {
		return mSuccessMessage;
	}

	/**
	 * @param pSuccessMessage
	 *            the successMessage to set
	 */
	public void setSuccessMessage(boolean pSuccessMessage) {
		mSuccessMessage = pSuccessMessage;
	}
	
	public LblTxtTemplateManager getLblTxtTemplateManager() {
		return lblTxtTemplateManager;
	}

	public void setLblTxtTemplateManager(LblTxtTemplateManager lblTxtTemplateManager) {
		this.lblTxtTemplateManager = lblTxtTemplateManager;
	}
	
	public ActivateCouponRequestVO getActivateBigBlueVO() {
		
		if ( this.activateBigBlueVO == null )
		{
			this.activateBigBlueVO = new ActivateCouponRequestVO();
		}
		return this.activateBigBlueVO;
	}

	public void setActivateBigBlueVO(ActivateCouponRequestVO activateBigBlueVO) {
		
		this.activateBigBlueVO = activateBigBlueVO;
	}

	/**
	 * This Method validates the value of the captcha entered by the user and sends back the appropriate response.
	 * @param pRequest
	 * @param pResponse
	 * @return
	 */
	public void validateCaptcha(final DynamoHttpServletRequest pRequest, final DynamoHttpServletResponse pResponse, Map<String, String> mErrorMap) throws ServletException, IOException {

		logDebug("ActivateBigBlueFormHandler.validateCaptcha() method started");
//		Captcha captcha = (Captcha) pRequest.getSession().getAttribute(Captcha.NAME);
		BBBSessionBean sessionBean = (BBBSessionBean) pRequest.resolveName(BBBGiftRegistryConstants.SESSION_BEAN);
		Captcha captcha = sessionBean.getCaptcha();
		pRequest.setCharacterEncoding("UTF-8");
		logDebug("SessionId = "+pRequest.getSession().getId());
		logDebug("Captcha in Session = "+captcha.toString());
		logDebug("Captcha User entered = "+getCaptchaAnswer());
		//if captcha validation is enabled 
		if (isValidateCaptcha() && !captcha.isCorrect(getCaptchaAnswer())) {
			addFormException(new DropletException("Captcha Error: Please enter right captcha text."));
			mErrorMap.put(BBBCoreConstants.REGISTER_ERROR,errorMessage);
		}
		logDebug("ActivateBigBlueFormHandler.validateCaptcha() method started");
	}
	
	/** Validate Email address entered by the users
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @param mErrorMap
	 */
	private void validateEmail(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse, Map<String, String> mErrorMap) {

		logDebug("ActivateBigBlueFormHandler.validateEmail() method started");
		if (BBBUtility.isEmpty(getActivateBigBlueVO().getEmailAddr())) {
			addFormException(new DropletException("Email Address Error:Email address is a required field."));
			mErrorMap.put(BBBCoreConstants.REGISTER_ERROR,errorMessage);
		}		
		if (!BBBUtility.isEmpty(getActivateBigBlueVO().getEmailAddr()) && !BBBUtility.isValidEmail(getActivateBigBlueVO().getEmailAddr())) {
			addFormException(new DropletException("Email Address Error:Please enter the email address in right format"));
			mErrorMap.put(BBBCoreConstants.REGISTER_ERROR,errorMessage);
			logError(LogMessageFormatter.formatMessage(pRequest, "Invalid email from validateEmail of ActivateBigBlueFormHandler", BBBCoreErrorConstants.GIFTREGISTRY_ERROR_1019));
		}
		setEmailAddress(getActivateBigBlueVO().getEmailAddr());
		logDebug("ActivateBigBlueFormHandler.validateEmail() method ends");
	}
	
	/**
	 * Validate Activate Big Blue form - Empty field check for
	 * Offer Code/Email/Mobile Number
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @param mErrorMap
	 */
	private void validateFields(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse, Map<String, String> mErrorMap) {
		logDebug("ActivateBigBlueFormHandler.emptyChkValidation() method started");
		if(BBBUtility.isEmpty(getActivateBigBlueVO().getOfferCd())){
			addFormException(new DropletException("Offer Numer Error: Offer Number is a required field."));
			mErrorMap.put(BBBCoreConstants.REGISTER_ERROR,errorMessage);
				logError(LogMessageFormatter.formatMessage(pRequest, "Invalid Offer Code from validateFields of ActivateBigBlueFormHandler", BBBCoreErrorConstants.GIFTREGISTRY_ERROR_1017));		
		}
		if(!BBBUtility.isEmpty(getActivateBigBlueVO().getOfferCd()) && !isValidOfferCode(getActivateBigBlueVO().getOfferCd())) {
			addFormException(new DropletException("Offer Numer Error: Please enter 6 digit offer number."));
			mErrorMap.put(BBBCoreConstants.REGISTER_ERROR,errorMessage);
				logError(LogMessageFormatter.formatMessage(pRequest, "Invalid Offer Code from validateFields of ActivateBigBlueFormHandler", BBBCoreErrorConstants.GIFTREGISTRY_ERROR_1017));		
		}
		
		if (!BBBUtility.isEmpty(getActivateBigBlueVO().getMobilePhone())&& !BBBUtility.isValidPhoneNumber(getActivateBigBlueVO().getMobilePhone())) {
			addFormException(new DropletException("Phone Numer Error: Please enter 10 digit phone number."));
			mErrorMap.put(BBBCoreConstants.REGISTER_ERROR,errorMessage);
				logError(LogMessageFormatter.formatMessage(pRequest, "Invalid MObile Phone Number from validateFields of ActivateBigBlueFormHandler", BBBCoreErrorConstants.GIFTREGISTRY_ERROR_1016));
		}		
		logDebug("ActivateBigBlueFormHandler.emptyChkValidation() method ends");
	}
	
	/** Validate Offer Code entered by the user
	 * 
	 * @param sOfferCd
	 * @return
	 */
	private boolean isValidOfferCode(String sOfferCd)
	{
		boolean validateStatus = true;
		

			if (!BBBUtility.isStringPatternValid(
					rules.getAlphaNumericPattern(), sOfferCd)) {
				validateStatus = false;
			} else {
				if (!BBBUtility.isStringLengthValid(sOfferCd, 1, 6)) {
					validateStatus = false;
				}
			}

		return validateStatus;
	}
	

	/**
	 * @return the captchaAnswer
	 */
	public String getCaptchaAnswer() {
		return captchaAnswer;
	}

	/**
	 * @param captchaAnswer the captchaAnswer to set
	 */
	public void setCaptchaAnswer(final String captchaAnswer) {
		this.captchaAnswer = captchaAnswer;
	}

	/**
	 * @return the validateCaptcha
	 */
	public boolean isValidateCaptcha() {
		return validateCaptcha;
	}

	/**
	 * @param validateCaptcha the validateCaptcha to set
	 */
	public void setValidateCaptcha(boolean validateCaptcha) {
		this.validateCaptcha = validateCaptcha;
	}
	
	
	/**
	 * Add Context path to the Property bean
	 * @param pContextPath
	 */
	private void addContextPath(String pContextPath) {
		if (pContextPath != null && !isContextAdded()) {
			setTemplateUrl(pContextPath + getTemplateUrl());
			setContextAdded(true);
		}
	}
	
	/**
	 * @return the mContextAdded
	 */
	public boolean isContextAdded() {
		return mContextAdded;
	}

	/**
	 * @param mContextAdded the mContextAdded to set
	 */
	public void setContextAdded(boolean pContextAdded) {
		this.mContextAdded = pContextAdded;
	}
	
	/**
	 * @return the templateUrl
	 */
	public String getTemplateUrl() {
		return mTemplateUrl;
	}

	/**
	 * @param pTemplateUrl
	 *            the templateUrl to set
	 */
	public void setTemplateUrl(String pTemplateUrl) {
		mTemplateUrl = pTemplateUrl;
	}
	
	public BBBCatalogTools getBbbCatalogTools() {
		return mBbbCatalogTools;
	}

	/**
	 * Sets the bbb catalog tools.
	 * 
	 * @param pBbbCatalogTools
	 *            the bbbCatalogTools to set
	 */
	public void setBbbCatalogTools(BBBCatalogTools pBbbCatalogTools) {
		mBbbCatalogTools = pBbbCatalogTools;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	/**
	 * This method performs the validation for Activate Big Blue form
	 * 
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @return void
	 */

	public void preRequestValidation(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse) throws ServletException, IOException {
		logDebug("ActiveBigBlueFormHandler.preRequestValidation() method started");
		//Map<String, String> mErrorMap = new HashMap<String, String>();
		mErrorMap= new HashMap<String,String>();
		validateEmail(pRequest, pResponse, mErrorMap);
		validateCaptcha(pRequest, pResponse, mErrorMap);
		validateFields(pRequest, pResponse, mErrorMap);		
		logDebug("ActivateBigBlueFormHandler.preRequestValidation() method ends");
	}
	
	/**
	 * Handle request for Activate Big Blue form
	 * @param pRequest
	 * @param pResponse
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public boolean handleActivateBigBlueRequest(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse) throws ServletException, IOException {

		logDebug("ActivateBigBlueFormHandler.handleActivateBigBlueRequest() method started");
		BBBSessionBean sessionBean = (BBBSessionBean) pRequest.resolveName(BBBGiftRegistryConstants.SESSION_BEAN);
		addContextPath(pRequest.getContextPath());
		preRequestValidation(pRequest, pResponse);
		final String siteId = mSiteContext.getSite().getId();
		if (!getFormError()) {
			if((BBBCoreConstants.SITE_BBB).equalsIgnoreCase(siteId))
			{
				getActivateBigBlueVO().setmSiteFlag(BBBCoreConstants.SITE_BBB_VALUE);
			}else if((BBBCoreConstants.SITE_BAB_US).equalsIgnoreCase(siteId)){
				getActivateBigBlueVO().setmSiteFlag(BBBCoreConstants.SITE_BAB_US_VALUE);
			}else{
				getActivateBigBlueVO().setmSiteFlag(BBBCoreConstants.SITE_BAB_CA_VALUE);
			}
			try {
				activateCouponRespVO = getActivateCouponManager().activateBigBlue(getActivateBigBlueVO());
				if(activateCouponRespVO.getStatus().isErrorExists())
				{
					setSuccessMessage(false);
					sessionBean.setCouponError(activateCouponRespVO.getStatus().getDisplayMessage());
					final String errorMessage= activateCouponRespVO.getStatus().getDisplayMessage();
					if(!BBBUtility.isEmpty(errorMessage)){
					addFormException(new DropletException(errorMessage));
					mErrorMap.put(BBBCoreConstants.REGISTER_ERROR,errorMessage);
					}else{
						addFormException(new DropletException("Coupon could not be activated."));
						mErrorMap.put(BBBCoreConstants.REGISTER_ERROR,"Coupon could not be activated.");
					}
					//setErrorMessage(activateCouponRespVO.getStatus().getDisplayMessage());
					//addFormExcif eption(new DropletException(activateCouponRespVO.getStatus().getDisplayMessage()));
				}
				else{
					setSuccessMessage(true);
					String emailAddr=getEmailAddress();
					sessionBean.setCouponEmail(emailAddr);
					sessionBean.setCouponExpiry(activateCouponRespVO.getEndDate());
				}
				
			} catch (Exception ex) {
				//if (isLoggingError()) {
					//logError(LogMessageFormatter.formatMessage(pRequest, "err_subscription_tibco_exception" , BBBCoreErrorConstants.ACCOUNT_ERROR_1121),ex);
				//}
				String errorMessage = getLblTxtTemplateManager().getErrMsg("err_subscription_tibco_exception", pRequest.getLocale().getLanguage(),
						null, null);
				addFormException(new DropletException(errorMessage));
				setSuccessMessage(false);
				logDebug(errorMessage,ex);
			}
		}
		logDebug("ActivateBigBlueFormHandler.handleActivateBigBlueRequest() method ends");
		return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest, pResponse);
	}
}
