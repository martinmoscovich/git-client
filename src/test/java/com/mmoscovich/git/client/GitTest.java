package com.mmoscovich.git.client;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

/**
 * Use this class for manual testing
 * 
 * @author Martin
 *
 */
@Slf4j
public class GitTest {

//	@Test
	public void a() throws Exception {
		GitClient cli = new JGitClient();
		cli.loadRepo(new File("c:/dev/proyectos/maven/multimodule"));
		log.info("{}", cli.branchExists("develop"));
		cli.close();
	}
}
