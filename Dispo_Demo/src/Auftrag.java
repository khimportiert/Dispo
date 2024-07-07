public class Auftrag extends Vertex implements Comparable<Auftrag> {
    int auftragsnummer;
    Position startPosition;
    Position endPosition;
    Time arrivalTime;
    Time travelTime;
    Time dwellTime;

    public Auftrag(int auftragsnummer, Position startPosition, Position endPosition, Time arrivalTime, Time dwellTime) {
        super(startPosition, auftragsnummer+":"+arrivalTime.time+"-"+(arrivalTime.time+dwellTime.time));
        this.auftragsnummer = auftragsnummer;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.arrivalTime = arrivalTime;
        this.travelTime = new Time((int) startPosition.distanceTo(endPosition));
        this.dwellTime = dwellTime;
    }

    static Auftrag generateRandomAuftrag() {
        int auftragsnummer = (int) (Math.random() * 9000) + 999;
        Position p1 = Position.generateRandomPosition();
        Position p2 = Position.generateRandomPosition();
        Time t1 = Time.generateRandomTime(10, 40);
        Time t2 = Time.generateRandomTime( 9);
        return new Auftrag(auftragsnummer, p1, p2, t1, t2);
    }

    @Override
    public int compareTo(Auftrag that) {
        return this.arrivalTime.compareTo(that.arrivalTime);
    }

    @Override
    public String toString() {
        return label;
    }
}
