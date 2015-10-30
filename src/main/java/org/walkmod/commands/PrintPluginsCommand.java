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
package org.walkmod.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.entities.impl.PluginConfigImpl;
import org.walkmod.exceptions.WalkModException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.vandermeer.asciitable.v2.V2_AsciiTable;

@Parameters(separators = "=", commandDescription = "Shows the available walkmod plugins created by the community.")
public class PrintPluginsCommand implements Command, AsciiTableAware {

	public final static String MVN_SEARCH_URL = "https://search.maven.org/solrsearch/select?q=walkmod&&rows=50&&wt=json";

	public final static String ARTIFACT_DETAILS_URL = "https://search.maven.org/remotecontent?filepath=";

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	private V2_AsciiTable at = null;

	public PrintPluginsCommand(JCommander command) {
		this.command = command;
	}

	public void execute() {
		if (help) {
			command.usage("init");
		} else {
			try {
				WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
				Configuration cfg = facade.getConfiguration();
				
				Collection<PluginConfig> installedPlugins = null;
				if(cfg != null){
					installedPlugins = cfg.getPlugins();
				}
				URL searchURL = new URL(MVN_SEARCH_URL);
				InputStream is = null;
				Map<String, Boolean> installedList = new LinkedHashMap<String, Boolean>();
				Map<String, String> pluginsList = new LinkedHashMap<String, String>();
				Map<String, String> pluginsURLs = new LinkedHashMap<String, String>();
				try {
					is = searchURL.openStream();

					String content = readInputStreamAsString(is);
					DefaultJSONParser parser = new DefaultJSONParser(content);
					JSONObject object = parser.parseObject();
					parser.close();

					JSONArray artifactList = (object.getJSONObject("response")).getJSONArray("docs");

					for (int i = 0; i < artifactList.size(); i++) {
						JSONObject artifact = artifactList.getJSONObject(i);
						String artifactId = artifact.getString("a");
						if (artifactId.startsWith("walkmod-") && artifactId.endsWith("-plugin")) {
							String groupId = artifact.getString("g");
							String latestVersion = artifact.getString("latestVersion");
							String pom = artifactId + "-" + latestVersion + ".pom";
							String directory = groupId.replaceAll("\\.", "/");

							URL artifactDetailsURL = new URL(ARTIFACT_DETAILS_URL + directory + "/" + artifactId + "/"
									+ latestVersion + "/" + pom);

							InputStream projectIs = artifactDetailsURL.openStream();

							try {
								DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
								DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
								Document doc = dBuilder.parse(projectIs);

								NodeList nList = doc.getElementsByTagName("description");
								String description = "unavailable description";
								if (nList.getLength() == 1) {
									description = nList.item(0).getTextContent();
								}
								String id = "";
								if (!groupId.equals("org.walkmod")) {
									id = groupId + ":";
								}
								id += artifactId.substring("walkmod-".length(),
										artifactId.length() - "-plugin".length());

								if (Character.isLowerCase(description.charAt(0))) {
									description = Character.toUpperCase(description.charAt(0))
											+ description.substring(1, description.length());
								}
								if (!description.endsWith(".")) {
									description = description + ".";
								}
								pluginsList.put(id, description);
								nList = doc.getChildNodes().item(0).getChildNodes();
								int max = nList.getLength();
								String url = "unavailable url";

								for (int j = 0; j < max; j++) {
									String name = nList.item(j).getNodeName();
									if (name.equals("url")) {
										url = nList.item(j).getTextContent();
										j = max;
									}
								}

								pluginsURLs.put(id, url);

								PluginConfig equivalentPluginCfg = new PluginConfigImpl();
								equivalentPluginCfg.setGroupId(groupId);
								equivalentPluginCfg.setArtifactId(artifactId);

								boolean isInstalled = (installedPlugins != null && installedPlugins
										.contains(equivalentPluginCfg));

								installedList.put(id, isInstalled);

							} finally {
								projectIs.close();
							}
						}
					}
				} finally {
					is.close();
				}
				Set<String> keys = pluginsList.keySet();
				List<String> sortedKeys = new LinkedList<String>(keys);
				Collections.sort(sortedKeys);

				at = new V2_AsciiTable();
				at.addRule();
				at.addRow("PLUGIN NAME (ID)", "INSTALLED", "URL (DOCUMENTATION)", "DESCRIPTION");
				at.addStrongRule();
				for (String key : sortedKeys) {
					String installed = "";
					if (installedList.get(key)) {
						installed = "*";
					}
					at.addRow(key, installed, pluginsURLs.get(key), pluginsList.get(key));
				}
				at.addRule();

			} catch (Exception e) {
				throw new WalkModException("Invalid plugins URL", e);
			}
		}
	}

	private String readInputStreamAsString(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		String line = br.readLine();
		String response = "";
		while (line != null) {
			response += line + '\n';
			line = br.readLine();
		}
		return response;
	}

	@Override
	public V2_AsciiTable getTable() {
		return at;
	}

}
