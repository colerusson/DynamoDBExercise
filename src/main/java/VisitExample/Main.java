package VisitExample;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        VisitsDAO dao = new VisitsDAO();

        /**
         * Delete old items
         */
        dao.deleteVisit("matt", "utah");
        dao.deleteVisit("matt", "guatemala");
        dao.deleteVisit("matt", "idaho");
        dao.deleteVisit("matt", "italy");
        dao.deleteVisit("elliot", "italy");
        dao.deleteVisit("nate", "italy");
        dao.deleteVisit("adam", "italy");




        /**
         * Get an Item
         */
        int count = dao.getVisitCount("matt", "guatemala");
        System.out.println("(Just getting) Matt has visited Guatemala " + count + " time(s)");





        /**
         * Update an item, one of which exists (guatemala), one of which is new (utah)
         */
        dao.recordVisit("matt", "utah");
        dao.recordVisit("matt", "guatemala");

        int countGuatemala = dao.getVisitCount("matt", "guatemala");
        System.out.println("(After recording) Matt has visited Guatemala " + countGuatemala + " time(s)");

        int countUtah = dao.getVisitCount("matt", "utah");
        System.out.println("(After recording) Matt has visited utah " + countUtah + " time(s)");




        /**
         * Delete an item
         */
        dao.deleteVisit("matt", "utah");
        int countUtah2 = dao.getVisitCount("matt", "utah");
        System.out.println("(After deletion) Matt has visited utah " + countUtah2 + " time(s)");


        /**
         * Add more items
         */
        dao.recordVisit("matt", "idaho");
        dao.recordVisit("matt", "italy");

        dao.recordVisit("elliot", "italy");
        dao.recordVisit("nate", "italy");
        dao.recordVisit("adam", "italy");

        /**
         * Get the first page (lastlocation = null) of items
         */
        DataPage<Visit> page = dao.getVisitedLocations("matt", 2, null);
        List<Visit> visits = page.getValues();
        boolean hasMorePages = page.isHasMorePages();
        System.out.println("Matt has visited: " + visits + ", and are there more pages? " + hasMorePages);
        verify(hasMorePages);

        String lastLocation = visits.get(visits.size() - 1).getLocation();

        /**
         * Get the second page (lastlocation = the last thing returned from the first page) of items
         */
        DataPage<Visit> page2 = dao.getVisitedLocations("matt", 2, lastLocation);
        List<Visit> visits2 = page2.getValues();
        boolean hasMorePages2 = page2.isHasMorePages();
        System.out.println("Matt has also visited: " + visits2  + ", and are there more pages? " + hasMorePages2);
        verify(!hasMorePages2);





        /**
         * Using an Index: Get the first page (lastVisitor = null) of items
         */
        DataPage<Visit> page3 = dao.getVisitors("italy", 2, null);
        List<Visit> visitsToItaly = page3.getValues();
        boolean hasMorePages3 = page3.isHasMorePages();
        System.out.println("Italy was visited by: " + visitsToItaly + ", and are there are more pages? " + hasMorePages3);
        verify(hasMorePages3);

        String lastVisitor = visitsToItaly.get(visitsToItaly.size() - 1).getVisitor();

        /**
         * Using an Index: Get the second page (lastVisitor = the last thing returned from the first page) of items
         */
        DataPage<Visit> page4 = dao.getVisitors("italy", 2, lastVisitor);
        List<Visit> visitsToItaly2 = page4.getValues();
        boolean hasMorePages4 = page4.isHasMorePages();
        System.out.println("Italy was also visited by: " + visitsToItaly2 + ", and are there are more pages? " + hasMorePages4);
        verify(!hasMorePages4);

    }

    private static void verify(boolean b) {
        if (!b) {
            throw new IllegalStateException();
        }
    }
}
