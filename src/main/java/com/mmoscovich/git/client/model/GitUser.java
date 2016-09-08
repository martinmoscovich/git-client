package com.mmoscovich.git.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class that represents a Git User.
 * @author Martin Moscovich
 *
 */
@Getter
@AllArgsConstructor
public class GitUser {
	private String name;
	private String email;
	
	public String toString() {
		if(name != null && email != null) {
			return name + " <" + email + ">";
		} else if(name != null) {
			return name;
		} else if(email != null) {
			return email;
		}
		return "";
			
	}
}
