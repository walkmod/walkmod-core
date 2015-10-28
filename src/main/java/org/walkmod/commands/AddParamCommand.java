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

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Sets an specific parameter value in an specific bean (transformation, reader, writer or walker)")
public class AddParamCommand implements Command {

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	@Parameter(names = { "--param", "-p" }, description = "The parameter name to set", required = true)
	private String param;

	@Parameter(names = { "--value", "-v" }, description = "The parameter value to set", required = true)
	private String value;

	@Parameter(names = { "--type", "-t" }, description = "The bean type to set the parameter. If there are multiple beans with this type, all will contain the parameter", required = true)
	private String type;

	@Parameter(names = { "--category", "-c" }, description = "The bean category to set the parameter. If there are multiple beans with this category, all will contain the parameter ")
	private String category;

	@Parameter(names = { "--name", "-n" }, description = "The bean identifier for a transformation to set the parameter")
	private String name;

	@Parameter(names = { "--chain" }, description = "The chain to select the bean type")
	private String chain;

	private JCommander command;

	@Parameter(names = { "--recursive", "-R" }, description = "Apply the param to all submodules")
	private boolean recursive = false;

	public AddParamCommand(JCommander command) {
		this.command = command;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("add-param");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.addConfigurationParameter(param, value, type, category, name, chain, recursive);
		}
	}

}
