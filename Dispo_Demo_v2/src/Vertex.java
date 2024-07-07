public class Vertex {
    int id;
    Position position;
    String label;
    String type;
    Time time;

    public Vertex(int id, Position pos, String label, String type, Time time) {
        this.id = id;
        this.position = pos;
        this.label = label;
        this.type = type;
        this.time = time;
    }

    public Vertex(Vertex v) {
        this.id = v.id;
        this.position = v.position;
        this.label = v.label;
        this.type = v.type;
        this.time = v.time;
    }
}
