package eliranh.three_layers_security.Classes;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.StreamResource;
@Tag("audio")
public class AudioPlayer extends Component {
    public AudioPlayer() {
        getElement().setAttribute("controls", true);
        getElement().setAttribute("autoplay", true);
    }
    // public void setSource(String path) {
    //     getElement().setProperty("src", path);
    // }
    public void setSource(StreamResource resource) {
        getElement().setAttribute("src", resource);
    }
}
