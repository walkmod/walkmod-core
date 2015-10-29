package org.walkmod.commands;

import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.Configuration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.vandermeer.asciitable.v2.V2_AsciiTable;

@Parameters(separators = "=", commandDescription = "Shows the list of modules.")
public class PrintModulesCommand implements Command, AsciiTableAware {

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;

	private V2_AsciiTable at = null;

	public PrintModulesCommand(JCommander command) {
		this.command = command;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("modules");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			Configuration cfg = facade.getConfiguration();
			at = new V2_AsciiTable();
			at.addRule();
			at.addRow("MODULE");
			at.addRule();

			if (cfg != null) {
				List<String> modules = cfg.getModules();
				if (modules != null) {
					for (String module : modules) {
						at.addRow(module);
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
