package sample;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import sample.animations.Shake;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField login_field;

    @FXML
    private Button log_in_button;

    @FXML
    private Button Registration_button;

    @FXML
    private PasswordField password_field;

    @FXML
    private Label login_error_label;

    @FXML
    private Label password_error_label;

    @FXML
    public Label reg_completed_label;

    @FXML
    public Label buy_tick_label;

    @FXML
    private ImageView exitbutton;

    Stage primaryStage;
    Consumer<Stage> setTitle = (stage) -> stage.setTitle("TrainLand");
    TrainClock trainClock;
    FXMLLoader loader, loader1, thisloader, loader2;
    Parent root, root1, root2;
    Thread clockthreaf;
    OracleSQL oracleSQL;
    RegistrationController Reg_Controller;
    ticket1_controller Buy1_controller;

    public void setStage(Stage primarystage){
        this.primaryStage=primarystage;
    }

    public void setLoader(FXMLLoader loader){
        this.thisloader=loader;

    }

    @FXML
    void initialize() {
        Random rand = new Random();
        Image i1 = new javafx.scene.image.Image("sample/assets/captcha1.png");
        Image i2 = new javafx.scene.image.Image("sample/assets/captcha2.png");
        Image i3 = new javafx.scene.image.Image("sample/assets/captcha3.png");
        Image i4 = new javafx.scene.image.Image("sample/assets/captcha4.png");
        Image i5 = new javafx.scene.image.Image("sample/assets/captcha5.png");
        Image i6 = new javafx.scene.image.Image("sample/assets/captcha6.png");
        Image i7 = new javafx.scene.image.Image("sample/assets/captcha7.png");
        Image i8 = new javafx.scene.image.Image("sample/assets/captcha8.png");
        Image i9 = new Image("sample/assets/captcha9.png");
        Shake loginanim = new Shake(login_field);
        Shake passwordanim = new Shake(password_field);

        loader=new FXMLLoader(getClass().getResource("sample.fxml"));
        try {
            root = loader.load();

        }
        catch (Exception ex){
        }

        loader1=new FXMLLoader(getClass().getResource("registration.fxml"));
        try {
            root1 = loader1.load();
        }
        catch (Exception ex){

        }

        loader2=new FXMLLoader(getClass().getResource("buy_ticket1.fxml"));
        try {
            root2 = loader2.load();
        }
        catch (Exception ex){

        }

        log_in_button.setOnAction(event -> {//нажатие на кнопку авторизация
            Reg_Controller=loader1.getController();
            clearlabels();
            String loginText = login_field.getText().trim();
            String passwordText = password_field.getText().trim();
            if (!loginText.equals("") && !passwordText.equals("")) {
                try {
                    log_in_user(loginText, Reg_Controller.md5Custom(passwordText));
                    if (log_in_user(loginText, Reg_Controller.md5Custom(passwordText))==2)//вход как админ
                    {
                        setTitle.accept(primaryStage);
                        primaryStage.getScene().setRoot(root);
                        ( (MoveController)loader.getController()).authroot=root2;
                        ( (MoveController)loader.getController()).primarystage=primaryStage;
                        primaryStage.show();
                    }
                    if (log_in_user(loginText, Reg_Controller.md5Custom(passwordText))==1)//вход как обычный пользователь
                    {
                        Buy1_controller = loader2.getController();//после этого запускается initialize
                        Buy1_controller.setStage(primaryStage);
                        Buy1_controller.back_to_auh=thisloader;
                        Buy1_controller.authroot=primaryStage.getScene().getRoot();
                        for (int i=0; i<10; i++)
                        {
                            Buy1_controller.labels_place.set(i,null);
                        }
                        for (int i=0; i<10; i++)
                        {
                            Buy1_controller.chairs.set(i,null);
                        }
                        for (int i=0; i<10; i++)
                        {
                            Buy1_controller.labels_vagon.set(i,null);
                        }
                        Buy1_controller.pag_tick.setVisible(false);
                        Buy1_controller.data_button.setVisible(false);
                        primaryStage.getScene().setRoot(root2);
                        primaryStage.setTitle("Покупка билета");
                        primaryStage.show();
                        login_field.setText("");
                        password_field.setText("");
                    }
                    if (log_in_user(loginText, Reg_Controller.md5Custom(passwordText))==0)
                    {
                        loginanim.playAnim();
                        passwordanim.playAnim();
                        login_error_label.setText("Неверный логин или пароль!");
                        password_error_label.setText("Неверный логин или пароль!");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                loginanim.playAnim();
                passwordanim.playAnim();
                if (loginText.equals("")) {login_error_label.setText("Введите логин!");}
                if (passwordText.equals("")) {password_error_label.setText("Введите пароль!");}
            }

        });

        try {
            oracleSQL = new OracleSQL(loader.getController());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {//создание и запуск часов
            trainClock = new TrainClock(loader.getController(), oracleSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TrainClock.isActive) {
            clockthreaf = new Thread(trainClock);
            clockthreaf.start();
        }


        Registration_button.setOnAction(event -> {//нажатие на кнопку регистрация
            login_field.setText("");
            password_field.setText("");
            clearlabels();
            int r = rand.nextInt(9);
            Reg_Controller=loader1.getController();
            set_different_captcha(Reg_Controller, r, i1, i2, i3, i4, i5, i6, i7, i8, i9);
            Reg_Controller.RegoracleSQL=oracleSQL;
            Reg_Controller.back_to_auth=thisloader;
            Reg_Controller.authroot=primaryStage.getScene().getRoot();
            Reg_Controller.setStage1(primaryStage);
            primaryStage.getScene().setRoot(root1);
            primaryStage.setTitle("Регистрация");
            primaryStage.show();

        });

    }
    public void set_different_captcha(RegistrationController Reg_Controller, int r, Image i1, Image i2, Image i3, Image i4,
    Image i5, Image i6, Image i7, Image i8, Image i9)//при открытии страницы регистрация каждый раз установка новой капчи
    {
        switch (r)
        {
            case 0: Reg_Controller.captcha_img.setImage(i1);
                Reg_Controller.captcha="moiates";
                break;

            case 1:  Reg_Controller.captcha_img.setImage(i2);
                Reg_Controller.captcha="Polyconveb";
                break;

            case 2:  Reg_Controller.captcha_img.setImage(i3);
                Reg_Controller.captcha="captio";
                break;

            case 3:  Reg_Controller.captcha_img.setImage(i4);
                Reg_Controller.captcha="plings";
                break;

            case 4:  Reg_Controller.captcha_img.setImage(i5);
                Reg_Controller.captcha="khaverst";
                break;

            case 5:  Reg_Controller.captcha_img.setImage(i6);
                Reg_Controller.captcha="pexpopti";
                break;

            case 6:  Reg_Controller.captcha_img.setImage(i7);
                Reg_Controller.captcha="duffledest";
                break;

            case 7:  Reg_Controller.captcha_img.setImage(i8);
                Reg_Controller.captcha="prabi";
                break;

            case 8:  Reg_Controller.captcha_img.setImage(i9);
                Reg_Controller.captcha="tibra";
                break;
        }

    }
    private void clearlabels ()
    {
        login_error_label.setText("");
        password_error_label.setText("");
    }
    private int log_in_user(String loginText, String passwordText) throws SQLException {
        User user = new User(loginText, passwordText);
        ResultSet result = oracleSQL.getUser(user);

        int counter=0;
        if (!loginText.equals("admin1"))
       {
            while (result.next()) {
                counter++;
            }
            if (counter == 1) {
                return 1;
            }
       }
        if (loginText.equals("admin1"))
        {
            while (result.next()) {
                counter++;
            }
            if (counter == 1) {
                return 2;
            }
        }
        return 0;
        }

    @FXML
    public void exit(){
        Platform.exit();
        System.exit(0);
    }
}
