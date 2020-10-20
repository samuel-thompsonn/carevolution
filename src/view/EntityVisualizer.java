package view;

import javafx.scene.Parent;

/**
 * What do all entity-visualizing objects have in common, if anything?
 */
public interface EntityVisualizer {
  public Parent getGroup();
  public void subscribe(VisualizerListener listener);
}
