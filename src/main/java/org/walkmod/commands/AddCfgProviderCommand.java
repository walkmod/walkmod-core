package org.walkmod.commands;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.JSONConfigParser;
import org.walkmod.conf.entities.ProviderConfig;
import org.walkmod.conf.entities.impl.ProviderConfigImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;

@Parameters(separators = "=", commandDescription = "Adds a configuration provider to enrich the configuration.e.g Calculate the project classpath")
public class AddCfgProviderCommand implements Command {

	@Parameter(description = "The configuration provider type identifier", required = true)
	private String type;

	@Parameter(names = "--params", description = "Transformation parameters as JSON object", converter = JSONConverter.class)
	private JsonNode params;

	private JCommander command;
	
	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	public AddCfgProviderCommand(JCommander command) {
		this.command = command;
	}
	
	public AddCfgProviderCommand(String type, JsonNode params){
		this.type = type;
		this.params = params;
	}

	public ProviderConfig build() throws Exception {
		ProviderConfig prov = new ProviderConfigImpl();

		prov.setType(type);

		if (params != null) {
			JSONConfigParser parser = new JSONConfigParser();

			prov.setParameters(parser.getParams(params));
		}

		return prov;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("add-cfgprovider");
		} else {

			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.addProviderConfig(build());
		}

	}

}
