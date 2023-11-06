package VisitExample;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class Visit {
    private String visitor;
    private String location;
    private int visit_count;

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = VisitsDAO.IndexName)
    public String getVisitor() {
        return visitor;
    }

    public void setVisitor(String visitor) {
        this.visitor = visitor;
    }

    @DynamoDbSortKey
    @DynamoDbSecondaryPartitionKey(indexNames = VisitsDAO.IndexName)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getVisit_count() {
        return visit_count;
    }

    public void setVisit_count(int visit_count) {
        this.visit_count = visit_count;
    }


    @Override
    public String toString() {
        return "Visit{" +
                "visitor='" + visitor + '\'' +
                ", location='" + location + '\'' +
                ", visit_count=" + visit_count +
                '}';
    }
}
