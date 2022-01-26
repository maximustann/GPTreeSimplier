
import abstractexpressions.expression.classes.Expression;
import exceptions.EvaluationException;
import exceptions.ExpressionException;

public class Testing {
    public static void main(String[] args){

        String expressionPath = "/local/scratch/tanboxi/VMCreationGP/NonAnyFitFramework" +
                "/Container200_realData_small_OS2_BIAS/Container200_realData_small/simpleGPTree_";
//        ReadExpression expReader = new ReadExpression(expressionPath);
//        String rawExpression = expReader.readExpFrom(4);
        String rawExpression = "((d / ((a + e) / (d - e))) - (c * (d / ((d - e) / d)))) + (((f / ((d + f) / (d - e))) - (c / ((a / d) + e))) - (b / (((e * b) * c) * c)))";
        Expression fSimplifiedStandard = null;
        try {
            Expression f = Expression.build(rawExpression);
            System.out.println(f);
            fSimplifiedStandard = f.simplify();
        } catch (ExpressionException | EvaluationException e){
            e.printStackTrace();
        }
        System.out.println(fSimplifiedStandard);
    }
}
