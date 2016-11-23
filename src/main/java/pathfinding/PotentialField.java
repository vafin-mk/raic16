package pathfinding;

import model.Game;
import model.Wizard;
import model.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotentialField {

  private Map<Vertex, Integer> field = new HashMap<>();

  public void updateField(Wizard self, Vertex moveTarget, World world, Game game) {
    field.clear();
    for(Vertex vertex : available(self)) {
      field.put(vertex, calculatePotential(vertex, moveTarget, world, game));
    }
  }

  public Vertex bestVertex() {
    return field.entrySet().stream()
        .sorted(Map.Entry.<Vertex, Integer>comparingByValue().reversed())
        .findFirst().get().getKey();
  }

  private int calculatePotential(Wizard self, Vertex moveTarget, World world, Game game) {
    int enemyPoint = 0;
    int allyPoint = 0;


    return enemyPoint + allyPoint;
  }

  private List<Vertex> available(Wizard self) {
    List<Vertex> result = new ArrayList<>();
    //http://stackoverflow.com/a/15856549
    int x, y, radius = 4, xSym, ySym;//todo hastened to radius
    int xCenter = (int)self.getX(), yCenter = (int) self.getY();
    for (x = xCenter - radius ; x <= xCenter; x++) {
      for (y = yCenter - radius ; y <= yCenter; y++) {
        // we don't have to take the square root, it's slow
        if ((x - xCenter)*(x - xCenter) + (y - yCenter)*(y - yCenter) <= radius*radius) {
          xSym = xCenter - (x - xCenter);
          ySym = yCenter - (y - yCenter);
          // (x, y), (x, ySym), (xSym , y), (xSym, ySym) are in the circle
          result.add(new Vertex(x, y));
          if (xSym > 0 && xSym < 4000) {
            result.add(new Vertex(xSym, y));
            if (ySym > 0 && ySym < 4000) {
              result.add(new Vertex(x, ySym));
              result.add(new Vertex(xSym, ySym));
            }
          }
        }
      }
    }
    return result;
  }
}
