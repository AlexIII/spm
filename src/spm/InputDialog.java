/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spm;

import javax.swing.JOptionPane;

/**
 *
 * @author Alex
 */
public class InputDialog extends java.awt.Dialog {
//public interface
    public static final int FIELD_NORMAL = 0;
    public static final int FIELD_PASSWORD = 1<<0;
    public static final int FIELD_NOTEMPTY = 1<<1;
    
    public interface CheckFields {
        public String check(Object[] fields);
    }
    public interface CheckField {
        public String check(Object field);
    }    

    public static Object show(java.awt.Frame parent, String title) {
        return show(parent, title, (String)null);
    }
    public static Object show(java.awt.Frame parent, String title, String labelName) {
        return show(parent, title, labelName, null);
    }
    public static Object show(java.awt.Frame parent, String title, String labelName, String defValue) {
        return show(parent, title, labelName, defValue, 0);
    }
    public static Object show(java.awt.Frame parent, String title, String labelName, String defValue, int fieldMode) {
        return show(parent, title, labelName, defValue, fieldMode, null);
    }
    public static Object show(java.awt.Frame parent, String title, String labelName, String defValue, int fieldMode, CheckField checkSingle) {
        Object[] tmp = show(parent, title, new String[] {labelName}, new String[] {defValue}, new int[] {fieldMode}, null, checkSingle);
        return tmp != null && tmp.length > 0? tmp[0] : null;
    }
    
    public static Object[] show(java.awt.Frame parent, String title, String[] labelNames) {
        return show(parent, title, labelNames, null);
    }    
    public static Object[] show(java.awt.Frame parent, String title, String[] labelNames, String[] defValues) {
        return show(parent, title, labelNames, defValues, null);
    }    
    public static Object[] show(java.awt.Frame parent, String title, String[] labelNames, String[] defValues, int[] fieldMode) {
        return show(parent, title, labelNames, defValues, fieldMode, null);
    } 
    public static Object[] show(java.awt.Frame parent, String title, String[] labelNames, String[] defValues, int[] fieldMode, CheckFields checkMulti) {
        return show(parent, title, labelNames, defValues, fieldMode, checkMulti, null);
    }

//private methods
    private static Object[] show(java.awt.Frame parent, String title, String[] labelNames, String[] defValues, int[] fieldMode, CheckFields checkMulti, CheckField checkSingle) {
        InputDialog inp = new InputDialog(parent, title, labelNames, defValues, fieldMode, checkMulti, checkSingle);
        inp.setVisible(true);
        Object[] result = inp.result;
        inp.dispose();
        return result;
    }
    
    private InputDialog(java.awt.Frame parent, String title, String[] labelNames, String[] defValues, int[] fieldMode, CheckFields checkMulti, CheckField checkSingle) {
        super(parent, true);
        initComponents(title, labelNames, defValues, fieldMode);
        this.fieldMode = fieldMode;
        this.checkMulti = checkMulti;
        this.checkSingle = checkSingle;
    }
    
    private void initComponents(String title, String[] labelNames, String[] defValues, int[] fieldMode) {
        //create components
        okButt = new javax.swing.JButton("OK");
        cancelButt = new javax.swing.JButton("Cancel");   
        labels = new javax.swing.JLabel[labelNames.length];
        fields = new javax.swing.JTextField[labelNames.length];
        for(int i = 0; i < labelNames.length; ++i) {
            labels[i] = new javax.swing.JLabel(labelNames[i]);
            if(fieldMode != null && i < fieldMode.length) {
                if((fieldMode[i]&FIELD_PASSWORD) != 0) fields[i] = new javax.swing.JPasswordField();
                else fields[i] = new javax.swing.JTextField();
            } else fields[i] = new javax.swing.JTextField();
            fields[i].setColumns(25);
        }
        if(defValues != null)
            for(int i = 0; i < defValues.length && i < fields.length; ++i)
                fields[i].setText(defValues[i]);
        
        //set properties
        setResizable(false);
        setTitle(title);
        setIconImage(null);
        
        //set listeners
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {close();}
        });        
        cancelButt.addActionListener((evt) -> {close();});
        okButt.addActionListener((evt) -> {ok();});
        fields[fields.length-1].addActionListener((evt) -> {ok();});
        
        //layout
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        if(labelNames.length > 1) layoutMulti(layout);
        else layoutSingle(layout);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void layoutMulti(javax.swing.GroupLayout layout) {
        //Horizontal
        javax.swing.GroupLayout.ParallelGroup horLabels = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING);
        javax.swing.GroupLayout.ParallelGroup horFields = layout.createParallelGroup();
        for(int i = 0; i < fields.length; ++i) {
            horLabels.addComponent(labels[i]);
            horFields.addComponent(fields[i]);
        }
        javax.swing.GroupLayout.SequentialGroup horLF = 
            layout.createSequentialGroup()
                .addGroup(horLabels)
                .addGroup(horFields)
        ;
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addGroup(horLF)
                .addGroup(
                    layout.createSequentialGroup()
                        .addComponent(okButt)
                        .addComponent(cancelButt)
                )
        );
        
        //Vertical
        javax.swing.GroupLayout.SequentialGroup verLF = layout.createSequentialGroup();
        for(int i = 0; i < fields.length; ++i)
            verLF.addGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labels[i])
                    .addComponent(fields[i])
            );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(verLF)                
                .addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(okButt)
                        .addComponent(cancelButt) 
                )
        );        
    }
    
    private void layoutSingle(javax.swing.GroupLayout layout) {
        //Horizontal
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(labels[0])
                .addComponent(fields[0])
                .addGroup(
                    layout.createSequentialGroup()
                        .addComponent(okButt)
                        .addComponent(cancelButt)
                )
        );
        
        //Vertical
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(labels[0])
                .addComponent(fields[0])                
                .addGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(okButt)
                        .addComponent(cancelButt) 
                )
        );                
    }
    
    private void close() {
        setVisible(false);
    }
    
    private void ok() {
        Object[] input = new Object[fields.length];
        for(int i = 0; i < fields.length; ++i)
            input[i] = fields[i] instanceof javax.swing.JPasswordField? 
                    ((javax.swing.JPasswordField)fields[i]).getPassword() :
                    fields[i].getText();
        
        if(!checkInput(input)) {
            result = null;
            return;
        }
        result = input;
        setVisible(false);
    }
    
    private boolean checkInput(Object[] input) {
        for(int i = 0; i < input.length; ++i)
            if(fieldMode != null && i < fieldMode.length) {
                if((fieldMode[i]&FIELD_NOTEMPTY) != 0)
                    if((input[i] instanceof char[]? 
                        ((char[])input[i]).length :
                        ((String)input[i]).length()
                        ) < 1) {
                    JOptionPane.showMessageDialog(null, "\"" + labels[i].getText() + "\" field cannot be empty",
                            "Warning: Empty field", JOptionPane.WARNING_MESSAGE);
                    return false;
                    }
            }
                
        String msg = input.length > 1? 
                (checkMulti == null? null : checkMulti.check(input)) 
                :(checkSingle == null? null : checkSingle.check(input));
        if(msg != null) {
            JOptionPane.showMessageDialog(null, msg, "Warning: Input error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private javax.swing.JButton okButt;
    private javax.swing.JButton cancelButt;
    private javax.swing.JLabel[] labels;
    private javax.swing.JTextField[] fields;
    private Object[] result;
    private final int[] fieldMode;
    private final CheckFields checkMulti;
    private final CheckField checkSingle;
}
