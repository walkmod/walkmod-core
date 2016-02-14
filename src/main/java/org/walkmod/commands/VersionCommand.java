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

import com.beust.jcommander.Parameters;

@Parameters(hidden = true, separators = "=", commandDescription = "Shows the current version of this tool")
public class VersionCommand implements Command {

	@Override
	public void execute() throws Exception {
		System.out.println("Walkmod version \"2.3.0\"");
		System.out.println("Java version: " + System.getProperty("java.version"));
		System.out.println("Java Home: " + System.getProperty("java.home"));
		System.out.println("OS: " + System.getProperty("os.name") + ", Version: " + System.getProperty("os.version"));
	}

}
