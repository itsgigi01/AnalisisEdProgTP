import java.util.*;

public class Analysis {

    // =========================================
    // REACHING DEFINITIONS
    // =========================================

    public static void computeReachingDefinitions(
            CFGBuilder.CFG cfg
    ) {

        // =========================
        // KILL
        // =========================

        for (CFGBuilder.CFGNode node :
                cfg.nodes.values()) {

            for (String defVar : node.defs) {

                for (CFGBuilder.CFGNode other :
                        cfg.nodes.values()) {

                    if (other.id == node.id) {
                        continue;
                    }

                    if (other.defs.contains(defVar)) {

                        node.kill.add(
                                new Definition(
                                        defVar,
                                        other.id
                                )
                        );
                    }
                }
            }
        }

        // =========================
        // Fixed Point
        // =========================

        boolean changed;

        do {

            changed = false;

            for (CFGBuilder.CFGNode node :
                    cfg.nodes.values()) {

                Set<Definition> oldIn =
                        new HashSet<>(node.in);

                Set<Definition> oldOut =
                        new HashSet<>(node.out);

                // IN(n)

                node.in.clear();

                for (CFGBuilder.CFGEdge e :
                        cfg.edges) {

                    if (e.dst == node.id) {

                        CFGBuilder.CFGNode pred =
                                cfg.nodes.get(e.src);

                        node.in.addAll(pred.out);
                    }
                }

                // OUT(n)

                node.out.clear();

                Set<Definition> temp =
                        new HashSet<>(node.in);

                temp.removeAll(node.kill);

                node.out.addAll(temp);

                node.out.addAll(node.gen);

                if (!oldIn.equals(node.in)
                        || !oldOut.equals(node.out)) {

                    changed = true;
                }
            }

        } while (changed);
    }

    // =========================================
    // DDG
    // =========================================

    public static List<DDGEdge> buildDDG(
            CFGBuilder.CFG cfg
    ) {

        List<DDGEdge> ddg =
                new ArrayList<>();

        Set<String> added =
                new HashSet<>();

        for (CFGBuilder.CFGNode node :
                cfg.nodes.values()) {

            for (String usedVar :
                    node.uses) {

                for (Definition def :
                        node.in) {

                    if (!def.variable.equals(usedVar)) {
                        continue;
                    }

                    // Filtrar autoarcos:
                    // el nodo no puede depender
                    // de su propia definicion
                    if (def.nodeId == node.id) {
                        continue;
                    }

                    // Filtrar duplicados
                    String key =
                            def.nodeId +
                            "->" +
                            node.id +
                            ":" +
                            usedVar;

                    if (added.contains(key)) {
                        continue;
                    }

                    added.add(key);

                    ddg.add(
                            new DDGEdge(
                                    def.nodeId,
                                    node.id,
                                    usedVar
                            )
                    );
                }
            }
        }

        return ddg;
    }
}