public class Fahrzeug extends Vertex
{
    int fahrzeugNummer;
    Position currentPosition;

    public Fahrzeug(int fahrzeugNummer, Position currentPosition) {
        super(currentPosition, "F"+fahrzeugNummer);
        this.fahrzeugNummer = fahrzeugNummer;
        this.currentPosition = currentPosition;
    }

    @Override
    public String toString() {
        return label;
    }
}
