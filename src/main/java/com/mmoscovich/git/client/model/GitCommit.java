package com.mmoscovich.git.client.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class that represents a commit.
 * 
 * @author Martin Moscovich
 *
 */
@Getter
@Setter
@ToString
public class GitCommit {
	private String hash;
	private GitUser user;
	private String message;
	private Date date;
}
