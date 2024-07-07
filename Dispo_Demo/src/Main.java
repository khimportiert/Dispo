import org.jgrapht.Graph;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main {
    static final int mode = 1;
    static final int Anz_Fahrzeuge = 3;
    static final int Anz_Aufträge = 21;

    static LinkedList<Fahrzeug> FahrzeugListe = new LinkedList<>();
    static LinkedList<Auftrag> AuftragListe = new LinkedList<>();

    static SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> fullGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    static SimpleDirectedWeightedGraph<Vertex, DefaultWeightedEdge> bipartitGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    static LinkedList<Set<DefaultWeightedEdge>> match = new LinkedList<>();
    static LinkedList<Vertex> Lost = new LinkedList<>();

    static void fillFahrzeugListe() {
        for (int i = 0; i < Anz_Fahrzeuge; i++) {
            FahrzeugListe.add(new Fahrzeug(i, Position.generateRandomPosition()));
        }
    }

    static void fillAuftragListe() {
        for (int i = 0; i < Anz_Aufträge; i++) {
            AuftragListe.add(Auftrag.generateRandomAuftrag());
        }
    }

    static double calculateMinimumDist(Auftrag auftrag, LinkedList<Vertex> fahrzeugListe) {
        double min_dist = Double.MAX_VALUE;
        for (Vertex fahrzeug : fahrzeugListe) {
            double dist = fahrzeug.postion.distanceTo(auftrag.startPosition);
            if (dist < min_dist) {
                min_dist = dist;
            }
        }
        return auftrag.arrivalTime.time - min_dist;
    }

    static void exportGraph(Graph<Vertex, DefaultWeightedEdge> graph) throws IOException {
        DOTExporter<Vertex, DefaultWeightedEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.label));
            map.put("pos", DefaultAttribute.createAttribute(v.postion.x+","+v.postion.y+"!"));
            return map;
        });
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            double weight = bipartitGraph.getEdgeWeight(e);
            if (weight == Double.MAX_VALUE) {
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

    static void prepareFullGraph(Auftrag auftrag) {
        Vertex start = new Vertex(auftrag.startPosition, auftrag.label);
        Vertex end = new Vertex(auftrag.endPosition, auftrag.label);
        fullGraph.addVertex(start);
        fullGraph.addVertex(end);
        fullGraph.addEdge(start, end);
        fullGraph.setEdgeWeight(start, end, start.postion.distanceTo(end.postion));
    }

    static void drawVertex(Auftrag auftrag) {

    }

    static void drawVertex(Fahrzeug fahrzeug) {

    }

    public static void main(String[] args) throws IOException {
        fillFahrzeugListe();
        fillAuftragListe();

        for (Fahrzeug fahrzeug : FahrzeugListe) {
            bipartitGraph.addVertex(fahrzeug);
            fullGraph.addVertex(fahrzeug);
        }

        LinkedList<Vertex> FahrzeugSubListe = new LinkedList<>(FahrzeugListe);
//        AuftragListe.sort(Comparator.comparingInt(a -> a.arrivalTime.time));
        AuftragListe.sort(Comparator.comparingDouble(value -> calculateMinimumDist(value, FahrzeugSubListe)));

        for (int i=1; i*Anz_Fahrzeuge < Anz_Aufträge; i++) {
            List<Auftrag> AuftragSubListe = AuftragListe.subList((i-1)*Anz_Fahrzeuge, i*Anz_Fahrzeuge);

            AuftragSubListe.sort(Comparator.comparingDouble(value -> calculateMinimumDist(value, FahrzeugSubListe)));

            for (Auftrag auftrag : AuftragSubListe) {
                prepareFullGraph(auftrag);
                bipartitGraph.addVertex(auftrag);
                for (Vertex fahrzeug : FahrzeugSubListe) {
                    double dist = fahrzeug.postion.distanceTo(auftrag.startPosition);
                    bipartitGraph.addEdge(fahrzeug, auftrag);
                    if (Time.currentTime < (auftrag.arrivalTime.time - dist)) {
                        bipartitGraph.setEdgeWeight(fahrzeug, auftrag, dist);
                    } else {
                        bipartitGraph.setEdgeWeight(fahrzeug, auftrag, Double.MAX_VALUE); // Overflow!?!?!?
                    }
                }
            }

            KuhnMunkresMinimalWeightBipartitePerfectMatching<Vertex, DefaultWeightedEdge> matching =
                    new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(bipartitGraph, new HashSet<>(FahrzeugSubListe), new HashSet<>(AuftragSubListe));

            Set<DefaultWeightedEdge> allEdges = matching.getMatching().getEdges();
            Set<DefaultWeightedEdge> takenEdges = new HashSet<>();
            allEdges.forEach(edge -> {
                if (bipartitGraph.getEdgeWeight(edge) != Double.MAX_VALUE) {
                    Vertex source = bipartitGraph.getEdgeSource(edge);
                    Vertex target = bipartitGraph.getEdgeTarget(edge);

                    Vertex vSource = fullGraph.vertexSet().stream().filter(v -> {
                        if (v.postion == source.postion)
                            return true;
                        return false;
                    }).findAny().get();

                    Vertex vTarget = fullGraph.vertexSet().stream().filter(v -> {
                        if (v.postion == target.postion)
                            return true;
                        return false;
                    }).findAny().get();

                    fullGraph.addEdge(vSource, vTarget);
                    fullGraph.setEdgeWeight(vSource, vTarget, vSource.postion.distanceTo(vTarget.postion));

                    takenEdges.add(edge);
                    source.postion.x = target.postion.x;
                    source.postion.y = target.postion.y;
                    bipartitGraph.removeVertex(target);
                }
            });
            System.out.println(takenEdges);
            match.add(takenEdges);

            // verbleibenden Aufträge löschen
            Set<Vertex> vertexToRemove = new HashSet<>();
            bipartitGraph.vertexSet().forEach(vertex -> {
                if (AuftragSubListe.contains(vertex)) {
                    Lost.add(vertex);
                    vertexToRemove.add(vertex);
                }
            });
            for (Vertex vertex : vertexToRemove) {
                bipartitGraph.removeVertex(vertex);
            }
        }

        System.out.println("Verlorene Aufträge: "+Lost.size());
        exportGraph(fullGraph);

//        JFrame frame = new JFrame("Java 2D Graphics Demo");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1500, 1000);
//        frame.setVisible(true);
    }

}
