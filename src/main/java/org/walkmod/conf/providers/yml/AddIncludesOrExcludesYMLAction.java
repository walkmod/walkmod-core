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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class AddIncludesOrExcludesYMLAction extends AbstractYMLConfigurationAction {

	private List<String> includes;
	private String chain;
	private boolean setToReader;
	private boolean setToWriter;
	private boolean isExcludes;

	public AddIncludesOrExcludesYMLAction(List<String> includes, String chain, boolean recursive, boolean setToReader,
			boolean setToWriter, boolean isExcludes, YAMLConfigurationProvider provider) {
		super(provider, recursive);
		this.includes = includes;
		this.chain = chain;
		this.setToReader = setToReader;
		this.setToWriter = setToWriter;
		this.isExcludes = isExcludes;
	}

	private void setIncludesOrExcludesList(ObjectNode node) {

		ArrayNode wildcardArray = null;
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
		if (wildcardArray == null) {
			wildcardArray = new ArrayNode(provider.getObjectMapper().getNodeFactory());
			node.set(label, wildcardArray);
		}
		for (String includesItem : includes) {
			wildcardArray.add(includesItem);
		}
	}

	@Override
	public void doAction(JsonNode node) throws Exception {
		if (chain == null) {
			chain = "default";
		}
		ObjectMapper mapper = provider.getObjectMapper();
		if (node.has("chains")) {
			JsonNode chains = node.get("chains");
			if (chains.isArray()) {
				ArrayNode chainsArray = (ArrayNode) chains;
				int limit = chainsArray.size();
				ObjectNode selectedChain = null;
				for (int i = 0; i < limit && selectedChain == null; i++) {
					JsonNode chainNode = chainsArray.get(i);
					if (chainNode.has("name")) {
						if (chainNode.get("name").asText().equals(chain)) {
							selectedChain = (ObjectNode) chainNode;
						}
					}
				}
				if (selectedChain == null) {
					selectedChain = new ObjectNode(mapper.getNodeFactory());
					selectedChain.set("name", new TextNode(chain));
					chainsArray.add(selectedChain);
				}

				if (setToReader) {
					JsonNode reader = null;
					if (selectedChain.has("reader")) {
						reader = selectedChain.get("reader");
					} else {
						reader = new ObjectNode(mapper.getNodeFactory());
						selectedChain.set("reader", reader);
					}
					setIncludesOrExcludesList((ObjectNode) reader);

				}
				if (setToWriter) {
					JsonNode reader = null;
					if (selectedChain.has("writer")) {
						reader = selectedChain.get("writer");
					} else {
						reader = new ObjectNode(mapper.getNodeFactory());
						selectedChain.set("writer", reader);
					}
					setIncludesOrExcludesList((ObjectNode) reader);
				}

			}
		} else {
			ObjectNode root = (ObjectNode) node;
			if (node.has("transformations")) {
				JsonNode transformations = node.get("transformations");

				root.remove("transformations");
				ObjectNode chainNode = new ObjectNode(mapper.getNodeFactory());
				chainNode.set("name", new TextNode("default"));
				chainNode.set("transformations", transformations);
				ArrayNode chains = new ArrayNode(mapper.getNodeFactory());
				chains.add(chainNode);
				if (!chain.equals("default")) {
					chainNode = new ObjectNode(mapper.getNodeFactory());
					chainNode.set("name", new TextNode(chain));
					chains.add(chainNode);
				}
				ObjectNode reader = new ObjectNode(mapper.getNodeFactory());
				setIncludesOrExcludesList(reader);

				chainNode.set("reader", reader);
				ObjectNode writer = new ObjectNode(mapper.getNodeFactory());
				setIncludesOrExcludesList(writer);
				chainNode.set("writer", writer);

				root.set("chains", chains);
			} else if (!node.has("modules")) {
				ObjectNode chainNode = new ObjectNode(mapper.getNodeFactory());
				chainNode.set("name", new TextNode("default"));
				ArrayNode chains = new ArrayNode(mapper.getNodeFactory());
				chains.add(chainNode);

				ObjectNode reader = new ObjectNode(mapper.getNodeFactory());
				setIncludesOrExcludesList(reader);

				chainNode.set("reader", reader);
				ObjectNode writer = new ObjectNode(mapper.getNodeFactory());
				setIncludesOrExcludesList(writer);
				chainNode.set("writer", writer);

				root.set("chains", chains);
			}

		}
		provider.write(node);
	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new AddIncludesOrExcludesYMLAction(includes, chain, recursive, setToReader, setToWriter, isExcludes,
				(YAMLConfigurationProvider) provider);
	}

}
