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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.impl.ProviderConfigImpl;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Adds a configuration provider.")
public class AddCfgProviderCommand implements Command {

   @Parameter(arity = 1, description = "The configuration provider type identifier", required = true)
   private List<String> type;

   @DynamicParameter(names = "-D", description = "Dynamic transformation parameters go here")
   private Map<String, String> params = new HashMap<String, String>();

   private JCommander command;

   @Parameter(names = "--help", help = true, hidden = true)
   private boolean help;

   @Parameter(names = { "--recursive", "-R" }, description = "Adds the provider to all submodules")
   private boolean recursive = false;

   @Parameter(names = { "-e",
         "--verbose" }, description = "Prints the stacktrace of the produced error during the execution")
   private Boolean printErrors = false;

   public AddCfgProviderCommand(JCommander command) {
      this.command = command;
   }

   public AddCfgProviderCommand(String type, Map<String, String> params) {
      this.type = new LinkedList<String>();
      this.type.add(type);
      this.params = params;
   }

   public ProviderConfig build() throws Exception {
      ProviderConfig prov = new ProviderConfigImpl();

      prov.setType(type.get(0));

      if (params != null) {

         prov.setParameters(new HashMap<String, Object>(params));
      }

      return prov;
   }

   @Override
   public void execute() throws Exception {
      if (help) {
         command.usage("add-provider");
      } else {

         WalkModFacade facade = new WalkModFacade(OptionsBuilder.options().printErrors(printErrors));
         facade.addProviderConfig(build(), recursive);
      }

   }

}
