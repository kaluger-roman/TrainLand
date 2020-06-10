package sample;

import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;


public class RegistrationController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    @FXML
    private PasswordField password2_registration_field;

    @FXML
    private Label phone_label;

    @FXML
    private Label password_2_label;

    @FXML
    private Label surname_label;

    @FXML
    private Label password_label;

    @FXML
    private PasswordField password_registration_field;

    @FXML
    private Button firstregistration_button;

    @FXML
    private Label firstname_label;

    @FXML
    private TextField surname_registration_field;

    @FXML
    private TextField login_registration_field;

    @FXML
    private TextField captcha_registration_field;

    @FXML
    private TextField email_registration_field;

    @FXML
    private Label captcha_label;

    @FXML
    private Label email_label;

    @FXML
    private TextField name_registration_field;

    @FXML
    private TextField phone_registration_field;

    @FXML
    private CheckBox confidency_registration_check;

    @FXML
    private Label login_label;

    @FXML
    private Label confidency_label;

    @FXML
    public ImageView captcha_img;

    @FXML
    private ImageView back;

    @FXML
    private ImageView exitbutton;

    @FXML
    private Tooltip email_tooltip;

    @FXML
    private Tooltip login_tooltip;

    @FXML
    private Tooltip phone_tooltip;

    @FXML
    private Tooltip password_tooltip;

    @FXML
    private Tooltip firstname_tooltip;

    @FXML
    private Tooltip captcha_tooltip;

    @FXML
    private Tooltip surname_tooltip;

    @FXML
    private Tooltip password_2_tooltip;

    Stage primaryStage;
    String captcha;
    Parent authroot;
    OracleSQL RegoracleSQL;
    FXMLLoader back_to_auth;
    Controller auth;

    public void setStage1(Stage primarystage){
        this.primaryStage=primarystage;

    }

    @FXML
    void initialize() {

        email_registration_field.setFocusTraversable(false);
        login_registration_field.setFocusTraversable(false);
        password_registration_field.setFocusTraversable(false);
        Label [] labels = new Label[]{email_label,login_label,password_label,password_2_label,firstname_label,surname_label,phone_label,captcha_label,confidency_label};
        TextField [] textFields = new TextField[]{email_registration_field, login_registration_field, surname_registration_field, name_registration_field,
                captcha_registration_field, phone_registration_field};
        PasswordField [] passwordFields = new PasswordField[]{password_registration_field, password2_registration_field};
        Tooltip [] tooltips = new Tooltip[]{email_tooltip,login_tooltip,password_tooltip,password_2_tooltip,firstname_tooltip,surname_tooltip,captcha_tooltip,phone_tooltip};
        for (int i = 0; i<tooltips.length;i++) {tooltips[i].setShowDelay(Duration.seconds(0));tooltips[i].setShowDuration(Duration.seconds(20));}

        firstregistration_button.setOnAction(event ->
        {   String emailText = email_registration_field.getText().trim();
            String loginText = login_registration_field.getText().trim();
            String passwordText = password_registration_field.getText().trim();
            String password2Text = password2_registration_field.getText().trim();
            String nameText = name_registration_field.getText().trim();
            String surnameText = surname_registration_field.getText().trim();
            String phoneText = phone_registration_field.getText().trim();
            String captchaText = captcha_registration_field.getText().trim();
            for (int i = 0; i<labels.length;i++) {labels[i].setText("");}
            Boolean ch = true;

            checkemail(emailText);
            checklogin(loginText);
            try {
                check_existence_login(loginText);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            checkpassword(passwordText);
            checkpassword2(password2Text,passwordText);
            checkname(nameText);
            checksurname(surnameText);
            checkphone(phoneText);
            checkconfidency();
            checkcaptcha(captchaText, captcha);
            if (!checkemail(emailText)||!checklogin(loginText)||!checkpassword(passwordText)||!checkpassword2(password2Text,passwordText)||
                    !checkname(nameText)||!checksurname(surnameText)||!checkphone(phoneText)||!checkconfidency()||!checkcaptcha(captchaText, captcha))
            {ch=false;}

            if (ch==true)
            {
                try {sign_up_new_user(loginText, passwordText);}
                catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i<textFields.length;i++) {textFields[i].setText("");}
                for (int i = 0; i<passwordFields.length;i++) {passwordFields[i].setText("");}
                confidency_registration_check.setSelected(false);
                auth=back_to_auth.getController();
                Thread thread = new Thread(() -> {
                    double o = 1;
                    auth.reg_completed_label.setVisible(true);
                    auth.reg_completed_label.setOpacity(o);
                    try {
                        Thread.sleep(5000);
                        for (int i = 0; i < 100; i++) {
                            o = o - 0.01;
                            auth.reg_completed_label.setOpacity(o);
                            Thread.sleep(100);
                        }
                    }
                    catch (InterruptedException e) {}
                });
                thread.start();

                primaryStage.getScene().setRoot(authroot);
                primaryStage.setTitle("Авторизация");
                primaryStage.show();
            };
        });

        back.setOnMouseClicked(e ->{//возвращение к странице авторизации
            for (int i = 0; i<labels.length;i++) {labels[i].setText("");}
            for (int i = 0; i<textFields.length;i++) {textFields[i].setText("");}
            for (int i = 0; i<passwordFields.length;i++) {passwordFields[i].setText("");}
            confidency_registration_check.setSelected(false);
            primaryStage.getScene().setRoot(authroot);
            primaryStage.setTitle("Авторизация");
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

    private boolean checklogin(String loginText)
    {
        boolean ch=true;
        if (!loginText.matches("^[a-zA-Z]+([-_]?[a-zA-Z0-9]+)+$")) {login_label.setText("Неверный формат ввода логина");ch=false;}
        if (loginText.length()<6 && loginText.length()>0) {login_label.setText("Слишком короткий логин!");ch=false;}
        if (loginText.equals("")) {login_label.setText("Введите логин!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkpassword(String passwordText)
    {
        boolean ch=true;
        if (!passwordText.matches("(?=.*[0-9])(?=.*[-_?.!@#$%^&*])(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z-_?.!@#$%^&*]+")) {password_label.setText("Неверный формат ввода пароля");ch=false;}
        if (passwordText.length()<9 && passwordText.length()>0) {password_label.setText("Слишком короткий пароль!");ch=false;}
        if (passwordText.equals("")) {password_label.setText("Введите пароль!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkpassword2(String password2Text, String passwordText)
    {
        boolean ch=true;
        if (!password2Text.equals(passwordText)) {password_2_label.setText("Не совпадают введенные пароли!");ch=false;}
        if (password2Text.equals("")) {password_2_label.setText("Повторите пароль!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkname(String nameText)
    {
        boolean ch=true;
        if (!nameText.matches("^[A-Za-zа-яА-Я]+$")) {firstname_label.setText("Неверный формат ввода!");ch=false;}
        if (nameText.equals("")) {firstname_label.setText("Введите ваше имя!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checksurname(String surnameText)
    {
        boolean ch=true;
        if (!surnameText.matches("^[A-Za-zа-яА-Я]+$")) {surname_label.setText("Неверный формат ввода!");ch=false;}
        if (surnameText.equals("")) {surname_label.setText("Введите вашу фамилию!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkphone(String phoneText)
    {
        boolean ch=true;
        if (!phoneText.matches("^[//+]7\\d{10}")&&phoneText.length()>0) {phone_label.setText("Неверный формат ввода!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkconfidency()
    {
        boolean ch=true;
        if (!confidency_registration_check.isSelected()) {confidency_label.setText("Необходимо дать согласие на обработку персональных данных!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private boolean checkcaptcha(String captchaText, String captcha)
    {
        boolean ch=true;
        if (!captchaText.equals(captcha)) {captcha_label.setText("Неправильно введено слово. Повторите ввод!");ch=false;}
        if (captchaText.equals("")) {captcha_label.setText("Введите слово, изображенное на картинке!");ch=false;}
        if (ch) {return true;} else {return false;}
    }

    private void sign_up_new_user(String login, String password) throws SQLException, ClassNotFoundException {
        User user = new User (login,md5Custom(password));
        RegoracleSQL.signUpUser(user);
    }//добавляет пользователя в бд

    public String md5Custom(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }//функция, хэширующая пароль

    private boolean check_existence_login(String loginText) throws SQLException {
        User user = new User(loginText);
        ResultSet result = RegoracleSQL.check_existence_login(user);

        int counter = 0;
        while (result.next())
        {
            counter++;
        }
        if (counter==1) { login_label.setText("Пользователь с таким логином уже существует!"); return false;}
        return true;
    }//проверяем свободен ли логин

}
