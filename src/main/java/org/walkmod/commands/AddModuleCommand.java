package org.walkmod.commands;

import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Adds modules to the configuration.")
public class AddModuleCommand implements Command{

	@Parameter(description = "List of modules to add", required = true)
	private List<String> modules;
	
	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander jcommander;
	
	public AddModuleCommand(JCommander jcommander){
		this.jcommander = jcommander;
	}
	
	public AddModuleCommand(List<String> modules){
		this.modules = modules;
	}
	
	
	@Override
	public void execute() throws Exception {
		if (help) {
			jcommander.usage("add-module");
		} else {

			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.addModules(modules);
		}
		
	}

}
