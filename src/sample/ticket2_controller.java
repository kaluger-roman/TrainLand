package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ticket2_controller {

    @FXML
    private CheckBox man_check;

    @FXML
    private CheckBox woman_check;

    @FXML
    private Label surname_label;

    @FXML
    private TextField fathername_ticket_field;

    @FXML
    private Tooltip fathername_tooltip;

    @FXML
    private Label sex_label;

    @FXML
    private Button buy_button;

    @FXML
    private TextField name_ticket_field;

    @FXML
    private Label fathername_label;

    @FXML
    private Label name_label;

    @FXML
    private TextField document_ticket_field;

    @FXML
    public Label price_ticket_label;

    @FXML
    private Label document_label;

    @FXML
    private Tooltip document_tooltip;

    @FXML
    private TextField surname_ticket_field;

    @FXML
    private Tooltip name_tooltip;

    @FXML
    private AnchorPane back_page;

    @FXML
    private Tooltip email_tooltip;

    @FXML
    private Label email_label;

    @FXML
    private Tooltip surname_tooltip;

    @FXML
    private TextField email_ticket_field;

    @FXML
    private ImageView back_t1;

    @FXML
    private ImageView exitbutton;
    FXMLLoader back_to_auh;
    Controller auth;
    Stage primaryStage;
    Parent t1_root, authroot;
    ticket1_controller tick1contrl;
    int idzatichka;

    public void setStage(Stage primarystage){
        this.primaryStage=primarystage;
    }
    @FXML
    void initialize() {

    }
    @FXML
    public void zatichka(){
        price_ticket_label.setText(String.valueOf(tick1contrl.allgoes.stream().map(i->i.getprice()).reduce((x, y)->x+y)));
        email_ticket_field.setFocusTraversable(false);
        fathername_ticket_field.setFocusTraversable(false);
        document_ticket_field.setFocusTraversable(false);
        Label [] labels = new Label[]{email_label,name_label,fathername_label,document_label,surname_label,sex_label};
        TextField [] textFields = new TextField[]{email_ticket_field, surname_ticket_field, name_ticket_field, fathername_ticket_field, document_ticket_field};
        Tooltip [] tooltips = new Tooltip[]{email_tooltip,surname_tooltip,name_tooltip,fathername_tooltip,document_tooltip};
        for (int i = 0; i<tooltips.length;i++) {tooltips[i].setShowDelay(Duration.seconds(0));tooltips[i].setShowDuration(Duration.seconds(20));}

        buy_button.setOnAction(e ->{
            String tick_emailText = email_ticket_field.getText().trim();
            String tick_surnameText = surname_ticket_field.getText().trim();
            String tick_nameText = name_ticket_field.getText().trim();
            String tick_fathernameText = fathername_ticket_field.getText().trim();
            String tick_documentText = document_ticket_field.getText().trim();
            Boolean ch = true;
            checkemail(tick_emailText);
            checksurname(tick_surnameText);
            checkname(tick_nameText);
            checkfathername(tick_fathernameText);
            checkdocument(tick_documentText);
            if (!man_check.isSelected() && !woman_check.isSelected()) {sex_label.setText("Выберите пожалуйста ваш пол");}
            if (man_check.isSelected() && woman_check.isSelected()) {sex_label.setText("Поставьте галочку в одном поле!");}
            if (!checkemail(tick_emailText)||!checksurname(tick_surnameText)||!checkname(tick_nameText)||!checkfathername(tick_fathernameText)||
                    !checkdocument(tick_documentText))
            {ch=false;}
            if (ch)
            {
                //idzatichka=((Controller)tick1contrl.back_to_auh.getController()).oracleSQL.allpassengers.size()+1;
                tick1contrl.curpas=new Passenger(0, tick_nameText,tick_surnameText,tick_fathernameText,"unknown",tick_documentText, "Main" );
                ((MoveController)((Controller)(tick1contrl.back_to_auh.getController())).loader.getController()).realPassengersTransition.idpasandallsendingshashmap.put(tick1contrl.curpas, tick1contrl.alltickets);
                for (int i = 0; i<textFields.length;i++) {textFields[i].setText("");}
                for (int i = 0; i<labels.length;i++) {labels[i].setText("");}
                man_check.setSelected(false); woman_check.setSelected(false);
                auth = back_to_auh.getController();
                Thread thread = new Thread(() -> {
                    double o = 1;
                    auth.buy_tick_label.setVisible(true);
                    auth.buy_tick_label.setOpacity(o);
                    try {
                        Thread.sleep(5000);
                        for (int i = 0; i < 100; i++) {
                            o = o - 0.01;
                            auth.buy_tick_label.setOpacity(o);
                            Thread.sleep(100);
                        }
                    }
                    catch (InterruptedException s) {}
                });
                thread.start();
                primaryStage.getScene().setRoot(authroot);
                primaryStage.setTitle("Авторизация");
                primaryStage.show();
            }
        });

        back_t1.setOnMouseClicked(e ->{//выбор  мест
            for (int i = 0; i<labels.length;i++) {labels[i].setText("");}
            for (int i = 0; i<textFields.length;i++) {textFields[i].setText("");}
            man_check.setSelected(false); woman_check.setSelected(false);
            primaryStage.getScene().setRoot(t1_root);
            primaryStage.setTitle("Покупка билета");
            primaryStage.show();
        });
    }
    private boolean checkemail(String emailText)
    {
        boolean ch=true;
        if (!emailText.matches("^[^а-яА-я]+@.+\\..+")) {email_label.setText("Неверный формат ввода e-mail");ch=false;}
        if (emailText.equals("")) {email_label.setText("Введите e-mail!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checksurname(String surnameText)
    {
        boolean ch=true;
        if (!surnameText.matches("^[A-Za-zа-яА-Я]+$")) {surname_label.setText("Неверный формат ввода!");ch=false;}
        if (surnameText.equals("")) {surname_label.setText("Введите вашу фамилию!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkname(String nameText)
    {
        boolean ch=true;
        if (!nameText.matches("^[A-Za-zа-яА-Я]+$")) {name_label.setText("Неверный формат ввода!");ch=false;}
        if (nameText.equals("")) {name_label.setText("Введите ваше имя!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkfathername(String fathernameText)
    {
        boolean ch=true;
        if (!fathernameText.matches("^[A-Za-zа-яА-Я]+$")&&fathernameText.length()>0) {fathername_label.setText("Неверный формат ввода!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkdocument(String documentText)
    {
        boolean ch=true;
        if (!documentText.matches("^[A-Za-zа-яА-Я1-9]+$")) {document_label.setText("Неверный формат ввода!");ch=false;}
        if (documentText.equals("")) {document_label.setText("Введите номер документа!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    @FXML
    public void exit(){
        Platform.exit();
        System.exit(0);
    }

}
