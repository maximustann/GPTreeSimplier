package abstractexpressions.expression.classes;

import abstractexpressions.expression.computation.ArithmeticUtils;
import computationbounds.ComputationBounds;
import enums.TypeExpansion;
import enums.TypeSimplify;
import exceptions.EvaluationException;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyAlgebraicExpressionUtils;
import abstractexpressions.expression.basic.SimplifyBinaryOperationUtils;
import abstractexpressions.expression.basic.SimplifyExpLogUtils;
import abstractexpressions.expression.basic.SimplifyFunctionUtils;
import abstractexpressions.expression.basic.SimplifyFunctionalRelationsUtils;
import abstractexpressions.expression.basic.SimplifyPolynomialUtils;
import abstractexpressions.expression.basic.SimplifyUtilities;
import enums.TypeFractionSimplification;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import lang.translator.Translator;

public class BinaryOperation extends Expression {

    private final Expression left, right;
    private final TypeBinary type;

    private static final HashSet<TypeSimplify> simplifyTypesExpandAndCollectIfShorter = getSimplifyTypesExpandAndCollectIfShorter();

    private static HashSet<TypeSimplify> getSimplifyTypesExpandAndCollectIfShorter() {
        /*
         Als Vereinfachungstyp darf NICHT 
         simplify_expand_and_collect_equivalents_if_shorter verwendet werden.
         */
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        return simplifyTypes;
    }

    public BinaryOperation(Expression left, Expression right, TypeBinary type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public TypeBinary getType() {
        return this.type;
    }

    public Expression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }

    @Override
    public Expression copy() {
        return new BinaryOperation(this.left, this.right, this.type);
    }

    @Override
    public double evaluate() throws EvaluationException {

        double valueLeft = this.left.evaluate();
        double valueRight = this.right.evaluate();

        if (Double.isNaN(valueLeft) || Double.isInfinite(valueLeft) || Double.isNaN(valueRight) || Double.isInfinite(valueRight)) {
            throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
        }

        switch (type) {
            case PLUS:
                if (Double.isNaN(valueLeft + valueRight) || Double.isInfinite(valueLeft + valueRight)) {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                }
                return valueLeft + valueRight;
            case MINUS:
                if (Double.isNaN(valueLeft - valueRight) || Double.isInfinite(valueLeft - valueRight)) {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                }
                return valueLeft - valueRight;
            case TIMES:
                if (Double.isNaN(valueLeft * valueRight) || Double.isInfinite(valueLeft * valueRight)) {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                }
                return valueLeft * valueRight;
            case DIV:
                if ((!Double.isNaN(valueLeft / valueRight)) && (!Double.isInfinite(valueLeft / valueRight))) {
                    if (Double.isNaN(valueLeft / valueRight) || Double.isInfinite(valueLeft / valueRight)) {
                        throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                    }
                    return valueLeft / valueRight;
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_DIVISION_BY_ZERO"));
                }
            case POW:
                // Abfangen von Wurzeln ungerader Ordnung aus negativen Zahlen.
                if (valueLeft < 0 && this.right.isRationalConstant() && ((BinaryOperation) this.right).getRight().isOddIntegerConstant()) {
                    double result;
                    if (((BinaryOperation) this.right).getLeft().isOddIntegerConstant()) {
                        result = -Math.pow(-valueLeft, valueRight);
                    } else {
                        result = Math.pow(-valueLeft, valueRight);
                    }
                    if (!Double.isNaN(result) && !Double.isInfinite(result)) {
                        return result;
                    } else {
                        throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                    }
                }
                // Dann ganz normal weiter.
                if (!Double.isNaN(Math.pow(valueLeft, valueRight)) && !Double.isInfinite(Math.pow(valueLeft, valueRight))) {
                    return Math.pow(valueLeft, valueRight);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                }
            default:
                return 0;
        }

    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        this.left.addContainedVars(vars);
        this.right.addContainedVars(vars);
    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
        this.left.addContainedIndeterminates(vars);
        this.right.addContainedIndeterminates(vars);
    }

    @Override
    public boolean contains(String var) {
        return this.left.contains(var) || this.right.contains(var);
    }

    @Override
    public boolean containsApproximates() {
        return (this.left.containsApproximates() || this.right.containsApproximates());
    }

    @Override
    public boolean containsFunction() {
        if (this.type.equals(TypeBinary.POW) && !this.right.isConstant()) {
            // Im diesem Fall handelt es sich (eventuell) um Exponentialfunktionen.
            return true;
        }
        return this.left.containsFunction() || this.right.containsFunction();
    }

    @Override
    public boolean containsExponentialFunction() {
        if (this.type.equals(TypeBinary.POW) && !this.right.isConstant()) {
            // Im diesem Fall handelt es sich (eventuell) um Exponentialfunktionen.
            return true;
        }
        return this.left.containsExponentialFunction() || this.right.containsExponentialFunction();
    }

    @Override
    public boolean containsTrigonometricalFunction() {
        return this.left.containsTrigonometricalFunction() || this.right.containsTrigonometricalFunction();
    }

    @Override
    public boolean containsIndefiniteIntegral() {
        return this.left.containsIndefiniteIntegral() || this.right.containsIndefiniteIntegral();
    }

    @Override
    public boolean containsOperator() {
        return this.left.containsOperator() || this.right.containsOperator();
    }

    @Override
    public boolean containsOperator(TypeOperator type) {
        return this.left.containsOperator(type) || this.right.containsOperator(type);
    }

    @Override
    public boolean containsAlgebraicOperation() {
        return this.left.containsAlgebraicOperation() || this.right.containsAlgebraicOperation()
                || this.right.isRationalConstant() && ((Constant) ((BinaryOperation) this.right).right).getValue().abs().compareTo(BigDecimal.ONE) > 0;
    }

    @Override
    public Expression turnToApproximate() {
        return new BinaryOperation(this.left.turnToApproximate(), this.right.turnToApproximate(), this.type);
    }

    @Override
    public Expression turnToPrecise() {
        return new BinaryOperation(this.left.turnToPrecise(), this.right.turnToPrecise(), this.type);
    }

    @Override
    public Expression replaceVariable(String var, Expression expr) {
        return new BinaryOperation(this.left.replaceVariable(var, expr), this.right.replaceVariable(var, expr), this.type);
    }

    @Override
    public Expression replaceSelfDefinedFunctionsByPredefinedFunctions() {
        return new BinaryOperation(this.left.replaceSelfDefinedFunctionsByPredefinedFunctions(),
                this.right.replaceSelfDefinedFunctionsByPredefinedFunctions(), this.type);
    }

    @Override
    public Expression diff(String var) throws EvaluationException {

        if (!this.contains(var) && !this.containsAtLeastOne(this.getContainedVariablesDependingOnGivenVariable(var))) {
            return Expression.ZERO;
        }

        if (this.isSum()) {
            return this.left.diff(var).add(this.right.diff(var));
        } else if (this.isDifference()) {
            return this.left.diff(var).sub(this.right.diff(var));
        } else if (this.isProduct()) {
            return this.left.diff(var).mult(this.right).add(this.left.mult(this.right.diff(var)));
        } else if (this.isQuotient()) {
            Expression enumerator = this.left.diff(var).mult(this.right).sub(this.left.mult(this.right.diff(var)));
            Expression denominator = this.right.pow(2);
            return enumerator.div(denominator);
        } else if (!this.right.contains(var)) {
            //Regel: (f^n)' = n*f^(n - 1)*f'
            return this.right.mult(this.left.pow(this.right.sub(1))).mult(this.left.diff(var));
        } else if (!this.left.contains(var)) {
            //Regel: (a^g)' = ln(a)*a^g*g')
            //Fehlerbehandlung: a muss > 0 sein!
            if (this.left.isConstant() && this.left.isNonPositive()) {
                throw new EvaluationException(Translator.translateOutputMessage("EB_BinaryOperation_FUNCTION_NOT_DIFFERENTIABLE"));
            }
            return new Function(this.left, TypeFunction.ln).mult(this).mult(this.right.diff(var));
        } else {
            //Regel: (f^g)' = f^g*(gf'/f + ln(f)*g')
            Expression rightBracket = this.left.diff(var).mult(this.right).div(this.left).add(new Function(this.left, TypeFunction.ln).mult(this.right.diff(var)));
            return this.mult(rightBracket);
        }

    }

    @Override
    public String toString() {

        String leftAsText, rightAsText;

        if (this.isSum()) {
            if (this.right.doesExpressionStartWithAMinusSign()) {
                return this.left.toString() + "+(" + this.right.toString() + ")";
            } else {
                return this.left.toString() + "+" + this.right.toString();
            }
        } else if (this.isDifference()) {

            leftAsText = this.left.toString();

            //0 - a soll als -a ausgegeben werden.
            if (this.left.equals(Expression.ZERO)) {
                leftAsText = "";
            }

            if (this.right.doesExpressionStartWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                return leftAsText + "-(" + this.right.toString() + ")";
            }
            return leftAsText + "-" + this.right.toString();

        } else if (this.isProduct()) {

            if (this.left.isSum() || this.left.isDifference()) {

                leftAsText = "(" + this.left.toString() + ")";
                if (this.right.doesExpressionStartWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                    rightAsText = "(" + this.right.toString() + ")";
                } else {
                    rightAsText = this.right.toString();
                }

            } else {

                leftAsText = this.left.toString();
                if (this.left instanceof Constant
                        && ((Constant) this.left).getValue().compareTo(BigDecimal.valueOf(-1)) == 0) {
                    // Ausnahmefall: Der Ausdruck fängt mit einem - an.
                    if (this.right.doesExpressionStartWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                        return "-(" + this.right.toString() + ")";
                    }
                    return "-" + this.right.toString();
                } else if (this.right.doesExpressionStartWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                    rightAsText = "(" + this.right.toString() + ")";
                } else {
                    rightAsText = this.right.toString();
                }

            }

            return leftAsText + "*" + rightAsText;

        } else if (this.isQuotient()) {

            if (this.left.isSum() || this.left.isDifference() || this.left.isProduct()) {
                leftAsText = "(" + this.left.toString() + ")";
            } else {
                leftAsText = this.left.toString();
            }

            if (this.right.doesExpressionStartWithAMinusSign()
                    || (this.right instanceof BinaryOperation && !this.right.isPower())) {
                rightAsText = "(" + this.right.toString() + ")";
            } else {
                rightAsText = this.right.toString();
            }

            return leftAsText + "/" + rightAsText;

        }

        // Hier handelt es sich um eine Potenz.
        if (this.left instanceof BinaryOperation
                || (this.left instanceof Constant && ((Constant) this.left).getValue().compareTo(BigDecimal.ZERO) < 0)) {
            leftAsText = "(" + this.left.toString() + ")";
        } else {
            leftAsText = this.left.toString();
        }

        if (this.right instanceof BinaryOperation
                || (this.right instanceof Constant && ((Constant) this.right).getValue().compareTo(BigDecimal.ZERO) < 0)) {
            rightAsText = "(" + this.right.toString() + ")";
        } else {
            rightAsText = this.right.toString();
        }

        return leftAsText + "^" + rightAsText;

    }

    @Override
    public String expressionToLatex() {

        String leftAsLatexCode, rightAsLatexCode;

        if (this.isSum()) {
            return this.left.expressionToLatex() + "+" + this.right.expressionToLatex();
        } else if (this.isDifference()) {

            leftAsLatexCode = this.left.toString();

            //0 - a soll als -a ausgegeben werden.
            if (this.left.equals(Expression.ZERO)) {
                leftAsLatexCode = "";
            }

            if (this.right.isSum() || this.right.isDifference()) {
                return leftAsLatexCode + "-\\left(" + this.right.expressionToLatex() + "\\right)";
            }
            return leftAsLatexCode + "-" + this.right.expressionToLatex();

        } else if (this.isProduct()) {

            //(-1)*a soll als -a ausgegeben werden.
            if (this.left.equals(Expression.MINUS_ONE)) {
                if (this.right.isSum() || this.right.isDifference()) {
                    // Hier noch zusätzliche Klammern um den rechten Faktor.
                    return "-(" + this.right.expressionToLatex() + ")";
                }
                return "-" + this.right.expressionToLatex();
            }

            if (this.left.isSum() || this.left.isDifference()) {
                leftAsLatexCode = "\\left(" + this.left.expressionToLatex() + "\\right)";
            } else {
                leftAsLatexCode = this.left.expressionToLatex();
            }

            if (this.right.isSum() || this.right.isDifference()) {
                rightAsLatexCode = "\\left(" + this.right.expressionToLatex() + "\\right)";
            } else {
                rightAsLatexCode = this.right.expressionToLatex();
            }

            return leftAsLatexCode + " \\cdot " + rightAsLatexCode;

        } else if (this.isQuotient()) {

            return "\\frac{" + this.left.expressionToLatex() + "}{" + this.right.expressionToLatex() + "}";

        } else {

            if (this.left instanceof BinaryOperation) {
                if (this.left.isDifference() && ((BinaryOperation) this.left).getLeft().equals(Expression.ZERO)) {
                    leftAsLatexCode = this.left.expressionToLatex();
                } else {
                    leftAsLatexCode = "\\left(" + this.left.expressionToLatex() + "\\right)";
                }
            } else {
                leftAsLatexCode = this.left.expressionToLatex();
            }

            if (this.left instanceof Variable) {

                if (this.right instanceof Variable && (this.right.toString().length() == 1)) {
                    return leftAsLatexCode + "^" + this.right.expressionToLatex();
                }
                return leftAsLatexCode + "^{" + this.right.expressionToLatex() + "}";

            } else if (this.left instanceof Constant) {

                if (this.left.isNonNegative()) {
                    if ((this.right instanceof Variable) && (this.right.toString().length() == 1)) {
                        return leftAsLatexCode + "^" + this.right.expressionToLatex();
                    }
                    return leftAsLatexCode + "^{" + this.right.expressionToLatex() + "}";
                }

            } else if ((this.right instanceof Variable) && (this.right.toString().length() == 1)) {
                return "{" + leftAsLatexCode + "}^" + this.right.expressionToLatex();
            }

            return "{" + leftAsLatexCode + "}^{" + this.right.expressionToLatex() + "}";

        }

    }

    @Override
    public boolean isConstant() {
        return this.left.isConstant() && this.right.isConstant();
    }

    @Override
    public boolean isNonNegative() {

        if (!this.isConstant()) {
            return false;
        }

        try {
            return this.evaluate() >= 0;
        } catch (EvaluationException e) {
        }

        if (this.type.equals(TypeBinary.PLUS)) {
            return this.left.isNonNegative() && this.right.isNonNegative();
        } else if (this.type.equals(TypeBinary.MINUS)) {
            return this.left.isNonNegative() && this.right.isNonPositive();
        } else if (this.type.equals(TypeBinary.TIMES) || this.type.equals(TypeBinary.DIV)) {
            return this.left.isNonNegative() && this.right.isNonNegative()
                    || this.left.isNonPositive() && this.right.isNonPositive();
        } else {

            // Hier ist type == TypeBinary.POW
            if (this.left.isNonNegative()) {
                return true;
            }
            if (this.right.isEvenIntegerConstant()) {
                return true;
            }
            if (this.right.isRationalConstant() && ((BinaryOperation) this.right).getLeft().isEvenIntegerConstant()) {
                return true;
            }
            if (this.right.isRationalConstant() && ((BinaryOperation) this.right).getLeft().isOddIntegerConstant()
                    && ((BinaryOperation) this.right).getRight().isOddIntegerConstant()) {
                return this.left.isNonNegative();
            }
            return false;

        }

    }

    @Override
    public boolean isNonPositive() {

        if (!this.isConstant()) {
            return false;
        }

        try {
            return this.evaluate() <= 0;
        } catch (EvaluationException e) {
        }

        if (this.type.equals(TypeBinary.PLUS)) {
            return this.left.isNonPositive() && this.right.isNonPositive();
        } else if (this.type.equals(TypeBinary.MINUS)) {
            return this.left.isNonPositive() && this.right.isNonNegative();
        } else if (this.type.equals(TypeBinary.TIMES) || this.type.equals(TypeBinary.DIV)) {
            return this.left.isNonNegative() && this.right.isNonPositive()
                    || this.left.isNonPositive() && this.right.isNonNegative();
        } else {

            // Hier ist type == TypeBinary.POW
            if (this.left.isNonPositive() && this.right.isOddIntegerConstant()) {
                return true;
            }
            if (this.left.isNonPositive() && this.right.isRationalConstant()
                    && ((BinaryOperation) this.right).getLeft().isOddIntegerConstant()
                    && ((BinaryOperation) this.right).getRight().isOddIntegerConstant()) {
                return true;
            }
            return false;

        }

    }

    @Override
    public boolean isAlwaysNonNegative() {

        if (this.isNonNegative()) {
            return true;
        }
        if (this.isSum()) {
            return this.left.isAlwaysNonNegative() && this.right.isAlwaysNonNegative();
        }
        if (this.isDifference()) {
            return this.left.isAlwaysNonNegative() && this.right.isAlwaysNonPositive();
        }
        if (this.isProduct() || this.isQuotient()) {
            return this.left.isAlwaysNonNegative() && this.right.isAlwaysNonNegative()
                    || this.left.isAlwaysNonPositive() && this.right.isAlwaysNonPositive();
        }
        if (this.isPower()) {
            return this.left.isAlwaysNonNegative() || this.right.isEvenIntegerConstant()
                    || this.right.isRationalConstant()
                    && (((BinaryOperation) this.right).left.isEvenIntegerConstant() || ((BinaryOperation) this.right).right.isEvenIntegerConstant());
        }
        return false;

    }

    @Override
    public boolean isAlwaysPositive() {

        if (this.isPositive()) {
            return true;
        }
        if (this.isSum()) {
            return this.left.isAlwaysPositive() && this.right.isAlwaysNonNegative()
                    || this.left.isAlwaysNonNegative() && this.right.isAlwaysPositive();
        }
        if (this.isDifference()) {
            return this.left.isAlwaysPositive() && this.right.isAlwaysNonPositive()
                    || this.left.isAlwaysNonNegative() && this.right.isAlwaysNegative();
        }
        if (this.isProduct() || this.isQuotient()) {
            return this.left.isAlwaysPositive() && this.right.isAlwaysPositive()
                    || this.left.isAlwaysNegative() && this.right.isAlwaysNegative();
        }
        if (this.isPower()) {
            return this.left.isAlwaysPositive()
                    || this.left.isAlwaysNegative() && (this.right.isEvenIntegerConstant()
                    || this.right.isRationalConstant()
                    && ((BinaryOperation) this.right).getLeft().isEvenIntegerConstant()
                    && ((BinaryOperation) this.right).getRight().isOddIntegerConstant());
        }
        return false;

    }

    @Override
    public boolean isAlwaysNonPositive() {

        if (this.isNonPositive()) {
            return true;
        }
        if (this.isSum()) {
            return this.left.isAlwaysNonPositive() && this.right.isAlwaysNonPositive();
        }
        if (this.isDifference()) {
            return this.left.isAlwaysNonPositive() && this.right.isAlwaysNonNegative();
        }
        if (this.isProduct() || this.isQuotient()) {
            return this.left.isAlwaysNonNegative() && this.right.isAlwaysNonPositive()
                    || this.left.isAlwaysNonPositive() && this.right.isAlwaysNonNegative();
        }
        if (this.isPower()) {
            return this.left.isAlwaysNonPositive() && (this.right.isOddIntegerConstant()
                    || this.right.isRationalConstant()
                    && (((BinaryOperation) this.right).left.isOddIntegerConstant() || ((BinaryOperation) this.right).right.isOddIntegerConstant()));
        }
        return false;

    }

    @Override
    public boolean isAlwaysNegative() {

        if (this.isNegative()) {
            return true;
        }
        if (this.isSum()) {
            return this.left.isAlwaysNegative() && this.right.isAlwaysNonPositive()
                    || this.left.isAlwaysNonPositive() && this.right.isAlwaysNegative();
        }
        if (this.isDifference()) {
            return this.left.isAlwaysNegative() && this.right.isAlwaysNonNegative()
                    || this.left.isAlwaysNonPositive() && this.right.isAlwaysPositive();
        }
        if (this.isProduct() || this.isQuotient()) {
            return this.left.isAlwaysNegative() && this.right.isAlwaysPositive()
                    || this.left.isAlwaysPositive() && this.right.isAlwaysNegative();
        }
        if (this.isPower()) {
            return this.left.isAlwaysNegative() && (this.right.isOddIntegerConstant()
                    || this.right.isRationalConstant()
                    && ((BinaryOperation) this.right).getLeft().isOddIntegerConstant()
                    && ((BinaryOperation) this.right).getRight().isOddIntegerConstant());
        }
        return false;

    }

    @Override
    public boolean equals(Expression expr) {
        return expr instanceof BinaryOperation
                && this.type.equals(((BinaryOperation) expr).type)
                && this.left.equals(((BinaryOperation) expr).left)
                && this.right.equals(((BinaryOperation) expr).right);
    }

    @Override
    public boolean equivalent(Expression expr) {

        if (expr instanceof BinaryOperation) {
            if (this.type.equals(((BinaryOperation) expr).type)) {
                if (this.isSum()) {

                    ExpressionCollection summandsOfThis = SimplifyUtilities.getSummands(this);
                    ExpressionCollection summandsOfExpr = SimplifyUtilities.getSummands(expr);
                    return summandsOfThis.getBound() == summandsOfExpr.getBound()
                            && SimplifyUtilities.difference(summandsOfThis, summandsOfExpr).isEmpty();

                }
                if (this.isDifference()) {

                    ExpressionCollection summandsLeftOfThis = SimplifyUtilities.getSummandsLeftInExpression(this);
                    ExpressionCollection summandsRightOfThis = SimplifyUtilities.getSummandsRightInExpression(this);
                    ExpressionCollection summandsLeftOfExpr = SimplifyUtilities.getSummandsLeftInExpression(expr);
                    ExpressionCollection summandsRightOfExpr = SimplifyUtilities.getSummandsRightInExpression(expr);

                    ExpressionCollection summandsLeftOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsLeftOfExprWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfExprWithSign = new ExpressionCollection();

                    try {
                        for (int i = 0; i < summandsLeftOfThis.getBound(); i++) {
                            if (summandsLeftOfThis.get(i).hasPositiveSign()) {
                                summandsLeftOfThisWithSign.add(summandsLeftOfThis.get(i));
                            } else {
                                summandsRightOfThisWithSign.add(MINUS_ONE.mult(summandsLeftOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfThis.getBound(); i++) {
                            if (summandsRightOfThis.get(i).hasPositiveSign()) {
                                summandsRightOfThisWithSign.add(summandsRightOfThis.get(i));
                            } else {
                                summandsLeftOfThisWithSign.add(MINUS_ONE.mult(summandsRightOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsLeftOfExpr.getBound(); i++) {
                            if (summandsLeftOfExpr.get(i).hasPositiveSign()) {
                                summandsLeftOfExprWithSign.add(summandsLeftOfExpr.get(i));
                            } else {
                                summandsRightOfExprWithSign.add(MINUS_ONE.mult(summandsLeftOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfExpr.getBound(); i++) {
                            if (summandsRightOfExpr.get(i).hasPositiveSign()) {
                                summandsRightOfExprWithSign.add(summandsRightOfExpr.get(i));
                            } else {
                                summandsLeftOfExprWithSign.add(MINUS_ONE.mult(summandsRightOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        return summandsLeftOfThisWithSign.getBound() == summandsLeftOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsLeftOfThisWithSign, summandsLeftOfExprWithSign).isEmpty()
                                && summandsRightOfThisWithSign.getBound() == summandsRightOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsRightOfThisWithSign, summandsRightOfExprWithSign).isEmpty();
                    } catch (EvaluationException e) {
                    }

                }
                if (this.isProduct()) {

                    ExpressionCollection factorsThis = SimplifyUtilities.getFactors(this);
                    ExpressionCollection factorsExpr = SimplifyUtilities.getFactors(expr);
                    if (factorsThis.getBound() != factorsExpr.getBound()) {
                        return false;
                    }

                    int numberOfFactors = factorsThis.getBound();
                    int numberOfEquivalentFactors = 0;
                    int numberOfAntiEquivalentFactors = 0;
                    for (int i = 0; i < factorsThis.getBound(); i++) {
                        for (int j = 0; j < factorsExpr.getBound(); j++) {
                            if (factorsThis.get(i).equivalent(factorsExpr.get(j))) {
                                factorsThis.remove(i);
                                factorsExpr.remove(j);
                                numberOfEquivalentFactors++;
                                break;
                            } else if (factorsThis.get(i).antiEquivalent(factorsExpr.get(j))) {
                                factorsThis.remove(i);
                                factorsExpr.remove(j);
                                numberOfAntiEquivalentFactors++;
                                break;
                            }

                        }
                    }
                    return numberOfEquivalentFactors + numberOfAntiEquivalentFactors == numberOfFactors && numberOfAntiEquivalentFactors % 2 == 0;

                }
                if (this.isQuotient()) {

                    return this.left.equivalent(((BinaryOperation) expr).left)
                            && this.right.equivalent(((BinaryOperation) expr).right)
                            || this.left.antiEquivalent(((BinaryOperation) expr).left)
                            && this.right.antiEquivalent(((BinaryOperation) expr).right);

                }
                if (this.isPower() && expr.isPower() && this.right.equivalent(((BinaryOperation) expr).right)
                        && (((BinaryOperation) expr).right.isEvenIntegerConstant() || ((BinaryOperation) expr).right.isRationalConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr).right).left.isEvenIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr).right).right.isOddIntegerConstant())) {

                    /* 
                     Bei geraden Potenzen oder bei rationalen Potenzen mit geradem Zählen und ungeradem Nenner sollen 
                     die Ausdrücke äquivalent sein, wenn sich die Basen sogar um ein Vorzeichen unterscheiden.
                     */
                    ExpressionCollection summandsLeftOfThis = SimplifyUtilities.getSummandsLeftInExpression(this.left);
                    ExpressionCollection summandsRightOfThis = SimplifyUtilities.getSummandsRightInExpression(this.left);
                    ExpressionCollection summandsLeftOfExpr = SimplifyUtilities.getSummandsLeftInExpression(((BinaryOperation) expr).left);
                    ExpressionCollection summandsRightOfExpr = SimplifyUtilities.getSummandsRightInExpression(((BinaryOperation) expr).left);

                    ExpressionCollection summandsLeftOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsLeftOfExprWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfExprWithSign = new ExpressionCollection();

                    try {
                        for (int i = 0; i < summandsLeftOfThis.getBound(); i++) {
                            if (summandsLeftOfThis.get(i).hasPositiveSign()) {
                                summandsLeftOfThisWithSign.add(summandsLeftOfThis.get(i));
                            } else {
                                summandsRightOfThisWithSign.add(MINUS_ONE.mult(summandsLeftOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfThis.getBound(); i++) {
                            if (summandsRightOfThis.get(i).hasPositiveSign()) {
                                summandsRightOfThisWithSign.add(summandsRightOfThis.get(i));
                            } else {
                                summandsLeftOfThisWithSign.add(MINUS_ONE.mult(summandsRightOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsLeftOfExpr.getBound(); i++) {
                            if (summandsLeftOfExpr.get(i).hasPositiveSign()) {
                                summandsLeftOfExprWithSign.add(summandsLeftOfExpr.get(i));
                            } else {
                                summandsRightOfExprWithSign.add(MINUS_ONE.mult(summandsLeftOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfExpr.getBound(); i++) {
                            if (summandsRightOfExpr.get(i).hasPositiveSign()) {
                                summandsRightOfExprWithSign.add(summandsRightOfExpr.get(i));
                            } else {
                                summandsLeftOfExprWithSign.add(MINUS_ONE.mult(summandsRightOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        return summandsLeftOfThisWithSign.getBound() == summandsLeftOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsLeftOfThisWithSign, summandsLeftOfExprWithSign).isEmpty()
                                && summandsRightOfThisWithSign.getBound() == summandsRightOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsRightOfThisWithSign, summandsRightOfExprWithSign).isEmpty()
                                || summandsLeftOfThisWithSign.getBound() == summandsRightOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsLeftOfThisWithSign, summandsRightOfExprWithSign).isEmpty()
                                && summandsRightOfThisWithSign.getBound() == summandsLeftOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsRightOfThisWithSign, summandsLeftOfExprWithSign).isEmpty();
                    } catch (EvaluationException e) {
                    }

                }
                return this.left.equivalent(((BinaryOperation) expr).left)
                        && this.right.equivalent(((BinaryOperation) expr).right);
            }
            return false;
        }
        return false;

    }

    @Override
    public boolean antiEquivalent(Expression expr) {

        if (expr instanceof BinaryOperation) {
            if (this.type.equals(((BinaryOperation) expr).type)) {
                if (this.isSum() || this.isDifference()) {

                    ExpressionCollection summandsLeftOfThis = SimplifyUtilities.getSummandsLeftInExpression(this);
                    ExpressionCollection summandsRightOfThis = SimplifyUtilities.getSummandsRightInExpression(this);
                    ExpressionCollection summandsLeftOfExpr = SimplifyUtilities.getSummandsLeftInExpression(expr);
                    ExpressionCollection summandsRightOfExpr = SimplifyUtilities.getSummandsRightInExpression(expr);

                    ExpressionCollection summandsLeftOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsLeftOfExprWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfExprWithSign = new ExpressionCollection();

                    try {
                        for (int i = 0; i < summandsLeftOfThis.getBound(); i++) {
                            if (summandsLeftOfThis.get(i).hasPositiveSign()) {
                                summandsLeftOfThisWithSign.add(summandsLeftOfThis.get(i));
                            } else {
                                summandsRightOfThisWithSign.add(MINUS_ONE.mult(summandsLeftOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfThis.getBound(); i++) {
                            if (summandsRightOfThis.get(i).hasPositiveSign()) {
                                summandsRightOfThisWithSign.add(summandsRightOfThis.get(i));
                            } else {
                                summandsLeftOfThisWithSign.add(MINUS_ONE.mult(summandsRightOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsLeftOfExpr.getBound(); i++) {
                            if (summandsLeftOfExpr.get(i).hasPositiveSign()) {
                                summandsLeftOfExprWithSign.add(summandsLeftOfExpr.get(i));
                            } else {
                                summandsRightOfExprWithSign.add(MINUS_ONE.mult(summandsLeftOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfExpr.getBound(); i++) {
                            if (summandsRightOfExpr.get(i).hasPositiveSign()) {
                                summandsRightOfExprWithSign.add(summandsRightOfExpr.get(i));
                            } else {
                                summandsLeftOfExprWithSign.add(MINUS_ONE.mult(summandsRightOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        return summandsLeftOfThisWithSign.getBound() == summandsRightOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsLeftOfThisWithSign, summandsRightOfExprWithSign).isEmpty()
                                && summandsRightOfThisWithSign.getBound() == summandsLeftOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsRightOfThisWithSign, summandsLeftOfExprWithSign).isEmpty();
                    } catch (EvaluationException e) {
                    }

                }
                if (this.isProduct()) {

                    ExpressionCollection factorsThis = SimplifyUtilities.getFactors(this);
                    ExpressionCollection factorsExpr = SimplifyUtilities.getFactors(expr);
                    if (factorsThis.getBound() != factorsExpr.getBound()) {
                        return false;
                    }

                    int numberOfFactors = factorsThis.getBound();
                    int numberOfEquivalentFactors = 0;
                    int numberOfAntiEquivalentFactors = 0;
                    for (int i = 0; i < factorsThis.getBound(); i++) {
                        for (int j = 0; j < factorsExpr.getBound(); j++) {
                            if (factorsThis.get(i).equivalent(factorsExpr.get(j))) {
                                factorsThis.remove(i);
                                factorsExpr.remove(j);
                                numberOfEquivalentFactors++;
                                break;
                            } else if (factorsThis.get(i).antiEquivalent(factorsExpr.get(j))) {
                                factorsThis.remove(i);
                                factorsExpr.remove(j);
                                numberOfAntiEquivalentFactors++;
                                break;
                            }

                        }
                    }
                    return numberOfEquivalentFactors + numberOfAntiEquivalentFactors == numberOfFactors && numberOfAntiEquivalentFactors % 2 == 1;

                }
                if (this.isQuotient()) {
                    return this.left.equivalent(((BinaryOperation) expr).left) && this.right.antiEquivalent(((BinaryOperation) expr).right)
                            || this.left.antiEquivalent(((BinaryOperation) expr).left) && this.right.equivalent(((BinaryOperation) expr).right);

                }
                if (this.isPower() && expr.isPower() && this.right.equivalent(((BinaryOperation) expr).right)
                        && (((BinaryOperation) expr).right.isOddIntegerConstant() || ((BinaryOperation) expr).right.isRationalConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr).right).left.isOddIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr).right).right.isOddIntegerConstant())) {

                    /* 
                     Bei ungeraden Potenzen oder bei rationalen Potenzen mit ungeradem Zählen und ungeradem Nenner sollen 
                     die Ausdrücke antiäquivalent sein, wenn sich die Basen sogar um ein Vorzeichen unterscheiden.
                     */
                    ExpressionCollection summandsLeftOfThis = SimplifyUtilities.getSummandsLeftInExpression(this.left);
                    ExpressionCollection summandsRightOfThis = SimplifyUtilities.getSummandsRightInExpression(this.left);
                    ExpressionCollection summandsLeftOfExpr = SimplifyUtilities.getSummandsLeftInExpression(((BinaryOperation) expr).left);
                    ExpressionCollection summandsRightOfExpr = SimplifyUtilities.getSummandsRightInExpression(((BinaryOperation) expr).left);

                    ExpressionCollection summandsLeftOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsLeftOfExprWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfExprWithSign = new ExpressionCollection();

                    try {
                        for (int i = 0; i < summandsLeftOfThis.getBound(); i++) {
                            if (summandsLeftOfThis.get(i).hasPositiveSign()) {
                                summandsLeftOfThisWithSign.add(summandsLeftOfThis.get(i));
                            } else {
                                summandsRightOfThisWithSign.add(MINUS_ONE.mult(summandsLeftOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfThis.getBound(); i++) {
                            if (summandsRightOfThis.get(i).hasPositiveSign()) {
                                summandsRightOfThisWithSign.add(summandsRightOfThis.get(i));
                            } else {
                                summandsLeftOfThisWithSign.add(MINUS_ONE.mult(summandsRightOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsLeftOfExpr.getBound(); i++) {
                            if (summandsLeftOfExpr.get(i).hasPositiveSign()) {
                                summandsLeftOfExprWithSign.add(summandsLeftOfExpr.get(i));
                            } else {
                                summandsRightOfExprWithSign.add(MINUS_ONE.mult(summandsLeftOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfExpr.getBound(); i++) {
                            if (summandsRightOfExpr.get(i).hasPositiveSign()) {
                                summandsRightOfExprWithSign.add(summandsRightOfExpr.get(i));
                            } else {
                                summandsLeftOfExprWithSign.add(MINUS_ONE.mult(summandsRightOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        return summandsLeftOfThisWithSign.getBound() == summandsRightOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsLeftOfThisWithSign, summandsRightOfExprWithSign).isEmpty()
                                && summandsRightOfThisWithSign.getBound() == summandsLeftOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsRightOfThisWithSign, summandsLeftOfExprWithSign).isEmpty();
                    } catch (EvaluationException e) {
                    }

                }
            }
            return false;
        }
        return false;

    }

    @Override
    public boolean hasPositiveSign() {
        if (this.type != TypeBinary.TIMES && this.type != TypeBinary.DIV) {
            return true;
        }
        return (this.left.hasPositiveSign() && this.right.hasPositiveSign()) || (!this.left.hasPositiveSign() && !this.right.hasPositiveSign());
    }

    @Override
    public int getLength() {
        if (this.isProduct()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            int length = 0;
            for (int i = 0; i < factors.getBound(); i++) {
                if (!(factors.get(i) instanceof Constant)) {
                    length += factors.get(i).getLength();
                }
            }
            /* 
             Konstante Koeffizienten sollen nicht in die Länge miteinfließen, außer, 
             der Ausdruck ist an sich konstant.
             */
            return Math.max(length, 1);
        }
        if (this.isPower()) {
            if (this.left instanceof Constant) {
                return ((BinaryOperation) this).getRight().getLength();
            }
            if (this.right instanceof Constant && !(this.right instanceof BinaryOperation)) {
                return ((BinaryOperation) this).getLeft().getLength();
            }
        }
        if (this.isQuotient()) {
            // Hier wird eher die Länge des Bruches gewertet, nicht die des ausgeschriebenen Strings.
            return Math.max(this.left.getLength(), this.right.getLength());
        }
        return this.left.getLength() + this.right.getLength();
    }

    @Override
    public int getMaximalNumberOfSummandsInExpansion() {

        int numberOfSummands = 0;

        if (this.isSum() || this.isDifference()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);

            for (Expression summand : summandsLeft) {
                numberOfSummands = numberOfSummands + summand.getMaximalNumberOfSummandsInExpansion();
            }
            for (Expression summand : summandsRight) {
                numberOfSummands = numberOfSummands + summand.getMaximalNumberOfSummandsInExpansion();
            }

            return numberOfSummands;

        }
        if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);

            for (Expression factor : factors) {
                numberOfSummands = numberOfSummands * factor.getMaximalNumberOfSummandsInExpansion();
            }

            return numberOfSummands;

        }
        if (this.isQuotient()) {

            return this.left.getMaximalNumberOfSummandsInExpansion();

        }

        // Ab hier ist this eine Potenz;
        if (this.right.isPositiveIntegerConstant()
                && ((Constant) this.right).getBigIntValue().compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_BINOMIAL)) <= 0) {
            int exponent = ((Constant) this.right).getBigIntValue().intValue();
            int numberOfSummandsInBase = this.left.getMaximalNumberOfSummandsInExpansion();
            BigInteger numberOfSummandsInResult = ArithmeticUtils.factorial(numberOfSummandsInBase - 1 + exponent).divide(ArithmeticUtils.factorial(numberOfSummandsInBase - 1).multiply(ArithmeticUtils.factorial(exponent)));

            if (numberOfSummandsInResult.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                return Integer.MAX_VALUE;
            }
            return numberOfSummandsInResult.intValue();
        }

        // In diesem Fall findet kein Ausmultiplizieren statt. Die Anzahl der Summanden ist also 1.
        return 1;

    }

    @Override
    public Expression simplifyBasic() throws EvaluationException {

        // Allgemeine Vereinfachungen, falls der zugrundeliegende Ausdruck konstant ist.
        Expression exprLeftAndRightSimplified;
        BinaryOperation expr;

        if (this.isSum()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).simplifyBasic());
            }

            ExpressionCollection summandsRight = new ExpressionCollection();

            // Nullen in Summen beseitigen.
            SimplifyBinaryOperationUtils.removeZerosInSums(summandsLeft);

            // Summanden mit negativen Koeffizienten in den Subtrahenden bringen.
            SimplifyBinaryOperationUtils.simplifySumsAndDifferencesWithNegativeCoefficient(summandsLeft, summandsRight);

            // Schließlich: Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
            if (this.isConstant() && this.containsApproximates()) {
                SimplifyBinaryOperationUtils.computeSumIfApprox(summandsLeft, summandsRight);
            }

            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isDifference()) {

            exprLeftAndRightSimplified = this.left.simplifyBasic().sub(this.right.simplifyBasic());
            if (!(exprLeftAndRightSimplified instanceof BinaryOperation)) {
                return exprLeftAndRightSimplified;
            }
            expr = (BinaryOperation) exprLeftAndRightSimplified;

            // Triviale Umformungen
            Expression exprSimplified = SimplifyBinaryOperationUtils.trivialOperationsInDifferenceWithZeroOne(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(exprSimplified);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(exprSimplified);

            // Brüche subtrahieren
            SimplifyBinaryOperationUtils.subtractFractions(summandsLeft, summandsRight);

            // Summanden mit negativen Koeffizienten in den in jeweils anderen Teil herübertragen.
            SimplifyBinaryOperationUtils.simplifySumsAndDifferencesWithNegativeCoefficient(summandsLeft, summandsRight);

            // Schließlich: Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
            if (this.isConstant() && this.containsApproximates()) {
                return SimplifyBinaryOperationUtils.computeDifferenceIfApprox(SimplifyUtilities.produceDifference(summandsLeft, summandsRight));
            }

            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyBasic());
            }

            Expression exprSimplified = SimplifyUtilities.produceProduct(factors);
            if (!exprSimplified.isProduct()) {
                return exprSimplified;
            }

            // Falls Nullen in Produkten auftauchen: 0 zurückgeben.
            SimplifyBinaryOperationUtils.reduceProductWithZeroToZero(factors);

            // Einsen in Produkten beseitigen.
            SimplifyBinaryOperationUtils.removeOnesInProducts(factors);

            /* 
             Falls in den Faktoren Summen / Differenzen auftauchen, in denen die 
             Summanden alle negatives Vorzeichen besitzen: -1 ausklammern!
             */
            SimplifyBinaryOperationUtils.pullMinusSignFromProductOrQuotientsWithCompleteNegativeSums(factors, new ExpressionCollection());

            // Schließlich: Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
            if (this.isConstant() && this.containsApproximates()) {
                SimplifyBinaryOperationUtils.computeProductIfApprox(factors);
            }

            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isQuotient()) {

            exprLeftAndRightSimplified = this.left.simplifyBasic().div(this.right.simplifyBasic());
            if (!(exprLeftAndRightSimplified instanceof BinaryOperation)) {
                return exprLeftAndRightSimplified;
            }
            expr = (BinaryOperation) exprLeftAndRightSimplified;
            Expression exprSimplified;

            // Triviale Umformungen
            exprSimplified = SimplifyBinaryOperationUtils.trivialOperationsInQuotientWithZeroOne(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Division durch 0 im Approximationsmodus ausschließen, sonst dividieren.
            exprSimplified = SimplifyBinaryOperationUtils.computeReciprocalInApprox(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Negatives Vorzeichen aus dem Nenner in den Zähler bringen.
            exprSimplified = SimplifyBinaryOperationUtils.takeMinusSignOutOfDenominatorInFraction(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Rationale Konstanten zu einem Bruch machen (etwa 0.74/0.2 = 37/10)
            exprSimplified = SimplifyBinaryOperationUtils.rationalConstantToQuotient(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Negative Zähler eliminieren.
            exprSimplified = SimplifyBinaryOperationUtils.eliminateNegativeDenominator(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
            if (expr.isConstant() && expr.containsApproximates()) {
                return SimplifyBinaryOperationUtils.computeQuotientIfApprox(expr);
            }

            /* 
             Schließlich: Falls in den Faktoren im Zähler oder im Nenner Summen / Differenzen 
             auftauchen, in denen die Summanden alle negatives Vorzeichen besitzen: 
             -1 ausklammern!
             */
            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(expr);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr);
            SimplifyBinaryOperationUtils.pullMinusSignFromProductOrQuotientsWithCompleteNegativeSums(factorsEnumerator, factorsDenominator);
            return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

        }

        // Ab hier ist this eine Potenz.
        /*
         Hier wird das folgende kritische Problem aus dem Weg geschafft: Wird
         (-2)^(1/3) approximiert, so wird der Exponent zu 0.33333333333
         approximiert und dementsprechend kann das Ergebnis nicht berechnet
         werden. Daher, wenn expr eine ungerade Wurzeln darstellt: negatives
         Vorzeichen rausschaffen!
         */
        Expression exprSimplified = SimplifyBinaryOperationUtils.computeOddRootOfNegativeConstantsInApprox(this);
        if (!exprSimplified.equals(this)) {
            return exprSimplified;
        }

        exprLeftAndRightSimplified = this.left.simplifyBasic().pow(this.right.simplifyBasic());
        if (!(exprLeftAndRightSimplified instanceof BinaryOperation)) {
            return exprLeftAndRightSimplified;
        }
        expr = (BinaryOperation) exprLeftAndRightSimplified;

        // Nun folgen Vereinfachungen von Potenzen und Wurzeln konstanter Ausdrücke, soweit möglich.
        // Berechnung ganzzahliger Potenzen ganzer Zahlen.
        exprSimplified = SimplifyBinaryOperationUtils.computePowersOfIntegers(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Berechnung ganzzahliger Potenzen von Brüchen.
        exprSimplified = SimplifyBinaryOperationUtils.computePowersOfFractions(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Triviale Umformungen
        exprSimplified = SimplifyBinaryOperationUtils.trivialOperationsInPowerWithZeroOne(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Macht z.B. (5/7)^(4/3) = (5/7)*(5/7)^(1/3) = 5*(5/7)^(1/3)/7
        exprSimplified = SimplifyBinaryOperationUtils.separateIntegerPowersOfRationalConstants(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        /* 
         Negative Vorzeichen in der Basis eliminieren, wenn Exponent die Form m/n mit m gerade und n ungerade besitzt.
         Sind m und n beide ungerade, so wird das Vorzeichen herausgezogen. 
         */
        exprSimplified = SimplifyBinaryOperationUtils.takeMinusSignOutOfOddRoots(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Prüfen, ob Wurzeln gerader Ordnung aus negativen Konstanten gezogen werden.
        exprSimplified = SimplifyBinaryOperationUtils.checkNegativityOfBaseInRootsOfEvenDegree(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Versuchen, Wurzeln (z.B. in Quotienten oder von ganzen Zahlen) zum Teil exakt anzugeben.
        exprSimplified = SimplifyBinaryOperationUtils.tryTakePartialRootsPrecisely(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Versuchen, ganzzahlige Anteile von Exponenten abzuspalten.
        exprSimplified = SimplifyBinaryOperationUtils.separateIntegerPowersOfRationalConstants(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfache Potenzen von Quotienten, falls im Zähler oder im Nenner ganze Zahlen auftauchen.
        exprSimplified = SimplifyBinaryOperationUtils.simplifyPowerOfQuotient(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfacht: (a/b)^(-k) = (b/a)^k
        exprSimplified = SimplifyBinaryOperationUtils.negativePowersOfQuotientsToReciprocal(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfacht Folgendes (1/x)^y = 1/x^y.
        exprSimplified = SimplifyBinaryOperationUtils.simplifyPowersOfReciprocals(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Negative Potenzen in den Nenner: x^y = 1/x^(-y), falls y < 0. 
        exprSimplified = SimplifyBinaryOperationUtils.negativePowersOfExpressionsToReciprocal(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // (a^x)^y = a^(x*y)
        exprSimplified = SimplifyBinaryOperationUtils.simplifyDoublePowers(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // exp(x)^y = exp(x*y)
        exprSimplified = SimplifyBinaryOperationUtils.simplifyPowersOfExpFunction(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfacht Potenzen von Beträgen.
        exprSimplified = SimplifyBinaryOperationUtils.simplifyPowersOfAbs(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Schließlich: Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
        if (expr.isConstant() && expr.containsApproximates()) {
            return SimplifyBinaryOperationUtils.computePowerIfApprox(expr);
        }

        return expr;

    }

    @Override
    public Expression simplifyByInsertingDefinedVars() throws EvaluationException {
        return new BinaryOperation(this.left.simplifyByInsertingDefinedVars(), this.right.simplifyByInsertingDefinedVars(), this.type);
    }

    @Override
    public Expression simplifyExpandRationalFactors() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln ausmultiplizieren.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyExpandRationalFactors());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isDifference() || this.isPower()) {
            return new BinaryOperation(this.left.simplifyExpandRationalFactors(), this.right.simplifyExpandRationalFactors(), this.type);
        }

        BinaryOperation expr;
        if (this.isProduct()) {
            // In jedem Faktor einzeln ausmultiplizieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyExpandRationalFactors());
            }
            Expression productOfSimplifiedFactors = SimplifyUtilities.produceProduct(factors);
            if (!(productOfSimplifiedFactors instanceof BinaryOperation)) {
                return productOfSimplifiedFactors;
            }
            expr = (BinaryOperation) productOfSimplifiedFactors;
        } else {
            Expression simplifiedQuotient;
            simplifiedQuotient = this.left.simplifyExpandRationalFactors().div(this.right.simplifyExpandRationalFactors());
            if (!(simplifiedQuotient instanceof BinaryOperation)) {
                return simplifiedQuotient;
            }
            expr = (BinaryOperation) this.left.simplifyExpandRationalFactors().div(this.right.simplifyExpandRationalFactors());
        }

        if (expr.isProduct() && expr.getLeft() instanceof Constant) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getRight());
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getRight());

            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, expr.getLeft().mult(summandsLeft.get(i)));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, expr.getLeft().mult(summandsRight.get(i)));
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (expr.isQuotient() && expr.getRight() instanceof Constant) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());

            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).div(expr.getRight()));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).div(expr.getRight()));
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        }

        return expr;

    }

    @Override
    public Expression simplifyExpand(TypeExpansion type) throws EvaluationException {

        Expression expr = this, exprExpanded = SimplifyBinaryOperationUtils.simplifySingleExpand(this, type);

        // Es wird solange ausmultipliziert, bis keine weitere Ausmultiplikation mehr möglich ist.
        while (!expr.equals(exprExpanded)) {
            expr = exprExpanded.copy();
            exprExpanded = SimplifyBinaryOperationUtils.simplifySingleExpand(expr, type);
        }

        return expr;

    }

    @Override
    public Expression simplifyBringExpressionToCommonDenominator(TypeFractionSimplification type) throws EvaluationException {

        Expression expr = this;
        boolean containsMultipleFractions = containsDoubleFraction(expr);

        if (this.isSum() || this.isDifference()) {
            // Jeden Summanden einzeln auf einen Nenner bringen.
            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).simplifyBringExpressionToCommonDenominator(type));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).simplifyBringExpressionToCommonDenominator(type));
            }
            expr = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
        } else if (this.isProduct() || this.isQuotient()) {
            ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(this);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);
            for (int i = 0; i < factorsNumerator.getBound(); i++) {
                factorsNumerator.put(i, factorsNumerator.get(i).simplifyBringExpressionToCommonDenominator(type));
            }
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                factorsDenominator.put(i, factorsDenominator.get(i).simplifyBringExpressionToCommonDenominator(type));
            }
            // Bis hierhin ist das Ergebnis von der Form (A_1/B_1)* ... *(A_m/B_m) / (C_1/D_1)* ... *(C_n/D_n). Den Rest erledigt das Ordnen.
            expr = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator).orderDifferencesAndQuotients();
        } else if (this.isPower()) {
            expr = this.left.simplifyBringExpressionToCommonDenominator(type).pow(this.right.simplifyBringExpressionToCommonDenominator(type));
        }

        // Nur bei Mehrfachbrüchen alles auf einen Nenner bringen.
        if (!(expr instanceof BinaryOperation) || type.equals(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS) && !containsMultipleFractions) {
            return expr;
        }

        Expression exprSimplified = SimplifyBinaryOperationUtils.bringExpressionToCommonDenominator(expr);

        // Es wird solange auf einen Nenner gebracht, bis dies nicht mehr möglich ist.
        while (!expr.equals(exprSimplified)) {
            expr = exprSimplified.copy();
            exprSimplified = SimplifyBinaryOperationUtils.bringExpressionToCommonDenominator(expr);
        }

        return expr;

    }

    /**
     * Hilfsmethode. Gibt zurück, ob expr Doppelbrüche enthält, die man auf
     * einen Bruch bringen könnte (d.h. beispielsweise, dass Brüche in
     * Funktionsargumenten ignoriert werden).
     */
    private static boolean containsDoubleFraction(Expression expr) {
        return containsRepeatedFraction(expr, true);
    }

    /**
     * Hilfsmethode. Gibt zurück, ob expr Doppelbrüche enthält, die man auf
     * einen Bruch bringen könnte (d.h. beispielsweise, dass Brüche in
     * Funktionsargumenten ignoriert werden).
     */
    private static boolean containsRepeatedFraction(Expression expr, boolean nestedFractionAllowed) {

        if (expr instanceof BinaryOperation) {
            if (expr.isSum() || expr.isDifference() || expr.isProduct()) {
                return containsRepeatedFraction(((BinaryOperation) expr).getLeft(), nestedFractionAllowed) || containsRepeatedFraction(((BinaryOperation) expr).getRight(), nestedFractionAllowed);
            }
            if (expr.isQuotient()) {
                if (!nestedFractionAllowed) {
                    return true;
                }
                return containsRepeatedFraction(((BinaryOperation) expr).getLeft(), false) || containsRepeatedFraction(((BinaryOperation) expr).getRight(), false);
            }
            if (expr.isIntegerPower()) {
                return containsRepeatedFraction(((BinaryOperation) expr).getLeft(), nestedFractionAllowed);
            }
        }
        return false;

    }

    @Override
    public Expression simplifyReduceDifferencesAndQuotientsAdvanced() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReduceDifferencesAndQuotientsAdvanced());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReduceDifferencesAndQuotientsAdvanced());
            }

            /*
             Prüft, ob man beispielsweise flgendermaßen kürzen kann: 10*x*(1/6 + y/14)
             = 5*x*(1/3 + y/7).
             */
            SimplifyBinaryOperationUtils.pullGCDOfCoefficientsInProducts(factors);

            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isPower()) {
            return this.left.simplifyReduceDifferencesAndQuotientsAdvanced().pow(this.right.simplifyReduceDifferencesAndQuotientsAdvanced());
        }

        // Nun kann es dich nur noch um Differenzen oder Quotienten handeln.
        ExpressionCollection termsLeft;
        ExpressionCollection termsRight;
        if (this.isDifference()) {
            termsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
            termsRight = SimplifyUtilities.getSummandsRightInExpression(this);
        } else {
            termsLeft = SimplifyUtilities.getFactorsOfNumeratorInExpression(this);
            termsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);
        }

        // Zunächst in allen Summanden/Faktoren einzeln kürzen.
        for (int i = 0; i < termsLeft.getBound(); i++) {
            termsLeft.put(i, termsLeft.get(i).simplifyReduceDifferencesAndQuotientsAdvanced());
        }
        for (int i = 0; i < termsRight.getBound(); i++) {
            termsRight.put(i, termsRight.get(i).simplifyReduceDifferencesAndQuotientsAdvanced());
        }

        // Nun das eigentliche Kürzen!
        if (this.isDifference()) {

            SimplifyBinaryOperationUtils.reduceLeadingCoefficientsInDifferenceInApprox(termsLeft, termsRight);
            SimplifyBinaryOperationUtils.reduceLeadingCoefficientsInDifference(termsLeft, termsRight);

            // Ergebnis bilden.
            return SimplifyUtilities.produceDifference(termsLeft, termsRight);

        } else {

            // Vereinfachungen, bei den im Quotienten die FAKTOREN im Zähler und Nenner eine Rolle spielen.
            SimplifyBinaryOperationUtils.reduceLeadingCoefficientsInQuotientInApprox(termsLeft, termsRight);
            SimplifyBinaryOperationUtils.reduceLeadingCoefficientsInQuotient(termsLeft, termsRight);

            /*
             Prüft, ob man beispielsweise flgendermaßen kürzen kann: x*(10*a +
             25*b)*y/(35*c - 20*d) = x*(2*a + 5*b)*y/(7*c - 4*d).
             */
            SimplifyBinaryOperationUtils.reduceGCDInQuotient(termsLeft, termsRight);

            /*
             Prüft im Zähler und im Nenner, ob man beispielsweise flgendermaßen 
             kürzen kann: 10*x*(1/6 + y/14) = 5*x*(1/3 + y/7).
             */
            SimplifyBinaryOperationUtils.pullGCDOfCoefficientsInProducts(termsLeft);
            SimplifyBinaryOperationUtils.pullGCDOfCoefficientsInProducts(termsRight);

            /*
             Prüft, ob sich ganze Ausdrücke zu einer Konstanten kürzen lassen,
             etwa (5*a + 7*b)/(15*a + 21*b) = 1/3, (x - 3*y)/(12*y - 4*x) =
             -1/4 etc.
             */
            SimplifyBinaryOperationUtils.reduceFactorsInNumeratorAndFactorInDenominatorToConstant(termsLeft, termsRight);

            /*
             Prüft, ob sich (ganzzahlige Potenzen von) Ausdrücken aus Brüchen kürzen lassen, d.h. ob z.B. 
             (x^2*y + z*x^3)/(2*x - x^4) zu (x*y + z*x^2)/(2 - x^3) vereinfacht werden kann.
             */
            SimplifyBinaryOperationUtils.reduceSameExpressionInAllSummandsInQuotient(termsLeft, termsRight);

            /*
             Prüft, ob für RATIONALE Polynome (von nicht allzu hohem Grad) im Zähler und Nenner gekürzt werden können.
             */
            SimplifyBinaryOperationUtils.reducePolynomialFactorsInNumeratorAndDenominatorByGCD(termsLeft, termsRight);

            /*
             Prüft, ob bei (ganzzahlige Potenzen von) Ausdrücken aus Brüchen prinzipiell gekürzt werden kann, z. B. wird
             (ab^3+a^2)/(b^3+a) zu a gekürzt.
             */
            SimplifyBinaryOperationUtils.reduceGeneralFractionToNonFractionInQuotient(termsLeft, termsRight);

            /*
            Kürzen von Fakultäten mit ganzzahligen Differenzen (z.B. (x+3)!/x! = (x+1)*(x+2)*(x+3))
             */
            SimplifyBinaryOperationUtils.reduceFactorialsInQuotients(termsLeft, termsRight);

            return SimplifyUtilities.produceQuotient(termsLeft, termsRight);

        }

    }

    @Override
    public Expression orderSumsAndProducts() throws EvaluationException {

        if (this.isNotSum() && this.isNotProduct()) {
            return new BinaryOperation(this.left.orderSumsAndProducts(), this.right.orderSumsAndProducts(), this.type);
        }

        // Fall type = +.
        if (this.isSum()) {

            Expression result = ZERO;
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // Sammelt Konstanten im ersten Summanden. Beispiel: 2+x+3+y+sin(1) wird zu 5+sin(1)+x+y
            summands = SimplifyBinaryOperationUtils.collectConstantsAndConstantExpressionsInSum(summands);

            for (int i = summands.getBound() - 1; i >= 0; i--) {
                if (summands.get(i) == null) {
                    continue;
                }
                summands.put(i, summands.get(i).orderSumsAndProducts());
                if (result.equals(ZERO)) {
                    result = summands.get(i).orderSumsAndProducts();
                } else {
                    result = summands.get(i).orderSumsAndProducts().add(result);
                }
            }

            return result;

        } else {

            // Fall type = *.
            Expression result = ONE;
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // Sammelt Konstanten im ersten Summanden. Beispiel: 2*x*3*y*sin(1) wird zu 6*sin(1)*x*y
            factors = SimplifyBinaryOperationUtils.collectConstantsAndConstantExpressionsInProduct(factors);

            for (int i = factors.getBound() - 1; i >= 0; i--) {
                if (factors.get(i) == null) {
                    continue;
                }
                factors.put(i, factors.get(i).orderSumsAndProducts());
                if (result.equals(ONE)) {
                    result = factors.get(i).orderSumsAndProducts();
                } else {
                    result = factors.get(i).orderSumsAndProducts().mult(result);
                }
            }

            return result;

        }

    }

    @Override
    public Expression orderDifferencesAndQuotients() throws EvaluationException {

        Expression result;

        ExpressionCollection termsLeft = new ExpressionCollection();
        ExpressionCollection termsRight = new ExpressionCollection();

        if (this.isSum() || this.isDifference()) {

            SimplifyBinaryOperationUtils.orderDifference(this, termsLeft, termsRight);
            for (int i = 0; i < termsLeft.getBound(); i++) {
                termsLeft.put(i, termsLeft.get(i).orderDifferencesAndQuotients());
            }
            for (int i = 0; i < termsRight.getBound(); i++) {
                termsRight.put(i, termsRight.get(i).orderDifferencesAndQuotients());
            }
            result = SimplifyUtilities.produceDifference(termsLeft, termsRight);

        } else if (this.isProduct() || this.isQuotient()) {

            SimplifyBinaryOperationUtils.orderQuotient(this, termsLeft, termsRight);
            for (int i = 0; i < termsLeft.getBound(); i++) {
                termsLeft.put(i, termsLeft.get(i).orderDifferencesAndQuotients());
            }
            for (int i = 0; i < termsRight.getBound(); i++) {
                termsRight.put(i, termsRight.get(i).orderDifferencesAndQuotients());
            }
            result = SimplifyUtilities.produceQuotient(termsLeft, termsRight);

        } else {
            // Hier ist expr.getType() == TypeBinary.POW.
            result = this.left.orderDifferencesAndQuotients().pow(this.right.orderDifferencesAndQuotients());
        }

        return result;

    }

    @Override
    public Expression simplifyCollectProducts() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln Faktoren sammeln.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyCollectProducts());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isNotProduct()) {
            // Im linken und rechten Teil einzeln Faktoren sammeln.
            return new BinaryOperation(this.left.simplifyCollectProducts(), this.right.simplifyCollectProducts(), this.type);
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(this);

        //Ab hier ist type == *.
        // Zunächst in jedem Faktor einzeln Faktoren sammeln.
        for (int i = 0; i < factors.getBound(); i++) {
            factors.put(i, factors.get(i).simplifyCollectProducts());
        }

        SimplifyBinaryOperationUtils.collectFactorsInProduct(factors);
        return SimplifyUtilities.produceProduct(factors);

    }

    @Override
    public Expression simplifyFactorize() throws EvaluationException {

        if (this.isSum()) {

            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFactorize());
            }
            // Eigentliche Faktorisierung.
            SimplifyBinaryOperationUtils.simplifyFactorizeAntiEquivalentExpressionsInSums(summands);
            SimplifyBinaryOperationUtils.simplifyFactorizeInSums(summands);
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isDifference()) {

            Expression expr = this.left.simplifyFactorize().sub(this.right.simplifyFactorize());
            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);
            // Eigentliche Faktorisierung.
            SimplifyBinaryOperationUtils.simplifyFactorizeAntiEquivalentExpressionsInDifferences(summandsLeft, summandsRight);
            SimplifyBinaryOperationUtils.simplifyFactorizeInDifferences(summandsLeft, summandsRight);
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {

            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorize());
            }
            return SimplifyUtilities.produceProduct(factors);

        }

        // Hier ist type == DIV oder type == POW.
        return new BinaryOperation(this.left.simplifyFactorize(), this.right.simplifyFactorize(), this.type);

    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInSums() throws EvaluationException {

        if (this.isProduct()) {
            // In jedem Faktor einzeln faktorisieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorizeAllButRationalsInSums());
            }
            return SimplifyUtilities.produceProduct(factors);
        }
        if (this.isNotSum()) {
            return new BinaryOperation(this.left.simplifyFactorizeAllButRationalsInSums(), this.right.simplifyFactorizeAllButRationalsInSums(), this.type);
        }

        // Ab hier muss this als type + besitzen.
        ExpressionCollection summands = SimplifyUtilities.getSummands(this);
        // In jedem Summanden einzeln faktorisieren
        for (int i = 0; i < summands.getBound(); i++) {
            summands.put(i, summands.get(i).simplifyFactorizeAllButRationalsInSums());
        }

        // Eigentliche Faktorisierung.
        SimplifyBinaryOperationUtils.simplifyFactorizeAllButRationalsInSums(summands);
        // Ergebnis bilden.
        return SimplifyUtilities.produceSum(summands);

    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInDifferences() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFactorizeAllButRationalsInDifferences());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorizeAllButRationalsInDifferences());
            }
            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isQuotient() || this.isPower()) {
            return new BinaryOperation(this.left.simplifyFactorizeAllButRationalsInDifferences(), this.right.simplifyFactorizeAllButRationalsInDifferences(), this.type);
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
        // In jedem Summanden einzeln faktorisieren
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            summandsLeft.put(i, summandsLeft.get(i).simplifyFactorizeAllButRationalsInDifferences());
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            summandsRight.put(i, summandsRight.get(i).simplifyFactorizeAllButRationalsInDifferences());
        }

        // Eigentliche Faktorisierung.
        SimplifyBinaryOperationUtils.simplifyFactorizeAllButRationalsInDifferences(summandsLeft, summandsRight);
        // Ergebnis bilden.
        return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

    }

    @Override
    public Expression simplifyFactorizeAllButRationals() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln faktorisieren
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFactorizeAllButRationals());
            }
            // Eigentliche Faktorisierung.
            SimplifyBinaryOperationUtils.simplifyFactorizeAllButRationalsInSums(summands);
            SimplifyBinaryOperationUtils.simplifyFactorizeAllButRationalsForAntiEquivalentExpressionsInSums(summands);
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isDifference()) {

            Expression expr = this.left.simplifyFactorizeAllButRationals().sub(this.right.simplifyFactorizeAllButRationals());

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);
            // Eigentliche Faktorisierung.
            SimplifyBinaryOperationUtils.simplifyFactorizeAllButRationalsInDifferences(summandsLeft, summandsRight);
            SimplifyBinaryOperationUtils.simplifyFactorizeAllButRationalsForAntiEquivalentExpressionsInDifferences(summandsLeft, summandsRight);
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {
            // In jedem Faktor einzeln faktorisieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorizeAllButRationals());
            }
            return SimplifyUtilities.produceProduct(factors);
        }

        // Hier ist type == DIV oder type == POW.
        return new BinaryOperation(this.left.simplifyFactorizeAllButRationals(), this.right.simplifyFactorizeAllButRationals(), this.type);

    }

    @Override
    public Expression simplifyReduceQuotients() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReduceQuotients());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReduceQuotients());
            }
            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isDifference() || this.isPower()) {
            return new BinaryOperation(this.left.simplifyReduceQuotients(), this.right.simplifyReduceQuotients(), this.type);
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(this);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);

        // In jedem Faktor einzeln kürzen
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            factorsEnumerator.put(i, factorsEnumerator.get(i).simplifyReduceQuotients());
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            factorsDenominator.put(i, factorsDenominator.get(i).simplifyReduceQuotients());
        }

        // Eigentliches Kürzen.
        SimplifyBinaryOperationUtils.simplifyReduceFactorsInQuotients(factorsEnumerator, factorsDenominator);
        // Ergebnis bilden.
        return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

    }

    @Override
    public Expression simplifyPullApartPowers() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyPullApartPowers());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        }

        if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyPullApartPowers());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        if (this.isDifference() || this.isQuotient()) {
            return new BinaryOperation(this.left.simplifyPullApartPowers(), this.right.simplifyPullApartPowers(), this.type);
        }

        // Ab hier ist type == TypeBinary.POW
        Expression expr = this.left.simplifyPullApartPowers().pow(this.right.simplifyPullApartPowers());
        Expression exprSimplified;

        exprSimplified = SimplifyExpLogUtils.splitPowersInProduct(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        exprSimplified = SimplifyExpLogUtils.splitPowersInQuotient(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        return expr;

    }

    @Override
    public Expression simplifyMultiplyExponents() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyMultiplyExponents());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyMultiplyExponents());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isDifference() || this.isQuotient()) {

            return new BinaryOperation(this.left.simplifyMultiplyExponents(), this.right.simplifyMultiplyExponents(), this.type);

        }

        // Hier ist this.type == TypeBinary.POW
        Expression leftSimplified = this.left.simplifyMultiplyExponents();
        if (leftSimplified.isPower()) {
            return ((BinaryOperation) leftSimplified).getLeft().pow(((BinaryOperation) leftSimplified).getRight().mult(this.right));
        }
        return this;

    }

    @Override
    public Expression simplifyFunctionalRelations() throws EvaluationException {

        BinaryOperation expr = this;
        Expression exprSimplified;

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionalgleichungen anwenden.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFunctionalRelations());
            }

            // sinh(x) + cosh(x) = exp(x)
            SimplifyFunctionalRelationsUtils.sumOfTwoFunctions(summands, TypeFunction.sinh, TypeFunction.cosh, TypeFunction.exp);
            //cos(x)^2 + sin(x)^2 = 1
            SimplifyFunctionalRelationsUtils.reduceSumOfSquaresOfSineAndCosine(summands);
            //1 + tan(x)^2 = sec(x)^2
            SimplifyFunctionalRelationsUtils.reduceOnePlusFunctionSquareToFunctionSquare(summands, TypeFunction.tan, TypeFunction.sec);
            //1 + cot(x)^2 = cosec(x)^2
            SimplifyFunctionalRelationsUtils.reduceOnePlusFunctionSquareToFunctionSquare(summands, TypeFunction.cot, TypeFunction.cosec);
            //1 + sinh(x)^2 = cosh(x)^2
            SimplifyFunctionalRelationsUtils.reduceOnePlusFunctionSquareToFunctionSquare(summands, TypeFunction.sinh, TypeFunction.cosh);
            //1 + cosech(x)^2 = coth(x)^2
            SimplifyFunctionalRelationsUtils.reduceOnePlusFunctionSquareToFunctionSquare(summands, TypeFunction.cosech, TypeFunction.coth);

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        }

        if (this.isDifference()) {

            // Im Minuenden und Subtrahenden einzeln Funktionalgleichungen anwenden.
            Expression simplifiedDifference = this.left.simplifyFunctionalRelations().sub(this.right.simplifyFunctionalRelations());
            if (!(simplifiedDifference instanceof BinaryOperation)) {
                return simplifiedDifference;
            }
            expr = (BinaryOperation) simplifiedDifference;

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);

            //cosh(x) - sinh(x) = exp(-x) bzw. sinh(x) - cosh(x) = -exp(-x)
            SimplifyFunctionalRelationsUtils.reduceCoshMinusSinhToExp(summandsLeft, summandsRight);
            //cosh(x)^2 - sinh(x)^2 = 1 bzw. sinh(x)^2 - cosh(x)^2 = -1
            SimplifyFunctionalRelationsUtils.reduceDifferenceOfSquaresOfHypSineAndHypCosine(summandsLeft, summandsRight);
            //1 - tanh(x)^2 = sech(x)^2 bzw. tanh(x)^2 - 1 = -sech(x)^2
            SimplifyFunctionalRelationsUtils.reduceOneMinusFunctionSquareToFunctionSquare(summandsLeft, summandsRight, TypeFunction.tanh, TypeFunction.sech);
            //1 - sech(x)^2 = tanh(x)^2 bzw. sech(x)^2 - 1 = -tanh(x)^2
            SimplifyFunctionalRelationsUtils.reduceOneMinusFunctionSquareToFunctionSquare(summandsLeft, summandsRight, TypeFunction.sech, TypeFunction.tanh);
            //1 - sin(x)^2 = cos(x)^2 bzw. sin(x)^2 - 1 = -cos(x)^2
            SimplifyFunctionalRelationsUtils.reduceOneMinusFunctionSquareToFunctionSquare(summandsLeft, summandsRight, TypeFunction.sin, TypeFunction.cos);
            //1 - cos(x)^2 = sin(x)^2 bzw. cos(x)^2 - 1 = -sin(x)^2
            SimplifyFunctionalRelationsUtils.reduceOneMinusFunctionSquareToFunctionSquare(summandsLeft, summandsRight, TypeFunction.cos, TypeFunction.sin);
            //cosh(x)^2 - 1 = sinh(x)^2 bzw. 1 - cosh(x)^2 = -sinh(x)^2
            SimplifyFunctionalRelationsUtils.reduceFunctionSquareMinusOneToFunctionSquare(summandsLeft, summandsRight, TypeFunction.cosh, TypeFunction.sinh);
            //coth(x)^2 - 1 = cosech(x)^2 bzw. 1 - coth(x)^2 = -cosech(x)^2
            SimplifyFunctionalRelationsUtils.reduceFunctionSquareMinusOneToFunctionSquare(summandsLeft, summandsRight, TypeFunction.coth, TypeFunction.cosech);
            //sec(x)^2 - 1 = tan(x)^2 bzw. 1 - sec(x)^2 = -tan(x)^2
            SimplifyFunctionalRelationsUtils.reduceFunctionSquareMinusOneToFunctionSquare(summandsLeft, summandsRight, TypeFunction.sec, TypeFunction.tan);
            //cosec(x)^2 - 1 = cot(x)^2 bzw. 1 - cosec(x)^2 = -cot(x)^2
            SimplifyFunctionalRelationsUtils.reduceFunctionSquareMinusOneToFunctionSquare(summandsLeft, summandsRight, TypeFunction.cosec, TypeFunction.cot);

            // Ergebnis bilden.
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        }

        if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionalgleichungen anwenden.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFunctionalRelations());
            }

            //Potenzen von rationalen Zahlen sammeln
            SimplifyExpLogUtils.collectPowersOfRationalsWithSameExponentInProduct(factors);
            //Exponentialfunktionen sammeln
            SimplifyExpLogUtils.collectExponentialFunctionsInProduct(factors);
            //Produkte von Beträgen zu einem einzigen Betrag machen
            SimplifyFunctionalRelationsUtils.pullTogetherProductsOfMultiplicativeFunctions(factors, TypeFunction.abs);
            //Produkte von Signum zu einem einzigen Signum machen
            SimplifyFunctionalRelationsUtils.pullTogetherProductsOfMultiplicativeFunctions(factors, TypeFunction.sgn);
            //x*sgn(x) = abs(x)
            SimplifyFunctionalRelationsUtils.reduceProductOfIdAndSgnToAbs(factors);
            //abs(x)^k*sgn(x)^k = x^k (genauer = id(x)^k)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.abs, TypeFunction.sgn, TypeFunction.id);
            //cos(x)*tan(x) = sin(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.cos, TypeFunction.tan, TypeFunction.sin);
            //sin(x)*sec(x) = tan(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.sin, TypeFunction.sec, TypeFunction.tan);
            //sin(x)*cot(x) = cos(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.sin, TypeFunction.cot, TypeFunction.cos);
            //cos(x)*cosec(x) = cot(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.cos, TypeFunction.cosec, TypeFunction.cot);
            //sec(x)*cot(x) = cosec(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.sec, TypeFunction.cot, TypeFunction.cosec);
            //cosec(x)*tan(x) = sec(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.cosec, TypeFunction.tan, TypeFunction.sec);
            //cosh(x)*tanh(x) = sinh(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.cosh, TypeFunction.tanh, TypeFunction.sinh);
            //sinh(x)*sech(x) = tanh(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.sinh, TypeFunction.sech, TypeFunction.tanh);
            //sinh(x)*coth(x) = cosh(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.sinh, TypeFunction.coth, TypeFunction.cosh);
            //cosh(x)*cosech(x) = coth(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.cosh, TypeFunction.cosech, TypeFunction.coth);
            //sech(x)*coth(x) = cosech(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.sech, TypeFunction.coth, TypeFunction.cosech);
            //cosec(x)*tan(x) = sec(x)
            SimplifyFunctionalRelationsUtils.productOfTwoFunctions(factors, TypeFunction.cosech, TypeFunction.tanh, TypeFunction.sech);
            //sin(x)*cosec(x) = 1
            SimplifyFunctionalRelationsUtils.productOfTwoFunctionsEqualsOne(factors, TypeFunction.sin, TypeFunction.cosec);
            //cos(x)*sec(x) = 1
            SimplifyFunctionalRelationsUtils.productOfTwoFunctionsEqualsOne(factors, TypeFunction.cos, TypeFunction.sec);
            //tan(x)*cot(x) = 1
            SimplifyFunctionalRelationsUtils.productOfTwoFunctionsEqualsOne(factors, TypeFunction.tan, TypeFunction.cot);
            //sinh(x)*cosech(x) = 1
            SimplifyFunctionalRelationsUtils.productOfTwoFunctionsEqualsOne(factors, TypeFunction.sinh, TypeFunction.cosech);
            //cosh(x)*sech(x) = 1
            SimplifyFunctionalRelationsUtils.productOfTwoFunctionsEqualsOne(factors, TypeFunction.cosh, TypeFunction.sech);
            //tanh(x)*coth(x) = 1
            SimplifyFunctionalRelationsUtils.productOfTwoFunctionsEqualsOne(factors, TypeFunction.tanh, TypeFunction.coth);
            //sin(x)*cos(x) = sin(2*x)/2
            SimplifyFunctionalRelationsUtils.productOfTwoFunctionsToFunctionOfDoubleArgument(factors, TypeFunction.sin, TypeFunction.cos);
            //sinh(x)*cosh(x) = sinh(2*x)/2
            SimplifyFunctionalRelationsUtils.productOfTwoFunctionsToFunctionOfDoubleArgument(factors, TypeFunction.sinh, TypeFunction.cosh);
            // x!*(-1-x)! = x/sin(pi*x) (Ergänzungssatz)
            SimplifyFunctionalRelationsUtils.collectFactorialsInProductByReflectionFormula(factors);

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        if (this.isQuotient()) {

            // Im Dividenden und Divisor einzeln Funktionalgleichungen anwenden.
            Expression simplifiedQuotient = this.left.simplifyFunctionalRelations().div(this.right.simplifyFunctionalRelations());
            if (!(simplifiedQuotient instanceof BinaryOperation)) {
                return simplifiedQuotient;
            }
            expr = (BinaryOperation) simplifiedQuotient;

            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(expr);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr);

            //Potenzen von rationalen Zahlen sammeln
            SimplifyExpLogUtils.collectPowersOfRationalsWithSameExponentInQuotient(factorsEnumerator, factorsDenominator);
            //Exponentialfunktionen sammeln
            SimplifyExpLogUtils.collectExponentialFunctionsInQuotient(factorsEnumerator, factorsDenominator);
            //Bringt allgemeine nichtkonstante Exponentialfunktionen aus dem Nenner in den Zähler
            SimplifyExpLogUtils.bringNonConstantExponentialFunctionsToNumerator(factorsEnumerator, factorsDenominator);
            //Logarithmen zur Basis 10 zu rationalen Zahlen kürzen
            SimplifyExpLogUtils.simplifyQuotientsOfLogarithms(factorsEnumerator, factorsDenominator, TypeFunction.lg);
            //Logarithmen zur Basis e zu rationalen Zahlen kürzen
            SimplifyExpLogUtils.simplifyQuotientsOfLogarithms(factorsEnumerator, factorsDenominator, TypeFunction.ln);
            //Quotienten von Beträgen zu einem einzigen Betrag machen
            SimplifyFunctionalRelationsUtils.pullTogetherQuotientsOfMultiplicativeFunctions(factorsEnumerator, factorsDenominator, TypeFunction.abs);
            //Quotienten von Signum zu einem einzigen Signum machen
            SimplifyFunctionalRelationsUtils.pullTogetherQuotientsOfMultiplicativeFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sgn);
            //sin(x)/cos(x) = tan(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sin, TypeFunction.cos, TypeFunction.tan);
            //cos(x)/sin(x) = cot(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cos, TypeFunction.sin, TypeFunction.cot);
            //tan(x)/sin(x) = sec(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.tan, TypeFunction.sin, TypeFunction.sec);
            //cot(x)/cos(x) = cosec(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cot, TypeFunction.cos, TypeFunction.cosec);
            //sin(x)/tan(x) = cos(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sin, TypeFunction.tan, TypeFunction.cos);
            //cos(x)/cot(x) = sin(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cos, TypeFunction.cot, TypeFunction.sin);
            //sec(x)/cosec(x) = tan(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sec, TypeFunction.cosec, TypeFunction.tan);
            //cosec(x)/sec(x) = cot(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cosec, TypeFunction.sec, TypeFunction.cot);
            //sinh(x)/cosh(x) = tanh(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sinh, TypeFunction.cosh, TypeFunction.tanh);
            //cosh(x)/sinh(x) = coth(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cosh, TypeFunction.sinh, TypeFunction.coth);
            //tanh(x)/sinh(x) = sech(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.tanh, TypeFunction.sinh, TypeFunction.sech);
            //coth(x)/cosh(x) = cosech(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.coth, TypeFunction.cosh, TypeFunction.cosech);
            //sinh(x)/tanh(x) = cosh(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sinh, TypeFunction.tanh, TypeFunction.cosh);
            //cosh(x)/coth(x) = sinh(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cosh, TypeFunction.coth, TypeFunction.sinh);
            //sech(x)/cosech(x) = tanh(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sech, TypeFunction.cosech, TypeFunction.tanh);
            //cosech(x)/sech(x) = coth(x)
            SimplifyFunctionalRelationsUtils.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cosech, TypeFunction.sech, TypeFunction.coth);
            //1/sin(x) = cosec(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.sin, TypeFunction.cosec);
            //1/cos(x) = sec(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cos, TypeFunction.sec);
            //1/tan(x) = cot(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.tan, TypeFunction.cot);
            //1/cot(x) = tan(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cot, TypeFunction.tan);
            //1/sec(x) = cos(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.sec, TypeFunction.cos);
            //1/cosec(x) = sin(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cosec, TypeFunction.sin);
            //1/sinh(x) = cosech(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.sinh, TypeFunction.cosech);
            //1/cosh(x) = sech(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cosh, TypeFunction.sech);
            //1/tanh(x) = coth(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.tanh, TypeFunction.coth);
            //1/coth(x) = tanh(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.coth, TypeFunction.tanh);
            //1/sech(x) = cosh(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.sech, TypeFunction.cosh);
            //1/cosech(x) = sinh(x)
            SimplifyFunctionalRelationsUtils.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cosech, TypeFunction.sinh);

            // Ergebnis bilden.
            return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

        }

        if (this.isPower()) {

            // In Basis und Exponenten einzeln Funktionalgleichungen anwenden.
            Expression simplifiedPower = this.left.simplifyFunctionalRelations().pow(this.right.simplifyFunctionalRelations());
            if (!(simplifiedPower instanceof BinaryOperation)) {
                return simplifiedPower;
            }
            expr = (BinaryOperation) simplifiedPower;

            exprSimplified = SimplifyBinaryOperationUtils.reducePowerOfTenAndSumsOfLog10(expr);
            if (!exprSimplified.equals(this)) {
                return exprSimplified;
            }

            exprSimplified = SimplifyBinaryOperationUtils.reducePowerOfTenAndDifferencesOfLog10(expr);
            if (!exprSimplified.equals(this)) {
                return exprSimplified;
            }

            exprSimplified = SimplifyFunctionUtils.powerOfSgn(expr);
            if (!exprSimplified.equals(this)) {
                return exprSimplified;
            }

        }

        return expr;

    }

//    @Override
    public Expression simplifyExpandAndCollectEquivalentsIfShorter2() throws EvaluationException {

        Expression expr = this;
        Expression exprSimplified;

        try {
            // "Kurzes / Schnelles" Ausmultiplizieren soll stattfinden.
            if (this.isSum() || this.isDifference()) {

                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
                Expression summandSimplified;
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    summandSimplified = summandsLeft.get(i).simplifyExpandAndCollectEquivalentsIfShorter();
                    if (summandSimplified.getLength() < summandsLeft.get(i).getLength()) {
                        summandsLeft.put(i, summandSimplified);
                    }
                }
                for (int i = 0; i < summandsRight.getBound(); i++) {
                    summandSimplified = summandsRight.get(i).simplifyExpandAndCollectEquivalentsIfShorter();
                    if (summandSimplified.getLength() < summandsRight.get(i).getLength()) {
                        summandsRight.put(i, summandSimplified);
                    }
                }

                expr = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

            } else if (this.isProduct()) {

                ExpressionCollection factors = SimplifyUtilities.getFactors(this);
                Expression factorSimplified;
                for (int i = 0; i < factors.getBound(); i++) {
                    factorSimplified = factors.get(i).simplifyExpandAndCollectEquivalentsIfShorter();
                    if (factorSimplified.getLength() < factors.get(i).getLength()) {
                        factors.put(i, factorSimplified);
                    }
                }
                expr = SimplifyUtilities.produceProduct(factors);

            }

            if (this.isQuotient()) {
                /* 
                 Bei einem Quotienten soll man im Zähler und im Nenner separat beurteilen, 
                 ob sich diese Vereinfachungsart lohnt. 
                 */
                exprSimplified = ((BinaryOperation) expr).getLeft().simplifyExpandAndCollectEquivalentsIfShorter().div(
                        ((BinaryOperation) expr).getRight().simplifyExpandAndCollectEquivalentsIfShorter());
                exprSimplified = exprSimplified.simplify(simplifyTypesExpandAndCollectIfShorter);
            } else {
                exprSimplified = expr.simplifyExpand(TypeExpansion.SHORT);
                exprSimplified = exprSimplified.simplify(simplifyTypesExpandAndCollectIfShorter);
            }
            if (exprSimplified.getLength() < expr.getLength()) {
                return exprSimplified;
            }
        } catch (EvaluationException e) {
            return expr;
        }

        return expr;

    }

    @Override
    public Expression simplifyExpandAndCollectEquivalentsIfShorter() throws EvaluationException {

        Expression expr = this;
        Expression exprOptimized;

        // "Kurzes / Schnelles" Ausmultiplizieren soll stattfinden.
        if (this.isSum() || this.isDifference()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
            Expression summandSimplified;
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandSimplified = summandsLeft.get(i).simplifyExpandAndCollectEquivalentsIfShorter();
                if (summandSimplified.getLength() < summandsLeft.get(i).getLength()) {
                    summandsLeft.put(i, summandSimplified);
                }
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandSimplified = summandsRight.get(i).simplifyExpandAndCollectEquivalentsIfShorter();
                if (summandSimplified.getLength() < summandsRight.get(i).getLength()) {
                    summandsRight.put(i, summandSimplified);
                }
            }

            expr = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            Expression factorSimplified;
            for (int i = 0; i < factors.getBound(); i++) {
                factorSimplified = factors.get(i).simplifyExpandAndCollectEquivalentsIfShorter();
                if (factorSimplified.getLength() < factors.get(i).getLength()) {
                    factors.put(i, factorSimplified);
                }
            }
            expr = SimplifyUtilities.produceProduct(factors);

        }

        // Verusuch: Ausmultiplizieren, zusammenfassen und prüfen, ob das Ergebnis kürzer ist.
        if (expr.isQuotient()) {
            /* 
             Bei einem Quotienten soll man im Zähler und im Nenner separat beurteilen, 
             ob sich diese Vereinfachungsart lohnt. 
             */
            exprOptimized = ((BinaryOperation) expr).getLeft().simplifyExpandAndCollectEquivalentsIfShorter().div(
                    ((BinaryOperation) expr).getRight().simplifyExpandAndCollectEquivalentsIfShorter());
            exprOptimized = exprOptimized.simplify(simplifyTypesExpandAndCollectIfShorter);
        } else {
            exprOptimized = expr.simplifyExpand(TypeExpansion.SHORT);
            if (!exprOptimized.equivalent(expr)) {
                /* 
                Erneutes simplify(...) nur anwenden, wenn die Ausdrücke wirklich verschieden sind.
                Sonst sind Endlosschleifen möglich! 
                 */
                exprOptimized = exprOptimized.simplify(simplifyTypesExpandAndCollectIfShorter);
            }
        }
        if (exprOptimized.getLength() >= expr.getLength()) {
            // Dann die letzte Vereinfachung rückgängig machen.
            exprOptimized = expr;
        } else {
            // Dann die letzte Vereinfachung akzeptieren.
            expr = exprOptimized;
        }

        /* 
        Verusuch: Auf einen Nenner bringen und dann den Zähler und Nenner separat 
        analog vereinfachen und prüfen, ob das Ergebnis kürzer ist.
         */
        exprOptimized = expr.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
        if (!exprOptimized.equivalent(expr)) {
            /* 
            Erneutes simplify(...) nur anwenden, wenn die Ausdrücke wirklich verschieden sind.
            Sonst sind Endlosschleifen möglich! 
             */
            exprOptimized = exprOptimized.simplify(simplifyTypesExpandAndCollectIfShorter);
            if (exprOptimized.isQuotient()) {
                exprOptimized = ((BinaryOperation) exprOptimized).getLeft().simplifyExpandAndCollectEquivalentsIfShorter().div(
                        ((BinaryOperation) exprOptimized).getRight().simplifyExpandAndCollectEquivalentsIfShorter());
            }
            if (exprOptimized.getLength() < expr.getLength()) {
                // Dann die letzte Vereinfachung rückgängig machen.
                return exprOptimized;
            }
        }

        return expr;

    }

    @Override
    public Expression simplifyCollectLogarithms() throws EvaluationException {

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
        // Faktoren vor Logarithmusfunktionen zur Basis 10 in die Logarithmen hineinziehen.
        SimplifyExpLogUtils.pullFactorsIntoLogarithms(summandsLeft, TypeFunction.lg);
        SimplifyExpLogUtils.pullFactorsIntoLogarithms(summandsRight, TypeFunction.lg);
        // Faktoren vor Logarithmusfunktionen zur Basis e in die Logarithmen hineinziehen.
        SimplifyExpLogUtils.pullFactorsIntoLogarithms(summandsLeft, TypeFunction.ln);
        SimplifyExpLogUtils.pullFactorsIntoLogarithms(summandsRight, TypeFunction.ln);
        Expression expr = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        if (expr.isSum()) {

            // In jedem Summanden einzeln Logarithmen sammeln.
            ExpressionCollection summands = SimplifyUtilities.getSummands(expr);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyCollectLogarithms());
            }

            //Logarithmusfunktionen zur Basis 10 in einer Summe sammeln
            SimplifyExpLogUtils.collectLogarithmsInSum(summands, TypeFunction.lg);
            //Logarithmusfunktionen zur Basis e in einer Summe sammeln
            SimplifyExpLogUtils.collectLogarithmsInSum(summands, TypeFunction.ln);

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (expr.isDifference()) {

            // Im Minuenden und Subtrahenden einzeln Logarithmen sammeln.
            expr = ((BinaryOperation) expr).getLeft().simplifyCollectLogarithms().sub(((BinaryOperation) expr).getRight().simplifyCollectLogarithms());

            summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);

            //Logarithmusfunktionen zur Basis 10 in einer Differenz sammeln
            SimplifyExpLogUtils.collectLogarithmsInDifference(summandsLeft, summandsRight, TypeFunction.lg);
            //Logarithmusfunktionen zur Basis e in einer Differenz sammeln
            SimplifyExpLogUtils.collectLogarithmsInDifference(summandsLeft, summandsRight, TypeFunction.ln);

            // Ergebnis bilden.
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (expr.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
            // In jedem Faktor einzeln Logarithmen sammeln.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyCollectLogarithms());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (expr instanceof BinaryOperation) {
            return new BinaryOperation(((BinaryOperation) expr).getLeft().simplifyCollectLogarithms(),
                    ((BinaryOperation) expr).getRight().simplifyCollectLogarithms(),
                    ((BinaryOperation) expr).getType());
        }

        return expr;

    }

    @Override
    public Expression simplifyExpandLogarithms() throws EvaluationException {

        if (this.isSum() || this.isDifference()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
            // In jedem Summanden einzeln Logarithmen auseinanderziehen.
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).simplifyExpandLogarithms());
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).simplifyExpandLogarithms());
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct() || this.isQuotient()) {

            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(this);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);
            // In jedem Faktor einzeln Logarithmen auseinanderziehen.
            for (int i = 0; i < factorsEnumerator.getBound(); i++) {
                factorsEnumerator.put(i, factorsEnumerator.get(i).simplifyExpandLogarithms());
            }
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                factorsDenominator.put(i, factorsDenominator.get(i).simplifyExpandLogarithms());
            }
            return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

        }

        // Dann ist this eine Potenz.
        return this.left.simplifyExpandLogarithms().pow(this.right.simplifyExpandLogarithms());

    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReplaceExponentialFunctionsByDefinitions());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReplaceExponentialFunctionsByDefinitions());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isPower() && this.left.isConstant()) {
            // Nur dann ersetzen, wenn die Basis konstant ist.
            return this.left.ln().mult(this.right).exp();
        }

        return new BinaryOperation(this.left.simplifyReplaceExponentialFunctionsByDefinitions(),
                this.right.simplifyReplaceExponentialFunctionsByDefinitions(),
                this.type);

    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var));
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var));
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isPower() && !this.left.contains(var)) {
            // Nur dann ersetzen, wenn die Basis bzgl. var konstant ist.
            return this.left.ln().mult(this.right).exp();
        }

        return new BinaryOperation(this.left.simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var),
                this.right.simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var),
                this.type);

    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReplaceTrigonometricalFunctionsByDefinitions());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReplaceTrigonometricalFunctionsByDefinitions());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        return new BinaryOperation(this.left.simplifyReplaceTrigonometricalFunctionsByDefinitions(),
                this.right.simplifyReplaceTrigonometricalFunctionsByDefinitions(),
                this.type);

    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var));
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var));
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        return new BinaryOperation(this.left.simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var),
                this.right.simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var),
                this.type);

    }

    /* 
     Es folgen Methoden für die Integration von Funktionen vom Typ exp(ax+b) * h_1(c_1x+d_1)^n_1 * ... h_m(c_mx+d_m)^n_m
     mit h_i = sin oder = cos.
     */
    public static boolean isPolynomialInVariousExponentialAndTrigonometricalFunctions(Expression f, String var) {
        if (!f.contains(var)) {
            return true;
        }
        if (f.isSum() || f.isDifference() || f.isProduct()) {
            return isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var)
                    && isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) f).getRight(), var);
        }
        if (f.isQuotient()) {
            return isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var)
                    && !((BinaryOperation) f).getRight().contains(var);
        }
        if (f.isPositiveIntegerPower()) {
            return isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var);
        }
        if (f.isFunction()) {
            return (f.isFunction(TypeFunction.exp) || f.isFunction(TypeFunction.cos)
                    || f.isFunction(TypeFunction.sin))
                    && SimplifyPolynomialUtils.isLinearPolynomial(((Function) f).getLeft(), var);
        }
        if (f instanceof Operator) {
            return !f.contains(var);
        }
        return false;
    }

    private static BigInteger getUpperBoundForSummands(Expression f, String var) {

        if (f.isSum()) {
            BigInteger numberOfSummands = BigInteger.ZERO;
            ExpressionCollection summands = SimplifyUtilities.getSummands(f);
            for (int i = 0; i < summands.getBound(); i++) {
                if (summands.get(i) == null) {
                    continue;
                }
                numberOfSummands = numberOfSummands.add(getUpperBoundForSummands(summands.get(i), var));
            }
            return numberOfSummands;
        } else if (f.isDifference()) {
            return getUpperBoundForSummands(((BinaryOperation) f).getLeft(), var).add(getUpperBoundForSummands(((BinaryOperation) f).getRight(), var));
        } else if (f.isProduct()) {
            BigInteger numberOfSummands = BigInteger.ONE;
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i) == null) {
                    continue;
                }
                numberOfSummands = numberOfSummands.multiply(getUpperBoundForSummands(factors.get(i), var));
            }
            return numberOfSummands;
        } else if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
            return getUpperBoundForSummands(((BinaryOperation) f).getLeft(), var);
        } else if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isPositive()
                && ((Constant) ((BinaryOperation) f).getRight()).getValue().compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0) {

            Expression base = ((BinaryOperation) f).getLeft();
            int exponent = ((Constant) ((BinaryOperation) f).getRight()).getValue().intValue();

            /*
             Fall: Basis ist sin oder cos mit linearem Argument. Auf Linearität 
             des Arguments wird nicht geprüft, da dies vor dem Aufruf sichergestellt
             werden muss.
             */
            if (base.isFunction(TypeFunction.sin) || f.isFunction(TypeFunction.cos)) {
                return BigInteger.valueOf(exponent / 2 + 1);
            }

            // Fall: Basis ist keine trigonometrische Funktion.
            BigInteger numberOfSummandsInBaseAsBigInt = getUpperBoundForSummands(base, var);
            if (numberOfSummandsInBaseAsBigInt.compareTo(BigInteger.ZERO) > 0
                    && numberOfSummandsInBaseAsBigInt.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
                int numberOfSummandsInBase = numberOfSummandsInBaseAsBigInt.intValue();
                // Anzahl der Summanden in (a_1 + ... + a_n)^k ist (n - 1 + k)!/[(n - 1)! * k!]
                return ArithmeticUtils.factorial(numberOfSummandsInBase - 1 + exponent).divide(ArithmeticUtils.factorial(numberOfSummandsInBase - 1).multiply(ArithmeticUtils.factorial(exponent)));
            }
        } else if (f.isFunction(TypeFunction.exp) || f.isFunction(TypeFunction.cos) || f.isFunction(TypeFunction.sin)) {
            return BigInteger.ONE;
        } else if (f instanceof Operator) {
            if (!f.contains(var)) {
                return BigInteger.ONE;
            }
        }

        // Dann ist das kein Polynom in komplexen Exponentialfunktionen.
        return BigInteger.valueOf(-1);

    }

    private static Expression expandPowerOfCos(Expression argument, int n) {
        // Achtung: Im Folgenden findet keine Validierung für n statt. Dies muss im Vorfeld stattfinden.
        Expression result = ZERO;

        if (n % 2 == 0) {
            for (int i = 0; i < n / 2; i++) {
                result = result.add(new Constant(ArithmeticUtils.bin(n, i)).mult(new Constant(n - 2 * i).mult(argument).cos()).div(
                        new Constant(BigInteger.valueOf(2).pow(n - 1))));
            }
            result = result.add(new Constant(ArithmeticUtils.bin(n, n / 2)).div(new Constant(BigInteger.valueOf(2).pow(n))));
        } else {
            for (int i = 0; i <= (n - 1) / 2; i++) {
                result = result.add(new Constant(ArithmeticUtils.bin(n, i)).mult(new Constant(n - 2 * i).mult(argument).cos()).div(
                        new Constant(BigInteger.valueOf(2).pow(n - 1))));
            }
        }

        return result;
    }

    /* 
     Es folgen Methoden für die Integration von Funktionen vom Typ exp(ax+b) * h_1(c_1x+d_1)^n_1 * ... h_m(c_mx+d_m)^n_m
     mit h_i = sin oder = cos.
     */
    private static Expression expandPowerOfSin(Expression argument, int n) {

        // Achtung: Im Folgenden findet keine Validierung für n statt. Dies muss im Vorfeld stattfinden.
        Expression result = ZERO;

        int m;
        if (n % 2 == 0) {
            m = n / 2;
            for (int i = 0; i < m; i++) {
                result = result.add(new Constant(BigInteger.valueOf(-1).pow(m + i).multiply(ArithmeticUtils.bin(n, i))).mult(new Constant(n - 2 * i).mult(argument).cos()).div(
                        new Constant(BigInteger.valueOf(2).pow(n - 1))));
            }
            result = result.add(new Constant(ArithmeticUtils.bin(n, m)).div(new Constant(BigInteger.valueOf(2).pow(n))));
        } else {
            // n = 2 * m + 1 ist ungerade.
            m = (n - 1) / 2;
            for (int i = 0; i <= m; i++) {
                result = result.add(new Constant(BigInteger.valueOf(-1).pow(m + i).multiply(ArithmeticUtils.bin(n, i))).mult(new Constant(n - 2 * i).mult(argument).sin()).div(
                        new Constant(BigInteger.valueOf(2).pow(n - 1))));
            }
        }

        return result;

    }

    private static Expression rewriteProductOfSinSin(Expression argumentLeft, Expression argumentRight) {
        return argumentLeft.sub(argumentRight).cos().sub(argumentLeft.add(argumentRight).cos()).div(2);
    }

    private static Expression rewriteProductOfCosCos(Expression argumentLeft, Expression argumentRight) {
        return argumentLeft.sub(argumentRight).cos().add(argumentLeft.add(argumentRight).cos()).div(2);
    }

    private static Expression rewriteProductOfSinCos(Expression argumentSin, Expression argumentCos) {
        return rewriteProductOfCosSin(argumentCos, argumentSin);
    }

    private static Expression rewriteProductOfCosSin(Expression argumentCos, Expression argumentSin) {
        return argumentCos.add(argumentSin).sin().sub(argumentCos.sub(argumentSin).sin()).div(2);
    }

    @Override
    public Expression simplifyExpandProductsOfComplexExponentialFunctions(String var) throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyExpandProductsOfComplexExponentialFunctions(var));
            }
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyExpandProductsOfComplexExponentialFunctions(var));
            }

            Expression expr = SimplifyUtilities.produceProduct(factors);
            BigInteger numberOfSummands = getUpperBoundForSummands(expr, var);

            // Im Folgenden Fall nicht weiter ausmultiplizieren.
            if (numberOfSummands.compareTo(BigInteger.ZERO) < 0
                    || numberOfSummands.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_INTEGRABLE_SUMMANDS)) > 0) {
                return expr;
            }

            Expression factorLeft, factorRight;
            for (int i = 0; i < factors.getBound() - 1; i++) {

                if (factors.get(i) == null || !factors.get(i).contains(var)) {
                    continue;
                }
                factorLeft = factors.get(i);

                for (int j = i + 1; j < factors.getBound(); j++) {
                    if (factors.get(j) == null || !factors.get(j).contains(var)) {
                        continue;
                    }
                    factorRight = factors.get(j);

                    // Nun multiplikative Relationen anwenden.
                    if (factorLeft.isFunction(TypeFunction.cos)) {
                        if (factorRight.isFunction(TypeFunction.cos)) {
                            factors.put(i, rewriteProductOfCosCos(((Function) factorLeft).getLeft(), ((Function) factorRight).getLeft()));
                            factors.remove(j);
                        } else if (factorRight.isFunction(TypeFunction.sin)) {
                            factors.put(i, rewriteProductOfCosSin(((Function) factorLeft).getLeft(), ((Function) factorRight).getLeft()));
                            factors.remove(j);
                        }
                    } else if (factorLeft.isFunction(TypeFunction.sin)) {
                        if (factorRight.isFunction(TypeFunction.cos)) {
                            factors.put(i, rewriteProductOfSinCos(((Function) factorLeft).getLeft(), ((Function) factorRight).getLeft()));
                            factors.remove(j);
                        } else if (factorRight.isFunction(TypeFunction.sin)) {
                            factors.put(i, rewriteProductOfSinSin(((Function) factorLeft).getLeft(), ((Function) factorRight).getLeft()));
                            factors.remove(j);
                        }
                    }

                }
            }

            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isPower()) {

            Expression expr = this.left.simplifyExpandProductsOfComplexExponentialFunctions(var).pow(this.right.simplifyExpandProductsOfComplexExponentialFunctions(var));

            if (expr.isPower() && ((BinaryOperation) expr).right.isIntegerConstant()
                    && ((BinaryOperation) expr).right.isPositive()
                    && ((Constant) ((BinaryOperation) expr).right).getValue().compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0
                    && isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) expr).left, var)) {

                BigInteger numberOfSummands = getUpperBoundForSummands(this, var);
                if (numberOfSummands.compareTo(BigInteger.ZERO) > 0
                        && numberOfSummands.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_INTEGRABLE_SUMMANDS)) <= 0) {

                    Expression base = ((BinaryOperation) expr).left;
                    int exponent = ((Constant) ((BinaryOperation) expr).right).getValue().intValue();

                    /* 
                     Falls base eine echte Summe / Differenz ist, dann expand() anwenden.
                     Falls base eine Sinus oder Kosinusfunktion ist, und das Argument var
                     enthält, dann wird gemäß bestimmter Relationen entwickelt.
                     */
                    if (base.isFunction(TypeFunction.cos) && ((Function) base).getLeft().contains(var)) {
                        return expandPowerOfCos(((Function) base).getLeft(), exponent);
                    }
                    if (base.isFunction(TypeFunction.sin) && ((Function) base).getLeft().contains(var)) {
                        return expandPowerOfSin(((Function) base).getLeft(), exponent);
                    }
                    // "Langes" Ausmultiplizieren soll stattfinden (Boolscher Parameter = true).
                    return this.simplifyExpand(TypeExpansion.POWERFUL);

                }
            }

            return expr;

        }

        return new BinaryOperation(this.left.simplifyExpandProductsOfComplexExponentialFunctions(var),
                this.right.simplifyExpandProductsOfComplexExponentialFunctions(var),
                this.type);

    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {

        BinaryOperation expr;

        if (this.isSum()) {

            // In jedem Summanden einzeln algebraische Umformungen vornehmen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyAlgebraicExpressions());
            }
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            // In jedem Faktor einzeln algebraische Umformungen vornehmen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyAlgebraicExpressions());
            }

            Expression productOfAlgebraicallySimplifiedFactors = SimplifyUtilities.produceProduct(factors);

            if (!(productOfAlgebraicallySimplifiedFactors instanceof BinaryOperation)) {
                /*
                 Dies kann z. B. passieren, wenn factors aus 1 und exp(x)
                 besteht. SimplifyMethods.produceProduct(factors) liefert dann
                 exp(x), was KEINE Instanz von BinaryOperation ist.
                 */
                return productOfAlgebraicallySimplifiedFactors;
            }

            // Im Folgenden ist expr ein Produkt aus mindestens zwei Faktoren.
            expr = (BinaryOperation) productOfAlgebraicallySimplifiedFactors;
            if (!expr.equals(this)) {
                return expr;
            }

        } else {

            expr = new BinaryOperation(this.left.simplifyAlgebraicExpressions(), this.right.simplifyAlgebraicExpressions(), this.type);
            if (!expr.equals(this)) {
                return expr;
            }

        }

        Expression exprSimplified;

        /*
         Falls möglich, gewisse Binome vereinfachen (etwa (2^(1/2)+1)^2 =
         3+2*2^(1/2)). Maximal erlaubte Potenz ist <= einer bestimmten
         Schranke.
         */
        exprSimplified = SimplifyAlgebraicExpressionUtils.expandAlgebraicExpressionsByBinomial(expr, ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_BINOMIAL);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Faktorisiert ganze Faktoren aus Radikalen mit ganzzahligem Radikanden
        exprSimplified = SimplifyAlgebraicExpressionUtils.factorizeIntegerFactorsFromIntegerRoots(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Faktorisiert rationale Faktoren aus Radikalen mit rationalem Radikanden
        exprSimplified = SimplifyAlgebraicExpressionUtils.factorizeRationalFactorsFromRationalRoots(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Sammelt mehrere Wurzeln im Produkt zu einer Wurzel zusammen.
        exprSimplified = SimplifyAlgebraicExpressionUtils.collectVariousRootsToOneCommonRootInProducts(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Sammelt mehrere Wurzeln im Quotienten zu einer Wurzel zusammen.
        exprSimplified = SimplifyAlgebraicExpressionUtils.collectVariousRootsToOneCommonRootInQuotients(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Wendet sinnvoll Rationalisierung des Nenners an.
        exprSimplified = SimplifyAlgebraicExpressionUtils.makeFactorsInDenominatorRational(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        /*
         Wendet sinnvoll die 3. binomische Formel an, etwa falls als Faktoren
         der Form x + 3*y^(5/2) und x - 3*y^(5/2) auftauchen (-> neuer Faktor
         ist x^2 - 9*y^5).
         */
        exprSimplified = SimplifyAlgebraicExpressionUtils.collectFactorsByThirdBinomialFormula(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Versucht, Ausdrücke der Form (a+b*c^(1/2))^(1/n), n >= 2, als d+e*c^(1/2) darzustellen, wenn möglich (a, b, c, d, e rational).
        exprSimplified = SimplifyAlgebraicExpressionUtils.computeRootFromDegreeTwoElementsOverRationals(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        return expr;

    }

}
