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
import java.util.List;

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

@Parameters(separators = "=", commandDescription = "Shows the list of chains with its code transformations.")
public class PrintChainsCommand implements Command, AsciiTableAware{

	@Parameter(names = "--help", help = true, hidden = true)
	private boolean help;

	private JCommander command;
	
	private V2_AsciiTable at = null;
	
	private static Logger log = Logger.getLogger(PrintChainsCommand.class);


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
			at = new V2_AsciiTable();
			at.addRule();
			at.addRow("CHAIN", "READER PATH", "WRITER PATH", "TRANSFORMATIONS");
			at.addStrongRule();
			
			if(cfg == null){
				at.addRule();
				log.error("Sorry, the current directory does not contain a walkmod configuration file or it is invalid.");
				
			}
			if (cfg != null) {
				Collection<ChainConfig> chains = cfg.getChainConfigs();
				if (chains != null) {
					
					for (ChainConfig cc : chains) {
					
						List<TransformationConfig> transformations = cc.getWalkerConfig().getTransformations();
						
						int numTransformations = transformations.size();
						
						int numReaderIncludesExcludes = 0;
						
						int includesLength = 0;
						String[] excludes = cc.getReaderConfig().getExcludes();
						if(excludes != null){
						   
						   numReaderIncludesExcludes = excludes.length;
						}
						String[] includes = cc.getReaderConfig().getIncludes();
						if(includes != null){
						   includesLength = includes.length;
						   numReaderIncludesExcludes += includes.length;
						}
						
						
						int limit = numReaderIncludesExcludes+1;
						if(numTransformations > numReaderIncludesExcludes){
						   limit = numTransformations;
						}
						int includesExcludesWriterLength = 0;
						int includesWriterLength = 0;
						String[] excludesWriter = cc.getWriterConfig().getExcludes();
						if(excludesWriter != null){
						   includesExcludesWriterLength += excludesWriter.length;
						   if(excludesWriter.length +1 > limit){
						      limit = excludesWriter.length+1;
						   }
						}
						
						String[] includesWriter = cc.getWriterConfig().getIncludes();
						
						if(includesWriter != null){
						   includesExcludesWriterLength += includesWriter.length;
						   includesWriterLength = includesWriter.length;
						   if(includesWriter.length +1 > limit){
						      limit = includesWriter.length +1;
						   }
						}
						
						for(int i = 0; i < limit; i++) {
							TransformationConfig next = null;
							String type = "";
							if(i < numTransformations){
							   next = transformations.get(i);
							   type = "- "+next.getType();
							}
							
							if (i == 0) {
								at.addRow(cc.getName(), cc.getReaderConfig().getPath(),cc.getWriterConfig().getPath(),  type);
								
							} else {
							   String readerWildcard = "";
							  
							   if(i-1 < includesLength){
							      readerWildcard = "> "+includes[i-1];
							   }
							   else{
							      if(i -1 < numReaderIncludesExcludes){
							         readerWildcard = "< "+excludes[i-1+includesLength];
							      }
							   }
							   
							   String writerWildcard = "";
							   if(includesWriter != null && i - 1 < includesWriter.length){
							      writerWildcard = "> "+includesWriter[i-1];
							   }
							   else if(i -1 < includesExcludesWriterLength){
							      writerWildcard = "< "+excludesWriter[i-1+includesWriterLength];
							   }
							   
								at.addRow("", readerWildcard , writerWildcard, type);
								
							}

						}
						at.addRule();
						
					}
					
				}
			}
		}
	}

	@Override
	public V2_AsciiTable getTable() {
		
		return at;
	}

}
