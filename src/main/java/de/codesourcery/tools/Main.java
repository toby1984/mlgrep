package de.codesourcery.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Main
{
    private static void printHelp() {
        System.err.println();
        System.err.println( "Usage: [-v] [--help|-h|-help] [ --linestart-regex <Java regex>] [-f|--file <input file>] <Java regex to match>" );
        System.err.println( "-v                        => Enable verbose output");
        System.err.println( "[--help|-h|-help]         => Print this help.");
        System.err.println( "--linestart-regex <regex> => RegEx used to match start of log line");
        System.err.println( "--file|-f                 => Input file, defaults to StdIn when option is not given");
        System.err.println( "<regex>                   => Regex to look for in each log line");
    }

    public static void main(String[] args) throws IOException
    {
        final Predicate<String> isHelp = x -> x.trim().equalsIgnoreCase( "--help" ) || x.trim().equalsIgnoreCase( "-help" ) || x.trim().equals( "-h" );
        if ( args.length < 1 )
        {
            System.err.println( "ERROR: Invalid command line." );
            printHelp();
            System.exit(3);
        }

        File file = null;
        String lineStartRegEx = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*";
        String matchRegEx = null;
        boolean verbose = false;
        final Iterator<String> it = Arrays.stream( args ).iterator();
        while ( it.hasNext() )
        {
            final String arg = it.next();
            if ( isHelp.test( arg ) ) {
                printHelp();
                System.exit(2);
            }
            if ( arg.equals("-v" ) ) {
                verbose = true;
                continue;
            }
            if ( arg.equals("-f") || arg.equals("--file") )
            {
                if ( ! it.hasNext() )
                {
                    System.err.println( "ERROR: " + arg + " option needs an argument" );
                    System.exit( 3 );
                }
                file = new File( it.next() );
                if ( ! file.exists() || !file.canRead() )
                {
                    System.err.println( "ERROR: File " + file.getAbsolutePath() + " does not exist or is not readable." );
                    System.exit( 3 );
                }
                continue;
            }
            if ( arg.equals("--linestart-regex") )
            {
                if ( !it.hasNext() )
                {
                    System.err.println( "ERROR: " + arg + " option requires a parameter" );
                    System.exit( 3 );
                }
                lineStartRegEx = it.next();
                continue;
            }
            if ( matchRegEx != null ) {
                System.err.println("ERROR: More than one pattern on command line.");
                printHelp();
                System.exit(3);
            }
            if ( verbose ) {
                System.out.println("INFO: Going to match on '"+arg+"'");
            }
            matchRegEx = arg;
        }

        if ( matchRegEx == null ) {
            System.err.println("ERROR: You need to specify a regex to match.");
            printHelp();
            System.exit(3);
        }

        if ( verbose ) {
            System.out.println( "INFO: Reading from " + (file == null ? "std-in" : file.getAbsolutePath()) );
            System.out.println( "INFO: Matching start of log line using '" + lineStartRegEx + "'" );
        }
        final Pattern logLineStart = Pattern.compile( lineStartRegEx );
        Pattern toSearch = Pattern.compile( matchRegEx );

        final List<String> buffer = new ArrayList<>();
        boolean matched = false;
        try ( BufferedReader reader = new BufferedReader( file == null ? new InputStreamReader( System.in ) : new FileReader( file ) ) )
        {

            int lineNo = 0;
            String currentLine;
            String previousLine = null;
            while ((currentLine = reader.readLine()) != null)
            {
                lineNo++;
                if ( ! matched && verbose && (lineNo % 10000) == 0 )
                {
                    System.out.println( "INFO: Lines scanned without recognizing a log line: " + lineNo );
                }
                if ( previousLine == null )
                {
                    previousLine = currentLine;
                    continue;
                }
                if ( logLineStart.matcher( previousLine ).matches() )
                {
                    matched = true;
                    if ( logLineStart.matcher( currentLine ).matches() )
                    {
                        if ( toSearch.matcher( previousLine ).matches() )
                        {
                            System.out.println( previousLine );
                        }
                    }
                    else
                    {
                        buffer.clear();
                        buffer.add( previousLine );
                        buffer.add( currentLine );
                        while ((currentLine = reader.readLine()) != null)
                        {
                            if ( ! logLineStart.matcher( currentLine ).matches() )
                            {
                                buffer.add( currentLine );
                            }
                            else
                            {
                                break;
                            }
                        }
                        final String txt = String.join( " ", buffer );
                        if ( toSearch.matcher( txt ).matches() )
                        {
                            System.out.println( String.join( "\n", buffer ) );
                        }
                    }
                }
                previousLine = currentLine;
            }
            // handle case where input contained only one line
            if ( lineNo == 1 )
            {
                if ( logLineStart.matcher( previousLine ).matches() && toSearch.matcher( previousLine ).matches() )
                {
                    System.out.println( previousLine );
                }
            }
        }
        System.exit( matched ? 0 : 1 );
    }
}