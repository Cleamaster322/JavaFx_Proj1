module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j.simple;
    requires org.xerial.sqlitejdbc;


    opens com.example.demo1 to javafx.fxml;
    exports com.example.demo1;
}