importPackage(Packages.pivot.collections);
importPackage(Packages.pivot.wtk);

var foo = "Hello World";

var buttonPressListener = new ButtonPressListener() {
    buttonPressed: function(button) {
        Alert.alert("You clicked me!", button.getWindow());
        // BrowserApplicationContext.eval("alert('You clicked me!');");
    }
};

var listData = new ArrayList();
listData.add("One");
listData.add("Two");
listData.add("Three");
