import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.math3.complex.Complex;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CSVWritterExample {

    public static void exportDataToExcel(CSVWriter csvWriter, Complex[] data) throws FileNotFoundException, IOException {
        String[] values = new String[4096];
        for (int j = 0; j < 4096; j++) {

            double magnitute = Math.log(data[j].abs() + 1);

            values[j] = magnitute + "";
        }
        csvWriter.writeNext(values);

    }
}