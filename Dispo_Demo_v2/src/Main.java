import org.jgrapht.Graph;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jheaps.annotations.VisibleForTesting;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class Main {
    static int Anz_Fahrzeuge = 40;
    static int Anz_Aufträge = 440;
    static final int INFINITY = 99999999;

    static LinkedList<Fahrzeug> FahrzeugListe = new LinkedList<>();
    static LinkedList<Auftrag> AuftragListe = new LinkedList<>();

    static SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> BipartitGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    static LinkedList<LinkedList<Vertex>> Match = new LinkedList<>();
    static LinkedList<Vertex> Lost = new LinkedList<>();

    static void fillFahrzeugListe() {
        for (int i = 1; i <= Anz_Fahrzeuge; i++) {
            FahrzeugListe.add(new Fahrzeug(i, Position.generateRandomPosition(40, 75)));
        }
    }

    static void fillAuftragListe() {
        int anz_im_zentrum = (int) (Anz_Aufträge/2);

        for (int i = 10000; i < anz_im_zentrum+10000; i++) {
            AuftragListe.add(Auftrag.generateRandomAuftrag(i, 40, 75, Time.currentTime.calcTicks()+15, Time.currentTime.calcTicks()+480));
        }

        for (int i = anz_im_zentrum+10000; i < Anz_Aufträge+10000; i++) {
            AuftragListe.add(Auftrag.generateRandomAuftrag(i, 30, 85, Time.currentTime.calcTicks()+15, Time.currentTime.calcTicks()+480));
        }
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

    static void exportGraph(Graph<Vertex, DefaultWeightedEdge> graph) throws IOException {
        DOTExporter<Vertex, DefaultWeightedEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.label));
            map.put("pos", DefaultAttribute.createAttribute(v.position.x+","+v.position.y+"!"));
            return map;
        });
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            double weight = BipartitGraph.getEdgeWeight(e);
            if (weight == INFINITY) {
                map.put("color", DefaultAttribute.createAttribute("white"));
            } else {
                map.put("label", DefaultAttribute.createAttribute(String.format("%.2f",weight)));
            }
            return map;
        });

        Writer writer = new StringWriter();
        exporter.exportGraph(graph, writer);

        BufferedWriter BufferedWriter = new BufferedWriter(new FileWriter("bipartitGraph.txt"));
        BufferedWriter.write(writer.toString());
        BufferedWriter.close();
    }

    public static void main(String[] args) throws IOException {
        Time.initialize(8, 0);
        fillFahrzeugListe();
        fillAuftragListe();

//        LinkedList<Fahrzeug> testFahrzeugListe = Test.testFahrzeugListe2();
//        LinkedList<Auftrag> testAuftragListe = Test.testAuftragListe2();
//        Anz_Fahrzeuge = testFahrzeugListe.size();
//        Anz_Aufträge = testAuftragListe.size();
//        round1(testFahrzeugListe, testAuftragListe);

        round1(FahrzeugListe, AuftragListe);
    }


    static void round1(LinkedList<Fahrzeug> fahrzeugListe, LinkedList<Auftrag> auftragListe) {
        DrawingPanel drawPanel = new DrawingPanel();

        fahrzeugListe.forEach(fahrzeug -> {
            BipartitGraph.addVertex(fahrzeug);

            // Hier Fahrzeuge zeichnen
            drawPanel.addFahrzeug(new Fahrzeug(fahrzeug));
        });

        AuftragListe.sort(Comparator.comparingInt(a -> a.arrivalTime.calcTicks()));
//        auftragListe.sort(Comparator.comparingDouble(value -> calculateMinimumDist(value, fahrzeugListe)));

        for (int i=1; i*Anz_Fahrzeuge <= Anz_Aufträge; i++) {
            List<Auftrag> AuftragSubListe = auftragListe.subList((i-1)*Anz_Fahrzeuge, auftragListe.size());
            AuftragSubListe.sort(Comparator.comparingInt(a -> a.arrivalTime.calcTicks()));
//            AuftragSubListe.sort(Comparator.comparingDouble(value -> calculateMinimumDist(value, fahrzeugListe)));
            Set<Auftrag> auftragSet = new HashSet<>(AuftragSubListe.subList(0, Anz_Fahrzeuge));

            for (Auftrag auftrag : auftragSet) {
                BipartitGraph.addVertex(auftrag);
                for (Vertex fahrzeug : fahrzeugListe) {
                    double dist = fahrzeug.position.distanceTo(auftrag.startPosition);
                    BipartitGraph.addEdge(fahrzeug, auftrag);
                    if (fahrzeug.time.calcTicks() < (auftrag.arrivalTime.calcTicks() - dist)) {
                        BipartitGraph.setEdgeWeight(fahrzeug, auftrag, dist);
                    } else {
                        BipartitGraph.setEdgeWeight(fahrzeug, auftrag, INFINITY);
                    }
                }
            }

            KuhnMunkresMinimalWeightBipartitePerfectMatching<Vertex, DefaultWeightedEdge> matching =
                    new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(BipartitGraph, new HashSet<>(fahrzeugListe), auftragSet);

            Set<DefaultWeightedEdge> allEdges = matching.getMatching().getEdges();
            Set<DefaultWeightedEdge> takenEdges = new HashSet<>();
            allEdges.forEach(edge -> {
                if (BipartitGraph.getEdgeWeight(edge) != INFINITY) {
                    Vertex fahrzeug = BipartitGraph.getEdgeSource(edge);
                    Auftrag target = (Auftrag) BipartitGraph.getEdgeTarget(edge);

                    // Hier Source und Target zeichnen
                    drawPanel.addAuftrag(new Auftrag(target));
                    // Hier Verbindung zeichnen
                    drawPanel.addPath(new Position[]{new Position(fahrzeug.position), new Position(target.startPosition)});

                    takenEdges.add(edge);
                    fahrzeug.position = target.endPosition;
                    fahrzeug.time = new Time(target.arrivalTime.addTime(target.travelTime));

                    BipartitGraph.removeVertex(target);
                }
            });

            Set<Vertex> vertexToRemove = new HashSet<>();
            BipartitGraph.vertexSet().forEach(vertex -> {
                if (auftragSet.contains(vertex)) {
                    Lost.add(vertex);

                    vertexToRemove.add(vertex);
                }
            });
            for (Vertex vertex : vertexToRemove) {
                BipartitGraph.removeVertex(vertex);
            }

            System.out.println(takenEdges);
        }


        System.out.println("Verlorene Aufträge: "+Lost.size());

        // Verlorene Aufträge zeichnen
        Lost.forEach(lostVertex -> {
            Auftrag v = new Auftrag((Auftrag) lostVertex);
            v.type = "Lost";
            drawPanel.addLost(v);
        });

        JFrame frame = new JFrame("Java 2D Graphics");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 1300);
        drawPanel.setScale(11);
        frame.add(drawPanel);
        frame.setVisible(true);
        drawPanel.save("./output_image_with_edges.png");
        drawPanel.clearPaths();
        drawPanel.save("./output_image_no_edges.png");
    }
}
