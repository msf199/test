package main.com.whitespell.peak.model;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/3/2015
 */
public class ReportingTypeObject {

    int reportingTypeId;
    String reportingTypeName;
    public ReportingTypeObject(int reportingTypeId, String reportingTypeName) {

        this.reportingTypeId = reportingTypeId;
        this.reportingTypeName = reportingTypeName;
    }

    public int getReportingTypeId() {
        return reportingTypeId;
    }

    public String getReportingTypeName() {
        return reportingTypeName;
    }
}