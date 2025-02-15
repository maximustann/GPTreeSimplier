package expression.computationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyPolynomialUtils;
import static abstractexpressions.expression.basic.SimplifyTrigonometryUtils.THREE;
import abstractexpressions.expression.basic.SimplifyUtilities;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import basic.MathToolTestBase;
import java.math.BigInteger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.fail;
import utilities.TestUtilities;

public class PolynomialTests extends MathToolTestBase {

    Expression f, g, h, expectedFactorizationOfF, expectedFactorizationOfFLight, ggT;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void getPolynomialCoefficientsTest1() {
        try {
            f = Expression.build("(1+x)^2-(x-1)^2");
            ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(f, "x");
            ExpressionCollection coefficientsExpected = new ExpressionCollection(0, 4);
            TestUtilities.printResult(coefficientsExpected, coefficients);
            Assert.assertTrue(coefficients.equals(coefficientsExpected));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getPolynomialCoefficientsTest2() {
        try {
            f = Expression.build("(1/2+x)^3-x^2");
            ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(f, "x");
            ExpressionCollection coefficientsExpected = new ExpressionCollection(ONE.div(8), THREE.div(4), ONE.div(TWO), ONE);
            TestUtilities.printResult(coefficientsExpected, coefficients);
            Assert.assertTrue(coefficients.equals(coefficientsExpected));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getPolynomialCoefficientsTest3() {
        try {
            f = Expression.build("(2+x/3)^3+(1+x)^5-3*x^5");
            ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(f, "x");
            ExpressionCollection coefficientsExpected = new ExpressionCollection(9, 9, "32/3", "271/27", 5, -2);
            TestUtilities.printResult(coefficientsExpected, coefficients);
            Assert.assertTrue(coefficients.equals(coefficientsExpected));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void periodicCoefficientsTest1() {
        ExpressionCollection c = new ExpressionCollection(2, 1, -4, 2, 1, -4);
        TestUtilities.printResult(3, SimplifyPolynomialUtils.getPeriodOfCoefficients(c));
        Assert.assertTrue(SimplifyPolynomialUtils.getPeriodOfCoefficients(c) == 3);
    }

    @Test
    public void periodicCoefficientsTest2() {
        ExpressionCollection c = new ExpressionCollection(2, 1, -4, 2, 7, -4);
        TestUtilities.printResult(6, SimplifyPolynomialUtils.getPeriodOfCoefficients(c));
        Assert.assertTrue(SimplifyPolynomialUtils.getPeriodOfCoefficients(c) == 6);
    }

    @Test
    public void periodicCoefficientsEmptyTest() {
        ExpressionCollection c = new ExpressionCollection();
        TestUtilities.printResult(0, SimplifyPolynomialUtils.getPeriodOfCoefficients(c));
        Assert.assertTrue(SimplifyPolynomialUtils.getPeriodOfCoefficients(c) == 0);
    }

    @Test
    public void antiperiodicCoefficientsTest1() {
        ExpressionCollection c = new ExpressionCollection(2, 1, -4, -2, -1, 4, 2, 1, -4);
        TestUtilities.printResult(3, SimplifyPolynomialUtils.getAntiperiodOfCoefficients(c));
        Assert.assertTrue(SimplifyPolynomialUtils.getAntiperiodOfCoefficients(c) == 3);
    }

    @Test
    public void antiperiodicCoefficientsTest2() {
        ExpressionCollection c = new ExpressionCollection(5, 1, -5, -1, 5, 1, -5, -1);
        TestUtilities.printResult(2, SimplifyPolynomialUtils.getAntiperiodOfCoefficients(c));
        Assert.assertTrue(SimplifyPolynomialUtils.getAntiperiodOfCoefficients(c) == 2);
    }

    @Test
    public void antiperiodicCoefficientsTest3() {
        ExpressionCollection c = new ExpressionCollection(4, 3, -8, 2);
        TestUtilities.printResult(4, SimplifyPolynomialUtils.getAntiperiodOfCoefficients(c));
        Assert.assertTrue(SimplifyPolynomialUtils.getAntiperiodOfCoefficients(c) == 4);
    }

    @Test
    public void antiperiodicCoefficientsEmptyTest() {
        ExpressionCollection c = new ExpressionCollection();
        TestUtilities.printResult(0, SimplifyPolynomialUtils.getAntiperiodOfCoefficients(c));
        Assert.assertTrue(SimplifyPolynomialUtils.getAntiperiodOfCoefficients(c) == 0);
    }

    // Tests für Polynomdivision.
    @Test
    public void polynomialDivisionWithRestTest() {
        // f = 2*x^5+3*x^4+x^3/3-x^2+6*x+10 und g = 5*x^2+x-1/2.
        // Quotient = 2*x^3/5+13*x^2/25+x/375-557/3750 und Rest = 11531*x/1875+74443/7500.
        try {
            ExpressionCollection coefficientsF = new ExpressionCollection(10, 6, -1, ONE.div(THREE), 3, 2);
            ExpressionCollection coefficientsG = new ExpressionCollection(MINUS_ONE.div(TWO), 1, 5);
            ExpressionCollection[] divisionResult = SimplifyPolynomialUtils.polynomialDivision(coefficientsF, coefficientsG);
            ExpressionCollection expectedQuotient = new ExpressionCollection(new Constant(-557).div(3750), ONE.div(375), new Constant(13).div(25), TWO.div(5));
            ExpressionCollection expectedRest = new ExpressionCollection(new Constant(74443).div(7500), new Constant(11531).div(1875));
            TestUtilities.printResults(new Object[]{expectedQuotient, expectedRest},
                    new Object[]{divisionResult[0], divisionResult[1]});
            Assert.assertTrue(divisionResult.length == 2);
            Assert.assertTrue(divisionResult[0].equals(expectedQuotient));
            Assert.assertTrue(divisionResult[1].equals(expectedRest));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void polynomialDivisionDegEnumeratorIsSmallerThanDegDenominatorTest() {
        // f = x^3+5*x^2+1 und g = x^5-4*x^3+2.
        // Quotient = 0 und Rest = x^3+5*x^2+1.
        try {
            ExpressionCollection coefficientsF = new ExpressionCollection(1, 0, 5, 3);
            ExpressionCollection coefficientsG = new ExpressionCollection(2, 0, 0, -4, 0, 1);
            ExpressionCollection[] divisionResult = SimplifyPolynomialUtils.polynomialDivision(coefficientsF, coefficientsG);
            ExpressionCollection expectedQuotient = new ExpressionCollection();
            ExpressionCollection expectedRest = new ExpressionCollection(1, 0, 5, 3);
            TestUtilities.printResults(new Object[]{expectedQuotient, expectedRest},
                    new Object[]{divisionResult[0], divisionResult[1]});
            Assert.assertTrue(divisionResult.length == 2);
            Assert.assertTrue(divisionResult[0].equals(expectedQuotient));
            Assert.assertTrue(divisionResult[1].equals(expectedRest));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void polynomialDivisionWithoutRestTest() {
        // f = (7*x^2)/2+x^6+15*x^3+x^5-(7/2+(11*x)/2+13*x^4) und g = x^2+3*x-7.
        // Quotient = x^4-2*x^3+x+1/2 und Rest = 0.
        try {
            ExpressionCollection coefficientsF = new ExpressionCollection(new Constant(-7).div(2), new Constant(-11).div(2),
                    new Constant(7).div(2), 15, -13, 1, 1);
            ExpressionCollection coefficientsG = new ExpressionCollection(-7, 3, 1);
            ExpressionCollection[] divisionResult = SimplifyPolynomialUtils.polynomialDivision(coefficientsF, coefficientsG);
            ExpressionCollection expectedQuotient = new ExpressionCollection(ONE.div(2), 1, 0, -2, 1);
            ExpressionCollection expectedRest = new ExpressionCollection();
            TestUtilities.printResults(new Object[]{expectedQuotient, expectedRest},
                    new Object[]{divisionResult[0], divisionResult[1]});
            Assert.assertTrue(divisionResult.length == 2);
            Assert.assertTrue(divisionResult[0].equals(expectedQuotient));
            Assert.assertTrue(divisionResult[1].equals(expectedRest));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    // Tests für die Berechnung des ggT von Polynomen.
    @Test
    public void getGGTOfPolynomialsTest1() {
        // ggT von f = 6+5*x+x^2 ( = (3+x)*(2+x)) und g = 3+13*x+7*x^2+x^3 ( = (3+x)*(1+4*x+x^2)).
        // ggT = 3+x.
        try {
            f = Expression.build("6+5*x+x^2");
            g = Expression.build("3+13*x+7*x^2+x^3");
            ggT = Expression.build("3+x");
            Expression expectedResult = SimplifyPolynomialUtils.getGGTOfPolynomials(f, g, "x");
            TestUtilities.printResult(expectedResult, ggT);
            Assert.assertTrue(expectedResult.equivalent(ggT));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getGGTOfPolynomialsTest2() {
        /* 
         ggT von f = 64+244*x^2+41*x^4+352*x+136*x^3+x^6+9*x^5 ( = (x^2+5*x+1)*(x^2+2*x+8)^2) 
         und g = x^5+4*x^3-(256+48*x^2+64*x) ( = (x-4)*(x^2+2*x+8)^2).
         */
        // ggT = 64+20*x^2+x^4+32*x+4*x^3 ( = (x^2+2*x+8)^2).
        try {
            f = Expression.build("64+244*x^2+41*x^4+352*x+136*x^3+x^6+9*x^5");
            g = Expression.build("x^5+4*x^3-(256+48*x^2+64*x)");
            ggT = Expression.build("64+20*x^2+x^4+32*x+4*x^3");
            Expression expectedResult = SimplifyPolynomialUtils.getGGTOfPolynomials(f, g, "x");
            TestUtilities.printResult(expectedResult, ggT);
            Assert.assertTrue(expectedResult.equivalent(ggT));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getGGTOfPolynomialsTest3() {
        /* 
         ggT von f = t/(1+x^2-2*x)+1/(x+x^3-2*x^2)+1/(1-x)-(t/(1-x)+2/(1+x^2-2*x)) + z*((2*t)/(1+x^2-2*x)+1/(x^2+x^4-2*x^3)+1/(x-x^2)-(t/(1-x)+(x*t)/(1+x^2-2*x)+2/(x+x^3-2*x^2))) 
         und g = x + z bezüglich der Veränderlichen z.
         */
        // ggT = x + z.
        try {
            f = Expression.build("t/(1+x^2-2*x)+1/(x+x^3-2*x^2)+1/(1-x)-(t/(1-x)+2/(1+x^2-2*x)) + z*((2*t)/(1+x^2-2*x)+1/(x^2+x^4-2*x^3)+1/(x-x^2)-(t/(1-x)+(x*t)/(1+x^2-2*x)+2/(x+x^3-2*x^2)))");
            g = Expression.build("x+z");
            ggT = Expression.build("x+z");
            ExpressionCollection coefficientsF = SimplifyPolynomialUtils.getPolynomialCoefficients(f, "z");
            ExpressionCollection coefficientsG = SimplifyPolynomialUtils.getPolynomialCoefficients(g, "z");
            ExpressionCollection expectedResultCoefficients = SimplifyPolynomialUtils.getGGTOfPolynomials(coefficientsF, coefficientsG);
            Expression expectedResult = SimplifyPolynomialUtils.getPolynomialFromCoefficients(expectedResultCoefficients, "z");
            TestUtilities.printResult(expectedResult, ggT);
            Assert.assertTrue(expectedResult.equivalent(ggT));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getGGTOfPolynomialsTest4() {
        // ggT von f = x^2+(a+b)*x+a*b und g = x^2+(a+c)*x+a*c bezüglich der Veränderlichen x ist x+a.
        try {
            f = Expression.build("x^2+(a+b)*x+a*b");
            g = Expression.build("x^2+(a+c)*x+a*c");
            ggT = Expression.build("x+a");
            ExpressionCollection coefficientsF = SimplifyPolynomialUtils.getPolynomialCoefficients(f, "x");
            ExpressionCollection coefficientsG = SimplifyPolynomialUtils.getPolynomialCoefficients(g, "x");
            ExpressionCollection expectedResultCoefficients = SimplifyPolynomialUtils.getGGTOfPolynomials(coefficientsF, coefficientsG);
            Expression expectedResult = SimplifyPolynomialUtils.getPolynomialFromCoefficients(expectedResultCoefficients, "x");
            TestUtilities.printResult(expectedResult, ggT);
            Assert.assertTrue(expectedResult.equivalent(ggT));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getGGTOfPolynomialsIsTrivialTest() {
        // ggT von f = x^3+5*x^2-7*x-4 und g = 2*x^2+4*x-18 ist 1.
        try {
            f = Expression.build("x^3+5*x^2-7*x-4");
            g = Expression.build("2*x^2+4*x-18");
            ggT = ONE;
            Expression expectedResult = SimplifyPolynomialUtils.getGGTOfPolynomials(f, g, "x");
            TestUtilities.printResult(expectedResult, ggT);
            Assert.assertTrue(expectedResult.equals(ggT));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getEuclideanRepresentationOfPolynomialsTest1() {
        // ggT von f = x^2+3*x+2 und g = x^2+4*x+3 ist = x+1 und die Eukliddarstellung ist ggT = 1*f - 1*g.
        try {
            f = Expression.build("x^2+3*x+2");
            g = Expression.build("x^2+4*x+3");
            Expression[] expectedResult = SimplifyPolynomialUtils.getEuclideanRepresentationOfGCDOfTwoPolynomials(f, g, "x");
            TestUtilities.printResults(new Object[]{expectedResult[0], expectedResult[1]},
                    new Object[]{MINUS_ONE, ONE});
            Assert.assertTrue(expectedResult.length == 2);
            Assert.assertTrue(expectedResult[0].equals(MINUS_ONE));
            Assert.assertTrue(expectedResult[1].equals(ONE));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getEuclideanRepresentationOfPolynomialsTest2() {
        /* 
        ggT von f = 8+16*x+10*x^2+2*x^3 = (x+2)^2*(2*x+2) und g = 60+95*x+40*x^2+5*x^3 = (x+3)*(x+4)*(5*x+5) ist = x+1. 
        und die Eukliddarstellung ist ggT = 1*f - 1*g.
         */
        try {
            f = Expression.build("8+16*x+10*x^2+2*x^3");
            g = Expression.build("60+95*x+40*x^2+5*x^3");
            Expression[] expectedResult = SimplifyPolynomialUtils.getEuclideanRepresentationOfGCDOfTwoPolynomials(f, g, "x");
            TestUtilities.printResults(new Object[]{expectedResult[0], expectedResult[1]},
                    new Object[]{SimplifyPolynomialUtils.getPolynomialFromCoefficients("x", new Constant(13).div(8), new Constant(3).div(8)),
                        SimplifyPolynomialUtils.getPolynomialFromCoefficients("x", new Constant(-1).div(5), new Constant(-3).div(20))});
            Assert.assertTrue(expectedResult.length == 2);
            Assert.assertTrue(expectedResult[0].equivalent(SimplifyPolynomialUtils.getPolynomialFromCoefficients("x", new Constant(13).div(8), new Constant(3).div(8))));
            Assert.assertTrue(expectedResult[1].equivalent(SimplifyPolynomialUtils.getPolynomialFromCoefficients("x", new Constant(-1).div(5), new Constant(-3).div(20))));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getEuclideanRepresentationOfPolynomialsTest3() {
        /* 
        ggT von f = x^2+x+2 und g = x^2+1 ist = 1 
        und die optimale Eukliddarstellung von h = x^2 ist ((-1)/2+(1/2)*x)*f + (1+((-1)/2)*x)*g.
         */
        try {
            f = Expression.build("x^2+x+2");
            g = Expression.build("x^2+1");
            h = Expression.build("x^2");
            Expression[] expectedResult = SimplifyPolynomialUtils.getOptimalEuclideanRepresentation(f, g, h, "x");
            TestUtilities.printResults(new Object[]{expectedResult[0], expectedResult[1]},
                    new Object[]{Expression.build("(-1)/2+(1/2)*x"),
                        Expression.build("1+((-1)/2)*x")});
            Assert.assertTrue(expectedResult.length == 2);
            Assert.assertTrue(expectedResult[0].equivalent(Expression.build("(-1)/2+(1/2)*x")));
            Assert.assertTrue(expectedResult[1].equivalent(Expression.build("1+((-1)/2)*x")));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getEuclideanRepresentationOfPolynomialsTest4() {
        /* 
        ggT von f = x^2+3*x+2 und g = x^2+4*x+3 ist = x+1 
        und die optimale Eukliddarstellung von h = x^3+x^2 ist 3*x*f + (-2*x)*g.
         */
        try {
            f = Expression.build("x^2+3*x+2");
            g = Expression.build("x^2+4*x+3");
            h = Expression.build("x^3+x^2");
            Expression[] expectedResult = SimplifyPolynomialUtils.getOptimalEuclideanRepresentation(f, g, h, "x");
            TestUtilities.printResults(new Object[]{expectedResult[0], expectedResult[1]},
                    new Object[]{Expression.build("3*x"),
                        Expression.build("(-2)*x")});
            Assert.assertTrue(expectedResult.length == 2);
            Assert.assertTrue(expectedResult[0].equivalent(Expression.build("3*x")));
            Assert.assertTrue(expectedResult[1].equivalent(Expression.build("(-2)*x")));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void noEuclideanRepresentationOfPolynomialsTest() {
        /* 
        ggT von f = x^2+3*x+2 und g = x^2+4*x+3 ist = x+1 
        und eine Eukliddarstellung von h = x^4 existiert nicht.
         */
        try {
            f = Expression.build("x^2+3*x+2");
            g = Expression.build("x^2+4*x+3");
            h = Expression.build("x^4");
            Expression[] expectedResult = SimplifyPolynomialUtils.getOptimalEuclideanRepresentation(f, g, h, "x");
            TestUtilities.printResults(expectedResult, new Expression[]{});
            Assert.assertTrue(expectedResult.length == 0);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    // Tests für Polynomfaktorisierung.
    @Test
    public void decomposeCyclicPolynomialTest1() {
        // Zerlegung von x^5-7 in irreduzible Faktoren.
        try {
            f = Expression.build("x^5-7");
            expectedFactorizationOfF = Expression.build("(x-7^(1/5))*((x^2+7^(2/5))+(7^(1/5)*(1+5^(1/2))*x)/2)*((x^2+7^(2/5))-(7^(1/5)*(5^(1/2)-1)*x)/2)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposeCyclicPolynomialTest2() {
        // Zerlegung von x^4+1 in irreduzible Faktoren.
        try {
            f = Expression.build("x^4+1");
            expectedFactorizationOfF = Expression.build("(x^2+2^(1/2)*x+1)*((x^2+1)-2^(1/2)*x)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposeCyclicPolynomialTest3() {
        // Zerlegung von x^3+1 in irreduzible Faktoren.
        try {
            f = Expression.build("x^3+1");
            expectedFactorizationOfF = Expression.build("(x+1)*((1+x^2)-x)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposeGeometricPolynomialOfEvenDegreeTest() {
        // Zerlegung von 1+x+x^2+x^3+x^4 in irreduzible Faktoren.
        try {
            f = Expression.build("1+x+x^2+x^3+x^4");
            expectedFactorizationOfF = Expression.build("((1+x^2)+((1-5^(1/2))*x)/2)*(1+x^2+((5^(1/2)+1)*x)/2)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposeGeometricPolynomialOfOddDegreeTest() {
        // Zerlegung von 1+x+x^2+x^3+x^4+x^5 in irreduzible Faktoren.
        try {
            f = Expression.build("1+x+x^2+x^3+x^4+x^5");
            expectedFactorizationOfF = Expression.build("(1+x)*((1+x^2)-x)*(1+x+x^2)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest2() {
        // Zerlegung von 4*x^5-7*x^4+2*x^3+4*x^2-7*x+2 in irreduzible Faktoren.
        try {
            f = Expression.build("4*x^5+3*x^4+25*x^3+4*x^2+3*x+25");
            expectedFactorizationOfF = Expression.build("4*(x^2+(3*x)/4+25/4)*(1+x)*((1+x^2)-x)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest3() {
        // Zerlegung von 12+2*x+x^2-12*x^3-2*x^4-x^5+12*x^6+2*x^7+x^8 in irreduzible Faktoren.
        try {
            f = Expression.build("12+2*x+x^2-12*x^3-2*x^4-x^5+12*x^6+2*x^7+x^8");
            expectedFactorizationOfF = Expression.build("(12+2*x+x^2)*((1+x^6)-x^3)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x").orderDifferencesAndQuotients();
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest4() {
        // Zerlegung von x^3+5*x+7 in irreduzible Faktoren.
        try {
            f = Expression.build("x^3+5*x+7");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            TestUtilities.printResults(new Object[]{SimplifyPolynomialUtils.getDegreeOfPolynomial(factors.get(0), "x"),
                SimplifyPolynomialUtils.getDegreeOfPolynomial(factors.get(1), "x")},
                    new Object[]{BigInteger.ONE, BigInteger.valueOf(2)});
            Assert.assertTrue(factors.getBound() == 2);
            // Hier werden nur die Grade getestet, da die Faktoren etwas zu kompliziert sind.
            Assert.assertTrue(SimplifyPolynomialUtils.getDegreeOfPolynomial(factors.get(0), "x").compareTo(BigInteger.ONE) == 0);
            Assert.assertTrue(SimplifyPolynomialUtils.getDegreeOfPolynomial(factors.get(1), "x").compareTo(BigInteger.valueOf(2)) == 0);
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest5() {
        // Zerlegung von f = 144+33*x^2+x^4+72*x+6*x^3 = (x^2+3*x+12)^2 in irreduzible Faktoren.
        try {
            f = Expression.build("144+33*x^2+x^4+72*x+6*x^3");
            expectedFactorizationOfF = Expression.build("(x^2+3*x+12)^2");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest6() {
        /* 
         Zerlegung von f = 248832+259200*x^2+61020*x^4+5085*x^6+150*x^8+x^10
         +311040*x+142560*x^3+19683*x^5+990*x^7+15*x^9 = (x^2+3*x+12)^5 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("248832+259200*x^2+61020*x^4+5085*x^6+150*x^8+x^10+311040*x+142560*x^3+19683*x^5+990*x^7+15*x^9");
            expectedFactorizationOfF = Expression.build("(x^2+3*x+12)^5");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest7() {
        /* 
         Zerlegung von f = 216+585*x^2+275*x^4+35*x^6+459*x+469*x^3+113*x^5+x^8+7*x^7 = (x^2+x+8)*(x^2+2*x+3)^3 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("216+585*x^2+275*x^4+35*x^6+459*x+469*x^3+113*x^5+x^8+7*x^7");
            expectedFactorizationOfF = Expression.build("(x^2+x+8)*(x^2+2*x+3)^3");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest8() {
        /* 
         Zerlegung von f = 2+4*x^2+2*x^4 = 2*(x^2+1)^2 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("2+4*x^2+2*x^4");
            expectedFactorizationOfF = Expression.build("2*(1+x^2)^2");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest9() {
        /* 
         Zerlegung von f = 32+178*x^2+259*x^4+165*x^6+80*x+218*x^3+197*x^5+56*x^8+82*x^7+11*x^10+16*x^9+x^12+x^11 = (x^2+2)*(x^2-x+4)^2*(x^2+x+1)^3 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("32+178*x^2+259*x^4+165*x^6+80*x+218*x^3+197*x^5+56*x^8+82*x^7+11*x^10+16*x^9+x^12+x^11");
            expectedFactorizationOfF = Expression.build("(x^2+2)*((4+x^2)-x)^2*(x^2+x+1)^3");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest10() {
        /* 
         Zerlegung von f = 14+93*x+125*x^2+51*x^3+5*x^4 = 5*(x+1)*(x+2)*(x+7)*(x+1/5) in irreduzible Faktoren.
         */
        try {
            f = Expression.build("14+93*x+125*x^2+51*x^3+5*x^4");
            expectedFactorizationOfF = Expression.build("5*(x+1)*(x+2)*(x+7)*(x+1/5)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest11() {
        /* 
         Zerlegung von f = 27*x+27*x^3+9*x^5+x^7-(135+135*x^2+45*x^4+5*x^6) = (x^2+3)^3*(x-5) in irreduzible Faktoren.
         */
        try {
            f = Expression.build("27*x+27*x^3+9*x^5+x^7-(135+135*x^2+45*x^4+5*x^6)");
            expectedFactorizationOfF = Expression.build("(x^2+3)^3*(x-5)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialBySolvoingPolynomialSystemTest() {
        /* 
         Zerlegung von f = x^4+5*x^3+21*x^2+35*x+50 = (x^2+2*x+5)*(x^2+3*x+10) in irreduzible Faktoren.
         */
        try {
            f = Expression.build("x^4+5*x^3+21*x^2+35*x+50");
            expectedFactorizationOfF = Expression.build("(x^2+2*x+5)*(x^2+3*x+10)");
            f = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialIntoSquarefreeFactorsTest1() {
        /* 
         Zerlegung von f = 864+1728*x+2448*x^2+2768*x^3+2240*x^4+1616*x^5+864*x^6+408*x^7+138*x^8+40*x^9+7*x^10+x^11 
         = (1+x)*(2+x^2)^2*(6+2*x+x^2)^3 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("864+1728*x+2448*x^2+2768*x^3+2240*x^4+1616*x^5+864*x^6+408*x^7+138*x^8+40*x^9+7*x^10+x^11");
            expectedFactorizationOfF = Expression.build("(1+x)*(2+x^2)^2*(6+2*x+x^2)^3");
            f = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialIntoSquarefreeFactorsTest2() {
        /* 
         Zerlegung von f = 10125+23625*x+26325*x^2+22425*x^3+14990*x^4+7910*x^5+3486*x^6+1254*x^7+357*x^8+81*x^9+13*x^10+x^11 
         = (1+x)*(5+x^2)^3*(x+3)^4 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("10125+23625*x+26325*x^2+22425*x^3+14990*x^4+7910*x^5+3486*x^6+1254*x^7+357*x^8+81*x^9+13*x^10+x^11");
            expectedFactorizationOfF = Expression.build("(1+x)*(5+x^2)^3*(x+3)^4");
            f = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialIntoSquarefreeFactorsTest3() {
        /* 
         Zerlegung von f = a^2+2*a*x+x^2 
         = (a+x)^2 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("a^2+2*a*x+x^2");
            expectedFactorizationOfF = Expression.build("(a+x)^2");
            f = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialIntoSquarefreeFactorsTest4() {
        /* 
         Zerlegung von f = 1+3*a*x+3*a^2*x^2+a^3*x^3 
         = (1+a*x)^3 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("1+3*a*x+3*a^2*x^2+a^3*x^3");
            expectedFactorizationOfF = Expression.build("a^3*(1/a+x)^3");
            f = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialIntoSquarefreeFactorsTest5() {
        /* 
         Zerlegung von f = a*b^2+2*a*b*x+a*x^2+x*b^2+2*x^2*b+x^3 
         = (a+x)*(b+x)^2 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("a*b^2+2*a*b*x+a*x^2+x*b^2+2*x^2*b+x^3");
            expectedFactorizationOfF = Expression.build("(a+x)*(b+x)^2");
            f = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialIntoSquarefreeFactorsTest6() {
        /* 
         Zerlegung von f = a^3+3*a^2*b+3*a*b^2+b^3+3*a^2*x+6*a*b*x+3*b^2*x+3*a*x^2+3*b*x^2+x^3 
         = (a+b+x)^3 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("a^3+3*a^2*b+3*a*b^2+b^3+3*a^2*x+6*a*b*x+3*b^2*x+3*a*x^2+3*b*x^2+x^3");
            expectedFactorizationOfF = Expression.build("(a+b+x)^3");
            f = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialIntoSquarefreeFactorsTest7() {
        /* 
         Zerlegung von f = x^4+2*x^2*t+t^2 = (x^2+t)^2 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("x^4+2*x^2*t+t^2");
            expectedFactorizationOfF = Expression.build("(x^2+t)^2");
            f = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(f, "x");
            TestUtilities.printResult(expectedFactorizationOfF, f);
            Assert.assertTrue(f.equivalent(expectedFactorizationOfF));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialIntoSquarefreeFactorsTwoVariantsTest() {
        /* 
         Zerlegung von f = (2+4*x+2*x^2)*(3+4*x+x^2) = 2*(3+x)*(1+x)^3 in irreduzible Faktoren.
         Hier wird auf zwei Arten faktorisiert.
         */
        try {
            f = Expression.build("(2+4*x+2*x^2)*(3+4*x+x^2)");
            expectedFactorizationOfF = Expression.build("2*(3+x)*(1+x)^3");
            Expression fDecomposed = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactors(f, "x");
            Assert.assertTrue(fDecomposed.equivalent(expectedFactorizationOfF));
            Expression fDecomposedLight = SimplifyPolynomialUtils.decomposePolynomialIntoSquarefreeFactorsLight(f, "x");
            expectedFactorizationOfFLight = Expression.build("2*(1+x)^2*(3+4*x+x^2)");
            Assert.assertTrue(fDecomposedLight.equivalent(expectedFactorizationOfFLight));
            TestUtilities.printResults(new Object[]{expectedFactorizationOfF, expectedFactorizationOfFLight},
                    new Object[]{fDecomposed, fDecomposedLight});
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getResultantOfPolynomialsTest1() {
        /* 
         Resultante von f = 2*x^2+5*x-3 und g = x^2-4*x+6 ist 459. 
         */
        try {
            f = Expression.build("2*x^2+5*x-3");
            g = Expression.build("x^2-4*x+6");
            MatrixExpression resultant = SimplifyPolynomialUtils.getResultant(SimplifyPolynomialUtils.getPolynomialCoefficients(f, "x"),
                    SimplifyPolynomialUtils.getPolynomialCoefficients(g, "x"));
            TestUtilities.printResult(new Constant(459), resultant.convertOneTimesOneMatrixToExpression());
            Assert.assertTrue(resultant.convertOneTimesOneMatrixToExpression() instanceof Expression);
            Assert.assertTrue(((Expression) resultant.convertOneTimesOneMatrixToExpression()).equivalent(new Constant(459)));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getResultantOfPolynomialsTest2() {
        /* 
         Resultante von f = 5*x^3+x-1 und g = 2*x^2+4*x+3 ist 731. 
         */
        try {
            f = Expression.build("5*x^3+x-1");
            g = Expression.build("2*x^2+4*x+3");
            MatrixExpression resultant = SimplifyPolynomialUtils.getResultant(SimplifyPolynomialUtils.getPolynomialCoefficients(f, "x"),
                    SimplifyPolynomialUtils.getPolynomialCoefficients(g, "x"));
            TestUtilities.printResult(new Constant(731), resultant.convertOneTimesOneMatrixToExpression());
            Assert.assertTrue(resultant.convertOneTimesOneMatrixToExpression() instanceof Expression);
            Assert.assertTrue(((Expression) resultant.convertOneTimesOneMatrixToExpression()).equivalent(new Constant(731)));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getResultantOfPolynomialsTest3() {
        /* 
         Resultante von f = a und g = b*x+c ist a. 
         */
        try {
            f = Expression.build("a");
            g = Expression.build("b*x+c");
            MatrixExpression resultant = SimplifyPolynomialUtils.getResultant(SimplifyPolynomialUtils.getPolynomialCoefficients(f, "x"),
                    SimplifyPolynomialUtils.getPolynomialCoefficients(g, "x"));
            TestUtilities.printResult(Variable.create("a"), resultant.convertOneTimesOneMatrixToExpression());
            Assert.assertTrue(resultant.convertOneTimesOneMatrixToExpression() instanceof Expression);
            Assert.assertTrue(((Expression) resultant.convertOneTimesOneMatrixToExpression()).equivalent(Variable.create("a")));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
