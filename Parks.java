import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * Creates/edits an output file for a planned trip to parks, lists all parks, 
 * and searches for a park based on the inputted file of a list of trips.
 * @author Nitesh Kanamarlapudi
 */
public class Parks {

    /** Approximate radius of the earth in miles*/
    public static final double EARTH_RADIUS = 3959;
    
    /** Minimum degrees for earth's latitude*/
    public static final double LATITUDE_MIN_DEGREES = -90;
    
    /** Maximum degrees for earth's latitude*/
    public static final double LATITUDE_MAX_DEGREES = 90;
    
    /** Minimum degrees for earth's longitude*/
    public static final double LONGITUDE_MIN_DEGREES = -180;
    
    /** Maximum degrees for earth's longitude*/
    public static final double LONGITUDE_MAX_DEGREES = 180;
    
    /** The valid amount of values the input file can have to comply with the program*/
    public static final int VALID_NUMBER_OF_FILE_VALUES = 4;
    
    /** The valid amount of arguments the user can provide to comply with the program*/
    public static final int VALID_NUMBER_OF_UI_ARGS = 2;
    
    /**
     * Prompts user for action type with the input file (list/search parks), 
     * and creates/edits an output file for a user's planned trip to parks.
     * @param args command line arguments (not used)
     */
    public static void main (String[] args) {
    
        //ERROR HANDLING IN USER INTERFACE IF NOT 2 ARGUMENTS
        if (args.length != VALID_NUMBER_OF_UI_ARGS) {
            System.out.println("Usage: java -cp bin Parks parkfile tripfile");
            System.exit(1);
        } //if
        
        //File scanner
        Scanner in = null;
        
        //Print writer
        PrintWriter out = null;
        
        //Try to open input file
        try {
            in = new Scanner(new FileInputStream(args[0]));
        } //try
        //ERROR HANDLING IN USER INTERFACE IF FILE IS INACCESSIBLE
        catch (FileNotFoundException e) {
            System.out.println("Unable to access park file: " + args[0]);
            System.exit(1);
        } //catch
        
        //Output scanner
        Scanner scnr = new Scanner(System.in);
        
        //Path of the 2nd argument
        Path path = Path.of(args[1]);
        
        //ERROR HANDLING IN USER INTERFACE CHECKS IF OUTPUT FILE EXISTS
        if (Files.exists(path)) {
        
            //Checking if user is ok with rewriting their output file
            System.out.print(args[1] + " exists - OK to overwrite (y,n)?: ");
            String answer = scnr.next();
            
            //Leaving program if user doesn't want to change their output file
            if (!answer.toLowerCase().startsWith("y")) {
                System.exit(1);
            } //if
        } //if
        
        //Try to create the output PrintWriter
        try {
            out = new PrintWriter(new FileOutputStream(args[1]));
        } //try
        //ERROR HANDLING IN USER INTERFACE IF OUTPUT FILE CANNOT BE MADE
        catch (FileNotFoundException e) {
            System.out.println("Cannot create trip file");
            System.exit(1);
        } //catch
        
        //number of parks in input file
        int countOfParks = getNumberOfLines(in);
        
        //Try to open input file
        try {
            in = new Scanner(new FileInputStream(args[0]));
        } //try
        //ERROR HANDLING IN USER INTERFACE IF FILE IS INACCESSABLE
        catch (FileNotFoundException e) {
            System.out.println("Unable to access park file: " + args[0]);
            System.exit(1);
        } //catch

        //ERROR HANDLING IN USER INTERFACE IF INPUT FILE IS EMPTY
        if (countOfParks <= 0) {
            System.out.println("Empty park file");
            System.exit(1);
        } //if
        
        //Array for all the park ids from input file
        int[] parkIds = new int[countOfParks];
        
        //Array for all the park names from input file
        String[] parkNames = new String[countOfParks];
        
        //Array for all the park latitudes from input file
        double[] parkLatitudes = new double[countOfParks];
        
        //Array for all the park longitudes from input file
        double[] parkLongitudes = new double[countOfParks];
        
        //ERROR HANDLING IN USER INTERFACE IF INPUT FILE IS INVALID
        if (!inputParks(in, parkIds, parkNames, parkLatitudes, parkLongitudes)) {
            System.out.println("Invalid park file");
            System.exit(1);
        } //if
        
        //Number of parks that user adds to their trip
        int numberOfParksInTrip = 0;
        
        //Array for all the park ids from user's trip
        int[] tripIds = new int[10];
        
        //Array for all the park names from user's trip
        String[] tripNames = new String[10];
        
        //Array for all the park distances from user's trip
        double[] tripDistances = new double[10];
        
        //String option for what action user wants to use
        String option = "";
        scnr.nextLine();

        while (!option.equalsIgnoreCase("Q")) {
        
            //Printing user interface
            displayMenu();
            
            //Scanning which action user wants to use
            option = scnr.nextLine();
            
            //If user wants to list all the parks in the input file
            if (option.equalsIgnoreCase("L")) {
            
                System.out.println();
                
                //Printing heading for park ids, names, latitudes, and longitudes
                System.out.print(" ID");
                System.out.print("               Name");
                System.out.println("                        Latitude Longitude");
                
                //Printing list for all parks
                System.out.println(getParkList(parkIds, parkNames, 
                                               parkLatitudes, parkLongitudes));
            } //if
            
            //Else if user wants to search for parks using a keyword
            else if (option.equalsIgnoreCase("S")) {
            
                //Scanning for keyword
                System.out.print("Park name (is/contains): ");
                String codeWord = scnr.nextLine();
                System.out.println();
                
                //Printing heading for park ids, names, latitudes, and longitudes
                System.out.print(" ID");
                System.out.print("               Name");
                System.out.println("                        Latitude Longitude");
                
                //Printing list of all parks that contain that keyword
                System.out.println(searchForPark(codeWord, parkIds, 
                                                 parkNames, parkLatitudes, 
                                                 parkLongitudes));
                System.out.println();
            } //else if
            
            //Else if user wants to add a park to their trip
            else if (option.equalsIgnoreCase("A")) {

                //ERROR HANDLING IN USER INTERFACE IF USER'S TRIP IS FULL
                if (numberOfParksInTrip == 10) {
                    System.out.println("Trip is full");
                    System.out.println();
                    continue;
                } //if
            
                System.out.print("Park id: ");
                
                //int id for the park that the user wants to add to their trip
                int parkId = 0;
                
                //Try to scan park id
                try {
                    parkId = scnr.nextInt();
                    scnr.nextLine();
                } //try
                //ERROR HANDLING IN USER INTERFACE IF PARK ID IS INVALID
                catch (InputMismatchException e) {
                    System.out.println("Invalid id");
                    scnr.nextLine();
                    System.out.println();
                    continue;
                } //catch
                
                //int index to test the existence of the park
                int index = -1;
                for (int i = 0; i < parkIds.length; i++) {
                    if (parkIds[i] == parkId) {
                        index = i;
                    } //if
                } //for
        
                //ERROR HANDLING IN USER INTERFACE IF PARK ID DOESN'T EXIST
                if (index == -1) {
                    System.out.println("Invalid id");
                    System.out.println();
                    continue;
                } //if
                
                //Adding user's desired trip and finding number of parks in trip
                numberOfParksInTrip = addParkToTrip(parkId, numberOfParksInTrip, parkIds, 
                                                    parkNames, parkLatitudes, parkLongitudes, 
                                                    tripIds, tripNames, tripDistances);

                //Finding the name of the park added by user
                String addedParkName = "";
                for (int i = 0; i < parkIds.length; i++) {
                    if (parkId == parkIds[i]) {
                        addedParkName = parkNames[i];
                    } //if
                } //for
                
                //Printing if park was added to the user's trip
                System.out.println("Park added to trip: " + addedParkName);
                System.out.println();
            } //else if
            
            //Else if user wants to display their trip
            else if (option.equalsIgnoreCase("D")) {

                System.out.println();
                
                //Printing heading for park ids, names, and distance
                System.out.print(" ID");
                System.out.print("               Name");
                System.out.println("                        Distance");
                
                //Printing list of all added parks to the user's trip
                System.out.println(getTrip(numberOfParksInTrip, tripIds, 
                                           tripNames, tripDistances));
            } //else if
            
            //Else if user chooses an program action that is not listed
            else if (!option.equalsIgnoreCase("Q")) {
            
                //ERROR HANDLING IN USER INTERFACE IF USER CHOOSES AN INVALID OPTION
                System.out.println("Invalid option");
                System.out.println();
            } //else if
        } //while
        
        System.out.println();
        
        //Making output file of user's trip
        outputTrip(out, numberOfParksInTrip, tripNames, tripDistances);
        out.close();
    } //main method
    
    /**
     * Prints user interface for the ciphers
     */
    public static void displayMenu() {
        
        System.out.println("Parks Program - Please choose an option.");
        System.out.println();
        System.out.println("L - List parks");
        System.out.println("S - Search for park");
        System.out.println("A - Add park to trip");
        System.out.println("D - Display trip");
        System.out.println("Q - Quit");
        System.out.println();
        System.out.print("Option: ");
    } //displayMenu method
    
    /**
     * Calculates the number of lines in the input file
     * 
     * @param in scanner for inside the input file
     * @return numberOfLines the number of lines in the input file
     * @throws IllegalArgumentException "Null file" if the input scanner is null
     */
    public static int getNumberOfLines(Scanner in) {
        
        //ERROR HANDLING IN USER INTERFACE IF INPUT FILE IS NULL
        if (in == null) {
            throw new IllegalArgumentException("Null file");
        } //if
        
        //int number of lines in input file
        int numberOfLines = 0;
        
        //Calculating the number of lines in the input file
        while (in.hasNextLine()) {
            in.nextLine();
            numberOfLines++;
        } //while
        
        //returns the number of lines in input file
        return numberOfLines;
    } //getNumberOfLines method
    
    /**
     * Calculates the distance between 2 locations' latitudes and longitudes
     * 
     * @param latitude1 latitude of the first location
     * @param longitude1 longitude of the first location
     * @param latitude2 latitude of the second location
     * @param longitude2 longitude of the second location
     * @return distance the distance from one location to another
     * @throws IllegalArgumentException "Invalid latitude" if either latitude is 
     *           less than -90 degrees or more than 90 degrees
     * @throws IllegalArgumentException "Invalid longitude" if either longitude is 
     *           less than -180 degrees or more than 180 degrees
     * 
     */
    public static double calculateDistance(double latitude1, double longitude1,
                                           double latitude2, double longitude2) {
                                           
        //ERROR HANDLING IN USER INTERFACE IF LATITUDE IS TOO SMALL OR TOO LARGE
        if (latitude1 < LATITUDE_MIN_DEGREES || latitude1 > LATITUDE_MAX_DEGREES ||
            latitude2 < LATITUDE_MIN_DEGREES || latitude2 > LATITUDE_MAX_DEGREES) {
            
            throw new IllegalArgumentException("Invalid latitude");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF LONGITUDE IS TOO SMALL OR TOO LARGE
        if (longitude1 < LONGITUDE_MIN_DEGREES || longitude1 > LONGITUDE_MAX_DEGREES ||
            longitude2 < LONGITUDE_MIN_DEGREES || longitude2 > LONGITUDE_MAX_DEGREES) {
            
            throw new IllegalArgumentException("Invalid longitude");
        } //if
        
        //double latitude1 into radians
        double latitude1Rad = Math.toRadians(latitude1);
        
        //double longitude1 into radians
        double longitude1Rad = Math.toRadians(longitude1);
        
        //double latitude2 into radians
        double latitude2Rad = Math.toRadians(latitude2);
        
        //double longitude2 into radians
        double longitude2Rad = Math.toRadians(longitude2);
        
        //double difference between the 2 latitudes in radians
        double latitudeDiff = latitude2Rad - latitude1Rad;
        
        //double difference between the 2 longitudes in radians
        double longitudeDiff = longitude2Rad - longitude1Rad;
        
        //double mean of the 2 latitudes in radians
        double latitudeMean = (latitude1Rad + latitude2Rad) / 2.0;

        //double planar formula to calculate distance between 2 coordinates
        double planarFormula = Math.sqrt((Math.pow(latitudeDiff, 2.0)) + 
                                    (Math.pow(((Math.cos(latitudeMean)) * 
                                               longitudeDiff), 2.0)));
        
        //double distance between 2 coordinates
        double distance = planarFormula * EARTH_RADIUS;
        
        //returns the distance between 2 coordinates
        return distance;
    } //calculateDistance method
    
    /**
     * Stores the data from the input file and tests if the input file 
     * works with the program
     * 
     * @param in scanner for inside the input file
     * @param ids array of all the park ids in the input file
     * @param names array of all the park names in the input file
     * @param latitudes array of all the park latitudes in the input file
     * @param longitudes array of all the park longitudes in the input file
     * @return boolean if input file can be used for the program
     * @throws IllegalArgumentException "Null file" if the input scanner is null
     * @throws IllegalArgumentException "Null array" if any of the array 
     *         parameters are null
     * @throws IllegalArgumentException "Invalid array length" if all arrays are
     *         not the same length or if arrays have less than 1 index
     */
    public static boolean inputParks(Scanner in, int[] ids, 
                                     String[] names, double[] latitudes,
                                     double[] longitudes) {

        //ERROR HANDLING IN USER INTERFACE IF INPUT SCANNER IS NULL
        if (in == null) {

            throw new IllegalArgumentException("Null file");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY PARAMETERS ARE NULL
        if (ids == null || names == null || 
            latitudes == null || longitudes == null) {

            throw new IllegalArgumentException("Null array");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY HAS AN INVALID LENGTH
        if (ids.length < 1 || 
            ids.length != names.length || 
            names.length != latitudes.length || 
            latitudes.length != longitudes.length) {
            
            throw new IllegalArgumentException("Invalid array length");
        } //if
        
        //int index for index of the arrays
        int index = 0;
        
        //Testing if input file can work with the program
        //Storing id, name, latitude, and longitude values from input file into arrays
        while (in.hasNextLine()) {
        
            //String each line of input file
            String line = in.nextLine();
            
            //Checking if input file has only 4 values on each line
            String[] lineArray = line.split(",");
            if (lineArray.length != VALID_NUMBER_OF_FILE_VALUES) {
                return false;
            } //if
            
            //Scanner scnr to read the line for its values
            Scanner scnr = new Scanner(line);
            
            //Delimiter to separate the line by its commas
            scnr.useDelimiter(",");
            
            //Int park id of each park in input file
            int parkId = 0;
            
            //Checking if park id is an integer
            if (scnr.hasNextInt()) {
            
                parkId = scnr.nextInt();
            } //if
            else if (!scnr.hasNextInt()) {
                return false;
            } //else if
            
            //Try to assign variables from lines of input files
            try {
                //String park name of each park in input file
                String parkName = scnr.next();
                
                //Double latitude of each park in input file
                double latitude = scnr.nextDouble();
                
                //Double longitude of each park in input file
                double longitude = scnr.nextDouble();
                
                //Checking if park id is not negative
                //Checking if latitude and longitude are not too small or too large
                if (parkId < 0 || latitude < LATITUDE_MIN_DEGREES || 
                    latitude > LATITUDE_MAX_DEGREES || longitude < LONGITUDE_MIN_DEGREES || 
                    longitude > LONGITUDE_MAX_DEGREES) {
    
                    return false;
                } //if
                
                //Checking if 2 or more parks have the same park id
                for (int i = 0; i < index; i++) {
                    if (parkId == ids[i]) {
                    
                        return false;
                    } //if
                } //for
            
                //Storing each park id into ids array
                ids[index] = parkId;
            
                //Storing each park name into names array
                names[index] = parkName;
            
                //Storing each park latitude into latitudes array
                latitudes[index] = latitude;
            
                //Storing each park longitude into longitudes array
                longitudes[index] = longitude;
            
                //increasing index for next iteration of while loop
                index++;
            
            } //try
            //ERROR HANDLING IN USER INTERFACE IF VARIABLES CANNOT BE ASSIGNED
            catch (InputMismatchException e) {
                return false;
            } //catch
        } //while
        
        //returns true if input file can work with the program
        return true;
    } //inputParks method
    
    /**
     * Constructs the string list for all the parks given to it from the array parameters
     * 
     * @param ids array of all the park ids in the input file
     * @param names array of all the park names in the input file
     * @param latitudes array of all the park latitudes in the input file
     * @param longitudes array of all the park longitudes in the input file
     * @return parkList list of all the parks in the array parameters
     * @throws IllegalArgumentException "Null array" if any of the array 
     *         parameters are null
     * @throws IllegalArgumentException "Invalid array length" if all arrays are
     *           not the same length or if arrays have less than 1 index
     */
    public static String getParkList(int[] ids, String[] names, 
                                     double[] latitudes, double[] longitudes) {
        
        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY PARAMETER IS NULL
        if (ids == null || names == null || 
            latitudes == null || longitudes == null) {

            throw new IllegalArgumentException("Null array");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY HAS AN INVALID LENGTH
        if (ids.length < 1 || 
            ids.length != names.length || 
            names.length != latitudes.length || 
            latitudes.length != longitudes.length) {
            
            throw new IllegalArgumentException("Invalid array length");
        } //if

        //Int length of array parameters
        int arrayLength = ids.length;

        //String list for all the parks from the array parameters
        String parkList = "";
        
        //Constructing list for all the parks from the array parameters
        for (int i = 0; i < arrayLength; i++) {
            int id = ids[i];
            String name = names[i];
            double latitude = latitudes[i];
            double longitude = longitudes[i];
            parkList += String.format("%3d %-40s %8.2f %8.2f", id, name, latitude, longitude);
            parkList += "\n";
        } //for
        
        //returns list of all the parks from the array parameters
        return parkList;
    } //getParkList
    
    /**
     * Searches for parks specified by a keyword
     * 
     * @param parkName keyword that user gives to find specific parks
     * @param ids array of all the park ids in the input file
     * @param names array of all the park names in the input file
     * @param latitudes array of all the park latitudes in the input file
     * @param longitudes array of all the park longitudes in the input file
     * @return parkList list of all the parks in the array parameters
     * @throws IllegalArgumentException "Null array" if any of the array 
     *         parameters are null
     * @throws IllegalArgumentException "Invalid array length" if all arrays are
     *           not the same length or if arrays have less than 1 index
     */
    public static String searchForPark(String parkName, int[] ids, 
                                       String[] names, double[] latitudes, 
                                       double[] longitudes) {

        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY PARAMETER IS NULL
        if (ids == null || names == null || 
            latitudes == null || longitudes == null) {

            throw new IllegalArgumentException("Null array");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY HAS AN INVALID LENGTH
        if (ids.length < 1 || 
            ids.length != names.length || 
            names.length != latitudes.length || 
            latitudes.length != longitudes.length) {
            
            throw new IllegalArgumentException("Invalid array length");
        } //if

        //Int length of array parameters
        int arrayLength = ids.length;
        
        //String list of all parks that contain the keyword
        String parkSearch = "";
        
        //Searching for all parks that contain the keyword from the array parameters
        parkName = parkName.toLowerCase();
        for (int i = 0; i < arrayLength; i++) {
            if (names[i].toLowerCase().contains(parkName)) {
                parkSearch += String.format("%3d %-40s %8.2f %8.2f", ids[i], 
                                            names[i], latitudes[i], 
                                            longitudes[i]);
                parkSearch += "\n";
            } //if
        } //for
        
        //returns list of all parks that contain the keyword
        return parkSearch;
    } //searchForPark method
    
    /**
     * Formats the park id, name, latitude, and longitude for a list
     * 
     * @param id the id of the park
     * @param name the name of the park
     * @param latitude the latitude coordinate for the park
     * @param longitude the longitude coordinate for the park
     * @return formatted string of the park id, name, latitude, and longitude
     */
    public static String toString(int id, String name, 
                                  double latitude, double longitude) {

        return String.format("%3d %-40s %8.2f %8.2f", id, name, latitude, longitude);
    } //toString method
    
    /**
     * Adds parks to user's trip
     * 
     * @param parkId id of the park the user wants to add to their trip
     * @param numberOfParksInTrip the number of parks that the user currently
     *           has in their trip
     * @param ids array of all the park ids in the input file
     * @param names array of all the park names in the input file
     * @param latitudes array of all the park latitudes in the input file
     * @param longitudes array of all the park longitudes in the input file
     * @param tripIds array of all the park ids the user wants to add to their trip
     * @param tripNames array of all the park names the user wants to add to
     *          their trip
     * @param tripDistances array of all the park distances the user wants to add
     *           to their trip
     * @return numberOfParksInTrip the number of parks in the user's trip after adding 
     *         a park to the user's trip
     * @throws IllegalArgumentException "Invalid number of parks" if 
     *            numberOfParksInTrip is less than 0
     * @throws IllegalArgumentException "Null array" if any of the array 
     *         parameters are null
     * @throws IllegalArgumentException "Invalid array length" if all park arrays 
     *           are not the same length or if arrays have less than 1 index
     * @throws IllegalArgumentException "Invalid array length" if all of the user's 
     *           trip arrays don't have a length of 10
     * @throws IllegalArgumentException "Trip is full" if numberOfParksInTrip is
     *            equal to 10
     * @throws IllegalArgumentException "Invalid id" if parkId does not resemble
     *            any park's id
     */
    public static int addParkToTrip(int parkId, int numberOfParksInTrip, 
                                    int[] ids, String[] names, 
                                    double[] latitudes, double[] longitudes, 
                                    int[] tripIds, String[] tripNames, 
                                    double[] tripDistances) {

        
        //ERROR HANDLING IN USER INTERFACE IF THERE ARE LESS THAN 0 PARKS IN TRIP
        if (numberOfParksInTrip < 0) {
            throw new IllegalArgumentException("Invalid number of parks");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY PARAMETER IS NULL
        if (ids == null || names == null || 
            latitudes == null || longitudes == null || 
            tripIds == null || tripNames == null ||
            tripDistances == null) {

            throw new IllegalArgumentException("Null array");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY PARK ARRAY HAS AN INVALID LENGTH
        if (ids.length < 1 || 
            ids.length != names.length || 
            names.length != latitudes.length || 
            latitudes.length != longitudes.length) {
            
            throw new IllegalArgumentException("Invalid array length");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY TRIP ARRAY HAS AN INVALID LENGTH
        if (tripIds.length != 10 || 
            tripNames.length != 10 ||
            tripDistances.length != 10) {
        
            throw new IllegalArgumentException("Invalid array length");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF TRIP HAS 10 PARKS ALREADY
        if (numberOfParksInTrip == 10) {
            throw new IllegalArgumentException("Trip is full");
        } //if
        
        //Int length of park array parameters
        int arrayLength = ids.length;
        
        //Finding if parkId is a real id of a park
        int index = -1;
        for (int i = 0; i < arrayLength; i++) {
            if (ids[i] == parkId) {
                index = i;
            } //if
        } //for
        
        //ERROR HANDLING IN USER INTERFACE IF THE ID ADDED DOESN'T EXIST
        if (index == -1) {
            throw new IllegalArgumentException("Invalid id");
        } //if
        
        //Storing added park id into the user's trip ids array
        tripIds[numberOfParksInTrip] = ids[index];
        
        //Storing added park name into the user's trip names array
        tripNames[numberOfParksInTrip] = names[index];
        
        //Int id of the previously added park
        int priorId = 0;
        
        //Int index of the previously added park
        int priorIndex = 0;
        
        //Calculating if the added park is the first added park
        if (numberOfParksInTrip == 0) {
            tripDistances[0] = 0.0;
        } //if
        //Calculating the index of the previously added park
        else {
            priorId = tripIds[numberOfParksInTrip - 1];
            for (int i = 0; i < ids.length; i++) {
                if (priorId == ids[i]) {
                    priorIndex = i;
                } //if
            } //for
            
            //Finding the distances between the added park and the previously added park
            tripDistances[numberOfParksInTrip] = tripDistances[numberOfParksInTrip - 1] + 
                                                 calculateDistance(latitudes[priorIndex],
                                                                   longitudes[priorIndex], 
                                                                   latitudes[index],
                                                                   longitudes[index]);
        } //else

        //Incrementing the number of parks in trip for each added park
        numberOfParksInTrip++;
        
        //returns the number of parks in user's trip
        return numberOfParksInTrip;
    } //addParkToTrip method
    
    /**
     * Constructs the string list for all the parks in the user's trip
     * 
     * @param numberOfParksInTrip the number of parks that the user currently
     *           has in their trip
     * @param tripIds array of all the park ids the user wants to add to their trip
     * @param tripNames array of all the park names the user wants to add to
     *          their trip
     * @param tripDistances array of all the park distances the user wants to add
     *           to their trip
     * @return s the formatted string list for all the parks in the user's trip
     * @throws IllegalArgumentException "Invalid number of parks" if 
     *            numberOfParksInTrip is less than 0
     * @throws IllegalArgumentException "Null array" if any of the array 
     *         parameters are null
     * @throws IllegalArgumentException "Invalid array length" if all of the user's 
     *           trip arrays don't have a length of 10
     */
    public static String getTrip(int numberOfParksInTrip, int[] tripIds, 
                                 String[] tripNames, double[] tripDistances) {
        
        //ERROR HANDLING IN USER INTERFACE IF THERE ARE LESS THAN 0 PARKS IN TRIP
        if (numberOfParksInTrip < 0) {
            throw new IllegalArgumentException("Invalid number of parks");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY PARAMETER IS NULL
        if (tripIds == null || tripNames == null || tripDistances == null) {
            throw new IllegalArgumentException("Null array");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY TRIP ARRAY HAS AN INVALID LENGTH
        if (tripIds.length != 10 || tripNames.length != 10 || tripDistances.length != 10) {
            throw new IllegalArgumentException("Invalid array length");
        } //if

        //String list for all the parks in the user's trip
        String s = "";
        
        
        for (int i = 0; i < numberOfParksInTrip; i++) {
            s += String.format("%3d %-40s %8.2f\n", tripIds[i], tripNames[i], tripDistances[i]);
        } //for
        
        //returns list of all the parks from the user's trip
        return s;
    } //getTrip method
    
    /**
     * Prints the user's trip in the output file
     * 
     * @param out print writer for inside the output file
     * @param numberOfParksInTrip the number of parks that the user currently
     *           has in their trip
     * @param tripNames array of all the park names the user wants to add to
     *          their trip
     * @param tripDistances array of all the park distances the user wants to add
     *           to their trip
     * @throws IllegalArgumentException "Null file" if the output print writer is null
     * @throws IllegalArgumentException "Null array" if any of the array 
     *         parameters are null
     * @throws IllegalArgumentException "Invalid number of parks" if 
     *            numberOfParksInTrip is less than 0
     * @throws IllegalArgumentException "Invalid array length" if all of the user's 
     *           trip arrays don't have a length of 10
     */
    public static void outputTrip(PrintWriter out, int numberOfParksInTrip,
                                    String[] tripNames, double[] tripDistances) {
        
        //ERROR HANDLING IN USER INTERFACE IF OUT PRINT WRITER IS NULL
        if (out == null) {
            throw new IllegalArgumentException("Null file");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY ARRAY PARAMETER IS NULL
        if (tripNames == null || tripDistances == null) {
            throw new IllegalArgumentException("Null array");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF THERE ARE LESS THAN 0 PARKS IN TRIP
        if (numberOfParksInTrip < 0) {
            throw new IllegalArgumentException("Invalid number of parks");
        } //if
        
        //ERROR HANDLING IN USER INTERFACE IF ANY TRIP ARRAY HAS AN INVALID LENGTH
        if (tripNames.length != tripDistances.length) {
            throw new IllegalArgumentException("Invalid array length");
        } //if
        
        //Printing user's trip names and distances into output file
        for (int i = 0; i < numberOfParksInTrip; i++) {
            out.printf("%s,%.2f\n", tripNames[i], tripDistances[i]);
        } //for
        
        //Closing out print writer
        out.close();
    } //outputTrip method
} //Parks class