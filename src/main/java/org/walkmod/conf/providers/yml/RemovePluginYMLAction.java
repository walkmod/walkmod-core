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
package org.walkmod.conf.providers.yml;

import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class RemovePluginYMLAction extends AbstractYMLConfigurationAction {

	private PluginConfig pluginConfig;

	public RemovePluginYMLAction(PluginConfig pluginConfig, YAMLConfigurationProvider provider, boolean recursive) {
		super(provider, recursive);
		this.pluginConfig = pluginConfig;
	}

	@Override
	public void doAction(JsonNode node) throws Exception {
		if (node.has("plugins")) {
			ArrayNode pluginList = null;
			JsonNode aux = node.get("plugins");
			if (aux.isArray()) {
				pluginList = (ArrayNode) node.get("plugins");
				Iterator<JsonNode> it = pluginList.iterator();

				int index = -1;
				int i = 0;
				while (it.hasNext() && index == -1) {
					JsonNode next = it.next();
					if (next.isTextual()) {
						String text = next.asText();
						String[] parts = text.split(":");
						if (parts.length >= 2) {
							if (parts[0].equals(pluginConfig.getGroupId())
									&& parts[1].equals(pluginConfig.getArtifactId())) {
								index = i;
							}
						}
					}
					i++;
				}
				if (index > -1) {
					pluginList.remove(index);
				}

			} else {
				throw new TransformerException("The plugins element is not a valid array");
			}
		}

		provider.write(node);

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new RemovePluginYMLAction(pluginConfig, (YAMLConfigurationProvider) provider, recursive);
	}

}
