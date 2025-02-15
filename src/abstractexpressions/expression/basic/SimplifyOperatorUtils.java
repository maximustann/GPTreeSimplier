package abstractexpressions.expression.basic;

import computationbounds.ComputationBounds;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import exceptions.EvaluationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public abstract class SimplifyOperatorUtils {

    /**
     * Falls im Summenoperator eine Summe oder eine Differenz auftaucht, so wird
     * dieser in eine entsprechende Summe oder Differenz von Summenoperatoren
     * aufgeteilt.
     */
    public static Expression splitSumOfSumsOrDifferences(BinaryOperation summand, String var, Expression lowerLimit, Expression upperLimit) {

        if (summand.isDifference()) {

            Object[] paramsLeft = new Object[4];
            paramsLeft[0] = summand.getLeft();
            paramsLeft[1] = var;
            paramsLeft[2] = lowerLimit;
            paramsLeft[3] = upperLimit;
            Object[] paramsRight = new Object[4];
            paramsRight[0] = summand.getRight();
            paramsRight[1] = var;
            paramsRight[2] = lowerLimit;
            paramsRight[3] = upperLimit;
            return new Operator(TypeOperator.sum, paramsLeft).sub(new Operator(TypeOperator.sum, paramsRight));

        } else {

            ExpressionCollection summands = SimplifyUtilities.getSummands(summand);
            Object[][] params = new Object[summands.getBound()][4];
            for (int i = 0; i < summands.getBound(); i++) {
                for (int j = 0; j < 4; j++) {
                    params[i][0] = summands.get(i);
                    params[i][1] = var;
                    params[i][2] = lowerLimit;
                    params[i][3] = upperLimit;
                }
                summands.put(i, new Operator(TypeOperator.sum, params[i]));
            }

            return SimplifyUtilities.produceSum(summands);

        }

    }

    /**
     * Falls im Summenoperator konstante Faktoren (im Zähler oder Nenner)
     * auftauchen, so werden diese herausgezogen. VORAUSSETZUNG: expr ist ein
     * Produkt oder ein Quotient.
     */
    public static Expression takeConstantsOutOfSums(BinaryOperation summand, String var, Expression lowerLimit, Expression upperLimit) {

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summand);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summand);
        ExpressionCollection resultFactorsInEnumeratorOutsideOfSum = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorOutsideOfSum = new ExpressionCollection();
        ExpressionCollection resultFactorsInEnumeratorInSum = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorInSum = new ExpressionCollection();

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (!factorsEnumerator.get(i).contains(var)) {
                resultFactorsInEnumeratorOutsideOfSum.add(factorsEnumerator.get(i));
                factorsEnumerator.remove(i);
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (!factorsDenominator.get(i).contains(var)) {
                resultFactorsInDenominatorOutsideOfSum.add(factorsDenominator.get(i));
                factorsDenominator.remove(i);
            }
        }
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) != null) {
                resultFactorsInEnumeratorInSum.add(factorsEnumerator.get(i));
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) != null) {
                resultFactorsInDenominatorInSum.add(factorsDenominator.get(i));
            }
        }

        Expression resultArgumentInSum;
        if (resultFactorsInEnumeratorInSum.isEmpty() && resultFactorsInDenominatorInSum.isEmpty()) {
            resultArgumentInSum = Expression.ONE;
        } else if (!resultFactorsInEnumeratorInSum.isEmpty() && resultFactorsInDenominatorInSum.isEmpty()) {
            resultArgumentInSum = SimplifyUtilities.produceProduct(resultFactorsInEnumeratorInSum);
        } else if (resultFactorsInEnumeratorInSum.isEmpty() && !resultFactorsInDenominatorInSum.isEmpty()) {
            resultArgumentInSum = Expression.ONE.div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorInSum));
        } else {
            resultArgumentInSum = SimplifyUtilities.produceQuotient(resultFactorsInEnumeratorInSum, resultFactorsInDenominatorInSum);
        }

        Object[] params = new Object[4];
        params[0] = resultArgumentInSum;
        params[1] = var;
        params[2] = lowerLimit;
        params[3] = upperLimit;

        if (resultFactorsInEnumeratorOutsideOfSum.isEmpty() && resultFactorsInDenominatorOutsideOfSum.isEmpty()) {
            return new Operator(TypeOperator.sum, params);
        } else if (!resultFactorsInEnumeratorOutsideOfSum.isEmpty() && resultFactorsInDenominatorOutsideOfSum.isEmpty()) {
            return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfSum).mult(new Operator(TypeOperator.sum, params));
        } else if (resultFactorsInEnumeratorOutsideOfSum.isEmpty() && !resultFactorsInDenominatorOutsideOfSum.isEmpty()) {
            return new Operator(TypeOperator.sum, params).div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfSum));
        }
        return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfSum).mult(new Operator(TypeOperator.sum, params)).div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfSum));

    }

    /**
     * Falls im Summenoperator eine Summe oder eine Differenz auftaucht, so wird
     * dieser in eine entsprechende Summe oder Differenz von Summenoperatoren
     * aufgeteilt. VORAUSSETZUNG: expr ist ein Produkt oder ein Quotient.
     */
    public static Expression splitProductsOfProductsOrQuotients(BinaryOperation factor, String var, Expression lowerLimit, Expression upperLimit) {

        if (factor.isQuotient()) {

            Object[] paramsLeft = new Object[4];
            paramsLeft[0] = factor.getLeft();
            paramsLeft[1] = var;
            paramsLeft[2] = lowerLimit;
            paramsLeft[3] = upperLimit;
            Object[] paramsRight = new Object[4];
            paramsRight[0] = factor.getRight();
            paramsRight[1] = var;
            paramsRight[2] = lowerLimit;
            paramsRight[3] = upperLimit;
            return new Operator(TypeOperator.prod, paramsLeft).div(new Operator(TypeOperator.prod, paramsRight));

        } else {

            ExpressionCollection factors = SimplifyUtilities.getFactors(factor);
            Object[][] params = new Object[factors.getBound()][4];
            for (int i = 0; i < factors.getBound(); i++) {
                for (int j = 0; j < 4; j++) {
                    params[i][0] = factors.get(i);
                    params[i][1] = var;
                    params[i][2] = lowerLimit;
                    params[i][3] = upperLimit;
                }
                factors.put(i, new Operator(TypeOperator.prod, params[i]));
            }
            return SimplifyUtilities.produceProduct(factors);

        }

    }

    /**
     * Falls im Produktoperator konstante Exponenten auftauchen, so werden diese
     * herausgezogen. VORAUSSETZUNG: Exponent ist ein Produkt oder ein Quotient
     */
    public static Expression takeConstantExponentsOutOfProducts(BinaryOperation factor, String var, Expression lowerLimit, Expression upperLimit) {

        if (factor.isNotPower()) {
            // Dann nichts tun!
            Object[] params = new Object[4];
            params[0] = factor;
            params[1] = var;
            params[2] = lowerLimit;
            params[3] = upperLimit;
            return new Operator(TypeOperator.prod, params);
        }

        ExpressionCollection exponentFactorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(factor.getRight());
        ExpressionCollection exponentFactorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(factor.getRight());
        Expression exponentEnumeratorOutsideOfProduct = Expression.ONE;
        Expression exponentDenominatorOutsideOfProduct = Expression.ONE;
        ExpressionCollection resultFactorsInExponentEnumerator = new ExpressionCollection();
        ExpressionCollection resultFactorsInExponentDenominator = new ExpressionCollection();

        if (exponentFactorsEnumerator.get(0) != null && !exponentFactorsEnumerator.get(0).contains(var) && exponentFactorsEnumerator.get(0).isIntegerConstant()) {
            exponentEnumeratorOutsideOfProduct = exponentFactorsEnumerator.get(0);
            exponentFactorsEnumerator.remove(0);
        }

        if (exponentFactorsDenominator.get(0) != null && !exponentFactorsDenominator.get(0).contains(var) && exponentFactorsDenominator.get(0).isOddIntegerConstant()) {
            exponentDenominatorOutsideOfProduct = exponentFactorsDenominator.get(0);
            exponentFactorsDenominator.remove(0);
        }

        for (int i = 0; i < exponentFactorsEnumerator.getBound(); i++) {
            if (exponentFactorsEnumerator.get(i) != null) {
                resultFactorsInExponentEnumerator.add(exponentFactorsEnumerator.get(i));
            }
        }
        for (int i = 0; i < exponentFactorsDenominator.getBound(); i++) {
            if (exponentFactorsDenominator.get(i) != null) {
                resultFactorsInExponentDenominator.add(exponentFactorsDenominator.get(i));
            }
        }

        Expression resultExponentInProduct;
        if (resultFactorsInExponentEnumerator.isEmpty() && resultFactorsInExponentDenominator.isEmpty()) {
            resultExponentInProduct = Expression.ONE;
        } else if (!resultFactorsInExponentEnumerator.isEmpty() && resultFactorsInExponentDenominator.isEmpty()) {
            resultExponentInProduct = SimplifyUtilities.produceProduct(resultFactorsInExponentEnumerator);
        } else if (resultFactorsInExponentEnumerator.isEmpty() && !resultFactorsInExponentDenominator.isEmpty()) {
            resultExponentInProduct = Expression.ONE.div(SimplifyUtilities.produceProduct(resultFactorsInExponentDenominator));
        } else {
            resultExponentInProduct = SimplifyUtilities.produceQuotient(resultFactorsInExponentEnumerator, resultFactorsInExponentDenominator);
        }

        Object[] params = new Object[4];
        params[0] = factor.getLeft().pow(resultExponentInProduct);
        params[1] = var;
        params[2] = lowerLimit;
        params[3] = upperLimit;

        if (exponentEnumeratorOutsideOfProduct.equals(Expression.ONE) && exponentDenominatorOutsideOfProduct.equals(Expression.ONE)) {
            return new Operator(TypeOperator.prod, params);
        } else if (!exponentEnumeratorOutsideOfProduct.equals(Expression.ONE) && exponentDenominatorOutsideOfProduct.equals(Expression.ONE)) {
            return new Operator(TypeOperator.prod, params).pow(exponentEnumeratorOutsideOfProduct);
        } else if (exponentEnumeratorOutsideOfProduct.equals(Expression.ONE) && !exponentDenominatorOutsideOfProduct.equals(Expression.ONE)) {
            return new Operator(TypeOperator.prod, params).pow(Expression.ONE.div(exponentDenominatorOutsideOfProduct));
        }
        return new Operator(TypeOperator.prod, params).pow(exponentEnumeratorOutsideOfProduct.div(exponentDenominatorOutsideOfProduct));

    }

    /**
     * Vereinfacht folgendes: prod(a^f(k),k,m,n) = a^sum(f(k),k,m,n)
     */
    public static Expression simplifyProductWithConstantBase(BinaryOperation factor, String var, Expression lowerLimit, Expression upperLimit) {

        if (factor.isNotPower() || factor.getLeft().contains(var)) {
            // Dann nichts tun!
            Object[] params = new Object[4];
            params[0] = factor;
            params[1] = var;
            params[2] = lowerLimit;
            params[3] = upperLimit;
            return new Operator(TypeOperator.prod, params);
        }

        Object[] params = new Object[4];
        params[0] = factor.getRight();
        params[1] = var;
        params[2] = lowerLimit;
        params[3] = upperLimit;
        return factor.getLeft().pow(new Operator(TypeOperator.sum, params));

    }

    /**
     * Vereinfacht folgendes: sum(k^n,k,p,q) = Polynom in k für kleines n.
     */
    public static Expression simplifySumOfPowersOfIntegers(Expression summand, String var, Expression lowerLimit, Expression upperLimit) {

        if (!summand.equals(Variable.create(var))
                && !(summand.isPower() && ((BinaryOperation) summand).getLeft().equals(Variable.create(var))
                && ((BinaryOperation) summand).getRight().isIntegerConstant() && ((BinaryOperation) summand).getRight().isPositive())) {
            // Dann nichts tun!
            Object[] params = new Object[4];
            params[0] = summand;
            params[1] = var;
            params[2] = lowerLimit;
            params[3] = upperLimit;
            return new Operator(TypeOperator.sum, params);
        }

        if (summand.isPower() && ((Constant) ((BinaryOperation) summand).getRight()).getBigIntValue().compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_DEGREE_OF_POLYNOMIAL_INSIDE_SUM)) > 0) {
            // Dann ist der Exponent zu groß.
            Object[] params = new Object[4];
            params[0] = summand;
            params[1] = var;
            params[2] = lowerLimit;
            params[3] = upperLimit;
            return new Operator(TypeOperator.sum, params);
        }

        int n;
        if (summand.equals(Variable.create(var))) {
            n = 1;
        } else {
            n = ((Constant) ((BinaryOperation) summand).getRight()).getBigIntValue().intValue();
        }

        ExpressionCollection coefficients = getPolynomialCoefficientsForSumsOfPowersOfIntegers(n);
        Expression abstractPolynomial = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficients, var);
        return abstractPolynomial.replaceVariable(var, upperLimit).sub(abstractPolynomial.replaceVariable(var, lowerLimit.sub(ONE)));

    }

    private static ExpressionCollection getPolynomialCoefficientsForSumsOfPowersOfIntegers(int n) {

        if (n <= 0 || n > ComputationBounds.BOUND_OPERATOR_MAX_DEGREE_OF_POLYNOMIAL_INSIDE_SUM) {
            return new ExpressionCollection();
        }

        ExpressionCollection coefficients = new ExpressionCollection();

        switch (n) {
            case 1:
                coefficients.add(ZERO);
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(TWO));
                break;
            case 2:
                coefficients.add(ZERO);
                coefficients.add(ONE.div(6));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(THREE));
                break;
            case 3:
                coefficients.add(ZERO);
                coefficients.add(ZERO);
                coefficients.add(ONE.div(4));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(4));
                break;
            case 4:
                coefficients.add(ZERO);
                coefficients.add(MINUS_ONE.div(30));
                coefficients.add(ZERO);
                coefficients.add(ONE.div(THREE));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(5));
                break;
            case 5:
                coefficients.add(ZERO);
                coefficients.add(ZERO);
                coefficients.add(MINUS_ONE.div(12));
                coefficients.add(ZERO);
                coefficients.add(new Constant(5).div(12));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(6));
                break;
            case 6:
                coefficients.add(ZERO);
                coefficients.add(ONE.div(42));
                coefficients.add(ZERO);
                coefficients.add(MINUS_ONE.div(6));
                coefficients.add(ZERO);
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(7));
                break;
            case 7:
                coefficients.add(ZERO);
                coefficients.add(ONE.div(12));
                coefficients.add(ZERO);
                coefficients.add(ZERO);
                coefficients.add(new Constant(-7).div(24));
                coefficients.add(ZERO);
                coefficients.add(new Constant(7).div(12));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(8));
                break;
            case 8:
                coefficients.add(ZERO);
                coefficients.add(MINUS_ONE.div(30));
                coefficients.add(ZERO);
                coefficients.add(TWO.div(9));
                coefficients.add(ZERO);
                coefficients.add(new Constant(-7).div(15));
                coefficients.add(ZERO);
                coefficients.add(TWO.div(THREE));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(9));
                break;
            case 9:
                coefficients.add(ZERO);
                coefficients.add(ZERO);
                coefficients.add(new Constant(-3).div(20));
                coefficients.add(ZERO);
                coefficients.add(ONE.div(TWO));
                coefficients.add(ZERO);
                coefficients.add(new Constant(-7).div(10));
                coefficients.add(ZERO);
                coefficients.add(THREE.div(4));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(10));
                break;
            case 10:
                coefficients.add(ZERO);
                coefficients.add(new Constant(5).div(66));
                coefficients.add(ZERO);
                coefficients.add(MINUS_ONE.div(TWO));
                coefficients.add(ZERO);
                coefficients.add(ONE);
                coefficients.add(ZERO);
                coefficients.add(MINUS_ONE);
                coefficients.add(ZERO);
                coefficients.add(new Constant(5).div(6));
                coefficients.add(ONE.div(TWO));
                coefficients.add(ONE.div(11));
                break;
            default:
                coefficients = getCoefficientsForPolynomialSumExpression(n);
        }

        return coefficients;

    }

    private static ExpressionCollection getCoefficientsForPolynomialSumExpression(int n) {

        if (n <= 1 || n > ComputationBounds.BOUND_OPERATOR_MAX_DEGREE_OF_POLYNOMIAL_INSIDE_SUM) {
            return new ExpressionCollection();
        }

        ExpressionCollection coefficients = new ExpressionCollection();

        try {
            coefficients.put(n + 1, ONE.div(n + 1));
            coefficients.put(n, ONE.div(TWO));
            coefficients.put(0, ZERO);

            Expression currentCoefficient;
            for (int i = n - 1; i > 0; i--) {
                currentCoefficient = ZERO;
                for (int j = 0; j < n + 1 - i; j++) {
                    if (j % 2 == 0) {
                        currentCoefficient = currentCoefficient.add(getBinomialCoefficient(i + 1 + j, j + 2).mult(coefficients.get(i + 1 + j)));
                    } else {
                        currentCoefficient = currentCoefficient.sub(getBinomialCoefficient(i + 1 + j, j + 2).mult(coefficients.get(i + 1 + j)));
                    }
                }
                currentCoefficient = currentCoefficient.div(i).simplify();
                coefficients.put(i, currentCoefficient);
            }
            return coefficients;
        } catch (EvaluationException e) {
            // Sollte eigentlich NIE vorkommen.
            return new ExpressionCollection();
        }

    }

    private static Expression getBinomialCoefficient(int n, int k) {
        if (n < 0 || k < 0 || k > n) {
            return ZERO;
        }
        BigInteger binCoeff = BigInteger.ONE;
        for (int i = 1; i <= k; i++) {
            binCoeff = binCoeff.multiply(BigInteger.valueOf(n - i + 1)).divide(BigInteger.valueOf(i));
        }
        return new Constant(binCoeff);
    }

    /**
     * Vereinfacht: prod(exp(f(k)), k, m, n) = exp(sum(f(k), k, m, n)).
     */
    public static Expression simplifyProductOfExponentialFunctions(Expression factor, String var, Expression lowerLimit, Expression upperLimit) {
        if (!factor.isFunction(TypeFunction.exp)) {
            // Dann nichts tun!
            Object[] params = new Object[4];
            params[0] = factor;
            params[1] = var;
            params[2] = lowerLimit;
            params[3] = upperLimit;
            return new Operator(TypeOperator.prod, params);
        }
        Object[] params = new Object[4];
        params[0] = ((Function) factor).getLeft();
        params[1] = var;
        params[2] = lowerLimit;
        params[3] = upperLimit;
        return new Operator(TypeOperator.sum, params).exp();
    }

    /**
     * Vereinfacht: sum(log(f(k)), k, m, n) = log(prod(f(k), k, m, n)), mit log
     * = lg, ln.
     */
    public static Expression simplifySumOfLogarithmicFunctions(Expression summand, String var, Expression lowerLimit, Expression upperLimit, TypeFunction logType) {
        if (!summand.isFunction(logType)) {
            // Dann nichts tun!
            Object[] params = new Object[4];
            params[0] = summand;
            params[1] = var;
            params[2] = lowerLimit;
            params[3] = upperLimit;
            return new Operator(TypeOperator.sum, params);
        }
        Object[] params = new Object[4];
        params[0] = ((Function) summand).getLeft();
        params[1] = var;
        params[2] = lowerLimit;
        params[3] = upperLimit;
        return new Function(new Operator(TypeOperator.prod, params), logType);
    }

}
