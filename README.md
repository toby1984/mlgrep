# What's this ?
A simple Java command-line tool that does regex matching on multi-line (log) files or stdin.

This is just a quick hack to avoid having to remember the syntax to do multi-line string matching using pcrgrep etc.

The program reads input from either stdin or a file and looks for a start-of-line regex to recognize log lines. Any line that does not
match this regex but has been preceded by one is assumed to be part of a multi-line log line and those lines will 
be treated as a single line, replacing any linefeed character with a single whitespace before matching the actual regex.
Any matching lines will be printed to stdout.

# Building

You'll need Maven >= 3.6 and JDK >=17
Just run `mvn clean package` and you should find a target/mlgrep.jar self-executable JAR file.

# Running

`java -jar target/mlgrep.jar [-v] [--help|-h|-help] [ --linestart-regex <Java regex>] [-f|--file <input file>] <Java regex to match>`

The supported options are:

        -v                        => Enable verbose output
        --help|-h|-help           => Print this help
        --linestart-regex <regex> => RegEx used to match start of log line
        --file|-f                 => Input file, defaults to StdIn when option is not given
        <regex>                   => Regex to look for in each log line
