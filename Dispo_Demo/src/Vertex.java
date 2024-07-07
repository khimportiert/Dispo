public class Vertex {
    Position postion;
    String label;

    public Vertex(Position pos, String label) {
        this.postion = pos;
        this.label = label;
    }

    public Vertex(Vertex v) {
        this.postion = new Position(v.postion.x, v.postion.y);
        this.label = v.label;
    }
}
