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

import java.util.List;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RemoveIncludesOrExcludesYMLAction extends AbstractYMLConfigurationAction {

	private List<String> includes;
	private String chain;
	private boolean setToReader;
	private boolean setToWriter;
	private boolean isExcludes;

	public RemoveIncludesOrExcludesYMLAction(List<String> includes, String chain, boolean recursive,
			boolean setToReader, boolean setToWriter, boolean isExcludes, YAMLConfigurationProvider provider) {
		super(provider, recursive);
		this.includes = includes;
		this.chain = chain;
		this.setToReader = setToReader;
		this.setToWriter = setToWriter;
	}

	@Override
	public void doAction(JsonNode node) throws Exception {
		if (chain == null) {
			chain = "default";
		}

		if (node.has("chains")) {
			JsonNode chains = node.get("chains");
			if (chains.isArray()) {
				ArrayNode chainsArray = (ArrayNode) chains;
				int limit = chainsArray.size();
				JsonNode selectedChain = null;
				for (int i = 0; i < limit && selectedChain == null; i++) {
					JsonNode chainNode = chainsArray.get(i);
					if (chainNode.has("name")) {
						if (chainNode.get("name").asText().equals(chain)) {
							selectedChain = chainNode;
						}
					}
				}
				if (selectedChain != null) {
					if (setToReader) {
						JsonNode reader = null;
						if (selectedChain.has("reader")) {
							reader = selectedChain.get("reader");
						}
						if (reader != null) {
							removesIncludesOrExcludesList((ObjectNode) reader);
						}
					}
					if (setToWriter) {
						JsonNode writer = null;
						if (selectedChain.has("writer")) {
							writer = selectedChain.get("writer");
						}
						if (writer != null) {
							removesIncludesOrExcludesList((ObjectNode) writer);
						}
					}
				}
			}
			provider.write(node);
		}
	}

	private void removesIncludesOrExcludesList(ObjectNode node) {
		ArrayNode wildcardArray = null;

		ArrayNode result = new ArrayNode(provider.getObjectMapper().getNodeFactory());
		String label = "includes";
		if (isExcludes) {
			label = "excludes";
		}
		if (node.has(label)) {
			JsonNode wildcard = node.get(label);
			if (wildcard.isArray()) {
				wildcardArray = (ArrayNode) wildcard;
			}
		}
		if (wildcardArray != null) {
			int limit = wildcardArray.size();
			for (int i = 0; i < limit; i++) {
				String aux = wildcardArray.get(i).asText();
				if (!includes.contains(aux)) {
					result.add(wildcardArray.get(i));
				}
			}
		}
		if (result.size() > 0) {
			node.set(label, result);
		} else {
			node.remove(label);
		}

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {
		return new RemoveIncludesOrExcludesYMLAction(includes, chain, recursive, setToReader, setToWriter, isExcludes,
				(YAMLConfigurationProvider) provider);
	}

}
