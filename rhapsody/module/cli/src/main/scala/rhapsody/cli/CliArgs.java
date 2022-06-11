package rhapsody.cli;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class CliArgs {
    @Parameter(variableArity = true)
    public List<String> inputFilePaths = new ArrayList<>();

    @Parameter(names = {"-o"}, description = "path to the output .ttl file; if not specified, print Turtle to stdout")
    public String outputFilePath = null;

    @Parameter(names = {"--validate-only"}, description = "only validate the inputs, don't transform them")
    public boolean validateOnly = false;

    @Parameter(names = {"--xsd"}, description = "path to an .xsd file to use for validation, only works with --validate-only")
    public String xsdFilePath = null;
}
