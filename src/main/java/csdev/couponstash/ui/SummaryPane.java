package csdev.couponstash.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import csdev.couponstash.commons.moneysymbol.MoneySymbol;
import csdev.couponstash.commons.util.DateUtil;
import csdev.couponstash.model.coupon.Coupon;
import csdev.couponstash.model.coupon.savings.DateSavingsSumMap;
import csdev.couponstash.model.coupon.savings.MonetaryAmount;
import csdev.couponstash.model.coupon.savings.PureMonetarySavings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Savings summary of CouponStash.
 */
public class SummaryPane extends UiPart<Region> {
    private static final String FXML = "SummaryPane.fxml";
    // message to be shown before the numerical amount saved
    private static final String SAVED_TOTAL_PRE_MESSAGE = "You saved a total of ";
    // message to be shown before the list of saveables
    private static final String SAVEABLES_PRE_MESSAGE = "And these saveables too!";
    // controls font size of number amount
    private static final int BASE_FONT_SIZE = 125;

    // Independent Ui parts residing in this Ui container
    private ObservableList<Coupon> allCoupons;
    private MoneySymbol moneySymbol;

    // Individual FXML components
    @FXML
    private StackPane baseNode;
    @FXML
    private Label savedText;
    @FXML
    private Label numericalAmount;
    @FXML
    private Label saveablesText;
    @FXML
    private BarChart<String, Number> savingsChart;
    @FXML
    private NumberAxis savingsAxis;
    @FXML
    private CategoryAxis daysAxis;
    @FXML
    private VBox allSaveables;

    // JavaFX location where bar graph labels are put
    ObservableList<Node> barGraphLabels = null;

    // Total amount to show in the summary tab
    private MonetaryAmount shownMonetaryAmount = new MonetaryAmount(0.0);

    /**
     * Constructor for a new SummaryPane. This is the
     * tab that holds the savings summary graph.
     *
     * @param allCoupons The ObservableList of all the Coupons
     *                   in Coupon Stash, whether hidden or shown.
     * @param moneySymbol The money symbol as set in user preferences.
     */
    public SummaryPane(ObservableList<Coupon> allCoupons, MoneySymbol moneySymbol) {
        super(FXML);
        this.allCoupons = allCoupons;
        this.moneySymbol = moneySymbol;
        savedText.setText(SummaryPane.SAVED_TOTAL_PRE_MESSAGE);
        saveablesText.setText(SummaryPane.SAVEABLES_PRE_MESSAGE);
    }

    /**
     * Updates the SummaryPane based on changes in total savings
     * of the Coupons. This method is called whenever the active
     * tab is switched to the summary tab.
     */
    public void updateView() {
        // ensure that existing items are cleared from the view
        this.allSaveables.getChildren().clear();
        this.savingsChart.getData().clear();
        // clear the bar graph labels when view is updated
        if (this.barGraphLabels != null) {
            this.barGraphLabels.removeIf(element -> element instanceof Text);
        }

        // set the graph
        DateSavingsSumMap mapOfAllCoupons = getMapOfAllCoupons();
        XYChart.Series<String, Number> savingsPerWeek = this.getSeries(mapOfAllCoupons);
        this.savingsChart.getData().add(savingsPerWeek);

        // sum up the total savings
        PureMonetarySavings totalSavings = mapOfAllCoupons.values().stream()
                .reduce(new PureMonetarySavings(),
                        PureMonetarySavings::add);

        // set saveables
        this.addToSaveables(totalSavings);

        // set the total amount
        this.shownMonetaryAmount = totalSavings.getMonetaryAmount().orElseThrow();
        this.updateTotalAmount();
    }

    /**
     * Given a PureMonetarySavings, retrieves the Saveables
     * that it holds, makes a new Label for each one and places
     * these Labels in the VBox that shows the total saveables
     * in the SummaryPane.
     *
     * @param savingsSum The PureMonetarySavings representing the
     *                   total savings accumulated by the user.
     */
    private void addToSaveables(PureMonetarySavings savingsSum) {
        savingsSum.getSaveables().ifPresent(saveablesList -> saveablesList
                .forEach(sva -> {
                    Label label = new Label(sva.getValue());
                    allSaveables.getChildren().add(label);
                }));
    }

    /**
     * Gets a hash table that maps a date to the savings
     * earned on that date, across all Coupons.
     *
     * @return DateSavingsSumMap that maps dates to savings
     *         on that particular date (from every Coupon).
     */
    private DateSavingsSumMap getMapOfAllCoupons() {
        DateSavingsSumMap map = new DateSavingsSumMap();
        this.allCoupons.stream()
                .map(Coupon::getSavingsMap)
                .forEach(map::addAll);
        return map;
    }

    /**
     * Gets a chart series for use in the bar graph.
     *
     * @param mapOfAllCoupons The DateSavingsSumMap representing
     *                        savings earned on each date from
     *                        using every Coupon in Coupon Stash.
     * @return XYChart Series with the data in the map provided.
     */
    private XYChart.Series<String, Number> getSeries(DateSavingsSumMap mapOfAllCoupons) {
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        List<XYChart.Data<String, Number>> listOfData = new ArrayList<XYChart.Data<String, Number>>();
        mapOfAllCoupons.forEach((date, savings) -> this.addMapEntryToList(date, savings, listOfData));
        series.setData(FXCollections.observableList(listOfData));
        return series;
    }

    /**
     * Updates the total amount shown in the Summary Tab.
     * To be used to force money symbol to change, or to
     * force the total amount shown to change.
     */
    private void updateTotalAmount() {
        this.numericalAmount.setText(
                this.shownMonetaryAmount.getStringWithMoneySymbol(this.moneySymbol.getString()));
    }

    /**
     * Converts an entry from a DateSavingsSumMap to a
     * data entry in the form of XYChart Data, and adds
     * this to the given List of XYChart Data.
     *
     * @param ld LocalDate representing date that the
     *           savings was earned.
     * @param pms PureMonetarySavings representing the
     *            amount of savings earned.
     * @param dataList The List of XYChart Data to be
     *                 populated with data.
     */
    private void addMapEntryToList(
            LocalDate ld,
            PureMonetarySavings pms,
            List<XYChart.Data<String, Number>> dataList) {

        XYChart.Data<String, Number> data = new XYChart.Data<String, Number>(
                DateUtil.formatDate(ld),
                pms.getMonetaryAmountAsDouble()
        );
        dataList.add(data);
        Text dataLabel = new Text(SummaryPane.formatMoneyAmount(pms.getMonetaryAmountAsDouble()));
        data.nodeProperty().addListener((obv, oldNode, newNode) -> {
            if (newNode != null) {
                addListenersForLabel(newNode, dataLabel);
            }
        });

    }

    /**
     * Given the JavaFX Node of a certain data element,
     * as well as a Text label representing the amount of the
     * data element, add listeners to the Node such that the
     * Text label provided will be displayed correctly with
     * the data (when this data is rendered in the BarChart).
     *
     * @param node The Node of a data element.
     * @param dataLabel The Label to be applied to the Node.
     */
    private void addListenersForLabel(Node node, Text dataLabel) {
        // listener to add label when possible
        node.parentProperty().addListener((obv, oldParent, newParent) -> {
            if (newParent != null) {
                ObservableList<Node> children = ((Group) newParent).getChildren();
                children.add(dataLabel);
                // store the list of children so we can update the
                // labels when the labels need to be updated
                this.barGraphLabels = children;
            }
        });
        // listener to set label position
        node.boundsInParentProperty().addListener((obv, oldBounds, newBounds) -> {
            dataLabel.setLayoutX(Math.round(newBounds.getMinX() + newBounds.getWidth() / 2
                    - dataLabel.prefWidth(-1) / 2));
            dataLabel.setLayoutY(newBounds.getMinY());
        });
    }

    /**
     * Formats a numerical amount to look like a
     * monetary amount (2 decimal places).
     *
     * @param amount The Number to be formatted.
     * @return String representing the amount, but
     *         formatted to 2 decimal places.
     */
    private static String formatMoneyAmount(Number amount) {
        return String.format("%.2f", amount.doubleValue());
    }
}
