@ProcurementModule
Feature: RFQ Management
     
Scenario:Create New RFQ Successfully
     Given User is clicks on RFQ Page
     When User click on create RFQ button
     And User selects Customer "Yamaha"
     And User selects RFQ Received Date
     And  User selects days
     And User select Start date
     And User enters RFQ details
     And User select Quote due date
     And User select Employee name
     And User select Expected Launch
     And User clicks on save button
     Then RFQ should be in list
     