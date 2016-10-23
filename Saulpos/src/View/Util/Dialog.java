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
package View.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * CLASS DERIVED FROM https://github.com/4ntoine/JavaFxDialog/
 * @author Anton Smirnov (dev@antonsmirnov.name)
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class Dialog extends Stage {
    protected String stacktrace;
    protected double originalWidth, originalHeight;
    
    protected Scene scene;
    protected BorderPane borderPanel;
    protected ImageView icon;
    
    protected VBox messageBox;
    protected Label messageLabel;
    
    protected boolean stacktraceVisible;
    protected HBox stacktraceButtonsPanel;
    protected ToggleButton viewStacktraceButton;    
    protected Button copyStacktraceButton;
    protected ScrollPane scrollPane;
    protected Label stackTraceLabel;
    
    protected HBox buttonsPanel;
    protected Button okButton;
    
    /**
     * Extracts stack trace from Throwable
     */
    protected static class StacktraceExtractor {

        public String extract(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            return sw.toString();
        }
    }
    

    /**
     * Show information dialog box as parentWindow child
     * 
     * @param title dialog title
     * @param message dialog message
     * @param owner parent window
     */
    public static void showInfo(String title, String message, Window owner) {
        new DialogBuilder()
            .create()
            .setOwner(owner)
            .setTitle(title)
            .setInfoIcon()
            .setMessage(message)            
            .addOkButton()
                .build()
                    .show();
    }
    
    /**
     * Show information dialog box as parentStage child
     * 
     * @param title dialog title
     * @param message dialog message
     */
    public static void showInfo(String title, String message) {
        showInfo(title, message, null);
    }

    /**
     * Show warning dialog box as parentStage child
     * 
     * @param title dialog title
     * @param message dialog message
     * @param owner parent window
     */
    public static void showWarning(String title, String message, Window owner) {
        new DialogBuilder()
            .create()
            .setOwner(owner)
            .setTitle(title)
            .setWarningIcon()
            .setMessage(message)
            .addOkButton()
                .build()
                    .show();
    }
    
    /**
     * Show warning dialog box
     * 
     * @param title dialog title
     * @param message dialog message
     */
    public static void showWarning(String title, String message) {
        showWarning(title, message, null);
    }

    /**
     * Show error dialog box
     * 
     * @param title dialog title
     * @param message dialog message
     * @param owner parent window
     */
    public static void showError(String title, String message, Window owner) {
        new DialogBuilder()
            .create()
            .setOwner(owner)
            .setTitle(title)
            .setErrorIcon()
            .setMessage(message)
            .addOkButton()
                .build()
                    .show();
    }
    
    /**
     * Show error dialog box
     * 
     * @param title dialog title
     * @param message dialog message
     */
    public static void showError(String title, String message) {
        showError(title, message, null);
    }
    
    /**
     * Show error dialog box with stacktrace
     * 
     * @param title dialog title
     * @param message dialog message
     * @param t throwable
     * @param owner parent window 
     */
    public static void showThrowable(String title, String message, Throwable t, Window owner) {
        new DialogBuilder()
            .create()
            .setOwner(owner)
            .setTitle(title)
            .setThrowableIcon()
            .setMessage(message)
            .setStackTrace(t)
            .addOkButton()
                .build()
                    .show();
    }
    
    /**
     * Show error dialog box with stacktrace
     * 
     * @param title dialog title     
     * @param message dialog message
     * @param t throwable
     */
    public static void showThrowable(String title, String message, Throwable t) {
        showThrowable(title, message, t, null);
    }
    
    /**
     * Build confirmation dialog builder
     * 
     * @param title dialog title     
     * @param message dialog message
     * @param owner parent window
     * @return 
     */
    public static DialogBuilder buildConfirmation(String title, String message, Window owner) {
        return new DialogBuilder()
            .create()
            .setOwner(owner)
            .setTitle(title)
            .setConfirmationIcon()
            .setMessage(message);
    }
    
    /**
     * Build confirmation dialog builder
     * 
     * @param title dialog title     
     * @param message dialog message
     * @return 
     */
    public static DialogBuilder buildConfirmation(String title, String message) {
        return buildConfirmation(title, message, null);
    }
}
