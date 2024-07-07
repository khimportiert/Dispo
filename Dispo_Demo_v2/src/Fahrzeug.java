public class Fahrzeug extends Vertex
{
    int fahrzeugNummer;

    public Fahrzeug(int fahrzeugNummer, Position currentPosition) {
        super(fahrzeugNummer, currentPosition, "F"+fahrzeugNummer, "Fahrzeug", Time.currentTime);
        this.fahrzeugNummer = fahrzeugNummer;
    }

    public Fahrzeug(Fahrzeug v) {
        super(v.fahrzeugNummer, v.position, "F"+v.fahrzeugNummer, "Fahrzeug", Time.currentTime);
        this.fahrzeugNummer = v.fahrzeugNummer;
    }

    @Override
    public String toString() {
        return label;
    }
}
