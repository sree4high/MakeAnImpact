/**
 * This Class is used to display the Captcha on different pages.
 */
package com.bbb.browse;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;

import nl.captcha.Captcha;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import com.bbb.common.BBBDynamoServlet;
import com.bbb.constants.BBBGiftRegistryConstants;
import com.bbb.profile.session.BBBSessionBean;
/**
 * @author agupt8
 *
 */
 // for conflict: FILE
// again for conflict : WEB
public class BBBCaptchaServlet extends BBBDynamoServlet {
	protected int width=300;
	protected int height=100;
	
	// no conflict: WEB
	
	/* (non-Javadoc)
	 * @see atg.servlet.DynamoServlet#service(atg.servlet.DynamoHttpServletRequest, atg.servlet.DynamoHttpServletResponse)
	 */
	public void service(final DynamoHttpServletRequest req, final DynamoHttpServletResponse res)
			throws ServletException, IOException {

		//Creating the Captcha
		final Captcha captcha = new Captcha.Builder(width, height)
		.addText()
		.addBackground(new GradiatedBackgroundProducer())
		.gimp()
		.addNoise()
		.addBorder()
		.build();
		//Setting the content type to image
		res.setContentType("image/png");
		
		//Writing the Captcha image to the output stream
		ImageIO.write(captcha.getImage(), "png", res.getOutputStream());
		res.getOutputStream().close();
		logDebug("SessionId = "+req.getSession().getId());
		logDebug("Captcha Generated = "+captcha.toString());
//		req.getSession().setAttribute(Captcha.NAME, captcha);
		/** The Session bean. */
		BBBSessionBean sessionBean = (BBBSessionBean) req.resolveName(BBBGiftRegistryConstants.SESSION_BEAN);		
		sessionBean.setCaptcha(captcha);
		
	}
	
}
