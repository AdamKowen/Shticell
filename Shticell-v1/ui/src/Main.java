import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // טוען את קובץ ה-FXML
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("UIscene.fxml")));

            // יצירת סצנה מה-FXML
            Scene scene = new Scene(root);

            // הגדרת כותרת
            primaryStage.setTitle("Shticell");

            // הגדרת הסצנה
            primaryStage.setScene(scene);


            // טוען את קובץ ה-CSS
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());


            // הגדרת רוחב וגובה מינימלי לחלון
            primaryStage.setMinWidth(800); // גודל מינימלי לרוחב
            primaryStage.setMinHeight(600); // גודל מינימלי לגובה


            // הצגת החלון
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}








//Project part 1
    /*


    PROJECT PART ONE:

    import dto.CellDto;
import dto.SheetDto;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateImpl;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


    public static void main(String[] args) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException {
        //  sheetEngine
        sheetEngine = new SheetEngineImpl();

        // for user input
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // printing menu
            printMenu();

            // input from user
            String choice = scanner.nextLine();

            if (!sheetEngine.isSheetLoaded()) {
                switch (choice) {
                    case "1":
                        loadSheet(scanner);
                        break;
                    case "2":
                        System.out.println("Exiting the system.");
                        scanner.close();
                        return; // יוצא מהתוכנית
                    default:
                        System.out.println("Invalid choice. Please select either 1 or 2.");
                }
            } else {
                switch (choice) {
                    case "1":
                        loadSheet(scanner);
                        break;
                    case "2":
                        displaySheet();
                        break;
                    case "3":
                        displayCellValue(scanner);
                        break;
                    case "4":
                        updateCellValue(scanner);
                        break;
                    case "5":
                        displayVersions(scanner);
                        break;
                    case "6":
                        System.out.println("Exiting the system.");
                        scanner.close();
                        return; // exiting
                    default:
                        System.out.println("Invalid choice. Please select a valid option.");
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println("\nSelect an option:");
        if (!sheetEngine.isSheetLoaded()) {
            System.out.println("1. Load sheet from XML file");
            System.out.println("2. Exit");
        } else {
            System.out.println("1. Load sheet from XML file");
            System.out.println("2. Display current sheet");
            System.out.println("3. Display a single cell value");
            System.out.println("4. Update a single cell value");
            System.out.println("5. Display versions");
            System.out.println("6. Exit");
        }
        System.out.print("Your choice: ");
    }

    private static void loadSheet(Scanner scanner) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException {
        boolean success = false; // checking if loading success

        while (!success) {
            System.out.print("Enter the full path to the XML file (or type '1' to go back): ");
            String filePath = scanner.nextLine();

            // checking if user wants out
            if (filePath.equals("1")) {
                System.out.println("Returning to the previous menu.");
                return; // back
            }

            try {
                sheetEngine.loadSheetFromXML(filePath);
                System.out.println("Sheet loaded successfully.");
                success = true; // exit loop if success
            } catch (Exception e) {
                System.out.println("Failed to load sheet: " + e.getMessage());
                System.out.println("Please try again or type '1' to go back.");
            }
        }
    }

    private static void displaySheet() {
        System.out.println("Displaying current sheet:");
        SheetDto sheetDTO = sheetEngine.getCurrentSheetDTO();
        printSheet(sheetDTO);
    }

    public static void printSheet(SheetDto sheetDto) {
        int numOfColumns = sheetDto.getNumOfColumns();
        int numOfRows = sheetDto.getNumOfRows();
        int columnWidth = sheetDto.getColumnUnits(); // assume this is the number of characters for each column
        Map<Coordinate, CellDto> cellsInSheet = sheetDto.getSheet();

        // Print the header for columns
        System.out.print("\t|"); // Space for row numbers and leading pipe
        for (int col = 1; col <= numOfColumns; col++) {
            char columnLabel = (char) ('A' + col - 1);
            System.out.print(centerText(Character.toString(columnLabel), columnWidth) + "|");
        }
        System.out.println();

        // Print rows with row numbers and cell values
        for (int row = 1; row <= numOfRows; row++) {
            // Print the row label
            System.out.print(String.format("%02d", row) + "\t|");

            for (int col = 1; col <= numOfColumns; col++) {
                Coordinate coord = new CoordinateImpl(row, col);
                CellDto cell = cellsInSheet.get(coord);

                if (cell != null) {
                    String value = cell.getValue();
                    if (value != null) {
                        System.out.print(padRight(truncateText(value, columnWidth), columnWidth) + "|");
                    } else {
                        System.out.print(padRight("", columnWidth) + "|");
                    }
                } else {
                    System.out.print(padRight("", columnWidth) + "|");
                }
            }
            System.out.println();
        }

        // Print sheet information
        System.out.println("Sheet Name: " + sheetDto.getName());
        System.out.println("Sheet Version: " + sheetDto.getVersion());
    }

    // cutting text if there is no space
    private static String truncateText(String text, int width) {
        if (text.length() > width) {
            return text.substring(0, width - 3) + "...";
        } else {
            return text;
        }
    }

    // adjusting the padding for text in col
    private static String padRight(String text, int width) {
        return String.format("%-" + width + "s", text);
    }

    // centers text
    private static String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        String format = "%" + padding + "s%s%" + padding + "s";
        return String.format(format, "", text, "");
    }

    private static void displayCellValue(Scanner scanner) {
        while (true) {
            System.out.print("Enter the cell (e.g., A1) or '1' to go back: ");
            String str = scanner.nextLine();

            // checks if want back
            if (str.equals("1")) {
                System.out.println("Returning to the previous menu.");
                break;
            }

            try {
                CellDto currCell = sheetEngine.getCellDTO(str);

                if (currCell != null) {
                    // printing cell value
                    System.out.println("Coordinate: " + formatCoordinate(currCell.getCoordinate()));
                    System.out.println("Original Value: " + currCell.getOriginalValue());

                    String effValue = currCell.getValue();

                    if (effValue != null)
                    {
                        System.out.println("Effective Value: " + effValue);
                    }
                    else {
                        System.out.println("Effective Value: Empty - No Value");
                    }


                    System.out.println("Version: " + currCell.getVersion());

                    // printing dependencis
                    List<Coordinate> dependentCells = currCell.getDependsOn();
                    if (dependentCells != null && !dependentCells.isEmpty()) {
                        System.out.println("Dependent Cells:");
                        for (Coordinate coord : dependentCells) {
                            System.out.println(formatCoordinate(coord));
                        }
                    } else {
                        System.out.println("No dependent cells.");
                    }

                    // printing influenced cells
                    List<Coordinate> influencedCells = currCell.getInfluencingOn();
                    if (influencedCells != null && !influencedCells.isEmpty()) {
                        System.out.println("Influenced Cells:");
                        for (Coordinate coord : influencedCells) {
                            System.out.println(formatCoordinate(coord));
                        }
                    } else {
                        System.out.println("No influenced cells.");
                    }

                    break; // exiting loop

                } else {
                    System.out.println("Cell is Empty."); //update with static info! cell never created/existed in the first place. so no one is depended on it or ever was dep
                }
            } catch (Exception e) {
                // תפיסת חריגות כלליות
                System.out.println("An error occurred: " + e.getMessage());
                System.out.println("Please enter a valid cell or '1' to go back.");
            }
        }
    }

    // Helper function to format coordinates as "A1", "B2", etc.
    private static String formatCoordinate(Coordinate coord) {
        int column = coord.getColumn();
        int row = coord.getRow();
        // Convert column number to letter (e.g., 1 -> A, 2 -> B)
        char columnLetter = (char) ('A' + column - 1);
        return columnLetter + String.valueOf(row);
    }

    private static void updateCellValue(Scanner scanner) {
        while (true) {
            System.out.print("Enter the cell to update (e.g., A1) or '1' to go back: ");
            String cellStr = scanner.nextLine();

            // if wants back
            if (cellStr.equals("1")) {
                System.out.println("Returning to the previous menu.");
                break;
            }

            try {

                if (sheetEngine.isCoordinateInRange(cellStr)) //if in range
                {
                    if(!sheetEngine.isCellEmpty(cellStr))
                    {
                        // will get cell it exists
                        CellDto cell = sheetEngine.getCellDTO(cellStr);

                        System.out.println("Current original value of " + cellStr + " coordinate: " + cell.getOriginalValue());


                    }

                    System.out.print("Enter the new value: ");
                    String newValue = scanner.nextLine();

                    // updates
                    sheetEngine.updateCellValue(cellStr, newValue);
                    System.out.println("Cell " + cellStr + " updated successfully.");
                    break;
                }

                else
                {
                    CellDto cell = sheetEngine.getCellDTO(cellStr); //will throw valid exception
                }

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please try again.");
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private static void displayVersions(Scanner scanner) {
        System.out.println("Displaying versions:");

        // gets num of changes for every version
        List<Integer> changes = sheetEngine.getNumChangedCellsInAllVersions();

        for (int i = 0; i < changes.size(); i++) {
            System.out.println("Version " + (i + 1) + ": " + changes.get(i) + " changes");
        }

        while (true) {
            System.out.print("Enter a version number to view (or type '0' to return): ");
            String input = scanner.nextLine();

            if (input.equals("0")) {
                break;
            }

            try {
                int version = Integer.parseInt(input);

                if (version < 1 || version > changes.size()) {
                    System.out.println("Invalid version number. Please choose a valid version.");
                } else {
                    printSheet(sheetEngine.getVersionDto(version));
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid version number.");
            }
        }
    }

    }



//      /Users/adamkowen/Documents/Java/Shticell P1/costume.xml


     */

