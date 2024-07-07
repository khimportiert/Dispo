import java.util.LinkedList;

public class Test {
    static LinkedList<Fahrzeug> testFahrzeugListe1() {
        LinkedList<Fahrzeug> testFahrzeugListe = new LinkedList<>();
        testFahrzeugListe.add(new Fahrzeug(1, new Position(5, 20)));
        testFahrzeugListe.add(new Fahrzeug(2, new Position(15, 20)));
        return testFahrzeugListe;
    }

    static LinkedList<Auftrag> testAuftragListe1() {
        LinkedList<Auftrag> testAuftragListe1 = new LinkedList<>();
        testAuftragListe1.add(new Auftrag(10001, new Position(10, 10), new Position(30, 10), new Time(11, 0)));
        testAuftragListe1.add(new Auftrag(10002, new Position(40, 10), new Position(60, 10), new Time(12, 0)));
        testAuftragListe1.add(new Auftrag(10003, new Position(10, 100), new Position(50, 100), new Time(12, 0)));
        testAuftragListe1.add(new Auftrag(10004, new Position(60, 100), new Position(90, 100), new Time(13, 0)));
        return testAuftragListe1;
    }


    static LinkedList<Fahrzeug> testFahrzeugListe2() {
        LinkedList<Fahrzeug> testFahrzeugListe = new LinkedList<>();
        testFahrzeugListe.add(new Fahrzeug(1, new Position(5, 20)));
        testFahrzeugListe.add(new Fahrzeug(2, new Position(15, 20)));
        return testFahrzeugListe;
    }

    static LinkedList<Auftrag> testAuftragListe2() {
        LinkedList<Auftrag> testAuftragListe1 = new LinkedList<>();
        testAuftragListe1.add(new Auftrag(10001, new Position(10, 10), new Position(30, 10), new Time(11, 0)));
        testAuftragListe1.add(new Auftrag(10002, new Position(40, 10), new Position(60, 10), new Time(11, 30)));

        testAuftragListe1.add(new Auftrag(10003, new Position(10, 40), new Position(20, 40), new Time(12, 0)));
        testAuftragListe1.add(new Auftrag(10004, new Position(30, 40), new Position(50, 40), new Time(12, 30)));
        testAuftragListe1.add(new Auftrag(10005, new Position(60, 40), new Position(80, 40), new Time(13, 30)));

//        testAuftragListe1.add(new Auftrag(10006, new Position(10, 100), new Position(50, 100), new Time(12, 0)));
        return testAuftragListe1;
    }
}
