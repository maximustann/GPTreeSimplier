package abstractexpressions.expression.classes;

import abstractexpressions.expression.basic.ExpressionCollection;
import enums.TypeExpansion;
import exceptions.EvaluationException;
import abstractexpressions.expression.basic.SimplifyExpLogUtils;
import abstractexpressions.expression.basic.SimplifyFunctionUtils;
import abstractexpressions.expression.basic.SimplifyFunctionalRelationsUtils;
import abstractexpressions.expression.basic.SimplifyTrigonometryUtils;
import abstractexpressions.expression.basic.SimplifyUtilities;
import enums.TypeFractionSimplification;
import enums.TypeSimplify;
import java.math.BigDecimal;
import java.util.HashSet;
import lang.translator.Translator;

public class Function extends Expression {

    private final Expression left;
    private final TypeFunction type;

    private static final HashSet<TypeSimplify> simplifyTypesExpandAndCollectIfShorter = getSimplifyTypesExpandAndCollectIfShorter();

    private static HashSet<TypeSimplify> getSimplifyTypesExpandAndCollectIfShorter() {
        /*
         Als Vereinfachungstyp darf NICHT 
         simplify_expand_and_collect_equivalents_if_shorter verwendet werden.
         */
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        return simplifyTypes;
    }

    public Function(Expression left, TypeFunction type) {
        this.left = left;
        this.type = type;
    }

    public TypeFunction getType() {
        return this.type;
    }

    public Expression getLeft() {
        return this.left;
    }

    public String getName() {
        return this.type.toString();
    }

    @Override
    public Expression copy() {
        return new Function(this.left, this.type);
    }

    @Override
    public double evaluate() throws EvaluationException {
        double argumentValue = left.evaluate();
        switch (type) {
            case id:
                if (!Double.isNaN(argumentValue) && !Double.isInfinite(argumentValue)) {
                    return argumentValue;
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case abs:
                if (!Double.isNaN(argumentValue) && !Double.isInfinite(argumentValue)) {
                    return Math.abs(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case sgn:
                if (!Double.isNaN(argumentValue) && !Double.isInfinite(argumentValue)) {
                    if (argumentValue > 0) {
                        return 1;
                    }
                    if (argumentValue == 0) {
                        return 0;
                    }
                    return -1;
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case exp:
                if (!Double.isNaN(Math.exp(argumentValue)) && !Double.isInfinite(Math.exp(argumentValue))) {
                    return Math.exp(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case lg:
                if (!Double.isNaN(Math.log10(argumentValue)) && !Double.isInfinite(Math.log10(argumentValue))) {
                    return Math.log10(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case ln:
                if (!Double.isNaN(Math.log(argumentValue)) && !Double.isInfinite(Math.log(argumentValue))) {
                    return Math.log(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case sin:
                if (!Double.isNaN(Math.sin(argumentValue)) && !Double.isInfinite(Math.sin(argumentValue))) {
                    return Math.sin(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case cos:
                if (!Double.isNaN(Math.cos(argumentValue)) && !Double.isInfinite(Math.cos(argumentValue))) {
                    return Math.cos(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case tan:
                if (!Double.isNaN(Math.tan(argumentValue)) && !Double.isInfinite(Math.tan(argumentValue))) {
                    return Math.tan(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case cot:
                if (!Double.isNaN(1 / Math.tan(argumentValue)) && !Double.isInfinite(1 / Math.tan(argumentValue))) {
                    return 1 / Math.tan(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case sec:
                if (!Double.isNaN(1 / Math.cos(argumentValue)) && !Double.isInfinite(1 / Math.cos(argumentValue))) {
                    return 1 / Math.cos(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case cosec:
                if (!Double.isNaN(1 / Math.sin(argumentValue)) && !Double.isInfinite(1 / Math.sin(argumentValue))) {
                    return 1 / Math.sin(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case sinh:
                if (!Double.isNaN(Math.sinh(argumentValue)) && !Double.isInfinite(Math.sinh(argumentValue))) {
                    return Math.sinh(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case cosh:
                if (!Double.isNaN(Math.cosh(argumentValue)) && !Double.isInfinite(Math.cosh(argumentValue))) {
                    return Math.cosh(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case tanh:
                if (!Double.isNaN(Math.tanh(argumentValue)) && !Double.isInfinite(Math.tanh(argumentValue))) {
                    return Math.tanh(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case coth:
                if (!Double.isNaN(1 / Math.tanh(argumentValue)) && !Double.isInfinite(1 / Math.tanh(argumentValue))) {
                    return 1 / Math.tanh(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case sech:
                if (!Double.isNaN(1 / Math.cosh(argumentValue)) && !Double.isInfinite(1 / Math.cosh(argumentValue))) {
                    return 1 / Math.cosh(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case cosech:
                if (!Double.isNaN(1 / Math.sinh(argumentValue)) && !Double.isInfinite(1 / Math.sinh(argumentValue))) {
                    return 1 / Math.sinh(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arcsin:
                if (!Double.isNaN(Math.asin(argumentValue)) && !Double.isInfinite(Math.asin(argumentValue))) {
                    return Math.asin(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arccos:
                if (!Double.isNaN(Math.acos(argumentValue)) && !Double.isInfinite(Math.acos(argumentValue))) {
                    return Math.acos(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arctan:
                if (!Double.isNaN(Math.atan(argumentValue)) && !Double.isInfinite(Math.atan(argumentValue))) {
                    return Math.atan(argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arccot:
                if (!Double.isNaN(Math.atan(1 / argumentValue)) && !Double.isInfinite(Math.atan(1 / argumentValue))) {
                    return Math.atan(1 / argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arcsec:
                if (!Double.isNaN(Math.acos(1 / argumentValue)) && !Double.isInfinite(Math.acos(1 / argumentValue))) {
                    return Math.acos(1 / argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arccosec:
                if (!Double.isNaN(Math.asin(1 / argumentValue)) && !Double.isInfinite(Math.asin(1 / argumentValue))) {
                    return Math.asin(1 / argumentValue);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arsinh:
                if (!Double.isNaN(Math.log(argumentValue + Math.sqrt(Math.pow(argumentValue, 2) + 1))) && !Double.isInfinite(Math.log(argumentValue + Math.sqrt(Math.pow(argumentValue, 2) + 1)))) {
                    return Math.log(argumentValue + Math.sqrt(Math.pow(argumentValue, 2) + 1));
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arcosh:
                if (!Double.isNaN(Math.log(argumentValue + Math.sqrt(Math.pow(argumentValue, 2) - 1))) && !Double.isInfinite(Math.log(argumentValue + Math.sqrt(Math.pow(argumentValue, 2) - 1)))) {
                    return Math.log(argumentValue + Math.sqrt(Math.pow(argumentValue, 2) - 1));
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case artanh:
                if (!Double.isNaN(Math.log((1 + argumentValue) / (1 - argumentValue)) / 2) && !Double.isInfinite(Math.log((1 + argumentValue) / (1 - argumentValue)) / 2)) {
                    return Math.log((1 + argumentValue) / (1 - argumentValue)) / 2;
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arcoth:
                if (!Double.isNaN(Math.log((1 + argumentValue) / (argumentValue - 1)) / 2) && !Double.isInfinite(Math.log((1 + argumentValue) / (argumentValue - 1)) / 2)) {
                    return Math.log((1 + argumentValue) / (argumentValue - 1)) / 2;
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arsech:
                if (!Double.isNaN(Math.log(1 / argumentValue + Math.sqrt(Math.pow(1 / argumentValue, 2) - 1))) && !Double.isInfinite(Math.log(1 / argumentValue + Math.sqrt(Math.pow(1 / argumentValue, 2) - 1)))) {
                    return Math.log(1 / argumentValue + Math.sqrt(Math.pow(1 / argumentValue, 2) - 1));
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case arcosech:
                if (!Double.isNaN(Math.log(1 / argumentValue + Math.sqrt(Math.pow(1 / argumentValue, 2) + 1))) && !Double.isInfinite(Math.log(1 / argumentValue + Math.sqrt(Math.pow(1 / argumentValue, 2) + 1)))) {
                    return Math.log(1 / argumentValue + Math.sqrt(Math.pow(1 / argumentValue, 2) + 1));
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            case sqrt:
                if (!Double.isNaN(Math.pow(argumentValue, 0.5)) && !Double.isInfinite(Math.pow(argumentValue, 0.5))) {
                    return Math.pow(argumentValue, 0.5);
                } else {
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Function_UNDEFINED_VALUE"));
                }
            default:
                return 0;
        }
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        this.left.addContainedVars(vars);
    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
        this.left.addContainedIndeterminates(vars);
    }

    @Override
    public boolean contains(String var) {
        return this.left.contains(var);
    }

    @Override
    public boolean containsApproximates() {
        return this.left.containsApproximates();
    }

    @Override
    public boolean containsFunction() {
        return true;
    }

    @Override
    public boolean containsExponentialFunction() {
        if ((this.type.equals(TypeFunction.exp) || this.type.equals(TypeFunction.sinh)
                || this.type.equals(TypeFunction.cosh) || this.type.equals(TypeFunction.tanh)
                || this.type.equals(TypeFunction.coth) || this.type.equals(TypeFunction.sech)
                || this.type.equals(TypeFunction.cosech)) && !this.left.isConstant()) {
            // Im diesem Fall handelt es sich (eventuell) um Exponentialfunktionen.
            return true;
        }
        return this.left.containsExponentialFunction();
    }

    @Override
    public boolean containsTrigonometricalFunction() {
        return (this.type.equals(TypeFunction.sin) || this.type.equals(TypeFunction.cos)
                || this.type.equals(TypeFunction.tan) || this.type.equals(TypeFunction.cot)
                || this.type.equals(TypeFunction.sec) || this.type.equals(TypeFunction.cosec)) && !this.left.isConstant()
                || this.left.containsTrigonometricalFunction();
    }

    @Override
    public boolean containsIndefiniteIntegral() {
        return this.left.containsIndefiniteIntegral();
    }

    @Override
    public boolean containsOperator() {
        return this.left.containsOperator();
    }

    @Override
    public boolean containsAlgebraicOperation() {
        /*
         Ausdrücke wie exp(ln(x)/2) (= x^(1/2)) zählen hier noch nicht zu algebraischen 
         Operationen. Sobald diese aber entsprechend vereinfacht werden, dagegen schon!
         Sinn dahinter: algebraische Umformungen müssen erst vorgenommen werden, sobald
         der Ausdruck auf dieser Form gebracht wird, nicht vorher.
         */
        return this.left.containsAlgebraicOperation();
    }

    @Override
    public boolean containsOperator(TypeOperator type) {
        return this.left.containsOperator(type);
    }

    @Override
    public Expression turnToApproximate() {
        return new Function(this.left.turnToApproximate(), this.type);
    }

    @Override
    public Expression turnToPrecise() {
        return new Function(this.left.turnToPrecise(), this.type);
    }

    @Override
    public Expression replaceVariable(String var, Expression expr) {
        return new Function(this.left.replaceVariable(var, expr), this.type);
    }

    @Override
    public Expression replaceSelfDefinedFunctionsByPredefinedFunctions() {
        return new Function(this.left.replaceSelfDefinedFunctionsByPredefinedFunctions(), this.type);
    }

    @Override
    public Expression diff(String var) throws EvaluationException {

        if (!this.contains(var) && !this.containsAtLeastOne(this.getContainedVariablesDependingOnGivenVariable(var))) {
            return Expression.ZERO;
        }

        if (this.type == TypeFunction.id) {
            return this.left.diff(var);
        } else if (this.type == TypeFunction.abs) {
            return Expression.ONE.div(new Function(this.left, TypeFunction.sgn)).mult(this.left.diff(var));
        } else if (this.type == TypeFunction.sgn) {
            return Expression.ZERO;
        } else if (this.type == TypeFunction.exp) {
            return this.mult(this.left.diff(var));
        } else if (this.type == TypeFunction.lg) {
            return this.left.diff(var).div(new Function(new Constant(BigDecimal.TEN), TypeFunction.ln).mult(this.left));
        } else if (this.type == TypeFunction.ln) {
            return this.left.diff(var).div(this.left);
        } else if (this.type == TypeFunction.sin) {
            return (new Function(this.left, TypeFunction.cos)).mult(this.left.diff(var));
        } else if (this.type == TypeFunction.cos) {
            return (Expression.MINUS_ONE).mult(new Function(this.left, TypeFunction.sin).mult(this.left.diff(var)));
        } else if (this.type == TypeFunction.tan) {
            return new Function(this.left, TypeFunction.sec).pow(2).mult(this.left.diff(var));
        } else if (this.type == TypeFunction.cot) {
            return Expression.MINUS_ONE.mult(new Function(this.left, TypeFunction.cosec).pow(2).mult(this.left.diff(var)));
        } else if (this.type == TypeFunction.sec) {
            return this.mult(new Function(this.left, TypeFunction.tan)).mult(this.left.diff(var));
        } else if (this.type == TypeFunction.cosec) {
            return Expression.MINUS_ONE.mult(this.mult(new Function(this.left, TypeFunction.cot)).mult(this.left.diff(var)));
        } else if (this.type == TypeFunction.sinh) {
            return (new Function(this.left, TypeFunction.cosh)).mult(this.left.diff(var));
        } else if (this.type == TypeFunction.cosh) {
            return (new Function(this.left, TypeFunction.sinh)).mult(this.left.diff(var));
        } else if (this.type == TypeFunction.tanh) {
            return new Function(this.left, TypeFunction.sech).pow(2).mult(this.left.diff(var));
        } else if (this.type == TypeFunction.coth) {
            return MINUS_ONE.mult(new Function(this.left, TypeFunction.cosech).pow(2).mult(this.left.diff(var)));
        } else if (this.type == TypeFunction.sech) {
            return MINUS_ONE.mult(this.mult(new Function(this.left, TypeFunction.tanh).mult(this.left.diff(var))));
        } else if (this.type == TypeFunction.cosech) {
            return MINUS_ONE.mult(this.mult(new Function(this.left, TypeFunction.coth).mult(this.left.diff(var))));
        } else if (this.type == TypeFunction.arcsin) {
            return this.left.diff(var).div(ONE.sub(this.left.pow(2)).pow(1, 2));
        } else if (this.type == TypeFunction.arccos) {
            return MINUS_ONE.mult(this.left.diff(var)).div(ONE.sub(this.left.pow(2)).pow(1, 2));
        } else if (this.type == TypeFunction.arctan) {
            return this.left.diff(var).div(ONE.add(this.left.pow(2)));
        } else if (this.type == TypeFunction.arccot) {
            return MINUS_ONE.mult(this.left.diff(var)).div(ONE.add(this.left.pow(2)));
        } else if (this.type == TypeFunction.arcsec) {
            return this.left.diff(var).mult(this.left.pow(2).sub(1).pow(1, 2)).div(this.left);
        } else if (this.type == TypeFunction.arccosec) {
            return MINUS_ONE.mult(this.left.mult(this.left.diff(var))).div(this.left.pow(2).sub(1).pow(1, 2));
        } else if (this.type == TypeFunction.arsinh) {
            return this.left.diff(var).div(ONE.add(this.left.pow(2)).pow(1, 2));
        } else if (this.type == TypeFunction.arcosh) {
            return this.left.diff(var).div(this.left.pow(2).sub(1).pow(1, 2));
        } else if (this.type == TypeFunction.artanh) {
            return this.left.diff(var).div(ONE.sub(this.left.pow(2)));
        } else if (this.type == TypeFunction.arcoth) {
            return this.left.diff(var).div(ONE.sub(this.left.pow(2)));
        } else if (this.type == TypeFunction.arsech) {
            return MINUS_ONE.mult(this.left.diff(var)).div(this.left.mult(ONE.sub(this.left.pow(2)).pow(1, 2)));
        } else {
            // Hier ist type == arcosech.
            return MINUS_ONE.mult(this.left.diff(var)).div(this.left.mult(ONE.add(this.left.pow(2)).pow(1, 2)));
        }

    }

    @Override
    public String toString() {

        if (this.type == TypeFunction.id) {
            return this.left.toString();
        } else if (this.type == TypeFunction.abs) {
            return "|" + this.left.toString() + "|";
        }
        return this.type.toString() + "(" + this.left.toString() + ")";

    }

    @Override
    public String expressionToLatex() {

        if (this.type == TypeFunction.id) {
            return this.left.expressionToLatex();
        } else if (this.type == TypeFunction.abs) {
            return "\\|" + this.left.expressionToLatex() + "\\|";
        } else if (this.type == TypeFunction.exp) {
            return "\\exp\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.ln) {
            return "\\ln\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.sin) {
            return "\\sin\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.cos) {
            return "\\cos\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.tan) {
            return "\\tan\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.cot) {
            return "\\cot\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.sec) {
            return "\\sec\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.cosec) {
            return "\\csc\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.sinh) {
            return "\\sinh\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.cosh) {
            return "\\cosh\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.tanh) {
            return "\\tanh\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.coth) {
            return "\\coth\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.sech) {
            return "\\text{sech}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.cosech) {
            return "\\text{csch}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arcsin) {
            return "\\arcsin\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arccos) {
            return "\\arccos\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arctan) {
            return "\\arctan\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arccot) {
            return "\\text{arccot}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arcsec) {
            return "\\text{arcsec}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arccosec) {
            return "\\text{arccsc}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arsinh) {
            return "\\text{arsinh}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arcosh) {
            return "\\text{arcosh}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.artanh) {
            return "\\text{artanh}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arcoth) {
            return "\\text{arcoth}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else if (this.type == TypeFunction.arsech) {
            return "\\text{arsech}\\left(" + this.left.expressionToLatex() + "\\right)";
        } else {
            return "\\text{arcsch}\\left(" + this.left.expressionToLatex() + "\\right)";
        }
    }

    @Override
    public boolean isConstant() {
        return this.getLeft().isConstant();
    }

    @Override
    public boolean isNonNegative() {

        if (!this.left.isConstant()) {
            return false;
        }

        try {
            return this.evaluate() >= 0;
        } catch (EvaluationException e) {
        }

        if (this.type.equals(TypeFunction.abs)) {
            return true;
        } else if (this.type.equals(TypeFunction.sgn)) {
            return this.left.isNonNegative();
        } else if (this.type.equals(TypeFunction.exp)) {
            return true;
        } else if (this.type.equals(TypeFunction.ln)) {
            try {
                return this.left.sub(Expression.ONE).simplify().isNonNegative();
            } catch (EvaluationException e) {
                return false;
            }
        } else if (this.type.equals(TypeFunction.sinh) || this.type.equals(TypeFunction.tanh)
                || this.type.equals(TypeFunction.coth) || this.type.equals(TypeFunction.cosech)
                || this.type.equals(TypeFunction.arcsin) || this.type.equals(TypeFunction.arctan)
                || this.type.equals(TypeFunction.arccot) || this.type.equals(TypeFunction.artanh)
                || this.type.equals(TypeFunction.arcoth) || this.type.equals(TypeFunction.sqrt)) {
            return this.left.isNonNegative();
        } else if (this.type.equals(TypeFunction.cosh) || this.type.equals(TypeFunction.sech)) {
            return true;
        } else if (this.type.equals(TypeFunction.sqrt)) {
            return this.left.isNonNegative();
        }
        return false;

    }

    @Override
    public boolean isNonPositive() {

        if (!this.left.isConstant()) {
            return false;
        }

        try {
            return this.evaluate() <= 0;
        } catch (EvaluationException e) {
        }

        if (this.type.equals(TypeFunction.sgn)) {
            return this.left.isNonPositive();
        } else if (this.type.equals(TypeFunction.ln)) {
            try {
                return this.left.sub(Expression.ONE).simplify().isNonPositive();
            } catch (EvaluationException e) {
                return false;
            }
        } else if (this.type.equals(TypeFunction.sinh) || this.type.equals(TypeFunction.tanh)
                || this.type.equals(TypeFunction.coth) || this.type.equals(TypeFunction.cosech)
                || this.type.equals(TypeFunction.arcsin) || this.type.equals(TypeFunction.arctan)
                || this.type.equals(TypeFunction.arccot) || this.type.equals(TypeFunction.artanh)
                || this.type.equals(TypeFunction.arcoth) || this.type.equals(TypeFunction.sqrt)) {
            return this.left.isNonPositive();
        }
        return false;

    }

    @Override
    public boolean isAlwaysNonNegative() {

        if (this.isNonNegative()) {
            return true;
        }
        if (this.type.equals(TypeFunction.abs) || this.type.equals(TypeFunction.exp)
                || this.type.equals(TypeFunction.cosh) || this.type.equals(TypeFunction.sech)
                || this.type.equals(TypeFunction.arccos) || this.type.equals(TypeFunction.arcsec)
                || this.type.equals(TypeFunction.arcosh) || this.type.equals(TypeFunction.arsech)
                || this.type.equals(TypeFunction.sqrt)) {
            return true;
        }
        if (this.type.equals(TypeFunction.sgn) || this.type.equals(TypeFunction.sinh)
                || this.type.equals(TypeFunction.tanh) || this.type.equals(TypeFunction.coth)
                || this.type.equals(TypeFunction.arcsin) || this.type.equals(TypeFunction.arctan)
                || this.type.equals(TypeFunction.arccot) || this.type.equals(TypeFunction.arsinh)
                || this.type.equals(TypeFunction.artanh) || this.type.equals(TypeFunction.arcoth)
                || this.type.equals(TypeFunction.arcosech)) {
            return this.left.isAlwaysNonNegative();
        }
        return false;

    }

    @Override
    public boolean isAlwaysPositive() {

        if (this.isNonNegative() && !this.equals(ZERO)) {
            return true;
        }
        if (this.type.equals(TypeFunction.exp)
                || this.type.equals(TypeFunction.cosh) || this.type.equals(TypeFunction.sech)) {
            return true;
        }
        if (this.type.equals(TypeFunction.abs) || this.type.equals(TypeFunction.sgn)
                || this.type.equals(TypeFunction.sinh) || this.type.equals(TypeFunction.tanh)
                || this.type.equals(TypeFunction.coth) || this.type.equals(TypeFunction.arcsin)
                || this.type.equals(TypeFunction.arctan) || this.type.equals(TypeFunction.arccot)
                || this.type.equals(TypeFunction.arsinh) || this.type.equals(TypeFunction.artanh)
                || this.type.equals(TypeFunction.arcoth) || this.type.equals(TypeFunction.arcosech)) {
            return this.left.isAlwaysPositive();
        }
        return false;

    }

    @Override
    public boolean isAlwaysNonPositive() {

        if (this.isNonPositive()) {
            return true;
        }
        if (this.type.equals(TypeFunction.sgn) || this.type.equals(TypeFunction.sinh)
                || this.type.equals(TypeFunction.tanh) || this.type.equals(TypeFunction.coth)
                || this.type.equals(TypeFunction.arcsin) || this.type.equals(TypeFunction.arctan)
                || this.type.equals(TypeFunction.arccot) || this.type.equals(TypeFunction.arsinh)
                || this.type.equals(TypeFunction.artanh) || this.type.equals(TypeFunction.arcoth)
                || this.type.equals(TypeFunction.arcosech)) {
            return this.left.isAlwaysNonPositive();
        }
        return false;

    }

    @Override
    public boolean isAlwaysNegative() {

        if (this.isNonPositive() && !this.equals(ZERO)) {
            return true;
        }
        if (this.type.equals(TypeFunction.abs) || this.type.equals(TypeFunction.sgn)
                || this.type.equals(TypeFunction.sinh) || this.type.equals(TypeFunction.tanh)
                || this.type.equals(TypeFunction.coth) || this.type.equals(TypeFunction.arcsin)
                || this.type.equals(TypeFunction.arctan) || this.type.equals(TypeFunction.arccot)
                || this.type.equals(TypeFunction.arsinh) || this.type.equals(TypeFunction.artanh)
                || this.type.equals(TypeFunction.arcoth) || this.type.equals(TypeFunction.arcosech)) {
            return this.left.isAlwaysNegative();
        }
        return false;

    }

    @Override
    public boolean equals(Expression expr) {
        return expr instanceof Function && this.type.equals(((Function) expr).getType())
                && this.getLeft().equals(((Function) expr).getLeft());
    }

    @Override
    public boolean equivalent(Expression expr) {

        // Sonderfall: Bei geraden Funktionen sind die Ausdrücke äquivalent, wenn sich die Argumente um ein Vorzeichen unterscheiden.
        if (expr instanceof Function && this.type.equals(((Function) expr).getType()) && this.type.isEvenFunction()) {

            ExpressionCollection summandsLeftoFThis = SimplifyUtilities.getSummandsLeftInExpression(this.left);
            ExpressionCollection summandsRightOfThis = SimplifyUtilities.getSummandsRightInExpression(this.left);
            ExpressionCollection summandsLeftOfExpr = SimplifyUtilities.getSummandsLeftInExpression(((Function) expr).getLeft());
            ExpressionCollection summandsRightOfExpr = SimplifyUtilities.getSummandsRightInExpression(((Function) expr).getLeft());

            ExpressionCollection summandsLeftOfThisWithSign = new ExpressionCollection();
            ExpressionCollection summandsRightOfThisWithSign = new ExpressionCollection();
            ExpressionCollection summandsLeftOfExprWithSign = new ExpressionCollection();
            ExpressionCollection summandsRightOfExprWithSign = new ExpressionCollection();

            try {
                for (int i = 0; i < summandsLeftoFThis.getBound(); i++) {
                    if (summandsLeftoFThis.get(i).hasPositiveSign()) {
                        summandsLeftOfThisWithSign.add(summandsLeftoFThis.get(i));
                    } else {
                        summandsRightOfThisWithSign.add(MINUS_ONE.mult(summandsLeftoFThis.get(i)).orderSumsAndProducts());
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

        return expr instanceof Function && this.getType().equals(((Function) expr).getType())
                && this.getLeft().equivalent(((Function) expr).getLeft());

    }

    @Override
    public boolean antiEquivalent(Expression expr) {
        return expr instanceof Function && ((Function) expr).type.equals(this.type)
                && this.type.isOddFunction() && this.left.antiEquivalent(((Function) expr).left);
    }

    @Override
    public boolean hasPositiveSign() {
        return true;
    }

    @Override
    public int getLength() {
        if (((Function) this).getLeft().getLength() == 1) {
            return 1;
        }
        return ((Function) this).getLeft().getLength() + 1;
    }

    @Override
    public int getMaximalNumberOfSummandsInExpansion() {
        return 1;
    }

    @Override
    public Expression simplifyBasic() throws EvaluationException {

        //Zunächst linken Teil (Argument in der Funktion) vereinfachen.
        Expression argumentSimplified = this.getLeft().simplifyBasic();
        Function function = new Function(argumentSimplified, this.type);

        Expression functionSimplified;

        // Konstante Funktionswerte im Approximationsmodus approximieren.
        functionSimplified = SimplifyFunctionUtils.approxConstantExpression(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // Prüfen, ob gewisse Funktionswerte definiert sind.
        functionSimplified = SimplifyFunctionUtils.checkIfFunctionValueDefined(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // id(x) zu x vereinfachen
        functionSimplified = SimplifyFunctionUtils.simplifyIdentity(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // Kompositionen bestimmter Funktionen vereinfachen.
        functionSimplified = SimplifyFunctionUtils.simplifyCompositionOfTwoFunctions(this.type, argumentSimplified);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // Logarithmen und Exponentialfunktionen behandeln.
        functionSimplified = SimplifyExpLogUtils.reduceExpOfSumsOfLn(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyExpLogUtils.reduceExpOfDifferencesOfLn(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyExpLogUtils.reduceLnOfProductsOfExp(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyExpLogUtils.reduceLnOfQuotientsOfExp(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyExpLogUtils.reduceLgOfProductsOfPowersOf10(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyExpLogUtils.reduceLgOfQuotientsOfPowersOf10(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // Vereinfachungen von abs() und sgn().
        /*
         Versucht, in einfachen Fällen den Betrag genau anzugeben (d.h. zu
         entscheiden, ob |x| = x oder ob |x| = -x gilt).
         */
        functionSimplified = SimplifyFunctionUtils.computeAbsIfExpressionIsConstant(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // Falls das Argument immer negativ oder immer nichtnegativ ist, dann einfach das Argument zurückgeben.
        functionSimplified = SimplifyFunctionUtils.computeAbsIfExpressionIsAlwaysNonNegative(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyFunctionUtils.computeAbsIfExpressionIsAlwaysNonPositive(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        /*
         Versucht, in einfachen Fällen das Signum genau anzugeben (d.h. zu
         entscheiden, ob sgn(x) = 1, 0 oder -1 gilt).
         */
        functionSimplified = SimplifyFunctionUtils.computeSgnIfExpressionIsConstant(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyFunctionUtils.computeSgnIfExpressionIsAlwaysPositive(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyFunctionUtils.computeSgnIfExpressionIsAlwaysNegative(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // Gerade Potenzen aus Produkten in abs() herausziehen
        functionSimplified = SimplifyFunctionalRelationsUtils.takeEvenPowersOutOfProductsInAbs(function);
        if (!functionSimplified.equals(this)) {
            return functionSimplified;
        }

        // Gerade Potenzen aus Quotienten in abs() herausziehen
        functionSimplified = SimplifyFunctionalRelationsUtils.takeEvenPowersOutOfQuotientsInAbs(function);
        if (!functionSimplified.equals(this)) {
            return functionSimplified;
        }

        // Konstanten aus Produkten in abs() herausziehen.
        functionSimplified = SimplifyFunctionalRelationsUtils.takeConstantsOutOfProductsInAbs(function);
        if (!functionSimplified.equals(this)) {
            return functionSimplified;
        }

        // Konstanten aus Quotienten in abs() herausziehen.
        functionSimplified = SimplifyFunctionalRelationsUtils.takeConstantsOutOfQuotientsInAbs(function);
        if (!functionSimplified.equals(this)) {
            return functionSimplified;
        }

        // Gerade Potenzen in Produkten in sgn() beseitigen
        functionSimplified = SimplifyFunctionalRelationsUtils.removeSpecialPowersInProductsInSgn(function);
        if (!functionSimplified.equals(this)) {
            return functionSimplified;
        }

        // Gerade Potenzen in Quotienten in sgn() beseitigen
        functionSimplified = SimplifyFunctionalRelationsUtils.removeSpecialPowersInQuotientsInSgn(function);
        if (!functionSimplified.equals(this)) {
            return functionSimplified;
        }

        // Konstanten aus Produkten in sgn() beseitigen.
        functionSimplified = SimplifyFunctionalRelationsUtils.removeConstantsInProductsInSgn(function);
        if (!functionSimplified.equals(this)) {
            return functionSimplified;
        }

        // Konstanten aus Quotienten in sgn() beseitigen.
        functionSimplified = SimplifyFunctionalRelationsUtils.removeConstantsInQuotientsInSgn(function);
        if (!functionSimplified.equals(this)) {
            return functionSimplified;
        }

        /*
         Anwendung der Funktionalgleichung f(-x) = -f(x) bzw. f(-x) = f(x) für
         bestimmte Funktionen f. Beispielsweise wird cos(-x) = cos(x) etc.
         */
        functionSimplified = SimplifyFunctionUtils.simplifySymetricAndAntisymmetricFunctions(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        /*
         Vereinfachung bestimmter Funktionswerte f(0). Beispielsweise wird
         sin(0) = 0, cos(0) = 1, exp(0) = 1 etc.
         */
        functionSimplified = SimplifyFunctionUtils.simplifySpecialValuesOfFunctions(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // Subtraktion ganzer Vielfacher von Pi in Argumenten von trigonometrischen Funtionen.
        functionSimplified = SimplifyTrigonometryUtils.reduceSineCosineSecansCosecansIfArgumentContainsSummandOfMultipleOfPi(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        /* Identitäten von der Bauart sin(x+pi/2) = cos(x), sin(x+3*pi/2) = -cos(x) etc.
         Funktionstypen hierfür: sin, cos, sec, cosec.
         */
        functionSimplified = SimplifyTrigonometryUtils.interchangeSineWithCosineAndSecansWithCosecansIfArgumentContainsSummandOfMultipleOfPi(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        functionSimplified = SimplifyTrigonometryUtils.reduceTangentCotangentIfArgumentContainsSummandOfMultipleOfPi(function);
        if (!functionSimplified.equals(function)) {
            return functionSimplified;
        }

        // Vereinfachung von speziellen konstanten trigonometrischen Ausdrücken.
        if (function.getType().equals(TypeFunction.sin)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceSine(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.cos)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceCosine(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.tan)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceTangent(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.cot)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceCotangent(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.sec)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceSecans(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.cosec)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceCosecans(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.arcsin)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceArcsine(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.arccos)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceArccosine(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.arctan)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceArctangent(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.arccot)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceArccotangent(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.arcsec)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceArcsecans(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }
        if (function.getType().equals(TypeFunction.arccosec)) {
            functionSimplified = SimplifyTrigonometryUtils.reduceArccosecans(function);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }
        }

        return function;

    }

    @Override
    public Expression simplifyByInsertingDefinedVars() throws EvaluationException {
        return new Function(this.left.simplifyByInsertingDefinedVars(), this.type);
    }

    @Override
    public Expression simplifyExpandRationalFactors() throws EvaluationException {
        return new Function(this.left.simplifyExpandRationalFactors(), this.type);
    }

    @Override
    public Expression simplifyExpand(TypeExpansion type) throws EvaluationException {
        return new Function(this.left.simplifyExpand(type), this.type);
    }

    @Override
    public Expression simplifyBringExpressionToCommonDenominator(TypeFractionSimplification type) throws EvaluationException {
        return new Function(this.left.simplifyBringExpressionToCommonDenominator(type), this.type);
    }

    @Override
    public Expression simplifyReduceDifferencesAndQuotientsAdvanced() throws EvaluationException {
        return new Function(this.left.simplifyReduceDifferencesAndQuotientsAdvanced(), this.type);
    }

    @Override
    public Expression orderSumsAndProducts() throws EvaluationException {
        return new Function(this.left.orderSumsAndProducts(), this.type);
    }

    @Override
    public Expression orderDifferencesAndQuotients() throws EvaluationException {
        return new Function(this.getLeft().orderDifferencesAndQuotients(), this.getType());
    }

    @Override
    public Expression simplifyCollectProducts() throws EvaluationException {
        return new Function(this.getLeft().simplifyCollectProducts(), this.getType());
    }

    @Override
    public Expression simplifyFactorize() throws EvaluationException {
        return new Function(this.getLeft().simplifyFactorize(), this.getType());
    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInSums() throws EvaluationException {
        return new Function(this.getLeft().simplifyFactorizeAllButRationalsInSums(), this.getType());
    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInDifferences() throws EvaluationException {
        return new Function(this.getLeft().simplifyFactorizeAllButRationalsInDifferences(), this.getType());
    }

    @Override
    public Expression simplifyFactorizeAllButRationals() throws EvaluationException {
        return new Function(this.getLeft().simplifyFactorizeAllButRationals(), this.getType());
    }

    @Override
    public Expression simplifyReduceQuotients() throws EvaluationException {
        return new Function(this.getLeft().simplifyReduceQuotients(), this.getType());
    }

    @Override
    public Expression simplifyFunctionalRelations() throws EvaluationException {
        Function functionSimplified = new Function(this.left.simplifyFunctionalRelations(), this.type);
        if (functionSimplified.type.equals(TypeFunction.abs)) {
            return SimplifyFunctionalRelationsUtils.reduceAbsOfQuotientIfNumeratorHasFixedSign(functionSimplified);
        }
        if (functionSimplified.type.equals(TypeFunction.sgn)) {
            return SimplifyFunctionalRelationsUtils.reduceSgnOfQuotientIfNumeratorHasFixedSign(functionSimplified);
        }
        return functionSimplified;
    }

    @Override
    public Expression simplifyExpandAndCollectEquivalentsIfShorter() throws EvaluationException {

        if (this.type.isTrigonometric()) {

        }

        return new Function(this.left.simplifyExpandAndCollectEquivalentsIfShorter(), this.type);

    }

    @Override
    public Expression simplifyCollectLogarithms() throws EvaluationException {
        return new Function(this.left.simplifyCollectLogarithms(), this.type);
    }

    @Override
    public Expression simplifyExpandLogarithms() throws EvaluationException {

        // Zunächst linken Teil (Argument in der Funktion) vereinfachen.
        Function function = new Function(this.left.simplifyExpandLogarithms(), this.type);

        // Vereinfacht lg(x^y) zu y*lg(x) und lg(x*/y) = lg(x) +- lg(y), analog mit ln.
        if (this.type.equals(TypeFunction.lg) || this.type.equals(TypeFunction.ln)) {

            // Vereinfacht ln(1/x) zu -ln(x) und lg(1/x) zu -lg(x).
            Expression functionSimplified = SimplifyExpLogUtils.reduceLogarithmOfReciprocal(function.getLeft(), this.type);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }

            functionSimplified = SimplifyExpLogUtils.expandLogarithms(function.getLeft(), this.type);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }

            functionSimplified = SimplifyExpLogUtils.expandLogarithmOfProduct(function.getLeft(), this.type);
            if (!functionSimplified.equals(function)) {
                return functionSimplified;
            }

        }

        return function;

    }

    @Override
    public Expression simplifyPullApartPowers() throws EvaluationException {
        return new Function(this.left.simplifyPullApartPowers(), this.type);
    }

    @Override
    public Expression simplifyMultiplyExponents() throws EvaluationException {
        return new Function(this.left.simplifyMultiplyExponents(), this.type);
    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException {

        Function function = new Function(this.left.simplifyReplaceExponentialFunctionsByDefinitions(), this.type);

        // Dekadischer Logarithmus.
        if (function.getType().equals(TypeFunction.lg)) {
            return new Function(function.getLeft(), TypeFunction.ln).div(new Function(new Constant(10), TypeFunction.ln));
        }

        // Hyperbolische Funktionen.
        if (function.getType().equals(TypeFunction.cosh)) {
            return new Function(((Function) function).getLeft(), TypeFunction.exp).add(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)).div(2);
        }

        if (function.getType().equals(TypeFunction.sinh)) {
            return new Function(((Function) function).getLeft(), TypeFunction.exp).sub(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)).div(2);
        }

        if (function.getType().equals(TypeFunction.tanh)) {
            return new Function(((Function) function).getLeft(), TypeFunction.exp).sub(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)).div(new Function(((Function) function).getLeft(), TypeFunction.exp).add(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)));
        }

        if (function.getType().equals(TypeFunction.coth)) {
            return new Function(((Function) function).getLeft(), TypeFunction.exp).add(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)).div(new Function(((Function) function).getLeft(), TypeFunction.exp).sub(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)));
        }

        if (function.getType().equals(TypeFunction.sech)) {
            return Expression.TWO.div(new Function(((Function) function).getLeft(), TypeFunction.exp).add(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)));
        }

        if (function.getType().equals(TypeFunction.cosech)) {
            return Expression.TWO.div(new Function(((Function) function).getLeft(), TypeFunction.exp).sub(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)));
        }

        return function;

    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException {

        Function function = new Function(this.left.simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var), this.type);

        // Nur ersetzen, wenn das Argument der Funktion function von var abhängt.
        if (!function.contains(var)) {
            return function;
        }

        // Dekadischer Logarithmus.
        if (function.getType().equals(TypeFunction.lg)) {
            return new Function(function.getLeft(), TypeFunction.ln).div(new Function(new Constant(10), TypeFunction.ln));
        }

        // Hyperbolische Funktionen.
        if (function.getType().equals(TypeFunction.cosh)) {
            return new Function(((Function) function).getLeft(), TypeFunction.exp).add(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)).div(2);
        }

        if (function.getType().equals(TypeFunction.sinh)) {
            return new Function(((Function) function).getLeft(), TypeFunction.exp).sub(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)).div(2);
        }

        if (function.getType().equals(TypeFunction.tanh)) {
            return new Function(((Function) function).getLeft(), TypeFunction.exp).sub(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)).div(new Function(((Function) function).getLeft(), TypeFunction.exp).add(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)));
        }

        if (function.getType().equals(TypeFunction.coth)) {
            return new Function(((Function) function).getLeft(), TypeFunction.exp).add(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)).div(new Function(((Function) function).getLeft(), TypeFunction.exp).sub(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)));
        }

        if (function.getType().equals(TypeFunction.sech)) {
            return Expression.TWO.div(new Function(((Function) function).getLeft(), TypeFunction.exp).add(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)));
        }

        if (function.getType().equals(TypeFunction.cosech)) {
            return Expression.TWO.div(new Function(((Function) function).getLeft(), TypeFunction.exp).sub(new Function(Expression.MINUS_ONE.mult(((Function) function).getLeft()).simplify(), TypeFunction.exp)));
        }

        return function;

    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException {

        Function function = new Function(this.left.simplifyReplaceTrigonometricalFunctionsByDefinitions(), this.type);

        if (function.getType().equals(TypeFunction.tan)) {
            return function.getLeft().sin().div(function.getLeft().cos());
        }

        if (function.getType().equals(TypeFunction.cot)) {
            return function.getLeft().cos().div(function.getLeft().sin());
        }

        if (function.getType().equals(TypeFunction.sec)) {
            return Expression.ONE.div(function.getLeft().cos());
        }

        if (function.getType().equals(TypeFunction.cosec)) {
            return Expression.ONE.div(function.getLeft().sin());
        }

        return function;

    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException {

        Function function = new Function(this.left.simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var), this.type);

        // Nur ersetzen, wenn das Argument der Funktion function von var abhängt.
        if (!function.contains(var)) {
            return function;
        }

        if (function.getType().equals(TypeFunction.tan)) {
            return function.getLeft().sin().div(function.getLeft().cos());
        }

        if (function.getType().equals(TypeFunction.cot)) {
            return function.getLeft().cos().div(function.getLeft().sin());
        }

        if (function.getType().equals(TypeFunction.sec)) {
            return Expression.ONE.div(function.getLeft().cos());
        }

        if (function.getType().equals(TypeFunction.cosec)) {
            return Expression.ONE.div(function.getLeft().sin());
        }

        return function;

    }

    @Override
    public Expression simplifyExpandProductsOfComplexExponentialFunctions(String var) throws EvaluationException {
        return new Function(this.left.simplifyExpandProductsOfComplexExponentialFunctions(var), this.type);
    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {
        return new Function(this.left.simplifyAlgebraicExpressions(), this.type);
    }

}
