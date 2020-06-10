package sample;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import javafx.scene.image.ImageView;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
//кастомный ползунок скорости, просто переделка базовой внешности крутилки чтоб красиво было
class MySlider extends Slider{
    ImageView cursor, strelkaplus, strelkaminus,relsy;
    double startDragX;
    MoveController moveController;
    //установка размеров, картинок, положения
    MySlider( MoveController moveController) {
        super();
        this.moveController=moveController;
        cursor=new ImageView(new Image("parovozik_17-300.png"));
        strelkaminus=new ImageView(new Image("icons8-office-xs-801.png"));
        strelkaplus=new ImageView(new Image("icons8-office-xs-80.png"));
        relsy=new ImageView(new Image("relsy.png"));
        this.setBlockIncrement(1);
        cursor.setLayoutX(408);
        cursor.setLayoutY(740);
        relsy.setLayoutX(408);
        relsy.setLayoutY(700);
        strelkaminus.setFitWidth(50);
        strelkaminus.setFitHeight(50);
        strelkaplus.setFitHeight(50);
        strelkaplus.setFitWidth(50);
        strelkaminus.setLayoutX(808+50);
        strelkaminus.setLayoutY(740);
        strelkaplus.setLayoutY(740);
        strelkaplus.setLayoutX(808+120);
        strelkaplus.setAccessibleRole(AccessibleRole.BUTTON);
        strelkaminus.setAccessibleRole(AccessibleRole.BUTTON);
        cursor.setFitWidth(40);
        cursor.setFitHeight(40);
        relsy.setFitWidth(440);
        relsy.setFitHeight(80);
        cursor.setAccessibleRole(AccessibleRole.BUTTON);
        this.setMajorTickUnit(1);
        this.setMax(10);
        this.setMin(1);
        //установка расчета того как оно будет раюотать при перетаскивании и нажатии кнопок ускорения и замедления(стрелок)
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                DoubleBinding doubleBindingvalue=new DoubleBinding() {
                    {
                        super.bind(cursor.layoutXProperty());
                    }
                    @Override
                    protected double computeValue() {
                                    return (1+(cursor.layoutXProperty().get()-408)/44.44);
                    }
                };
                MySlider.this.valueProperty().bind(doubleBindingvalue);
                cursor.setOnMousePressed(e -> {
                    startDragX = cursor.getLayoutX() - e.getSceneX();
                });
                strelkaminus.setOnMouseClicked(e->{
                    if(cursor.getLayoutX()-44.44>408){
                    cursor.setLayoutX(cursor.getLayoutX()-44.44);
                    }
                    else cursor.setLayoutX(408);
                });
                strelkaplus.setOnMouseClicked(e->{
                    if(cursor.getLayoutX()+44.44<808){
                        cursor.setLayoutX(cursor.getLayoutX()+44.44);
                    }
                    else cursor.setLayoutX(808);
                });
                cursor.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if ((mouseEvent.getSceneX() + startDragX <= 808) && (mouseEvent.getSceneX() + startDragX >= 408)) {
                            cursor.setLayoutX((mouseEvent.getSceneX() + startDragX));
                        }

                    }
                });
                moveController.tabwithmap.getChildren().addAll(cursor,strelkaminus,strelkaplus,relsy);
            }
            });
        }

    }
//класс места в вагоне(квадратик)
class Chair{
    Boolean free;
    static int counter=1;
    int num,vagon;
    Rectangle rec;
    public Chair(){
    rec=new Rectangle(25,25);
    }
}
//класс с потоками для различных целей(посадка, высадка пассжиров и тд)
class AllTasks{
   Task<TabPane> taskformaininfopanel;
   Task taskofremoving;
   Task taskoflanding;
   MoveController moveController;
    Passenger observablepass;
    Chair observablechair;
    public  AllTasks(SheduleMember sheduleMember, MoveController moveController) {
        this.moveController=moveController;
        //поток для просмотра информации о пассажире в окошке и просмотра информации о поезде
        taskformaininfopanel = new Task<TabPane>()
        {
            @Override
            protected TabPane call() throws Exception
            {
                TabPane TabpaneTableInfo=new TabPane();
                Tab tab1=new Tab();
                tab1.setText("Общая информация");
                AnchorPane anchorPane=new AnchorPane();
                tab1.setContent(anchorPane);
                TreeTableView tableInfo=new TreeTableView();
                anchorPane.getChildren().add(tableInfo);
                Tab tabwithvagons=new Tab();
                tabwithvagons.setText("Вагон");
                TabpaneTableInfo.getTabs().addAll(tab1,tabwithvagons);
                TabpaneTableInfo.setTranslateX(1100);
                TabpaneTableInfo.setTranslateY(300);
                TabpaneTableInfo.setMaxWidth(420);
                tableInfo.setMaxHeight(80);

                TableView pasInfo=new TableView();
                pasInfo.setOpacity(80);
                TableColumn pascolumn=new TableColumn();
                TableColumn chaircolumn=new TableColumn();
                pascolumn.setText("Пассажир");
                chaircolumn.setText("Место");
                TableColumn namecolumn=new TableColumn();
                TableColumn surcolumn=new TableColumn();
                TableColumn secnamecolumn=new TableColumn();
                TableColumn passwordcolumn=new TableColumn();
                namecolumn.setText("имя");
                secnamecolumn.setText("отчество");
                surcolumn.setText("фамилия");
                passwordcolumn.setText("паспорт");
                TableColumn vagoncolumn=new TableColumn();
                vagoncolumn.setText("вагон");
                pasInfo.getColumns().addAll(pascolumn,chaircolumn,vagoncolumn);
                pascolumn.getColumns().addAll(namecolumn,surcolumn,secnamecolumn,passwordcolumn);

                TreeTableColumn<Train, String> traincolumn = new TreeTableColumn<Train, String>("Поезд");
                TreeTableColumn<SheduleMember, String> timefromcolumn = new TreeTableColumn<SheduleMember, String>("Время \nотправки");
                TreeTableColumn<City, String> cityfromcolumn = new TreeTableColumn<City, String>("Откуда");
                TreeTableColumn<City, String> citytoclumn = new TreeTableColumn<City, String>("Куда");
                TreeTableColumn<SheduleMember, String> timetocolumn = new TreeTableColumn<SheduleMember, String>("Время \nприбытия");
                TreeTableColumn<Train, String> countpascolumn = new TreeTableColumn<Train, String>("Пассажиров");
                tableInfo.getColumns().addAll(traincolumn, timefromcolumn, timetocolumn,cityfromcolumn,citytoclumn,countpascolumn);
                TreeItem<Train> traincell = new TreeItem<Train>(sheduleMember.train);
                TreeItem<SheduleMember> timefromcell = new TreeItem<SheduleMember>(sheduleMember);
                TreeItem<SheduleMember> timetocell = new TreeItem<SheduleMember>(sheduleMember);
                tableInfo.setRoot(traincell);
                cityfromcolumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<City, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<City, String> trainStringCellDataFeatures) {
                        return new SimpleObjectProperty<String>(sheduleMember.fromcity.name);
                    }
                });
                citytoclumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<City, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<City, String> trainStringCellDataFeatures) {
                        return new SimpleObjectProperty<String>(sheduleMember.toCity.name);
                    }
                });
                countpascolumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Train, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Train, String> trainStringCellDataFeatures) {
                        return new SimpleObjectProperty<String>(String.valueOf(sheduleMember.train.getArrofpassengers().size()));
                    }
                });
                traincolumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Train, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Train, String> trainStringCellDataFeatures) {
                        return new SimpleObjectProperty<String>("КЖД" + sheduleMember.train.id + "МЕМ");
                    }
                });
                timefromcolumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<SheduleMember, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<SheduleMember, String> sheduleMemberStringCellDataFeatures) {
                        return new SimpleObjectProperty<>(sheduleMember.timeofstart);
                    }
                });
                timetocolumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<SheduleMember, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<SheduleMember, String> sheduleMemberStringCellDataFeatures) {
                        return new SimpleObjectProperty<>(sheduleMember.timeofarriving);
                    }
                });


                namecolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                    @Override
                    public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                        if(observablepass==null){
                            return new SimpleStringProperty("-----");}
                        return new SimpleObjectProperty(observablepass.name);
                    }
                });
                surcolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                    @Override
                    public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                        if(observablepass==null){
                            return new SimpleStringProperty("-----");}
                        return new SimpleObjectProperty(observablepass.surname);
                    }
                });
                secnamecolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                    @Override
                    public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                        if(observablepass==null){
                            return new SimpleStringProperty("-----");}
                        return new SimpleObjectProperty(observablepass.fatherName);
                    }
                });
                chaircolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                    @Override
                    public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                        if(observablepass==null){
                            return new SimpleStringProperty(String.valueOf(observablechair.num));}
                        return new SimpleObjectProperty(observablepass.ticket.seat);
                    }
                });
                passwordcolumn.setCellValueFactory((new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                    @Override
                    public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                        if(observablepass==null){
                            return new SimpleStringProperty("-----");}
                        return new SimpleObjectProperty(observablepass.passport);
                    }
                }));
                vagoncolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                    @Override
                    public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                        if(observablepass==null){
                            return new SimpleStringProperty(String.valueOf(observablechair.vagon));}
                        return new SimpleObjectProperty(observablepass.ticket.vagon);
                    }
                });
//штука для переключения просмотра различных вагонов в окошке и просмотра общей картины мест(кто занят красный, желтыц-занят нами купленным билетом. ЗЕЛЕНЫЙ СВОБОДЕН)
                Pagination pagination = new Pagination();
                pagination.setCurrentPageIndex(0);
                pagination.setMaxPageIndicatorCount(sheduleMember.train.capacity / 50);
                pagination.setPageCount(sheduleMember.train.capacity / 50);
                tabwithvagons.setContent(pagination);
                Chair[][] sits = new Chair[(int) (sheduleMember.train.capacity / 50)][50];
                pagination.setPageFactory(new Callback<Integer, Node>() {
                    @Override
                    public Node call(Integer integer) {
                        AnchorPane vagon = new AnchorPane();
                        Chair.counter=1;
                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 10; j++) {
                                Chair chair = new Chair();

                                if (i < 3) {
                                    chair.rec.setX(40 * i);
                                } else {
                                    chair.rec.setX(40 * i + 55);
                                }

                                chair.rec.setY(42 * j);
                                chair.num = Chair.counter;
                                Chair.counter++;
                                chair.vagon = pagination.getCurrentPageIndex() + 1;
                                for (Passenger p : sheduleMember.train.getArrofpassengers()
                                ) {
                                    if (p.ticket.seat == chair.num && p.ticket.vagon == chair.vagon && p.type=="Main") {
                                        chair.rec.setFill(Color.YELLOW);
                                        break;
                                    }
                                    if (p.ticket.seat == chair.num && p.ticket.vagon == chair.vagon) {
                                        chair.rec.setFill(Color.RED);
                                        break;
                                    } else {
                                        chair.rec.setFill(Color.GREEN);
                                    }
                                }

                                sits[chair.vagon - 1][chair.num - 1] = chair;
                                vagon.getChildren().addAll(chair.rec);
                                chair.rec.setAccessibleRole(AccessibleRole.BUTTON);
                                chair.rec.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent mouseEvent) {
                                        pasInfo.setTranslateX(chair.rec.getX()+600);
                                        pasInfo.setTranslateY(chair.rec.getY()+230);
                                        pasInfo.setMaxHeight(80);
                                        pasInfo.getItems().remove(observablepass);
                                        observablepass=null;
                                        observablechair=chair;
                                        for (Passenger p: sheduleMember.train.getArrofpassengers()
                                             ) {
                                            if (p.ticket.seat==chair.num &&  p.ticket.vagon == chair.vagon){
                                                observablepass=p;
                                                break;
                                            }
                                        }
                                        pasInfo.getItems().add(observablepass);
                                        if (pasInfo.isVisible() == false) {
                                            pasInfo.setVisible(true);
                                            pasInfo.setDisable(false);
                                            moveController.pasInfo=pasInfo;
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    moveController.tabwithmap.getChildren().remove(moveController.pasInfo);
                                                    moveController.tabwithmap.getChildren().add(moveController.pasInfo);
                                                }
                                            });
                                        } else {
                                            pasInfo.setVisible(false);
                                            pasInfo.setDisable(true);
                                            moveController.pasInfo=pasInfo;
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    moveController.tabwithmap.getChildren().remove(moveController.pasInfo);
                                                }
                                            });
                                        }

                                    }
                                });
                            }
                        }
                        return vagon;
                    }
                });
                sheduleMember.train.TabpaneTableInfo=TabpaneTableInfo;
                sheduleMember.train.pasInfo=pasInfo;
                return null;
            }
        };
//поток для высадки пассажиров с поезда в город
        taskofremoving=new Task() {
            @Override
            protected Object call() throws Exception {
                moveController.getCurrenttransactions().remove(sheduleMember.pathtr);
                sheduleMember.train.getArrofpassengers().stream().filter(i->i.type=="Main").forEach(i->i.ticket=null);
               sheduleMember.train.getArrofpassengers().removeAll(sheduleMember.train.getArrofpassengers().stream().filter(i->i.type=="Main").collect(Collectors.toList()));
                Iterator<Passenger> itpasoutput = sheduleMember.train.getArrofpassengers().iterator();
                ArrayList<Passenger> toRemove=new ArrayList<>();
                while (itpasoutput.hasNext()) {
                    Passenger curpas = itpasoutput.next();
                    sheduleMember.toCity.getDumpofpassengers().add(curpas);
                    toRemove.add(curpas);
                }
                sheduleMember.train.getArrofpassengers().removeAll(toRemove);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        moveController.tabwithmap.getChildren().remove(sheduleMember.train.pasInfo);
                        moveController.tabwithmap.getChildren().remove(sheduleMember.train.TabpaneTableInfo);
                        moveController.stackpanewithmap.getChildren().remove(sheduleMember.train.viewtrain);
                        moveController.stackpanewithmap.getChildren().remove( sheduleMember.roadpicture);
                    }
                });
                return null;
            }
        };
        //поток для посадки пассажиров на поезд(так же занимается пересадкой наших реальных пассажиров кому мы купили билеты)
        taskoflanding =new Task() {
            @Override
            protected Object call() throws Exception {
                int seatnum,vagon;
                Integer [][] currentsits=new Integer[sheduleMember.train.capacity/50][50];
                try {
                    moveController.realPassengersTransition.idpasandallsendingshashmap.keySet().stream().forEach(x->
                    {moveController.realPassengersTransition.idpasandallsendingshashmap.get(x).stream().findFirst().ifPresentOrElse(i->{
                                if (i.sheduleMember==sheduleMember && x.ticket==null) {sheduleMember.train.getArrofpassengers().add(x); x.ticket=i; currentsits[i.vagon-1][i.seat-1]=1; moveController.realPassengersTransition.idpasandallsendingshashmap.get(x).remove(i); return;}},
                            ()-> moveController.realPassengersTransition.idpasandallsendingshashmap.remove(x)); return;});
                }
              catch (Exception ex){
                  //  ex.printStackTrace();
              }

                for (int i=1; i<(int)(100+ sheduleMember.train.capacity*Math.random());i++) {
                    Passenger curpas;
                    try {
                        curpas = sheduleMember.fromcity.getDumpofpassengers().get(i);
                    }
                    catch (IndexOutOfBoundsException ex){
                        break   ;
                    }

                    if (sheduleMember.train.getArrofpassengers().size() < sheduleMember.train.capacity -50) {
                        do {
                            seatnum = (int)( Math.random() * 50+1);
                            vagon = (int)(Math.random() * sheduleMember.train.capacity/50+1);
                        }  while (currentsits[vagon-1][seatnum-1] !=null);
                        currentsits[vagon-1][seatnum-1]=1;
                        curpas.ticket=new Ticket(sheduleMember, seatnum,vagon);
                        sheduleMember.train.getArrofpassengers().add(curpas);
                        sheduleMember.fromcity.getDumpofpassengers().remove(curpas);
                    }
                    else break;
                }

                return null;
            }
        };

    }
}
//класс-поток который просто следит чтобы пауза меньше лагала(однако лаги полностью не устраняет, это не стоит говорить ему), проверяет каждые 300-500 мс все ли норм с паузой , если не норм ставит необходимые паровозы на нее
class  PauseInspector implements Runnable{
    MoveController moveController;
    public  PauseInspector(MoveController moveController){
    this.moveController=moveController;
   }
    @Override
    public void run() {
     while (true){
         try {
             Thread.sleep(200);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         if (MoveController.isPaused==true){
             for (PathTransition p: moveController.getCurrenttransactions().values()
                  ) {

                     p.pause();

             }
         }
         ArrayList<PathTransition> toremove=new ArrayList<PathTransition>();
         ConcurrentLinkedQueue<SheduleMember> concurrentLinkedQueue=new ConcurrentLinkedQueue(moveController.getCurrenttransactions().keySet());
         for (SheduleMember s: concurrentLinkedQueue
              ) {
             try {
                 if (moveController.dateFormat.parse(s.timeofarriving).getHours()*60*60+moveController.dateFormat.parse(s.timeofarriving).getMinutes()*60+moveController.dateFormat.parse(s.timeofarriving).getSeconds()<moveController.trainClock.apptime.toZonedDateTime().toLocalTime().toSecondOfDay()){
                    s.pathtr.stop();
                    toremove.add(s.pathtr);
                    AllTasks allTasks=new AllTasks(s,moveController);
                    new Thread(allTasks.taskofremoving).start();
                 };
             } catch (ParseException e) {
                 e.printStackTrace();
                 System.out.println(s.timeofstart+s.timeofarriving+s.id);
             }
         }
         for (PathTransition p: toremove
              ) {
             moveController.getCurrenttransactions().remove(p);
         }
         toremove.clear();
     }
    }
}
// класс для просмотра расписания всех поездов и всех маршрутов
class SheduleviewInspector implements  Runnable{
    MoveController moveController;
    TableView sheduletable;
    public  SheduleviewInspector(MoveController moveController){
        this.moveController=moveController;
    }

    @Override
    public void run() {
        sheduletable=new TableView();
        sheduletable.setId("tableshedule");
        TableColumn<SheduleMember, String> cityfromcolumn=new TableColumn<SheduleMember, String>();
        TableColumn<SheduleMember, String> citytocolumn=new TableColumn<SheduleMember, String>();
        TableColumn<SheduleMember, String> timeofarrivingcolumn=new TableColumn<SheduleMember, String>();
        TableColumn<SheduleMember, String> timeofsendcolumn=new TableColumn<SheduleMember, String>();
        TableColumn<SheduleMember, String> traincolumn=new TableColumn<SheduleMember, String>();
        cityfromcolumn.setText("Город Отправления");
        citytocolumn.setText("Город Прибытия");
        timeofarrivingcolumn.setText("Время прибытия");
        timeofsendcolumn.setText("Время отправки");
        traincolumn.setText("Номер поезда");
        sheduletable.setPrefWidth(1550);
        sheduletable.setPrefHeight(850);
        cityfromcolumn.setMinWidth(1550/5);
        citytocolumn.setMinWidth(1550/5);
        timeofarrivingcolumn.setMinWidth(1550/5);
        timeofsendcolumn.setMinWidth(1550/5);
        traincolumn.setMinWidth(1550/5);

        synchronized (moveController.shedulelockcondition1){
                try {
                    moveController.shedulelockcondition1.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
       sheduletable.setItems(FXCollections.observableList(moveController.trainClock.oracleSQL.shedule.commonshedule));
        sheduletable.getColumns().addAll(traincolumn,cityfromcolumn,citytocolumn,timeofsendcolumn, timeofarrivingcolumn);
        cityfromcolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SheduleMember, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SheduleMember, String> trainStringCellDataFeatures) {
                return new SimpleObjectProperty<String>(trainStringCellDataFeatures.getValue().fromcity.name);
            }
        });
        citytocolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SheduleMember, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SheduleMember, String> trainStringCellDataFeatures) {
                return new SimpleObjectProperty<String>(trainStringCellDataFeatures.getValue().toCity.name);
            }
        });
        traincolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SheduleMember, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SheduleMember, String> trainStringCellDataFeatures) {
                return new SimpleObjectProperty<String>("КЖД" + trainStringCellDataFeatures.getValue().train.id + "МЕМ");
            }
        });
        timeofsendcolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SheduleMember, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SheduleMember, String> sheduleMemberStringCellDataFeatures) {
                return new SimpleObjectProperty<>(sheduleMemberStringCellDataFeatures.getValue().timeofstart);
            }
        });
        timeofarrivingcolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SheduleMember, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SheduleMember, String> sheduleMemberStringCellDataFeatures) {
                return new SimpleObjectProperty<>(sheduleMemberStringCellDataFeatures.getValue().timeofarriving);
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                moveController.SheduleTabAnchor.getChildren().addAll(sheduletable);
            }
        });
    }
}
//класс с хранением информации о пасаажирах, которых добавили мы сами
class RealPassengersTransition{
    MoveController moveController;
    HashMap<Passenger, LinkedList<Ticket>> idpasandallsendingshashmap;
    public  RealPassengersTransition(MoveController moveController){
        this.moveController=moveController;
        idpasandallsendingshashmap=new HashMap<>();
    }
}
// класс для запуска паровозиков
class Trainsgoclass implements Runnable{
    SheduleMember sheduleMember;
    MoveController moveController;
    public Trainsgoclass(SheduleMember sheduleMember, MoveController moveController){
        this.sheduleMember=sheduleMember;
        this.moveController=moveController;
    }
    @Override
    public void run() {
//установка небольшой задержки перед отправкой чтобы уменьшить лаг с паузой
        while (MoveController.isPaused==true){
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //запуск потока для посадки и просмотра инфы
        AllTasks allTasks=new AllTasks(sheduleMember, moveController);
        new Thread(allTasks.taskformaininfopanel).start();
        new Thread(allTasks.taskoflanding).start();
        //построение пути
        Path path=new Path();
        path.getElements().add(new MoveTo(sheduleMember.fromcity.location.x,
                sheduleMember.fromcity.location.y));
        Coordinates first,second;
        if(sheduleMember.fromcity.location.distanceto(sheduleMember.roadoftrip.nearesttocity1)<=sheduleMember.fromcity.location.distanceto(sheduleMember.roadoftrip.nearesttocity2)){
            first=sheduleMember.roadoftrip.nearesttocity1;
            second=sheduleMember.roadoftrip.nearesttocity2;
        }
        else {
            second=sheduleMember.roadoftrip.nearesttocity1;
            first=sheduleMember.roadoftrip.nearesttocity2;
        }
        path.getElements().add(new CubicCurveTo(first.x,first.y,second.x,second.y,sheduleMember.toCity.location.x,sheduleMember.toCity.location.y));
        sheduleMember.pathtr=new PathTransition();
        try {
            sheduleMember.pathtr.setDuration(Duration.millis((moveController.dateFormat.parse(sheduleMember.timeofarriving).getTime()-
                    moveController.dateFormat.parse(sheduleMember.timeofstart).getTime())/600)); //на 600 т к 1с = 10 минут(600 секунд)
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println(sheduleMember.id);
        }
        sheduleMember.pathtr.setPath(path);
        sheduleMember.train.viewtrain.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                moveController.infotableproperties(sheduleMember);

            }
        });
        //настройка скорости и прочего
        sheduleMember.pathtr.setRate(moveController.mySlider.getValue());
        sheduleMember.pathtr.setNode(sheduleMember.train.viewtrain);
        sheduleMember.pathtr.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        sheduleMember.pathtr.setAutoReverse(true);
        //отображение дороги (красной пункутирной линии)
        sheduleMember.roadpicture=new CubicCurve(sheduleMember.fromcity.location.x,
                sheduleMember.fromcity.location.y,first.x,first.y,second.x,second.y,sheduleMember.toCity.location.x,sheduleMember.toCity.location.y);
        sheduleMember.roadpicture.setStroke(Color.ORANGERED);
        sheduleMember.roadpicture.setStrokeWidth(5);
        sheduleMember.roadpicture.setStrokeDashOffset(10);
        sheduleMember.roadpicture.getStrokeDashArray().addAll(10.0, 10.0,10.0,10.0);
        sheduleMember.roadpicture.setFill(Color.TRANSPARENT);

        sheduleMember.roadpicture.setTranslateX(Math.min(sheduleMember.fromcity.location.x, sheduleMember.toCity.location.x));
        sheduleMember.roadpicture.setTranslateY(Math.min(sheduleMember.fromcity.location.y, sheduleMember.toCity.location.y));
        //добавление поезда и дороги на карту
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    moveController.stackpanewithmap.getChildren().add(sheduleMember.train.viewtrain);
                    moveController.stackpanewithmap.getChildren().add(sheduleMember.roadpicture);
                    sheduleMember.train.viewtrain.toFront();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        })  ;
        //запуск
        sheduleMember.pathtr.play();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        moveController.getCurrenttransactions().put(sheduleMember, sheduleMember.pathtr);
    }
}
//класс контроллер связывающий все
public class MoveController {

    PauseInspector pauseInspector;
    @FXML
    ImageView backtoauth;
    @FXML
    ImageView ImageViewofmap;
    @FXML
    ImageView exitbutton;
    @FXML
    ImageView pausebutton;
    @FXML
    AnchorPane SheduleTabAnchor;
    MySlider mySlider;
    TableView curtablecity;
    ConcurrentHashMap<SheduleMember, PathTransition> currenttransactions;
    DateFormat dateFormat=new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    static boolean isPaused=false;
    TrainClock trainClock;
    @FXML
    Label clock;
    //просто процедура врзрата штуки с временем чтоб где-то использовать
    @FXML
    public Label getClockLabel(){

        return clock;
    }
    @FXML
    public Tab TrainMapTab;
    @FXML
    StackPane stackpanewithmap;
    @FXML
    AnchorPane borderformap;
    @FXML
    AnchorPane tabwithmap;
    @FXML
    StackPane borderofborder;
    Object shedulelockcondition1;
    RealPassengersTransition realPassengersTransition;
    Task audioplay;
    AudioClip phoneaudio;

    double startDragX;
    double startDragY;
    TabPane curtabpane;
    boolean pausebuttonpressed;
    TableView pasInfo;
    Future future;
    Boolean execotorbol1=true;
    ReentrantLock pauselocker;
    SheduleviewInspector sheduleviewInspector;
    Stage primarystage;
    Parent authroot;
    //поток для нажатия кнопки паузы
    Runnable pauserannable=()->{
        pausebutton.setDisable(true);
        if (pausebuttonpressed) return;
        pausebuttonpressed=true;
        if(isPaused==true){isPaused=false;
            for (PathTransition p: getCurrenttransactions().values()
            ) {
                p.play();
                p.setRate(mySlider.getValue());
            }}

        else {isPaused=true;
            for (PathTransition p: getCurrenttransactions().values()
            ) {
                p.pause();
            }}

        synchronized (TrainClock.pauseLock){
            if (isPaused==true){TrainClock.paused=true; }
            else {
                TrainClock.paused=false;
                TrainClock.pauseLock.notifyAll();
            }}
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pausebutton.setDisable(false);
        pausebuttonpressed=false;
    };
    synchronized public ConcurrentHashMap<SheduleMember, PathTransition> getCurrenttransactions() {
        return currenttransactions;
    }

    @FXML
    public void initialize(){
        {
            backtoauth.setAccessibleRole(AccessibleRole.BUTTON);
            //все текущие поездки
            currenttransactions=new ConcurrentHashMap<SheduleMember, PathTransition>();
            //крутилка скорости
            mySlider=new MySlider(this);
            shedulelockcondition1=new Object();
            //пуск музычки
            audioplay=new Task() {
                @Override
                protected Object call() throws Exception {
                    File audio=new File("src/primo.mp3");
                    try {
                        phoneaudio = new AudioClip(audio.toURI().toString());
                    }
                    catch (Exception ex){
                        ex.printStackTrace();               }
                    phoneaudio.setCycleCount(10);
                    Thread.sleep(3000);
                    phoneaudio.play();
                    return null;
                }
            };
            new Thread(audioplay).start();

            realPassengersTransition =new RealPassengersTransition(this);
            sheduleviewInspector=new SheduleviewInspector(this);
            //установка рамочки ыокруг карты
            new Thread(sheduleviewInspector).start();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Rectangle rect = new Rectangle(borderformap.getTranslateX()+60,borderformap.getTranslateY()+10,1200, 680);
                    rect.setArcHeight(45);
                    rect.setArcWidth(45);
                    borderformap.setClip(rect);
                    borderformap.setMaxSize(1230.,700.);
                    borderofborder.setPrefSize(1230.0, 710);

                }
            });
            pauselocker=new ReentrantLock();
            //возможность вертеть и масштабировать карту
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    mySlider.valueProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                            if (!isPaused){
                                for (PathTransition p: getCurrenttransactions().values()
                                ) {
                                    p.setRate(mySlider.getValue());
                                }}
                            TrainClock.durationofoneminute=(TrainClock.durationofoneminute*((double)number)/(double)t1);
                        }
                    });
                    stackpanewithmap.setOnScroll((ScrollEvent event) -> {
                        double zoomFactor = 1.05;
                        double deltaY = event.getDeltaY();
                        if (deltaY < 0){
                            zoomFactor = 2.0 - zoomFactor;
                        }
                        stackpanewithmap.setScaleX(stackpanewithmap.getScaleX() * zoomFactor);
                        stackpanewithmap.setScaleY(stackpanewithmap.getScaleY() * zoomFactor);
                    });;
                    stackpanewithmap.setOnMousePressed(e -> {
                        startDragX =stackpanewithmap.getLayoutX()- e.getSceneX();
                        startDragY =stackpanewithmap.getLayoutY()-e.getSceneY();
                    });
                    stackpanewithmap.setOnMouseDragged(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            stackpanewithmap.setLayoutX(mouseEvent.getSceneX()+startDragX);
                            stackpanewithmap.setLayoutY(mouseEvent.getSceneY()+startDragY);
                        }
                    });

                }
            });
            pauseInspector= new PauseInspector(this);
            new Thread(pauseInspector).start();
            //штука для запуска той штуки которая управляет паузой pauserannable
            ExecutorService executorService= Executors.newFixedThreadPool(1);
            pausebutton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (execotorbol1==true){
                    execotorbol1=false;
                    future = executorService.submit(pauserannable);
                        try {
                            future.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } finally {
                            execotorbol1=true;
                        }
                    }

                }
            });
        }
        //кнопка возврата к билетам
        backtoauth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                    primarystage.getScene().setRoot(authroot);

            }
        });
    }
    public MoveController() throws Exception{

    }
    //регулирует отображение табличек с информацией обо всем(не реализует, а именно управляет когда показывать надо их)
    @FXML
    public void infotableproperties(SheduleMember sheduleMember){
        if (sheduleMember.train.TabpaneTableInfo.isVisible()){
            sheduleMember.train.TabpaneTableInfo.setVisible(false);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    tabwithmap.getChildren().remove(sheduleMember.train.TabpaneTableInfo);
                }
            });
        }
        else {
            sheduleMember.train.TabpaneTableInfo.setVisible(true);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try{
                    tabwithmap.getChildren().remove(curtabpane);
                        tabwithmap.getChildren().remove(pasInfo);}
                    catch (NullPointerException ex){};
                    curtabpane=sheduleMember.train.TabpaneTableInfo;
                    tabwithmap.getChildren().add(curtabpane);
                }
            });
        }

    }
    //запускает поток запуска паровозов
    @FXML
    public void traintransacrion(SheduleMember sheduleMember) throws ParseException, InterruptedException {
        new Trainsgoclass(sheduleMember,this).run();
    }


//кнопка выхода из программы
    @FXML
    public void exit(){
    Platform.exit();
    System.exit(0);
    }

}
