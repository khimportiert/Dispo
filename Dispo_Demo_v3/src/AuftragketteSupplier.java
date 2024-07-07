import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class AuftragketteSupplier {
    static final double distanceThreshold = 20;
    static final int timeLowerThreshold = -5;
    static final int timeUpperThreshold = 10;
    static final int INFINITY = 999999;

    LinkedList<Auftrag> auftragListe;
    private final SimpleDirectedWeightedGraph<Auftrag, DefaultWeightedEdge> auftragGraph;

    public AuftragketteSupplier(LinkedList<Auftrag> auftragListe) {
        this.auftragListe = auftragListe;
        this.auftragGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    void initializeGraph() {
        auftragListe.forEach(auftragGraph::addVertex);
    }

    void connectGraph() {
        Set<Auftrag[]> toConnect = new HashSet<>();

        auftragGraph.vertexSet().forEach(source -> auftragGraph.vertexSet().forEach(target -> {
            if (!source.equals(target)) {
                int distance = (int) source.endPosition.distanceTo(target.startPosition);
                if (distance < distanceThreshold) {
                    int timeDif = target.arrivalTime.calcTicks() - (source.departureTime.calcTicks() + distance);
                    if (timeDif > timeLowerThreshold && timeDif < timeUpperThreshold) {
                        toConnect.add(new Auftrag[] { source, target });
                    }
                }
            }
        }));

        toConnect.forEach(auftrag -> {
            auftragGraph.addEdge(auftrag[0], auftrag[1]);
            auftragGraph.setEdgeWeight(auftrag[0], auftrag[1], auftrag[0].position.distanceTo(auftrag[1].position));
        });
    }

    void matchGraph() {
        Set<DefaultWeightedEdge> matchingEdgesToKeep = new HashSet<>();
        Set<DefaultWeightedEdge> edgesToKeep = new HashSet<>();

        // Zerlegung in Zusammenhangskomponenten
        ConnectivityInspector<Auftrag, DefaultWeightedEdge> connectivityInspector = new ConnectivityInspector<>(auftragGraph);
        List<Set<Auftrag>> weaklyConnectedComponents = connectivityInspector.connectedSets();
        weaklyConnectedComponents.removeIf(set -> set.size() < 2);

        // Verdoppelung und Partitionierung
        List<SimpleWeightedGraph<Auftrag, DefaultWeightedEdge>> bipartitGraphs = new LinkedList<>();
        weaklyConnectedComponents.forEach(auftragSet -> {
            SimpleWeightedGraph<Auftrag, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

            // Knoten Verdoppelung und hinzufügen
            List<Auftrag> inVertices = new LinkedList<>();
            List<Auftrag> outVertices = new LinkedList<>();
            auftragSet.forEach(vertex -> {
                Auftrag in = new Auftrag(vertex);
                Auftrag out = new Auftrag(vertex);
                in.label += ":in";
                out.label += ":out";
                inVertices.add(in);
                outVertices.add(out);
            });
            inVertices.forEach(graph::addVertex);
            outVertices.forEach(graph::addVertex);

            // Kanten hinzufügen
            auftragGraph.edgeSet().forEach(edge -> {
                Auftrag source = graph.getEdgeSource(edge);
                Auftrag target = graph.getEdgeTarget(edge);

                Auftrag in = inVertices.stream().filter(v -> v.id == target.id).findFirst().orElse(null);
                Auftrag out = outVertices.stream().filter(v -> v.id == source.id).findFirst().orElse(null);
                if (in != null && out != null) {
                    graph.addEdge(out, in);
                    graph.setEdgeWeight(out, in, source.position.distanceTo(target.position));
                }
            });
            bipartitGraphs.add(graph);
        });

        // Vollständigkeitsabschluss
        bipartitGraphs.forEach(graph -> {
            Set<Auftrag> partition1 = graph.vertexSet().stream().filter(v -> v.label.endsWith(":in")).collect(Collectors.toSet());
            Set<Auftrag> partition2 = graph.vertexSet().stream().filter(v -> v.label.endsWith(":out")).collect(Collectors.toSet());
            partition1.forEach(v1 -> partition2.forEach(v2 -> {
                if(!graph.containsEdge(v1, v2)) {
                    graph.addEdge(v1, v2);
                    graph.setEdgeWeight(v1, v2, INFINITY);
                }
            }));

            // Minimum-Weight Matching
            KuhnMunkresMinimalWeightBipartitePerfectMatching<Auftrag, DefaultWeightedEdge> matching =
                    new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(graph, partition1, partition2);

            // Kanten merken
            matchingEdgesToKeep.addAll(matching.getMatching().getEdges().stream().filter(e -> graph.getEdgeWeight(e) < INFINITY).collect(Collectors.toSet()));
            auftragGraph.edgeSet().forEach(edge -> {
                int id1 = auftragGraph.getEdgeSource(edge).id;
                int id2 = auftragGraph.getEdgeTarget(edge).id;
                matchingEdgesToKeep.forEach(e -> {
                    int id3 = graph.getEdgeSource(e).id;
                    int id4 = graph.getEdgeTarget(e).id;

                    if ((id1 == id3 || id2 == id3) && (id1 == id4 || id2 == id4)) {
                        edgesToKeep.add(edge);
                    }
                });
            });
        });

        // Restlichen Kanten entfernen
        Set<DefaultWeightedEdge> edgesToRemove = auftragGraph.edgeSet().stream().filter(e -> !edgesToKeep.contains(e)).collect(Collectors.toSet());
        auftragGraph.removeAllEdges(edgesToRemove);
        System.out.println(edgesToRemove.size() + " Kanten entfernt");
    }

    List<Auftragkette> extractAuftragketten() {
        List<Auftragkette> auftragkettenListe = new LinkedList<>();
        ConnectivityInspector<Auftrag, DefaultWeightedEdge> connectivityInspector = new ConnectivityInspector<>(auftragGraph);
        connectivityInspector.connectedSets().forEach(set -> auftragkettenListe.add(new Auftragkette(set.stream().sorted(Comparator.comparingInt(v -> v.arrivalTime.calcTicks())).toList())));
        return auftragkettenListe;
    }

    void showGraph() {
        DrawingPanel drawPanel = new DrawingPanel();
        auftragGraph.vertexSet().forEach(drawPanel::addAuftrag);
        auftragGraph.edgeSet().forEach(e -> {
            Position pos1 = auftragGraph.getEdgeSource(e).endPosition;
            Position pos2 = auftragGraph.getEdgeTarget(e).startPosition;
            drawPanel.addPinkPath(new Position[] {pos1, pos2});
        });
        JFrame frame = new JFrame("Java 2D Graphics");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1200);
        drawPanel.setScale(10);
        frame.add(drawPanel);
        frame.setVisible(true);
    }

    DrawingPanel getDrawingPanel() {
        DrawingPanel drawPanel = new DrawingPanel();
        auftragGraph.vertexSet().forEach(drawPanel::addAuftrag);
        auftragGraph.edgeSet().forEach(e -> {
            Position pos1 = auftragGraph.getEdgeSource(e).endPosition;
            Position pos2 = auftragGraph.getEdgeTarget(e).startPosition;
            drawPanel.addPinkPath(new Position[] {pos1, pos2});
        });
        return drawPanel;
    }
}
