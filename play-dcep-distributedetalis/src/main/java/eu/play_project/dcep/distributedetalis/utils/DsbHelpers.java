package eu.play_project.dcep.distributedetalis.utils;

import java.util.Iterator;

import javax.xml.namespace.QName;

import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.TopicExpressionType;

/**
 * A few helper methods to deal with data from the DSB.
 * 
 * @author chamerling
 * @author stuehmer
 */
public class DsbHelpers {

	/**
	 * Create a {@linkplain QName} from the topic content.
	 */
	public static QName topicToQname(TopicExpressionType topic) {
		String topicContent = topic.getContent();
        String prefix = null;
        String localPart = null;
        if (topicContent.contains(":")) {
            prefix = topicContent.substring(0, topicContent.indexOf(":"));
            localPart = topicContent.substring(topicContent.indexOf(":") + 1);
        }
        // get the NS for the prefix
        String ns = null;
        if (prefix != null && topic.getTopicNamespaces() != null) {
            boolean found = false;
            Iterator<QName> iter = topic.getTopicNamespaces().iterator();
            while (iter.hasNext() && !found) {
                QName qname = iter.next();
                if (prefix.equals(qname.getLocalPart())) {
                    ns = qname.getNamespaceURI();
                    found = true;
                }
            }
        }

        QName topicName = new QName(ns, localPart, prefix);
        return topicName;
	}

	/**
	 * Create a URI string from the topic content.
	 */
	public static String topicToUri(TopicExpressionType topic) {
		QName qn = topicToQname(topic);
		return qn.getNamespaceURI() + qn.getLocalPart();
	}

}
