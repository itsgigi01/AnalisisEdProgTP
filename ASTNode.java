import java.util.HashSet;
import java.util.Set;

public abstract class ASTNode {

    // =========================
    // Statements
    // =========================

    public abstract static class Stmt extends ASTNode {
    }

    public static class Program extends ASTNode {

        public final java.util.List<Stmt> statements;

        public Program(java.util.List<Stmt> statements) {
            this.statements = statements;
        }
    }

    public static class AssignStmt extends Stmt {

        public final String target;
        public final Expr expr;

        public AssignStmt(String target, Expr expr) {
            this.target = target;
            this.expr = expr;
        }

        @Override
        public String toString() {
            return target + " = " + expr;
        }
    }

    public static class PrintStmt extends Stmt {

        public final Expr expr;

        public PrintStmt(Expr expr) {
            this.expr = expr;
        }

        @Override
        public String toString() {
            return "print(" + expr + ")";
        }
    }

    public static class IfStmt extends Stmt {

        public final Expr condition;
        public final java.util.List<Stmt> thenBranch;
        public final java.util.List<Stmt> elseBranch;

        public IfStmt(
                Expr condition,
                java.util.List<Stmt> thenBranch,
                java.util.List<Stmt> elseBranch
        ) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public String toString() {
            return "if (" + condition + ")";
        }
    }

    public static class WhileStmt extends Stmt {

        public final Expr condition;
        public final java.util.List<Stmt> body;

        public WhileStmt(
                Expr condition,
                java.util.List<Stmt> body
        ) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public String toString() {
            return "while (" + condition + ")";
        }
    }

    // =========================
    // Expressions
    // =========================

    public abstract static class Expr extends ASTNode {

        public abstract Set<String> getUsedVariables();
    }

    public static class ValueExpr extends Expr {

        public final String value;
        public final boolean isVariable;

        public ValueExpr(String value, boolean isVariable) {
            this.value = value;
            this.isVariable = isVariable;
        }

        @Override
        public Set<String> getUsedVariables() {

            Set<String> vars = new HashSet<>();

            if (isVariable) {
                vars.add(value);
            }

            return vars;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class BinopExpr extends Expr {

        public final Expr left;
        public final String op;
        public final Expr right;

        public BinopExpr(Expr left, String op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public Set<String> getUsedVariables() {

            Set<String> vars = new HashSet<>();

            vars.addAll(left.getUsedVariables());
            vars.addAll(right.getUsedVariables());

            return vars;
        }

        @Override
        public String toString() {
            return "(" + left + " " + op + " " + right + ")";
        }
    }

    public static class ReturnStmt extends Stmt {

    public final Expr expr;

    public ReturnStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        return "return " + expr;
    }
}
}