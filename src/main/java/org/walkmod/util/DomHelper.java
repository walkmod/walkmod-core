/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.util.location.Location;
import org.walkmod.util.location.LocationAttributes;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import java.util.Map;

public class DomHelper {

	private static final Log LOG = LogFactory.getLog(DomHelper.class);

	public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

	public static Location getLocationObject(Element element) {
		return LocationAttributes.getLocation(element);
	}

	/**
	 * Creates a W3C Document that remembers the location of each element in the
	 * source file. The location of element nodes can then be retrieved using
	 * the {@link #getLocationObject(Element)} method.
	 *
	 * @param inputSource
	 *            the inputSource to read the document from
	 * @return Document
	 */
	public static Document parse(InputSource inputSource) {
		return parse(inputSource, null);
	}

	/**
	 * Creates a W3C Document that remembers the location of each element in the
	 * source file. The location of element nodes can then be retrieved using
	 * the {@link #getLocationObject(Element)} method.
	 *
	 * @param inputSource
	 *            the inputSource to read the document from
	 * @param dtdMappings
	 *            a map of DTD names and public ids
	 * @return Document
	 */
	public static Document parse(InputSource inputSource, Map<String, String> dtdMappings) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating((dtdMappings != null));
		factory.setNamespaceAware(true);
		SAXParser parser = null;
		try {
			parser = factory.newSAXParser();
		} catch (Exception ex) {
			throw new WalkModException("Unable to create SAX parser", ex);
		}
		DOMBuilder builder = new DOMBuilder();
		ContentHandler locationHandler = new LocationAttributes.Pipe(builder);
		try {
			parser.parse(inputSource, new StartHandler(locationHandler, dtdMappings));
		} catch (Exception ex) {
			throw new WalkModException(ex);
		}
		return builder.getDocument();
	}

	/**
	 * The <code>DOMBuilder</code> is a utility class that will generate a W3C
	 * DOM Document from SAX events.
	 *
	 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
	 */
	public static class DOMBuilder implements ContentHandler {

		/** The default transformer factory shared by all instances */
		protected static SAXTransformerFactory FACTORY;

		/** The transformer factory */
		protected SAXTransformerFactory factory;

		/** The result */
		protected DOMResult result;

		/** The parentNode */
		protected Node parentNode;

		protected ContentHandler nextHandler;

		static {
			FACTORY = (SAXTransformerFactory) TransformerFactory.newInstance();
		}

		/**
		 * Construct a new instance of this DOMBuilder.
		 */
		public DOMBuilder() {
			this((Node) null);
		}

		/**
		 * Construct a new instance of this DOMBuilder.
		 * 
		 * @param factory
		 *            Transformer factory to use
		 */
		public DOMBuilder(SAXTransformerFactory factory) {
			this(factory, null);
		}

		/**
		 * Constructs a new instance that appends nodes to the given parent
		 * node.
		 * 
		 * @param parentNode
		 *            The parent node to use
		 */
		public DOMBuilder(Node parentNode) {
			this(null, parentNode);
		}

		/**
		 * Construct a new instance of this DOMBuilder.
		 * 
		 * @param factory
		 *            Transformer factory to use
		 * @param parentNode
		 *            The parent node to use
		 */
		public DOMBuilder(SAXTransformerFactory factory, Node parentNode) {
			this.factory = factory == null ? FACTORY : factory;
			this.parentNode = parentNode;
			setup();
		}

		/**
		 * Setup this instance transformer and result objects.
		 */
		private void setup() {
			try {
				TransformerHandler handler = this.factory.newTransformerHandler();
				nextHandler = handler;
				if (this.parentNode != null) {
					this.result = new DOMResult(this.parentNode);
				} else {
					this.result = new DOMResult();
				}
				handler.setResult(this.result);
			} catch (javax.xml.transform.TransformerException local) {
				throw new WalkModException("Fatal-Error: Unable to get transformer handler", local);
			}
		}

		/**
		 * Return the newly built Document.
		 * 
		 * @return Document
		 */
		public Document getDocument() {
			if (this.result == null || this.result.getNode() == null) {
				return null;
			} else if (this.result.getNode().getNodeType() == Node.DOCUMENT_NODE) {
				return (Document) this.result.getNode();
			} else {
				return this.result.getNode().getOwnerDocument();
			}
		}

		public void setDocumentLocator(Locator locator) {
			nextHandler.setDocumentLocator(locator);
		}

		public void startDocument() throws SAXException {
			nextHandler.startDocument();
		}

		public void endDocument() throws SAXException {
			nextHandler.endDocument();
		}

		public void startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
			nextHandler.startElement(uri, loc, raw, attrs);
		}

		public void endElement(String arg0, String arg1, String arg2) throws SAXException {
			nextHandler.endElement(arg0, arg1, arg2);
		}

		public void startPrefixMapping(String arg0, String arg1) throws SAXException {
			nextHandler.startPrefixMapping(arg0, arg1);
		}

		public void endPrefixMapping(String arg0) throws SAXException {
			nextHandler.endPrefixMapping(arg0);
		}

		public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
			nextHandler.characters(arg0, arg1, arg2);
		}

		public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
			nextHandler.ignorableWhitespace(arg0, arg1, arg2);
		}

		public void processingInstruction(String arg0, String arg1) throws SAXException {
			nextHandler.processingInstruction(arg0, arg1);
		}

		public void skippedEntity(String arg0) throws SAXException {
			nextHandler.skippedEntity(arg0);
		}
	}

	public static class StartHandler extends DefaultHandler {

		private ContentHandler nextHandler;

		private Map<String, String> dtdMappings;

		/**
		 * Create a filter that is chained to another handler.
		 * 
		 * @param next
		 *            the next handler in the chain.
		 * @param dtdMappings
		 *            Set of supported versions of dtdMappings
		 */
		public StartHandler(ContentHandler next, Map<String, String> dtdMappings) {
			nextHandler = next;
			this.dtdMappings = dtdMappings;
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			nextHandler.setDocumentLocator(locator);
		}

		@Override
		public void startDocument() throws SAXException {
			nextHandler.startDocument();
		}

		@Override
		public void endDocument() throws SAXException {
			nextHandler.endDocument();
		}

		@Override
		public void startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
			nextHandler.startElement(uri, loc, raw, attrs);
		}

		@Override
		public void endElement(String arg0, String arg1, String arg2) throws SAXException {
			nextHandler.endElement(arg0, arg1, arg2);
		}

		@Override
		public void startPrefixMapping(String arg0, String arg1) throws SAXException {
			nextHandler.startPrefixMapping(arg0, arg1);
		}

		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
			nextHandler.endPrefixMapping(arg0);
		}

		@Override
		public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
			nextHandler.characters(arg0, arg1, arg2);
		}

		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
			nextHandler.ignorableWhitespace(arg0, arg1, arg2);
		}

		@Override
		public void processingInstruction(String arg0, String arg1) throws SAXException {
			nextHandler.processingInstruction(arg0, arg1);
		}

		@Override
		public void skippedEntity(String arg0) throws SAXException {
			nextHandler.skippedEntity(arg0);
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			if (dtdMappings != null && dtdMappings.containsKey(publicId)) {
				String val = dtdMappings.get(publicId).toString();
				return new InputSource(ClassLoaderUtil.getResourceAsStream(val, DomHelper.class));
			}
			return null;
		}

		@Override
		public void warning(SAXParseException exception) {
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			LOG.error(exception.getMessage() + " at (" + exception.getPublicId() + ":" + exception.getLineNumber()
					+ ":" + exception.getColumnNumber() + ")", exception);
			throw exception;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			LOG.fatal(exception.getMessage() + " at (" + exception.getPublicId() + ":" + exception.getLineNumber()
					+ ":" + exception.getColumnNumber() + ")", exception);
			throw exception;
		}
	}
}
