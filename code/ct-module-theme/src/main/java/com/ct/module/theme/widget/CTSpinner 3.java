package com.ct.module.theme.widget;

import com.ct.module.theme.Theme;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.basic.BasicTextFieldUI;

public final class CTSpinner extends JSpinner {
    private static final long serialVersionUID = 1L;

    public CTSpinner(SpinnerNumberModel model) {
        super(model);
        setUI(new SpinnerUi());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        JTextField field = editorField();
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                putClientProperty("ct.focused", Boolean.TRUE);
                repaint();
            }

            @Override
            public void focusLost(FocusEvent event) {
                putClientProperty("ct.focused", Boolean.FALSE);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Canvas.prepare(graphics);
        boolean focused = isFocusOwner() || Boolean.TRUE.equals(getClientProperty("ct.focused"));
        Canvas.roundedRect(g, 0, 0, getWidth(), getHeight(), Canvas.radius(),
                isEnabled() ? Theme.FIELD : Theme.SURFACE_ALT,
                focused ? Theme.ACCENT : Theme.BORDER);
        g.dispose();
        super.paintComponent(graphics);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, Math.max(size.height, 34));
    }

    private JTextField editorField() {
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
        JTextField field = editor.getTextField();
        field.setUI(new BasicTextFieldUI());
        field.setOpaque(false);
        field.setFont(Theme.ui(13));
        field.setForeground(Theme.TEXT);
        field.setDisabledTextColor(Theme.TEXT_FAINT);
        field.setCaretColor(Theme.ACCENT);
        field.setSelectionColor(Theme.SELECTION);
        field.setSelectedTextColor(Theme.TEXT);
        field.setBorder(new EmptyBorder(6, 10, 6, 2));
        field.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        return field;
    }

    private static final class SpinnerUi extends BasicSpinnerUI {
        @Override
        protected Component createNextButton() {
            ChevronButton button = new ChevronButton(ChevronButton.Direction.UP, 22);
            installNextButtonListeners(button);
            return button;
        }

        @Override
        protected Component createPreviousButton() {
            ChevronButton button = new ChevronButton(ChevronButton.Direction.DOWN, 22);
            installPreviousButtonListeners(button);
            return button;
        }
    }
}
