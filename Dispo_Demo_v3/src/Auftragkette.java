import java.util.List;

public class Auftragkette extends Vertex{
    List<Auftrag> auftragListe;

    public Auftragkette(List<Auftrag> auftragListe) {
        super(auftragListe.getFirst().auftragsnummer, auftragListe.getFirst().startPosition, Integer.toString(auftragListe.getFirst().auftragsnummer), Time.currentTime);
        this.auftragListe = auftragListe;
    }

    public Auftragkette(Auftragkette auftragkette) {
        this(auftragkette.auftragListe);
    }

    Auftrag getStartAuftrag() {
        return auftragListe.getFirst();
    }

    Auftrag getEndAuftrag() {
        return auftragListe.getLast();
    }

    int size() {
        return auftragListe.size();
    }
}
