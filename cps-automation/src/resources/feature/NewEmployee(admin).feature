@Employee
Feature: Employee Management

  Scenario: Add Employee Successfully
    Given User is on Employee page
    When User clicks Add Employee button
    And User enters employee details
    And User selects role "ROLE_USER"
    And User clicks Save button
    Then Employee should be added in list