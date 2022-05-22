module com.msi.testlinkdemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires testlink.java.api;
    requires org.slf4j;
    requires reactor.core;
    requires org.reactivestreams;

    opens com.msi.testlinkdemo to javafx.fxml;
    exports com.msi.testlinkdemo;
}