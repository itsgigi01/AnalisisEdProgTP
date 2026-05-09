import java.util.*;

public class PostDominatorTree {

    public static Map<Integer, Integer> compute(
            CFGBuilder.CFG cfg
    ) {

        // =========================
        // Construir grafo inverso
        // (sucesores en CFG original
        //  = predecesores en inverso)
        // =========================

        Map<Integer, List<Integer>> succs =
                new HashMap<>();

        Map<Integer, List<Integer>> preds =
                new HashMap<>();

        for (int n : cfg.nodes.keySet()) {
            succs.put(n, new ArrayList<>());
            preds.put(n, new ArrayList<>());
        }

        for (CFGBuilder.CFGEdge e : cfg.edges) {
            succs.get(e.src).add(e.dst);
            preds.get(e.dst).add(e.src);
        }

        Set<Integer> allNodes =
                cfg.nodes.keySet();

        // =========================
        // INIT
        // =========================

        Map<Integer, Set<Integer>> postdom =
                new HashMap<>();

        for (int n : allNodes) {

            postdom.put(n, new HashSet<>());

            if (n == cfg.exit) {
                postdom.get(n).add(n);
            } else {
                postdom.get(n).addAll(allNodes);
            }
        }

        // =========================
        // FIXED POINT
        // iteramos en orden topologico
        // inverso (BFS desde EXIT)
        // =========================

        boolean changed;

        do {

            changed = false;

            for (int n : allNodes) {

                if (n == cfg.exit) {
                    continue;
                }

                List<Integer> successors =
                        succs.get(n);

                if (successors.isEmpty()) {
                    continue;
                }

                Set<Integer> newSet =
                        new HashSet<>(allNodes);

                for (int s : successors) {
                    newSet.retainAll(
                            postdom.get(s)
                    );
                }

                newSet.add(n);

                if (!newSet.equals(
                        postdom.get(n))) {

                    postdom.put(n, newSet);
                    changed = true;
                }
            }

        } while (changed);

        // =========================
        // IMMEDIATE POSTDOM
        // Para cada nodo n, el ipdom
        // es el post-dom mas cercano:
        // el c en postdom(n)-{n} tal
        // que no hay otro d en
        // postdom(n)-{n} con
        // c en postdom(d)
        // =========================

        Map<Integer, Integer> ipdom =
                new HashMap<>();

        for (int n : allNodes) {

            if (n == cfg.exit) {
                ipdom.put(n, null);
                continue;
            }

            Set<Integer> candidates =
                    new HashSet<>(postdom.get(n));

            candidates.remove(n);

            Integer imm = null;

            for (int c : candidates) {

                boolean immediate = true;

                for (int d : candidates) {

                    if (d == c) {
                        continue;
                    }

                    // c no es inmediato si
                    // hay un d que esta entre
                    // n y c, es decir d postdom n
                    // y c postdom d
                    if (postdom.get(d).contains(c)
                            && !postdom.get(c)
                                .contains(d)) {

                        immediate = false;
                        break;
                    }
                }

                if (immediate) {
                    imm = c;
                    break;
                }
            }

            ipdom.put(n, imm);
        }

        return ipdom;
    }
}