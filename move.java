import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.animation.Timeline;

import javafx.application.Application;

import javafx.scene.Group;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.paint.Color;

import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Circle;

import javafx.stage.Stage;

import javafx.util.Duration;

import javafx.animation.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.event.ActionEvent;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;
import java.lang.System.*;
import javafx.animation.PathTransition.OrientationType;


public class move extends Application
{
   Circle car = new Circle(10, 10, 10, Color.BLUE);
   Path road = new Path();
   AnchorPane root = new AnchorPane();

   @Override
   public void start(Stage primaryStage)
   {
      // car.setImage(new Image("file:res/car.gif"));
      // car.setX(-car.getImage().getWidth() / 2);
      // car.setY(300 - car.getImage().getHeight());
      car.setRotate(90);

      PathElement[] path = 
      {
         new MoveTo(10, 10),
         // new ArcTo(100, 100, 0, 100, 400, false, false),
         new LineTo(10, 500),
         // new ArcTo(100, 100, 0, 400, 300, false, false),
         // new LineTo(400, 100),
         // new ArcTo(100, 100, 0, 300, 0, false, false),
         // new LineTo(100, 0),
         // new ArcTo(100, 100, 0, 0, 100, false, false),
         // new LineTo(0, 300),
         new ClosePath()
      };

      road.getElements().addAll(path);

      PathTransition anim = new PathTransition();
      anim.setNode(car);
      anim.setPath(road);
      anim.setOrientation(OrientationType.ORTHOGONAL_TO_TANGENT);
      anim.setInterpolator(Interpolator.LINEAR);
      anim.setDuration(new Duration(1000));
      anim.setCycleCount(1);

      root.getChildren().add(car);
      anim.setOnFinished(e -> delete());
      anim.play();
      // root.setOnMouseClicked(me -> 
      //             {
      //               Animation.Status status = anim.getStatus();
      //               if (status == Animation.Status.RUNNING &&
      //                   status != Animation.Status.PAUSED)
      //                   anim.pause();
      //               else
      //                   anim.play();
      //             });
      Scene scene = new Scene(root, 500, 500);
      primaryStage.setTitle("PathTransition Demo");
      primaryStage.setScene(scene);
      primaryStage.show();
      // for (long i = 0; i < 10000000000l; ++i) {
      // }
   }

   public void delete() {
      root.getChildren().remove(car);
   }
}