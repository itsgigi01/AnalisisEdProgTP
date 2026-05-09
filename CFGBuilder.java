import java.util.*;

public class CFGBuilder {

    public enum NodeType {
        ENTRY,
        EXIT,
        ASSIGN,
        IF,
        WHILE,
        RETURN
    }

    public static class CFGNode {

        public int id;
        public String label;
        public NodeType type;

        public Set<String> defs = new HashSet<>();
        public Set<String> uses = new HashSet<>();

        public Set<Definition> gen = new HashSet<>();
        public Set<Definition> kill = new HashSet<>();

        public Set<Definition> in = new HashSet<>();
        public Set<Definition> out = new HashSet<>();

        public CFGNode(int id, String label, NodeType type) {
            this.id = id;
            this.label = label;
            this.type = type;
        }
    }

    public static class CFGEdge {

        public int src;
        public int dst;
        public String label;

        public CFGEdge(int src, int dst, String label) {
            this.src = src;
            this.dst = dst;
            this.label = label;
        }
    }

    public static class CFG {

        public Map<Integer, CFGNode> nodes =
                new LinkedHashMap<>();

        public List<CFGEdge> edges =
                new ArrayList<>();

        public int entry;
        public int exit;

        public void addNode(CFGNode n) {
            nodes.put(n.id, n);
        }

        public void addEdge(int src, int dst) {
            addEdge(src, dst, "");
        }

        public void addEdge(
                int src,
                int dst,
                String label
        ) {
            edges.add(
                    new CFGEdge(src, dst, label)
            );
        }
    }

    private final CFG cfg = new CFG();

    private int nextId = 0;

    private int newNode(
            String label,
            NodeType type
    ) {

        int id = nextId++;

        cfg.addNode(
                new CFGNode(id, label, type)
        );

        return id;
    }

    // =====================================================
    // BUILD
    // =====================================================

    public CFG build(ASTNode.Program program) {

        int entry = newNode(
                "ENTRY",
                NodeType.ENTRY
        );

        cfg.entry = entry;

        List<Integer> exits =
                new ArrayList<>();

        exits.add(entry);

        for (ASTNode.Stmt stmt :
                program.statements) {

            exits = buildStmt(stmt, exits);
        }

        int exit = newNode(
                "EXIT",
                NodeType.EXIT
        );

        cfg.exit = exit;

        for (int e : exits) {
            cfg.addEdge(e, exit);
        }

        return cfg;
    }

    // =====================================================
    // STMT
    // =====================================================

    private List<Integer> buildStmt(
            ASTNode.Stmt stmt,
            List<Integer> prev
    ) {

        if (stmt instanceof ASTNode.AssignStmt s) {
            return buildAssign(s, prev);
        }

        if (stmt instanceof ASTNode.IfStmt s) {
            return buildIf(s, prev);
        }

        if (stmt instanceof ASTNode.WhileStmt s) {
            return buildWhile(s, prev);
        }

        if (stmt instanceof ASTNode.ReturnStmt s) {
            return buildReturn(s, prev);
        }

        return prev;
    }

    // =====================================================
    // ASSIGN
    // =====================================================

    private List<Integer> buildAssign(
            ASTNode.AssignStmt s,
            List<Integer> prev
    ) {

        int nid = newNode(
                s.target + " = " + s.expr,
                NodeType.ASSIGN
        );

        CFGNode node =
                cfg.nodes.get(nid);

        node.defs.add(s.target);

        node.uses.addAll(
                s.expr.getUsedVariables()
        );

        node.gen.add(
                new Definition(
                        s.target,
                        nid
                )
        );

        for (int p : prev) {
            cfg.addEdge(p, nid);
        }

        return List.of(nid);
    }

    // =====================================================
    // RETURN
    // =====================================================

    private List<Integer> buildReturn(
            ASTNode.ReturnStmt s,
            List<Integer> prev
    ) {

        int nid = newNode(
                "return " + s.expr,
                NodeType.RETURN
        );

        CFGNode node =
                cfg.nodes.get(nid);

        node.uses.addAll(
                s.expr.getUsedVariables()
        );

        for (int p : prev) {
            cfg.addEdge(p, nid);
        }

        return List.of(nid);
    }

    // =====================================================
    // IF
    // =====================================================

    private List<Integer> buildIf(
            ASTNode.IfStmt s,
            List<Integer> prev
    ) {

        int cond = newNode(
                "if (" + s.condition + ")",
                NodeType.IF
        );

        CFGNode condNode =
                cfg.nodes.get(cond);

        condNode.uses.addAll(
                s.condition.getUsedVariables()
        );

        for (int p : prev) {
            cfg.addEdge(p, cond);
        }

        List<Integer> thenPrev =
                List.of(cond);

        List<Integer> thenExits =
                thenPrev;

        for (ASTNode.Stmt stmt :
                s.thenBranch) {

            thenExits =
                    buildStmt(stmt, thenExits);
        }

        List<Integer> elsePrev =
                List.of(cond);

        List<Integer> elseExits =
                elsePrev;

        for (ASTNode.Stmt stmt :
                s.elseBranch) {

            elseExits =
                    buildStmt(stmt, elseExits);
        }

        for (CFGEdge e : cfg.edges) {

            if (e.src == cond) {

                if (e.dst ==
                        thenExits.get(0)) {

                    e.label = "True";
                }

                else if (e.dst ==
                        elseExits.get(0)) {

                    e.label = "False";
                }
            }
        }

        List<Integer> exits =
                new ArrayList<>();

        exits.addAll(thenExits);
        exits.addAll(elseExits);

        return exits;
    }

    // =====================================================
    // WHILE
    // =====================================================

    private List<Integer> buildWhile(
            ASTNode.WhileStmt s,
            List<Integer> prev
    ) {

        int cond = newNode(
                "while (" + s.condition + ")",
                NodeType.WHILE
        );

        CFGNode condNode =
                cfg.nodes.get(cond);

        condNode.uses.addAll(
                s.condition.getUsedVariables()
        );

        for (int p : prev) {
            cfg.addEdge(p, cond);
        }

        List<Integer> bodyExits =
                List.of(cond);

        for (ASTNode.Stmt stmt :
                s.body) {

            bodyExits =
                    buildStmt(stmt, bodyExits);
        }

        for (int e : bodyExits) {
            cfg.addEdge(e, cond);
        }

        return List.of(cond);
    }
}