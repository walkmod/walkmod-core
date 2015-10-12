package org.walkmod.commands;

import java.util.LinkedList;
import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Sets an specific writer, optionally, for an specific chain")
public class SetWriterCommand implements Command{
	
	@Parameter(arity= 1,  description = "The writer type identifier", required= true)
	public List<String> writerType;
	
	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander jcommander;

	@Parameter(names = { "--chain" }, description = "The chain identifier", required = false)
	private String chain = null;
	
	public SetWriterCommand(JCommander jcommander){
		this.jcommander = jcommander;
	}
	
	public SetWriterCommand(String writerType, String chain){
		this.chain = chain;
		this.writerType = new LinkedList<String>();
		this.writerType.add(writerType);
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			jcommander.usage("set-writer");
		} else {
			WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
			facade.setWriter(chain, writerType.get(0));
		}
	}

}
