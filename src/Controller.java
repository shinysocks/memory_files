import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Controller implements Initializable {
    private boolean WIDTH_SET = false;
    private boolean ANIMATING = false;
    private final int QUOTES_CAPACITY = 3;
    private ArrayList<ArrayList<String>> quotes = new ArrayList<ArrayList<String>>(QUOTES_CAPACITY);

    @FXML
    private Text out;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 0; i < QUOTES_CAPACITY; i++)
            quotes.add(new ArrayList<String>());

        try {
			fill_quotes(Files.readAllLines(Path.of("quotes.txt")));
		} catch (IOException e) {
            System.err.println("cannot read quotes.txt, make sure it exists.");
            System.exit(0);
		}
    }

    @FXML
    private void button_clicked(ActionEvent e) {
        if (!WIDTH_SET) set_out_wrapping_width();

        if (!ANIMATING) {
            String id = ((Button) e.getSource()).getId();
            int quote_subset_index = Integer.valueOf(id);
            int random_index = (int) (Math.random() * quotes.get(quote_subset_index).size());
            animate(quotes.get(quote_subset_index).get(random_index));
        }
    }

    private void animate(String text) {
        ANIMATING = true;
        final String quoted_text = "\"" + text + "\"";
        final Timeline timeline = new Timeline();
        final AtomicInteger i = new AtomicInteger(0);
        KeyFrame frame = new KeyFrame(
            Duration.millis(30 + (long) (Math.random() * 20)),
            e -> {
                if (i.get() > quoted_text.length())
                    timeline.stop();
                else
                    out.setText(quoted_text.substring(0, i.addAndGet(1)));
            }
        );

        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(quoted_text.length());
        timeline.setOnFinished(e -> ANIMATING = false);
        timeline.play();
    }

    private void set_out_wrapping_width() {
        out.setWrappingWidth(out.getScene().getWidth() / 1.5);
        WIDTH_SET = true;
    }

    private void fill_quotes(List<String> input) {
        int i = 0;
        for (String l : input) {
            if (l.length() == 0) continue;
            else if (l.charAt(0) == '$') i++;
            else if (!(l.charAt(0) == '#')) quotes.get(i).add(l);
        }
    }
}
