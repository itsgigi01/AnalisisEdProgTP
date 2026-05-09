import java.util.*;

public class CDGBuilder {

    public static List<int[]> build(
            CFGBuilder.CFG cfg,
            Map<Integer, Integer> ipdom
    ) {

        List<int[]> cdg =
                new ArrayList<>();

        Set<String> added =
                new HashSet<>();

        // =====================================
        // Dependencias desde ENTRY
        // todos los nodos excepto ENTRY, EXIT,
        // IF y WHILE
        // =====================================

        for (CFGBuilder.CFGNode node :
                cfg.nodes.values()) {

            if (node.id == cfg.entry) continue;
            if (node.id == cfg.exit)  continue;
            if (node.type == CFGBuilder.NodeType.IF
                    || node.type == CFGBuilder.NodeType.WHILE) continue;

            String key = cfg.entry + "-" + node.id;

            if (!added.contains(key)) {
                cdg.add(new int[]{cfg.entry, node.id});
                added.add(key);
            }
        }

        // =====================================
        // Dependencias desde IF / WHILE
        // solo sucesores que NO son el
        // post-dominador inmediato del predicado
        // =====================================

        for (CFGBuilder.CFGNode node :
                cfg.nodes.values()) {

            if (node.type != CFGBuilder.NodeType.IF
                    && node.type != CFGBuilder.NodeType.WHILE) {
                continue;
            }

            int A = node.id;
            Integer ipdomA = ipdom.get(A);

            for (CFGBuilder.CFGEdge edge :
                    cfg.edges) {

                if (edge.src != A) continue;

                // Saltar el sucesor que es
                // el post-dominador inmediato
                // de A — ese no depende de A
                if (ipdomA != null
                        && edge.dst == ipdomA) continue;

                // Saltar el back-edge del while
                // (el sucesor que es el propio A)
                if (edge.dst == A) continue;

                String key = A + "-" + edge.dst;

                if (!added.contains(key)) {
                    cdg.add(new int[]{A, edge.dst});
                    added.add(key);
                }
            }

            // ENTRY apunta al IF/WHILE
            String key = cfg.entry + "-" + A;
            if (!added.contains(key)) {
                cdg.add(new int[]{cfg.entry, A});
                added.add(key);
            }
        }

        return cdg;
    }
}