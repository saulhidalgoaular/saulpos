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
    requires org.hibernate.orm.core;
    requires org.apache.commons.codec;
    requires com.dlsc.formsfx;
    requires dynamicreports.core;
    requires jasperreports;
    requires com.saulpos.javafxcrudgenerator;

    exports com.saulpos;
    exports com.saulpos.model.bean;
    exports com.saulpos.presenter;
    opens com.saulpos to javafx.graphics, org.hibernate.orm.core, javafx.fxml;
    opens com.saulpos.presenter to javafx.fxml;
    opens com.saulpos.model.bean to org.hibernate.orm.core;
}