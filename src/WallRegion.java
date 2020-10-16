import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;


public class WallRegion extends Parent implements ImpassableRegion {
  private Shape myRegion;
  private double myX;
  private double myY;
  private double myWidth;
  private double myHeight;

  public WallRegion(double xPos, double yPos, double width, double height) {
    myRegion = new Rectangle(xPos,yPos,width,height);
    myRegion.setFill(Color.GRAY);
    getChildren().add(myRegion);
    myX = xPos;
    myY = yPos;
    myWidth = width;
    myHeight = height;
  }

  public boolean pointInBounds(double xPos, double yPos) {
    return (xPos >= myX && xPos < myX + myWidth &&
            yPos >= myY && yPos < myY + myHeight);
  }
}
