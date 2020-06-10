package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.CheckedOutputStream;
class SearchEvent extends Event{
    public SearchEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

   /* public SearchEvent(EventType<? extends MouseEvent> eventType, double v, double v1, double v2, double v3, MouseButton mouseButton, int i, boolean b, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7, boolean b8, boolean b9, PickResult pickResult) {
        super(eventType, v, v1, v2, v3, mouseButton, i, b, b1, b2, b3, b4, b5, b6, b7, b8, b9, pickResult);
    }*/
}
public class ticket1_controller {

    @FXML
    public Pagination pag_tick;

    @FXML
    private ResourceBundle resources;

    @FXML
    public ComboBox<String> from_place;

    @FXML
    public ComboBox<String> to_place;

    @FXML
    private Label place_warning;


    @FXML
    private ImageView exitbutton;

    @FXML
    private Label city_to_label;

    @FXML
    private Label city_from_label;

    @FXML
    private Button search_button;

    @FXML
    private ImageView back_to_auth;

    @FXML
    public Button data_button;

    @FXML
    private URL location;
    ReentrantLock mylock=new ReentrantLock();
    LinkedList<SheduleMember> allgoes;
    LinkedList<Ticket> alltickets;
    FXMLLoader loader;
    FXMLLoader  back_to_auh;
    Parent root;
    Parent authroot;
    Stage primaryStage;
    ticket2_controller Buy2_controller;
    int x1 = 270, x2 = 500;
    Chair pred_chair, help_chair;
    ArrayList<Label> labels_place = new ArrayList<Label>();
    ArrayList<Label> labels_vagon = new ArrayList<Label>();
    ArrayList<Integer> chairs = new ArrayList<>();
    Passenger curpas;
    EventType<SearchEvent> searchEventEventType=new EventType<>();
    DateFormat dateFormat=new SimpleDateFormat("HH:mm", Locale.ENGLISH);

    static int[][] matrix_int(ArrayList<SheduleMember> commonshedule) throws ParseException {
        int[][] matrix = new int[commonshedule.size()][commonshedule.size()];
        DateFormat dateformat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        for (int i = 0; i < commonshedule.size(); i++)
        {
            SheduleMember shed = commonshedule.get(i);
            matrix[shed.fromcity.id - 1][shed.toCity.id - 1] = (int)(dateformat.parse(shed.timeofarriving).getTime() - dateformat.parse(shed.timeofstart).getTime());
        }
        return matrix;
    }

    static int[] Dejkstra(int[][] matrix, int begin, int kon)
    {
        int[] d = new int[matrix.length];
        int[] v = new int[matrix.length];
        int begin_index = begin - 1;
        int temp, minindex, min;

        for (int i = 0; i<matrix.length; i++) {
            d[i] = 1000000000;
            v[i] = 1;
        }
        d[begin_index] = 0;

        do{
            minindex = 1000000000;
            min = 1000000000;
            for (int i = 0; i < matrix.length; i++)
            {
                if ((v[i] == 1) && (d[i] < min))
                {
                    min = d[i];
                    minindex = i;
                }
            }
            if (minindex != 1000000000)
            {
                for (int i = 0; i < matrix.length; i++)
                {
                    if (matrix[minindex][i] > 0)
                    {
                        temp = min + matrix[minindex][i];
                        if (temp < d[i])
                            d[i] = temp;
                    }
                }
                v[minindex] = 0;
            }
        } while (minindex < 1000000000);

        int[] ver = new int[matrix.length];
        int end = kon - 1;
        ver[0] = end + 1;
        int k = 1;
        int wieght = d[end];

        while (end != begin_index) {
            for (int i = 0; i < matrix.length; i++)
                if (matrix[i][end] != 0)
                {
                    temp = wieght - matrix[i][end];
                    if (temp == d[i])
                    {
                        wieght = temp;
                        end = i;
                        ver[k] = i + 1;
                        k++;
                    }
                }
        }
        return ver;
    }

    public void setStage(Stage primarystage){
        this.primaryStage=primarystage;
    }

    public void pose_Label(Label lbl, int x, int y)
    {
        lbl.setLayoutX(x);
        lbl.setLayoutY(y);
        lbl.setFont(new Font("Britannic Bold",20));
    }

    @FXML
    void initialize() throws ParseException, IOException {

        allgoes=new LinkedList<>();
        alltickets=new LinkedList<>();
        for (int i=0; i<10; i++)
        {
        labels_place.add(i,null);
        }

        for (int i=0; i<10; i++)
        {
            labels_vagon.add(i,null);
        }

        for (int i=0; i<10; i++)
        {
            chairs.add(i,null);
        }


        from_place.setFocusTraversable(false);
        to_place.setFocusTraversable(false);
        ObservableList<String> cities_from = FXCollections.observableArrayList(  "Актау", "Актюбинск" ,  "Аральск" , "Астана" ,
                "Атасу" , "Атбасар" , "Атырау" ,  "Бейнеу" , "Ерментау" , "Жалтыр" ,
                "Иргиз" , "Кандыагаш" , "Караганда" , "Кокшетау" , "Костанай" , "Кушмурун" ,
                "Петропавловск"  , "Тургай"  ,
                "Урицкий" );
        ObservableList<String> cities_to;
        cities_to = cities_from;
        from_place.setItems(cities_from);
        to_place.setItems(cities_to);
        from_place.setOnAction(e ->{
            city_to_label.setText("");
            city_from_label.setText("");
            if (to_place.getValue()==null) { city_to_label.setText("Выберите пункт назначения!");}
            if (from_place.getValue().equals(to_place.getValue())) {city_from_label.setText("Пункты отправления и назначения совпадают!");
            city_to_label.setText("Пункты отправления и назначения совпадают!");}
            //search_button.setDisable(false);//---------------------
        });
        to_place.setOnAction(e ->{
            city_to_label.setText("");
            city_from_label.setText("");
            if (from_place.getValue()==null) {city_from_label.setText("Выберите пункт отправления!");}
            if (to_place.getValue().equals(from_place.getValue())) {city_to_label.setText("Пункты отправления и назначения совпадают!");
                city_from_label.setText("Пункты отправления и назначения совпадают!");}
            //search_button.setDisable(false);
        });
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        search_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                pag_tick.setCurrentPageIndex(0);
                Event.fireEvent(search_button, new SearchEvent(searchEventEventType));
                Event.fireEvent(search_button, new SearchEvent(searchEventEventType));

            }
        });

        search_button.addEventHandler(searchEventEventType, new EventHandler<SearchEvent>() {
            @Override
            public void handle(SearchEvent searchEvent) {
                boolean ch=true;
                int cityfromid, citytoid;
                OracleSQL oracleSQL;
                if (!city_from_label.getText().equals("")||!city_to_label.getText().equals("")) {ch=false;}
                if (from_place.getValue()==null) {city_from_label.setText("Выберите пункт отправления!");ch=false;}
                if (to_place.getValue()==null) { city_to_label.setText("Выберите пункт назначения!");ch=false;}

                if (ch) {
                    oracleSQL=  ((Controller)back_to_auh.getController()).oracleSQL;
                    cityfromid= oracleSQL.allcities.values().stream().filter(i->i.name.equals(from_place.getValue())).findFirst().get().id;
                    citytoid= oracleSQL.allcities.values().stream().filter(i->i.name.equals(to_place.getValue())).findFirst().get().id;

                    try {
                        int[] citymas= Arrays.stream(Dejkstra(matrix_int(oracleSQL.shedule.commonshedule),cityfromid,citytoid)).filter(i->i>0).toArray();
                        for (int i = 0; i < citymas.length / 2; i++) {
                            int tmp = citymas[i];
                            citymas[i] = citymas[citymas.length - i - 1];
                            citymas[citymas.length - i - 1] = tmp;
                        }
                        allgoes.clear();
                        for (int i=0;i<citymas.length;i++){
                            int finalI = i;
                            try {

                                allgoes.add(oracleSQL.shedule.commonshedule.stream().filter(j -> ((j.fromcity.id == citymas[finalI]) && (j.toCity.id == citymas[finalI + 1]))).findFirst().get());
                            }
                            catch (Exception ex){

                            }
                        }


                    }
                    catch (ParseException ex) {
                        ex.printStackTrace();
                    }


                    data_button.setVisible(true);
                    data_button.setLayoutX(801);
                    pag_tick.setVisible(true);
                    pag_tick.setCurrentPageIndex(0);
                    pag_tick.setMaxPageIndicatorCount(allgoes.size());
                    pag_tick.setPageCount(allgoes.size());

                    pag_tick.setPageFactory(new Callback<Integer, Node>() {

                        @Override
                        public Node call(Integer integer) {
                            AnchorPane vagon = new AnchorPane();
                            ComboBox<String> vagonsComboBox = new ComboBox<String>();
                            for (int i=0;i<allgoes.get(integer).train.capacity/50;i++) {
                                vagonsComboBox.getItems().add(String.valueOf(i+1));
                            }
                            vagonsComboBox.setValue("Вагон:");
                            vagonsComboBox.setLayoutX(x1);
                            vagonsComboBox.setStyle("-fx-font-size:20");

                            Label vagon_num = new Label("Вагон:");
                            pose_Label(vagon_num,x1,60);
                            Label vagon_num_value = new Label("выбери");
                            pose_Label(vagon_num_value,x2,60);
                            if (!(labels_vagon.get(integer) ==null)) vagon_num_value.setText(labels_vagon.get(integer).getText());

                            Label place = new Label("Место:");
                            pose_Label(place,x1,90);
                            Label place_value = new Label("выбери");
                            pose_Label(place_value,x2,90);
                            if (!(labels_place.get(integer) ==null)) place_value.setText(labels_place.get(integer).getText());

                            Label way = new Label("Маршрут:");
                            pose_Label(way,x1,150);
                            Label way_value = new Label(allgoes.get(integer).fromcity.name+"--"+allgoes.get(integer).toCity.name);
                            pose_Label(way_value,x2,150);

                            Label lv_time = new Label("Время отправления:");
                            pose_Label(lv_time,x1,180);
                            Label lv_time_value = new Label(allgoes.get(integer).timeofstart);
                            pose_Label(lv_time_value,x2,180);

                            Label arr_time = new Label("Время прибытия:");
                            pose_Label(arr_time,x1,210);
                            Label arr_time_value = new Label(allgoes.get(integer).timeofarriving);
                            pose_Label(arr_time_value,x2,210);

                            Label way_time = new Label("Время пути:");
                            pose_Label(way_time,x1,240);
                            Label way_time_value = null;
                            try {
                                way_time_value = new Label(Long.toString((dateFormat.parse(allgoes.get(integer).timeofarriving).getTime()-dateFormat.parse(allgoes.get(integer).timeofstart).getTime())/1000/60/60)+" часов " + Long.toString((dateFormat.parse(allgoes.get(integer).timeofarriving).getTime()-dateFormat.parse(allgoes.get(integer).timeofstart).getTime())/1000/60%60)+" минут");
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                            pose_Label(way_time_value,x2,240);

                            Label typ_train = new Label("Тип поезда:");
                            pose_Label(typ_train,x1,270);
                            Label typ_train_value = new Label(allgoes.get(integer).train.typeoftrain);
                            pose_Label(typ_train_value,x2,270);

                            Label way_price = new Label("Цена:");
                            pose_Label(way_price,x1,300);
                            Label way_price_value = new Label(allgoes.get(integer).getprice()+" Тенге");
                            pose_Label(way_price_value,x2,300);

                            vagon.getChildren().addAll(vagonsComboBox, way, way_value, lv_time, lv_time_value, arr_time, arr_time_value, way_time,
                                    way_time_value, typ_train,typ_train_value, way_price, way_price_value, vagon_num, vagon_num_value, place, place_value);

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
                                    chair.vagon = pag_tick.getCurrentPageIndex() + 1;
                                    chair.rec.setFill(Color.GREEN);
                                    if (chairs.get(integer)!=null&&chair.num==chairs.get(integer)) {chair.rec.setFill(Color.RED);help_chair=chair;}

                                    vagon.getChildren().addAll(chair.rec);

                                    chair.rec.setAccessibleRole(AccessibleRole.BUTTON);
                                    chair.rec.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                        @Override
                                        public void handle(MouseEvent mouseEvent) {
                                            if (!(labels_vagon.get(integer)==null)) {
                                                if (help_chair != null) help_chair.rec.setFill(Color.GREEN);
                                                if (pred_chair != null) pred_chair.rec.setFill(Color.GREEN);
                                                pred_chair = chair;
                                                chair.rec.setFill(Color.RED);
                                                place_value.setText("" + chair.num);
                                                labels_place.set(integer, place_value);
                                                chairs.set(integer, chair.num);
                                            }
                                            else {place_value.setText("Выберите вагон!");}

                                        }
                                    });

                                }
                            }
                            vagonsComboBox.setOnAction(e ->{
                                vagon_num_value.setText(""+vagonsComboBox.getValue());
                                labels_vagon.set(integer,vagon_num_value);
                                if (pred_chair!=null) pred_chair.rec.setFill(Color.GREEN);
                                place_value.setText("");
                                labels_place.set(integer,null);
                            });
                            return vagon;
                        }

                    });
                    //search_button.setDisable(true);
                }
            }

        }
        );
        InputStream stream = getClass().getResourceAsStream("dbuy_ticket2.fxml");

/*
        loader=new FXMLLoader(getClass().getResource("/sample/buy_ticket2.fxml"));
*/

        loader =new FXMLLoader();
        try {

            root = loader.load(stream);
            ((ticket2_controller) loader.getController()).tick1contrl=this;
            ((ticket2_controller) loader.getController()).zatichka();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        stream.close();
        data_button.setOnAction(e ->{
            ArrayList<Label> buf=new ArrayList<>();

            for (int i=0; i<allgoes.size();i++){
                buf.add(labels_place.get(i));
            }
            labels_place.clear();
            labels_place.addAll(buf);
            buf.clear();

            for (int i=0; i<allgoes.size();i++){
                buf.add(labels_vagon.get(i));
            }
            labels_vagon.clear();
            labels_vagon.addAll(buf);
            buf.clear();

            place_warning.setVisible(false);
            if (labels_place.contains(null)||labels_vagon.contains(null)) place_warning.setVisible(true); else {
                for (int i=0; i<allgoes.size();i++){
                    alltickets.add(new Ticket(allgoes.get(i),Integer.valueOf(labels_place.get(i).getText()),Integer.valueOf(labels_vagon.get(i).getText()) ));
                }

                Buy2_controller = loader.getController();
                Buy2_controller.setStage(primaryStage);
                Buy2_controller.t1_root=primaryStage.getScene().getRoot();
                Buy2_controller.back_to_auh=back_to_auh;
                Buy2_controller.authroot=authroot;
                Buy2_controller.tick1contrl=this;

                Buy2_controller.price_ticket_label.setText("СТОИМОСТЬ ПОЕЗДКИ "+String.valueOf(this.allgoes.stream().map(i->i.getprice()).reduce((x, y)->x+y).get())+" ТЕНГЕ");

                primaryStage.getScene().setRoot(root);
                primaryStage.setTitle("Ввод данных");
                primaryStage.show();
               /* System.out.println(labels_place.size());//тест
                System.out.println(labels_place.get(0).getText());//тест
                System.out.println(labels_place.get(1).getText());//тест
                System.out.println(labels_place.get(2).getText());//тест
                System.out.println(labels_vagon.size());//тест
                System.out.println(labels_vagon.get(0).getText());//тест
                System.out.println(labels_vagon.get(1).getText());//тест
                System.out.println(labels_vagon.get(2).getText());//тест
                System.out.println(chairs.get(0));*/
            }
            });

        }
    @FXML
    public void exit(){
        Platform.exit();
        System.exit(0);
    }

    public void back(MouseEvent mouseEvent) {
        city_from_label.setText("");
        city_to_label.setText("");
        primaryStage.getScene().setRoot(authroot);
        primaryStage.show();
    }
}
