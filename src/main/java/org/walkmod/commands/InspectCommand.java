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

import java.util.Arrays;
import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.BeanDefinition;
import org.walkmod.conf.entities.PropertyDefinition;
import org.walkmod.conf.entities.impl.PluginConfigImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.vandermeer.asciitable.v2.V2_AsciiTable;

@Parameters(separators = "=", commandDescription = "Prints the components of a walkmod plugin")
public class InspectCommand implements Command, AsciiTableAware {

   @Parameter(names = "--help", help = true, hidden = true)
   private boolean help;

   private JCommander command;

   @Parameter(description = "The plugin id to inspect (e.g imports-cleaner)")
   private List<String> pluginId;

   @Parameter(names = "--offline", description = "Resolves the walkmod plugins and their dependencies in offline mode")
   private boolean offline = false;

   private V2_AsciiTable at = null;

   public InspectCommand(JCommander command) {
      this.command = command;
   }

   @Override
   public void execute() throws Exception {
      if (help) {
         command.usage("inspect");
      } else {
         WalkModFacade facade = new WalkModFacade(OptionsBuilder.options().offline(offline));
         List<BeanDefinition> beans = facade.inspectPlugin(new PluginConfigImpl(pluginId.get(0)));

         List<String> validTypesList = Arrays.asList("String", "JSONObject", "JSONArray");
         if (beans != null) {

            at = new V2_AsciiTable();
            at.addRule();
            at.addRow("TYPE NAME (ID)", "CATEGORY", "PROPERTIES", "DESCRIPTION");
            at.addStrongRule();

            for (BeanDefinition bean : beans) {
               List<PropertyDefinition> properties = bean.getProperties();
               if (properties == null || properties.isEmpty()) {
                  at.addRow(bean.getType(), bean.getCategory(), "", bean.getDescription());
               } else {
                  int i = 0;
                  for (PropertyDefinition pd : properties) {
                     if (validTypesList.contains(pd.getType())) {
                        String label = pd.getName() + ":" + pd.getType();
                        if (pd.getDefaultValue() != null && pd.getDefaultValue().length() != 0) {
                           label = label + " (" + pd.getDefaultValue() + ")";
                        }
                        if (i == 0) {
                           at.addRow(bean.getType(), bean.getCategory(), label, bean.getDescription());
                        } else {
                           at.addRow("", "", label, "");
                        }
                        i++;
                     }
                  }
               }
               at.addRule();

            }

         }
      }
   }

   @Override
   public V2_AsciiTable getTable() {
      return at;
   }

}
