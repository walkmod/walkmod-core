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
public class PrintProvidersCommand implements Command, AsciiTableAware{

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander jcommander;
	
	private V2_AsciiTable at;
	
	public PrintProvidersCommand(JCommander jcommander){
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
						if(params == null){
							at.addRow(provider.getType(), "");
						}
						else{
							Set<String> keys = params.keySet();
							int i = 0;
							for(String key: keys){
								if(i == 0){
									at.addRow(provider.getType(), params.get(key).toString());
								}
								else{
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
