import lombok.val;

import java.io.IOException;
import java.util.*;

public class Application {


    public static void main(String[] args) throws IOException {
        val scanner = new Scanner(System.in);
        System.out.println("Enter Latitude");
        double latitude = scanner.nextDouble(); //          40,730610
        System.out.println("Enter longitude");
        double longitude = scanner.nextDouble(); //          -73,935242
        System.out.println("Wait a sec ...");

        EarthquakeAnalyzer.showTheClosestEarthquakes(longitude,latitude);
    }

}