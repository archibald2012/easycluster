package org.easycluster.easycluster.cluster.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.easycluster.easycluster.cluster.exception.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlUtil {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(XmlUtil.class);

	/**
	 * Deserialize the XML string to object
	 * 
	 * @param content
	 * @param clazz
	 * @return instance of given class
	 * @throws SerializationException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(String content, Class<?> clazz) {
		InputStream is = new ByteArrayInputStream(content.getBytes());
		T obj = null;
		try {
			obj = (T) createContext(clazz).createUnmarshaller().unmarshal(is);
		} catch (JAXBException e) {
			String errorMsg = "Failed to parse the xml string with error: " + e.getMessage();
			LOGGER.error(errorMsg);
			throw new SerializationException(errorMsg, e);
		} finally {
			try {
				is.close();
			} catch (IOException ignore) {
			}
		}
		return obj;
	}

	/**
	 * serialize the object to XML string
	 * 
	 * @param obj
	 * @return XML
	 * @throws SerializationException
	 */
	public static String marshal(Object obj) {
		OutputStream os = new ByteArrayOutputStream();
		String xmlStr = null;
		try {
			createContext(obj.getClass()).createMarshaller().marshal(obj, os);
			xmlStr = os.toString();
		} catch (JAXBException e) {
			String errorMsg = "Failed to parse the object to xml string with error: " + e.getMessage();
			LOGGER.error(errorMsg);
			throw new SerializationException(errorMsg, e);
		} finally {
			try {
				os.close();
			} catch (IOException ignore) {
			}
		}
		return xmlStr;
	}

	private static JAXBContext createContext(Class<?> clazz) throws JAXBException {
		return JAXBContext.newInstance(clazz);
	}
}
