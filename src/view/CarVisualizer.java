package view;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import model.CollisionEntity;
import model.entity.Car;
import model.entity.CarListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CarVisualizer extends Parent implements CarListener, EntityVisualizer {
  public static final double MAX_WHEEL_ROTATION = 40;
  public static final double FRICTION_FACTOR = 0.01;
  public static final double MAX_ACCELERATION = 10;
  public static final double CAR_RADIUS = 10.0;
  public static final int SENSOR_LENGTH = 300;
  private double myX;
  private double myY;
  private double myRotation;
  private double myWheelRotation;
  private ImageView myCarImage;
  private Polygon myHitbox;
  private List<VisualizerListener> myListeners;
  private Car myCar;
  private Line myLeftFinder;
  private Line myLeftForwardFinder;
  private Line myForwardFinder;
  private Line myRightForwardFinder;
  private Line myRightFinder;
  private List<Line> myFinderVisuals;
  private Line myWheelRotateVisual;
  private Circle myCarFront;
  private Circle myCarBack;
  private List<Circle> myPath;
  private double myLength;
  private double myWidth;

  private boolean showingFinders;

  public CarVisualizer(Car car) {
    myCar = car;
    myRotation = 0;
    try {
      InputStream stream = new FileInputStream("resources/yellowcar.png");
      myCarImage = new ImageView(new Image(stream));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    initFinders();
    myLength = myCar.getLengthBetweenAxles() * 1.5;
    myWidth = myCar.getAxleWidth() * 1.5;
    myCarImage.setFitWidth(myLength);
    myCarImage.setFitHeight(myWidth);
    getChildren().add(myCarImage);
    myHitbox = new Polygon();
    myHitbox.setFill(Color.color(1.0,0.0,0.0,0.3));
    getChildren().add(myHitbox);
    refreshHitboxVisual();
    myWheelRotateVisual = new Line();
    myWheelRotateVisual.setStroke(Color.RED);
    getChildren().add(myWheelRotateVisual);
    myCarFront = new Circle(myWidth / 2.0);
    myCarFront.setFill(Color.color(0.0,1.0,0.0,0.3));
    myCarBack = new Circle(myWidth / 2.0);
    myCarBack.setFill(Color.color(0.0,1.0,0.0,0.3));
    getChildren().addAll(myCarFront,myCarBack);
    myPath = new ArrayList<>();

    myListeners = new ArrayList<>();

    showingFinders = false;
    car.subscribe(this);
    setPosition(car.getXPos(),car.getYPos());
  }

  private void refreshHitboxVisual() {
    List<double[]> hitboxPoints = myCar.getHitboxPoints();
    List<Double> pointsAsDoubles = new ArrayList<>();
    for (double[] point : hitboxPoints) {
      pointsAsDoubles.add(point[0]);
      pointsAsDoubles.add(point[1]);
    }
    ObservableList<Double> visualPoints = myHitbox.getPoints();
    visualPoints.clear();
    visualPoints.addAll(pointsAsDoubles);
  }

  private void initFinders() {
    myLeftFinder = new Line();
    myLeftForwardFinder = new Line();
    myForwardFinder = new Line();
    myRightForwardFinder = new Line();
    myRightFinder = new Line();
    myFinderVisuals = new ArrayList<>();
    myFinderVisuals.add(myLeftFinder);
    myFinderVisuals.add(myLeftForwardFinder);
    myFinderVisuals.add(myForwardFinder);
    myFinderVisuals.add(myRightForwardFinder);
    myFinderVisuals.add(myRightFinder);
    getChildren().addAll(myFinderVisuals);
  }

  public void setPosition(double xPos, double yPos) {
    myX = xPos;
    myY = yPos;
    double axleX = (myCar.getBackXPos() - myCar.getFrontXPos());
    double axleY = (myCar.getBackYPos() - myCar.getFrontYPos());
    double middleX = ((myCar.getBackXPos() + myCar.getFrontXPos()) / 2) - myWidth / 2;
    double middleY = ((myCar.getBackYPos() + myCar.getFrontYPos()) / 2) - myWidth / 2;
//    myCarImage.setX(myCar.getBackXPos());
//    myCarImage.setY(myCar.getBackYPos());
    myCarImage.setX(middleX);
    myCarImage.setY(middleY);
    refreshHitboxVisual();
    myCarFront.setCenterX(myCar.getFrontXPos());
    myCarFront.setCenterY(myCar.getFrontYPos());
    myCarBack.setCenterX(myCar.getBackXPos());
    myCarBack.setCenterY(myCar.getBackYPos());
    Circle trace = new Circle(5,Color.color(1.0,0.0,0.0,0.3));
    trace.setCenterX(myCar.getBackXPos());
    trace.setCenterY(myCar.getBackYPos());
    Circle trace2 = new Circle(5,Color.color(1.0,0.0,0.0,0.3));
    trace2.setCenterX(myCar.getFrontXPos());
    trace2.setCenterY(myCar.getFrontYPos());
    myPath.add(trace);
    myPath.add(trace2);
    getChildren().add(trace);
    getChildren().add(trace2);
    if (myPath.size() > 60) {
      Circle removed = myPath.get(0);
      Circle removed2 = myPath.get(1);
      getChildren().remove(removed);
      getChildren().remove(removed2);
      myPath.remove(removed);
      myPath.remove(removed2);
    }
    refreshFinders();
  }

  public double getXPos() {
    return myX;
  }

  public double getYPos() {
    return myY;
  }

  public void setRotation(double degreeAmount) {
    myRotation = degreeAmount;
    myCarImage.setRotate(myRotation);
    refreshFinders();
  }

  private void refreshFinders() {
    for (Line finder : myFinderVisuals) {
      finder.setStartX(myCar.getFrontXPos());
      finder.setStartY(myCar.getFrontYPos());
    }
    myLeftFinder.setEndX(finderEndX(90,myCar.getLeftDistance()));
    myLeftFinder.setEndY(finderEndY(90,myCar.getLeftDistance()));
    myLeftForwardFinder.setEndX(finderEndX(45,myCar.getForwardLeftDistance()));
    myLeftForwardFinder.setEndY(finderEndY(45,myCar.getForwardLeftDistance()));
    myForwardFinder.setEndX(finderEndX(0,myCar.getForwardDistance()));
    myForwardFinder.setEndY(finderEndY(0,myCar.getForwardDistance()));
    myRightForwardFinder.setEndX(finderEndX(-45,myCar.getForwardRightDistance()));
    myRightForwardFinder.setEndY(finderEndY(-45,myCar.getForwardRightDistance()));
    myRightFinder.setEndX(finderEndX(-90,myCar.getRightDistance()));
    myRightFinder.setEndY(finderEndY(-90,myCar.getRightDistance()));

    myWheelRotateVisual.setStartX(myCar.getFrontXPos());
    myWheelRotateVisual.setStartY(myCar.getFrontYPos());
    myWheelRotateVisual.setEndX(finderEndX(myCar.getWheelTurn(),50));
    myWheelRotateVisual.setEndY(finderEndY(myCar.getWheelTurn(),50));
  }

  private double finderEndX(double angleDiffDegrees, double length) {
    return myCar.getFrontXPos() + (Math.cos(degreesToRadians(myRotation + angleDiffDegrees)) * length);
  }

  private double finderEndY(double angleDiffDegrees, double length) {
    return myCar.getFrontYPos() + (Math.sin(degreesToRadians(myRotation + angleDiffDegrees)) * length);
  }

  private double degreesToRadians(double degrees) {
    return (degrees / 360.0) * 2 * Math.PI;
  }

  @Override
  public void reactToPointGain(double pointGain) {
    // Doesn't care (yet)
  }

  @Override
  public void reactToPositionChange(double newX, double newY) {
    setPosition(newX,newY);
  }

  @Override
  public void reactToDirectionChange(double directionDegrees) {
    setRotation(directionDegrees);
  }

  @Override
  public void reactToRemoval(CollisionEntity entity) {
    //TODO: just pass the message up through the ranks I guess?
    for (VisualizerListener listener : myListeners) {
      listener.reactToDeath(this);
    }
  }

  @Override
  public Parent getGroup() {
    return this;
  }

  @Override
  public void subscribe(VisualizerListener listener) {
    myListeners.add(listener);
  }
}
