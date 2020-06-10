package sample;

import java.sql.*;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.CubicCurve;
import javafx.stage.Stage;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.util.Callback;

//класс связи с базами данных
class OracleSQL   {
    MoveController moveController;
    Shedule shedule;
    Connection conn;
    HashMap<Integer,City> allcities;
    HashMap<Integer, Train> alltrains;
    HashMap<Integer, Road> allroads;
    HashMap<Integer, Passenger> allpassengers;
    //класс расписание
    class Shedule{
        ArrayList<SheduleMember> commonshedule;
        public Shedule() throws SQLException {
            commonshedule=OracleSQL.this.getshedule();
        }
        //отправка паровозов если сработала функция проверка на наличие отправлений в текущее время
        public void startcurrenttrains(GregorianCalendar currenttime) throws ParseException, InterruptedException {

            if (commonshedule!=null) {
                Iterator<SheduleMember> iterator1 = commonshedule.iterator();
                while (iterator1.hasNext()) {
                    SheduleMember curshedulemember = iterator1.next();
                    if (curshedulemember.checkbyclock(currenttime)) {
                        moveController.traintransacrion(curshedulemember);
                    }
                }
            }
            else System.out.println("Расписание не загружено");
        }
    }
    //возвращает arraylist членов расписания
    public ArrayList<SheduleMember> getshedule() throws SQLException {
        ArrayList<SheduleMember> sheduleMembers=new ArrayList<SheduleMember>();
        Statement statement=conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * from TIMETABLE");
        while (resultSet.next()){
            sheduleMembers.add(new SheduleMember(resultSet.getInt("id_of_sending"),
                    resultSet.getString("time_start"),
                    resultSet.getString("time_arriving"),
                    allroads.get(resultSet.getInt("ID_OF_road")),
                    alltrains.get(resultSet.getInt("id_of_train")),
                    allcities.get(resultSet.getInt("id_fromcity")),
                    allcities.get(resultSet.getInt("id_tocity"))));
        }
        return sheduleMembers;
    }

    public OracleSQL(MoveController moveController) throws ClassNotFoundException, SQLException {
        this.moveController=moveController;
        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:EC11", "c##kaluger", "Googlerast1");
        allcities=gethashmapofcities();
        alltrains=getalltrains();
        allroads=allroads();
        shedule=new Shedule();
        allpassengers=allpassengers();


        Iterator  itfillcity=allcities.entrySet().iterator();
        Iterator  itfillpas=allpassengers.entrySet().iterator();
        while (itfillcity.hasNext()){
            Map.Entry curcity =(Map.Entry)itfillcity.next();
            for (int i=1; i<(int)allpassengers.size()/allcities.size();i++){
                Map.Entry curpas =(Map.Entry)itfillpas.next();
                ((City)curcity.getValue()).getDumpofpassengers().add((Passenger)curpas.getValue());
            }
        }

//устанавливается информация о городах для ее дальнейшего отображения в окошках информации о городе, добавляются иконки городов
        for (City c: this.allcities.values()
        ) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    moveController.stackpanewithmap.getChildren().add(c.icon) ;
                    c.icon.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            if (moveController.tabwithmap.getChildren().contains(c.cityinform)) {
                                moveController.tabwithmap.getChildren().remove(c.cityinform);
                            }
                            else {
                                moveController.tabwithmap.getChildren().remove(moveController.curtablecity);
                                moveController.tabwithmap.getChildren().add(c.cityinform);
                                moveController.curtablecity=c.cityinform;
                                c.cityinform.setItems(FXCollections.observableList(c.getDumpofpassengers()));
                            }
                        }
                    });
                }
            });
            c.namecolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Passenger, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Passenger, String> passengerStringCellDataFeatures) {
                    return new SimpleObjectProperty<String>(((Passenger)passengerStringCellDataFeatures.getValue()).name);
                }
            });
            c.surcolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Passenger, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Passenger, String> passengerStringCellDataFeatures) {
                    return new SimpleObjectProperty<String>(((Passenger)passengerStringCellDataFeatures.getValue()).surname);
                }
            });
            c.secnamecolumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Passenger, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Passenger, String> passengerStringCellDataFeatures) {
                    return new SimpleObjectProperty<String>(((Passenger)passengerStringCellDataFeatures.getValue()).fatherName);
                }
            });
            c.num.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Passenger, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Passenger, String> passengerStringCellDataFeatures) {
                    return new SimpleObjectProperty<String>(Integer.toString(1+(Integer)(c.getDumpofpassengers().indexOf(passengerStringCellDataFeatures.getValue()))));
                }
            });


        }

    }
//получаем все города из БД
    public HashMap<Integer,City> gethashmapofcities() throws SQLException {
        HashMap<Integer,City> integerCityHashMap=new HashMap<Integer,City>();
        Statement statement =conn.createStatement() ;
        ResultSet resultSet=statement.executeQuery("SELECT * FROM CITIES");
        while (resultSet.next()){
            integerCityHashMap.put(resultSet.getInt("id_of_city"),
                    new City(resultSet.getString("name_of_city"),
                            resultSet.getInt("X"),
                            resultSet.getInt("Y"),
                            resultSet.getInt("id_of_city")));
        }
        return integerCityHashMap;
    }
    //возвращает hashmap поездов
    public HashMap<Integer,Train> getalltrains() throws SQLException {
        HashMap <Integer,Train> integerTrainHashMap=new HashMap<Integer, Train>();
        Statement statement=conn.createStatement();
        ResultSet resultSet=statement.executeQuery("SELECT  * FROM TRAINS");
        while (resultSet.next()){
            integerTrainHashMap.put(resultSet.getInt("id_of_train"),
                    new Train(resultSet.getInt("id_of_train"),
                            resultSet.getInt("capacity_of_train"), resultSet.getString("type_of_train"),
                            resultSet.getInt("price_km")));
        }
        return integerTrainHashMap;
    }
    //возвращает hashmap дорог
    public HashMap<Integer,Road> allroads() throws SQLException {
        HashMap<Integer,Road> integerRoadHashMap=new HashMap<Integer, Road>();
        Statement statement =conn.createStatement() ;
        ResultSet resultSet=statement.executeQuery("SELECT * FROM ROADS");
        while (resultSet.next()){
            integerRoadHashMap.put(resultSet.getInt("id_of_road"),
                    new Road(allcities.get(resultSet.getInt("id_first_city")),
                            allcities.get(resultSet.getInt("id_second_city")),
                            new Coordinates(resultSet.getInt("x1"), resultSet.getInt("y1")),
                            new Coordinates(resultSet.getInt("x2"), resultSet.getInt("y2")),
                            resultSet.getInt("id_of_road")));
        }
        return integerRoadHashMap;
    }
    //возвращает hashmap пассажиров
    public HashMap<Integer,Passenger> allpassengers() throws SQLException {
        HashMap<Integer,Passenger> integerPassengersHashMap=new HashMap<Integer, Passenger>();
        Statement statement =conn.createStatement() ;
        ResultSet resultSet=statement.executeQuery("SELECT * FROM PASSENGERS");
        while (resultSet.next()){
            integerPassengersHashMap.put(resultSet.getInt("id_pas"),
                    new Passenger(resultSet.getInt("id_pas"),
                            resultSet.getString("name"),
                            resultSet.getString("surname"),
                            resultSet.getString("fatherName"),
                            resultSet.getString("gender"),
                            resultSet.getString("passport"),
                            resultSet.getString("type")));
        }
        return integerPassengersHashMap;
    }
    //добавление в бд пользователя( с окна регистрация)
    public void signUpUser (User user) throws SQLException, ClassNotFoundException {//добавление в oracle нового пользователя после регистрации
        String insert = " insert into USERS ( LOGIN, PASSWORD)" + " VALUES (?, ?)";
        try {
            PreparedStatement prSt = conn.prepareStatement(insert);
            prSt.setString(1,user.getLogin());
            prSt.setString(2,user.getPassword());
            prSt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    //получаем userов с заданными паролем и логином
    public ResultSet getUser (User user)
    {
        ResultSet resset = null;
        String select = "SELECT * FROM USERS WHERE LOGIN =? AND PASSWORD =?";
        try {
            PreparedStatement prSt = conn.prepareStatement(select);
            prSt.setString(1,user.getLogin());
            prSt.setString(2,user.getPassword());
            resset = prSt.executeQuery();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return resset;
    }
//проверка на существование логина
    public ResultSet check_existence_login (User user)
    {
        ResultSet resset = null;
        String select = "SELECT * FROM USERS WHERE LOGIN =?";
        try {
            PreparedStatement prSt = conn.prepareStatement(select);
            prSt.setString(1,user.getLogin());
            resset = prSt.executeQuery();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return resset;
    }


    Consumer<Connection> close=conn-> {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    };
}
//класс член расписания(поля, конструктор, методы)
class SheduleMember{
    int id;
    String timeofstart,timeofarriving;
    Train train;
    double price;
    Road roadoftrip;
    City fromcity;
    City toCity;
    CubicCurve roadpicture;
    PathTransition pathtr;
    DateFormat dateFormat;
    public SheduleMember(int id,String timeofstart,String timeofarriving,Road road,Train train, City from, City to){
        this.id=id;
        this.roadoftrip=road;
        this.timeofstart=timeofstart;
        this.timeofarriving=timeofarriving;
        this.fromcity=from;
        this.toCity=to;
        this.train=train;
        dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        this.price=getprice();
    }
    //получение цены поездки
    public int getprice(){
        return (int)(roadoftrip.length()*train.priceforkm);
    }
    //проверка на отправку поезда в текущее время
    public boolean checkbyclock(GregorianCalendar apptime) throws ParseException {
        if (dateFormat.format(apptime.getTime()).equals(timeofstart)) {

            return true;
        }

        else{ return false;}
    }
}

class Ticket{//класс билет
    SheduleMember sheduleMember;
    Integer vagon,seat;

    public Ticket(SheduleMember sheduleMember, Integer seat, Integer vagon)
    {
        this.sheduleMember=sheduleMember;
        this.seat=seat;
        this.vagon=vagon;
    }
}

class Passenger{
    int id;
    String name,surname, fatherName, passport, gender, type;
    Ticket ticket;

    public Passenger(int id, String name, String surname, String fatherName, String gender,String passport,String type)
    {
        this.type=type;
        this.id=id;
        this.name=name;
        this.surname=surname;
        this.fatherName=fatherName;
        this.gender=gender;
        this.passport=passport;
    }
}//класс пассажир(поля, конструктор)

class Coordinates{
    double x,y;
    public Coordinates(double x, double y){
        this.x=x;
        this.y=y;
    }
    public double distanceto(Coordinates newcor){
        return Math.sqrt(Math.pow(x-newcor.x,2)+Math.pow(y-newcor.y,2));
    }
}//класс координаты(поля, конструктор, метод)

class Road{
    City city1, city2;
    Coordinates nearesttocity1, nearesttocity2;
    int id;
    public Road(City city1, City city2,Coordinates c1, Coordinates c2,int id){
        this.city1=city1;
        this.city2=city2;
        this.id=id;
        this.nearesttocity1=c1;
        this.nearesttocity2=c2;
    }
    public int length(){
        return (int)(Math.sqrt(Math.pow(city1.location.x-city2.location.x,2)+Math.pow(city1.location.y-city2.location.y,2)));}
}//класс дорога(поля, конструктор, метод)

class Train {
    int id, capacity, priceforkm;
    String typeoftrain;
    static ArrayList<String> allimages=new ArrayList<String>();

   ArrayList<Passenger> arrofpassengers=new ArrayList<Passenger>();
    TabPane TabpaneTableInfo;
    TableView pasInfo;

     synchronized public ArrayList<Passenger> getArrofpassengers() {
        return arrofpassengers;
    }


    static
    {
        allimages.add("trian_PNG16630.png");
        allimages.add(("trian_PNG16630.png"));
    };
    Image im;
    ImageView viewtrain;
    public Train(int id, int capacity, String typeoftrain, int priceforkm) {
        this.capacity = capacity;
        this.id = id;
        this.typeoftrain = typeoftrain;
        this.priceforkm = priceforkm;
        switch (typeoftrain){
            case ("p"): this.im=new Image(allimages.get(0));break;
            case ("k"):this.im=new Image(allimages.get(1));break;
        }
        viewtrain=new ImageView(im);
        viewtrain.setAccessibleRole(AccessibleRole.BUTTON);
        viewtrain.setFitWidth(20);
        viewtrain.setFitHeight(20);
    }
}// класс поезд(поля, конструктор)

class City{
    ArrayList <Passenger> dumpofpassengers=new ArrayList<Passenger>();
    int id;
    Coordinates location;
    String name;
    ImageView icon;
    TableColumn<Passenger, String> namecolumn;
    TableColumn<Passenger, String> surcolumn;
    TableView<Passenger> cityinform;
    TableColumn<Passenger, String> pascolumn;
    TableColumn<Passenger, String> secnamecolumn;
    TableColumn<Passenger, String > num;

    synchronized public ArrayList<Passenger> getDumpofpassengers() {
        return dumpofpassengers;
    }

    public City(String name, int x, int y, int id){
        location=new Coordinates(x,y);
        this.name=name;
        this.id =id;
        icon=new ImageView();
        icon.setAccessibleRole(AccessibleRole.BUTTON);

        icon.setImage(new Image("icons8-цветные-96.png"));
        icon.setTranslateX(this.location.x-10);
        icon.setTranslateY(this.location.y-10);
        icon.setFitWidth(20);
        icon.setFitHeight(20);

        cityinform=new TableView();
        cityinform.setEditable(true);
        cityinform.setManaged(true);

        pascolumn=new TableColumn();
        pascolumn.setText("Ожидающие в городе "+ this.name);

        namecolumn=new TableColumn<Passenger, String>();
        surcolumn=new TableColumn<Passenger, String>();
        secnamecolumn=new TableColumn<Passenger, String>();
        num = new TableColumn<Passenger,String>();
        namecolumn.setText("имя");
        secnamecolumn.setText("отчество");
        surcolumn.setText("фамилия");
        cityinform.getColumns().addAll(pascolumn);
        pascolumn.getColumns().addAll(num, namecolumn,surcolumn,secnamecolumn );

        cityinform.setMaxHeight(300);
        cityinform.setMaxWidth(300);
        cityinform.setTranslateX(this.location.x);
        cityinform.setTranslateY(this.location.y);
    }
}//класс город

//часы
class TrainClock implements  Runnable{

    static MoveController moveController;
    GregorianCalendar  apptime;
    static double durationofoneminute;
    DateFormat dateFormat;
    static  boolean isActive;
    public static  boolean paused;
    OracleSQL oracleSQL;
    static Object pauseLock = new Object();
    SimpleStringProperty apptimeproperty;
    private ObjectPropertyBase<GregorianCalendar> obsr=new SimpleObjectProperty<GregorianCalendar>();
//добавление слушателя на каждое изменение времени
    {
        apptime=new GregorianCalendar(2019,0,1,0,0);//переделать на java.time
        obsr.addListener(new ChangeListener<GregorianCalendar>() {
            @Override
            public void changed(ObservableValue<? extends GregorianCalendar> observableValue, GregorianCalendar gregorianCalendar, GregorianCalendar t1) {
                try {
                    oracleSQL.shedule.startcurrenttrains(apptime);
                } catch (ParseException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public TrainClock(MoveController movecontroller, OracleSQL oracleSQL)throws  Exception{
        durationofoneminute = 100;
        this.moveController = movecontroller;
        movecontroller.trainClock=this;
        this.oracleSQL=oracleSQL;
        dateFormat = new SimpleDateFormat("'Day'DD'Time'HH:mm", Locale.ENGLISH);
        paused=false;
        apptimeproperty=new SimpleStringProperty();
        synchronized (moveController.shedulelockcondition1){
            moveController.shedulelockcondition1.notify();
        }
    }
//постановка часов на паузу при нажатии кнопки
    public void run(){
        isActive = true;
        while (isActive){
            synchronized (pauseLock){
                if(paused==true){
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }}

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    moveController.getClockLabel().setText(dateFormat.format(apptime.getTime()));
                }
            })  ;
            try {
                for (int i=0; i<5;i++){
                    Thread.sleep((long) durationofoneminute);
                    apptime.add(Calendar.MINUTE,1);}
                apptimeproperty.setValue(dateFormat.format(apptime.getTime()));
            } catch (InterruptedException e) { }

            obsr.set(new GregorianCalendar());
        }
    }
}//класс про часы


public class Main extends Application {

    public Main() throws SQLException, ClassNotFoundException {
    }

    FXMLLoader loader;
    Controller controller;
    public void start(Stage primaryStage) throws Exception {

      /*  loader=new FXMLLoader(getClass().getResource("buy_ticket2.fxml"));
*//*
        loader =new FXMLLoader();
*//*

        try {
            Parent root = loader.load();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }*/

        loader= new FXMLLoader(getClass().getResource("authtrain.fxml"));

        Parent content = loader.load();
        Scene scene = new Scene(content);
        controller=loader.getController();
        primaryStage.setTitle("Авторизация");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);////

        controller.setStage(primaryStage);
        controller.setLoader(loader);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}


