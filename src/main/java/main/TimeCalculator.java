package main;

import java.util.Scanner;

public class TimeCalculator {

    public static void main(String[] args) {

        // Declare Constants
        final double HOUR_TO_MINUTE = 60;
        final double MILE_TO_METER = 1609.344;

        // Prepare keyboard reader
        Scanner scanner = new Scanner(System.in);

        // Read distance
        System.out.print("Distance: ");
        double distance = scanner.nextDouble();


        // Read speed
        System.out.print("Speed: ");
        double speed = scanner.nextDouble();

        // If direction of destination and speed vectors are not same, time is infinitive.
        if((distance > 0 && speed <= 0) || (distance < 0 && speed >= 0)) {
            System.err.println("Error: You cannot reach the destination.");
        }
        else {
            double time;
            // If speed is zero, then time is zero.
            if(speed == 0) {
                time = 0;
            }
            // Else time = distance/speed with appropriate unit conversion
            else {
                double distance_in_miles = distance / MILE_TO_METER;
                double time_in_hours = distance_in_miles / speed;
                time = time_in_hours * HOUR_TO_MINUTE;
            }

            // Display time
            System.out.println("Time: " + time + " minutes.");

        }

    }
}
