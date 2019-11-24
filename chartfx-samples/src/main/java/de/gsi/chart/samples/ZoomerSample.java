package de.gsi.chart.samples;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.AxisMode;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.event.AddedDataEvent;
import de.gsi.dataset.spi.DoubleErrorDataSet;
import de.gsi.dataset.testdata.spi.RandomDataGenerator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * @author rstein
 */
public class ZoomerSample extends Application {
    private static final int PREF_WIDTH = 600;
    private static final int PREF_HEIGHT = 300;
    private static final int N_SAMPLES = 1000000; // default: 1000000

    @Override
    public void start(final Stage primaryStage) {

        final FlowPane root = new FlowPane();
        root.setAlignment(Pos.CENTER);

        DataSet testDataSet = generateData();

        Label label = new Label("left-click-hold-drag for zooming. middle-button for panning.\n"
                + "Tip: drag horizontally/vertically/diagonally for testing; try to select the outlier");
        label.setFont(Font.font(20));
        label.setAlignment(Pos.CENTER);
        label.setContentDisplay(ContentDisplay.CENTER);
        label.setPrefWidth(2.0 * PREF_WIDTH);

        // chart with default zoom
        final Chart chart1 = getTestChart("default zoom", testDataSet);
        chart1.getPlugins().add(new Zoomer());

        // chart with auto xy zoom
        final Chart chart2 = getTestChart("auto xy zoom", testDataSet);
        final Zoomer zoomer2 = new Zoomer();
        zoomer2.setAutoZoomEnabled(true);
        chart2.getPlugins().add(zoomer2);

        // chart with x-only zoom
        final Chart chart3 = getTestChart("x-only zoom", testDataSet);
        chart3.getPlugins().add(new Zoomer(AxisMode.X));

        // chart with x-only zoom
        final Chart chart4 = getTestChart("y-only zoom", testDataSet);
        chart4.getPlugins().add(new Zoomer(AxisMode.Y));

        root.getChildren().addAll(chart1, chart2, chart3, chart4, label);

        primaryStage.setTitle(this.getClass().getSimpleName());
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(evt -> Platform.exit());
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    private static Chart getTestChart(final String title, final DataSet testDataSet) {
        final Chart chart = new XYChart();
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.getDatasets().add(testDataSet);
        chart.setPrefSize(PREF_WIDTH, PREF_HEIGHT);

        return chart;
    }

    private static DataSet generateData() {
        DoubleErrorDataSet dataSet = new DoubleErrorDataSet("test data");

        dataSet.lock().writeLockGuard(() -> {
            // auto notification is suppressed by write lock guard
            dataSet.clearData();
            double oldY = 0;

            for (int n = 0; n < N_SAMPLES; n++) {
                final double x = n;
                oldY += RandomDataGenerator.random() - 0.5;
                final double y = oldY + (n == 500000 ? 500.0 : 0) /* + ((x>1e4 && x <2e4) ? Double.NaN: 0.0) */;
                final double ex = 0.1;
                final double ey = 10;
                dataSet.add(x, y, ex, ey);

                if (n == 500000) { // NOPMD this point is really special ;-)
                    dataSet.getDataLabelMap().put(n, "special outlier");
                }
            }

            dataSet.autoNotification().set(true);
        });
        // need to issue a separate update notification
        // N.B. for performance reasons we let only 'dataSet' fire an event, since we modified both
        // dataSetNoErrors will be updated alongside dataSet.
        dataSet.fireInvalidated(new AddedDataEvent(dataSet));

        return dataSet;
    }
}
