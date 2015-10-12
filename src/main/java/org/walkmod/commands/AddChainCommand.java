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
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.JSONConfigParser;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.conf.entities.impl.WriterConfigImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;

@Parameters(separators = "=", commandDescription = "Creates a chain/group of transformations for an specific source directory.")
public class AddChainCommand implements Command {

	@Parameter(names = "--name", description = "The chain identifier")
	private String name = null;

	@Parameter(names = { "-d", "--path", "--reader.path" }, description = "Source code location.")
	private String readerPath = DEFAULT_PATH;

	private static final String DEFAULT_PATH = "src/main/java";

	@Parameter(names = "--reader", description = "Reader JSON object definition", converter = JSONConverter.class)
	private JsonNode reader = null;

	@Parameter(names = "--writer", description = "Writer JSON object definition", converter = JSONConverter.class)
	private JsonNode writer = null;

	@Parameter(names = "--walker", description = "Walker JSON object definition", converter = JSONConverter.class)
	private JsonNode walker = null;

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	private JSONConfigParser parser = new JSONConfigParser();

	public AddChainCommand(JCommander command) {
		this.command = command;
	}

	public AddChainCommand(String name, String readerPath, JsonNode reader, JsonNode writer, JsonNode walker) {
		this.name = name;
		this.readerPath = readerPath;
		this.reader = reader;
		this.writer = writer;
		this.walker = walker;
	}

	public ChainConfig buildChainCfg() throws Exception {
		ChainConfig chainCfg = new ChainConfigImpl();
		chainCfg.setName(name);

		if (reader != null) {

			chainCfg.setReaderConfig(parser.getReader(reader));
		} else {
			if (!readerPath.equals(DEFAULT_PATH)) {
				ReaderConfig readerCfg = new ReaderConfig();
				readerCfg.setPath(readerPath);
				chainCfg.setReaderConfig(readerCfg);
			}
		}
		if (writer != null) {
			chainCfg.setWriterConfig(parser.getWriter(writer));
		} else {
			if (!readerPath.equals(DEFAULT_PATH)) {
				WriterConfig writerCfg = new WriterConfigImpl();
				writerCfg.setPath(readerPath);
				chainCfg.setWriterConfig(writerCfg);
			}
		}

		if (walker != null) {

			chainCfg.setWalkerConfig(parser.getWalker(walker));
		} else {
			chainCfg.setWalkerConfig(new WalkerConfigImpl());
		}

		return chainCfg;

	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("add-plugin");
		} else {

			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.addChainConfig(buildChainCfg());
		}
	}
}
