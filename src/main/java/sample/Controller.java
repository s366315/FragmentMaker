package sample;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Rational;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.*;


public class Controller implements Initializable, IController, NativeMouseInputListener {
    public static final File dir = new File("capture");
    public static final String[] extensions = new String[]{"jpg", "png", "jpeg"};
    public Button btnStart, btnStop;
    private Stage stage;
    private Main main;
    private Rectangle rect = new Rectangle();
    private Point startPoint;
    private Timer timer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void btnStartListener() {
        main.openSelectArea();
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeMouseListener(this);
        } catch (NativeHookException e) {
            e.printStackTrace();
        }

    }

    public void btnStopListener() {
        if (stage != null) {
            if (timer != null) {
                timer.cancel();
            }
            makeVideo();
        }
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setMainClass(Main main) {
        this.main = main;
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        startPoint = nativeMouseEvent.getPoint();
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        if (rect == null) return;

        int left = startPoint.x, top = startPoint.y, right = nativeMouseEvent.getX(), bottom = nativeMouseEvent.getY();
        int width = right - left, height = bottom - top;
        width = (width / 2) * 2;
        height = (height / 2) * 2;
        rect.setBounds(Math.min(right, left), Math.min(bottom, top), (width > 0) ? width : width * -1, (height > 0) ? height : height * -1);

        try {
            GlobalScreen.removeNativeMouseListener(this);
            GlobalScreen.unregisterNativeHook();

            makeScreenshots(rect);

        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void keyEscPressed() {
        try {
            GlobalScreen.removeNativeMouseListener(this);
            GlobalScreen.unregisterNativeHook();

            rect = null;

        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    void makeScreenshots(Rectangle rect) {
        if (rect == null) return;

        if (timer != null) {
            timer.cancel();
        }

        btnStop.setDisable(false);

        for (File file : dir.listFiles()) {
            file.delete();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    int counter = 0;
                    String suffix = "0000";

                    @Override
                    public void run() {
                        try {
                            if (counter == 10 || counter == 100 || counter == 1000 || counter == 10000) {
                                suffix = suffix.replaceFirst("0", "");
                            }
                            BufferedImage image = new Robot().createScreenCapture(rect);
                            ImageIO.write(image, "png", new File("capture/screenshot" + suffix + counter + ".png"));
                            counter += 1;
                            if (counter == 10000) {
                                cancel();
                            }
                        } catch (AWTException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                0,
                40
        );
    }

    FilenameFilter imageFilter = (dir, name) -> {
        for (final String ext : extensions) {
            if (name.endsWith("." + ext)) {
                return (true);
            }
        }
        return (false);
    };

    void makeVideo() {
        try {
            File out = new File("capture\\a.mp4");
            if (!out.exists()) {
                out.createNewFile();
            }

            ArrayList<File> lst = new ArrayList<>();

            if (dir.isDirectory()) {
                lst.addAll(Arrays.asList(dir.listFiles(imageFilter)));
            }

            SequenceEncoder enc = SequenceEncoder.createWithFps(NIOUtils.writableChannel(out), new Rational(24, 1));
            for (File file : lst) {
                enc.encodeNativeFrame(AWTUtil.decodePNG(file, ColorSpace.RGB));
            }

            enc.finish();

            for (File file : lst) {
                file.delete();
            }

            btnStop.setDisable(true);

            displayTray(out);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayTray(File file) throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

        ArrayList<File> files = new ArrayList<>();
        files.add(file);

        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> {
            Platform.runLater(() -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putFiles(files);
                clipboard.setContent(content);
            });

        });

        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);

        trayIcon.displayMessage("Success", "Capture successful saved. Click to copy link", TrayIcon.MessageType.INFO);
    }
}
