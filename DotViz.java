import java.io.*;
import java.util.*;

public class DotViz {

    private static String nodeAttrs(
            CFGBuilder.NodeType type
    ) {

        switch (type) {

            case ENTRY:
                return "shape=ellipse, style=filled, " +
                       "fillcolor=\"#4CAF50\", fontcolor=white";

            case EXIT:
                return "shape=ellipse, style=filled, " +
                       "fillcolor=\"#F44336\", fontcolor=white";

            case ASSIGN:
                return "shape=box, style=filled, " +
                       "fillcolor=\"#ECEFF1\", fontcolor=black";

            case RETURN:
                return "shape=box, style=filled, " +
                       "fillcolor=\"#FF9800\", fontcolor=white";

            case IF:
                return "shape=diamond, style=filled, " +
                       "fillcolor=\"#2196F3\", fontcolor=white";

            case WHILE:
                return "shape=diamond, style=filled, " +
                       "fillcolor=\"#FF9800\", fontcolor=white";
        }

        return "";
    }

    private static String esc(String s) {

        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static void appendNodes(
            StringBuilder sb,
            CFGBuilder.CFG cfg
    ) {

        sb.append(
                "  node [fontname=Helvetica, fontsize=12];\n\n"
        );

        for (CFGBuilder.CFGNode n :
                cfg.nodes.values()) {

            sb.append(
                    String.format(
                            "  %d [label=\"%s\", %s];\n",
                            n.id,
                            esc(n.label),
                            nodeAttrs(n.type)
                    )
            );
        }

        sb.append("\n");
    }

    // =========================================
    // CFG
    // =========================================

    public static String cfgToDot(
            CFGBuilder.CFG cfg
    ) {

        StringBuilder sb =
                new StringBuilder();

        sb.append("digraph CFG {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  label=\"Control Flow Graph\";\n");
        sb.append("  labelloc=t;\n");

        appendNodes(sb, cfg);

        for (CFGBuilder.CFGEdge e :
                cfg.edges) {

            String attrs = "";

            if (!e.label.isEmpty()) {

                attrs =
                        " [label=\"" +
                        e.label +
                        "\"]";
            }

            sb.append(
                    String.format(
                            "  %d -> %d%s;\n",
                            e.src,
                            e.dst,
                            attrs
                    )
            );
        }

        sb.append("}\n");

        return sb.toString();
    }

    // =========================================
    // DDG
    // =========================================

    public static String generateDDGDot(
            CFGBuilder.CFG cfg,
            List<DDGEdge> ddg
    ) {

        StringBuilder sb =
                new StringBuilder();

        sb.append("digraph DDG {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  label=\"Data Dependence Graph\";\n");
        sb.append("  labelloc=t;\n");

        appendNodes(sb, cfg);

        for (DDGEdge edge : ddg) {

            sb.append(
                    String.format(
                            "  %d -> %d [label=\"%s\", " +
                            "color=\"#009688\"];\n",
                            edge.from,
                            edge.to,
                            edge.variable
                    )
            );
        }

        sb.append("}\n");

        return sb.toString();
    }

    // =========================================
    // POST DOM TREE
    // =========================================

    public static String postdomTreeToDot(
            CFGBuilder.CFG cfg,
            Map<Integer, Integer> ipdom
    ) {

        StringBuilder sb =
                new StringBuilder();

        sb.append("digraph PostDomTree {\n");
        sb.append("  rankdir=BT;\n");
        sb.append("  label=\"Árbol de Postdominadores\";\n");
        sb.append("  labelloc=t;\n");
        sb.append(
                "  node [fontname=Helvetica, fontsize=12];\n\n"
        );

        // Calcular a quién apunta cada nodo (su ipdom)
        // y quiénes son hijos directos de cada nodo
        Map<Integer, List<Integer>> children =
                new HashMap<>();

        for (int n : cfg.nodes.keySet()) {
            children.put(n, new ArrayList<>());
        }

        for (Map.Entry<Integer, Integer> e :
                ipdom.entrySet()) {

            if (e.getValue() != null) {
                children.get(e.getValue())
                        .add(e.getKey());
            }
        }

        // Asignar ranks por nivel del árbol
        // nivel 0 = EXIT (raiz), nivel 1 = hijos de EXIT, etc.
        Map<Integer, Integer> level =
                new HashMap<>();

        Queue<Integer> queue =
                new LinkedList<>();

        level.put(cfg.exit, 0);
        queue.add(cfg.exit);

        while (!queue.isEmpty()) {

            int cur = queue.poll();

            for (int child : children.get(cur)) {

                if (!level.containsKey(child)) {
                    level.put(child,
                            level.get(cur) + 1);
                    queue.add(child);
                }
            }
        }

        // Agrupar nodos por nivel
        Map<Integer, List<Integer>> byLevel =
                new HashMap<>();

        for (Map.Entry<Integer, Integer> e :
                level.entrySet()) {

            byLevel.computeIfAbsent(
                    e.getValue(),
                    k -> new ArrayList<>()
            ).add(e.getKey());
        }

        // Emitir rank=same por nivel
        for (Map.Entry<Integer, List<Integer>> e :
                byLevel.entrySet()) {

            sb.append("  { rank=same;");

            for (int n : e.getValue()) {
                sb.append(" " + n + ";");
            }

            sb.append(" }\n");
        }

        sb.append("\n");

        // Nodos
        for (CFGBuilder.CFGNode n :
                cfg.nodes.values()) {

            sb.append(
                    String.format(
                            "  %d [label=\"%s\", %s];\n",
                            n.id,
                            esc(n.label),
                            nodeAttrs(n.type)
                    )
            );
        }

        sb.append("\n");

        // Aristas
        for (Map.Entry<Integer, Integer> e :
                ipdom.entrySet()) {

            if (e.getValue() != null) {

                sb.append(
                        String.format(
                                "  %d -> %d " +
                                "[style=dashed, " +
                                "color=\"#607D8B\"];\n",
                                e.getKey(),
                                e.getValue()
                        )
                );
            }
        }

        sb.append("}\n");

        return sb.toString();
    }

    // =========================================
    // CDG
    // =========================================

    public static String cdgToDot(
            CFGBuilder.CFG cfg,
            List<int[]> cdg
    ) {

        StringBuilder sb =
                new StringBuilder();

        sb.append("digraph CDG {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  label=\"Control Dependence Graph\";\n");
        sb.append("  labelloc=t;\n");

        appendNodes(sb, cfg);

        for (int[] edge : cdg) {

            sb.append(
                    String.format(
                            "  %d -> %d;\n",
                            edge[0],
                            edge[1]
                    )
            );
        }

        sb.append("}\n");

        return sb.toString();
    }

    // =========================================
    // WRITE + RENDER
    // =========================================

    public static void writeAndRender(
            String dotSrc,
            String name,
            String outDir
    ) throws IOException {

        File dir = new File(outDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String dotPath =
                outDir +
                File.separator +
                name +
                ".dot";

        try (PrintWriter pw =
                     new PrintWriter(
                             new FileWriter(dotPath)
                     )) {

            pw.print(dotSrc);
        }

        System.out.println(
                "DOT guardado: " + dotPath
        );

        String pngPath =
                outDir +
                File.separator +
                name +
                ".png";

        try {

            ProcessBuilder pb =
                    new ProcessBuilder(
                            "dot",
                            "-Tpng",
                            dotPath,
                            "-o",
                            pngPath
                    );

            pb.redirectErrorStream(true);

            Process proc = pb.start();

            proc.waitFor();

            System.out.println(
                    "PNG generado: " + pngPath
            );

        } catch (Exception e) {

            System.out.println(
                    "Graphviz no encontrado."
            );
        }
    }
}