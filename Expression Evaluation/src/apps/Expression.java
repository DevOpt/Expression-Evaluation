package apps;


import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

    /**
     * Expression to be evaluated
     */
    String expr;

    /**
     * Scalar symbols in the expression
     */
    ArrayList<ScalarSymbol> scalars;

    /**
     * Array symbols in the expression
     */
    ArrayList<ArraySymbol> arrays;

    /**
     * String containing all delimiters (characters other than variables and constants),
     * to be used with StringTokenizer
     */
    public static final String delims = " \t*+-/()[]";

    /**
     * Initializes this Expression object with an input expression. Sets all other
     * fields to null.
     *
     * @param expr Expression
     */
    public Expression(String expr) {
        this.expr = expr;
    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every variable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() {
        this.scalars = new ArrayList<ScalarSymbol>();
        this.arrays = new ArrayList<ArraySymbol>();
        String temp = "";
        for(int i = 0; i < this.expr.length(); i++){
            if(!Character.isDigit(expr.charAt(i))){
                temp += expr.charAt(i);
            }
        }

        StringTokenizer st = new StringTokenizer(temp, " \t*+-/()]");
        while (st.hasMoreTokens()){
            String token = st.nextToken();
            if(token.contains("[")){
                arrays.add(new ArraySymbol(token.replace("[", "")));
            }else{
                scalars.add(new ScalarSymbol(token));
            }
        }
        //System.out.println(arrays);
        //System.out.println(scalars);
    }

    /**
     * Loads values for symbols in the expression
     *
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input
     */
    public void loadSymbolValues(Scanner sc)
            throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String sym = st.nextToken();
            ScalarSymbol ssymbol = new ScalarSymbol(sym);
            ArraySymbol asymbol = new ArraySymbol(sym);
            int ssi = scalars.indexOf(ssymbol);
            int asi = arrays.indexOf(asymbol);
            if (ssi == -1 && asi == -1) {
                continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                scalars.get(ssi).value = num;
            } else { // array symbol
                asymbol = arrays.get(asi);
                asymbol.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    asymbol.values[index] = val;
                }
            }
        }
    }

    /**
     * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array
     * subscript expressions.
     *
     * @return Result of evaluation
     */
    public float evaluate() {
        boolean isExpression = false;
        for(int i = 0; i < this.expr.length(); i++){
            if(Character.isDigit(this.expr.charAt(i)) || Character.isLetter(this.expr.charAt(i))){
                isExpression = true;
            }else
                continue;
        }
        if(!isExpression)
            return 0;
        else
            return Float.parseFloat(evaluate(this.expr));
    }

    private String evaluate(String e){
        String express = e;
        for(int i = 0; i < this.scalars.size(); i++){
            express = express.replace(this.scalars.get(i).name, "" + this.scalars.get(i).value);
        }

        Stack<String> orator = new Stack<String>();
        Stack<String> operand = new Stack<String>();
        StringTokenizer st = new StringTokenizer(express+" ", delims, true);
        while(st.countTokens() !=0 || operand.size() > 1){

            if (st.countTokens() !=0) {
                String token = st.nextToken();
                if(token.matches("[0-9]+") || token.equals("*") || token.equals("/") || token.equals("+") || token.equals("-")){
                    if(token.matches("[0-9]+")){
                        operand.push(token);
                    }else{
                        if(orator.isEmpty()){
                            orator.push(token);
                        }else{
                            if(hasPrecedence(orator.peek()) >= hasPrecedence(token)){
                                String operator = orator.pop();
                                orator.push(token);
                                float o2 = Float.parseFloat(operand.pop());
                                float o1 = Float.parseFloat(operand.pop());
                                String result = compute(o1, o2, operator);
                                operand.push(result);
                            }else{
                                orator.push(token);
                            }
                        }
                    }
                }else if(token.equals("(") || token.equals("[")){
                    String temp = new StringBuilder(express).reverse().toString();
                    if(token.equals("(")){
                        int countIndex= 0;
                        for (int i=0; i<express.length(); i++){
                            if (express.charAt(i) == ')'){
                                countIndex = i;
                            }
                        }
                        String res = evaluate(express.substring(express.indexOf("(") + 1, countIndex));
                        operand.push(res);
                        for (int i=0; i<express.indexOf(")") - express.indexOf("("); i++){
                            if (st.hasMoreTokens()){
                                token = st.nextToken();
                            }
                        }
                    }else{
                        int countIndex= 0;
                        for (int i=0; i<express.length(); i++){
                            if (express.charAt(i) == ']'){
                                countIndex = i;
                            }
                        }
                        String res = evaluate(express.substring(express.indexOf("[") + 1, countIndex));
                        ArraySymbol a;
                        a = arrays.get(0);
                        int index = a.values[Integer.parseInt(String.valueOf(res.charAt(0)))];
                        operand.push(Integer.toString(index));
                        for (int i=0; i< express.indexOf("]") - express.indexOf("["); i++){
                            if (st.hasMoreTokens()){
                                token = st.nextToken();
                            }
                        }
                    }
                }
            }else if (operand.size() >= 1 && orator.size() >= 1){
                String operators = orator.pop();
                float o2 = Float.parseFloat(operand.pop());
                float o1 = Float.parseFloat(operand.pop());
                String result = compute(o1, o2, operators);
                operand.push(result);
            }else {
                if (st.hasMoreTokens()){
                    continue;
                }else {
                    break;
                }
            }
        }
        return operand.pop();
    }
    private String compute(float n1, float n2, String operator){
        if(operator.equals("*")){
            return n1 * n2 + "";
        }else if(operator.equals("/")){
            return n1 / n2 + "";
        }else if(operator.equals("+")){
            return n1 + n2 + "";
        }else if(operator.equals("-")){
            return n1 - n2 + "";
        }else{
            return null;
        }
    }
    private int hasPrecedence(String t){
        if(t.equals("*") || t.equals("/")){
            return 1;
        }else
            return 0;
    }

    /**
     * Utility method, prints the symbols in the scalars list
     */
    public void printScalars() {
        for (ScalarSymbol ss: scalars) {
            System.out.println(ss);
        }
    }

    /**
     * Utility method, prints the symbols in the arrays list
     */
    public void printArrays() {
        for (ArraySymbol as: arrays) {
            System.out.println(as);
        }
    }
}
