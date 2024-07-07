public class Fahrzeug extends Vertex
{
    int fahrzeugNummer;
    Position livePosition;
    Position simulationPosition;

    public Fahrzeug(int fahrzeugNummer, Position livePosition) {
        super(fahrzeugNummer, livePosition, "F"+fahrzeugNummer, Time.currentTime);
        this.fahrzeugNummer = fahrzeugNummer;
        this.livePosition = livePosition;
        this.simulationPosition = new Position(livePosition);
    }

    public Fahrzeug(int fahrzeugNummer, Position livePosition, Position simulationPosition) {
        this(fahrzeugNummer, livePosition);
        this.simulationPosition = simulationPosition;
    }

    public Fahrzeug(Fahrzeug v) {
        this(v.fahrzeugNummer, v.livePosition, v.simulationPosition);
    }

    @Override
    public String toString() {
        return label;
    }
}
