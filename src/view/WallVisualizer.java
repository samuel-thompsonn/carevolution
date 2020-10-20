package view;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.CollisionEntity;
import model.CollisionEntityListener;
import model.entity.WallRegion;

import java.util.ArrayList;
import java.util.List;


public class WallVisualizer extends Parent implements EntityVisualizer, CollisionEntityListener {
  private Shape myRegion;
  private List<VisualizerListener> myListeners;

  public WallVisualizer(WallRegion region) {
    myListeners = new ArrayList<>();
    myRegion = new Rectangle(region.getCollisionX(),region.getCollisionY(),region.getCollisionWidth(),region.getCollisionHeight());
    myRegion.setFill(Color.GRAY);
    getChildren().add(myRegion);
  }

  @Override
  public Parent getGroup() {
    return this;
  }

  @Override
  public void subscribe(VisualizerListener listener) {
    myListeners.add(listener);
  }

  @Override
  public void reactToRemoval(CollisionEntity removed) {
    for (VisualizerListener listener : myListeners) {
      listener.reactToDeath(this);
    }
  }
}

