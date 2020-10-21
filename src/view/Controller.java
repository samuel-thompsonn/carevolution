package view;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.EvolutionSim;

public class Controller extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    EvolutionSim simulation = new EvolutionSim(1, 0.50, 1000);
    EvolutionVisualizer visualizer = new EvolutionVisualizer(simulation);
    Group root = new Group();
    root.getChildren().add(visualizer);
    Scene scene = new Scene(root,1024,1000);
    scene.setOnMouseClicked(visualizer.getOnMouseClicked());
    scene.setOnKeyPressed(visualizer.getOnKeyPressed());
    scene.setOnKeyReleased(visualizer.getOnKeyReleased());
    primaryStage.setScene(scene);
    primaryStage.setTitle("Car Evolution");
    primaryStage.show();
  }
}
