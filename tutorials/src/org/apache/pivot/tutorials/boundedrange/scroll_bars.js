function init() {
    ranges.selection = weekButton;
}

function updateRange() {
    var amount;

    if (ranges.selection == dayButton) {
        amount = 1;
    } else if (ranges.selection == weekButton) {
        amount = 7;
    } else if (ranges.selection == fortnightButton) {
        amount = 14;
    } else {
        amount = 30;
    }

    scrollBar.extent = scrollBar.unitIncrement = amount;
    scrollBar.blockIncrement = 2 * amount;
}

function updateLabel() {
    var first = scrollBar.value + 1;
    var last = scrollBar.value + scrollBar.extent;
    label.setText("Days " + first + " through " + last);
}
