import java.util.Objects;

public class Definition {

    public final String variable;
    public final int nodeId;

    public Definition(String variable, int nodeId) {
        this.variable = variable;
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Definition d)) {
            return false;
        }

        return variable.equals(d.variable)
                && nodeId == d.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, nodeId);
    }

    @Override
    public String toString() {
        return variable + "@" + nodeId;
    }
}