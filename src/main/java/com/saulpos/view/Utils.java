/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.view;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Utils {

    // Utils.goForward(destinationReference, sourceReference);
    public static void goForward(final AbstractView viewDef,
                                 final Pane mainPane) throws IOException {
        goTo(viewDef, mainPane, mainPane.getScene().getWidth(), .0, true);
    }

    // Utils.goForward(destinationReference, sourceReference);
    public static void goBack(final AbstractView viewDef,
                       final Pane mainPane) throws IOException {
        goTo(viewDef, mainPane, (mainPane.getScene().getWidth()) * (-1), .0, false);
    }

    public static void goBackRemove(final AbstractView viewDef,
                             final Class classInstance, final Pane mainPane) throws IOException {
        goTo(viewDef, mainPane,.0, mainPane.getScene().getWidth(), true);
    }

    public static void goForwardRemove(final AbstractView viewDef,
                                final Pane mainPane) throws IOException {
        goTo(viewDef, mainPane, mainPane.getScene().getWidth(), .0, true);
    }

    public static void goTo(final AbstractView viewDef,
                     final Pane mainPane, final double from,
                     final double to, final boolean remove) throws IOException {
        Parent rootPane = mainPane;
        while ( rootPane != null && !(rootPane instanceof ParentPane) ){
            rootPane = rootPane.getParent();
        }

        final Pane rootPaneFinal = (Pane) rootPane;

        final ArrayList<Node> childrenToRemove = new ArrayList<>(rootPaneFinal.getChildren());
        if ( remove ){
            rootPaneFinal.getChildren().removeAll(childrenToRemove);
        }
        final Node root = viewDef.getRoot();

        AnchorPane.setBottomAnchor(root, .0);
        AnchorPane.setLeftAnchor(root, .0);
        AnchorPane.setRightAnchor(root, .0);
        AnchorPane.setTopAnchor(root, .0);


        //Set X of second scene to Height of window
        root.translateXProperty().set(from);
        //Add second scene. Now both first and second scene is present
        rootPaneFinal.getChildren().add(root);

        //Create new TimeLine animation
        Timeline timeline = new Timeline();
        //Animate Y property
        KeyValue kv = new KeyValue(root.translateXProperty(), to, Interpolator.EASE_IN);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    // Get the cashier name from cashier.txt file. If not exist, then create.
    public static String getCashierName(){
        String userDirectoryStr = System.getProperty("user.home");
        String fileDirectoryStr = "SaulPOS";
        String fileNameStr = "cashier.txt";

        String filePath = userDirectoryStr + File.separator + fileDirectoryStr + File.separator + fileNameStr;
        File file = new File(filePath);
        if (!file.exists()) {
            //create that file and write data
            String directoryStr = createDirectory(userDirectoryStr, fileDirectoryStr);
            if (!directoryStr.isEmpty()) {
                String filePathStr = createFile(directoryStr, fileNameStr);
                if (!filePathStr.isEmpty()) {
                    writeCashierData(filePathStr);
                }
            }
        }
        return readCashierData(filePath);
    }

    // Create directory with given base directory & subdirectory
    private static String createDirectory(String baseDirectory, String subDirectory) {
        File targetDirectory = new File(baseDirectory + File.separator + subDirectory);
        if(targetDirectory.exists()) {
            System.out.println("Directory exists!");
            return targetDirectory.getAbsolutePath();
        }else {
            boolean status = targetDirectory.mkdirs();
            if(status) {
                System.out.println("New directory created!");
                return targetDirectory.getAbsolutePath();
            }else {
                return "";
            }
        }
    }

    // Create file with given base directory & filename
    private static String createFile(String baseDirectory, String fileName) {
        try {
            File createdFile = new File(baseDirectory + File.separator + fileName);
            if(createdFile.createNewFile()) {
                System.out.println("New file created!");
            }else {
                System.out.println("File already exist!");
            }
            return createdFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Write cashier data to a given file path
    private static void writeCashierData(String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
            String createdAt = "createdAt=" + LocalDateTime.now();
            writer.write(createdAt);
            writer.append("\n");
            writer.append("cashier=").append(getComputerName());
            System.out.println("Status: File written done!");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Status: File written failed!");
        }
    }

    // Read current computer name
    private static String getComputerName() {
        //Using System...
        String osName = System.getProperty("os.name").toLowerCase();
        String computerName;
        if (osName.contains("windows")) {
            computerName = System.getenv("COMPUTERNAME");
        } else if (osName.contains("linux") || osName.contains("unix")) {
            computerName = System.getenv("HOSTNAME");
        } else if (osName.contains("mac")) {
            computerName = System.getenv("COMPUTERNAME");
        } else {
            computerName = "Unknown";
        }
        System.out.println("Current computer name: " + computerName);
        return computerName;
    }

    // Read cashier data from a given file path
    private static String readCashierData(String filePath) {
        try {
            FileReader reader = new FileReader(filePath);
            int ch;
            StringBuilder stringBuilder = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                stringBuilder.append((char) ch);
            }
            String data = stringBuilder.toString();
            System.out.println("File data:\n" + data);
            String result = data.substring(data.lastIndexOf('=') +1);
            System.out.println(result);
            return result;
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Status: Unable to read file!");
            return "";
        }
    }
}
