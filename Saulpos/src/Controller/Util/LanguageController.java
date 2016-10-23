/*
 * Copyright (C) 2012 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Controller.Util;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class LanguageController {
    
    private ResourceBundle lang = null;
    private Locale locale = null;
    
    /**
     * This function initializes the language.
     * @param language Language of the country.
     * @param country Country.
     */
    public void initializeLanguage(String language , String country){
        locale = new Locale(language, country);
        lang = ResourceBundle.getBundle(MainController.getInstance().getConstants().getDefaultLangDir(), locale);
    }
    
    /**
     * Returns a message in a the specified language.
     * @param k Key
     * @return Message in the right language
     */
    public String get(String k){
        if ( lang == null ){
            return "";
        }else if ( lang.containsKey(k) ){
            return lang.getString(k);
        }
        return k;
    }
    
    public void translateFrame(Object view){
        /** Autotranslating the controls and setting theme*/
        
        Class<?> viewClass = view.getClass();
        
        for (Field field : viewClass.getDeclaredFields()) {
            try {
                Object obj = field.get(view);
                if ( obj instanceof JLabel ){
                    JLabel label = (JLabel)obj;
                    label.setText(MainController.getInstance().getLanguage().get(label.getText()));
                }else if ( obj instanceof JTextField ){
                    JTextField textField = (JTextField)obj;
                    textField.setText(MainController.getInstance().getLanguage().get(textField.getText()));
                }else if ( obj instanceof JButton ){
                    JButton button = (JButton)obj;
                    button.setText(MainController.getInstance().getLanguage().get(button.getText()));
                }else if ( obj instanceof JCheckBox ){
                    JCheckBox checkBox = (JCheckBox)obj;
                    checkBox.setText(MainController.getInstance().getLanguage().get(checkBox.getText()));
                }else if ( obj instanceof JPanel ){
                    JPanel panel = (JPanel)obj;
                    if ( !panel.getToolTipText().isEmpty() ){
                        String translatedText = MainController.getInstance().getLanguage().get(panel.getToolTipText());
                        panel.setBorder(BorderFactory.createTitledBorder(translatedText));
                        panel.setToolTipText(translatedText);
                    }
                }else if ( obj instanceof Label ){
                    Label label = (Label)obj;
                    label.setText(MainController.getInstance().getLanguage().get(label.getText()));
                }else if ( obj instanceof Button ){
                    Button button = (Button)obj;
                    button.setText(MainController.getInstance().getLanguage().get(button.getText()));
                }else if ( obj instanceof TextArea ){
                    TextArea textArea = (TextArea)obj;
                    textArea.setText(MainController.getInstance().getLanguage().get(textArea.getText()));
                }else if ( obj instanceof TextField ){
                    TextField textField = (TextField)obj;
                    textField.setPromptText(MainController.getInstance().getLanguage().get(textField.getText()));
                    textField.setText("");
                }else if ( obj instanceof TableColumn){
                    TableColumn tableColumn = (TableColumn)obj;
                    tableColumn.setText(MainController.getInstance().getLanguage().get(tableColumn.getText()));
                }else if ( obj instanceof CheckBox ){
                    CheckBox checkBox = (CheckBox)obj;
                    checkBox.setText(MainController.getInstance().getLanguage().get(checkBox.getText()));
                }
            } catch (Exception ex) {
                MainController.getInstance().getLogger().warn( LanguageController.class , ex.getMessage());
            }
        }
        
        /** Set title */
        if ( view instanceof JFrame){
            JFrame frame = (JFrame)view;
            frame.setTitle(MainController.getInstance().getLanguage().get(frame.getTitle()));
        }
    }
}
