package co.videofirst.vft.capture.experimental;

import com.bulenkov.darcula.DarculaLaf;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicLookAndFeel;

/**
 * @author Bob Marks
 */
public class HideToSystemTray extends JFrame {

    private static final int width = 800;
    private static final int height = 800;

    TrayIcon trayIcon;
    SystemTray tray;

    HideToSystemTray() {
        super("Video First Capture");
        URL captureIcon16 = HideToSystemTray.class.getResource("/icons/icon-capture-16.gif");

        // Set size / position
        Dimension size = new Dimension(width, height);
        Point location = getCentredLocation(this, size);

        this.setSize(size);
        this.setLocation(location.x, location.y);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(captureIcon16));
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        System.out.println("creating instance");
        try {
            System.out.println("setting look and feel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Unable to set LookAndFeel");
        }

        if (SystemTray.isSupported()) {
            System.out.println("system tray supported");
            tray = SystemTray.getSystemTray();

            Image image = Toolkit.getDefaultToolkit().getImage(captureIcon16);
            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting....");
                    System.exit(0);
                }
            };
            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
            defaultItem = new MenuItem("Open");
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    setExtendedState(JFrame.NORMAL);
                }
            });
            popup.add(defaultItem);
            trayIcon = new TrayIcon(image, "Video First Capture", popup);
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        setVisible(true);
                        setExtendedState(JFrame.NORMAL);
                    }
                }

            });
        } else {
            System.out.println("system tray not supported");
        }
        addWindowStateListener(e -> {
            if (e.getNewState() == ICONIFIED) {
                try {
                    tray.add(trayIcon);
                    setVisible(false);
                    System.out.println("added to SystemTray");
                } catch (AWTException ex) {
                    System.out.println("unable to add to tray");
                }
            }
            if (e.getNewState() == 7) {
                try {
                    tray.add(trayIcon);
                    setVisible(false);
                    System.out.println("added to SystemTray");
                } catch (AWTException ex) {
                    System.out.println("unable to add to system tray");
                }
            }
            if (e.getNewState() == MAXIMIZED_BOTH) {
                tray.remove(trayIcon);
                setVisible(true);
                System.out.println("Tray icon removed");
            }
            if (e.getNewState() == NORMAL) {
                tray.remove(trayIcon);
                setVisible(true);
                System.out.println("Tray icon removed");
            }
        });
    }

    public static void main(String[] args) throws Exception {
        BasicLookAndFeel darcula = new DarculaLaf();
        UIManager.setLookAndFeel(darcula);

        new HideToSystemTray();
    }

    public static Point getCentredLocation(Window window, Dimension componentSize) {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gs.length; i++) {
            DisplayMode dm = gs[i].getDisplayMode();
            sb.append(i + ", width: " + dm.getWidth() + ", height: " + dm.getHeight() + "\n");
        }
        System.out.println(sb.toString());

        //int currentScreenWidth = gs[0].getDisplayMode().getWidth();
        //        //int currentScreenHeight = gs[0].getDisplayMode().getWidth();

        Dimension screenSize = window.getToolkit().getScreenSize();
        Point p = new Point(
            (int) (screenSize.getWidth() / 2 - componentSize.getWidth() / 2),
            (int) (screenSize.getHeight() / 2 - componentSize.getHeight() / 2));

        return p;
    }

}