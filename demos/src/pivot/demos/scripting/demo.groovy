import pivot.collections.adapter.*
import pivot.wtk.*

foo = "ABCDE"

public class MyButtonPressListener implements ButtonPressListener {
    public void buttonPressed(Button button) {
        Alert.alert("You clicked me!", button.getWindow())
    }
}

buttonPressListener = new MyButtonPressListener()

listData = []
listData << "One"
listData << "Two"
listData << "Three"

listData = new ListAdapter(listData)
