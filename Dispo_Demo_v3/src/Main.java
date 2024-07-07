import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class Main {
    static int Anz_Fahrzeuge = 3;
    static int Anz_Aufträge = 10;
    static final int INFINITY = 99999999;

    static LinkedList<Auftrag> generateRandomAuftragListe() {
        LinkedList<Auftrag> auftragListe = new LinkedList<>();
        int anz_im_zentrum = Anz_Aufträge/2;
        for (int i = 10000; i < anz_im_zentrum+10000; i++) {
            auftragListe.add(Auftrag.generateRandomAuftrag(i, 40, 75, Time.currentTime.calcTicks()+15, Time.currentTime.calcTicks()+480));
        }
        for (int i = anz_im_zentrum+10000; i < Anz_Aufträge+10000; i++) {
            auftragListe.add(Auftrag.generateRandomAuftrag(i, 30, 85, Time.currentTime.calcTicks()+15, Time.currentTime.calcTicks()+480));
        }
        return auftragListe;
    }

    static LinkedList<Fahrzeug> generateRandomFahrzeugListe() {
        LinkedList<Fahrzeug> fahrzeugListe = new LinkedList<>();
        for (int i = 1; i <= Anz_Fahrzeuge; i++) {
            fahrzeugListe.add(new Fahrzeug(i, Position.generateRandomPosition(40, 75)));
        }
        return fahrzeugListe;
    }

    static void printAvgTimes(List<Auftragkette> auftragkettenListe) {
        int avgAnfangszeit = 0;
        int avgEndzeit = 0;
        int count = 1;
        for(Auftragkette v : auftragkettenListe) {
            if (v.size() > 1) {
                avgAnfangszeit += v.getStartAuftrag().arrivalTime.calcTicks();
                avgEndzeit += v.getEndAuftrag().departureTime.calcTicks();
                count++;
            }
        }
        avgAnfangszeit /= count;
        avgEndzeit /= count;
        System.out.println("Durchschnittliche Anfangszeit: " + new Time(avgAnfangszeit));
        System.out.println("Durchschnittliche Endzeit: " + new Time(avgEndzeit));
    }

    static double calculateMinimumDist(Auftrag auftrag, LinkedList<Fahrzeug> fahrzeugListe) {
        double min_dist = Double.MAX_VALUE;
        for (Vertex fahrzeug : fahrzeugListe) {
            double dist = fahrzeug.position.distanceTo(auftrag.startPosition);
            if (dist < min_dist) {
                min_dist = dist;
            }
        }
        return auftrag.arrivalTime.calcTicks() - min_dist;
    }

    static void round1(LinkedList<Fahrzeug> fahrzeugListe, List<Auftragkette> auftragkettenListe, DrawingPanel drawingPanel) throws IOException {
        SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> BipartitGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        LinkedList<Auftragkette> Lost = new LinkedList<>();

        fahrzeugListe.forEach(fahrzeug -> {
            BipartitGraph.addVertex(fahrzeug);

            // Hier Fahrzeuge zeichnen
            drawingPanel.addFahrzeug(new Fahrzeug(fahrzeug));
        });

//        auftragkettenListe.sort(Comparator.comparingDouble(value -> calculateMinimumDist(value.getStartAuftrag(), fahrzeugListe)));
        auftragkettenListe.sort(Comparator.comparingInt(a -> a.getStartAuftrag().arrivalTime.calcTicks()));

        for (int i=1; i*Anz_Fahrzeuge <= auftragkettenListe.size(); i++) {
            List<Auftragkette> AuftragkettenSubListe = auftragkettenListe.subList((i-1)*Anz_Fahrzeuge, auftragkettenListe.size());

            AuftragkettenSubListe.sort(Comparator.comparingInt(a -> a.getStartAuftrag().arrivalTime.calcTicks()));
            Set<Auftragkette> auftragkettenSet = new HashSet<>(AuftragkettenSubListe.subList(0, Anz_Fahrzeuge));

            for (Auftragkette auftragkette : auftragkettenSet) {
                BipartitGraph.addVertex(auftragkette);
                for (Vertex fahrzeug : fahrzeugListe) {
                    double dist = fahrzeug.position.distanceTo(auftragkette.getStartAuftrag().startPosition);
                    BipartitGraph.addEdge(fahrzeug, auftragkette);
                    if (fahrzeug.time.calcTicks() < (auftragkette.getStartAuftrag().arrivalTime.calcTicks() - dist)) {
                        BipartitGraph.setEdgeWeight(fahrzeug, auftragkette, dist);
                    } else {
                        BipartitGraph.setEdgeWeight(fahrzeug, auftragkette, INFINITY);
                    }
                }
            }

            KuhnMunkresMinimalWeightBipartitePerfectMatching<Vertex, DefaultWeightedEdge> matching =
                    new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(BipartitGraph, new HashSet<>(fahrzeugListe), auftragkettenSet);

            Set<DefaultWeightedEdge> allEdges = matching.getMatching().getEdges();
            Set<DefaultWeightedEdge> takenEdges = new HashSet<>();
            allEdges.forEach(edge -> {
                if (BipartitGraph.getEdgeWeight(edge) != INFINITY) {
                    Vertex fahrzeug = BipartitGraph.getEdgeSource(edge);
                    Auftragkette target = (Auftragkette) BipartitGraph.getEdgeTarget(edge);

                    // Hier Verbindung zeichnen
                    drawingPanel.addPath(new Position[]{new Position(fahrzeug.position), new Position(target.getStartAuftrag().startPosition)});

                    takenEdges.add(edge);
                    fahrzeug.position = target.getEndAuftrag().endPosition;
                    fahrzeug.time = new Time(target.getEndAuftrag().departureTime.calcTicks());

                    BipartitGraph.removeVertex(target);
                }
            });

            Set<Vertex> vertexToRemove = new HashSet<>();
            BipartitGraph.vertexSet().forEach(vertex -> {
                if (auftragkettenSet.contains(vertex)) {
                    Lost.add((Auftragkette) vertex);

                    vertexToRemove.add(vertex);
                }
            });
            for (Vertex vertex : vertexToRemove) {
                BipartitGraph.removeVertex(vertex);
            }
        }


        System.out.println("Verlorene Aufträge: "+Lost.stream().mapToInt(Auftragkette::size).sum() + " in " + Lost.size() + " Ketten");

        // Verlorene Aufträge zeichnen
        Lost.forEach(lostVertex -> {
            Auftragkette auftragkette = new Auftragkette(lostVertex);
            auftragkette.auftragListe.forEach(drawingPanel::addLost);
        });

        JFrame frame = new JFrame("Java 2D Graphics");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1200);
        drawingPanel.setScale(10);
        frame.add(drawingPanel);
        frame.setVisible(true);
        drawingPanel.save("./output_image_with_edges.png");
        drawingPanel.clearPaths();
        drawingPanel.save("./output_image_no_edges.png");
    }

    public static void main(String[] args) throws IOException {
        Time.initialize(8, 0);

        LinkedList<Auftrag> auftragListe = generateRandomAuftragListe();
        AuftragketteSupplier auftragketteSupplier = new AuftragketteSupplier(auftragListe);
        auftragketteSupplier.initializeGraph();
        auftragketteSupplier.connectGraph();
        auftragketteSupplier.matchGraph();
        auftragketteSupplier.showGraph();

        List<Auftragkette> auftragkettenListe = auftragketteSupplier.extractAuftragketten();
        System.out.println(Anz_Aufträge + " Äuftrage in " + auftragkettenListe.size() + " Auftragketten");
        printAvgTimes(auftragkettenListe);

        round1(generateRandomFahrzeugListe(), auftragkettenListe, auftragketteSupplier.getDrawingPanel());
    }
}
