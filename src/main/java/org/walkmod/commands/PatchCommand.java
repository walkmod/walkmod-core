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

import java.util.Map;

import org.walkmod.WalkModFacade;
import org.walkmod.patches.PatchFormat;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Generates the corresponding patches for all your code transformations.")
public class PatchCommand extends AbstractChainCommand implements Command {

    private JCommander command;

    @Parameter(names = { "-g", "--global" }, description = "Produces a global patch per file")
    private boolean patchPerFile = true;

    @Parameter(names = { "-o", "--occurrence" }, description = "Produces a patch per change occurrence")
    private boolean patchPerChange = false;

    @Parameter(names = { "-s", "--style" }, description = "Patch style (json or raw)")
    private String patchFormat = PatchFormat.RAW.name();

    public PatchCommand(JCommander command) {
        this.command = command;
    }

    @Override
    public void execute() throws Exception {
        if (isHelpNeeded()) {
            command.usage("patch");
        } else {
            Map<String, String> dynParams = getDynamicParams();
            dynParams.put("patchPerFile", Boolean.toString(patchPerFile));
            dynParams.put("patchPerChange", Boolean.toString(patchPerChange));
            dynParams.put("patchFormat", patchFormat);

            WalkModFacade facade = new WalkModFacade(buildOptions());
            String[] params = new String[getParameters().size()];
            if (params.length == 0) {
                facade.patch();
            } else {
                facade.patch(getParameters().toArray(params));
            }
        }
    }
}
