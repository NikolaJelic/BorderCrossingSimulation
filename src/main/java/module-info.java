module com.nikola.bordercrossingsimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens com.nikola.bordercrossingsimulator to javafx.fxml;
    exports com.nikola.bordercrossingsimulator;

    opens com.nikola.bordercrossingsimulator.models  to javafx.fxml;
    exports com.nikola.bordercrossingsimulator.models;
    exports com.nikola.bordercrossingsimulator.models.vehicle;
    exports com.nikola.bordercrossingsimulator.models.passenger;
    exports com.nikola.bordercrossingsimulator.models.terminal;
    exports com.nikola.bordercrossingsimulator.models.transportable;


    opens com.nikola.bordercrossingsimulator.controllers to javafx.fxml;
    exports com.nikola.bordercrossingsimulator.controllers to javafx.fxml;
}