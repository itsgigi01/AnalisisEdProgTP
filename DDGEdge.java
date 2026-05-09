public class DDGEdge {

    public final int from;
    public final int to;
    public final String variable;

    public DDGEdge(int from, int to, String variable) {
        this.from = from;
        this.to = to;
        this.variable = variable;
    }

    @Override
    public String toString() {
        return from + " -> " + to + " [" + variable + "]";
    }
}