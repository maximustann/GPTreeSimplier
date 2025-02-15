package abstractexpressions.expression.integration;

import abstractexpressions.expression.computation.ArithmeticUtils;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.substitution.SubstitutionUtilities;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyBinaryOperationUtils;
import abstractexpressions.expression.equation.SolveGeneralEquationUtils;
import abstractexpressions.expression.basic.SimplifyPolynomialUtils;
import abstractexpressions.expression.basic.SimplifyPolynomialUtils.PolynomialNotDecomposableException;
import abstractexpressions.expression.basic.SimplifyRationalFunctionUtils;
import abstractexpressions.expression.basic.SimplifyUtilities;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import enums.TypeFractionSimplification;
import exceptions.NotAlgebraicallyIntegrableException;
import exceptions.NotAlgebraicallySolvableException;
import java.math.BigInteger;
import java.util.HashSet;
import notations.NotationLoader;

public abstract class RischAlgorithmUtils extends GeneralIntegralUtils {

    private static final HashSet<TypeSimplify> simplifyTypesForDifferentialFieldExtension = new HashSet<>();
    private static final HashSet<TypeSimplify> simplifyTypesRischAlgorithm = new HashSet<>();
    private static final HashSet<TypeSimplify> simplifyTypesRischDifferentialEquation = new HashSet<>();

    static {
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.order_difference_and_division);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.order_sums_and_products);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_basic);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_collect_products);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_expand_logarithms);
        simplifyTypesForDifferentialFieldExtension.add(TypeSimplify.simplify_replace_exponential_functions_with_respect_to_variable_by_definitions);

        simplifyTypesRischAlgorithm.add(TypeSimplify.order_difference_and_division);
        simplifyTypesRischAlgorithm.add(TypeSimplify.order_sums_and_products);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_basic);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_collect_products);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_factorize);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypesRischAlgorithm.add(TypeSimplify.simplify_expand_logarithms);

        simplifyTypesRischDifferentialEquation.add(TypeSimplify.order_difference_and_division);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.order_sums_and_products);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_basic);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_collect_products);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_factorize);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_expand_logarithms);
        simplifyTypesRischDifferentialEquation.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
    }

    /**
     * Gibt zurück, ob transzendente Erweiterungen eine Standardform besitzen.
     * Für exponentielle Erweiterungen t = exp(f(x)) muss gelten, dass f(x)
     * keine konstanten nichttrivialen Summanden besitzt, für logarithmische
     * Erweiterungen t = ln(f(x)) muss gelten, dass f(x) keine konstanten
     * nichttrivialen Faktoren besitzt.
     */
    private static boolean areFieldExtensionsInCorrectForm(ExpressionCollection fieldGenerators, String var) {
        for (Expression fieldExtension : fieldGenerators) {
            if (fieldExtension.isFunction(TypeFunction.exp)) {
                ExpressionCollection constantSummandsLeft = SimplifyUtilities.getConstantSummandsLeftInExpression(fieldExtension, var);
                ExpressionCollection constantSummandsRight = SimplifyUtilities.getConstantSummandsRightInExpression(fieldExtension, var);
                return constantSummandsLeft.isEmpty() && constantSummandsRight.isEmpty();
            }
            if (fieldExtension.isFunction(TypeFunction.ln)) {
                ExpressionCollection constantFactorsNumerator = SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(fieldExtension, var);
                ExpressionCollection constantFactorsDenominator = SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(fieldExtension, var);
                return constantFactorsNumerator.isEmpty() && constantFactorsDenominator.isEmpty();
            }
            if (!fieldExtension.isFunction(TypeFunction.exp) && !fieldExtension.isFunction(TypeFunction.ln)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sei x = var. Diese Methode gibt zurück, ob f algebraisch über dem Körper
     * R(x, t_1, ..., t_n) ist, wobei t_1, ..., t_n die Elemente von
     * fieldGenerators sind.<br>
     * VORAUSSETZUNGEN: (1) f enthält keine algebraischen Ausdrücke (also keine
     * Ausdrücke der Form (...)^(p/q) mit rationalem und nicht-ganzem p/q)<br>
     * (2) f ist so weit, wie es geht vereinfacht (d.h. f enthält nicht
     * Ausdrücke wie exp(ln(...)) o. ä.).<br>
     * (3) fieldGenerators darf nur Ausdrücke der Form exp(...) oder ln(...)
     * enthalten, also keine Summen, Differenzen etc. Die Methode
     * areFieldExtensionsInCorrectForm(fieldGenerators) muss also true
     * zurückgeben.<br>
     * BEISPIEL: (1) f = exp(x+2), var = "x", fieldGenerators = {exp(x)}. Hier
     * wird true zurückgegeben.<br>
     * (2) f = ln(exp(x+2)+x^2)+x^3/7, var = "x", fieldGenerators = {exp(x)}.
     * Hier wird false zurückgegeben.<br>
     * (3) f = ln(x)+x!, var = "x", fieldGenerators = {ln(x), exp(x)}. Hier wird
     * false zurückgegeben (aufgrund des Summanden x!, welcher transzendent über
     * der angegebenen Körpererweiterung ist).<br>
     */
    public static boolean isFunctionRationalOverDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) {

        if (!areFieldExtensionsInCorrectForm(fieldGenerators, var)) {
            // Schlechter Fall, da fieldExtensions nicht in die korrekte Form besitzt.
            return false;
        }
        // Weitestgehend vereinfachen, wenn möglich.
        try {
            f = f.simplify(simplifyTypesForDifferentialFieldExtension, var);
        } catch (EvaluationException e) {
            return false;
        }
        return isRationalOverDifferentialField(f, var, fieldGenerators);

    }

    /**
     * Sei x = var. Diese Hilfsmethode gibt zurück, ob f algebraisch über dem
     * Körper R(x, t_1, ..., t_n) ist, wobei t_1, ..., t_n die Elemente von
     * fieldGenerators sind.<br>
     * VORAUSSETZUNGEN: (1) f enthält keine algebraischen Ausdrücke (also keine
     * Ausdrücke der Form (...)^(p/q) mit rationalem und nicht-ganzem p/q)<br>
     * (2) f ist so weit, wie es geht vereinfacht (d.h. f enthält nicht
     * Ausdrücke wie exp(ln(...)) o. ä.).<br>
     * (3) fieldGenerators darf nur Ausdrücke der Form exp(...) oder ln(...)
     * enthalten, also keine Summen, Differenzen etc. Die Methode
     * areFieldExtensionsInCorrectForm(fieldGenerators) muss also true
     * zurückgeben.<br>
     * BEISPIEL: (1) f = exp(x+2), var = "x", fieldGenerators = {exp(x)}. Hier
     * wird true zurückgegeben.<br>
     * (2) f = ln(exp(x+2)+x^2)+x^3/7, var = "x", fieldGenerators = {exp(x)}.
     * Hier wird false zurückgegeben.<br>
     * (3) f = ln(x)+x!, var = "x", fieldGenerators = {ln(x), exp(x)}. Hier wird
     * false zurückgegeben (aufgrund des Summanden x!, welcher transzendent über
     * der angegebenen Körpererweiterung ist).<br>
     */
    private static boolean isRationalOverDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) {

        if (fieldGenerators.containsExquivalent(f)) {
            return true;
        }

        if (!f.contains(var) || f.equals(Variable.create(var))) {
            return true;
        }
        if (f instanceof BinaryOperation) {
            if (f.isNotPower()) {
                return isRationalOverDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators)
                        && isRationalOverDifferentialField(((BinaryOperation) f).getRight(), var, fieldGenerators);
            }
            if (f.isIntegerPower()) {
                return isRationalOverDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators);
            }
        }
        if (f.isFunction()) {

            if (fieldGenerators.containsExquivalent(f)) {
                return true;
            }

            if (f.isFunction(TypeFunction.exp)) {
                ExpressionCollection nonConstantSummandsLeft = SimplifyUtilities.getNonConstantSummandsLeftInExpression(((Function) f).getLeft(), var);
                ExpressionCollection nonConstantSummandsRight = SimplifyUtilities.getNonConstantSummandsRightInExpression(((Function) f).getLeft(), var);
                Expression nonConstantSummand = SimplifyUtilities.produceDifference(nonConstantSummandsLeft, nonConstantSummandsRight);
                Expression currentQuotient;
                for (Expression fieldGenerator : fieldGenerators) {
                    if (!fieldGenerator.isFunction(TypeFunction.exp)) {
                        continue;
                    }
                    try {
                        currentQuotient = nonConstantSummand.div(((Function) fieldGenerator).getLeft()).simplify(simplifyTypesForDifferentialFieldExtension);
                        if (currentQuotient.isIntegerConstant()) {
                            return true;
                        }
                    } catch (EvaluationException e) {
                    }
                }
                return false;
            }

            if (f.isFunction(TypeFunction.ln)) {
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(((Function) f).getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(((Function) f).getLeft());
                ExpressionCollection summandsLeftForCompare, summandsRightForCompare;
                Expression currentQuotient;
                boolean unclearCaseFound = false;
                for (Expression fieldGenerator : fieldGenerators) {
                    if (!fieldGenerator.isFunction(TypeFunction.ln)) {
                        continue;
                    }
                    summandsLeftForCompare = SimplifyUtilities.getSummandsLeftInExpression(((Function) fieldGenerator).getLeft());
                    summandsRightForCompare = SimplifyUtilities.getSummandsRightInExpression(((Function) fieldGenerator).getLeft());
                    if ((summandsLeft.getBound() + summandsRight.getBound()) * (summandsLeftForCompare.getBound() + summandsRightForCompare.getBound()) > 1) {
                        unclearCaseFound = true;
                    }
                    try {
                        currentQuotient = ((Function) f).getLeft().div(((Function) fieldGenerator).getLeft()).simplify();
                        if (currentQuotient.isIntegerConstantOrRationalConstant()) {
                            return true;
                        }
                    } catch (EvaluationException e) {
                        return false;
                    }

                }
                if (unclearCaseFound) {
                    return false;
                }
            }

            return false;

        }

        return false;

    }

    public static ExpressionCollection getOrderedTranscendentalGeneratorsForDifferentialField(Expression f, String var) {
        ExpressionCollection fieldGenerators = new ExpressionCollection();
        try {
            f = f.simplify(simplifyTypesForDifferentialFieldExtension, var);
        } catch (EvaluationException e) {
            return fieldGenerators;
        }
        boolean newGeneratorFound;
        do {
            newGeneratorFound = addTranscendentalGeneratorForDifferentialField(f, var, fieldGenerators);
        } while (newGeneratorFound);
        return fieldGenerators;
    }

    public static ExpressionCollection getOrderedTranscendentalGeneratorsForDifferentialField(Expression[] functions, String var) {
        ExpressionCollection fieldGenerators = new ExpressionCollection();
        try {
            for (int i = 0; i < functions.length; i++) {
                functions[i] = functions[i].simplify(simplifyTypesForDifferentialFieldExtension, var);
            }
        } catch (EvaluationException e) {
            return fieldGenerators;
        }
        boolean newGeneratorFound;
        do {
            newGeneratorFound = false;
            for (int i = 0; i < functions.length; i++) {
                newGeneratorFound = newGeneratorFound || addTranscendentalGeneratorForDifferentialField(functions[i], var, fieldGenerators);
            }
        } while (newGeneratorFound);
        return fieldGenerators;
    }

    private static boolean addTranscendentalGeneratorForDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) {

        if (isFunctionRationalOverDifferentialField(f, var, fieldGenerators)) {
            return false;
        }
        if (f instanceof BinaryOperation) {
            if (f.isNotPower()) {
                return addTranscendentalGeneratorForDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators)
                        || addTranscendentalGeneratorForDifferentialField(((BinaryOperation) f).getRight(), var, fieldGenerators);
            }
            if (f.isIntegerPower()) {
                return addTranscendentalGeneratorForDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators);
            }
        }
        if (f.isFunction()) {

            if (!isFunctionRationalOverDifferentialField(((Function) f).getLeft(), var, fieldGenerators)) {
                // Dann zuerst Erzeuger hinzufügen, die im Funktionsargument enthalten sind.
                return addTranscendentalGeneratorForDifferentialField(((Function) f).getLeft(), var, fieldGenerators);
            }

            if (f.isFunction(TypeFunction.exp)) {
                ExpressionCollection nonConstantSummandsLeft = SimplifyUtilities.getNonConstantSummandsLeftInExpression(((Function) f).getLeft(), var);
                ExpressionCollection nonConstantSummandsRight = SimplifyUtilities.getNonConstantSummandsRightInExpression(((Function) f).getLeft(), var);
                Expression nonConstantSummand = SimplifyUtilities.produceDifference(nonConstantSummandsLeft, nonConstantSummandsRight);
                Expression currentQuotient;
                for (int i = 0; i < fieldGenerators.getBound(); i++) {
                    if (fieldGenerators.get(i) == null || !fieldGenerators.get(i).isFunction(TypeFunction.exp)) {
                        continue;
                    }
                    try {
                        currentQuotient = nonConstantSummand.div(((Function) fieldGenerators.get(i)).getLeft()).simplify();
                        if (currentQuotient.isRationalConstant()) {
                            // Wenn das Verhältnis ganz ist, braucht man nichts aufzunehmen.
                            /* Wenn currentQuotient = p/q ist und fieldGenerators.get(i) = exp(f(x)),
                             so wird fieldGenerators.get(i) zu exp(f(x)/q).
                             */
                            BigInteger a = BigInteger.ONE;
                            BigInteger b = BigInteger.ONE;
                            ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(nonConstantSummand);
                            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(nonConstantSummand);

                            if (factorsNumerator.get(0).isIntegerConstant()) {
                                a = ((Constant) factorsNumerator.get(0)).getBigIntValue().abs();
                                factorsNumerator.remove(0);
                            }
                            if (!factorsDenominator.isEmpty() && factorsDenominator.get(0).isIntegerConstant()) {
                                b = ((Constant) factorsDenominator.get(0)).getBigIntValue().abs();
                                factorsDenominator.remove(0);
                            }

                            Expression quotient = new Constant(a).div(b).div(currentQuotient).simplify();

                            BigInteger c, d;
                            if (quotient.isIntegerConstant()) {
                                c = ((Constant) quotient).getBigIntValue();
                                d = BigInteger.ONE;
                            } else {
                                c = ((Constant) ((BinaryOperation) quotient).getLeft()).getBigIntValue();
                                d = ((Constant) ((BinaryOperation) quotient).getRight()).getBigIntValue();
                            }

                            a = a.gcd(c);
                            b = ArithmeticUtils.lcm(b, d);
                            factorsNumerator.add(new Constant(a));
                            factorsDenominator.add(new Constant(b));
                            Expression expArgument = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);

                            fieldGenerators.put(i, expArgument.exp().simplify());
                            return true;
                        }
                    } catch (EvaluationException e) {
                    }
                }
                fieldGenerators.add(f);
                return true;
            }

            if (f.isFunction(TypeFunction.ln)) {
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(((Function) f).getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(((Function) f).getLeft());
                ExpressionCollection summandsLeftForCompare, summandsRightForCompare;
                Expression currentQuotient;
                boolean unclearCaseFound = false;
                for (Expression fieldGenerator : fieldGenerators) {
                    if (!fieldGenerator.isFunction(TypeFunction.ln)) {
                        continue;
                    }
                    try {
                        currentQuotient = ((Function) f).getLeft().div(((Function) fieldGenerator).getLeft()).simplify();
                        if (currentQuotient.isIntegerConstantOrRationalConstant()) {
                            return false;
                        }
                    } catch (EvaluationException e) {
                    }
                    summandsLeftForCompare = SimplifyUtilities.getSummandsLeftInExpression(((Function) fieldGenerator).getLeft());
                    summandsRightForCompare = SimplifyUtilities.getSummandsRightInExpression(((Function) fieldGenerator).getLeft());
                    if ((summandsLeft.getBound() + summandsRight.getBound()) * (summandsLeftForCompare.getBound() + summandsRightForCompare.getBound()) > 1) {
                        unclearCaseFound = true;
                    }
                }
                if (unclearCaseFound) {
                    return false;
                }
                fieldGenerators.add(f);
                return true;
            }

            return false;

        }

        return false;

    }

    ////////////////////////////////////////////////// Der Risch-Algorithmus ///////////////////////////////////////////
    /**
     * Hauptmethode für das Integrieren gemäß dem Risch-Algorithmus im Falle
     * einer Erweiterung durch transzendente Elemente.
     *
     * @throws NotAlgebraicallyIntegrableException
     * @throws EvaluationException
     */
    public static Expression integrateByRischAlgorithmForTranscendentalExtension(Operator expr) throws NotAlgebraicallyIntegrableException, EvaluationException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        // Integranden auf bestimmte Art und Weise vereinfachen.
        f = f.simplify(simplifyTypesRischAlgorithm);

        ExpressionCollection transcendentalExtensions = getOrderedTranscendentalGeneratorsForDifferentialField(f, var);

        // Nur echte transzende Erweiterungen betrachten. Diese müssen die Funktion auch erzeugen können.
        if (transcendentalExtensions.isEmpty() || !isFunctionRationalOverDifferentialField(f, var, transcendentalExtensions)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Letztes transzendes Element wählen und damit den Risch-Algorithmus starten.
        Expression transcententalElement = transcendentalExtensions.getLast();
        String transcendentalVar = SubstitutionUtilities.getSubstitutionVariable(f);
        Expression fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(f, transcententalElement, Variable.create(transcendentalVar)).simplify(simplifyTypesRischAlgorithm);

        // Sei t = transcendentalVar. Dann muss fSubstituted eine rationale Funktion in t sein.
        if (!SimplifyRationalFunctionUtils.isRationalFunction(fSubstituted, transcendentalVar)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        if (!(fSubstituted instanceof BinaryOperation)) {
            // Dann sind andere Integrationsmethoden dafür zuständig.
            throw new NotAlgebraicallyIntegrableException();
        }

        // Zunächst alles auf einen Bruch bringen.
        fSubstituted = SimplifyBinaryOperationUtils.bringExpressionToCommonDenominator((BinaryOperation) fSubstituted);

        if (!fSubstituted.isQuotient()) {
            // Dann kann es sich nur um ein Polynom in t = transcendentalElement handeln (sogar OHNE Laurentanteile!).
            if (SimplifyPolynomialUtils.isPolynomialAdmissibleForComputation(fSubstituted, transcendentalVar)) {
                ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(fSubstituted, transcendentalVar);
                return integrateByRischAlgorithmPolynomialPart(coefficients, new ExpressionCollection(), transcententalElement, var, transcendentalVar);
            }
            throw new NotAlgebraicallyIntegrableException();
        }

        // Ab hier ist der Integrand ein Bruch.
        ExpressionCollection coefficientsNumerator, coefficientsDenominator;
        try {
            coefficientsNumerator = SimplifyPolynomialUtils.getPolynomialCoefficients(((BinaryOperation) fSubstituted).getLeft(), transcendentalVar);
            coefficientsDenominator = SimplifyPolynomialUtils.getPolynomialCoefficients(((BinaryOperation) fSubstituted).getRight(), transcendentalVar);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }
        ExpressionCollection[] quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsNumerator, coefficientsDenominator);

        // Im Fall einer Exponentialerweiterung: t im Nenner faktorisieren und in den polynomiallen Teil übertragen.
        if (transcententalElement.isFunction(TypeFunction.exp)) {

            int ordOfTranscendentalElementInDenominator = 0;
            for (int i = 0; i < coefficientsDenominator.getBound(); i++) {
                if (!coefficientsDenominator.get(i).equals(ZERO)) {
                    break;
                } else {
                    ordOfTranscendentalElementInDenominator++;
                }
            }
            ExpressionCollection coefficientsOfDenominatorOfNonSpecialPart = new ExpressionCollection();
            for (int i = ordOfTranscendentalElementInDenominator; i < coefficientsDenominator.getBound(); i++) {
                coefficientsOfDenominatorOfNonSpecialPart.put(i - ordOfTranscendentalElementInDenominator, coefficientsDenominator.get(i));
            }

            if (ordOfTranscendentalElementInDenominator > 0) {

                ExpressionCollection laurentCoefficients = getPartialFractionDecompositionForIntegrandInCaseOfExponentialExtension(quotient[1], coefficientsDenominator, transcendentalVar);
                Expression numeratorOfNonSpecialPart = laurentCoefficients.get(0);
                laurentCoefficients.put(0, ZERO);
                ExpressionCollection coefficientsOfNumeratorOfNonSpecialPart;
                try {
                    coefficientsOfNumeratorOfNonSpecialPart = SimplifyPolynomialUtils.getPolynomialCoefficients(numeratorOfNonSpecialPart, transcendentalVar);
                } catch (EvaluationException e) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                Expression integralOfPolynomialPart = integrateByRischAlgorithmPolynomialPart(quotient[0], laurentCoefficients, transcententalElement, var, transcendentalVar);
                Expression integralOfFractionalPart = integrateByRischAlgorithmFractionalPart(coefficientsOfNumeratorOfNonSpecialPart,
                        coefficientsOfDenominatorOfNonSpecialPart, transcententalElement, var, transcendentalVar);
                return integralOfPolynomialPart.add(integralOfFractionalPart);

            }

        }

        /* 
         Im Fall einer Logarithmuserweiterung (oder Exponentialerweiterung ohne speziellen Teil): 
         Polynomialen und gebrochenen Teil separat integrieren (Nach Risch-Algorithmus erlaubt).
         */
        Expression integralOfPolynomialPart = integrateByRischAlgorithmPolynomialPart(quotient[0], new ExpressionCollection(), transcententalElement, var, transcendentalVar);
        Expression integralOfFractionalPart = integrateByRischAlgorithmFractionalPart(quotient[1], coefficientsDenominator, transcententalElement, var, transcendentalVar);
        return integralOfPolynomialPart.add(integralOfFractionalPart);

    }

    /**
     * Wenn der Integrand die Form f = g(x,t)/(t<sup>n</sup>*(a<sub>0</sub> +
     * ... + a<sub>k</sub>t<sup>k</sup>)), t = exponentielles Element, besitzt,
     * so gibt diese Methode eine Darstellung der Form f = b<sub>1</sub>/t + ...
     * + b<sub>n</sub>/t<sup>n</sup> + h(x,t)/(a<sub>0</sub> + ... +
     * a<sub>k</sub>t<sup>k</sup>) zurück. Die Grade von g und h sind dabei
     * stets kleiner als die Grade der entsprechenden Nenner. Es wird eine
     * ExpressionCollection der Form [h, b<sub>1</sub>, ..., b<sub>k</sub>]
     * zurückgegeben.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static ExpressionCollection getPartialFractionDecompositionForIntegrandInCaseOfExponentialExtension(ExpressionCollection coefficientsNumerator, ExpressionCollection coefficientsDenominator,
            String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        int ord = 0;
        for (int i = 0; i < coefficientsDenominator.getBound(); i++) {
            if (coefficientsDenominator.get(i).equals(ZERO)) {
                ord++;
            } else {
                break;
            }
        }

        if (ord == coefficientsDenominator.getBound()) {
            // Nenner = 0; sollte eigentlich nie passieren.
            throw new NotAlgebraicallyIntegrableException();
        }

        // Sonderfall: Nenner ist von der Form t^n.
        if (ord == coefficientsDenominator.getBound() - 1) {

            int degDenominator = coefficientsDenominator.getBound() - 1;
            ExpressionCollection numeratorsInPFD = new ExpressionCollection();
            numeratorsInPFD.put(0, ZERO);
            for (int i = 0; i < coefficientsNumerator.getBound(); i++) {
                numeratorsInPFD.put(degDenominator - i, coefficientsNumerator.get(i));
            }

            return numeratorsInPFD;

        }

        ExpressionCollection numeratorsInPFD = new ExpressionCollection();
        Expression denominator = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar);

        // Berechnung des nichtspeziellen Faktors des Nenner.
        ExpressionCollection nonSpecialFactorOfDenominatorCoefficients = new ExpressionCollection();
        for (int i = ord; i < coefficientsDenominator.getBound(); i++) {
            nonSpecialFactorOfDenominatorCoefficients.add(coefficientsDenominator.get(i));
        }
        Expression nonSpecialFactorOfDenominator = SimplifyPolynomialUtils.getPolynomialFromCoefficients(nonSpecialFactorOfDenominatorCoefficients, transcendentalVar);

        Expression f = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar).div(denominator);
        Expression currentNumerator;

        for (int i = ord; i > 0; i--) {
            currentNumerator = f;
            for (int j = ord; j > i; j--) {
                currentNumerator = currentNumerator.sub(numeratorsInPFD.get(j).div(Variable.create(transcendentalVar).pow(j)));
            }
            try {
                currentNumerator = currentNumerator.mult(Variable.create(transcendentalVar).pow(i));
                // Der gesamte Ausdruck muss auf einen Nenner gebracht werden. Dann wird "standardisiert" vereinfacht.
                currentNumerator = currentNumerator.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
                currentNumerator = currentNumerator.simplify().replaceVariable(transcendentalVar, ZERO);
                currentNumerator = currentNumerator.simplify();
                numeratorsInPFD.put(i, currentNumerator);
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }
        currentNumerator = f;
        for (int j = ord; j > 0; j--) {
            currentNumerator = currentNumerator.sub(numeratorsInPFD.get(j).div(Variable.create(transcendentalVar).pow(j)));
        }
        try {
            currentNumerator = currentNumerator.mult(nonSpecialFactorOfDenominator);
            // Der gesamte Ausdruck muss auf einen Nenner gebracht werden. Dann wird "standardisiert" vereinfacht.
            currentNumerator = currentNumerator.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            currentNumerator = currentNumerator.simplify();
            numeratorsInPFD.put(0, currentNumerator);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        /* 
         Jetzt prüfen, ob die Zähler der PBZ gewisse Bedingungen erfüllen:
         (1) Die Zähler zu den Nennern t^k sind bzgl. t konstant (t = transcendentalVar).    
         (2) Der Zähler zum nichtspeziellen Nenner hat Grad < grad(Nenner).    
         Dies sollte bei korrekter Berechnung immer der Fall sein.
         */
        for (int i = 1; i < numeratorsInPFD.getBound(); i++) {
            if (numeratorsInPFD.get(i).contains(transcendentalVar)) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }
        if (!SimplifyPolynomialUtils.isPolynomial(numeratorsInPFD.get(0), transcendentalVar)) {
            throw new NotAlgebraicallyIntegrableException();
        }
        try {
            ExpressionCollection coefficientsNumeratorOfNonSpecialPart = SimplifyPolynomialUtils.getPolynomialCoefficients(numeratorsInPFD.get(0), transcendentalVar);
            if (coefficientsNumeratorOfNonSpecialPart.getBound() >= nonSpecialFactorOfDenominatorCoefficients.getBound()) {
                throw new NotAlgebraicallyIntegrableException();
            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        return numeratorsInPFD;

    }

    /**
     * Risch-Algorithmus für den polynomialen Anteil.
     *
     * @throws NotAlgebraicallyIntegrableException
     * @throws EvaluationException
     */
    private static Expression integrateByRischAlgorithmPolynomialPart(ExpressionCollection polynomialCoefficients, ExpressionCollection laurentCoefficients,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException, EvaluationException {

        // Fall: Es gibt keinen polynomiellen Anteil.
        if (polynomialCoefficients.isEmpty() && laurentCoefficients.isEmpty()) {
            return ZERO;
        }

        // Fall: Es gibt einen polynomiellen Anteil, transzendentes Element ist exponentiell.
        if ((!polynomialCoefficients.isEmpty() || !laurentCoefficients.isEmpty()) && transcententalElement.isFunction(TypeFunction.exp)) {
            return integrateByRischAlgorithmPolynomialPartExponentialExtension(polynomialCoefficients, laurentCoefficients, transcententalElement, var, transcendentalVar);
        }

        // Fall: Es gibt einen polynomiellen Anteil, transzendentes Element ist logarithmisch.
        if (!polynomialCoefficients.isEmpty() && transcententalElement.isFunction(TypeFunction.ln)) {
            return integrateByRischAlgorithmPolynomialPartLogarithmicExtension(polynomialCoefficients, transcententalElement, var, transcendentalVar);
        }

        // Sollte eigentlich nie eintreten.
        throw new NotAlgebraicallyIntegrableException();

    }

    /**
     * Risch-Algorithmus für den polynomialen Anteil im Fall einer
     * logarithmischen Erweiterung.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateByRischAlgorithmPolynomialPartLogarithmicExtension(ExpressionCollection polynomialCoefficients, Expression transcententalElement,
            String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        Expression logArgument = ((Function) transcententalElement).getLeft();

        Expression[] coefficientsOfPolynomialInTranscendentalVar = new Expression[polynomialCoefficients.getBound() + 1];
        String[] freeConstantsVars = new String[polynomialCoefficients.getBound() + 1];

        for (int i = 0; i < freeConstantsVars.length; i++) {
            freeConstantsVars[i] = NotationLoader.FREE_INTEGRATION_CONSTANT_VAR + "_" + i;
        }

        Expression integral;
        Expression equoationForFreeConstant;
        ExpressionCollection valuesForFreeConstant;
        for (int i = polynomialCoefficients.getBound(); i >= 0; i--) {

            if (i == polynomialCoefficients.getBound()) {
                coefficientsOfPolynomialInTranscendentalVar[i] = Variable.create(freeConstantsVars[i]);
            } else {

                try {
                    integral = polynomialCoefficients.get(i).sub(new Constant(i + 1).mult(coefficientsOfPolynomialInTranscendentalVar[i + 1]).mult(logArgument.diff(var)).div(logArgument)).simplify(simplifyTypesRischAlgorithm);
                    integral = new Operator(TypeOperator.integral, new Object[]{integral, var}).simplify(simplifyTypesRischAlgorithm);

                    integral = SubstitutionUtilities.substituteExpressionByAnotherExpression(integral, transcententalElement, Variable.create(transcendentalVar));
                    equoationForFreeConstant = integral.diff(transcendentalVar).simplify(simplifyTypesRischAlgorithm);

                    if (!SimplifyPolynomialUtils.isLinearPolynomial(equoationForFreeConstant, freeConstantsVars[i + 1])) {
                        throw new NotAlgebraicallyIntegrableException();
                    }
                    valuesForFreeConstant = SolveGeneralEquationUtils.solvePolynomialEquation(equoationForFreeConstant, freeConstantsVars[i + 1]);
                    if (valuesForFreeConstant.getBound() != 1) {
                        throw new NotAlgebraicallyIntegrableException();
                    }
                    coefficientsOfPolynomialInTranscendentalVar[i + 1] = coefficientsOfPolynomialInTranscendentalVar[i + 1].replaceVariable(freeConstantsVars[i + 1], valuesForFreeConstant.get(0));
                    coefficientsOfPolynomialInTranscendentalVar[i] = new Operator(TypeOperator.integral, new Object[]{
                        polynomialCoefficients.get(i).sub(new Constant(i + 1).mult(coefficientsOfPolynomialInTranscendentalVar[i + 1]).mult(logArgument.diff(var)).div(logArgument)), var}).simplify(simplifyTypesRischAlgorithm);
                    if (coefficientsOfPolynomialInTranscendentalVar[i].containsIndefiniteIntegral()) {
                        throw new NotAlgebraicallyIntegrableException();
                    }
                    if (i > 0) {
                        coefficientsOfPolynomialInTranscendentalVar[i] = coefficientsOfPolynomialInTranscendentalVar[i].add(Variable.create(freeConstantsVars[i]));
                    }
                } catch (EvaluationException | NotAlgebraicallySolvableException e) {
                    throw new NotAlgebraicallyIntegrableException();
                }

            }

        }

        return SimplifyPolynomialUtils.getPolynomialFromCoefficients(new ExpressionCollection(coefficientsOfPolynomialInTranscendentalVar),
                transcendentalVar).replaceVariable(transcendentalVar, transcententalElement);

    }

    /**
     * Risch-Algorithmus für den polynomialen Anteil im Fall einer
     * exponentiellen Erweiterung.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateByRischAlgorithmPolynomialPartExponentialExtension(ExpressionCollection polynomialCoefficients, ExpressionCollection laurentCoefficients,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        Expression solution = ZERO, solutionOfRischDiffEq;
        Expression expArgument = ((Function) transcententalElement).getLeft();

        /*
        Hier wird eine Funktion vom Typ f = h_{-m}*t^(-m) + ... + h_n*t^n, m = laurentCoefficients.getBound(),
        n = polynomialCoefficients.getBound() gelöst (h_i, i < 0, sind Elemente von laurentCoefficients,
        h_i, i >= 0 Elemente von polynomialCoefficients). Da die Stammfunktion vom selben Typ ist,
        nämlich von der Form y_{-m}*t^(-m) + ... + y_n*t^n, ergeben sich die Differentialgleichungen
        y_i' + iy_i = h_i für jedes -m <= i <= n. Für jedes solche i wird von solveRischDifferentialEquation(...)
        eine Lösung dieser Differentialgleichung geliefert, sofern diese existiert. Dann wird entsprechend
        summiert und die gesamte Stammfunktion wird zurückgegeben.
         */
        Expression f, g, integralOfF;
        for (int i = 0; i < polynomialCoefficients.getBound(); i++) {
            try {
                f = new Constant(i).mult(expArgument.diff(var)).simplify(simplifyTypesRischDifferentialEquation);
                integralOfF = new Constant(i).mult(expArgument).simplify(simplifyTypesRischDifferentialEquation);
                g = polynomialCoefficients.get(i);
                solutionOfRischDiffEq = solveRischDifferentialEquation(f, integralOfF, g, expArgument, var);
                solution = solution.add(solutionOfRischDiffEq.mult(transcententalElement.pow(i)));
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }

        for (int i = 1; i < laurentCoefficients.getBound(); i++) {
            try {
                f = new Constant(-i).mult(expArgument.diff(var)).simplify(simplifyTypesRischDifferentialEquation);
                integralOfF = new Constant(-i).mult(expArgument).simplify(simplifyTypesRischDifferentialEquation);
                g = laurentCoefficients.get(i);
                solutionOfRischDiffEq = solveRischDifferentialEquation(f, integralOfF, g, expArgument, var);
                solution = solution.add(solutionOfRischDiffEq.mult(transcententalElement.pow(-i)));
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }

        return solution;

    }

    /**
     * Gibt die Lösung der Risch-Differentialgleichung y' + f*y = g zurück, wenn
     * diese lösbar und die Lösung rational in t ist. Ansonsten wird eine
     * NotAlgebraicallyIntegrableException geworfen.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression solveRischDifferentialEquation(Expression f, Expression integralOfF, Expression g, Expression expArgument, String var) throws NotAlgebraicallyIntegrableException {

        String transcendentalVar = SubstitutionUtilities.getSubstitutionVariable(f, g);

        // Sonderfall: f = 0. Dann ist y = int(g, x), x = var.
        if (f.equals(ZERO)) {
            try {
                return GeneralIntegralUtils.indefiniteIntegration(new Operator(TypeOperator.integral, new Object[]{g, var}), true).simplify(simplifyTypesRischDifferentialEquation);
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }

        // Zunächst: es muss das transzendente Element bestimmt werden.
        ExpressionCollection transcendentalExtensions = getOrderedTranscendentalGeneratorsForDifferentialField(new Expression[]{f, g}, var);
        Expression transcendentalElement;
        if (transcendentalExtensions.isEmpty()) {
            transcendentalElement = Variable.create(var);
        } else {
            transcendentalElement = transcendentalExtensions.getLast();
        }

        // Schritt 1: Zur schwachen Normierung übergehen.
        Expression[] weakNormalization = getWeaklyNormalizationOfFunction(f, integralOfF, g, var, transcendentalVar);
        if (weakNormalization.length != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }
        Expression p = weakNormalization[0];
        f = weakNormalization[1];
        g = weakNormalization[2];

        Expression[] denominatorData = getDenominatorDataOfRischDifferentialEquation(f, g, transcendentalElement, transcendentalVar, var);
        if (denominatorData.length != 4) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression denominatorOfSolution = denominatorData[0];
        Expression a = denominatorData[1];
        Expression b = denominatorData[2];
        Expression c = denominatorData[3];
        Expression numeratorOfSolution = getNumeratorOfRischDifferentialEquation(a, b, c, expArgument, transcendentalElement, var, transcendentalVar);

        return numeratorOfSolution.div(p.mult(denominatorOfSolution));

    }

    /**
     * Liefert den Nenner T der Lösung der Risch-Differentialgleichung y' + f*y
     * = g, falls diese lösbar ist, und zusätzlich Koeffizienten a, b, c, so
     * dass der Zähler q die Differentialgleichung a*q' + b*q = c erfüllt. Es
     * wird ein Array von Expression von der Form {T, a, b, c} zurückgegeben.
     * Ansonsten wird eine NotAlgebraicallyIntegrableException geworfen.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression[] getDenominatorDataOfRischDifferentialEquation(Expression f, Expression g, Expression transcendentalElement,
            String transcendentalVar, String var) throws NotAlgebraicallyIntegrableException {

        try {

            f = f.simplify(simplifyTypesRischAlgorithm);
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            f = f.simplify(simplifyTypesRischAlgorithm);
            g = g.simplify(simplifyTypesRischAlgorithm);
            g = g.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            g = g.simplify(simplifyTypesRischAlgorithm);

            Expression d, e, numeratorOfF, numeratorOfG;
            if (f.isQuotient()) {
                numeratorOfF = ((BinaryOperation) f).getLeft();
                d = ((BinaryOperation) f).getRight();
            } else {
                numeratorOfF = f;
                d = ONE;
            }
            if (g.isQuotient()) {
                numeratorOfG = ((BinaryOperation) g).getLeft();
                e = ((BinaryOperation) g).getRight();
            } else {
                numeratorOfG = g;
                e = ONE;
            }

            // In d und e das transzendente Element durch eine formale Variable ersetzen.
            Expression dSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(d, transcendentalElement, Variable.create(transcendentalVar));
            Expression eSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(e, transcendentalElement, Variable.create(transcendentalVar));

            if (!SimplifyPolynomialUtils.isPolynomialAdmissibleForComputation(dSubstituted, transcendentalVar)) {
                throw new NotAlgebraicallyIntegrableException();
            }
            if (!SimplifyPolynomialUtils.isPolynomialAdmissibleForComputation(eSubstituted, transcendentalVar)) {
                throw new NotAlgebraicallyIntegrableException();
            }

            Expression derivativeOfD = dSubstituted.diff(transcendentalVar).simplify(simplifyTypesRischDifferentialEquation);
            Expression derivativeOfE = eSubstituted.diff(transcendentalVar).simplify(simplifyTypesRischDifferentialEquation);

            ExpressionCollection coefficientsOfD = SimplifyPolynomialUtils.getPolynomialCoefficients(dSubstituted, transcendentalVar);
            ExpressionCollection coefficientsOfE = SimplifyPolynomialUtils.getPolynomialCoefficients(eSubstituted, transcendentalVar);
            ExpressionCollection coefficientsOfG = SimplifyPolynomialUtils.getGGTOfPolynomials(coefficientsOfD, coefficientsOfE);
            ExpressionCollection coefficientsOfDerivativeOfE = SimplifyPolynomialUtils.getPolynomialCoefficients(derivativeOfE, transcendentalVar);
            ExpressionCollection coefficientsOfDerivativeOfG = SimplifyPolynomialUtils.getPolynomialCoefficientsOfDerivative(coefficientsOfG);
            ExpressionCollection coefficientsOfGcdOfEAndDerivativeOfE = SimplifyPolynomialUtils.getGGTOfPolynomials(coefficientsOfE, coefficientsOfDerivativeOfE);
            ExpressionCollection coefficientsOfGcdOfGAndDerivativeOfG = SimplifyPolynomialUtils.getGGTOfPolynomials(coefficientsOfG, coefficientsOfDerivativeOfG);

            ExpressionCollection[] quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsOfGcdOfEAndDerivativeOfE, coefficientsOfGcdOfGAndDerivativeOfG);

            if (!quotient[1].isEmpty()) {
                throw new NotAlgebraicallyIntegrableException();
            }

            Expression denominator = SimplifyPolynomialUtils.getPolynomialFromCoefficients(quotient[0], transcendentalVar).simplify(simplifyTypesRischDifferentialEquation);

            // T ist der Nenner denominator.
            ExpressionCollection coefficientsOfT = quotient[0];

            // Im Nenner transzendente Veränderliche wieder durch ihren eigentlichen Wert ersetzen.
            denominator = denominator.replaceVariable(transcendentalVar, transcendentalElement).simplify(simplifyTypesRischDifferentialEquation);

            // Prüfung, ob DT^2 von E geteilt wird (als Polynome in t = transcendentalVar).
            ExpressionCollection coefficientsOfProductOfDAndTSquare = SimplifyPolynomialUtils.multiplyPolynomials(coefficientsOfT, coefficientsOfT);
            coefficientsOfProductOfDAndTSquare = SimplifyPolynomialUtils.multiplyPolynomials(coefficientsOfD, coefficientsOfProductOfDAndTSquare);
            quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsOfProductOfDAndTSquare, coefficientsOfE);
            if (!quotient[1].isEmpty()) {
                throw new NotAlgebraicallyIntegrableException();
            }

            /* 
             Sei T der Nenner. Dann ist a = DT, b = AT - DT', c = BDT^2/E. 
             D und E sind dabei die oberen d und e.
             */
            Expression derivativeOfDenominator = denominator.diff(var).simplify(simplifyTypesRischDifferentialEquation);
            Expression a = d.mult(denominator);
            Expression b = numeratorOfF.mult(denominator).sub(d.mult(derivativeOfDenominator));
            Expression c = numeratorOfG.mult(d).mult(denominator.pow(2)).div(e).simplify(simplifyTypesRischDifferentialEquation);

            return new Expression[]{denominator, a, b, c};

        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Gibt für die Differentialgleichung y' + f*y = g ein Polynom p in t =
     * transcendentalVar und Functionen F und G zurück, so dass F schwach
     * normiert ist und für z = p*y die Differentialgleichung z' + F*z = G
     * erfüllt ist. Der Parameter integralOfF ist die Stammfunktion von f bzgl.
     * der Integrationsvariablen var. f ist schwach normiert bedeutet dabei,
     * dass integralOfF die Form g + c<sub>1</sub>*ln(v<sub>1</sub>) + ... +
     * c<sub>n</sub>*ln(v<sub>n</sub>), c<sub>i</sub> nicht ganzzahlig und
     * positiv, g rational in t und v<sub>i</sub> polynomial in t, besitzt. Der
     * Rückgabewert ist ein Expression-Array aus drei Elementen der Form {p, F,
     * G}.
     */
    private static Expression[] getWeaklyNormalizationOfFunction(Expression f, Expression integralOfF, Expression g, String var, String transcendentalVar) {

        ExpressionCollection summandsLeftOfIntegralOfF = SimplifyUtilities.getSummandsLeftInExpression(integralOfF);
        ExpressionCollection summandsRightOfIntegralOfF = SimplifyUtilities.getSummandsRightInExpression(integralOfF);

        Expression p = ONE;
        for (Expression summand : summandsLeftOfIntegralOfF) {
            if (summand.isFunction(TypeFunction.ln) && SimplifyPolynomialUtils.isPolynomial(((Function) summand).getLeft(), transcendentalVar)) {
                p = p.mult(((Function) summand).getLeft());
            } else if (summand.isProduct() && ((BinaryOperation) summand).getLeft().isPositiveIntegerConstant()
                    && ((BinaryOperation) summand).getRight().isFunction(TypeFunction.ln)
                    && SimplifyPolynomialUtils.isPolynomial(((Function) ((BinaryOperation) summand).getRight()).getLeft(), transcendentalVar)) {
                p = p.mult(((Function) ((BinaryOperation) summand).getRight()).getLeft().pow(((BinaryOperation) summand).getLeft()));
            }
        }
        for (Expression summand : summandsRightOfIntegralOfF) {
            if (summand.isProduct() && ((BinaryOperation) summand).getLeft().isNegativeIntegerConstant()
                    && ((BinaryOperation) summand).getRight().isFunction(TypeFunction.ln)
                    && SimplifyPolynomialUtils.isPolynomial(((Function) ((BinaryOperation) summand).getRight()).getLeft(), transcendentalVar)) {
                p = p.mult(((Function) ((BinaryOperation) summand).getRight()).getLeft().pow(((BinaryOperation) summand).getLeft().negate()));
            }
        }

        try {
            p = p.simplify(simplifyTypesRischAlgorithm);
            Expression newF = f.sub(p.diff(var).div(p)).simplify(simplifyTypesRischAlgorithm);
            Expression newG = p.mult(g).simplify(simplifyTypesRischAlgorithm);
            return new Expression[]{p, newF, newG};
        } catch (EvaluationException e) {
            return new Expression[0];
        }

    }

    /**
     * Liefert den Zähler der Lösung der Risch-Differentialgleichung a*q' + b*q
     * = c, falls diese lösbar ist. Ansonsten wird eine
     * NotAlgebraicallyIntegrableException geworfen.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression getNumeratorOfRischDifferentialEquation(Expression a, Expression b, Expression c, Expression expArgument,
            Expression transcendentalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        if (transcendentalElement.equals(Variable.create(var))) {
            // Dann sind a, b, c rationale Funktionen in var.
            return getNumeratorOfRischDifferentialEquationInBaseCase(a, b, c, var);
        }
        if (transcendentalElement.isFunction(TypeFunction.exp)) {
            return getNumeratorOfRischDifferentialEquationInExponentialCase(a, b, c, expArgument, transcendentalElement, var, transcendentalVar).replaceVariable(transcendentalVar, transcendentalElement);
        }
        if (transcendentalElement.isFunction(TypeFunction.ln)) {
            return getNumeratorOfRischDifferentialEquationInLogarithmicCase(a, b, c, expArgument, transcendentalElement, var, transcendentalVar).replaceVariable(transcendentalVar, transcendentalElement);
        }

        // Sollte eigentlich nie eintreten.
        throw new NotAlgebraicallyIntegrableException();

    }

    /**
     * Liefert den Zähler der Lösung der Risch-Differentialgleichung a*q' + b*q
     * = c, falls diese lösbar ist und falls das transzendenteElement t die
     * Variable var selbst ist. Ansonsten wird eine
     * NotAlgebraicallyIntegrableException geworfen.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression getNumeratorOfRischDifferentialEquationInBaseCase(Expression a, Expression b, Expression c, String var) throws NotAlgebraicallyIntegrableException {

        try {

            ExpressionCollection coefficientsA = SimplifyPolynomialUtils.getPolynomialCoefficients(a, var);
            ExpressionCollection coefficientsB = SimplifyPolynomialUtils.getPolynomialCoefficients(b, var);
            ExpressionCollection coefficientsC = SimplifyPolynomialUtils.getPolynomialCoefficients(c, var);

            // Fall: a = 1
            if (a.equals(ONE)) {

                // Ist c = 0, so ist q = 0.
                if (coefficientsC.isEmpty()) {
                    return ZERO;
                }

                // Ist b = 0, so ist q = int(c, x), x = var.
                if (coefficientsB.isEmpty()) {
                    return GeneralIntegralUtils.indefiniteIntegration(new Operator(TypeOperator.integral, new Object[]{c, var}), true).simplify(simplifyTypesRischDifferentialEquation);
                }

                // Ab hier gilt b != 0.
                // Hier liefert getUpperBoundForDegreeOfNumeratorInBaseCase() sogar den exakten Grad! 
                BigInteger degNumerator = getUpperBoundForDegreeOfNumeratorInBaseCase(coefficientsA, coefficientsB, coefficientsC);
                if (degNumerator.compareTo(BigInteger.ZERO) < 0) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                Expression leadingCoefficient = coefficientsC.getLast().div(coefficientsB.getLast()).simplify(simplifyTypesRischDifferentialEquation);
                /*
                Ist q_n der Leitkoeffizient von q = Zähler und n = degNumerator der Grad von q, so sei
                cNew = c - (q_n*x^n)' - b*q_n*x^n.
                 */
                Expression cNew = c.sub(leadingCoefficient.mult(Variable.create(var).pow(degNumerator)).diff(var).add(
                        b.mult(leadingCoefficient.mult(Variable.create(var).pow(degNumerator))))).simplify(simplifyTypesRischDifferentialEquation);

                /* 
                restNumerator = r = q - q_n*x^n erfüllt die folgende Differentialgleichung:
                r' + b*r = cNew.
                 */
                Expression restNumerator = getNumeratorOfRischDifferentialEquationInBaseCase(ONE, b, cNew, var);
                return restNumerator.add(leadingCoefficient.mult(Variable.create(var).pow(degNumerator))).simplify(simplifyTypesRischDifferentialEquation);

            } else {

                // Fall: a != 1.
                // Sonderfall: a ist konstant und ungleich 1. Dann wird q' + (b/a)*q = c/a gelöst.
                if (coefficientsA.getBound() == 1 && !coefficientsA.getLast().equals(ONE)) {
                    if (!coefficientsA.getLast().equals(ZERO)) {
                        b = b.div(a).simplify(simplifyTypesRischDifferentialEquation);
                        c = c.div(a).simplify(simplifyTypesRischDifferentialEquation);
                        return getNumeratorOfRischDifferentialEquationInBaseCase(ONE, b, c, var);
                    }
                    // a = 0 sollte eigentlich nie vorkommen.
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Ab hier gilt deg(a) > 0.
                ExpressionCollection coefficientsOfGCDOfAAndB = SimplifyPolynomialUtils.getGGTOfPolynomials(coefficientsA, coefficientsB);

                // Sicherheitshalber. Sollte eigentlich nie passieren.
                if (coefficientsOfGCDOfAAndB.isEmpty()) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Fall: der ggT von A und B ist nicht trivial.
                if (coefficientsOfGCDOfAAndB.getBound() > 1 || !coefficientsOfGCDOfAAndB.getLast().equals(ONE)) {
                    /* 
                    Prüfung, ob d = ggTOfAB auch c teilt. Wenn nicht, gibt es keine Lösung. Ansonsten soll
                    (a/d)*q' + (b/d)*q = c/d gelöst werden.
                     */
                    ExpressionCollection[] quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsC, coefficientsOfGCDOfAAndB);
                    if (!quotient[1].isEmpty()) {
                        throw new NotAlgebraicallyIntegrableException();
                    }

                    coefficientsA = SimplifyPolynomialUtils.polynomialDivision(coefficientsA, coefficientsOfGCDOfAAndB)[0];
                    coefficientsB = SimplifyPolynomialUtils.polynomialDivision(coefficientsB, coefficientsOfGCDOfAAndB)[0];
                    coefficientsC = SimplifyPolynomialUtils.polynomialDivision(coefficientsC, coefficientsOfGCDOfAAndB)[0];
                    a = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsA, var);
                    b = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsB, var);
                    c = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsC, var);
                    return getNumeratorOfRischDifferentialEquationInBaseCase(a, b, c, var);
                }

                // Ab hier sind a und b teilerfremd.
                Expression[] euclideanCoefficients = SimplifyPolynomialUtils.getOptimalEuclideanRepresentation(a, b, c, var);
                if (euclideanCoefficients.length != 2) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Prüfung für die obere Schranke für den Grad des Zählers.
                BigInteger degNumerator = getUpperBoundForDegreeOfNumeratorInBaseCase(coefficientsA, coefficientsB, coefficientsC);
                if (degNumerator.compareTo(BigInteger.ZERO) < 0) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                /* 
                Seien s, t derart, dass s*a + t*b = c gilt. h = (q - t)/a erfüllt
                die Differentialgleichung a*h' + (b + a')*h = s - t' und insbesondere
                gilt deg(h) <= n - deg(a), n = obere Schranke für den Grad von q.
                Im Folgenden sei bNew = b + a', cNew = s - t'.
                 */
                Expression bNew = b.add(a.diff(var)).simplify(simplifyTypesRischDifferentialEquation);
                Expression cNew = euclideanCoefficients[0].sub(euclideanCoefficients[1].diff(var)).simplify(simplifyTypesRischDifferentialEquation);

                Expression solutionOfReducedDiffEq = getNumeratorOfRischDifferentialEquationInBaseCase(a, bNew, cNew, var);
                Expression numerator = solutionOfReducedDiffEq.mult(a).add(euclideanCoefficients[1]).simplify(simplifyTypesRischDifferentialEquation);
                return numerator;

            }

        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Hilfsmethode. Liefert eine obere Schranke für den Grad von q, wobei q die
     * Differentialgleichung a*q' + b*q = c erfüllt. q ist der Zähler der Lösung
     * der Risch-Differentialgleichung.
     */
    private static BigInteger getUpperBoundForDegreeOfNumeratorInBaseCase(ExpressionCollection coefficientsA, ExpressionCollection coefficientsB, ExpressionCollection coefficientsC) {

        if (coefficientsA.getBound() > coefficientsB.getBound() + 1) {
            // deg(q) = deg(c) - deg(a) + 1.
            return BigInteger.valueOf(coefficientsC.getBound() - coefficientsA.getBound() + 1);
        }
        if (coefficientsA.getBound() < coefficientsB.getBound() + 1) {
            // deg(q) = deg(c) - deg(b).
            return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound());
        }

        try {

            Expression possibleUpperBound = MINUS_ONE.mult(coefficientsB.getLast()).div(coefficientsA.getLast()).simplify(simplifyTypesRischDifferentialEquation);
            if (possibleUpperBound.isPositiveIntegerConstant()) {
                // Sonderfall: -lc(b)/lc(a) = n ist eine natürliche Zahl. Dann ist n ebenfalls eine mögliche obere Schranke (neben deg(c) - deg(b)). 
                return ((Constant) possibleUpperBound).getBigIntValue().max(BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound()));
            }

        } catch (EvaluationException e) {
        }

        // Sonst: Standardschranke deg(q) = deg(c) - deg(b) zurückgeben.
        return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound());

    }

    /**
     * Liefert den Zähler der Lösung der Risch-Differentialgleichung a*q' + b*q
     * = c, falls diese lösbar ist und falls das transzendenteElement t
     * exponentiell ist. Ansonsten wird eine NotAlgebraicallyIntegrableException
     * geworfen.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression getNumeratorOfRischDifferentialEquationInExponentialCase(Expression a, Expression b, Expression c,
            Expression expArgument, Expression transcendentalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        try {

            a = SubstitutionUtilities.substituteExpressionByAnotherExpression(a, transcendentalElement, Variable.create(transcendentalVar));
            b = SubstitutionUtilities.substituteExpressionByAnotherExpression(b, transcendentalElement, Variable.create(transcendentalVar));
            c = SubstitutionUtilities.substituteExpressionByAnotherExpression(c, transcendentalElement, Variable.create(transcendentalVar));

            ExpressionCollection coefficientsA = SimplifyPolynomialUtils.getPolynomialCoefficients(a.simplify(simplifyTypesRischDifferentialEquation), transcendentalVar);
            ExpressionCollection coefficientsB = SimplifyPolynomialUtils.getPolynomialCoefficients(b.simplify(simplifyTypesRischDifferentialEquation), transcendentalVar);
            ExpressionCollection coefficientsC = SimplifyPolynomialUtils.getPolynomialCoefficients(c.simplify(simplifyTypesRischDifferentialEquation), transcendentalVar);

            // Fall: a = 1
            if (a.equals(ONE)) {

                // Ist c = 0, so ist q = 0.
                if (coefficientsC.isEmpty()) {
                    return ZERO;
                }

                // Ist b = 0, so ist q = int(c, x), x = var.
                if (coefficientsB.isEmpty()) {
                    return GeneralIntegralUtils.indefiniteIntegration(new Operator(TypeOperator.integral, new Object[]{c, var}), true).simplify(simplifyTypesRischDifferentialEquation);
                }

                // Ab hier gilt b != 0.
                // Hier liefert getUpperBoundForDegreeOfNumeratorInBaseCase() sogar den exakten Grad! 
                BigInteger degNumerator = getUpperBoundForDegreeOfNumeratorInExponentialCase(coefficientsA, coefficientsB, coefficientsC, expArgument,
                        transcendentalElement, var, transcendentalVar);
                if (degNumerator.compareTo(BigInteger.ZERO) < 0) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                if (coefficientsB.getBound() > 1) {

                    // Fall: deg(b) > 0.
                    Expression leadingCoefficient = coefficientsC.getLast().div(coefficientsB.getLast()).simplify(simplifyTypesRischDifferentialEquation);
                    /*
                    Ist q_n der Leitkoeffizient von q = Zähler und n = degNumerator der Grad von q, so sei
                    cNew = c - (q_n*t^n)' - b*q_n*t^n.
                     */
                    // Hier ist summandToSubtract = (q_n*t^n)'.
                    Expression summandToSubtract = leadingCoefficient.mult(transcendentalElement.pow(degNumerator)).replaceVariable(transcendentalVar, transcendentalElement).diff(var);
                    summandToSubtract = summandToSubtract.simplify(simplifyTypesRischDifferentialEquation);
                    summandToSubtract = SubstitutionUtilities.substituteExpressionByAnotherExpression(summandToSubtract, transcendentalElement, Variable.create(transcendentalVar));

                    Expression cNew = c.sub(summandToSubtract.add(
                            b.mult(leadingCoefficient.mult(Variable.create(transcendentalVar).pow(degNumerator))))).simplify(simplifyTypesRischDifferentialEquation);

                    /* 
                    restNumerator = r = q - q_n*x^n erfüllt die folgende Differentialgleichung:
                    r' + b*r = cNew.
                     */
                    Expression restNumerator = getNumeratorOfRischDifferentialEquationInExponentialCase(ONE, b, cNew, expArgument, transcendentalElement, var, transcendentalVar);
                    return restNumerator.add(leadingCoefficient.mult(Variable.create(transcendentalVar).pow(degNumerator))).simplify(simplifyTypesRischDifferentialEquation);

                } else {

                    // Fall: deg(b) = 0.
                    Expression[] leadingCoefficientData = getDiffEqDataForLeadingCoefficientInExponentialCase(b, a, c, var, transcendentalVar);

                    if (leadingCoefficientData.length == 2) {

                        // Fall: b = f'/f + n*u'.
                        // Dann ist q = int(f*c*t^n)/(f*t^n).
                        Expression f = leadingCoefficientData[0];
                        Expression n = leadingCoefficientData[1];
                        Expression numeratorOfSolution = f.mult(SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsC, transcendentalVar)).mult(
                                transcendentalElement.pow(n));
                        numeratorOfSolution = new Operator(TypeOperator.integral, new Object[]{numeratorOfSolution, var}).simplify(simplifyTypesRischDifferentialEquation);
                        return numeratorOfSolution.div(f.mult(transcendentalElement.pow(n))).simplify(simplifyTypesRischDifferentialEquation);

                    } else {

                        // Fall: b != f'/f + n*u' für alle f und n.
                        // Dann erfüllt lc(q) die DGL: lc(q)' + (b + deg(c)*u')*lc(q) = lc(c).
                        Expression leadingCoefficient = getNumeratorOfRischDifferentialEquation(ONE,
                                b.add(new Constant(coefficientsC.getBound() - 1).mult(expArgument.diff(var))), coefficientsC.getLast(),
                                expArgument, transcendentalElement, var, transcendentalVar);

                        /*
                        Ist q_n der Leitkoeffizient von q = Zähler und n = degNumerator der Grad von q, so sei
                        cNew = c - (q_n*t^n)' - b*q_n*t^n.
                         */
                        Expression summandToSubtract = leadingCoefficient.mult(transcendentalElement).pow(degNumerator).diff(var).simplify(simplifyTypesRischDifferentialEquation);
                        summandToSubtract = SubstitutionUtilities.substituteExpressionByAnotherExpression(summandToSubtract, transcendentalElement, Variable.create(transcendentalVar));
                        Expression cNew = c.sub(summandToSubtract.add(b.mult(leadingCoefficient.mult(Variable.create(transcendentalVar).pow(degNumerator))))).simplify(simplifyTypesRischDifferentialEquation);

                        /* 
                        restNumerator = r = q - q_n*t^n erfüllt die folgende Differentialgleichung:
                        r' + b*r = cNew.
                         */
                        Expression restNumerator = getNumeratorOfRischDifferentialEquationInExponentialCase(ONE, b, cNew, expArgument, transcendentalElement, var, transcendentalVar);
                        return restNumerator.add(leadingCoefficient.mult(transcendentalElement.pow(degNumerator))).simplify(simplifyTypesRischDifferentialEquation);

                    }

                }

            } else {

                // Fall: a != 1.
                // Sonderfall: a ist konstant und ungleich 1. Dann wird q' + (b/a)*q = c/a gelöst.
                if (coefficientsA.getBound() == 1 && !coefficientsA.getLast().equals(ONE)) {
                    if (!coefficientsA.getLast().equals(ZERO)) {
                        b = b.div(a).simplify(simplifyTypesRischDifferentialEquation);
                        c = c.div(a).simplify(simplifyTypesRischDifferentialEquation);
                        return getNumeratorOfRischDifferentialEquationInExponentialCase(ONE, b, c, expArgument, transcendentalElement, var, transcendentalVar);
                    }
                    // a = 0 sollte eigentlich nie vorkommen.
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Ab hier gilt deg(a) > 0.
                ExpressionCollection coefficientsOfGCDOfAAndB = SimplifyPolynomialUtils.getGGTOfPolynomials(coefficientsA, coefficientsB);

                // Sicherheitshalber. Sollte eigentlich nie passieren.
                if (coefficientsOfGCDOfAAndB.isEmpty()) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Fall: der ggT von A und B ist nicht trivial.
                if (coefficientsOfGCDOfAAndB.getBound() > 1 || !coefficientsOfGCDOfAAndB.getLast().equals(ONE)) {
                    /* 
                    Prüfung, ob d = ggTOfAB auch c teilt. Wenn nicht, gibt es keine Lösung. Ansonsten soll
                    (a/d)*q' + (b/d)*q = c/d gelöst werden.
                     */
                    ExpressionCollection[] quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsC, coefficientsOfGCDOfAAndB);
                    if (!quotient[1].isEmpty()) {
                        throw new NotAlgebraicallyIntegrableException();
                    }

                    coefficientsA = SimplifyPolynomialUtils.polynomialDivision(coefficientsA, coefficientsOfGCDOfAAndB)[0];
                    coefficientsB = SimplifyPolynomialUtils.polynomialDivision(coefficientsB, coefficientsOfGCDOfAAndB)[0];
                    coefficientsC = SimplifyPolynomialUtils.polynomialDivision(coefficientsC, coefficientsOfGCDOfAAndB)[0];
                    a = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsA, var);
                    b = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsB, var);
                    c = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsC, var);
                    return getNumeratorOfRischDifferentialEquationInBaseCase(a, b, c, var);
                }

                // Ab hier sind a und b teilerfremd.
                Expression[] euclideanCoefficients = SimplifyPolynomialUtils.getOptimalEuclideanRepresentation(a, b, c, var);
                if (euclideanCoefficients.length != 2) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Prüfung für die obere Schranke für den Grad des Zählers.
                BigInteger degNumerator = getUpperBoundForDegreeOfNumeratorInExponentialCase(coefficientsA, coefficientsB, coefficientsC,
                        expArgument, transcendentalElement, var, transcendentalVar);
                if (degNumerator.compareTo(BigInteger.ZERO) < 0) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                /* 
                Seien s, t derart, dass s*a + t*b = c gilt. h = (q - t)/a erfüllt
                die Differentialgleichung a*h' + (b + a')*h = s - t' und insbesondere
                gilt deg(h) <= n - deg(a), n = obere Schranke für den Grad von q.
                Im Folgenden sei bNew = b + a', cNew = s - t'.
                 */
                Expression bNew = b.add(a.diff(var)).simplify(simplifyTypesRischDifferentialEquation);
                Expression cNew = euclideanCoefficients[0].sub(euclideanCoefficients[1].diff(var)).simplify(simplifyTypesRischDifferentialEquation);

                Expression solutionOfReducedDiffEq = getNumeratorOfRischDifferentialEquationInExponentialCase(a, bNew, cNew, expArgument, transcendentalElement, var, transcendentalVar);
                Expression numerator = solutionOfReducedDiffEq.mult(a).add(euclideanCoefficients[1]).simplify(simplifyTypesRischDifferentialEquation);
                return numerator;

            }

        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Hilfsmethode. Liefert eine obere Schranke für den Grad von q, wobei q die
     * Differentialgleichung a*q' + b*q = c erfüllt. q ist der Zähler der Lösung
     * der Risch-Differentialgleichung.
     */
    private static BigInteger getUpperBoundForDegreeOfNumeratorInExponentialCase(ExpressionCollection coefficientsA, ExpressionCollection coefficientsB, ExpressionCollection coefficientsC,
            Expression expArgument, Expression transcendentalElement, String var, String transcendentalVar) {

        if (coefficientsA.getBound() > coefficientsB.getBound()) {
            // deg(q) = deg(c) - deg(a).
            return BigInteger.valueOf(coefficientsC.getBound() - coefficientsA.getBound());
        }
        if (coefficientsA.getBound() < coefficientsB.getBound()) {
            // deg(q) = deg(c) - deg(b).
            return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound());
        }

        try {

            Expression integralOfQuotientOfLC = new Operator(TypeOperator.integral,
                    new Object[]{MINUS_ONE.mult(coefficientsB.getLast()).div(coefficientsA.getLast()).replaceVariable(transcendentalVar, transcendentalElement),
                        var}).simplify(simplifyTypesRischDifferentialEquation);
            if (!integralOfQuotientOfLC.isOperator(TypeOperator.integral)) {

                /* 
                Hier kommt eine wichtige Annahme zum Tragen: in expArgument
                kommt kein Summand vor, welches ein rationales Vielfaches des
                Logarithmus eines Ausdrucks ist, welcher var enthält. Wäre
                dies so, dann würde der Integrand entweder algebraische Anteile 
                enthalten (im rationalen Fall: z.B. u = 3*ln(1+x^2)/5, dann wäre 
                exp(u) = (1+x^2)^(3/5)) oder er würde sich weiter vereinfachen 
                lassen (im ganzzahligen Fall: z.B. u = 3*ln(1+x^2), dann wäre 
                exp(u) = (1+x^2)^3).
                 */
                integralOfQuotientOfLC = SubstitutionUtilities.substituteExpressionByAnotherExpression(integralOfQuotientOfLC, transcendentalElement,
                        Variable.create(transcendentalVar));
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(integralOfQuotientOfLC);
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(integralOfQuotientOfLC);

                // Alle entsprechenden Logarithmussummanden entfernen.
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    if (isIntegerMultipleOfLogarithmWithoutTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                        summandsLeft.remove(i);
                    }
                }
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    if (isIntegerMultipleOfLogarithmWithoutTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                        summandsLeft.remove(i);
                    }
                }

                /* 
                Falls der Rest = n*u, u = expArgument ist, dann ist die obere Schranke
                durch max(deg(c) - deg(b), n) gegeben.
                 */
                Expression rest = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
                Expression quotient = rest.div(expArgument).simplify();
                if (quotient.isNonNegativeIntegerConstant()) {
                    return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound()).max(((Constant) quotient).getBigIntValue());
                }

            }

        } catch (EvaluationException e) {
        }

        // Sonst: Standardschranke deg(q) = deg(c) - deg(b) zurückgeben.
        return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound());

    }

    /**
     * Hilfsmethode für getUpperBoundForDegreeOfNumeratorInExponentialCase().
     * Gibt zurück, ob f der Logarithmus einer transzendenten Funktion ist,
     * welche transcendentalVar nicht enthält.
     */
    private static boolean isIntegerMultipleOfLogarithmWithoutTranscendentalVar(Expression f, String transcendentalVar) {
        return f.isFunction(TypeFunction.ln) && !((Function) f).getLeft().contains(transcendentalVar)
                || f.isProduct() && ((BinaryOperation) f).getLeft().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isFunction(TypeFunction.ln)
                && !((Function) ((BinaryOperation) f).getRight()).getLeft().contains(transcendentalVar);
    }

    /**
     * Hilfsmethode. Liefert, falls b = f'/f + n*u' für ein natürliches n und
     * ein Polynom f in t = transcendentalVar ist, das Expression-Array {f, n}.
     * Ansonsten wird ein Expression-Array der Länge 0 zurückgegeben.
     */
    private static Expression[] getDiffEqDataForLeadingCoefficientInExponentialCase(Expression b,
            Expression expArgument, Expression transcendentalElement, String var, String transcendentalVar) {

        try {

            Expression integralOfB = new Operator(TypeOperator.integral, new Object[]{b, var}).simplify(simplifyTypesRischDifferentialEquation);
            if (!integralOfB.isOperator(TypeOperator.integral)) {

                /* 
                Hier kommt eine wichtige Annahme zum Tragen: in expArgument
                kommt kein Summand vor, welches ein rationales Vielfaches des
                Logarithmus eines Ausdrucks ist, welcher var enthält. Wäre
                dies so, dann würde der Integrand entweder algebraische Anteile 
                enthalten (im rationalen Fall: z.B. u = 3*ln(1+x^2)/5, dann wäre 
                exp(u) = (1+x^2)^(3/5)) oder er würde sich weiter vereinfachen 
                lassen (im ganzzahligen Fall: z.B. u = 3*ln(1+x^2), dann wäre 
                exp(u) = (1+x^2)^3).
                 */
                integralOfB = SubstitutionUtilities.substituteExpressionByAnotherExpression(integralOfB, transcendentalElement,
                        Variable.create(transcendentalVar));
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(integralOfB);
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(integralOfB);

                Expression f = ONE;

                // Alle entsprechenden Logarithmussummanden entfernen.
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    if (isfLogarithmOfPolynomialTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                        if (summandsLeft.get(i).isFunction(TypeFunction.ln)) {
                            f = f.mult(((Function) summandsLeft.get(i)).getLeft());
                        } else if (summandsLeft.get(i).isProduct() && ((BinaryOperation) summandsLeft.get(i)).getLeft().isIntegerConstant()
                                && ((BinaryOperation) summandsLeft.get(i)).getRight().isFunction(TypeFunction.ln)) {
                            f = f.mult(((Function) ((BinaryOperation) summandsLeft.get(i)).getRight()).getLeft().pow(((BinaryOperation) summandsLeft.get(i)).getLeft()));
                        }
                        summandsLeft.remove(i);
                    }
                }
                for (int i = 0; i < summandsRight.getBound(); i++) {
                    if (isfLogarithmOfPolynomialTranscendentalVar(summandsRight.get(i), transcendentalVar)) {
                        if (summandsRight.get(i).isFunction(TypeFunction.ln)) {
                            f = f.div(((Function) summandsRight.get(i)).getLeft());
                        } else if (summandsRight.get(i).isProduct() && ((BinaryOperation) summandsRight.get(i)).getLeft().isIntegerConstant()
                                && ((BinaryOperation) summandsRight.get(i)).getRight().isFunction(TypeFunction.ln)) {
                            f = f.div(((Function) ((BinaryOperation) summandsRight.get(i)).getRight()).getLeft().pow(((BinaryOperation) summandsRight.get(i)).getLeft()));
                        }
                    }
                }

                f = f.simplify(simplifyTypesRischDifferentialEquation);
                if (!SimplifyPolynomialUtils.isPolynomial(f, transcendentalVar)) {
                    return new Expression[0];
                }

                /* 
                Falls der Rest = n*u, u = expArgument ist, {f, n} zurückgeben.
                 */
                Expression rest = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
                Expression quotient = rest.div(expArgument).simplify();
                if (quotient.isNonNegativeIntegerConstant()) {
                    return new Expression[]{f, quotient};
                }

            }

        } catch (EvaluationException e) {
        }

        return new Expression[0];

    }

    /**
     * Hilfsmethode für getDiffEqDataForLeadingCoefficient(). Gibt zurück, ob f
     * der Logarithmus einer transzendenten Funktion ist, welche ein Polynom in
     * transcendentalVar ist.
     */
    private static boolean isfLogarithmOfPolynomialTranscendentalVar(Expression f, String transcendentalVar) {
        return f.isFunction(TypeFunction.ln) && SimplifyPolynomialUtils.isPolynomial(((Function) f).getLeft(), transcendentalVar)
                || f.isProduct() && ((BinaryOperation) f).getLeft().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isFunction(TypeFunction.ln)
                && SimplifyPolynomialUtils.isPolynomial(((Function) ((BinaryOperation) f).getRight()).getLeft(), transcendentalVar);
    }

    /**
     * Liefert den Zähler der Lösung der Risch-Differentialgleichung a*q' + b*q
     * = c, falls diese lösbar ist und falls das transzendenteElement t
     * logarithmisch ist. Ansonsten wird eine
     * NotAlgebraicallyIntegrableException geworfen.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression getNumeratorOfRischDifferentialEquationInLogarithmicCase(Expression a, Expression b, Expression c,
            Expression expArgument, Expression transcendentalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        try {

            a = SubstitutionUtilities.substituteExpressionByAnotherExpression(a, transcendentalElement, Variable.create(transcendentalVar));
            b = SubstitutionUtilities.substituteExpressionByAnotherExpression(b, transcendentalElement, Variable.create(transcendentalVar));
            c = SubstitutionUtilities.substituteExpressionByAnotherExpression(c, transcendentalElement, Variable.create(transcendentalVar));

            ExpressionCollection coefficientsA = SimplifyPolynomialUtils.getPolynomialCoefficients(a.simplify(simplifyTypesRischDifferentialEquation), transcendentalVar);
            ExpressionCollection coefficientsB = SimplifyPolynomialUtils.getPolynomialCoefficients(b.simplify(simplifyTypesRischDifferentialEquation), transcendentalVar);
            ExpressionCollection coefficientsC = SimplifyPolynomialUtils.getPolynomialCoefficients(c.simplify(simplifyTypesRischDifferentialEquation), transcendentalVar);

            // Fall: a = 1
            if (a.equals(ONE)) {

                // Ist c = 0, so ist q = 0.
                if (coefficientsC.isEmpty()) {
                    return ZERO;
                }

                // Ist b = 0, so ist q = int(c, x), x = var.
                if (coefficientsB.isEmpty()) {
                    return GeneralIntegralUtils.indefiniteIntegration(new Operator(TypeOperator.integral, new Object[]{c, var}), true).simplify(simplifyTypesRischDifferentialEquation);
                }

                // Ab hier gilt b != 0.
                // Hier liefert getUpperBoundForDegreeOfNumeratorInBaseCase() sogar den exakten Grad! 
                BigInteger degNumerator = getUpperBoundForDegreeOfNumeratorInLogarithmicCase(coefficientsA, coefficientsB, coefficientsC, expArgument,
                        transcendentalElement, var, transcendentalVar);
                if (degNumerator.compareTo(BigInteger.ZERO) < 0) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                if (coefficientsB.getBound() > 1) {

                    // Fall: deg(b) > 0.
                    Expression leadingCoefficient = coefficientsC.getLast().div(coefficientsB.getLast()).simplify(simplifyTypesRischDifferentialEquation);
                    /*
                    Ist q_n der Leitkoeffizient von q = Zähler und n = degNumerator der Grad von q, so sei
                    cNew = c - (q_n*t^n)' - b*q_n*t^n.
                     */
                    // Hier ist summandToSubtract = (q_n*t^n)'.
                    Expression summandToSubtract = leadingCoefficient.mult(transcendentalElement.pow(degNumerator)).replaceVariable(transcendentalVar, transcendentalElement).diff(var);
                    summandToSubtract = summandToSubtract.simplify(simplifyTypesRischDifferentialEquation);
                    summandToSubtract = SubstitutionUtilities.substituteExpressionByAnotherExpression(summandToSubtract, transcendentalElement, Variable.create(transcendentalVar));

                    Expression cNew = c.sub(summandToSubtract.add(
                            b.mult(leadingCoefficient.mult(Variable.create(transcendentalVar).pow(degNumerator))))).simplify(simplifyTypesRischDifferentialEquation);

                    /* 
                    restNumerator = r = q - q_n*x^n erfüllt die folgende Differentialgleichung:
                    r' + b*r = cNew.
                     */
                    Expression restNumerator = getNumeratorOfRischDifferentialEquationInLogarithmicCase(ONE, b, cNew, expArgument, transcendentalElement, var, transcendentalVar);
                    return restNumerator.add(leadingCoefficient.mult(Variable.create(transcendentalVar).pow(degNumerator))).simplify(simplifyTypesRischDifferentialEquation);

                } else {

                    // Fall: deg(b) = 0.
                    Expression[] leadingCoefficientData = getDiffEqDataForLeadingCoefficientInLogarithmicCase(b, a, c, var, transcendentalVar);

                    if (leadingCoefficientData.length == 1) {

                        // Fall: b = f'/f.
                        // Dann ist q = int(f*c)/f.
                        Expression f = leadingCoefficientData[0];
                        Expression numeratorOfSolution = f.mult(SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsC, transcendentalVar));
                        numeratorOfSolution = new Operator(TypeOperator.integral, new Object[]{numeratorOfSolution, var}).simplify(simplifyTypesRischDifferentialEquation);
                        return numeratorOfSolution.div(f).simplify(simplifyTypesRischDifferentialEquation);

                    } else {

                        // Fall: b != f'/f für alle f.
                        // Dann erfüllt lc(q) die DGL: lc(q)' + b*lc(q) = lc(c).
                        Expression leadingCoefficient = getNumeratorOfRischDifferentialEquation(ONE, b, coefficientsC.getLast(),
                                expArgument, transcendentalElement, var, transcendentalVar);

                        /*
                        Ist q_n der Leitkoeffizient von q = Zähler und n = degNumerator der Grad von q, so sei
                        cNew = c - (q_n*t^n)' - b*q_n*t^n.
                         */
                        Expression summandToSubtract = leadingCoefficient.mult(transcendentalElement).pow(degNumerator).diff(var).simplify(simplifyTypesRischDifferentialEquation);
                        summandToSubtract = SubstitutionUtilities.substituteExpressionByAnotherExpression(summandToSubtract, transcendentalElement, Variable.create(transcendentalVar));
                        Expression cNew = c.sub(summandToSubtract.add(b.mult(leadingCoefficient.mult(Variable.create(transcendentalVar).pow(degNumerator))))).simplify(simplifyTypesRischDifferentialEquation);

                        /* 
                        restNumerator = r = q - q_n*x^n erfüllt die folgende Differentialgleichung:
                        r' + b*r = cNew.
                         */
                        Expression restNumerator = getNumeratorOfRischDifferentialEquationInLogarithmicCase(ONE, b, cNew, expArgument, transcendentalElement, var, transcendentalVar);
                        return restNumerator.add(leadingCoefficient.mult(transcendentalElement.pow(degNumerator))).simplify(simplifyTypesRischDifferentialEquation);

                    }

                }

            } else {

                // Fall: a != 1.
                // Sonderfall: a ist konstant und ungleich 1. Dann wird q' + (b/a)*q = c/a gelöst.
                if (coefficientsA.getBound() == 1 && !coefficientsA.getLast().equals(ONE)) {
                    if (!coefficientsA.getLast().equals(ZERO)) {
                        b = b.div(a).simplify(simplifyTypesRischDifferentialEquation);
                        c = c.div(a).simplify(simplifyTypesRischDifferentialEquation);
                        return getNumeratorOfRischDifferentialEquationInLogarithmicCase(ONE, b, c, expArgument, transcendentalElement, var, transcendentalVar);
                    }
                    // a = 0 sollte eigentlich nie vorkommen.
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Ab hier gilt deg(a) > 0.
                ExpressionCollection coefficientsOfGCDOfAAndB = SimplifyPolynomialUtils.getGGTOfPolynomials(coefficientsA, coefficientsB);

                // Sicherheitshalber. Sollte eigentlich nie passieren.
                if (coefficientsOfGCDOfAAndB.isEmpty()) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Fall: der ggT von A und B ist nicht trivial.
                if (coefficientsOfGCDOfAAndB.getBound() > 1 || !coefficientsOfGCDOfAAndB.getLast().equals(ONE)) {
                    /* 
                    Prüfung, ob d = ggTOfAB auch c teilt. Wenn nicht, gibt es keine Lösung. Ansonsten soll
                    (a/d)*q' + (b/d)*q = c/d gelöst werden.
                     */
                    ExpressionCollection[] quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsC, coefficientsOfGCDOfAAndB);
                    if (!quotient[1].isEmpty()) {
                        throw new NotAlgebraicallyIntegrableException();
                    }

                    coefficientsA = SimplifyPolynomialUtils.polynomialDivision(coefficientsA, coefficientsOfGCDOfAAndB)[0];
                    coefficientsB = SimplifyPolynomialUtils.polynomialDivision(coefficientsB, coefficientsOfGCDOfAAndB)[0];
                    coefficientsC = SimplifyPolynomialUtils.polynomialDivision(coefficientsC, coefficientsOfGCDOfAAndB)[0];
                    a = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsA, var);
                    b = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsB, var);
                    c = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsC, var);
                    return getNumeratorOfRischDifferentialEquationInBaseCase(a, b, c, var);
                }

                // Ab hier sind a und b teilerfremd.
                Expression[] euclideanCoefficients = SimplifyPolynomialUtils.getOptimalEuclideanRepresentation(a, b, c, transcendentalVar);
                if (euclideanCoefficients.length != 2) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                // Prüfung für die obere Schranke für den Grad des Zählers.
                BigInteger degNumerator = getUpperBoundForDegreeOfNumeratorInLogarithmicCase(coefficientsA, coefficientsB, coefficientsC,
                        expArgument, transcendentalElement, var, transcendentalVar);
                if (degNumerator.compareTo(BigInteger.ZERO) < 0) {
                    throw new NotAlgebraicallyIntegrableException();
                }

                /* 
                Seien s, t derart, dass s*a + t*b = c gilt. h = (q - t)/a erfüllt
                die Differentialgleichung a*h' + (b + a')*h = s - t' und insbesondere
                gilt deg(h) <= n - deg(a), n = obere Schranke für den Grad von q.
                Im Folgenden sei bNew = b + a', cNew = s - t'.
                 */
                Expression derivativeOfA = a.replaceVariable(transcendentalVar, transcendentalElement).diff(var).simplify(simplifyTypesRischDifferentialEquation);
                derivativeOfA = SubstitutionUtilities.substituteExpressionByAnotherExpression(derivativeOfA, transcendentalElement, Variable.create(transcendentalVar));
                Expression derivativeOfT = euclideanCoefficients[1].replaceVariable(transcendentalVar, transcendentalElement).diff(var).simplify(simplifyTypesRischDifferentialEquation);
                derivativeOfT = SubstitutionUtilities.substituteExpressionByAnotherExpression(derivativeOfT, transcendentalElement, Variable.create(transcendentalVar));
                Expression bNew = b.add(derivativeOfA).simplify(simplifyTypesRischDifferentialEquation);
                Expression cNew = euclideanCoefficients[0].sub(derivativeOfT).simplify(simplifyTypesRischDifferentialEquation);

                Expression solutionOfReducedDiffEq = getNumeratorOfRischDifferentialEquationInLogarithmicCase(a, bNew, cNew, expArgument, transcendentalElement, var, transcendentalVar);
                Expression numerator = solutionOfReducedDiffEq.mult(a).add(euclideanCoefficients[1]).simplify(simplifyTypesRischDifferentialEquation);
                return numerator;

            }

        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Hilfsmethode. Liefert, falls b = f'/f für ein Polynom f in t =
     * transcendentalVar ist, das Expression-Array {f}. Ansonsten wird ein
     * Expression-Array der Länge 0 zurückgegeben.
     */
    private static Expression[] getDiffEqDataForLeadingCoefficientInLogarithmicCase(Expression b,
            Expression expArgument, Expression transcendentalElement, String var, String transcendentalVar) {

        try {

            Expression integralOfB = new Operator(TypeOperator.integral, new Object[]{b, var}).simplify(simplifyTypesRischDifferentialEquation);
            if (!integralOfB.isOperator(TypeOperator.integral)) {

                /* 
                Hier kommt eine wichtige Annahme zum Tragen: in expArgument
                kommt kein Summand vor, welches ein rationales Vielfaches des
                Logarithmus eines Ausdrucks ist, welcher var enthält. Wäre
                dies so, dann würde der Integrand entweder algebraische Anteile 
                enthalten (im rationalen Fall: z.B. u = 3*ln(1+x^2)/5, dann wäre 
                exp(u) = (1+x^2)^(3/5)) oder er würde sich weiter vereinfachen 
                lassen (im ganzzahligen Fall: z.B. u = 3*ln(1+x^2), dann wäre 
                exp(u) = (1+x^2)^3).
                 */
                integralOfB = SubstitutionUtilities.substituteExpressionByAnotherExpression(integralOfB, transcendentalElement,
                        Variable.create(transcendentalVar));
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(integralOfB);
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(integralOfB);

                Expression f = ONE;

                // Alle entsprechenden Logarithmussummanden entfernen.
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    if (isfLogarithmOfPolynomialTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                        if (summandsLeft.get(i).isFunction(TypeFunction.ln)) {
                            f = f.mult(((Function) summandsLeft.get(i)).getLeft());
                        } else if (summandsLeft.get(i).isProduct() && ((BinaryOperation) summandsLeft.get(i)).getLeft().isIntegerConstant()
                                && ((BinaryOperation) summandsLeft.get(i)).getRight().isFunction(TypeFunction.ln)) {
                            f = f.mult(((Function) ((BinaryOperation) summandsLeft.get(i)).getRight()).getLeft().pow(((BinaryOperation) summandsLeft.get(i)).getLeft()));
                        }
                        summandsLeft.remove(i);
                    }
                }
                for (int i = 0; i < summandsRight.getBound(); i++) {
                    if (isfLogarithmOfPolynomialTranscendentalVar(summandsRight.get(i), transcendentalVar)) {
                        if (summandsRight.get(i).isFunction(TypeFunction.ln)) {
                            f = f.div(((Function) summandsRight.get(i)).getLeft());
                        } else if (summandsRight.get(i).isProduct() && ((BinaryOperation) summandsRight.get(i)).getLeft().isIntegerConstant()
                                && ((BinaryOperation) summandsRight.get(i)).getRight().isFunction(TypeFunction.ln)) {
                            f = f.div(((Function) ((BinaryOperation) summandsRight.get(i)).getRight()).getLeft().pow(((BinaryOperation) summandsRight.get(i)).getLeft()));
                        }
                    }
                }

                f = f.simplify(simplifyTypesRischDifferentialEquation);
                if (!SimplifyPolynomialUtils.isPolynomial(f, transcendentalVar)) {
                    return new Expression[0];
                }

                if (summandsLeft.isEmpty() && summandsRight.isEmpty()) {
                    return new Expression[]{f};
                }

            }

        } catch (EvaluationException e) {
        }

        return new Expression[0];

    }

    /**
     * Hilfsmethode. Liefert eine obere Schranke für den Grad von q, wobei q die
     * Differentialgleichung a*q' + b*q = c erfüllt. q ist der Zähler der Lösung
     * der Risch-Differentialgleichung.
     */
    private static BigInteger getUpperBoundForDegreeOfNumeratorInLogarithmicCase(ExpressionCollection coefficientsA, ExpressionCollection coefficientsB, ExpressionCollection coefficientsC,
            Expression expArgument, Expression transcendentalElement, String var, String transcendentalVar) {

        if (coefficientsA.getBound() > coefficientsB.getBound() + 1) {
            // deg(q) <= deg(c) - deg(a) + 1.
            return BigInteger.valueOf(coefficientsC.getBound() - coefficientsA.getBound() + 1);
        }
        if (coefficientsA.getBound() < coefficientsB.getBound()) {
            // deg(q) = deg(c) - deg(b).
            return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound());
        }

        if (coefficientsA.getBound() == coefficientsB.getBound() + 1) {
            /* 
            deg(q) <= deg(c) - deg(a) + 1, außer -ln(b)/lc(c) = f' + nt', t = das vorliegende transzendente Element,
            f rationale Funktion in der Körpererweiterung, die t nicht enthält. Im letzteren
            Fall kann deg(q) auch n sein.
             */
            try {

                Expression integralOfQuotientOfLC = new Operator(TypeOperator.integral,
                        new Object[]{MINUS_ONE.mult(coefficientsB.getLast()).div(coefficientsA.getLast()).replaceVariable(transcendentalVar, transcendentalElement),
                            var}).simplify(simplifyTypesRischDifferentialEquation);
                if (!integralOfQuotientOfLC.isOperator(TypeOperator.integral)) {

                    /* 
                    Hier kommt eine wichtige Annahme zum Tragen: in expArgument
                    kommt kein Summand vor, welches ein rationales Vielfaches des
                    Logarithmus eines Ausdrucks ist, welcher var enthält. Wäre
                    dies so, dann würde der Integrand entweder algebraische Anteile 
                    enthalten (im rationalen Fall: z.B. u = 3*ln(1+x^2)/5, dann wäre 
                    exp(u) = (1+x^2)^(3/5)) oder er würde sich weiter vereinfachen 
                    lassen (im ganzzahligen Fall: z.B. u = 3*ln(1+x^2), dann wäre 
                    exp(u) = (1+x^2)^3).
                     */
                    integralOfQuotientOfLC = SubstitutionUtilities.substituteExpressionByAnotherExpression(integralOfQuotientOfLC, transcendentalElement,
                            Variable.create(transcendentalVar));
                    ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(integralOfQuotientOfLC);
                    ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(integralOfQuotientOfLC);

                    // Alle entsprechenden Logarithmussummanden entfernen.
                    for (int i = 0; i < summandsLeft.getBound(); i++) {
                        if (isIntegerMultipleOfLogarithmWithoutTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                            summandsLeft.remove(i);
                        }
                    }
                    for (int i = 0; i < summandsLeft.getBound(); i++) {
                        if (isIntegerMultipleOfLogarithmWithoutTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                            summandsLeft.remove(i);
                        }
                    }

                    /* 
    ^                Falls der Rest = n*u, u = expArgument ist, dann ist die obere Schranke
                     durch max(deg(c) - deg(a) + 1, n) gegeben.
                     */
                    Expression rest = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
                    Expression quotient = rest.div(expArgument).simplify();
                    if (quotient.isNonNegativeIntegerConstant()) {
                        return BigInteger.valueOf(coefficientsC.getBound() - coefficientsA.getBound() + 1).max(((Constant) quotient).getBigIntValue());
                    }

                }

            } catch (EvaluationException e) {
                return BigInteger.valueOf(coefficientsC.getBound() - coefficientsA.getBound() + 1);
            }
        }

        // Fall: deg(a) = deg(b).
        /* 
        deg(q) <= deg(c) - deg(b) + 1, außer -ln(b)/lc(a) = q_n'/q_n und lc(lc(b)*a - lc(a)*b)/ln(a)^2 = f' + nt', 
        t = das vorliegende transzendente Element, q_n und f rationale Funktionen in der Körpererweiterung, 
        die t nicht enthält. Im letzteren Fall kann deg(q) auch n sein.
         */
        try {

            Expression integralOfQuotientOfLC = new Operator(TypeOperator.integral,
                    new Object[]{MINUS_ONE.mult(coefficientsB.getLast()).div(coefficientsA.getLast()).replaceVariable(transcendentalVar, transcendentalElement),
                        var}).simplify(simplifyTypesRischDifferentialEquation);
            if (!integralOfQuotientOfLC.isOperator(TypeOperator.integral)) {

                /* 
                Hier kommt eine wichtige Annahme zum Tragen: in expArgument
                kommt kein Summand vor, welches ein rationales Vielfaches des
                Logarithmus eines Ausdrucks ist, welcher var enthält. Wäre
                dies so, dann würde der Integrand entweder algebraische Anteile 
                enthalten (im rationalen Fall: z.B. u = 3*ln(1+x^2)/5, dann wäre 
                exp(u) = (1+x^2)^(3/5)) oder er würde sich weiter vereinfachen 
                lassen (im ganzzahligen Fall: z.B. u = 3*ln(1+x^2), dann wäre 
                exp(u) = (1+x^2)^3).
                 */
                integralOfQuotientOfLC = SubstitutionUtilities.substituteExpressionByAnotherExpression(integralOfQuotientOfLC, transcendentalElement,
                        Variable.create(transcendentalVar));
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(integralOfQuotientOfLC);
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(integralOfQuotientOfLC);

                // Alle entsprechenden Logarithmussummanden entfernen.
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    if (isIntegerMultipleOfLogarithmWithoutTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                        summandsLeft.remove(i);
                    }
                }
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    if (isIntegerMultipleOfLogarithmWithoutTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                        summandsLeft.remove(i);
                    }
                }

                if (!summandsLeft.isEmpty() || !summandsRight.isEmpty()) {
                    // Fall: -lc(b)/lc(a) != q_n'/q_n für alle q_n. Dann ist deg(q) = deg(c) - deg(b).
                    return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound());
                }

                /* 
                Prüfung, ob lc(lc(b)*a - lc(a)*b)/ln(a)^2 = f' + nt' für geeignete f und n.
                durch max(deg(c) - deg(b), n) gegeben.
                 */
                ExpressionCollection polynomialInNumerator = SimplifyPolynomialUtils.subtractPolynomials(SimplifyPolynomialUtils.multiplyPolynomials(new ExpressionCollection(coefficientsB.getLast()), coefficientsA),
                        SimplifyPolynomialUtils.multiplyPolynomials(new ExpressionCollection(coefficientsA.getLast()), coefficientsB));
                Expression quotient = polynomialInNumerator.getLast().div(coefficientsA.getLast().pow(2)).replaceVariable(transcendentalVar, transcendentalElement).simplify(simplifyTypesRischDifferentialEquation);
                integralOfQuotientOfLC = new Operator(TypeOperator.integral, new Object[]{quotient, var}).simplify(simplifyTypesRischDifferentialEquation);
                integralOfQuotientOfLC = SubstitutionUtilities.substituteExpressionByAnotherExpression(integralOfQuotientOfLC, transcendentalElement,
                        Variable.create(transcendentalVar));

                summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(integralOfQuotientOfLC);
                summandsRight = SimplifyUtilities.getSummandsRightInExpression(integralOfQuotientOfLC);

                // Alle entsprechenden Logarithmussummanden entfernen.
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    if (isIntegerMultipleOfLogarithmWithoutTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                        summandsLeft.remove(i);
                    }
                }
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    if (isIntegerMultipleOfLogarithmWithoutTranscendentalVar(summandsLeft.get(i), transcendentalVar)) {
                        summandsLeft.remove(i);
                    }
                }

                /* 
^                Falls der Rest = n*u, u = expArgument ist, dann ist die obere Schranke
                 durch max(deg(c) - deg(a) + 1, n) gegeben.
                 */
                Expression rest = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
                Expression quotientByTranscendentalElement = rest.div(expArgument).simplify();
                if (quotientByTranscendentalElement.isNonNegativeIntegerConstant()) {
                    return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound()).max(((Constant) quotientByTranscendentalElement).getBigIntValue());
                }

            }

        } catch (EvaluationException e) {
            return BigInteger.valueOf(coefficientsC.getBound() - coefficientsA.getBound() + 1);
        }

        // Sonst: deg(q) = deg(c) - deg(b).
        return BigInteger.valueOf(coefficientsC.getBound() - coefficientsB.getBound());

    }

    /**
     * Risch-Algorithmus für den gebrochenen Anteil.
     *
     * @throws NotAlgebraicallyIntegrableException
     * @throws EvaluationException
     */
    private static Expression integrateByRischAlgorithmFractionalPart(ExpressionCollection coefficientsNumerator, ExpressionCollection coefficientsDenominator,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException, EvaluationException {

        System.out.println("---------- Risch-Algorithmus für den gebrochenen Teil ----------------");

        Expression decompositionOfDenominator;
        try {
            decompositionOfDenominator = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(coefficientsDenominator, transcendentalVar);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        } catch (SimplifyPolynomialUtils.PolynomialNotDecomposableException e) {
            decompositionOfDenominator = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar);
        }

        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactors(decompositionOfDenominator);
        Expression leadingCoefficient = ONE;

        if (factorsDenominator.getBound() == 1) {
            leadingCoefficient = coefficientsDenominator.get(coefficientsDenominator.getBound() - 1);
            factorsDenominator.divideByExpression(leadingCoefficient);
            factorsDenominator = factorsDenominator.simplify(simplifyTypesRischAlgorithm);
        } else {
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                if (!factorsDenominator.get(i).contains(transcendentalVar)) {
                    leadingCoefficient = leadingCoefficient.mult(factorsDenominator.get(i));
                    factorsDenominator.put(i, null);
                }
            }
        }

        leadingCoefficient = leadingCoefficient.simplify(simplifyTypesRischAlgorithm);

        // Nenner normieren!
        decompositionOfDenominator = SimplifyUtilities.produceProduct(factorsDenominator);
        coefficientsNumerator.divideByExpression(leadingCoefficient);
        coefficientsNumerator = coefficientsNumerator.simplify(simplifyTypesRischAlgorithm);

        // Hermite-Reduktion und später expliziten Risch-Algorithmus anwenden, wenn der Nenner quadratfrei ist.
        return doHermiteReduction(coefficientsNumerator, decompositionOfDenominator, transcententalElement, var, transcendentalVar);

    }

    ////////////////////////////////////////////////// Die Hermite-Reduktion ///////////////////////////////////////////
    /**
     * Integration mittels Hermite-Reduktion.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression doHermiteReduction(ExpressionCollection coefficientsNumerator, Expression denominator,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        System.out.println("Hermite-Reduktion:");
        System.out.println("Transzendentes Element = " + transcententalElement);
        System.out.println("Zählerkoeffizienten = " + coefficientsNumerator + "; Nenner = " + denominator);

        // Zunächst: Prüfung, ob Zähler und Nenner teilerfremd sind. Wenn nicht: kürzen, neuen Nenner wieder zerlegen und fortfahren.
        ExpressionCollection coefficientsDenominator;
        try {

            // Alle transzendenten Ausdrücke werden vorübergehend durch Unbestimmte ersetzt -> verkürzt Rechenaufwand.
            Expression numerator = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar);
            ExpressionCollection transcendentalExtensions = getOrderedTranscendentalGeneratorsForDifferentialField(numerator.div(denominator), var);
            String[] transcendentalVars = new String[transcendentalExtensions.getBound()];
            for (int i = transcendentalExtensions.getBound() - 1; i >= 0; i--) {
                transcendentalVars[i] = SubstitutionUtilities.getSubstitutionVariable(coefficientsNumerator, denominator);
                coefficientsNumerator = SubstitutionUtilities.substituteExpressionByAnotherExpressionInExpressionCollection(
                        coefficientsNumerator, transcendentalExtensions.get(i), Variable.create(transcendentalVars[i]));
                denominator = SubstitutionUtilities.substituteExpressionByAnotherExpression(denominator, transcendentalExtensions.get(i), Variable.create(transcendentalVars[i]));
            }

            Expression integrandSimplified = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar).div(denominator);
            System.out.println("Integrand = " + integrandSimplified);
            integrandSimplified = prepareIntegrandForHermiteReduction(integrandSimplified, transcendentalVar);

            // Rücksubstitution.
            for (int i = 0; i < transcendentalVars.length; i++) {
                integrandSimplified = integrandSimplified.replaceVariable(transcendentalVars[i], transcendentalExtensions.get(i));
                denominator = denominator.replaceVariable(transcendentalVars[i], transcendentalExtensions.get(i));
            }

            System.out.println("Vereinfachter Integrand = " + integrandSimplified);

            if (!SimplifyRationalFunctionUtils.isRationalFunction(integrandSimplified, transcendentalVar)) {
                // Sollte eigentlich nie vorkommen. Trotzdem: sicherheitshalber abfragen!
                throw new NotAlgebraicallyIntegrableException();
            }

            if (!integrandSimplified.contains(transcendentalVar)) {
                /* 
                 In diesem Fall: Erneute Integration, welche auf Ad-Hoc-Methoden, Risch-Algorithmus 
                 mit weniger transcendenten Erweiterungen oder auf Integration mittels Partialbruchzerlegung
                 hinausläuft.
                 */
                return GeneralIntegralUtils.integrateIndefinite(new Operator(TypeOperator.integral, new Object[]{integrandSimplified, var}));
            }

            if (integrandSimplified.isQuotient()) {
                coefficientsDenominator = SimplifyPolynomialUtils.getPolynomialCoefficients(((BinaryOperation) integrandSimplified).getRight(), transcendentalVar);
                try {
                    denominator = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(coefficientsDenominator, transcendentalVar);
                } catch (PolynomialNotDecomposableException e) {
                    // Nichts tun, der Nenner ist dann einfach unzerlegbar.
                    denominator = ((BinaryOperation) integrandSimplified).getRight();
                }
            } else {
                throw new NotAlgebraicallyIntegrableException();
            }

            coefficientsNumerator = SimplifyPolynomialUtils.getPolynomialCoefficients(((BinaryOperation) integrandSimplified).getLeft(), transcendentalVar);
            int degNumerator = coefficientsNumerator.getBound() - 1;
            int degDenominator = coefficientsDenominator.getBound() - 1;

            /*
             Der Grad des Zählers sollte eigentlich bei jedem Reduktionsschritt kleiner als der Grad des Nenners 
             bzgl. der Veränderlichen t = transcendentalVar sein.
             (Der Fall, dass beide Polynome konstant sind, der Integrand also eine rationale Funktion in den 
             restlichen Veränderlichen ist, wurde bereits vorher behandelt). Trotzdem diese Sicherheitsabfrage.
             */
            if (degNumerator >= degDenominator && integrandSimplified.contains(transcendentalVar)) {
                throw new NotAlgebraicallyIntegrableException();
            }

            if (denominator.isNotPower() && denominator.isNotProduct()) {
                // Nenner ist quadratfrei -> explizit Stammfunktion bestimmen.
                try {
                    return integrateByRischAlgorithmInSquareFreeCase(coefficientsNumerator, coefficientsDenominator, transcententalElement, var, transcendentalVar);
                } catch (EvaluationException e) {
                    throw new NotAlgebraicallyIntegrableException();
                }
            }

            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactors(denominator);
            for (int i = factorsDenominator.getBound() - 1; i >= 0; i--) {
                if (factorsDenominator.get(i).isIntegerPower()) {

                    BigInteger m = ((Constant) ((BinaryOperation) factorsDenominator.get(i)).getRight()).getBigIntValue();
                    Expression v = ((BinaryOperation) factorsDenominator.get(i)).getLeft();
                    factorsDenominator.put(i, null);
                    Expression u = SimplifyUtilities.produceProduct(factorsDenominator);
                    Expression derivativeOfV;
                    try {
                        derivativeOfV = v.replaceVariable(transcendentalVar, transcententalElement);
                        derivativeOfV = derivativeOfV.diff(var);
                        derivativeOfV = SubstitutionUtilities.substituteExpressionByAnotherExpression(derivativeOfV, transcententalElement, Variable.create(transcendentalVar)).simplify(simplifyTypesRischAlgorithm);

                        System.out.println("m = " + m);
                        System.out.println("u = " + u);
                        System.out.println("v = " + v);

                        Expression gcd = SimplifyPolynomialUtils.getGGTOfPolynomials(u.mult(derivativeOfV), v, transcendentalVar);
                        if (!gcd.equals(ONE)) {
                            // Der ggT ist nur zur Kontrolle gedacht. Sollte eigentlich laut Theorem nie passieren!
                            throw new NotAlgebraicallyIntegrableException();
                        }

                        Expression a = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar);
                        Expression[] euclideanCoefficients = SimplifyPolynomialUtils.getOptimalEuclideanRepresentation(u.mult(derivativeOfV), v, a.div(ONE.sub(m)).simplify(simplifyTypesRischAlgorithm), transcendentalVar);
                        Expression b = euclideanCoefficients[0].simplify(simplifyTypesRischAlgorithm);
                        Expression c = euclideanCoefficients[1].simplify(simplifyTypesRischAlgorithm);

                        System.out.println("B = " + b);
                        System.out.println("C = " + c);

                        Expression derivativeOfB = b.replaceVariable(transcendentalVar, transcententalElement);
                        derivativeOfB = derivativeOfB.diff(var);
                        derivativeOfB = SubstitutionUtilities.substituteExpressionByAnotherExpression(derivativeOfB, transcententalElement, Variable.create(transcendentalVar)).simplify(simplifyTypesRischAlgorithm);

                        System.out.println("B' = " + derivativeOfB);

                        Expression newIntegrand = ONE.sub(m).mult(c).sub(u.mult(derivativeOfB)).simplify(simplifyTypesRischAlgorithm);
                        ExpressionCollection coefficientsNewNumerator = SimplifyPolynomialUtils.getPolynomialCoefficients(newIntegrand, transcendentalVar);

                        System.out.println("(1-m)C - UB' = " + newIntegrand);

                        return b.div(v.pow(m.subtract(BigInteger.ONE))).replaceVariable(transcendentalVar, transcententalElement).add(
                                doHermiteReduction(coefficientsNewNumerator, u.mult(v.pow(m.subtract(BigInteger.ONE))),
                                        transcententalElement, var, transcendentalVar));

                    } catch (EvaluationException e) {
                        factorsDenominator.put(i, v.pow(m));
                    }

                }
            }

            // Dann ist der Nenner quadratfrei (aber eventuell faktorisiert).
            try {
                return integrateByRischAlgorithmInSquareFreeCase(coefficientsNumerator, coefficientsDenominator, transcententalElement, var, transcendentalVar);
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }

        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Hilfsmethode. Vereinfacht den Integranden passend für die
     * Hermite-Reduktion vor.
     */
    private static Expression prepareIntegrandForHermiteReduction(Expression f, String var) throws EvaluationException {

        // Zunächst passend vereinfachen.
        Expression integrandSimplified = f.simplify(simplifyTypesRischAlgorithm);

        // Nun wieder quadratfreie Faktorisierung des Nenners durchführen.
        return integrandSimplified;

    }

    /**
     * Risch-Algorithmus für den gebrochenen Anteil im Falle eines quadratfreien
     * Nenners.
     *
     * @throws NotAlgebraicallyIntegrableException
     * @throws EvaluationException
     */
    private static Expression integrateByRischAlgorithmInSquareFreeCase(ExpressionCollection coefficientsNumerator, ExpressionCollection coefficientsDenominator,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException, EvaluationException {

        // Leitkoeffizienten vom Nenner in den Zähler verschieben.
        if (!coefficientsDenominator.get(coefficientsDenominator.getBound() - 1).equals(ONE)) {
            coefficientsNumerator.divideByExpression(coefficientsDenominator.get(coefficientsDenominator.getBound() - 1));
            coefficientsNumerator = coefficientsNumerator.simplify(simplifyTypesRischAlgorithm);

            coefficientsDenominator.divideByExpression(coefficientsDenominator.get(coefficientsDenominator.getBound() - 1));
            coefficientsDenominator = coefficientsDenominator.simplify(simplifyTypesRischAlgorithm);
        }

        // Sonderfall: Nenner hat Grad = 0 (also von t nicht abhängig) -> Integration (nach Risch-Algorithmus), Transzendenzgrad ist gesunken.
        if (coefficientsDenominator.getBound() == 1) {
            if (coefficientsNumerator.getBound() > 1) {
                // Sollte eigentlich nie eintreten, da nach Hermite-Reduktion der Grad des Zählers kleiner als der Grad des Nenners sein sollte.
                throw new NotAlgebraicallyIntegrableException();
            }
            Expression integrand = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar).div(SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar)).replaceVariable(transcendentalVar, transcententalElement);

            System.out.println("Integration von " + integrand);

            return GeneralIntegralUtils.integrateIndefinite(new Operator(TypeOperator.integral, new Object[]{integrand, var}));
        }

        // Sei t = transcendentalVar, a(t) = Zähler, b(t) = Nenner.
        // Zunächst: b'(t) bestimmen (Ableitung nach x = var).
        Expression derivativeOfDenominator = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar);
        derivativeOfDenominator = derivativeOfDenominator.replaceVariable(transcendentalVar, transcententalElement);
        derivativeOfDenominator = derivativeOfDenominator.diff(var);
        derivativeOfDenominator = SubstitutionUtilities.substituteExpressionByAnotherExpression(derivativeOfDenominator, transcententalElement, Variable.create(transcendentalVar));
        derivativeOfDenominator = derivativeOfDenominator.simplify(simplifyTypesRischAlgorithm);

        // Koeffizienten von b'(t) bestimmen.
        ExpressionCollection coefficientsDerivativeOfDenominator = SimplifyPolynomialUtils.getPolynomialCoefficients(derivativeOfDenominator, transcendentalVar);

        String resultantVar = SubstitutionUtilities.getSubstitutionVariable(SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar),
                SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar));

        // Resultante bilden.
        // Erstes Argument der Resultante: a(t) - z*b'(t), z = resultantVar;
        ExpressionCollection coefficientsOfFirstArgument = SimplifyPolynomialUtils.subtractPolynomials(coefficientsNumerator,
                SimplifyPolynomialUtils.multiplyPolynomials(new ExpressionCollection(Variable.create(resultantVar)), coefficientsDerivativeOfDenominator));
        // Zweites Argument der Resultante: b(t);

        System.out.println("Koeffizienten der Argumente der Resultante: " + coefficientsOfFirstArgument + ", " + coefficientsDenominator);

        MatrixExpression resultantAsMatrixExpression = SimplifyPolynomialUtils.getResultant(coefficientsOfFirstArgument, coefficientsDenominator);

        if (!(resultantAsMatrixExpression.convertOneTimesOneMatrixToExpression() instanceof Expression)) {
            // Resultante nicht explizit berechenbar.
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression resultant = (Expression) resultantAsMatrixExpression.convertOneTimesOneMatrixToExpression();

        System.out.println("Resultante: " + resultant);

        if (!SimplifyPolynomialUtils.isPolynomial(resultant, resultantVar)) {
            // Sollte eigentlich nie vorkommen.
            throw new NotAlgebraicallyIntegrableException();
        }

        // Prüfen, ob 1. die Nullstellen von resultant von konstant sind und 2. ob ihre Anzahl = deg(resultant) ist.
        resultant = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(resultant, resultantVar);

        System.out.println("Faktorisierte Resultante: " + resultant);

        if (!isPolynomialDecomposedIntoPairwiseDifferentLinearFaktors(resultant, resultantVar, var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Prüfung auf die Anzahl der Nullstellen.
        ExpressionCollection zerosOfResultant;
        try {
            zerosOfResultant = SolveGeneralEquationUtils.solvePolynomialEquation(resultant, resultantVar);
            if (BigInteger.valueOf(zerosOfResultant.getBound()).compareTo(SimplifyPolynomialUtils.getDegreeOfPolynomial(resultant, resultantVar)) < 0) {
                throw new NotAlgebraicallyIntegrableException();
            }
        } catch (NotAlgebraicallySolvableException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        System.out.println("Nullstellen der Resultante: " + zerosOfResultant);

        ExpressionCollection thetas = new ExpressionCollection();
        Expression gcd;
        if (transcententalElement.isFunction(TypeFunction.exp) || transcententalElement.isFunction(TypeFunction.ln)) {
            // theta_i(t) = gcd(a(t) - z_i*b'(t), b(t)), {z_0, ..., z_(k - 1)} sind die Nullstellen der Resultante.
            for (Expression zero : zerosOfResultant) {
                gcd = SimplifyPolynomialUtils.getPolynomialFromCoefficients(SimplifyPolynomialUtils.getGGTOfPolynomials(SimplifyPolynomialUtils.subtractPolynomials(coefficientsNumerator,
                        SimplifyPolynomialUtils.multiplyPolynomials(new ExpressionCollection(zero), coefficientsDerivativeOfDenominator)),
                        coefficientsDenominator), transcendentalVar);
//                gcd = gcd.replaceVariable(transcendentalVar, transcententalElement);
                thetas.add(gcd);
            }
        }

        System.out.println("Thetas: " + thetas);

        // Logarithmischer Fall.
        if (transcententalElement.isFunction(TypeFunction.ln)) {
            // Stammfunktion explizit ausgeben.
            Expression integral = ZERO;
            for (int i = 0; i < thetas.getBound(); i++) {
                thetas.put(i, thetas.get(i).replaceVariable(transcendentalVar, transcententalElement));
            }
            for (int i = 0; i < zerosOfResultant.getBound(); i++) {
                integral = integral.add(zerosOfResultant.get(i).mult(thetas.get(i).ln()));
            }
            System.out.println("Integral: " + integral);
            return integral;
        }

        // Exponentieller Fall.
        if (transcententalElement.isFunction(TypeFunction.exp)) {
            // Stammfunktion explizit ausgeben.
            Expression integral = ZERO;
            for (int i = 0; i < zerosOfResultant.getBound(); i++) {
                integral = integral.add(zerosOfResultant.get(i).mult(SimplifyPolynomialUtils.getDegreeOfPolynomial(thetas.get(i), transcendentalVar)));
            }
            integral = MINUS_ONE.mult(integral).mult(((Function) transcententalElement).getLeft()).replaceVariable(transcendentalVar, transcententalElement);
            for (int i = 0; i < thetas.getBound(); i++) {
                thetas.put(i, thetas.get(i).replaceVariable(transcendentalVar, transcententalElement));
            }
            for (int i = 0; i < zerosOfResultant.getBound(); i++) {
                integral = integral.add(zerosOfResultant.get(i).mult(thetas.get(i).ln()));
            }
            System.out.println("Integral: " + integral);
            return integral;
        }

        throw new NotAlgebraicallyIntegrableException();

    }

    /**
     * Hilfsmethode. Gibt zurück, ob f ein Produkt aus entweder bzw. var
     * konstanten oder bzw. var linearen Polynomen ist und die linearen Polynome
     * bzgl. var dürfen die Variable varNotAllowedToOccur nicht enthalten.
     */
    private static boolean isPolynomialDecomposedIntoPairwiseDifferentLinearFaktors(Expression f, String var, String varNotAllowedToOccur) {
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        for (Expression factor : factors) {
            if (!factor.contains(var)) {
                continue;
            }
            if (!SimplifyPolynomialUtils.isLinearPolynomial(factor, var)) {
                return false;
            }
            if (SimplifyPolynomialUtils.isLinearPolynomial(factor, var) && factor.contains(varNotAllowedToOccur)) {
                return false;
            }
        }
        return true;
    }

}
