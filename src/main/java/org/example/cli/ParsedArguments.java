package org.example.cli;

public class ParsedArguments {
    private final String inputPath;
    private final String rootClass;
    private final String packageName;
    private final String outDir;
    private final boolean innerClasses;

    public ParsedArguments(String inputPath, String rootClass, String packageName, String outDir, boolean innerClasses) {
        this.inputPath = inputPath;
        this.rootClass = rootClass;
        this.packageName = packageName;
        this.outDir = outDir;
        this.innerClasses = innerClasses;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getRootClass() {
        return rootClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getOutDir() {
        return outDir;
    }

    public boolean isInnerClasses() {
        return innerClasses;
    }
}
