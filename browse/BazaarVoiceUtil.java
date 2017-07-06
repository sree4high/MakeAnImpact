package com.bbb.browse;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import atg.nucleus.logging.ApplicationLogging;
import atg.nucleus.logging.ClassLoggingFactory;
import atg.servlet.ServletUtil;

import com.bbb.constants.BBBCoreConstants;
import com.bbb.utils.BBBUtility;
//From web browser
public class BazaarVoiceUtil {
	
	private static final ApplicationLogging MLOGGING =
		    ClassLoggingFactory.getFactory().getLoggerForClass(BazaarVoiceUtil.class);

	private BazaarVoiceUtil(){
		
	}
	/**
	 * Returns a String of hex characters that represents the encoded version of
	 * the specified user ID and uses the default values for other user
	 * attributes.
	 * 
	 * @param userID
	 *            the user ID to be encoded
	 * @param sharedKey
	 *            the shared encoding key
	 * @param emailId 
	 * @return a String of hex characters that represents the encoded version of
	 *         the specified user ID
	 * @throws UnsupportedEncodingException 
	 */
	public static String createUserTokenBVRR(String userID, String sharedKey, String emailId) throws UnsupportedEncodingException{
		String userToken = null;
		if (userID != null) {
			userToken = createUserToken(userID, sharedKey, emailId).toString();
		}
		return userToken;

	}

	/**
	 * Returns a UserToken with a toString value that is a signed, hex-encoded
	 * version of the specified user ID.
	 * 
	 * @param userID
	 *            the user ID to be encoded
	 * @param sharedKey
	 *            the encoding key shared with Bazaarvoice
	 * @return a UserToken with a toString value that is a signed, hex-encoded
	 *         version of the specified user ID.
	 */
	private static UserToken createUserToken(String userID, String sharedKey, String emailId) throws UnsupportedEncodingException{
		if (userID == null) {
			return null;
		}
		return new UserToken(userID, sharedKey, emailId);
	}

	/**
	 * A date-stamped stamped set of user attributes, including the user ID.
	 */
	private static class UserToken {
		private final String sharedKey;
		private final StringBuilder builder;

		/**
		 * Constructs a UserToken with a specified user ID and the key to use to
		 * sign the user attributes.
		 * 
		 * @param userID
		 *            the required user ID
		 * @param sharedKey
		 *            the encoding key shared with Bazaarvoice
		 */
		private UserToken(String userID, String sharedKey, String emailId) throws UnsupportedEncodingException{
			this.sharedKey = sharedKey;
			this.builder = new StringBuilder();
			Date today = new Date();
			add(BBBCoreConstants.DATE, new SimpleDateFormat(BBBCoreConstants.DATE_FORMAT_BV, ServletUtil.getCurrentRequest().getLocale()).format(today));
			add(BBBCoreConstants.USER_ID, userID);
			//PS-18177. Updated UAS to generate token for BV
			if(!BBBUtility.isEmpty(emailId)) {
				add(BBBCoreConstants.EMAIL_ADDRESS, emailId.toLowerCase());
			}
		}

		/**
		 * Adds a URL-style name/value pair to the user authentication string.
		 */
		private UserToken add(String key, String value) throws UnsupportedEncodingException{
			if (value != null && value.length() > 0) {
				if (this.builder.length() > 0) {
					this.builder.append('&');
				}
				this.builder.append(key).append('=').append(urlEncode(value));
			}
			return this;
		}

		/**
		 * Returns a signed, hex-encoded string containing a set of user
		 * attributes.
		 * 
		 * @return a signed, hex-encoded string containing a set of user
		 *         attributes.
		 */
		public String toString() {
			return signAndEncode(this.builder.toString(), this.sharedKey);
		}
	}

	/**
	 * Returns a String of hex characters contining an encoded version of the
	 * specified string combined with a cryptographically secure signature of
	 * the string. The signature is extremely difficult to forge without knowing
	 * the value of the sharedKey, so a correctly signed and encoded string can
	 * be trusted as having come from someone who knows the shared key.
	 * 
	 * @param string
	 *            the string to be encoded
	 * @param sharedKey
	 *            the encoding key shared with Bazaarvoice
	 * @return a String of hex characters that represents the encoded version of
	 *         the specified string
	 */
	private static String signAndEncode(String string, String sharedKey) {
		String signature = null;
		String hexString = null;
		try {
			signature = encodeHex(md5(sharedKey + string));
			hexString = encodeHex(string.getBytes(BBBCoreConstants.UTF_8));
		} catch (Exception ex) {
			return BBBCoreConstants.BAZAAR_VOICE_ERROR;
		}
		return signature + hexString;
	}

	/**
	 * Returns a MessageDigest for the given <code>algorithm</code>.
	 * 
	 * @param algorithm
	 *            The MessageDigest algorithm name.
	 * @return An MD5 digest instance,
	 */
	private static MessageDigest getDigest(String algorithm)throws NoSuchAlgorithmException {
		MessageDigest messageDigest = null;
		
			messageDigest = MessageDigest.getInstance(algorithm);
		return messageDigest;
	}

	/**
	 * Returns an MD5 MessageDigest.
	 * 
	 * @return An MD5 digest instance,
	 */
	private static MessageDigest getMd5Digest()throws NoSuchAlgorithmException {
		return getDigest(BBBCoreConstants.MD_5);
	}

	/**
	 * Calculates the MD5 digest and returns the value as a 16 element
	 * <code>byte[]</code>.
	 * 
	 * @param data
	 *            Data to digest
	 * @return MD5 digest
	 */
	private static byte[] md5(String data)throws NoSuchAlgorithmException,UnsupportedEncodingException {
		byte[] getMd5Digest = null;

		getMd5Digest = getMd5Digest().digest(data.getBytes(BBBCoreConstants.UTF_8));
		return getMd5Digest;
	}

	/**
	 * URL encodes the specified string using the UTF-8 encoding.
	 * 
	 * @param string
	 *            the string to encode
	 * @return the URL encoded string
	 */
	private static String urlEncode(String string) throws UnsupportedEncodingException{
		String urlEncoded = null;
		urlEncoded = URLEncoder.encode(string, BBBCoreConstants.UTF_8);
		return urlEncoded;
	}

	/**
	 * Used building output as Hex
	 */
	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Converts an array of bytes into an array of characters representing the
	 * hexidecimal values of each byte in order. The returned array will be
	 * double the length of the passed array, as it takes two characters to
	 * represent any given byte.
	 * 
	 * @param data
	 *            a byte[] to convert to Hex characters
	 * @return A String containing hexidecimal characters
	 */
	private static String encodeHex(byte[] data) {

		int l = data.length;

		char[] out = new char[l << 1];

		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS[0x0F & data[i]];
		}

		return new String(out);
	}
	
	/**R2.2
	 * Calculates the MD5 hash and returns the value as a 32 character string
	 *
	 * 
	 * @param plaintext data to encrypt
	 *           
	 * @return MD5 hash of plain text
	 */
	public static String generateMD5(String plaintext) {
		MessageDigest messageDigest = null;
		byte[] digest = null;
		try {
			messageDigest = MessageDigest.getInstance(BBBCoreConstants.MD_5);
		} catch (NoSuchAlgorithmException e) {
			// No chance of throwing this exception here since MessageDigest implements MD5 algorithm
			MLOGGING.logError("Error while encryption "+ e.getMessage());
		}
		if(null != messageDigest){
			messageDigest.reset();
			messageDigest.update(plaintext.getBytes());
			digest = messageDigest.digest();	
		}
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
		  hashtext = "0"+hashtext;
		}
		return hashtext;
	}
}
