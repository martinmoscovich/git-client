package com.mmoscovich.git.client;

/**
 * Exception thrown by the {@link GitClient} when something goes wrong
 * @author Martin Moscovich
 *
 */
public class GitClientException extends RuntimeException {
	private static final long serialVersionUID = -2192461931322509828L;
	
	public GitClientException(String message) {
		super(message);
	}
	public GitClientException(String message, Throwable e) {
		super(message, e);
	}
}
