package Hospital_Management_System;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HospitalManagementMain 
{
    private static final String URL = "jdbc:mysql://localhost:3306/Hospital";
    private static final String Username = "root";
    private static final String Password = "";

    public static void main(String[] args) {    
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    
        Scanner scan = new Scanner(System.in);
        try {
            Connection connect = DriverManager.getConnection(URL, Username, Password);
            Patient pt = new Patient(connect, scan);
            Doctor doc = new Doctor(connect);

            while(true) {
                System.out.println("HOSPITAL MANAGEMENT SYSTEM ");
                System.out.println("1. Add Patient ");
                System.out.println("2. View Patients ");
                System.out.println("3. View Doctors ");
                System.out.println("4. Check Doctor Availability ");
                System.out.println("5. Book Appointment ");
                System.out.println("6. View Booking Details ");
                System.out.println("7. Discharge Patient ");
                System.out.println("8. Exit ");

                System.out.println("Enter Your Choice :");
                int choice = scan.nextInt();

                switch(choice) {
                    case 1 : pt.addPatient();
                    System.out.println();
                    break;
                    case 2 : pt.viewPatient();
                    System.out.println();
                    break;
                    case 3 : doc.viewDoctors();
                    System.out.println();
                    break;
                    case 4 : checkAvailableDoctors(doc, connect, scan);
                    System.out.println();
                    break;
                    case 5 : bookAppointment(pt, doc, connect, scan);
                    System.out.println();
                    break;
                    case 6: viewBookingDetails(connect);
                    System.out.println();
                    break;
                    case 7:
                    dischargePatient(connect,scan);
                    System.out.println();
                    break;
                    case 8 :
                    	try {
                        exit();
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while exiting.");
                    }
                    scan.close();
                    return;
                    default : System.out.println("Invalid Choice");
                }
            }
        } catch(SQLException s) {
            s.printStackTrace();
        }
    }

    public static void bookAppointment(Patient pt, Doctor doc, Connection connect, Scanner scan) {
        System.out.println("Enter Patient Id : ");
        int pId = scan.nextInt();
        System.out.println("Enter Doctor Id : ");
        int docId = scan.nextInt();
        System.out.println("Enter Appointment Date (YYYY-MM-DD) : ");
        String appointmentDate = scan.next();
        System.out.println("Enter Appointment Time (HH:MM:SS) : ");
        String appointmentTime = scan.next();

        if (pt.getPatientById(pId) && doc.getDoctorById(docId)) {
            if (checkDocAvailability(docId, appointmentDate, appointmentTime, connect)) {
                String appointmentQuery = "INSERT INTO appointments (patient_Id, doctor_Id, appointment_Date, appointment_Time) values (?,?,?,?)";

                try {
                    PreparedStatement pst = connect.prepareStatement(appointmentQuery);
                    pst.setInt(1, pId);
                    pst.setInt(2, docId);
                    pst.setString(3, appointmentDate);
                    pst.setString(4, appointmentTime);

                    int rows = pst.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Appointment Booked Successfully.");
                    } else {
                        System.out.println("Booking Failed!!");
                    }

                } catch (SQLException s) {
                    s.printStackTrace();
                }
            } else {
                System.out.println("Doctor not available on this date and time.");
            }
        } else {
            System.out.println("Either Doctor or Patient Doesn't Exist!!");
        }
    }

    private static boolean checkDocAvailability(int docId, String appointmentDate, String appointmentTime, Connection connect) {
        String query = "SELECT count(*) FROM appointments WHERE doctor_Id = ? AND appointment_Date = ? AND appointment_Time = ?";

        try {
            PreparedStatement pst = connect.prepareStatement(query);
            pst.setInt(1, docId);
            pst.setString(2, appointmentDate);
            pst.setString(3, appointmentTime);

            ResultSet rst = pst.executeQuery();

            if (rst.next()) {
                int count = rst.getInt(1);
                return count == 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void viewBookingDetails(Connection connect) {
        String query = "SELECT p.pName, a.appointment_Date, a.appointment_Time, d.docName " +
                       "FROM appointments a " +
                       "JOIN Patients p ON a.patient_Id = p.pId " +
                       "JOIN Doctors d ON a.doctor_Id = d.docId";

        try {
            PreparedStatement pst = connect.prepareStatement(query);
            ResultSet rst = pst.executeQuery();

            System.out.println("Booking Details:");
            System.out.println("+----------------+------------------+------------------+------------------+");
            System.out.println("| Patient Name   | Appointment Date | Appointment Time | Doctor Name      |");
            System.out.println("+----------------+------------------+------------------+------------------+");

            while (rst.next()) {
                String patientName = rst.getString("pName");
                String appointmentDate = rst.getString("appointment_Date");
                String appointmentTime = rst.getString("appointment_Time");
                String doctorName = rst.getString("docName");

                System.out.printf("| %-14s | %-16s | %-16s | %-16s |\n", patientName, appointmentDate, appointmentTime, doctorName);
                System.out.println("+----------------+------------------+------------------+------------------+");
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }


    public static void dischargePatient(Connection connect, Scanner scan) {
        System.out.println("Enter Patient Id to discharge: ");
        int pId = scan.nextInt();

        // Start a transaction
        try {
            connect.setAutoCommit(false); // Disable auto-commit

            // Delete from the appointments table
            String deleteAppointmentQuery = "DELETE FROM appointments WHERE patient_Id = ?";
            try (PreparedStatement pst = connect.prepareStatement(deleteAppointmentQuery)) {
                pst.setInt(1, pId);
                int appointmentRows = pst.executeUpdate();
                if (appointmentRows > 0) {
                    System.out.println("Appointment record removed.");
                } else {
                    System.out.println("No appointment found for this Patient ID.");
                }
            }

            // Delete from the Patients table
            String deletePatientQuery = "DELETE FROM Patients WHERE pId = ?";
            try (PreparedStatement pst = connect.prepareStatement(deletePatientQuery)) {
                pst.setInt(1, pId);
                int patientRows = pst.executeUpdate();
                if (patientRows > 0) {
                    System.out.println("Patient record removed.");
                } else {
                    System.out.println("No patient found with this Patient ID.");
                }
            }

            // Commit the transaction if both deletions are successful
            connect.commit();
            System.out.println("Patient discharged successfully.");

        } catch (SQLException e) {
            try {
                connect.rollback(); // Rollback transaction if any deletion fails
                System.out.println("Discharge failed. Transaction rolled back.");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connect.setAutoCommit(true); // Enable auto-commit
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    

    private static void checkAvailableDoctors(Doctor doc, Connection connect, Scanner scan) {
        System.out.println("Enter Date (YYYY-MM-DD) to check available doctors: ");
        String date = scan.next();

        String query = "SELECT d.docId, d.docName, d.Specialization FROM Doctors d WHERE d.docId NOT IN (SELECT a.doctor_Id FROM appointments a WHERE a.appointment_Date = ?)";

        try {
            PreparedStatement pst = connect.prepareStatement(query);
            pst.setString(1, date);

            ResultSet rst = pst.executeQuery();

            System.out.println("Available Doctors on " + date + ":");
            System.out.println("+------------+-------------------+--------------------+");
            System.out.println("| Doctor Id  |   Doctor's Name   |   Specialization   |");
            System.out.println("+------------+-------------------+--------------------+");

            boolean anyDoctorAvailable = false;

            while(rst.next()) {
                int id = rst.getInt("docId");
                String name = rst.getString("docName");
                String specialization = rst.getString("Specialization");

                System.out.printf("|%-12s|%-19s|%-20s|\n", id, name, specialization);
                System.out.println("+------------+-------------------+--------------------+");

                anyDoctorAvailable = true;
            }

            if (!anyDoctorAvailable) {
                System.out.println("No doctors available on this date.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while (i != 0) {
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("We always are here for your Help !!");
        System.out.println("Thank you for using the Hospital Management System!!!");
}
}
