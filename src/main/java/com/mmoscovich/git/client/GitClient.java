package com.mmoscovich.git.client;

import java.io.File;
import java.util.List;

import com.mmoscovich.git.client.model.GitCommit;
import com.mmoscovich.git.client.model.GitUser;

/**
 * Git Client interface
 * 
 * @author Martin Moscovich
 *
 */
public interface GitClient extends AutoCloseable {
	
	/**
	 * @return whether the client is closed
	 */
	boolean isClosed();
	
	/**
	 * @return whether the client has loaded the repo (or created one)
	 */
	boolean repoLoaded();
	
	/**
	 * Checks whether a Git Repository exists in the current directory (or any parent directory).
	 * 
	 * @return
	 * @throws GitClientException if something fails
	 */
	boolean repoExists() throws GitClientException;
	
	/**
	 * Creates a repo in the current directory if it does not exist.
	 * 
	 * @throws GitClientException if the repo already exists or something fails.
	 */
	void createRepo() throws GitClientException;
	
	/**
	 * Creates a repo in the specified directory if it does not exist.
	 * 
	 * @param gitDir directory where to start the search for a Git repo.
	 * @throws GitClientException if the repo already exists or something fails.
	 */
	void createRepo(File gitDir) throws GitClientException;
	
	/**
	 * Loads the repo in the current working directory or above if it exists
	 * 
	 * @throws GitClientException if no repository is found in that directory
	 */
	void loadRepo() throws GitClientException;
	
	/**
	 * Loads the repo on the specified directory or above.
	 *
	 * @param gitDir directory where to start the search for a Git repo.
	 * @throws GitClientException if no repository is found in that directory
	 */
	void loadRepo(File gitDir) throws GitClientException;

//	/**
//	 * Initializes the adapter, using the repo on the current working directory or above.
//	 * @throws GitClientException if no repository is found in that directory
//	 */
//	void initClient() throws GitClientException;
//	
//	
//	/**
//	 * Initializes the adapter, using the repo on the specified directory or above.
//	 * @throws GitClientException if no repository is found in that directory
//	 * @param gitDir directory where to start the search for a Git repo.
//	 * @throws GitClientException if no repository is found in that directory
//	 */
//	void initClient(File gitDir) throws GitClientException;
	
	
	/**
	 * Configures a value in the Git Repo
	 * @param name the key to save (must have 3 parts separated by ".")
	 * @param value the value to store on that key
	 * @throws GitClientException if the name is invalid or something fails
	 */
	void setConfig(String name, String value) throws GitClientException;
	
	
	/**
	 * Looks for <strong>local</strong> branches that start with the provided prefix.
	 * <br>Only the branch name is required, Git-specific parts are completed inside.
	 * @param branchPrefix the prefix to search for
	 * @return the branch names that match the query or an empty list is none is found
	 * @throws GitClientException if there is a problem while searching
	 */
	List<String> findBranches(final String branchPrefix) throws GitClientException;
	
	/**
	 * Looks for the first <strong>local</strong> branch that starts with the provided prefix.
	 * <br>Only the branch name is required, Git-specific parts are completed inside.
	 * @param branchPrefix the prefix to search for
	 * @return the first branch name that matches the query or <code>null</code> is none is found.
	 * @throws GitClientException if there is a problem while searching
	 */
	String findFirstBranch(final String branchPrefix) throws GitClientException;
	
	
	/**
	 * Looks for a <strong>local</strong> branch with exactly the provided name
	 * @param branchName the name to search for
	 * @return the branch name if found or <code>null</code> otherwise.
	 * @throws GitClientException if there is a problem while searching
	 */
	String findBranch(final String branchName) throws GitClientException;
	
	/**
	 * Checks if a branch with the provided name exists
	 * 
	 * @param branchName name of the branch to search for
	 * @return <code>true</code> if the branch exists, <code>false</code> otherwise.
	 * @throws GitClientException if there is a problem while searching
	 */
	boolean branchExists(final String branchName) throws GitClientException;
	
	/**
	 * Looks for <strong>local</strong> tags that start with the provided prefix.
	 * <br>Only the tag name is required, Git-specific parts are completed inside.
	 * @param tagPrefix the prefix to search for
	 * @return the tag names that match the query or an empty list is none is found
	 * @throws GitClientException if there is a problem while searching
	 */
	List<String> findTags(String tagPrefix) throws GitClientException;
	
	/**
	 * Looks for the first <strong>local</strong> tag that starts with the provided prefix.
	 * <br>Only the tag name is required, Git-specific parts are completed inside.
	 * @param tagPrefix the prefix to search for
	 * @return the first tag name that matches the query or <code>null</code> is none is found.
	 * @throws GitClientException if there is a problem while searching
	 */
	String findFirstTag(String tagPrefix) throws GitClientException;
	
	/**
	 * Looks for a <strong>local</strong> tag with exactly the provided name
	 * @param tagName the name to search for
	 * @return the tag name if found or <code>null</code> otherwise.
	 * @throws GitClientException if there is a problem while searching
	 */
	String findTag(final String tagName) throws GitClientException;
	
	/**
	 * Checks if a tag with the provided name exists
	 * 
	 * @param tagName name of the tag to search for
	 * @return <code>true</code> if the tag exists, <code>false</code> otherwise.
	 * @throws GitClientException if there is a problem while searching
	 */
	boolean tagExists(final String tagName) throws GitClientException;
	
	/**
	 * Checkout the specified branch (ie. switches the current branch to the one specified).
	 * @param branchName branch to checkout 
	 * @throws GitClientException if there is a problem while checking out the branch
	 */
	void checkout(final String branchName) throws GitClientException;
	
	/**
	 * Creates a new branch with the provided name and makes it the current one.
	 * <br>Equivalent to <code>checkout -b</code>.
	 * @param newBranchName name of the branch to create
	 * @param fromBranchName name of the branch to use a base for the new branch
	 * @throws GitClientException if there is a problem while checking out the branch
	 */
	void createAndCheckout(final String newBranchName, final String fromBranchName) throws GitClientException;
	
	/**
	 * Creates a new branch with the provided name and makes it the current one.
	 * <br>The new branch will be based of HEAD.
	 * <br>Equivalent to <code>checkout -b</code>.
	 * @param newBranchName name of the branch to create
	 * @throws GitClientException if there is a problem while checking out the branch
	 */
	void createAndCheckout(final String newBranchName) throws GitClientException;
	
	
	/**
	 * Adds files to commit list
	 * 
	 * @param filenames files to include
	 */
	void stageFiles(List<String> filenames);
	
	/**
	 * Commits the staged changes on the current branch using the provided message 
	 * @param message commit message
	 * @throws GitClientException if there is a problem while commiting
	 */
	void commit(final String message) throws GitClientException;
	
	
	/**
	 * Merges the current branch with the one specified.
	 * @param branchName name of the branch to merge with the current one
	 * @param rebase whether it should merge or rebase (not supported on Native Java client)
	 * @param noff if <code>true</code> the merge wont do a FF even if possible (ie a merge commit is always created).
	 * @param squash whether to squash the merge.
	 * @throws GitClientException if the branch to merge does not exists, if the merge is not successful (conflicts) or if there is a problem while merging.
	 */
	void merge(final String branchName, boolean rebase, boolean noff, boolean squash) throws GitClientException;
	
	
	/**
	 * Merges the current branch with the one specified (with no-ff flag).
	 * <br>Equivalent to calling {@link #merge(String, boolean, boolean, boolean)} with <code>noff</code> set to <code>true</code>
	 * @param branchName name of the branch to merge with the current one
	 * @throws GitClientException if the branch to merge does not exists, if the merge is not successful (conflicts) or if there is a problem while merging.
	 */
	void mergeNoff(final String branchName) throws GitClientException;
	
	/**
	 * Creates a tag.
	 * 
	 * @param tagName name of the tag
	 * @param message tag message
	 * @throws GitClientException if there is a problem while tagging
	 */
	void tag(final String tagName, final String message) throws GitClientException;
	
	/**
	 * Deletes a <strong>local</strong> branch
	 * @param branchName branch to delete
	 * @param force whether to force (-D) or not (-d)
	 * @throws GitClientException if there is a problem while merging
	 */
	void branchDelete(final String branchName, boolean force) throws GitClientException;
	
	/**
	 * Checks is the current branch contains uncommited changes.
	 * <br>NOTE: Command Line version currently ignores the parameter.
	 * 
	 * @param allowUntracked whether to consider untracked (new) files as uncommited changes.
	 * @return <code>true</code> if there are uncommited changes. <code>false</code> otherwise.
	 * @throws GitClientException if there is a problem while checking
	 */
	boolean hasUncommitedChanges(boolean allowUntracked) throws GitClientException;
	
	/**
	 * Fetches from remote.
	 * 
	 * @throws GitClientException if there is a problem while pulling
	 */
	void fetch() throws GitClientException;
	
	/**
	 * Pulls a remote branch
	 * @param branchName branch to pull
	 * @throws GitClientException if there is a problem while pulling
	 */
	void pull(final String branchName) throws GitClientException;
	
	/**
	 * Pushes a branch to remote
	 * @param branchName branch to push
	 * @throws GitClientException if there is a problem while pushing
	 */
	void push(final String branchName) throws GitClientException;
	
	
	/**
	 * Pushes a tag to remote
	 * @param tagName tag to push
	 * @throws GitClientException if there is a problem while pushing
	 */
	void pushTag(String tagName) throws GitClientException;
	
	
	/**
	 * Retrieves the current branch <strong>short</strong> name (ie without Git-specific parts)
	 * @return the current branch name
	 * @throws GitClientException
	 */
	String getCurrentBranchName() throws GitClientException;
	
	/**
	 * Checks whether a remote branch exists
	 * @param branchName name of the remote branch to search for
	 * @return <code>true</code> if it exists, <code>false</code> otherwise.
	 * @throws GitClientException
	 */
	Boolean remoteBranchExists(String branchName) throws GitClientException;
	
	/**
	 * Retrieves a config entry for the repository by key
	 * @param name key of the configuration
	 * @return the value if found, <code>null</code> otherwise
	 * @throws GitClientException if the name is invalid or there is a problem retrieving the configuration.
	 */
	String getConfig(String name) throws GitClientException;
	
	/**
	 * Retrieves the name of the remote repository with the specified name.
	 * <br>Equivalent of calling {@link #getConfig(String)} with "<code>remote.{remoteName}.url</code>" as the key.
	 * @param remoteName name of the remote branch. If <code>null</code>, it defaults to "origin"
	 * @return the url of the remote
	 * @throws GitClientException if there is a problem looking for the url
	 */
	String getRemoteUrl(String remoteName) throws GitClientException;
	
	
	/**
	 * Retrieves the info of last commit on the specified branch
	 * @param branchName 
	 * @return the info of the last commit 
	 */
	GitCommit getLastCommit(String branchName);

	
	/**
	 * Retrieves the user configured in the repository
	 * @return the user
	 * @throws GitClientException if there is a problem
	 */
	GitUser getConfiguredUser() throws GitClientException;

	/**
	 * Adds a remote repository with the given name (eg. origin) and url
	 * 
	 * @param remoteName the name of the remote repository
	 * @param url the repository url
	 * @throws GitClientException if the repository already exists or there is a problem
	 */
	void remoteRepoAdd(String remoteName, String url) throws GitClientException;;

	/**
	 * Updates the URL of an existing remote repository reference with that name (eg. origin).
	 * 
	 * @param remoteName the name of the remote repository
	 * @param url the repository url
	 * @throws GitClientException if the repository does not exist or there is a problem
	 */
	void remoteRepoUpdateUrl(String remoteName, String url);
	
	/**
	 * @return the directory where git stores the metadata for this repository
	 * (usually [repo]/.git
	 */
	File getGitDirectory();
	
	/**
	 * @return the list of staged files (files that will be commited).
	 */
	List<String> getStagedFiles();
}
