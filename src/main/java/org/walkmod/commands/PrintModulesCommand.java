package org.walkmod.commands;

import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.Configuration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthAbsoluteEven;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;

@Parameters(separators = "=", commandDescription = "Shows the list of modules.")
public class PrintModulesCommand implements Command{

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;
	
	public PrintModulesCommand(JCommander command){
		this.command = command;
	}
	
	
	@Override
	public void execute() throws Exception {
		if (help) {
			command.usage("modules");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			Configuration cfg = facade.getConfiguration();
			
			V2_AsciiTable at = new V2_AsciiTable();
			at.addRule();
			at.addRow("MODULE");
			at.addRule();
			
			if (cfg != null) {
				List<String> modules = cfg.getModules();
				if(modules != null){
					for(String module: modules){
						at.addRow(module);
					}
				}
			}
			at.addRule();
			V2_AsciiTableRenderer rend = new V2_AsciiTableRenderer();
			rend.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
			rend.setWidth(new WidthAbsoluteEven(20));
			RenderedTable rt = rend.render(at);
			System.out.println(rt);
		}
	}

}
