package spm;

import javax.swing.*;
import java.awt.*;

public class InputDialog extends Dialog {
    public static final int FIELD_NORMAL = 0;
    public static final int FIELD_PASSWORD = 1 << 0;
    public static final int FIELD_NOTEMPTY = 1 << 1;
    public static final int FIELD_GEN_PASSWORD = 1 << 2;

    public interface CheckFields {
        String check(Object[] fields);
    }

    public interface CheckField {
        String check(Object field);
    }

    public static Object show(Frame parent, String title) {
        return show(parent, title, (String) null);
    }

    public static Object show(Frame parent, String title, String labelName) {
        return show(parent, title, labelName, null);
    }

    public static Object show(Frame parent, String title, String labelName, String defValue) {
        return show(parent, title, labelName, defValue, 0);
    }

    public static Object show(Frame parent, String title, String labelName, String defValue, int fieldMode) {
        return show(parent, title, labelName, defValue, fieldMode, null);
    }

    public static Object show(Frame parent, String title, String labelName, String defValue, int fieldMode, CheckField checkSingle) {
        Object[] tmp = show(parent, title, new String[]{labelName}, new String[]{defValue}, new int[]{fieldMode}, null, checkSingle);
        return tmp != null && tmp.length > 0 ? tmp[0] : null;
    }

    public static Object[] show(Frame parent, String title, String[] labelNames) {
        return show(parent, title, labelNames, null);
    }

    public static Object[] show(Frame parent, String title, String[] labelNames, String[] defValues) {
        return show(parent, title, labelNames, defValues, null);
    }

    public static Object[] show(Frame parent, String title, String[] labelNames, String[] defValues, int[] fieldMode) {
        return show(parent, title, labelNames, defValues, fieldMode, null);
    }

    public static Object[] show(Frame parent, String title, String[] labelNames, String[] defValues, int[] fieldMode, CheckFields checkMulti) {
        return show(parent, title, labelNames, defValues, fieldMode, checkMulti, null);
    }

    private static Object[] show(Frame parent, String title, String[] labelNames, String[] defValues, int[] fieldMode, CheckFields checkMulti, CheckField checkSingle) {
        InputDialog inp = new InputDialog(parent, title, labelNames, defValues, fieldMode, checkMulti, checkSingle);
        inp.setVisible(true);
        Object[] result = inp.result;
        inp.dispose();
        return result;
    }

    private InputDialog(Frame parent, String title, String[] labelNames, String[] defValues, int[] fieldMode, CheckFields checkMulti, CheckField checkSingle) {
        super(parent, true);
        this.fieldMode = fieldMode;
        this.checkMulti = checkMulti;
        this.checkSingle = checkSingle;
        initComponents(title, labelNames, defValues);
    }

    private void initComponents(String title, String[] labelNames, String[] defValues) {
        okButt = new JButton("OK");
        genPassButt = new JButton("↻");
        Insets genPassButtMargin = genPassButt.getMargin();
        genPassButt.setMargin(new Insets(genPassButtMargin.top, 5, genPassButtMargin.bottom, 5));
        genPassButt.setToolTipText("Generate password and copy to clipboard");
        cancelButt = new JButton("Cancel");
        labels = new JLabel[labelNames.length];
        fields = new JTextField[labelNames.length];

        for (int i = 0; i < labelNames.length; ++i) {
            labels[i] = new JLabel(labelNames[i]);
            fields[i] = (fieldMode != null && i < fieldMode.length && (fieldMode[i] & FIELD_PASSWORD) != 0) ? new JPasswordField() : new JTextField();
            fields[i].setColumns(25);
        }

        if (defValues != null) {
            for (int i = 0; i < defValues.length && i < fields.length; ++i) {
                fields[i].setText(defValues[i]);
            }
        }

        setResizable(false);
        setTitle(title);
        setIconImage(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                close();
            }
        });

        cancelButt.addActionListener(evt -> close());
        okButt.addActionListener(evt -> ok());
        genPassButt.addActionListener(evt -> generatePassword());
        fields[fields.length - 1].addActionListener(evt -> ok());

        setLayout(createLayout(labelNames.length > 1));
        pack();
        setLocationRelativeTo(null);
    }

    private void generatePassword() {
        for (int i = 0; i < fields.length; ++i) {
            if ((fieldMode[i] & FIELD_GEN_PASSWORD) != 0) {
                String passwd = new String(Crypto.generatePassword());
                Clipboard.copy(passwd, 30);
                fields[i].setText(passwd);
                break;
            }
        }
    }

    private GroupLayout createLayout(boolean isMulti) {
        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        if (isMulti) {
            layoutMulti(layout);
        } else {
            layoutSingle(layout);
        }

        return layout;
    }

    private void layoutMulti(GroupLayout layout) {
        GroupLayout.ParallelGroup horLabels = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.ParallelGroup horFields = layout.createParallelGroup();
        GroupLayout.ParallelGroup horButt = layout.createParallelGroup();

        for (int i = 0; i < fields.length; ++i) {
            horLabels.addComponent(labels[i]);
            horFields.addComponent(fields[i]);
            if ((fieldMode[i] & FIELD_GEN_PASSWORD) != 0) {
                horButt.addComponent(genPassButt);
            }
        }

        GroupLayout.SequentialGroup horLF = layout.createSequentialGroup()
                .addGroup(horLabels)
                .addGroup(horFields)
                .addGroup(horButt);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(horLF)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(okButt)
                                .addComponent(cancelButt))
        );

        GroupLayout.SequentialGroup verLF = layout.createSequentialGroup();
        for (int i = 0; i < fields.length; ++i) {
            GroupLayout.ParallelGroup g = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labels[i])
                    .addComponent(fields[i]);
            if ((fieldMode[i] & FIELD_GEN_PASSWORD) != 0) {
                g.addComponent(genPassButt);
            }
            verLF.addGroup(g);
        }

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(verLF)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(okButt)
                                .addComponent(cancelButt))
        );
    }

    private void layoutSingle(GroupLayout layout) {
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(labels[0])
                        .addComponent(fields[0])
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(okButt)
                                .addComponent(cancelButt))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(labels[0])
                        .addComponent(fields[0])
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(okButt)
                                .addComponent(cancelButt))
        );
    }

    private void close() {
        setVisible(false);
    }

    private void ok() {
        Object[] input = new Object[fields.length];
        for (int i = 0; i < fields.length; ++i) {
            input[i] = fields[i] instanceof JPasswordField ? ((JPasswordField) fields[i]).getPassword() : fields[i].getText();
        }

        if (!checkInput(input)) {
            result = null;
            return;
        }
        result = input;
        setVisible(false);
    }

    private boolean checkInput(Object[] input) {
        for (int i = 0; i < input.length; ++i) {
            if (fieldMode != null && i < fieldMode.length && (fieldMode[i] & FIELD_NOTEMPTY) != 0) {
                if ((input[i] instanceof char[] ? ((char[]) input[i]).length : ((String) input[i]).length()) < 1) {
                    JOptionPane.showMessageDialog(null, "\"" + labels[i].getText() + "\" field cannot be empty",
                            "Warning: Empty field", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
        }

        String msg = input.length > 1 ? (checkMulti == null ? null : checkMulti.check(input)) : (checkSingle == null ? null : checkSingle.check(input));
        if (msg != null) {
            JOptionPane.showMessageDialog(null, msg, "Warning: Input error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private JButton okButt;
    private JButton genPassButt;
    private JButton cancelButt;
    private JLabel[] labels;
    private JTextField[] fields;
    private Object[] result;
    private final int[] fieldMode;
    private final CheckFields checkMulti;
    private final CheckField checkSingle;
}
