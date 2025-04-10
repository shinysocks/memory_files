import java.io.File;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Controller implements Initializable {
    private boolean WIDTH_SET = false;
    private boolean PLAYING = false;
    private final int AUDIO_CAPACITY = 3;
    private final String AUDIO_PATH = "audio/";
    private final String BUTTON_IDLE = "-fx-background-color: #f4f0db;" +
            "-fx-background-radius: 30;" +
            "-fx-border-radius: 30;" +
            "-fx-text-fill: black;";
    private final String BUTTON_HOVER = "-fx-background-color: black;" +
            "-fx-background-radius: 30;" +
            "-fx-border-radius: 30;" +
            "-fx-text-fill: #f4f0db;";
    private ArrayList<ArrayList<SimpleEntry<String, AudioClip>>> audio = 
            new ArrayList<ArrayList<SimpleEntry<String, AudioClip>>>(AUDIO_CAPACITY);

    @FXML
    private Text out;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 0; i < AUDIO_CAPACITY; i++)
                audio.add(new ArrayList<SimpleEntry<String, AudioClip>>());

        int i = 0;
        File[] dirs = new File(AUDIO_PATH).listFiles();
        
        // sort alphabetically so buttons map to correct directories
        Arrays.sort(dirs);

        for (File d : dirs) {
            if (d.isDirectory()) {
                for (File f : d.listFiles()) {
                    if (!f.isHidden()) {
                        String name = f.getName();
                        AudioClip clip = new AudioClip(f.toURI().toString());
                        var entry = new SimpleEntry<String, AudioClip>(name, clip);
                        audio.get(i).add(entry);
                    }
                }
                i++;
            }
        }
    }

    @FXML
    private void button_clicked(ActionEvent e) {
        if (!WIDTH_SET) set_out_wrapping_width();

        if (!PLAYING) {
            String id = ((Button) e.getSource()).getId();
            int audio_subset_index = Integer.valueOf(id);
            System.out.println("selected: " + audio_subset_index);
            int random_index = (int) (Math.random() * audio.get(audio_subset_index).size());

            var entry = audio.get(audio_subset_index).get(random_index);
            var clip = entry.getValue();
            animate_text(entry.getKey().substring(0, entry.getKey().length() - 4));
            new Thread(new Runnable() {
				@Override
				public void run() {
                    PLAYING = true;
                    clip.play();
                    while (clip.isPlaying()) {} // manually block thread until clip is done
                    PLAYING = false;
				}
            }).start();
        }
    }

    @FXML
    private void button_hover(MouseEvent e) {
        ((Button) e.getSource()).setStyle(BUTTON_HOVER);
    }

    @FXML
    private void button_default(MouseEvent e) {
        ((Button) e.getSource()).setStyle(BUTTON_IDLE);
    }

    private void animate_text(String text) {
        final String quoted_text = "\"" + text + "\"";
        final Timeline timeline = new Timeline();
        final AtomicInteger i = new AtomicInteger(0);
        KeyFrame frame = new KeyFrame(
            Duration.millis(100),
            _ -> {
                if (i.get() > quoted_text.length())
                    timeline.stop();
                else
                    out.setText(quoted_text.substring(0, i.addAndGet(1)));
            }
        );

        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(quoted_text.length());
        timeline.play();
    }

    private void set_out_wrapping_width() {
        out.setWrappingWidth(out.getScene().getWidth() / 1.5);
        WIDTH_SET = true;
    }
}
