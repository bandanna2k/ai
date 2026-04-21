package dnt.localagentapp;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/** Returns the current date and time in ISO-8601 format. */
class ClockTool implements Tool {

    @Override public String name() { return "get_time"; }

    @Override public String description() {
        return "get_time(<empty>) — returns the current date and time. Use when the user asks about the time or date.";
    }

    @Override public String execute(String argument) {
        return ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}

/**
 * Evaluates a simple arithmetic expression and returns the result.
 * Uses Nashorn/GraalJS (bundled with the JDK) so no extra deps are needed.
 */
class CalculatorTool implements Tool {

    @Override public String name() { return "calculate"; }

    @Override public String description() {
        return "calculate(<expression>) — evaluates a math expression and returns the numeric result. "
                + "Examples: calculate(2 + 2), calculate(100 / 7), calculate(Math.sqrt(144)). "
                + "Use for any arithmetic the user needs.";
    }

    @Override public String execute(String expression) {
        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            if (engine == null) {
                // Fallback: very basic four-op evaluator (no JS engine on this JVM)
                return String.valueOf(simpleEval(expression.trim()));
            }
            Object result = engine.eval(expression);
            return String.valueOf(result);
        } catch (Exception e) {
            throw new RuntimeException("Could not evaluate expression '" + expression + "': " + e.getMessage());
        }
    }

    /** Minimal fallback parser for expressions like "3 + 4 * 2". */
    private double simpleEval(String expr) {
        // Strip whitespace and delegate to a tiny recursive descent parser
        return new Object() {
            int pos = 0;
            double parse() {
                double val = parseTerm();
                while (pos < expr.length()) {
                    char op = expr.charAt(pos);
                    if (op == '+') { pos++; val += parseTerm(); }
                    else if (op == '-') { pos++; val -= parseTerm(); }
                    else break;
                }
                return val;
            }
            double parseTerm() {
                double val = parseFactor();
                while (pos < expr.length()) {
                    char op = expr.charAt(pos);
                    if (op == '*') { pos++; val *= parseFactor(); }
                    else if (op == '/') { pos++; val /= parseFactor(); }
                    else break;
                }
                return val;
            }
            double parseFactor() {
                while (pos < expr.length() && expr.charAt(pos) == ' ') pos++;
                if (pos < expr.length() && expr.charAt(pos) == '(') {
                    pos++; double val = parse(); pos++; return val; // skip ')'
                }
                int start = pos;
                if (pos < expr.length() && expr.charAt(pos) == '-') pos++;
                while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) pos++;
                return Double.parseDouble(expr.substring(start, pos));
            }
        }.parse();
    }
}