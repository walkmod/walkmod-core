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
package org.walkmod.conf.providers.xml;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.ConfigurationImpl;
import org.walkmod.conf.providers.XMLConfigurationProvider;

public class AddIncludesOrExcludesXMLAction extends AbstractXMLConfigurationAction {

   private List<String> includes;
   private String chain;
   private boolean setToReader;
   private boolean setToWriter;
   private boolean isExcludes;

   public AddIncludesOrExcludesXMLAction(List<String> includes, String chain, boolean recursive, boolean setToReader,
         boolean setToWriter, boolean isExcludes, XMLConfigurationProvider provider) {
      super(provider, recursive);
      this.includes = includes;
      this.setToReader = setToReader;
      this.setToWriter = setToWriter;
      this.chain = chain;
      this.isExcludes = isExcludes;
   }

   private void updateElement(Element element) {
      String label = "include";
      if (isExcludes) {
         label = "exclude";
      }
      Document document = provider.getDocument();
      String path = element.getAttribute("path");
      int limit = path.length();
      if (!path.endsWith("/")) {
         limit++;
      }
      for (String include : includes) {
         Element includeElem = document.createElement(label);
         if (include.startsWith(path)) {

            include = include.substring(limit);

         }
         includeElem.setAttribute("wildcard", include);
         element.appendChild(includeElem);
      }
   }

   @Override
   public void doAction() throws Exception {
      Document document = provider.getDocument();
      Element rootElement = document.getDocumentElement();
      NodeList children = rootElement.getChildNodes();
      int childSize = children.getLength();
      boolean chainFound = false;
      boolean containsChains = false;

      if (chain == null) {
         chain = "default";
      }

      for (int i = 0; i < childSize; i++) {
         Node childNode = children.item(i);
         if (childNode instanceof Element) {
            Element child = (Element) childNode;
            final String nodeName = child.getNodeName();
            String writerPath = "src/main/java";
            if ("chain".equals(nodeName)) {
               containsChains = true;
               String name = child.getAttribute("name");
               if (name.equals(chain)) {
                  chainFound = true;
                  NodeList chainChildren = child.getChildNodes();
                  int chainChildrenSize = chainChildren.getLength();
                  boolean existsReader = false;
                  boolean existsWriter = false;
                  for (int j = 0; j < chainChildrenSize; j++) {
                     Node chainChild = chainChildren.item(j);
                     if (chainChild instanceof Element) {
                        Element elementChain = (Element) chainChild;
                        String elementName = elementChain.getNodeName();
                        if (elementName.equals("reader") && setToReader) {
                           existsReader = true;
                           updateElement(elementChain);
                           writerPath = elementChain.getAttribute("path");
                        } else if (elementName.equals("writer") && setToWriter) {
                           existsWriter = true;
                           updateElement(elementChain);
                        }
                     }
                  }
                  if (!existsReader && setToReader) {
                     Element reader = document.createElement("reader");
                     reader.setAttribute("path", "src/main/java");
                     updateElement(reader);
                     if (chainChildrenSize == 0) {
                        child.appendChild(reader);
                     } else {
                        child.insertBefore(reader, chainChildren.item(0));
                     }
                  }
                  if (!existsWriter && setToWriter) {
                     Element writer = document.createElement("writer");

                     writer.setAttribute("path", writerPath);
                     updateElement(writer);
                     child.appendChild(writer);
                  }
               }
            }
         }
      }
      if (!chainFound) {
         ChainConfig chainCfg = null;
         if (!containsChains) {

            Configuration configuration = new ConfigurationImpl();
            provider.setConfiguration(configuration);
            // we write specifically a default chain, and
            // afterwards, we
            // add the requested one.
            provider.loadChains();
            Collection<ChainConfig> chainCfgs = configuration.getChainConfigs();
            chainCfg = chainCfgs.iterator().next();
            NodeList child = rootElement.getChildNodes();
            int limit = child.getLength();
            for (int i = 0; i < limit; i++) {
               Node item = child.item(i);
               if (item instanceof Element) {
                  Element auxElem = (Element) item;
                  if (auxElem.getNodeName().equals("transformation")) {
                     rootElement.removeChild(auxElem);
                  }
               }
            }

            if (!chain.equals("default")) {
               rootElement.appendChild(createChainElement(chainCfg));
               chainCfg = new ChainConfigImpl();
               chainCfg.setName(chain);
               provider.addDefaultReaderConfig(chainCfg);
               provider.addDefaultWriterConfig(chainCfg);
               provider.addDefaultWalker(chainCfg);
            }
         } else {
            chainCfg = new ChainConfigImpl();
            chainCfg.setName(chain);
            provider.addDefaultReaderConfig(chainCfg);
            provider.addDefaultWriterConfig(chainCfg);
            provider.addDefaultWalker(chainCfg);
         }
         if (setToReader) {
            ReaderConfig rcfg = chainCfg.getReaderConfig();
            String[] aux = new String[includes.size()];
            rcfg.setIncludes(includes.toArray(aux));
            chainCfg.setReaderConfig(rcfg);
         }
         if (setToWriter) {
            WriterConfig wcfg = chainCfg.getWriterConfig();
            String[] aux = new String[includes.size()];
            wcfg.setIncludes(includes.toArray(aux));
            chainCfg.setWriterConfig(wcfg);
         }
         rootElement.appendChild(createChainElement(chainCfg));
      }

      provider.persist();

   }

   @Override
   public AbstractXMLConfigurationAction clone(ConfigurationProvider provider, boolean recursive) {

      return new AddIncludesOrExcludesXMLAction(includes, chain, recursive, setToReader, setToWriter, isExcludes,
            (XMLConfigurationProvider) provider);
   }

}
