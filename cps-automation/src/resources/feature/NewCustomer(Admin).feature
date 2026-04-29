@Customer
Feature: Customer Management

   Scenario: Add Customer Successfully
     Given User is on Customer page
     When User clicks on add customer button
     And User enters customer details
     And User clicks Save button for customer
     Then Customer should be added in list