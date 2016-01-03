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
import java.util.LinkedList;
import java.util.List;

import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.conf.providers.YAMLConfigurationProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class AddTransformationYMLAction extends AbstractYMLConfigurationAction {

   private String chain;
   private String path;
   private TransformationConfig transformationCfg;
   private Integer order;
   private String before;

   public AddTransformationYMLAction(String chain, String path, TransformationConfig transformationCfg,
         YAMLConfigurationProvider provider, boolean recursive, Integer order, String before) {
      super(provider, recursive);
      this.chain = chain;
      this.path = path;
      this.transformationCfg = transformationCfg;
      this.order = order;
      this.before = before;
   }

   @Override
   public void doAction(JsonNode chainsNode) throws Exception {

      ArrayNode transformationsNode = null;

      boolean isMultiModule = chainsNode.has("modules");
      ObjectMapper mapper = provider.getObjectMapper();
      if (!isMultiModule) {
         boolean validChainName = chain != null && !"".equals(chain) && !"default".equals(chain);
         if (!chainsNode.has("chains")) {
            if (chainsNode.has("transformations")) {
               JsonNode aux = chainsNode.get("transformations");
               if (aux.isArray()) {
                  transformationsNode = (ArrayNode) aux;
               }

               if (!validChainName) {
                  ObjectNode auxRoot = (ObjectNode) chainsNode;
                  if (transformationsNode == null) {
                     transformationsNode = new ArrayNode(mapper.getNodeFactory());
                  }
                  auxRoot.set("transformations", transformationsNode);
               } else {
                  // reset the root
                  chainsNode = new ObjectNode(mapper.getNodeFactory());
                  ObjectNode auxRoot = (ObjectNode) chainsNode;

                  // the default chain list added
                  ObjectNode chainObject = new ObjectNode(mapper.getNodeFactory());
                  chainObject.set("name", new TextNode("default"));
                  chainObject.set("transformations", transformationsNode);
                  ArrayNode chainsListNode = new ArrayNode(mapper.getNodeFactory());
                  

                  // the requested chain added
                  ObjectNode newChain = new ObjectNode(mapper.getNodeFactory());
                  newChain.set("name", new TextNode(chain));
                  if (path != null && !"".equals(path.trim())) {

                     ObjectNode readerNode = new ObjectNode(mapper.getNodeFactory());
                     newChain.set("reader", readerNode);

                     populateWriterReader(readerNode, path, null, null, null, null);

                     ObjectNode writerNode = new ObjectNode(mapper.getNodeFactory());
                     newChain.set("writer", writerNode);
                     populateWriterReader(writerNode, path, null, null, null, null);
                  }

                  transformationsNode = new ArrayNode(mapper.getNodeFactory());
                  newChain.set("transformations", transformationsNode);
                  
                  if(before == null || !"default".equals(before)){
                     chainsListNode.add(chainObject);
                  }
                  chainsListNode.add(newChain);
                  if(before != null  && "default".equals(before)){
                     chainsListNode.add(chainObject);
                  }
                  auxRoot.set("chains", chainsListNode);

               }
            } else {
               ObjectNode auxRoot = (ObjectNode) chainsNode;
               transformationsNode = new ArrayNode(mapper.getNodeFactory());
               boolean writeChainInfo = validChainName;
               if (!writeChainInfo) {
                  writeChainInfo = path != null && !"".equals(path.trim());
                  chain = "default";
               }
               if (writeChainInfo) {
                  ArrayNode auxChainsList = new ArrayNode(mapper.getNodeFactory());
                  ObjectNode aux = new ObjectNode(mapper.getNodeFactory());
                  auxChainsList.add(aux);
                  aux.set("name", new TextNode(chain));
                  if (path != null && !"".equals(path.trim())) {

                     ObjectNode readerNode = new ObjectNode(mapper.getNodeFactory());
                     aux.set("reader", readerNode);
                     populateWriterReader(readerNode, path, null, null, null, null);

                  }
                  auxRoot.set("chains", auxChainsList);
                  if (path != null && !"".equals(path.trim())) {

                     ObjectNode writerNode = new ObjectNode(mapper.getNodeFactory());
                     aux.set("writer", writerNode);
                     populateWriterReader(writerNode, path, null, null, null, null);
                  }

                  auxRoot = aux;
               }
               auxRoot.set("transformations", transformationsNode);
            }

         } else {
            if (validChainName) {
               JsonNode aux = chainsNode.get("chains");
               boolean found = false;
               if (aux.isArray()) {
                  Iterator<JsonNode> it = aux.elements();
                  while (it.hasNext()) {
                     JsonNode next = it.next();
                     if (next.has("name")) {
                        String id = next.get("name").asText();
                        if (chain.equals(id)) {
                           found = true;
                           if (next.has("transformations")) {
                              JsonNode auxTrans = next.get("transformations");
                              if (auxTrans.isArray()) {
                                 transformationsNode = (ArrayNode) auxTrans;
                              } else {
                                 throw new Exception(
                                       "The chain [" + chain + "] does not have a valid transformations node");
                              }
                           } else if (next.isObject()) {
                              ObjectNode auxNext = (ObjectNode) next;
                              transformationsNode = new ArrayNode(mapper.getNodeFactory());
                              auxNext.set("transformations", transformationsNode);
                           } else {
                              throw new Exception("The chain [" + chain + "] does not have a valid structure");
                           }
                        }
                     }

                  }
                  if (!found) {
                     ChainConfig chainCfg = new ChainConfigImpl();
                     chainCfg.setName(chain);
                     WalkerConfig walkerCfg = new WalkerConfigImpl();
                     List<TransformationConfig> transfs = new LinkedList<TransformationConfig>();
                     transfs.add(transformationCfg);
                     walkerCfg.setTransformations(transfs);
                     chainCfg.setWalkerConfig(walkerCfg);
                     provider.addChainConfig(chainCfg, false, before);
                     return;
                  }
               }
            } else {

               ObjectNode node = new ObjectNode(mapper.getNodeFactory());
               node.set("name", new TextNode(chain));
               ArrayNode transNodes = new ArrayNode(mapper.getNodeFactory());

               node.set("transformations", transNodes);
               ArrayNode array = (ArrayNode) chainsNode.get("chains");
               array.add(node);

               ObjectNode transformationNode = new ObjectNode(mapper.getNodeFactory());
               transNodes.add(transformationNode);

               createTransformation(transformationNode, transformationCfg);

               return;
            }
         }
         if (transformationsNode != null) {
            ObjectNode transformationNode = new ObjectNode(mapper.getNodeFactory());
            
            if(order != null && order < transformationsNode.size()){
               transformationsNode.insert(order, transformationNode);
            }
            else{
               transformationsNode.add(transformationNode);
            }

            createTransformation(transformationNode, transformationCfg);
            provider.write(chainsNode);
            return;
         } else if (chain != null) {
            throw new Exception("The chain [" + chain + "] does not exists");
         }
      }

   }

   @Override
   public AbstractYMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

      return new AddTransformationYMLAction(chain, path, transformationCfg, (YAMLConfigurationProvider) provider,
            recursive, order, before);
   }

}
