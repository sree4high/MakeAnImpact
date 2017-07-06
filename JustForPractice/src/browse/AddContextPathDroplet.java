package com.bbb.browse;

import java.io.IOException;

import javax.servlet.ServletException;

import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

import com.bbb.common.BBBDynamoServlet;
import com.bbb.constants.BBBCoreConstants;
import com.bbb.utils.BBBUtility;

/**
 * 
 * @author Sapient
 *	
 *	This Droplet is called to correct the context path of the URL given in the input.
 *	It handles the following cases:
 *
 *	If the call is from /store site and
 *	Input starts with "tbs/" or "store/" 
 *	OR contains "/tbs/" or no context; then context in the
 *	Output will be replaced by "/store/"
 *
 * 	If the call is from /tbs site and
 *	Input starts with "store/" or "tbs/" 
 *	OR contains "/store/" or no context; then context in the
 *	Output will be replaced by "/tbs/"
 *	 
 */
 //This is from the file browser
public class AddContextPathDroplet extends BBBDynamoServlet{
	
	@Override
	public void service(final DynamoHttpServletRequest pRequest,
			final DynamoHttpServletResponse pResponse) throws ServletException,
			IOException {
		
		logDebug("Entering AddContextPathDroplet to add context path to URL");
		String URL = BBBCoreConstants.BLANK;
		if(pRequest.getParameter(BBBCoreConstants.INPUT_LINK)!=null){
			URL = pRequest.getParameter(BBBCoreConstants.INPUT_LINK).trim();
		}
		logDebug("Input URl "+URL);
		if(URL.startsWith(BBBCoreConstants.HTTP)||URL.startsWith(BBBCoreConstants.HTTPS_COLON)|| URL.startsWith(BBBCoreConstants.DOUBLE_SLASH)){
			pRequest.setParameter(BBBCoreConstants.OUTPUT_LINK,URL);
			pRequest.serviceParameter(BBBCoreConstants.OPARAM, pRequest, pResponse);	
		}
		else if((pRequest.getContextPath()).contains(BBBCoreConstants.STORE)){
		
			//BBBI-3842 | Keyword Search Response issue | URL length check updated
			if(URL.length() > 6 && (URL.substring(0,6)).equals(BBBCoreConstants.STORE+ BBBCoreConstants.SLASH)){
				
				URL= BBBCoreConstants.SLASH+ URL;
			}
			else if(URL.length() > 4 && (URL.substring(0,4)).equals(BBBUtility.toLowerCase(BBBCoreConstants.TBS)+ BBBCoreConstants.SLASH)){
				
				URL= URL.replaceFirst(BBBUtility.toLowerCase(BBBCoreConstants.TBS),BBBCoreConstants.CONTEXT_STORE);
			}
			else if(!URL.contains(BBBCoreConstants.CONTEXT_STORE+ BBBCoreConstants.SLASH)){
				
				if(URL.contains(BBBCoreConstants.CONTEXT_TBS+ BBBCoreConstants.SLASH)){
					URL= URL.replaceFirst(BBBCoreConstants.CONTEXT_TBS,BBBCoreConstants.CONTEXT_STORE);
				}
				
					else{
					//BBBI-3842 | Keyword Search Response issue | URL Slash updated - to avoid breakdown of page on garbage values
					if(URL.startsWith(BBBCoreConstants.SLASH)) {
						URL= BBBCoreConstants.CONTEXT_STORE + URL;
					} else {
						URL= BBBCoreConstants.CONTEXT_STORE + BBBCoreConstants.SLASH + URL;
					}
				}
			}
			
			pRequest.setParameter(BBBCoreConstants.OUTPUT_LINK,URL);
			pRequest.serviceParameter(BBBCoreConstants.OPARAM, pRequest, pResponse);
		}	else{
				
			if(URL.length() > 4 &&  (URL.substring(0,4)).equals(BBBUtility.toLowerCase(BBBCoreConstants.TBS)+ BBBCoreConstants.SLASH)){
				
				URL= BBBCoreConstants.SLASH+ URL;
			}
			else if(URL.length() > 6 && (URL.substring(0,6)).equals(BBBCoreConstants.STORE+ BBBCoreConstants.SLASH)){
				
				URL= URL.replaceFirst(BBBCoreConstants.STORE,BBBCoreConstants.CONTEXT_TBS);
			}
			else if(!URL.contains(BBBCoreConstants.CONTEXT_TBS+ BBBCoreConstants.SLASH)){
				
				if(URL.contains(BBBCoreConstants.CONTEXT_STORE+ BBBCoreConstants.SLASH)){
					URL= URL.replaceFirst(BBBCoreConstants.CONTEXT_STORE,BBBCoreConstants.CONTEXT_TBS);
				}
				else{
					URL= BBBCoreConstants.CONTEXT_TBS + URL;
				}
			}
			logDebug("Final URl "+URL);
			pRequest.setParameter(BBBCoreConstants.OUTPUT_LINK,URL);
			pRequest.serviceParameter(BBBCoreConstants.OPARAM, pRequest, pResponse);
		}
	}

}
