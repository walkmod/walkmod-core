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
package org.walkmod.conf.providers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.DomConstants;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.impl.PluginConfigImpl;
import org.walkmod.util.DomHelper;
import org.xml.sax.InputSource;

public class PluginsConfigurationProvider implements ConfigurationProvider {

	private Configuration configuration;

	private Map<String, String> dtdMappings;

	private Document document;

	private static final Log LOG = LogFactory.getLog(PluginsConfigurationProvider.class);

	private String fileName;

	private boolean errorIfMissing;

	public PluginsConfigurationProvider() {
		this("default-plugins.xml", true);
	}

	public PluginsConfigurationProvider(String fileName, boolean errorIfMissing) {
		this.fileName = fileName;
		this.errorIfMissing = errorIfMissing;
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.putAll(DomConstants.WALKMOD_DTD_MAPPINGS);
		mappings.put("-//WALKMOD//WalkMod 1.0//EN", "walkmod-plugins-1.0.dtd");
		setDtdMappings(mappings);
	}

	public void setDtdMappings(Map<String, String> mappings) {
		this.dtdMappings = Collections.unmodifiableMap(mappings);
	}

	public Map<String, String> getDtdMappings() {
		return dtdMappings;
	}

	@Override
	public void init(Configuration configuration) {
		this.configuration = configuration;
		this.document = lookUpDocument();
	}

	private Document lookUpDocument() {
		Document doc = null;
		URL url = null;
		if (configuration == null) {
			throw new ConfigurationException("Missing default values configuration");
		}

		File f = new File(fileName);
		if (f.exists()) {
			try {
				url = f.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new ConfigurationException("Unable to load " + fileName, e);
			}
		}
		if (url == null) {
			url = configuration.getClassLoader().getResource(fileName);
		}
		InputStream is = null;
		if (url == null) {
			if (errorIfMissing) {
				throw new ConfigurationException("Could not open files of the name " + fileName);
			} else {
				LOG.info("Unable to locate default values configuration of the name " + f.getName() + ", skipping");
				return doc;
			}
		}
		try {
			is = url.openStream();
			InputSource in = new InputSource(is);
			in.setSystemId(url.toString());
			doc = DomHelper.parse(in, dtdMappings);
		} catch (Exception e) {
			throw new ConfigurationException("Unable to load " + fileName, e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.error("Unable to close input stream", e);
			}
		}
		if (doc != null) {
			LOG.debug("Default Walkmod plugins configuration parsed");
		}
		return doc;
	}

	@Override
	public void load() throws ConfigurationException {
		Collection<PluginConfig> plugins = configuration.getPlugins();
		if (plugins == null) {
			plugins = new LinkedList<PluginConfig>();
			configuration.setPlugins(plugins);
		}

		Element rootElement = document.getDocumentElement();
		NodeList childNodes = rootElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if ((childNode.getNodeType() == Node.ELEMENT_NODE) && "plugin".equals(childNode.getNodeName())) {

				Element paramElement = (Element) childNode;
				String groupId = paramElement.getAttribute("groupId");
				String artifactId = paramElement.getAttribute("artifactId");
				String version = paramElement.getAttribute("version");

				PluginConfig defaultPlugin = new PluginConfigImpl();
				defaultPlugin.setGroupId(groupId);
				defaultPlugin.setArtifactId(artifactId);
				defaultPlugin.setVersion(version);

				plugins.add(defaultPlugin);
			}
		}
	}

}
