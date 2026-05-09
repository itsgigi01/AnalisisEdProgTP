import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        try {

            // ==========================================================
            // Leer programa de entrada
            // ==========================================================

            String input;

            if (args.length > 0) {
                input = Files.readString(Paths.get(args[0]));
            } else {
                input = Files.readString(Paths.get("mi_programa.txt"));
            }

            // ==========================================================
            // 1. PARSING (JavaCC)
            // ==========================================================

            ASTNode.Program program =
                    MiniLangParser.parseString(input);

            System.out.println();
            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  1. PARSING  (JavaCC)");
            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  Función : integer main()");
            System.out.println("  Sentencias en el cuerpo: " +
                    program.statements.size());
            System.out.println();

            // ==========================================================
            // 2. CONTROL FLOW GRAPH (CFG)
            // ==========================================================

            CFGBuilder builder = new CFGBuilder();
            CFGBuilder.CFG cfg = builder.build(program);

            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  2. CONTROL FLOW GRAPH (CFG)");
            System.out.println("══════════════════════════════════════════════════════════════════");

            System.out.println("  Nodos (" + cfg.nodes.size() + "):");

            for (CFGBuilder.CFGNode node : cfg.nodes.values()) {

                String suffix = "";

                if (node.id == cfg.entry) {
                    suffix = "  ← ENTRY";
                } else if (node.id == cfg.exit) {
                    suffix = "  ← EXIT";
                }

                System.out.printf(
                        "    [%d] %-24s%s%n",
                        node.id,
                        node.label,
                        suffix
                );
            }

            System.out.println();
            System.out.println("  Arcos (" + cfg.edges.size() + "):");

            for (CFGBuilder.CFGEdge edge : cfg.edges) {

                String suffix = "";

                if (edge.label != null &&
                        !edge.label.isBlank()) {
                    suffix = "  [" + edge.label + "]";
                }

                System.out.printf(
                        "    %d  →  %d%s%n",
                        edge.src,
                        edge.dst,
                        suffix
                );
            }

            System.out.println();

            // ==========================================================
            // Reaching Definitions
            // ==========================================================

            Analysis.computeReachingDefinitions(cfg);

            // ==========================================================
            // DDG
            // ==========================================================

            List<DDGEdge> ddg =
                    Analysis.buildDDG(cfg);

            // ==========================================================
            // 3. POSTDOMINADORES
            // ==========================================================

            Map<Integer, Set<Integer>> postdom =
                    computePostDominators(cfg);

            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  3. POSTDOMINADORES");
            System.out.println("══════════════════════════════════════════════════════════════════");

            for (CFGBuilder.CFGNode node : cfg.nodes.values()) {

                System.out.printf(
                        "  PostDom(%d: \"%s\") = %s%n",
                        node.id,
                        node.label,
                        formatSet(postdom.get(node.id))
                );
            }

            System.out.println();

            // ==========================================================
            // 4. ÁRBOL DE POSTDOMINADORES (ipdom)
            // ==========================================================

            Map<Integer, Integer> ipdom =
                    PostDominatorTree.compute(cfg);

            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  4. ÁRBOL DE POSTDOMINADORES  (ipdom)");
            System.out.println("══════════════════════════════════════════════════════════════════");

            for (CFGBuilder.CFGNode node : cfg.nodes.values()) {

                Integer parent = ipdom.get(node.id);

                if (parent == null) {

                    System.out.printf(
                            "  ipdom(%d: \"%s\") = null  (raíz del árbol)%n",
                            node.id,
                            node.label
                    );

                } else {

                    CFGBuilder.CFGNode parentNode =
                            cfg.nodes.get(parent);

                    System.out.printf(
                            "  ipdom(%d: \"%s\") = %d: \"%s\"%n",
                            node.id,
                            node.label,
                            parentNode.id,
                            parentNode.label
                    );
                }
            }

            System.out.println();

            // ==========================================================
            // 5. CONTROL DEPENDENCE GRAPH (CDG)
            // ==========================================================

            List<int[]> cdg =
                    CDGBuilder.build(cfg, ipdom);

            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  5. CONTROL DEPENDENCE GRAPH (CDG)");
            System.out.println("══════════════════════════════════════════════════════════════════");

            for (int[] edge : cdg) {

                CFGBuilder.CFGNode from =
                        cfg.nodes.get(edge[0]);

                CFGBuilder.CFGNode to =
                        cfg.nodes.get(edge[1]);

                System.out.printf(
                        "  %d:\"%s\"  →cd→  %d:\"%s\"%n",
                        from.id,
                        from.label,
                        to.id,
                        to.label
                );
            }

            System.out.println();

            // ==========================================================
            // 6. DATA DEPENDENCE GRAPH (DDG)
            // ==========================================================

            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  6. DATA DEPENDENCE GRAPH (DDG)");
            System.out.println("══════════════════════════════════════════════════════════════════");

            for (DDGEdge edge : ddg) {

                CFGBuilder.CFGNode from =
                        cfg.nodes.get(edge.from);

                CFGBuilder.CFGNode to =
                        cfg.nodes.get(edge.to);

                System.out.printf(
                        "  %d:\"%s\"  →dd→  %d:\"%s\" (%s)%n",
                        edge.from,
                        from.label,
                        edge.to,
                        to.label,
                        edge.variable
                );
            }

            System.out.println();

            // ==========================================================
            // Generar archivos DOT e imágenes
            // ==========================================================

            DotViz.writeAndRender(
                    DotViz.cfgToDot(cfg),
                    "cfg",
                    "output"
            );

            DotViz.writeAndRender(
                    DotViz.generateDDGDot(cfg, ddg),
                    "ddg",
                    "output"
            );

            DotViz.writeAndRender(
                    DotViz.postdomTreeToDot(cfg, ipdom),
                    "postdom_tree",
                    "output"
            );

            DotViz.writeAndRender(
                    DotViz.cdgToDot(cfg, cdg),
                    "cdg",
                    "output"
            );

            // ==========================================================
            // FIN
            // ==========================================================

            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  ANÁLISIS COMPLETADO");
            System.out.println("══════════════════════════════════════════════════════════════════");
            System.out.println("  Archivos generados en output/");
            System.out.println("    - cfg.png");
            System.out.println("    - postdom_tree.png");
            System.out.println("    - cdg.png");
            System.out.println("    - ddg.png");
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================================
    // Cálculo local de postdominadores
    // ==========================================================

    private static Map<Integer, Set<Integer>> computePostDominators(
            CFGBuilder.CFG cfg
    ) {

        Map<Integer, List<Integer>> succs =
                new HashMap<>();

        for (Integer id : cfg.nodes.keySet()) {
            succs.put(id, new ArrayList<>());
        }

        for (CFGBuilder.CFGEdge e : cfg.edges) {
            succs.get(e.src).add(e.dst);
        }

        Set<Integer> allNodes =
                new LinkedHashSet<>(cfg.nodes.keySet());

        Map<Integer, Set<Integer>> postdom =
                new HashMap<>();

        for (Integer n : allNodes) {

            Set<Integer> set =
                    new LinkedHashSet<>();

            if (n == cfg.exit) {
                set.add(n);
            } else {
                set.addAll(allNodes);
            }

            postdom.put(n, set);
        }

        boolean changed;

        do {

            changed = false;

            for (Integer n : allNodes) {

                if (n == cfg.exit) {
                    continue;
                }

                List<Integer> successors =
                        succs.get(n);

                if (successors.isEmpty()) {
                    continue;
                }

                Set<Integer> newSet =
                        new LinkedHashSet<>(allNodes);

                for (Integer s : successors) {
                    newSet.retainAll(postdom.get(s));
                }

                newSet.add(n);

                if (!newSet.equals(postdom.get(n))) {
                    postdom.put(n, newSet);
                    changed = true;
                }
            }

        } while (changed);

        return postdom;
    }

    // ==========================================================
    // Formatear conjuntos
    // ==========================================================

    private static String formatSet(Set<Integer> set) {

        if (set == null || set.isEmpty()) {
            return "{ }";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{ ");

        boolean first = true;

        for (Integer value : set) {

            if (!first) {
                sb.append(", ");
            }

            sb.append(value);
            first = false;
        }

        sb.append(" }");

        return sb.toString();
    }
}