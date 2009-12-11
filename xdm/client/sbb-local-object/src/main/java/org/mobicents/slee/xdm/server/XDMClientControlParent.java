/**
 * 
 */
package org.mobicents.slee.xdm.server;

import java.util.Map;

import org.openxdm.xcap.common.key.XcapUriKey;
import org.openxdm.xcap.common.uri.AttributeSelector;
import org.openxdm.xcap.common.uri.DocumentSelector;
import org.openxdm.xcap.common.uri.NodeSelector;

/**
 * @author martins
 *
 */
public interface XDMClientControlParent {

	/**
	 * Provides response of a get request.
	 * 
	 * @param key
	 * @param responseCode
	 * @param mimetype
	 * @param content
	 * @param eTag
	 */
	public void getResponse(XcapUriKey key, int responseCode, String mimetype,
			String content, String eTag);

	/**
	 * Provides response of a put request.
	 * 
	 * @param key
	 * @param responseCode
	 * @param eTag
	 * @param tag 
	 */
	public void putResponse(XcapUriKey key, int responseCode, String responseContent, String eTag);

	/**
	 * Provides response of a delete request.
	 * 
	 * @param key
	 * @param responseCode
	 * @param eTag
	 * @param tag 
	 */
	public void deleteResponse(XcapUriKey key, int responseCode, String responseContent, String eTag);

	/**
	 * Callback that indicates a document subscribed in the XDM client was
	 * updated
	 */
	public void documentUpdated(DocumentSelector documentSelector,
			String oldETag, String newETag, String documentAsString);

	/**
	 * Callback that indicates a element in a subscribed document was updated
	 */
	public void elementUpdated(DocumentSelector documentSelector,
			NodeSelector nodeSelector, Map<String, String> namespaces,
			String oldETag, String newETag, String documentAsString,
			String elementAsString);

	/**
	 * Callback that indicates a attribute in a subscribed document was updated
	 */
	public void attributeUpdated(DocumentSelector documentSelector,
			NodeSelector nodeSelector, AttributeSelector attributeSelector,
			Map<String, String> namespaces, String oldETag, String newETag,
			String documentAsString, String attributeValue);
	
}
