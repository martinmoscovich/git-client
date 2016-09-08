## Git Client for Java

This library allows to interact with a git repository (local and remote).
It has two modes:
 - Native Java (wraps the excellent JGit library into a simpler but less powerful API)
 - Command Line (uses the git client installed on the OS)

The Native client should be used in general. But the command line contains all the features, while some
may be missing in JGit implementation. In that case, the command line client should be used.

### Note: This library is a WIP