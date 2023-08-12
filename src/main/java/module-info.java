module com.saulpos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires fontawesomefx;
    requires jakarta.persistence;
    requires jfxtras.controls;
    requires jakarta.validation;
    requires com.saulpos.javafxcrudgenerator;
    requires org.hibernate.orm.core;
    requires org.apache.commons.codec;
    requires com.dlsc.formsfx;

    opens com.saulpos;
    exports com.saulpos;
    exports com.saulpos.presenter;

}