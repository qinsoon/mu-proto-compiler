package compiler.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import compiler.UVMCompiler;

public class DotGraph {
    StringBuilder dot = new StringBuilder();
    
    String name;
    
    public DotGraph(String name) {
        this.name = name;
    }
    
    public void newSequencialSubgraph(String name, List<String> innerNodes) {
        dot.append(String.format("subgraph \"cluster_%s\" {", name));
        dot.append(String.format("label = \"%s\";", name));
        for (int i = 0; i < innerNodes.size() - 1; i++) {
            dot.append(String.format("\"%s\" -> \"%s\";", innerNodes.get(i), innerNodes.get(i+1)));
        }
        dot.append("}");
        dot.append('\n');
    }
    
    public void newEdge(String from, String to) {
        dot.append(String.format("\"%s\" -> \"%s\";", from, to));
        dot.append('\n');
    }
    
    public void output(String file) {
        BufferedWriter writer = null;
        try {
            File outFile = new File(file);
            outFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(outFile));
            
            writer.write(String.format("digraph \"%s\" {\n", name));
            writer.write(dot.toString());
            writer.write("}");
        } catch (IOException e) {
            UVMCompiler.error("Error when writing dot file: " + file);
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
