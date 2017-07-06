//New one
package com.bbb.browse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringEscapeUtils;

import atg.core.util.StringUtils;
import atg.droplet.DropletException;
import atg.multisite.SiteContext;
import atg.multisite.SiteContextManager;
import atg.repository.MutableRepository;
import atg.service.localeservice.LocaleService;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

import com.bbb.cms.manager.LblTxtTemplateManager;
import com.bbb.commerce.catalog.BBBCatalogTools;
import com.bbb.commerce.catalog.vo.SKUDetailVO;
import com.bbb.common.BBBGenericFormHandler;
import com.bbb.constants.BBBCoreConstants;
import com.bbb.constants.BBBWebServiceConstants;
import com.bbb.email.BBBEmailHelper;
import com.bbb.exception.BBBBusinessException;
import com.bbb.exception.BBBSystemException;
import com.bbb.utils.BBBUtility;

/**
 * This form handler will take requests from users to be notified when an item
 * is back in stock.
 */
public class BBBBackInStockFormHandler extends BBBGenericFormHandler {


	/**
	 * Resource bundle name.
	 */
	private static final String MY_RESOURCE_NAME = "atg.projects.store.inventory.UserMessage";

	
	/*
	 * ===================================================== * Constants
	 * =====================================================
	 */
	protected static final String MSG_INVALIDATE_EMAIL_FORMAT = "invalidateEmailFormat";

	/** The lbl txt template manager. */
	private LblTxtTemplateManager lblTxtTemplateManager;
	/**
	 * Success redirect URL.
	 */
	private String mSuccessURL;

	/**
	 * Error redirect URL.
	 */
	private String mErrorURL;

	/**
	 * Catalog reference id.
	 */
	private String mCatalogRefId;

	/**
	 * E-mail address.
	 */
	private String mEmailAddress;

	/**
	 * Product id.
	 */
	private String mProductId;


	/**
	 * Product Name.
	 */
	private String mProductName;
	
	private BBBCatalogTools catalogTools;
	private SiteContext siteContext;
	private String fromPage;// Page Name set from JSP
	private Map<String,String> successUrlMap;
	private Map<String,String> errorUrlMap;

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

	public String getProductName() {
		return this.mProductName;
	}

	public void setProductName(final String mProductName) {
		this.mProductName = mProductName;
	}

	/**
	 * Confirm email
	 */

	private String mConfirmEmailAddress;

	public String getConfirmEmailAddress() {
		return this.mConfirmEmailAddress;
	}

	public void setConfirmEmailAddress(final String mConfirmEmailAddress) {
		this.mConfirmEmailAddress = mConfirmEmailAddress;
	}

	/**
	 * Profile repository.
	 */
	private MutableRepository mProfileRepository;

	/**
	 * @return the profile repository.
	 */
	public MutableRepository getProfileRepository() {
		return this.mProfileRepository;
	}

	/**
	 * @param pProfileRepository
	 *            - the profile repository to set.
	 */
	public void setProfileRepository(final MutableRepository pProfileRepository) {
		this.mProfileRepository = pProfileRepository;
	}

	
	private LocaleService mLocaleService;

	public LocaleService getLocaleService() {
		return this.mLocaleService;
	}

	public void setLocaleService(final LocaleService pLocaleService) {
		this.mLocaleService = pLocaleService;
	}

	/**
	 * @return the catalog reference id.
	 */
	public String getCatalogRefId() {
		return this.mCatalogRefId;
	}

	/**
	 * @param pCatalogRefId
	 *            - the catalog reference id.
	 */
	public void setCatalogRefId(final String pCatalogRefId) {
		this.mCatalogRefId = pCatalogRefId;
	}

	/**
	 * @return the e-mail address.
	 */
	public String getEmailAddress() {
		return this.mEmailAddress;
	}

	/**
	 * @param pEmailAddress
	 *            - the e-mail address to set.
	 */
	public void setEmailAddress(final String pEmailAddress) {
		this.mEmailAddress = pEmailAddress;
	}

	/**
	 * @return the product id.
	 */
	public String getProductId() {
		return this.mProductId;
	}

	/**
	 * @param pProductId
	 *            - the product id to set.
	 */
	public void setProductId(final String pProductId) {
		this.mProductId = pProductId;
	}

	/**
	 * @return the success redirect URL.
	 */
	public String getSuccessURL() {
		return this.mSuccessURL;
	}

	/**
	 * @param pSuccessURL
	 *            - the success redirect URL to set.
	 */
	public void setSuccessURL(final String pSuccessURL) {
		this.mSuccessURL = pSuccessURL;
	}

	/**
	 * @return the error redirect URL.
	 */
	public String getErrorURL() {
		return this.mErrorURL;
	}

	/**
	 * @param pErrorURL
	 *            - the error redirect URL to set.
	 */
	public void setErrorURL(final String pErrorURL) {
		this.mErrorURL = pErrorURL;
	}

	private String mNoJavascriptSuccessURL;

	/**
	 * @return mNoJavascriptSuccessURL
	 */
	public String getNoJavascriptSuccessURL() {
		return this.mNoJavascriptSuccessURL;
	}

	/**
	 * @param pNoJavascriptSuccessURL
	 */
	public void setNoJavascriptSuccessURL(final String pNoJavascriptSuccessURL) {
		this.mNoJavascriptSuccessURL = pNoJavascriptSuccessURL;
	}

	// ----------------------------------------------------
	// property: NoJavascriptErrorURL
	/**
	 * Error url set from a javascript free form
	 */
	private String mNoJavascriptErrorURL;

	/**
	 * @return mNoJavascriptErrorURL
	 */
	public String getNoJavascriptErrorURL() {
		return this.mNoJavascriptErrorURL;
	}

	/**
	 * @param pNoJavascriptErrorURL
	 */
	public void setNoJavascriptErrorURL(final String pNoJavascriptErrorURL) {
		this.mNoJavascriptErrorURL = pNoJavascriptErrorURL;
	}

	/**
	 * This method returns ResourceBundle object for specified locale.
	 * 
	 * @param pLocale
	 *            The locale used to retrieve the resource bundle. If
	 *            <code>null</code> then the default resource bundle is
	 *            returned.
	 * 
	 * @return the resource bundle.
	 */
	public ResourceBundle getResourceBundle(Locale pLocale) {
		if (pLocale == null) {
			return null;
		}

		ResourceBundle rb = atg.core.i18n.LayeredResourceBundle.getBundle(
				MY_RESOURCE_NAME, pLocale);

		return rb;
	}

	/**
	 * This method will handle "notify when back in stock" requests.
	 * 
	 * @param pRequest
	 *            the servlet's request
	 * @param pResponse
	 *            the servlet's response
	 * @throws ServletException
	 *             if there was an error while executing the code
	 * @throws IOException
	 *             if there was an error with servlet io
	 * @return true if success, false - otherwise
	 */
	@SuppressWarnings("rawtypes")
	public boolean handleSendOOSEmail(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {

		if ((getCatalogRefId() == null) || (getCatalogRefId().length() < 1)) {
			
			logDebug("catalogRefId is null. backInStockNotifyItem was not created.");
			

		// When javascript is off if the skuId is not
				if (getNoJavascriptErrorURL() != null) {
				return checkFormRedirect(null, getNoJavascriptErrorURL(),
						pRequest, pResponse);
			}
			// Set in jsps when javascript is enabled
			return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest,
					pResponse);
		}

		if ((getProductId() == null) || (getProductId().length() < 1)) {
			
			logDebug("productId is null. backInStockNotifyItem was not created.");
			

			// When javascript is off if the productId is
			// / not set dont display the success message
			if (getNoJavascriptErrorURL() != null) {
				return checkFormRedirect(null, getNoJavascriptErrorURL(),
						pRequest, pResponse);
			}
			return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest,
					pResponse);
		}

		if (!BBBUtility.isValidEmail(getEmailAddress())
				|| !BBBUtility.isValidEmail(getConfirmEmailAddress())
				|| !getEmailAddress().equals(getConfirmEmailAddress())) {
			
			addFormException(new DropletException(getLblTxtTemplateManager()
					.getErrMsg("err_email_add_invalid",
							pRequest.getLocale().getLanguage(), null, null),"err_email_add_invalid"));
			return checkFormRedirect(null, getErrorURL(), pRequest, pResponse);
		}

		try {

			Map emailParams = createEmailParameters();
			invokeSendOOSTibcoEmail(emailParams);

		}
		catch (BBBBusinessException e) {
			
			logError("BBBBusinessException" + e);
			
			addFormException(new DropletException(getLblTxtTemplateManager()
					.getErrMsg("err_bis_biz_exception",
							pRequest.getLocale().getLanguage(), null, null),"err_bis_biz_exception"));
		}catch (BBBSystemException e) {						
			
			logError("BBBSystemException" + e);
			
			addFormException(new DropletException(getLblTxtTemplateManager()
					.getErrMsg("err_bis_sys_exception",
							pRequest.getLocale().getLanguage(), null, null),"err_bis_sys_exception"));
		
			}
		// Notification created successfully
				if ((getNoJavascriptSuccessURL() != null)
						|| (getNoJavascriptErrorURL() != null)) {
					return checkFormRedirect(getNoJavascriptSuccessURL(),
							getNoJavascriptErrorURL(), pRequest, pResponse);
				}
				
	return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest,
						pResponse);
	}

	protected void invokeSendOOSTibcoEmail(Map emailParams) throws BBBSystemException, BBBBusinessException {
		BBBEmailHelper.sendTibcoEmail(emailParams);
	}
	
	/**
	 * This method is customized to validate the different form fields to match
	 * the business rules
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @param errorPlaceHolderMap Hash Map to store error message key
	 * 
	 */
	
	protected void preUnSubscribeOOSEmail(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) {
			if (!BBBUtility.isValidEmail(getEmailAddress())) {
				addFormException(new DropletException(getLblTxtTemplateManager().getErrMsg("err_email_add_invalid",	pRequest.getLocale().getLanguage(), null, null)));
			}
			if (BBBUtility.isEmpty(getCatalogRefId())) {
				addFormException(new DropletException(getLblTxtTemplateManager().getErrMsg("err_sku_add_invalid",	pRequest.getLocale().getLanguage(), null, null)));
			}
			if (BBBUtility.isEmpty(getProductId())) {
				addFormException(new DropletException(getLblTxtTemplateManager().getErrMsg("err_product_add_invalid",	pRequest.getLocale().getLanguage(), null, null)));
			}
			if (BBBUtility.isEmpty(getProductName())) {
				addFormException(new DropletException(getLblTxtTemplateManager().getErrMsg("err_product_name_add_invalid",	pRequest.getLocale().getLanguage(), null, null)));
			}
	}
	
	/**
	 * This method will handle "Unsubscribe back in stock notification mail" requests.
	 * 
	 * @param pRequest
	 *            the servlet's request
	 * @param pResponse
	 *            the servlet's response
	 * @throws ServletException
	 *             if there was an error while executing the code
	 * @throws IOException
	 *             if there was an error with servlet io
	 * @return true if success, false - otherwise
	 */
	public boolean handleRequestUnSubscribeOOSEmail(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		
		return this.handleUnSubscribeOOSEmail(pRequest, pResponse);
	}
	
	/**
	 * This method will handle "Unsubscribe back in stock notification mail" requests.
	 * 
	 * @param pRequest
	 *            the servlet's request
	 * @param pResponse
	 *            the servlet's response
	 * @throws ServletException
	 *             if there was an error while executing the code
	 * @throws IOException
	 *             if there was an error with servlet io
	 * @return true if success, false - otherwise
	 */
	@SuppressWarnings("rawtypes")
	public boolean handleUnSubscribeOOSEmail(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		String siteId = getSiteIdFromManager();
		if (!BBBUtility.siteIsTbs(siteId) && StringUtils.isNotEmpty(getFromPage())) {
			setSuccessURL(pRequest.getContextPath()
					+ getSuccessUrlMap().get(getFromPage()));
			setErrorURL(pRequest.getContextPath()
					+ getErrorUrlMap().get(getFromPage()));

		}
		this.preUnSubscribeOOSEmail(pRequest, pResponse);
		
		if(getFormError()) {
			return checkFormRedirect(null, getErrorURL(), pRequest, pResponse);
		}
 
		try {

			Map emailParams = createUnsubscribeEmailParameters();
			invokeSendUnsubscribeTibcoEmail(emailParams);

		}
		catch (BBBBusinessException e) {
			
				logError("BBBBusinessException" + e);
			
			addFormException(new DropletException(getLblTxtTemplateManager()
					.getErrMsg("err_bis_biz_exception",
							pRequest.getLocale().getLanguage(), null, null),"err_bis_biz_exception"));
		}catch (BBBSystemException e) {						
			
				logError("BBBSystemException" + e);
			
			addFormException(new DropletException(getLblTxtTemplateManager()
					.getErrMsg("err_bis_sys_exception",
							pRequest.getLocale().getLanguage(), null, null),"err_bis_sys_exception"));
		
			}
		// Notification created successfully
				if ((getNoJavascriptSuccessURL() != null)
						|| (getNoJavascriptErrorURL() != null)) {
					return checkFormRedirect(getNoJavascriptSuccessURL(),
							getNoJavascriptErrorURL(), pRequest, pResponse);
				}
				
	return checkFormRedirect(getSuccessURL(), getErrorURL(), pRequest,
						pResponse);
		
	}

	protected void invokeSendUnsubscribeTibcoEmail(Map emailParams) throws BBBSystemException, BBBBusinessException {
		BBBEmailHelper.sendUnsubscribeTibcoEmail(emailParams);
	}
		
	/**
	 * This method will set email params for Subscribe OOS Email Notification
	 * 
	 * @return Map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map createEmailParameters() throws  BBBSystemException, BBBBusinessException {
		Map emailParams = new HashMap();
		emailParams.put(BBBCoreConstants.SKU_PARAM_NAME, getCatalogRefId());
		emailParams.put(BBBCoreConstants.PRODUCT_ID_PARAM_NAME, getProductId());
		if(StringUtils.isEmpty(getProductName()) && !StringUtils.isEmpty(getCatalogRefId())){
			SKUDetailVO vo = getCatalogTools().getSKUDetails(getSiteIdFromManager(), getCatalogRefId() , false, false);
			if(vo != null){
				try {
					setProductName(URLDecoder.decode(vo.getDisplayName(), BBBCoreConstants.UTF_8));
				} catch (UnsupportedEncodingException e) {
					logError("No info found for sku - " + getCatalogRefId() + " - Product -" +getProductId());
				}
			} else {
				logError("No info found for sku - " + getCatalogRefId() + " - Product -" +getProductId());
			}
			
		}
		if(BBBCoreConstants.MOBILEWEB.equalsIgnoreCase(BBBUtility.getChannel())){
			String productName = StringEscapeUtils.escapeXml(getProductName());
			emailParams.put(BBBCoreConstants.PRODUCT_NAME_PARAM_NAME, productName);
		}
		else {
			emailParams.put(BBBCoreConstants.PRODUCT_NAME_PARAM_NAME, getProductName());
		}
		emailParams.put(BBBCoreConstants.CUST_NAME_PARAM_NAME,	getCustomerName());
		emailParams.put(BBBCoreConstants.EMAIL_ADDR_PARAM_NAME, getEmailAddress());
		String siteId = getSiteContext().getSite().getId();
		List siteIds = getCatalogTools().getAllValuesForKey(BBBWebServiceConstants.TXT_WSDLKEY_WSSITEFLAG,siteId);
		if(null != siteIds && !siteIds.isEmpty()){
			setSiteFlag(siteIds.get(0).toString());
		}
		
		emailParams.put(BBBCoreConstants.SITE_FLAG_PARAM_NAME,  getSiteFlag());		
		emailParams.put(BBBCoreConstants.REQUESTED_DT_PARAM_NAME, BBBUtility.getXMLCalendar(new java.util.Date()));
		return emailParams;
	}

	protected String getSiteIdFromManager() {
		return SiteContextManager.getCurrentSiteId();
	}
	
	/**
	 * This method will set email params for UnSubscribe OOS Email Notification
	 * 
	 * @return Map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map createUnsubscribeEmailParameters() throws  BBBSystemException, BBBBusinessException {
		Map emailParams = new HashMap();
		emailParams.put(BBBCoreConstants.SKU_PARAM_NAME, getCatalogRefId());
		emailParams.put(BBBCoreConstants.PRODUCT_ID_PARAM_NAME, getProductId());
		emailParams.put(BBBCoreConstants.PRODUCT_NAME_PARAM_NAME, getProductName());
		emailParams.put(BBBCoreConstants.CUST_NAME_PARAM_NAME,	getCustomerName());
		emailParams.put(BBBCoreConstants.EMAIL_ADDR_PARAM_NAME, getEmailAddress());
		String siteId = getSiteContext().getSite().getId();
		List siteIds = getCatalogTools().getAllValuesForKey(BBBWebServiceConstants.TXT_WSDLKEY_WSSITEFLAG,siteId);
		if(null != siteIds){
			setSiteFlag(siteIds.get(0).toString());
		}
		
		emailParams.put(BBBCoreConstants.SITE_FLAG_PARAM_NAME,  getSiteFlag());
		emailParams.put(BBBCoreConstants.UNSUBSCRIBE_DT_PARAM_NAME, BBBUtility.getXMLCalendar(new java.util.Date()));
		return emailParams;
	}

	private String mCustomerName;

	public String getCustomerName() {
		return this.mCustomerName;
	}

	public void setCustomerName(final String mCustomerName) {
		this.mCustomerName = mCustomerName;
	}

	private String mSiteFlag;

	public String getSiteFlag() {
		return this.mSiteFlag;
	}

	public void setSiteFlag(final String mSiteFlag) {
		this.mSiteFlag = mSiteFlag;
	}

	/**
	 * @return the siteContext
	 */
	public SiteContext getSiteContext() {
		return this.siteContext;
	}

	/**
	 * @param siteContext the siteContext to set
	 */
	public void setSiteContext(SiteContext siteContext) {
		this.siteContext = siteContext;
	}

	/**
	 * @return the catalogTools
	 */
	public BBBCatalogTools getCatalogTools() {
		return this.catalogTools;
	}

	/**
	 * @param catalogTools the catalogTools to set
	 */
	public void setCatalogTools(BBBCatalogTools catalogTools) {
		this.catalogTools = catalogTools;
	}

	/**
	 * @return the lblTxtTemplateManager
	 */
	public LblTxtTemplateManager getLblTxtTemplateManager() {
		return this.lblTxtTemplateManager;
	}

	/**
	 * @param lblTxtTemplateManager the lblTxtTemplateManager to set
	 */
	public void setLblTxtTemplateManager(LblTxtTemplateManager lblTxtTemplateManager) {
		this.lblTxtTemplateManager = lblTxtTemplateManager;
	}

}
