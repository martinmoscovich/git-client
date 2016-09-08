package com.mmoscovich.git.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import com.mmoscovich.git.client.cmd.CommandLineExecutor;
import com.mmoscovich.git.client.cmd.CommandLineExecutor.CommandResult;
import com.mmoscovich.git.client.model.GitCommit;
import com.mmoscovich.git.client.model.GitUser;

@Slf4j
public class CommandLineGitClient implements GitClient {

	/** Command line for Git executable. */
    private CommandLineExecutor cmdExecutor;
    private String gitExecutable;
    
    public CommandLineGitClient(String gitExecutable) {
    	if (StringUtils.isBlank(gitExecutable)) {
            gitExecutable = "git" + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".exe" : "");
        }
    	this.gitExecutable = gitExecutable;
    }
    
    /**
     * Executes Git command and returns output.
     * 
     * @param args
     *            Git command line arguments.
     * @return Command output.
     */
    private String executeGitCommandReturn(final String... args) throws GitClientException {
        try {
        	return cmdExecutor.executeCommand(true, args).getOut();
        }catch(CommandLineException e) {
        	throw new GitClientException("Error while executing command", e);
        }
    }

    /**
     * Executes Git command without failing on non successful exit code.
     * 
     * @param args
     *            Git command line arguments.
     * @return Command result.
     */
    private CommandResult executeGitCommandExitCode(final String... args) throws GitClientException {
        try {
        	return cmdExecutor.executeCommand(false, args);
	    }catch(CommandLineException e) {
	    	throw new GitClientException("Error while executing command", e);
	    }
    }

    /**
     * Executes Git command.
     * 
     * @param args
     *            Git command line arguments.
     */
    private void executeGitCommand(final String... args) throws GitClientException {
	    try {
	    	cmdExecutor.executeCommand(true, args);
	    }catch(CommandLineException e) {
	    	throw new GitClientException("Error while executing command", e);
	    }
    }
    
	@Override
	public void initClient() {
		if(cmdExecutor == null) cmdExecutor = new CommandLineExecutor(gitExecutable);
	}
	@Override
	public void initClient(File gitDir) {
		throw new UnsupportedOperationException("Command line git adapter may only be used in the current directory");
	}

	@Override
	public void setConfig(String name, String value) throws GitClientException {
		if (value == null || value.isEmpty()) {
            value = "\"\"";
        }

        // ignore error exit codes
        executeGitCommandExitCode("config", name, value);
	}
	
	@Override
	public String getConfig(String name) throws GitClientException {
		String value = executeGitCommandReturn("config", "--get", name);
		if(StringUtils.isBlank(value)) return null;
		return value;
	}

	@Override
	public String getRemoteUrl(String remoteName) throws GitClientException {
		try {
			if(remoteName == null) remoteName = "origin";
			String key = String.format("%s.%s.%s", "remote", remoteName, "url");
			return this.getConfig(key);
		} catch (Exception e) {
			throw new GitClientException("Error while retrieving the remote '" + remoteName + "' url", e);
		}
	}
	
	@Override
	public List<String> findBranches(String branchPrefix) throws GitClientException {
		String branches;
        branches = executeGitCommandReturn("for-each-ref", "--format=\"%(refname:short)\"", "refs/heads/" + branchPrefix + "*");

        if(branches == null || branches.isEmpty()) return Collections.emptyList();

        // on *nix systems return values from git for-each-ref are wrapped in
        // quotes
        // https://github.com/aleksandr-m/gitflow-maven-plugin/issues/3
        branches = branches.replaceAll("\"", "").trim();
        
        return Arrays.asList(branches.split("\\r?\\n"));
	}

	@Override
	public String findFirstBranch(String branchPrefix) throws GitClientException {
		String branches;
        branches = executeGitCommandReturn("for-each-ref", "--count=1",
                "--format=\"%(refname:short)\"", "refs/heads/" + branchPrefix
                        + "*");

        // on *nix systems return values from git for-each-ref are wrapped in
        // quotes
        // https://github.com/aleksandr-m/gitflow-maven-plugin/issues/3
        if (branches != null && !branches.isEmpty()) {
            branches = branches.replaceAll("\"", "").trim();
        }

        return branches;
	}

	@Override
	public String findBranch(String branchName) throws GitClientException {
		return executeGitCommandReturn("for-each-ref", "refs/heads/" + branchName);
	}
	
	@Override
	public Boolean remoteBranchExists(String branchName) throws GitClientException {
		return StringUtils.isNotBlank(executeGitCommandReturn("for-each-ref", "refs/remotes/*/" + branchName));
	}

	@Override
	public void checkout(String branchName) throws GitClientException {
		executeGitCommand("checkout", branchName);
	}

	@Override
	public void createAndCheckout(String newBranchName, String fromBranchName) throws GitClientException {
		executeGitCommand("checkout", "-b", newBranchName, fromBranchName);
	}

	@Override
	public void commit(String message) throws GitClientException {
		executeGitCommand("commit", "-a", "-m", message);
	}

	@Override
	public void merge(String branchName, boolean rebase, boolean noff, boolean squash) throws GitClientException {
		if (rebase) {
            executeGitCommand("rebase", branchName);
        } else if (noff) {
        	List<String> args = new ArrayList<String>();
        	args.add("merge");
        	if(noff) args.add("--no-ff");
        	if(squash) args.add("--squash");
        	args.add(branchName);
        	
            executeGitCommand(args.toArray(new String[] {}));
        }
	}

	@Override
	public void mergeNoff(String branchName) throws GitClientException {
		merge(branchName, false, true, false);
	}

	@Override
	public void tag(String tagName, String message) throws GitClientException {
		executeGitCommand("tag", "-a", tagName, "-m", message);
	}

	@Override
	public void branchDelete(String branchName, boolean force) throws GitClientException {
		String param = (force?"-D":"-d");
		executeGitCommand("branch", param, branchName);
	}
	
	@Override
	public void pull(String branchName) throws GitClientException {
		this.checkout(branchName);
		executeGitCommand("pull");
	}

	@Override
	public void push(String branchName) throws GitClientException {
		this.checkout(branchName);
		executeGitCommand("push");
	}
	
	@Override
	public void pushTag(String tagName) throws GitClientException {
		executeGitCommand("push", "origin", tagName);
	}


	@Override
	public boolean hasUncommitedChanges(boolean allowUntracked) throws GitClientException {
		boolean uncommited = false;

        // 1 if there were differences and 0 means no differences

        // git diff --no-ext-diff --ignore-submodules --quiet --exit-code
        final CommandResult diffCommandResult = executeGitCommandExitCode(
                "diff", "--no-ext-diff", "--ignore-submodules", "--quiet",
                "--exit-code");

        String error = null;

        if (diffCommandResult.getExitCode() == CommandLineExecutor.SUCCESS_EXIT_CODE) {
            // git diff-index --cached --quiet --ignore-submodules HEAD --
            final CommandResult diffIndexCommandResult = executeGitCommandExitCode(
                    "diff-index", "--cached", "--quiet", "--ignore-submodules",
                    "HEAD", "--");
            if (diffIndexCommandResult.getExitCode() != CommandLineExecutor.SUCCESS_EXIT_CODE) {
                error = diffIndexCommandResult.getError();
                uncommited = true;
            }
        } else {
            error = diffCommandResult.getError();
            uncommited = true;
        }

        if (StringUtils.isNotBlank(error)) {
            throw new GitClientException("Error while checking for uncommited changes: " + error);
        }

        return uncommited;
	}

	@Override
	public String getCurrentBranchName() throws GitClientException {
		return executeGitCommandReturn("symbolic-ref", "--short", "HEAD");
	}

	@Override
	public GitCommit getLastCommit(String branchName) {
		String revHash = executeGitCommandReturn("rev-parse", "--verify", branchName);
		String commitMessage = executeGitCommandReturn("log", "--format=%B", "-1", branchName);
		Date commitDate = new Date(1000L * Long.parseLong(executeGitCommandReturn("log", " --format=%ct", "-1", branchName)));
		String username = executeGitCommandReturn("log", "--format=%cn", "-1", branchName);
		String userEmail = executeGitCommandReturn("log", "--format=%ce", "-1", branchName);
		
		GitCommit commit = new GitCommit();
		commit.setHash(revHash);
		commit.setMessage(commitMessage);
		commit.setUser(new GitUser(username, userEmail));
		commit.setDate(commitDate);
		
		return commit;
	}

	@Override
	public GitUser getConfiguredUser() throws GitClientException {
		String name = this.getConfig("user.name");
    	String email = this.getConfig("user.email");
    	
    	if(name == null && email == null) return null;
    	return new GitUser(name, email);
	}

	@Override
	public void close() throws Exception {}
}
