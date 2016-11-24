package pathfinding;

import model.Unit;
import ai.Utils;

import java.util.*;

public class PathFinder {

  public List<Vertex> buildPath(Unit unit, Vertex goal, Graph graph) {
    return buildPath(new Vertex(unit.getX(), unit.getY()), goal, graph);
  }

  //for now without heuristics
  public List<Vertex> buildPath(Vertex leftVertex, Vertex rightVertex, Graph graph) {
    List<Vertex> vertices = new ArrayList<>(graph.vertices);
    vertices.forEach(vertex -> {
      vertex.g = 0;
      vertex.parent = null;
    });
    List<Edge> edges = new ArrayList<>(graph.edges);

    Vertex startVertex = Utils.closestVertex(leftVertex, vertices);
    Vertex goalVertex = Utils.closestVertex(rightVertex, vertices);
    rightVertex.parent = goalVertex;

    Queue<Vertex> open = new PriorityQueue<>();
    Set<Vertex> closed = new HashSet<>();

    startVertex.g = 0;
    open.offer(startVertex);

    while (!open.isEmpty()) {
      Vertex currentVertex = open.poll();
      if (currentVertex.equals(goalVertex)) {
        return constructPath(startVertex, goalVertex);
      }
      closed.add(currentVertex);
      for (Vertex vert : currentVertex.adjs) {
        if (closed.contains(vert)) {
          continue;
        }
        double g = currentVertex.g + edges.get(edges.indexOf(new Edge(currentVertex, vert))).dist;
        if (!open.contains(vert) || g < vert.g) {
          open.remove(vert);
          vert.g = g;
          vert.parent = currentVertex;
          open.offer(vert);
        }
      }
    }

    System.out.println("FAILED TO BUILD PATH!");
    return constructPath(startVertex, goalVertex);
  }

  private List<Vertex> constructPath(Vertex start, Vertex goal) {
    List<Vertex> waypoint = new ArrayList<>();
    Vertex curr = goal;
    while (curr != null) {
      waypoint.add(curr);
      curr = curr.parent;
    }
    Collections.reverse(waypoint);
    return waypoint;
  }


}
