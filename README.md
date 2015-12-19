# MPIProgrammingChallenge
Build and Running Instructions:
Using a terminal, go into the src directory of this repo and call "javac MismatchDetector.java" to create the classfile.
Then call "java MismatchDetector path-to-log-file" to run the program on the given logfile. For example, the relative path from the src directory to log file in this repo would be ../logs/mpe_log_with_errors.log
The output will print "no mismatches" or a list of messages that could not be matched. Significant exceptions (such as IOExceptions or Number Parsing Exceptions) will be printed to the terminal as well. 
