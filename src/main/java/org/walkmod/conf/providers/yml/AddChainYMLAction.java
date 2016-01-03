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
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class AddChainYMLAction extends AbstractYMLConfigurationAction {

	private ChainConfig chainCfg;
	private String before;

	public AddChainYMLAction(ChainConfig chainCfg, YAMLConfigurationProvider provider, boolean recursive, String before) {
		super(provider, recursive);
		this.chainCfg = chainCfg;
		this.before = before;
	}

	@Override
	public void doAction(JsonNode chainsNode) throws Exception {
		ArrayNode chainsList = null;
		ObjectMapper mapper = provider.getObjectMapper();

		if (chainsNode != null) {
			if (!chainsNode.has("chains")) {
				chainsList = new ArrayNode(mapper.getNodeFactory());
				if (chainsNode.isObject()) {
					ObjectNode aux = (ObjectNode) chainsNode;
					aux.set("chains", chainsList);
				} else {
					throw new TransformerException("The root element is not a JSON node");
				}
			} else {
				JsonNode aux = chainsNode.get("chains");
				if (aux.isArray()) {
					chainsList = (ArrayNode) chainsNode.get("chains");
				} else {
					throw new TransformerException("The plugins element is not a valid array");
				}
			}
		}
		ObjectNode chainNode = new ObjectNode(mapper.getNodeFactory());
		ReaderConfig readerCfg = chainCfg.getReaderConfig();
		if (readerCfg != null) {
			if (chainsNode == null) {
				chainsNode = new ObjectNode(mapper.getNodeFactory());
				ObjectNode aux = (ObjectNode) chainsNode;
				chainsList = new ArrayNode(mapper.getNodeFactory());
				aux.set("chains", chainsList);
			}
			ObjectNode readerNode = new ObjectNode(mapper.getNodeFactory());
			chainNode.set("reader", readerNode);
			populateWriterReader(readerNode, readerCfg.getPath(), readerCfg.getType(), readerCfg.getIncludes(),
					readerCfg.getExcludes(), readerCfg.getParameters());

		} else {
			provider.addDefaultReaderConfig(chainCfg);
		}

		WalkerConfig walkerCfg = chainCfg.getWalkerConfig();
		if (walkerCfg != null) {

			ObjectNode walkerNode = null;

			String type = walkerCfg.getType();
			if (type != null) {
				if (chainsNode == null) {
					chainsNode = new ObjectNode(mapper.getNodeFactory());
					ObjectNode aux = (ObjectNode) chainsNode;
					chainsList = new ArrayNode(mapper.getNodeFactory());
					aux.set("chains", chainsList);
				}
				walkerNode = new ObjectNode(mapper.getNodeFactory());
				chainNode.set("walker", walkerNode);
				walkerNode.set("type", new TextNode(type));
			}

			Map<String, Object> wparams = walkerCfg.getParams();
			if (wparams != null && !wparams.isEmpty()) {
				if (walkerNode == null) {
					if (chainsNode == null) {
						chainsNode = new ObjectNode(mapper.getNodeFactory());
						ObjectNode aux = (ObjectNode) chainsNode;
						chainsList = new ArrayNode(mapper.getNodeFactory());
						aux.set("chains", chainsList);
					}
					walkerNode = new ObjectNode(mapper.getNodeFactory());
					chainNode.set("walker", walkerNode);
				}
				populateParams(walkerNode, wparams);
			}

			String rootNamespace = walkerCfg.getRootNamespace();
			if (rootNamespace != null) {
				if (walkerNode == null) {
					if (chainsNode == null) {
						chainsNode = new ObjectNode(mapper.getNodeFactory());
						ObjectNode aux = (ObjectNode) chainsNode;
						chainsList = new ArrayNode(mapper.getNodeFactory());
						aux.set("chains", chainsList);
					}
					walkerNode = new ObjectNode(mapper.getNodeFactory());
					chainNode.set("walker", walkerNode);
				}
				walkerNode.set("root-namespace", new TextNode(rootNamespace));
			}

			List<TransformationConfig> transformationList = walkerCfg.getTransformations();
			if (transformationList != null && !transformationList.isEmpty()) {
				ArrayNode transformationListNode = new ArrayNode(mapper.getNodeFactory());
				if (walkerNode == null) {
					if (chainsNode == null) {
						ObjectNode aux = new ObjectNode(mapper.getNodeFactory());
						aux.set("transformations", transformationListNode);
						chainsNode = aux;
					} else {
						chainNode.set("transformations", transformationListNode);
					}
				} else {
					walkerNode.set("transformations", transformationListNode);
				}
				for (TransformationConfig transCfg : transformationList) {
					ObjectNode transformationNode = new ObjectNode(mapper.getNodeFactory());
					transformationListNode.add(transformationNode);
					createTransformation(transformationNode, transCfg);
				}

			}

		}

		WriterConfig writerCfg = chainCfg.getWriterConfig();
		if (writerCfg != null) {
			if (chainsNode == null) {
				chainsNode = new ObjectNode(mapper.getNodeFactory());
				ObjectNode aux = (ObjectNode) chainsNode;
				chainsList = new ArrayNode(mapper.getNodeFactory());
				aux.set("chains", chainsList);
			}
			ObjectNode writerNode = new ObjectNode(mapper.getNodeFactory());
			chainNode.set("writer", writerNode);
			populateWriterReader(writerNode, writerCfg.getPath(), writerCfg.getType(), writerCfg.getIncludes(),
					writerCfg.getExcludes(), writerCfg.getParams());

		} else {
			provider.addDefaultWriterConfig(chainCfg);
		}
		if (chainsList != null) {
		   int beforePos = -1;
		   if(before != null){
		      Iterator<JsonNode> it = chainsList.iterator();
		      int i = 0;
		      while(it.hasNext() && beforePos == -1){
		         JsonNode next = it.next();
		         if(next.get("name").equals(before)){
		            beforePos = i;
		         }
		         i++;
		      }
		   }
		   if(beforePos == -1){
		      chainsList.add(chainNode);
		   }
		   else{
		      chainsList.insert(beforePos, chainNode);
		   }
		}
		if (readerCfg != null || walkerCfg != null || writerCfg != null) {
			provider.write(chainsNode);
		}

	}

	@Override
	public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

		return new AddChainYMLAction(chainCfg, (YAMLConfigurationProvider) provider, recursive, before);
	}

}
