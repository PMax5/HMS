package services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestsService {

    private static final String TESTS_INPUT_DIR = "tests/input";
    private static final String TESTS_OUTPUT_DIR = "tests/output/";
    private final List<Path> filePaths;
    private int currentTest;

    public TestsService() throws IOException {
        this.filePaths = Files.list(Paths.get(TESTS_INPUT_DIR)).collect(Collectors.toList());
        this.currentTest = 0;
    }

    public List<List<String>> getNextTestCommands() throws IOException {
        this.currentTest += 1;
        if (this.currentTest > this.filePaths.size()) {
            return null;
        }

        List<String> allLines = Files.lines(this.filePaths.get(this.currentTest - 1))
                .collect(Collectors.toList());

        List<List<String>> allArguments = new ArrayList<>();
        allLines.forEach(line -> {
            allArguments.add(Arrays.asList(line.split(" ")));
        });

        return allArguments;
    }

    private File getOutputFile() {
        Path currentTestFile = this.filePaths.get(this.currentTest - 1);
        return new File(TESTS_OUTPUT_DIR + currentTestFile.getFileName().toString() + "_output");
    }

    public void createOutputFile() throws IOException {
        File outputFile = this.getOutputFile();
        if (outputFile.createNewFile()) {
            System.out.println("[Tests Service] Created new test output file: " + outputFile.getAbsolutePath());
        } else {
            System.out.println("[Tests Service] File already exists: " + outputFile.getAbsolutePath());
        }
    }

    public void writeToOutputFile(String message) throws IOException {
        File outputFile = this.getOutputFile();
        Files.writeString(outputFile.toPath(), message);
    }

}
