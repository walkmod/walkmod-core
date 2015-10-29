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
import java.util.Map;
import java.util.Set;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.ProviderConfig;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.vandermeer.asciitable.v2.V2_AsciiTable;

@Parameters(separators = "=", commandDescription = "Shows the list of added providers with its parameters.")
public class PrintProvidersCommand implements Command, AsciiTableAware {

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander jcommander;

	private V2_AsciiTable at;

	public PrintProvidersCommand(JCommander jcommander) {
		this.jcommander = jcommander;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			jcommander.usage("modules");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			Configuration cfg = facade.getConfiguration();
			at = new V2_AsciiTable();
			at.addRule();
			at.addRow("CONFIGURATION PROVIDERS", "PARAMETERS");
			at.addRule();

			if (cfg != null) {
				Collection<ProviderConfig> providers = cfg.getProviderConfigurations();
				if (providers != null) {
					for (ProviderConfig provider : providers) {
						Map<String, Object> params = provider.getParameters();
						if (params == null) {
							at.addRow(provider.getType(), "");
						} else {
							Set<String> keys = params.keySet();
							int i = 0;
							for (String key : keys) {
								if (i == 0) {
									at.addRow(provider.getType(), params.get(key).toString());
								} else {
									at.addRow("", params.get(key).toString());
								}
								i++;
							}
						}
						at.addRule();
					}
				}
			}
			at.addRule();

		}
	}

	@Override
	public V2_AsciiTable getTable() {
		return at;
	}

}
