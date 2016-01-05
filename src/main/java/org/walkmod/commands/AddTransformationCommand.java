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
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.impl.TransformationConfigImpl;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Adds a code transformation/convention into a chain.")
public class AddTransformationCommand implements Command {

   @Parameter(names = "--merge-policy", description = "Merge policy to apply after executing the transformation")
   private String mergePolicy = null;

   @Parameter(names = { "--chain" }, description = "The chain name")
   private String chain = "default";

   @Parameter(names = { "--isMergeable", "--merge",
         "-m" }, description = "Sets if the changes made by the transformation requires to be merged")
   private boolean isMergeable = false;

   @Parameter(names = { "--path", "-d" }, description = "Source directory when the specified chain does not exists")
   private String path = null;

   @Parameter(names = { "--name", "--alias", "-n" }, description = "Alias to identify the transformation")
   private String name = null;

   @Parameter(arity = 1, description = "The transformation type identifier", required = true)
   private List<String> type = null;

   @Parameter(names = "--help", help = true, hidden = true)
   private boolean help;

   @Parameter(names = { "--order",
         "-o" }, description = "The execution order >= 0 of the the transformation. Zero (0) is the most prioritary.")
   private Integer order = null;

   @Parameter(names = { "--before",
         "-b" }, description = "Selects the execution order of the chain. The selected value determines which is the chain to execute.")
   private String before = null;

   @Parameter(names = { "--recursive", "-R" }, description = "Adds the transformation to all submodules")
   private boolean recursive = false;

   @Parameter(names = { "-e",
         "--verbose" }, description = "Prints the stacktrace of the produced error during the execution")
   private Boolean printErrors = false;

   private JCommander jcommander;

   @DynamicParameter(names = "-D", description = "Dynamic transformation parameters go here")
   private Map<String, String> params = new HashMap<String, String>();

   public AddTransformationCommand(String type, String chain, boolean isMergeable, String mergePolicy, String path,
         String name, Map<String, String> params, boolean recursive) {
      this(type, chain, isMergeable, mergePolicy, path, name, params, recursive, null, null);
   }

   public AddTransformationCommand(String type, String chain, boolean isMergeable, String mergePolicy, String path,
         String name, Map<String, String> params, boolean recursive, Integer order, String before) {
      this.type = new LinkedList<String>();
      this.type.add(type);
      this.chain = chain;
      this.isMergeable = isMergeable;
      this.mergePolicy = mergePolicy;
      this.params = params;
      this.path = path;
      this.recursive = recursive;
      this.name = name;
      this.order = order;
      this.before = before;
   }

   public AddTransformationCommand(JCommander jcommander) {
      this.jcommander = jcommander;
   }

   public TransformationConfig buildTransformationCfg() {
      TransformationConfig tconfig = new TransformationConfigImpl();
      tconfig.setType(type.get(0));
      tconfig.isMergeable(isMergeable);
      tconfig.setMergePolicy(mergePolicy);
      tconfig.setName(name);
      if (params != null) {
         tconfig.setParameters(new HashMap<String, Object>(params));
      }
      return tconfig;
   }

   @Override
   public void execute() throws Exception {
      if (help) {
         jcommander.usage("add");
      } else {

         WalkModFacade facade = new WalkModFacade(OptionsBuilder.options().printErrors(printErrors));
         facade.addTransformationConfig(chain, path, recursive, buildTransformationCfg(), order, before);
      }
   }

}
