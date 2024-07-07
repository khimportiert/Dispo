import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

public class AuftragSupplier {
    int anzahl;
    ArrayList<Auftrag> auftragListe;
    long[][] timeMatrix;
    long[][] timeWindows;

    public AuftragSupplier() {
        this.anzahl = 1;
        this.auftragListe = new ArrayList<>();
        auftragListe.add(new Auftrag(10000, new Position(55,55), new Position(55, 55), Time.currentTime)); // Depot
        this.timeMatrix = new long[anzahl][anzahl];
        this.timeWindows = new long[anzahl][2];
    }

    // Random Aufträge generieren
    void generate(int anzahl) {
        for (int i = 1; i < anzahl+1; i++) {
            auftragListe.add(Auftrag.generateRandomAuftrag(i, 25, 85, Time.currentTime.calcTicks()+30, Time.currentTime.calcTicks()+90));
            this.anzahl++;
        }
        this.timeMatrix = new long[anzahl+1][anzahl+1];
        this.timeWindows = new long[anzahl+1][2];
    }

    // Zeitmatrix erstellen
    void calcTimeMatrix() {
        for (int src = 0; src < this.auftragListe.size(); src++) {
            for (int dest = 0; dest < this.auftragListe.size(); dest++) {
                timeMatrix[src][dest] =
                        (long) auftragListe.get(src).endPosition.distanceTo(auftragListe.get(dest).startPosition);
            }
        }
    }

    // Zeitmatrix printen
    void printTimeMatrix() {
        for (long[] matrix : timeMatrix) {
            for (long l : matrix) {
                System.out.print(String.format("%02d", l) + " ");
            }
            System.out.println();
        }
    }
    
    // Zeit Windows erstellen
    void calcTimeWindows() {
        calcTimeWindows(-5,15);
    }
    void calcTimeWindows(int lowerThreshold, int upperThreshold) { // in minutes
        timeWindows[0] = new long[] {0, 5}; // Depot
        for (int i=1; i<auftragListe.size(); i++) {
            timeWindows[i] = new long[] {
                    auftragListe.get(i).arrivalTime.calcTicks()+lowerThreshold - Time.currentTime.calcTicks(),
                    auftragListe.get(i).arrivalTime.calcTicks()+upperThreshold - Time.currentTime.calcTicks()
            };
        }
    }

    // Zeit Windows printen
    void printTimeWindows() {
        for (long[] matrix : timeWindows) {
            for (long l : matrix) {
                System.out.print(String.format("%02d", l) + " ");
            }
            System.out.println();
        }
    }

    // Aufträge visualisieren
    void draw() {
        DrawingPanel drawPanel = new DrawingPanel();
        auftragListe.forEach(drawPanel::addAuftrag);
        JFrame frame = new JFrame("Java 2D Graphics");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1200);
        drawPanel.setScale(10);
        frame.add(drawPanel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Time.initialize(8,0);
        AuftragSupplier auftragSupplier = new AuftragSupplier();
        auftragSupplier.generate(3);
        auftragSupplier.draw();
        auftragSupplier.calcTimeMatrix();
        auftragSupplier.printTimeMatrix();
        auftragSupplier.calcTimeWindows();
        auftragSupplier.printTimeWindows();
    }

}
