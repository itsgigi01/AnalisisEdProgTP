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
        // Sucesores directos de ENTRY en el CFG
        // Solo estos nodos (primer nivel)
        // dependen de ENTRY
        // =====================================

        Set<Integer> directSuccessorsOfEntry =
                new HashSet<>();

        for (CFGBuilder.CFGEdge e : cfg.edges) {
            if (e.src == cfg.entry) {
                directSuccessorsOfEntry.add(e.dst);
            }
        }

        // =====================================
        // Dependencias desde ENTRY
        // Solo nodos de primer nivel que NO sean
        // IF, WHILE, ENTRY ni EXIT
        // =====================================

        for (CFGBuilder.CFGNode node :
                cfg.nodes.values()) {

            if (node.id == cfg.entry) continue;
            if (node.id == cfg.exit)  continue;

            // BUG 2 FIX: solo nodos directamente
            // conectados a ENTRY (primer nivel)
            if (!directSuccessorsOfEntry
                    .contains(node.id)) continue;

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
        // Solo sucesores que NO son el
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

                // BUG 1 FIX: se elimina el filtro
                // del back-edge del while.
                // El nodo WHILE SÍ debe tener
                // auto-arco (depende de sí mismo)
                // if (edge.dst == A) continue; ← ELIMINADO

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
