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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.TransformationConfig;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Shows the list of chains with its code transformations.")
public class PrintChainsCommand implements Command {

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	public PrintChainsCommand(JCommander jcommander) {
		this.command = jcommander;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("chains");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			Configuration cfg = facade.getConfiguration();
			if (cfg != null) {
				Collection<ChainConfig> chains = cfg.getChainConfigs();
				if (chains != null) {
					String line = "";

					for (int i = 0; i < 2 + 33 + 33 + 33 + 52; i++) {
						line = line + "-";
					}
					System.out.println(line);
					System.out.printf("| %30s | %30s | %30s | %50s |%n", StringUtils.center("CHAIN", 30),
							StringUtils.center("READER PATH", 30), StringUtils.center("WRITER PATH", 30),
							StringUtils.center("TRANSFORMATIONS", 50));
					System.out.println(line);
					for (ChainConfig cc : chains) {
						System.out.printf("| %30s | %30s | %30s | %50s |%n", "", "", "", "");
						List<TransformationConfig> transformations = cc.getWalkerConfig().getTransformations();
						Iterator<TransformationConfig> it = transformations.iterator();
						int i = 0;
						while (it.hasNext()) {
							TransformationConfig next = it.next();
							if (i == 0) {
								System.out.printf("| %30s | %30s | %30s | %50s |%n",
										StringUtils.center(cc.getName(), 30),
										StringUtils.center(cc.getReaderConfig().getPath(), 30),
										StringUtils.center(cc.getWriterConfig().getPath(), 30),
										StringUtils.center("- " + next.getType(), 50));
							} else {
								System.out.printf("| %30s | %30s | %30s | %50s |%n", "", "", "",
										StringUtils.center("- " + next.getType(), 50));
							}
							i++;

						}

						System.out.printf("| %30s | %30s | %30s | %50s |%n", "", "", "", "");
						System.out.println(line);
					}

				}
			}
		}
	}

}
