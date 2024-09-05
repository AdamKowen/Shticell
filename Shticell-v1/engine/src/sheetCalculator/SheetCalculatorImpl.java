package sheetCalculator;


import sheet.api.Sheet;
import sheet.api.SheetCalculator;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.api.Coordinate;

import java.util.*;

public class SheetCalculatorImpl implements SheetCalculator {

    private Map<Coordinate, Cell> cellsInSheet;
    private Map<Coordinate, List<Coordinate>> dependencyGraph;
    private List<Cell> newEmptyAddedCells;
    int versionCalculated;

    public SheetCalculatorImpl(Sheet sheet) {
        versionCalculated = sheet.getVersion() + 1;
        this.cellsInSheet = sheet.getSheet();
        this.dependencyGraph = new HashMap<>();
        this.newEmptyAddedCells = new ArrayList<>();
        buildDependencyGraph();

        List<Cell> newlist = calculateEvaluationOrder();
        //calculating everything. after this if no exeption thrown, can continue to saving lists
        for (Cell cell : newlist) {
            cell.calculateEffectiveValue(sheet);
        }


        //clearing all influencing cells and Dependent cells lists
        for (Map.Entry<Coordinate, Cell> entry : cellsInSheet.entrySet()) {
            Cell cell = entry.getValue(); // קבלת ה-Cell מהמפה

            // קבלת רשימת ה-influenced cells וריקונה
            List<Coordinate> influencedCells = cell.getInfluencedCells();
            influencedCells.clear(); // ריקון הרשימה

            // קבלת רשימת ה-influenced cells וריקונה
            List<Coordinate> DependentCells = cell.getDependentCells();
            DependentCells.clear(); // ריקון הרשימה
        }


        //saving all the new dep lists, and inside also adds to the lists of influencing we just cleared
        for (Map.Entry<Coordinate, List<Coordinate>> entry : dependencyGraph.entrySet()) {
            Coordinate coord = entry.getKey(); // המפתח: הקואורדינטה של התא התלוי
            List<Coordinate> newDependentCells = entry.getValue(); // הערך: רשימת התאים שעליהם התא תלוי

            // קבלת ה-Cell המתאים מהמפה cellsInSheet לפי הקואורדינטה
            Cell cell = cellsInSheet.get(coord);
            if (cell != null) {
                cell.getDependentCells().clear();
                cell.getDependentCells().addAll(newDependentCells);
                //cell.setDependentCells(newDependentCells); // הגדרת רשימה חדשה עם התאים התלויים
            }


            //  run on list cells that curr cell is dependent on
            for (Coordinate dependency : newDependentCells) {
                // get it from map
                Cell influencingCell = cellsInSheet.get(dependency);

                if (influencingCell != null) {
                    // add the curr cell to lists of cells who are influencing on it
                    influencingCell.addInfluencedCell(cell.getCoordinate());
                }
                else
                {
                    Cell EmptyCell = new CellImpl(dependency.getRow(), dependency.getColumn(), "", versionCalculated);
                    EmptyCell.calculateEffectiveValue(sheet);
                    newEmptyAddedCells.add(EmptyCell); //puts in sheet
                    EmptyCell.addInfluencedCell(cell.getCoordinate());
                }
            }

        }

        //adds all newly created empty cells to Sheet
        for (Cell item : newEmptyAddedCells) {
            sheet.setCell(item.getCoordinate(),item);
        }
    }

    private void buildDependencyGraph() {
        for (Map.Entry<Coordinate, Cell> entry : cellsInSheet.entrySet()) {
            Coordinate coord = entry.getKey();
            Cell cell = entry.getValue();
            if (cellContainsReference(cell)) {
                List<Coordinate> dependencies = getDependencies(cell);
                dependencyGraph.put(coord, dependencies);
            }
        }
    }

    private boolean cellContainsReference(Cell cell) {
        // checks if has ref in func
        return cell.doesContainRef();
    }

    private List<Coordinate> getDependencies(Cell cell) {
        List<Coordinate> dependencies= new ArrayList<>();
        cell.getExpression().collectDependencies(dependencies);
        return dependencies;
    }

    @Override
    public List<Cell> calculateEvaluationOrder() throws IllegalArgumentException {
        List<Cell> evaluationOrder = new ArrayList<>(); //empty cells first, then others will be added
        Set<Coordinate> visited = new HashSet<>();
        Set<Coordinate> recStack = new HashSet<>();

        // gets order of cells dependent of each other
        for (Coordinate coord : dependencyGraph.keySet()) {
            if (!visited.contains(coord)) {
                if (!dfs(coord, visited, recStack, evaluationOrder)) {
                    throw new IllegalArgumentException("Circular dependency detected!");
                }
            }
        }

        // all the others who are not depentend
        for (Coordinate coord : cellsInSheet.keySet()) {
            Cell cell = cellsInSheet.get(coord);
            if (!visited.contains(coord)) {
                evaluationOrder.add(cell);
            }
        }

        return evaluationOrder;
    }

    private boolean dfs(Coordinate coord, Set<Coordinate> visited, Set<Coordinate> recStack, List<Cell> evaluationOrder) {
        if (recStack.contains(coord)) {
            return false; // denected circle
        }

        if (!visited.contains(coord)) {
            visited.add(coord);
            recStack.add(coord);

            List<Coordinate> dependencies = dependencyGraph.getOrDefault(coord, new ArrayList<>());
            for (Coordinate dep : dependencies) {
                if (!dfs(dep, visited, recStack, evaluationOrder)) {
                    return false;
                }
            }

            recStack.remove(coord);
            Cell cell = cellsInSheet.get(coord);
            if (cell != null) {
                evaluationOrder.add(cell);
            }
        }

        return true;
    }

}




