package org.walkmod;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.walkmod.conf.entities.impl.ConfigurationImpl;

public abstract class AbstractWalkmodExecutionTest {
	public String run(String[] args) throws Exception {

		ByteArrayOutputStream mem = new ByteArrayOutputStream();
		BufferedOutputStream stream = new BufferedOutputStream(mem);

		PrintStream ps = new PrintStream(stream);
		PrintStream old = System.out;
		System.setOut(ps);

		WalkModFacade.log = Logger.getLogger(WalkModFacade.class.getName());
		WalkModFacade.log.removeAllAppenders();
		
		ConfigurationImpl.log = Logger.getLogger(ConfigurationImpl.class.getName());
		ConfigurationImpl.log.removeAllAppenders();
        
		ConsoleAppender appender = new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
		appender.setName("stdout");
		WalkModFacade.log.addAppender(appender);
		ConfigurationImpl.log.addAppender(appender);
		
		String result = "";
		try {
			WalkModDispatcher.main(args);
			stream.flush();
			result = mem.toString();

		} finally {
			System.setOut(old);
			ps.close();
			stream.close();
			mem.close();
		}

		return result;
	}
}
