package com.mmoscovich.git.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.StringUtils;

import com.mmoscovich.git.client.model.GitCommit;
import com.mmoscovich.git.client.model.GitUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JGitClient implements GitClient {
	private Git git;
	
	@Override
	public void loadRepo() throws GitClientException {
		doLoadRepo(new RepositoryBuilder().readEnvironment().findGitDir());
	}
	
	@Override
	public void loadRepo(File gitDir) throws GitClientException {
		doLoadRepo(new RepositoryBuilder().readEnvironment().findGitDir(gitDir));
	}

	private void doLoadRepo(RepositoryBuilder builder) throws GitClientException {
		try {
			File gitDir = builder.getGitDir();
		
			if (null != gitDir) {
				log.debug("Found existing git folder. Initializing");
				this.git = Git.open(gitDir);
			} else {
				throw new GitClientException("No Git Repository found on the specified directory");
			}
		} catch(Exception e) {
			throw new GitClientException("Error while initializing", e);
		}
	}
	
	@Override
	public List<String> findTags(String tagPrefix) throws GitClientException {
		List<String> tags = new ArrayList<String>();
        if(StringUtils.isEmptyOrNull(tagPrefix)) return tags;
        log.debug("Searching for tags that start with " + tagPrefix);

        try
        {
            ListTagCommand cmd = git.tagList();
            
            List<Ref> refs = cmd.call();

            for (Ref ref : refs) {
            	String simpleName = ref.getName().substring(ref.getName().indexOf(Constants.R_TAGS) + Constants.R_TAGS.length());
            	if (simpleName.startsWith(tagPrefix)) {
            		tags.add(simpleName);
                }
            }

            return tags;
        }
        catch (GitAPIException e) {
            throw new GitClientException("Error while searching tags", e);
        }
	}
	
	public List<String> doFindBranches(String branchPrefix, ListMode type) throws GitClientException {
		List<String> branches = new ArrayList<String>();
        if(StringUtils.isEmptyOrNull(branchPrefix)) return branches;
        log.debug("Searching for branches that start with " + branchPrefix);

        try
        {
            ListBranchCommand cmd = git.branchList();
            
            if(type != null) cmd.setListMode(type);
            
            List<Ref> refs = cmd.call();

            for (Ref ref : refs) {
                String simpleName;

                String originPrefix = Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + "/";

                if (ref.getName().indexOf(Constants.R_HEADS) > -1)
                {
                    simpleName = ref.getName().substring(ref.getName().indexOf(Constants.R_HEADS) + Constants.R_HEADS.length());
                }
                else if (ref.getName().indexOf(originPrefix) > -1)
                {
                    simpleName = ref.getName().substring(ref.getName().indexOf(originPrefix) + originPrefix.length());
                }
                else
                {
                    simpleName = "";
                }

                log.debug("Found branch [" + simpleName + "]. Is match? [" + branchPrefix + "] ? " + simpleName.startsWith(branchPrefix));

                if (simpleName.startsWith(branchPrefix)) {
                    branches.add(simpleName);
                }
            }

            return branches;
        }
        catch (GitAPIException e) {
            throw new GitClientException("Error while searching branches", e);
        }
	}
	
	@Override
	public List<String> findBranches(String branchPrefix) throws GitClientException {
        return this.doFindBranches(branchPrefix, null);
	}

	@Override
	public String findFirstBranch(String branchPrefix) throws GitClientException {
		List<String> branches = this.findBranches(branchPrefix);
		if(branches.isEmpty()) return null;
		return branches.get(0);
	}

	@Override
	public String findBranch(String branchName) throws GitClientException {
        log.debug("Searching for branch " + branchName);
        for(String branch : this.findBranches(branchName)) {
        	if(branch.equals(branchName)) return branch; 
        }
        return null;
	}
	
	@Override
	public String findFirstTag(String tagPrefix) throws GitClientException {
		List<String> tags = this.findTags(tagPrefix);
		if(tags.isEmpty()) return null;
		return tags.get(0);
	}
	
	@Override
	public String findTag(String tagName) throws GitClientException {
		log.debug("Searching for tag " + tagName);
        for(String tag : this.findTags(tagName)) {
        	if(tag.equals(tagName)) return tag; 
        }
        return null;
	}
	
	@Override
	public Boolean remoteBranchExists(String branchName) throws GitClientException {
		for(String branch : this.doFindBranches(branchName, ListMode.REMOTE)) {
        	if(branch.equals(branchName)) return true; 
        }
        return false;
	}
	

	@Override
	public void checkout(String branchName) throws GitClientException {
		try {
			this.git.checkout().setName(branchName).call();
		} catch (GitAPIException e) {
			throw new GitClientException("Error while checking out branch", e);
		}
	}

	@Override
	public void createAndCheckout(String newBranchName) throws GitClientException {
		this.createAndCheckout(newBranchName, null);
	}
	
	@Override
	public void createAndCheckout(String newBranchName, String fromBranchName) throws GitClientException {
		try {
			CheckoutCommand cmd = this.git.checkout().setCreateBranch(true).setName(newBranchName);
			
			// If from branch is specified, use it
			if(fromBranchName != null) cmd = cmd.setStartPoint(Constants.R_HEADS + fromBranchName);
			
			cmd.call();
				
		} catch (GitAPIException e) {
			throw new GitClientException("Error while checking out branch", e);
		}
	}

	@Override
	public void commit(String message) throws GitClientException {
		try {
			this.git.commit().setAll(true).setMessage(message).call();
		} catch (GitAPIException e) {
			throw new GitClientException("Error while commiting branch", e);
		}

	}
	
	@Override
	public void merge(String branchToMerge, boolean rebase, boolean noff, boolean squash) throws GitClientException {
		this.merge(branchToMerge, rebase, noff, squash, null);
	}

	@Override
	public void merge(String branchToMerge, boolean rebase, boolean noff, boolean squash, String message) throws GitClientException {
		try {
			Ref branchToMergeRef = this.getLocalBranch(branchToMerge); 
			if(branchToMergeRef == null) throw new GitClientException("The branch to merge (" + branchToMerge + ") doesnt exist");
			
			MergeCommand cmd = this.git.merge().include(branchToMergeRef);
			if(noff) cmd.setFastForward(FastForwardMode.NO_FF);
			if(message != null && !message.isEmpty()) cmd.setMessage(message);
			
			cmd.setSquash(squash);
			
			MergeResult result = cmd.call();
			
			if(!result.getMergeStatus().isSuccessful()) {
				String error = null;
				if (result.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
					error = "please resolve your merge conflicts";
	            }
	            else {
	            	error = "until JGit supports merge resets, please run 'git reset --merge' to get back to a clean state";
	            }
				throw new GitClientException("Error while merging. " + error + ": " + result.toString());
			}
			
		} catch (GitAPIException e) {
			throw new GitClientException("Error while merging", e);
		}
	}

	@Override
	public void mergeNoff(String branchToMerge) throws GitClientException {
		this.merge(branchToMerge, false, true, false);

	}

	@Override
	public void tag(String tagName, String message) throws GitClientException {
		try {
			this.git.tag().setName(tagName).setAnnotated(true).setMessage(message).call();
		} catch (GitAPIException e) {
			throw new GitClientException("Error while tagging", e);
		}

	}

	@Override
	public void branchDelete(String branchName, boolean force) throws GitClientException {
		try {
			this.git.branchDelete().setForce(force).setBranchNames(branchName).call();
		} catch (GitAPIException e) {
			throw new GitClientException("Error while deleting branch", e);
		}
	}

	@Override
	public void setConfig(String name, String value) throws GitClientException {
		if(name == null || value == null) throw new GitClientException("Neither the config attribute name nor value can be null");
		String[] parts = name.split("\\.");
		if(parts.length != 3) throw new GitClientException("The config attribute name must contain 3 parts (specified: " + name + ")");
		
		this.git.getRepository().getConfig().setString(parts[0], parts[1], parts[2], value);
	}
	
	@Override
	public String getConfig(String name) throws GitClientException {
		try {
			if(name == null) throw new GitClientException("The config attribute name cannot be null");
			String[] parts = name.split("\\.");
			if(parts.length == 3) {
				return this.git.getRepository().getConfig().getString(parts[0], parts[1], parts[2]);
			} else if(parts.length == 2) {
				return this.git.getRepository().getConfig().getString(parts[0], null, parts[1]);
			} else {
				throw new GitClientException("The config attribute name must contain 2 or 3 parts (specified: " + name + ")");
			}
			
		} catch(Exception e) {
			throw new GitClientException("Error while retrieving config for '" + name + "'", e);
		}
	
	}
	
	@Override
	public void pull(String branchName) throws GitClientException {
		try {
			PullCommand cmd = this.git.pull();

			boolean localExists = (this.findBranch(branchName) != null); 
			if(localExists) {
				this.checkout(branchName);
			} else {
				cmd = cmd.setRemoteBranchName(branchName);
			}
			cmd.call();
		} catch (GitAPIException e) {
			throw new GitClientException("Error while pulling", e);
		}
	}
	
	@Override
	public void push(String branchName) throws GitClientException {
		try {
			this.checkout(branchName);
			this.git.push().call();
		} catch (GitAPIException e) {
			throw new GitClientException("Error while pushing branch: " + branchName, e);
		}
	}
	
	@Override
	public void pushTag(String tagName) throws GitClientException {
		try {
			
			this.git.push().add(Constants.R_TAGS + tagName).call();
		} catch (GitAPIException e) {
			throw new GitClientException("Error while pushing tag: " + tagName, e);
		}
	}
	
	/**
     * Gets a reference to a local branch with the given name
     *
     * @param git        The git instance to use
     * @param branchName The name of the remote branch
     * @return A reference to the local branch or null
     * @throws com.atlassian.jgitflow.core.exception.JGitFlowIOException
     */
    private Ref getLocalBranch(String branchName) throws GitClientException
    {
        try
        {
            Ref ref2check = git.getRepository().findRef(branchName);
            Ref local = null;
            if (ref2check != null && ref2check.getName().startsWith(Constants.R_HEADS))
            {
                local = ref2check;
            }

            return local;
        }
        catch (IOException e)
        {
            throw new GitClientException("Error while retrieving branch " + branchName, e);
        }
    }
    
	@Override
	public boolean hasUncommitedChanges(boolean allowUntracked) throws GitClientException {
        log.debug("Verifying if working tree is clean");
        try
        {
            git.getRepository().getRefDatabase().refresh();
            IndexDiff diffIndex = new IndexDiff(git.getRepository(), Constants.HEAD, new FileTreeIterator(git.getRepository()));

            if (diffIndex.diff())
            {
                int addedSize = diffIndex.getAdded().size();
                int assumedSize = diffIndex.getAssumeUnchanged().size();
                int changedSize = diffIndex.getChanged().size();
                int conflictSize = diffIndex.getConflicting().size();
                int ignoredSize = diffIndex.getIgnoredNotInIndex().size();
                int missingSize = diffIndex.getMissing().size();
                int modifiedSize = diffIndex.getModified().size();
                int removedSize = diffIndex.getRemoved().size();
                int untrackedSize = diffIndex.getUntracked().size();
                int untrackedFolderSize = diffIndex.getUntrackedFolders().size();

                boolean changed = false;
                boolean untracked = false;
                StringBuilder sb = new StringBuilder();

                log.debug("diffIndex.diff() returned diffs. working tree is dirty!");
                log.debug("added size: " + addedSize);

                log.debug("assume unchanged size: " + assumedSize);
                log.debug("changed size: " + changedSize);

                log.debug("conflicting size: " + conflictSize);

                log.debug("ignored not in index size: " + ignoredSize);
                log.debug("missing size: " + missingSize);

                log.debug("modified size: " + modifiedSize);

                log.debug("removed size: " + removedSize);

                log.debug("untracked size: " + untrackedSize);

                log.debug("untracked folders size: " + untrackedFolderSize);


                if (addedSize > 0 || changedSize > 0 || conflictSize > 0 || missingSize > 0 || modifiedSize > 0 || removedSize > 0) {
                    changed = true;
                    sb.append("Working tree has uncommitted changes");
                }

                if (!allowUntracked && (untrackedSize > 0 || untrackedFolderSize > 0))
                {
                    if (ignoredSize > 0)
                    {
                        Set<String> ignores = diffIndex.getIgnoredNotInIndex();

                        if (untrackedSize > 0)
                        {
                            Set<String> utFiles = diffIndex.getUntracked();
                            utFiles.removeAll(ignores);

                            untrackedSize = utFiles.size();
                        }

                        if (untrackedFolderSize > 0)
                        {
                            Set<String> utFolders = diffIndex.getUntrackedFolders();
                            utFolders.removeAll(ignores);

                            untrackedFolderSize = utFolders.size();
                        }
                    }

                    if (untrackedSize > 0 || untrackedFolderSize > 0)
                    {
                        untracked = true;
                    }

                    if (!changed)
                    {
                        sb.append("Working tree has untracked files");
                    }
                    else
                    {
                        sb.append(" and untracked files");
                    }
                }
                
                log.debug("Working tree verification: " + sb.toString());

                return (untracked || changed);
            }

            log.debug("Working tree verification: Working tree is clean");
            return false;
        }
        catch (IOException e)
        {
        	log.error(e.getMessage());
            throw new GitClientException("Error while looking for uncommited changes", e);
        }
	}

	@Override
	public GitCommit getLastCommit(String branchName) {
		RevWalk rw = null;
		try {
			ObjectId headId;
			Ref branchHead = this.git.getRepository().findRef(branchName);
			if(branchHead != null) {
				log.debug("Branch Ref found: " + branchHead.getName());
				headId = branchHead.getObjectId();
			} else {
				log.debug("Branch Ref not found, probably the parameter is a commit HASH, not the branch name");
				log.debug("Trying to get the reference using resolve()");
				headId = this.git.getRepository().resolve(branchName);
				if(headId == null) {
					log.info("Branch Ref not found. Returning no Commit info!");
					return null;
				}
			} 
			
			rw = new RevWalk(this.git.getRepository());
			RevCommit revCommit = rw.parseCommit(headId);
			
			if(revCommit == null) return null;
			
			GitCommit result = new GitCommit();
			result.setHash(revCommit.name());
			result.setMessage(revCommit.getShortMessage());
			result.setUser(new GitUser(revCommit.getCommitterIdent().getName(), revCommit.getCommitterIdent().getEmailAddress()));
			result.setDate(new Date(revCommit.getCommitTime() * 1000L));
			
			return result;
		} catch(Exception e) {
			throw new GitClientException("Error while retriving the last commit", e);
		} finally {
			if(rw != null) {
				rw.dispose();
				rw.close();
			}
		}
	}
	
	@Override
	public String getCurrentBranchName() throws GitClientException {
		try {
			return this.git.getRepository().getBranch();
		} catch (IOException e) {
			throw new GitClientException("Error while retrieving the current branch", e);
		}
	}

	@Override
	public String getRemoteUrl(String remoteName) throws GitClientException {
		try {
			if(remoteName == null) remoteName = "origin";
			String key = String.format("%s.%s.%s", ConfigConstants.CONFIG_REMOTE_SECTION, remoteName, "url");
			return this.getConfig(key);
		} catch (Exception e) {
			throw new GitClientException("Error while retrieving the remote '" + remoteName + "' url", e);
		}
	}
	
	@Override
	public GitUser getConfiguredUser() throws GitClientException {
    	String name = this.getConfig("user.name");
    	String email = this.getConfig("user.email");
    	
    	if(name == null && email == null) return null;
    	return new GitUser(name, email);
    }
	
	@Override
	public void close() throws Exception {
		if(!this.isClosed()) this.git.close();
		this.git = null;
	}
	@Override
	public boolean repoExists() throws GitClientException {
		try {
			File gitDir = new RepositoryBuilder().readEnvironment().findGitDir().getGitDir();
			return (null != gitDir);
			
		} catch(Exception e) {
			throw new GitClientException("Error while initializing", e);
		}
	}
	@Override
	public void createRepo() throws GitClientException {
		if(this.repoExists()) throw new GitClientException("A Repository already exists in this directory");
		
		try {
			this.git = Git.init().call();
			
		} catch (GitAPIException e) {
            throw new GitClientException("Error while creating the repository", e);
        }
	}
	@Override
	public void createRepo(File gitDir) throws GitClientException {
		if(this.repoExists()) throw new GitClientException("A Repository already exists in this directory");
		
		try {
			this.git = Git.init().setDirectory(gitDir).call();
			
		} catch (GitAPIException e) {
            throw new GitClientException("Error while creating the repository", e);
        }
		
	}

	@Override
	public void stageFiles(List<String> filenames) {
		try {
			for(String file : filenames) {
				this.git.add().addFilepattern(file).call();
			}
		} catch (GitAPIException e) {
			throw new GitClientException("Error while adding files to commit list", e);
		}
	}

	@Override
	public void remoteRepoAdd(String remoteName, String url) throws GitClientException {
		try {
			RemoteAddCommand cmd = this.git.remoteAdd();
			cmd.setName(remoteName);
			cmd.setUri(new URIish(url));
			cmd.call();
		} catch(Exception e) {
			throw new GitClientException("Error while adding remote '" + remoteName + "'", e);
		}
	}

	@Override
	public void remoteRepoUpdateUrl(String remoteName, String url) {
		try {
			RemoteSetUrlCommand cmd = this.git.remoteSetUrl();
			cmd.setName(remoteName);
			cmd.setUri(new URIish(url));
			cmd.call();
		} catch(Exception e) {
			throw new GitClientException("Error while updating the URL for remote '" + remoteName, e);
		}
	}

	@Override
	public File getGitDirectory() {
		return this.git.getRepository().getDirectory();
	}

	@Override
	public List<String> getStagedFiles() {
		try {
			return this.git.diff()
				.setShowNameAndStatusOnly(true)
				.setCached(true)
				.call()
				.stream()
				.map(entry -> entry.getNewPath())
				.collect(Collectors.toList());
			
		} catch(Exception e) {
			throw new GitClientException("Error while retrieving staged files", e);
		}
	}

	@Override
	public void fetch() throws GitClientException {
		try {
			this.git.fetch().call();
		} catch (GitAPIException e) {
			throw new GitClientException("Error while fetching from remote", e);
		}
	}

	@Override
	public boolean repoLoaded() {
		return (this.git != null);
	}

	@Override
	public boolean isClosed() {
		// if the internal git client is null, it is closed
		return (this.git == null);
	}

	@Override
	public boolean branchExists(String branchName) throws GitClientException {
		return (this.findBranch(branchName) != null);
	}

	@Override
	public boolean tagExists(String tagName) throws GitClientException {
		return (this.findTag(tagName) != null);
	}

}
