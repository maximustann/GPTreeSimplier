import abstractexpressions.expression.classes.Expression;
import exceptions.EvaluationException;
import exceptions.ExpressionException;

import java.io.File;
import java.io.IOException;

public class Simplify {

    public static void main(String[] args){
        String path = args[0];
        String simplifiedGPTreePath = path + "_simplifiedGP/";
        int run = Integer.parseInt(args[1]);
        if(run < 0 && run >= 30){
            System.out.println("Error: number of run smaller than 0 or greater than 30");
            return;
        }

        File checkPath = new File(simplifiedGPTreePath);
        if(!checkPath.exists()){
            checkPath.mkdir();
        }

//        String rawGPTreePath = path + "/selectionGPTree_";
        String rawGPTreePath = path + "/simpleGPTree_";
        String simplifiedGPTreeFilePath = simplifiedGPTreePath + "bestGPTree_";


        ReadExpression expReader = new ReadExpression(rawGPTreePath);
        WriteExpression expWriter = new WriteExpression();
        String rawExpression = expReader.readExpFrom(run);

        Expression simplifiedExpression = null;
        try {
            Expression f = Expression.build(rawExpression);
            simplifiedExpression = f.simplify();
        } catch (ExpressionException | EvaluationException e){
            e.printStackTrace();
        }

        try {
            expWriter.writeExpTo(simplifiedGPTreeFilePath, run, simplifiedExpression.toString());
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
