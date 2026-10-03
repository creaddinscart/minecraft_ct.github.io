package com.ct.main;

import com.ct.main.api.ModuleContext;
import com.ct.main.core.ModuleHost;
import com.ct.main.ui.HostWindow;
import com.ct.main.ui.WelcomeView;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

final class OffscreenRender {
    private static final int WIDTH = 980;
    private static final int HEIGHT = 700;

    private OffscreenRender() {
    }

    static void renderMainViews(ModuleContext context, ModuleHost host, Path target)
            throws Exception {
        int index = 0;
        writeView(new WelcomeView(context.mainVersion()), target);
        for (ModuleContext.ViewEntry entry : context.views()) {
            index++;
            String name = target.getFileName().toString().replace(".png", "")
                    + "-view-" + index + "-" + sanitize(entry.title()) + ".png";
            writeView(entry.component(), target.resolveSibling(name));
        }
        writeView(HostWindow.modulesView(host, context),
                target.resolveSibling(target.getFileName().toString().replace(".png", "")
                        + "-view-modules.png"));
        System.out.println("Rendered " + (index + 2) + " view(s) next to " + target + ".");
    }

    private static String sanitize(String title) {
        return title.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    private static void writeView(JComponent component, Path target) throws Exception {
        JPanel holder = new JPanel(new BorderLayout());
        holder.add(HostWindow.wrapView(component), BorderLayout.CENTER);
        JFrame frame = new JFrame();
        try {
            frame.setUndecorated(true);
            frame.setContentPane(holder);
            frame.setSize(WIDTH, HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            frame.paint(graphics);
            graphics.dispose();
            Files.createDirectories(target.toAbsolutePath().getParent());
            javax.imageio.ImageIO.write(image, "png", target.toFile());
            System.out.println("Rendered view: " + target.toAbsolutePath());
        } finally {
            frame.dispose();
        }
    }
}
