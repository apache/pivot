importPackage(Packages.pivot.wtk);

var buttonPressListener = new ButtonPressListener() {
    buttonPressed: function(button) {
        Alert.alert("Welcome to Pivot!", button.getWindow());
    }
};
