//triangle
public class Forest {

  final static Forest LEFT = new Forest(new Vertex(400 - 35, 800 - 35), new Vertex(1600 + 35, 2000), new Vertex(400 - 35, 3200 + 35));
  final static Forest TOP = new Forest(new Vertex(800 - 35, 400 - 35), new Vertex(2000, 1600 + 35), new Vertex(3200 + 35, 400 - 35));
  final static Forest RIGHT = new Forest(new Vertex(3600 + 35, 800 - 35), new Vertex(2400 - 35, 2000), new Vertex(3600 + 35, 3200 + 35));
  final static Forest BOT = new Forest(new Vertex(800 - 35, 3600 + 35), new Vertex(2000, 2400 - 35), new Vertex(3200 + 35, 3600 + 35));

  final Vertex first, second, third;

  Forest(Vertex first, Vertex second, Vertex third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  private double area() {
    return StrictMath.abs((first.x*(second.y-third.y) + second.x*(third.y-first.y)+ third.x*(first.y-second.y))/2.0);
  }

  boolean contains(Vertex v) {
    double a = area();
    double a1 = new Forest(v, second, third).area();
    double a2 = new Forest(first, v, third).area();
    double a3 = new Forest(first, second, v).area();
    return StrictMath.abs(a - (a1+a2+a3)) < 1;
  }
}
