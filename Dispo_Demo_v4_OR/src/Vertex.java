public class Vertex {
    int id;
    Position position;
    String label;
    Time time;

    public Vertex(int id, Position pos, String label, Time time) {
        this.id = id;
        this.position = pos;
        this.label = label;
        this.time = time;
    }

    public Vertex(Vertex v) {
        this.id = v.id;
        this.position = v.position;
        this.label = v.label;
        this.time = v.time;
    }
}
