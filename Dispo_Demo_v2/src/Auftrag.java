public class Auftrag extends Vertex implements Comparable<Auftrag> {
    int auftragsnummer;
    Position startPosition;
    Position endPosition;
    Time arrivalTime;
    Time travelTime;

    public Auftrag(int auftragsnummer, Position startPosition, Position endPosition, Time arrivalTime) {
        super(auftragsnummer, startPosition, Integer.toString(auftragsnummer), "Auftrag", Time.currentTime);
        this.auftragsnummer = auftragsnummer;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.arrivalTime = arrivalTime;
        this.travelTime = new Time((int) startPosition.distanceTo(endPosition));
        this.time = travelTime;
    }

    public Auftrag(Auftrag v) {
        super(v.auftragsnummer, v.startPosition, Integer.toString(v.auftragsnummer), "Auftrag", Time.currentTime);
        this.auftragsnummer = v.auftragsnummer;
        this.startPosition = v.startPosition;
        this.endPosition = v.endPosition;
        this.arrivalTime = v.arrivalTime;
        this.travelTime = new Time((int) startPosition.distanceTo(endPosition));
        this.time = travelTime;
    }

    static Auftrag generateRandomAuftrag(int id, int minPos, int maxPos, int minTime, int maxTime) {
        Position p1 = Position.generateRandomPosition(minPos, maxPos);
        Position p2 = Position.generateRandomPosition(minPos, maxPos);
        Time arrival = Time.generateRandomTime(minTime, maxTime);
        return new Auftrag(id, p1, p2, arrival);
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
