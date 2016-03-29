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

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Sets an specific writer for an specific chain.")
public class SetWriterCommand implements Command {

   @Parameter(arity = 1, description = "The writer type identifier", required = false)
   public List<String> writerType;

   @Parameter(names = "--help", help = true, hidden = true)
   private boolean help;

   private JCommander jcommander;

   @Parameter(names = { "--chain" }, description = "The chain identifier", required = false)
   private String chain = "default";

   @Parameter(names = { "--path", "-d" }, description = "The reader path", required = false)
   private String path = "src/main/java";

   @Parameter(names = { "--recursive", "-R" }, description = "Removes the transformation to all submodules")
   private boolean recursive = false;

   @Parameter(names = { "-e",
         "--verbose" }, description = "Prints the stacktrace of the produced error during the execution")
   private Boolean printErrors = false;

   @DynamicParameter(names = "-D", description = "Dynamic transformation parameters go here")
   private Map<String, String> params = new HashMap<String, String>();

   public SetWriterCommand(JCommander jcommander) {
      this.jcommander = jcommander;
   }

   public SetWriterCommand(String writerType, String chain, Map<String, String> params) {
      this.chain = chain;
      this.writerType = new LinkedList<String>();
      this.writerType.add(writerType);
      this.params = params;
   }

   @Override
   public void execute() throws Exception {
      if (help) {
         jcommander.usage("set-writer");
      } else {
         WalkModFacade facade = new WalkModFacade(OptionsBuilder.options().printErrors(printErrors));
         if(writerType != null){
            facade.setWriter(chain, writerType.get(0), path, recursive, params);
         }
         else{
            facade.setWriter(chain, null, path, recursive, params);
         }
      }
   }

}
