package expression.parser;

import expression.api.Expression;
import expression.impl.*;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;
import sheet.coordinate.impl.CoordinateImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static sheet.coordinate.impl.CoordinateCache.createCoordinate;

public enum FunctionParser {
    IDENTITY {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for IDENTITY function. Expected 1, but got " + arguments.size());
            }

            // all is good. create the relevant function instance
            String actualValue = arguments.get(0).trim();
            if (isBoolean(actualValue)) {
                return new IdentityExpression(Boolean.parseBoolean(actualValue), CellType.BOOLEAN);
            } else if (isNumeric(actualValue)) {
                return new IdentityExpression(Double.parseDouble(actualValue), CellType.NUMERIC);
            } else {
                if(actualValue.isEmpty()) //for empty cell
                {
                    return new IdentityExpression(actualValue, CellType.UNKNOWN);
                }
                return new IdentityExpression(actualValue, CellType.STRING);
            }

        }

        private boolean isBoolean(String value) {
            return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
        }

        private boolean isNumeric(String value) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    },
    AVERAGE {
        @Override
        public Expression parse(List<String> arguments) {
            String rangeName = (arguments.get(0).trim());
            // Create and return the AVERAGE expression
            return new AverageExpression(rangeName);
        }
    },
    BIGGER {
        @Override
        public Expression parse(List<String> arguments) {
            // Validations: there should be exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for BIGGER function. Expected 2, but got " + arguments.size());
            }

            // Parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // Create and return the BIGGER expression
            return new BiggerExpression(left, right);
        }
    },
    PLUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function (e.g. number of arguments)
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PLUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            return new PlusExpression(left, right);
        }
    },
    EQUAL {
        @Override
        public Expression parse(List<String> arguments) {
            // Validations: there should be exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for EQUAL function. Expected 2, but got " + arguments.size());
            }

            // Parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // Create and return the EQUAL expression
            return new EqualExpression(left, right);
        }
    },
    IF {
        @Override
        public Expression parse(List<String> arguments) {
            // Validations: there should be exactly three arguments
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for IF function. Expected 3, but got " + arguments.size());
            }

            // Parse arguments
            Expression condition = parseExpression(arguments.get(0).trim());
            Expression thenExpr = parseExpression(arguments.get(1).trim());
            Expression elseExpr = parseExpression(arguments.get(2).trim());


            // Create and return the IF expression
            return new IfExpression(condition, thenExpr, elseExpr);
        }
    },
    LESS {
        @Override
        public Expression parse(List<String> arguments) {
            // Validations: there should be exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for LESS function. Expected 2, but got " + arguments.size());
            }

            // Parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());


            // Create and return the LESS expression
            return new LessExpression(left, right);
        }
    },
    NOT {
        @Override
        public Expression parse(List<String> arguments) {
            // Validations: there should be exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for NOT function. Expected 1, but got " + arguments.size());
            }

            // Parse the argument
            Expression expr = parseExpression(arguments.get(0).trim());



            // Create and return the NOT expression
            return new NotExpression(expr);
        }
    },
    OR {
        @Override
        public Expression parse(List<String> arguments) {
            // Validations: there should be exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for OR function. Expected 2, but got " + arguments.size());
            }

            // Parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // Create and return the OR expression
            return new OrExpression(left, right);
        }
    },
    PERCENT {
        @Override
        public Expression parse(List<String> arguments) {
            // Validations: there should be exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PERCENT function. Expected 2, but got " + arguments.size());
            }

            // Parse arguments
            Expression part = parseExpression(arguments.get(0).trim());
            Expression whole = parseExpression(arguments.get(1).trim());

            // Create and return the PERCENT expression
            return new PercentExpression(part, whole);
        }
    },
    UPPER_CASE {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for UPPER_CASE function. Expected 1, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression arg = parseExpression(arguments.get(0).trim());

            // all is good. create the relevant function instance
            return new UpperCaseExpression(arg);
        }
    },
    SUM {
        @Override
        public Expression parse(List<String> arguments) {
            // SUM can take a list of arguments, so no need for specific number validation

            String rangeName = (arguments.get(0).trim());
            // Create and return the SUM expression
            return new SumExpression(rangeName);
        }
    },
    MINUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MINUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());


            // all is good. create the relevant function instance
            return new MinusExpression(left, right);
        }
    },
    AND {
        @Override
        public Expression parse(List<String> arguments) {
            // Validations: there should be exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for AND function. Expected 2, but got " + arguments.size());
            }

            // Parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // Create and return the AND expression
            return new AndExpression(left, right);
        }
    },
    CONCAT {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for CONCAT function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // all is good. create the relevant function instance
            return new ConcatExpression(left, right);
        }
    },
    REF {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for REF function. Expected 1, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Coordinate coordinate = CoordinateCache.createCoordinateFromString(arguments.get(0).trim());

            // more validations on the expected argument types

            // all is good. create the relevant function instance
            return new RefExpression(coordinate);
        }
    },
    ABS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for ABS function. Expected 1, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression arg = parseExpression(arguments.get(0).trim());

            // all is good. create the relevant function instance
            return new AbsExpression(arg);
        }
    },
    DIVIDE {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for DIVIDE function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());


            // all is good. create the relevant function instance
            return new DivideExpression(left, right);
        }
    },
    MOD {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MOD function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());


            // all is good. create the relevant function instance
            return new ModExpression(left, right);
        }
    },
    POW {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for POW function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // all is good. create the relevant function instance
            return new PowExpression(left, right);
        }
    }
    ,
    TIMES {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for TIMES function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // all is good. create the relevant function instance
            return new TimesExpression(left, right);
        }
    },
    SUB {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for SUB function. Expected 3, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression source = parseExpression(arguments.get(0).trim());
            Expression startIndex = parseExpression(arguments.get(1).trim());
            Expression endIndex = parseExpression(arguments.get(2).trim());


            // all is good. create the relevant function instance
            return new SubExpression(source, startIndex, endIndex);
        }
    }






    ;
    //
    abstract public Expression parse(List<String> arguments);

    public static Expression parseExpression(String input) {

        if (input.startsWith("{") && input.endsWith("}")) {

            String functionContent = input.substring(1, input.length() - 1);
            List<String> topLevelParts = parseMainParts(functionContent);


            String functionName = topLevelParts.get(0).trim().toUpperCase();

            //remove the first element from the array
            topLevelParts.remove(0);

            // בדיקה אם שם הפונקציה קיים
            if (!functionExists(functionName)) {
                throw new IllegalArgumentException("Function name '" + functionName + "' does not exist.");
            }


            return FunctionParser.valueOf(functionName).parse(topLevelParts);
        }

        // handle identity expression
        return FunctionParser.IDENTITY.parse(List.of(input.trim()));
    }


    // פונקציה שבודקת אם הפונקציה קיימת ב-Enum
    private static boolean functionExists(String functionName) {
        for (FunctionParser func : FunctionParser.values()) {
            if (func.name().equals(functionName)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> parseMainParts(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char c : input.toCharArray()) {
            if (c == '{') {
                stack.push(c);
            } else if (c == '}') {
                stack.pop();
            }

            if (c == ',' && stack.isEmpty()) {
                // If we are at a comma and the stack is empty, it's a separator for top-level parts
                parts.add(buffer.toString().trim());
                buffer.setLength(0); // Clear the buffer for the next part
            } else {
                buffer.append(c);
            }
        }

        // Add the last part
        if (buffer.length() > 0) {
            parts.add(buffer.toString().trim());
        }

        return parts;
    }
    public static Coordinate createCoordinateFromString(String trim) {
        try {
            // חילוץ מספרים ואותיות מהקלט
            String columnPart = trim.replaceAll("[^A-Z]", ""); // מוצא את כל האותיות
            String rowPart = trim.replaceAll("[^0-9]", "");    // מוצא את כל המספרים

            // המרת האות לעמודה, לדוגמה: A=1, B=2, וכו'
            int column = columnPart.charAt(0) - 'A' + 1;

            // המרת מחרוזת השורה למספר
            int row = Integer.parseInt(rowPart);

            return new CoordinateImpl(row, column);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void main(String[] args) {

    }



}
//