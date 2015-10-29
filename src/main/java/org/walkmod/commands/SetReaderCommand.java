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

import java.util.LinkedList;
import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Sets an specific reader for an specific chain.")
public class SetReaderCommand implements Command{

	@Parameter(arity = 1, description = "The reader type identifier", required = false)
	public List<String> readerType;

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander jcommander;

	@Parameter(names = { "--chain" }, description = "The chain identifier", required = false)
	private String chain = "default";
	
	@Parameter(names = { "--path", "-d"}, description = "The reader path", required = false)
	private String path = "src/main/java";

	public SetReaderCommand(JCommander jcommander) {
		this.jcommander = jcommander;
	}

	public SetReaderCommand(String readerType, String chain) {
		this.chain = chain;
		this.readerType = new LinkedList<String>();
		this.readerType.add(readerType);
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			jcommander.usage("set-reader");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.setReader(chain, readerType.get(0), path);
		}
	}

}
