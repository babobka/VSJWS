package ru.babobka.vsjws.util;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public interface HtmlUtil {

	public static String xslToHtml(File xslFile, String html) throws TransformerException {
		StringReader reader = new StringReader(html);
		StringWriter writer = new StringWriter();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory
				.newTransformer(new javax.xml.transform.stream.StreamSource(xslFile.getPath()));
		transformer.transform(new javax.xml.transform.stream.StreamSource(reader),
				new javax.xml.transform.stream.StreamResult(writer));

		return writer.toString();
	}

}
