package com.ct.main.ui;

import com.ct.main.api.ModuleContext;
import com.ct.main.core.ModuleHost;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public final class HostWindow {
    private static final String WELCOME_CARD = "welcome";
    private static final String MODULES_CARD = "modules";

    private final ModuleContext context;
    private final ModuleHost host;

    public HostWindow(ModuleContext context, ModuleHost host) {
        this.context = context;
        this.host = host;
    }

    public void show() {
        JFrame window = new JFrame("CT-Main " + context.mainVersion());
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                window.dispose();
                System.exit(0);
            }
        });
        window.setContentPane(buildContent());
        if (context.views().isEmpty()) {
            window.setSize(560, 380);
            window.setMinimumSize(new Dimension(520, 340));
        } else {
            window.setSize(980, 700);
            window.setMinimumSize(new Dimension(880, 620));
        }
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public JComponent buildContent() {
        if (context.views().isEmpty()) {
            return new WelcomeView(context.mainVersion());
        }
        return buildNavigation();
    }

    private JComponent buildNavigation() {
        List<String> titles = new ArrayList<>();
        titles.add("Welcome");
        for (ModuleContext.ViewEntry entry : context.views()) {
            titles.add(entry.title());
        }
        titles.add("Modules");

        CardLayout cards = new CardLayout();
        JPanel stack = new JPanel(cards);
        stack.setBackground(BareTheme.BACKGROUND);
        stack.add(new WelcomeView(context.mainVersion()), WELCOME_CARD);
        for (ModuleContext.ViewEntry entry : context.views()) {
            stack.add(wrap(entry.component()), entry.title());
        }
        stack.add(new ModulesView(host.loaded(), host.skipped()), MODULES_CARD);

        JList<String> nav = new JList<>(titles.toArray(new String[0]));
        nav.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nav.setSelectedIndex(0);
        nav.setFont(BareTheme.ui(13));
        nav.setBackground(BareTheme.SURFACE);
        nav.setForeground(BareTheme.TEXT);
        nav.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        nav.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focused) {
                java.awt.Component component = super.getListCellRendererComponent(list, value, index,
                        selected, focused);
                component.setBackground(selected ? BareTheme.BACKGROUND.darker() : BareTheme.SURFACE);
                component.setForeground(selected ? BareTheme.ACCENT : BareTheme.TEXT);
                return component;
            }
        });
        nav.addListSelectionListener(event -> {
            int index = nav.getSelectedIndex();
            if (index < 0) {
                return;
            }
            String card = index == 0 ? WELCOME_CARD
                    : index == titles.size() - 1 ? MODULES_CARD
                    : titles.get(index);
            cards.show(stack, card);
        });

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BareTheme.SURFACE);
        sidebar.add(new JScrollPane(nav), BorderLayout.CENTER);
        sidebar.setPreferredSize(new Dimension(210, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BareTheme.BACKGROUND);
        root.add(sidebar, BorderLayout.WEST);
        root.add(stack, BorderLayout.CENTER);
        return root;
    }

    private JComponent wrap(JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BareTheme.BACKGROUND);
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }
}
