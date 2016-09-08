package com.mmoscovich.git.client.cmd;

import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

public class VerboseCommandLineConsumer extends StringStreamConsumer {
	
	@Override
	public void consumeLine(String line) {
		super.consumeLine(line);
		System.out.println( line );
	}

}
