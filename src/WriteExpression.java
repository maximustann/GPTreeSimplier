import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteExpression {
    public void writeExpTo(String path, int run, String expression)
            throws IOException {
        String filePath = path + run + ".txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(expression);
        writer.close();
    }
}
