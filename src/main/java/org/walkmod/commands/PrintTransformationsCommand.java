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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.TransformationConfig;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.vandermeer.asciitable.v2.V2_AsciiTable;

@Parameters(separators = "=", commandDescription = "Shows the list of transformations with its params for a chain.")
public class PrintTransformationsCommand implements Command, AsciiTableAware {

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	private V2_AsciiTable at = null;

	@Parameter(description = "The chain name (identifier)", required = false)
	private List<String> chain;
	
	private static Logger log = Logger.getLogger(PrintTransformationsCommand.class);

	public PrintTransformationsCommand(JCommander command) {
		this.command = command;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("transformations");
		} else {
			if (chain == null) {
				chain = new LinkedList<String>();
			}
			if (chain.isEmpty()) {

				chain.add("default");
			}
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			Configuration cfg = facade.getConfiguration();
			if (cfg != null) {
				Collection<ChainConfig> chains = cfg.getChainConfigs();
				if (chains != null) {
					Iterator<ChainConfig> it = chains.iterator();
					ChainConfig selected = null;
					while (it.hasNext() && selected == null) {
						ChainConfig current = it.next();
						if (current.getName().equals(chain.get(0))) {
							selected = current;
						}
					}
					if (selected != null) {
						at = new V2_AsciiTable();
						at.addRule();
						at.addRow("TRANSFORMATION TYPE", "PARAMETERS", "NAME/ALIAS");
						at.addStrongRule();

						List<TransformationConfig> transformations = selected.getWalkerConfig().getTransformations();
						if (transformations != null) {
							for (TransformationConfig transf : transformations) {
								Map<String, Object> parameters = transf.getParameters();
								if (parameters == null || parameters.isEmpty()) {
									at.addRow(transf.getType(), "", transf.getName());
								} else {
									Set<String> keys = parameters.keySet();
									int i = 0;
									for (String key : keys) {
										if (i == 0) {
											String name = transf.getName();
											if (name == null) {
												name = "";
											}
											at.addRow(transf.getType(), "-" + key + ":" + parameters.get(key), "");
										} else {
											at.addRow("", "-" + key + ":" + parameters.get(key), "");
										}
										i++;
									}
								}
								at.addRule();
							}
						}
					}
				}
			}
			else{
				log.error("Sorry, the current directory does not contain a walkmod configuration file or it is invalid.");
				at = new V2_AsciiTable();
				at.addRule();
				at.addRow("TRANSFORMATION TYPE", "PARAMETERS", "NAME/ALIAS");
				at.addStrongRule();
				at.addRule();
			}
		}
	}

	@Override
	public V2_AsciiTable getTable() {
		return at;
	}

}
