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

import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Removes include rules from the configuration.")
public class RemoveIncludesCommand implements Command {

	@Parameter(description = "List of excludes to remove", required = true)
	private List<String> includes;

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander jcommander;

	@Parameter(names = { "-e", "--verbose" }, description = "Prints the stacktrace of the produced error during the execution")
	private Boolean printErrors = false;

	@Parameter(names = { "--chain" }, description = "The chain name")
	private String chain = "default";

	@Parameter(names = { "--setToReader" }, description = "If the filter is applied in the reader")
	private boolean setToReader = true;

	@Parameter(names = { "--setToWriter" }, description = "If the filter is applied in the writer")
	private boolean setToWriter = false;

	@Parameter(names = { "--recursive", "-R" }, description = "Adds the transformation to all submodules")
	private boolean recursive = false;

	public RemoveIncludesCommand(JCommander jcommander) {
		this.jcommander = jcommander;
	}

	public RemoveIncludesCommand(List<String> includes, String chain, boolean setToReader, boolean setToWriter) {
		this.includes = includes;
		this.chain = chain;
		this.setToReader = setToReader;
		this.setToWriter = setToWriter;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			jcommander.usage("rm-includes");
		} else {

			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options().printErrors(printErrors));
			facade.removeIncludesToChain(chain, includes, recursive, setToReader, setToWriter);
		}
	}

}
