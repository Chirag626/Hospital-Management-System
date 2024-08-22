package Hospital_Management_System;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.ResultSet;


public class Patient 
{
    private Connection connect;
    private Scanner sc ;

    public Patient(Connection connect,Scanner sc)
    {
        this.connect = connect;
        this.sc = sc;
    }

    public void addPatient()
    {
        System.out.print("Enter Patient Name : ");
        String name = sc.next();
        System.out.print("Enter Patient Age : ");
        int age = sc.nextInt();
        System.out.print("Enter Patient Gender : ");
        String gender = sc.next();
        System.out.println("Enter Disease Name : ");
        String disease = sc.next();


        try 
        {
            String query = "INSERT INTO patients(pName,Age,Gender,Disease) values(?,?,?,?)";

            PreparedStatement pst = connect.prepareStatement(query);
            pst.setString(1, name);
            pst.setInt(2,age);
            pst.setString(3, gender);
            pst.setString(4, disease);

            int rows = pst.executeUpdate();

            if (rows > 0)
            {
                System.out.println("Patient added Successfully !!");
            }
            else
            {
                System.out.println("Failed to add Patient !!");
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();  
        }
    }

    public void viewPatient()
    {
        String query = "Select * from Patients";

        try 
        {
            PreparedStatement pst = connect.prepareStatement(query);

            ResultSet rst = pst.executeQuery();
            
            System.out.println("Patients :");
            System.out.println("+------------+-------------------+--------+------------+---------------+");
            System.out.println("| Patient Id |    Patient Name   |   Age  |   Gender   |   Disease     |");
            System.out.println("+------------+-------------------+--------+------------+---------------+");
            
            while(rst.next())
            {
                int id = rst.getInt("pId");
                String name = rst.getString("pName");
                int age = rst.getInt("Age");
                String gender = rst.getString("Gender");
                String disease = rst.getString("Disease");
                
                System.out.printf("|   %-8s |     %-13s |  %-6s|   %-9s|   %-12s|\n",id,name,age,gender,disease);
                System.out.println("+------------+-------------------+--------+------------+---------------+");
                
            }
            


        }
        catch (Exception e) 
        {
        System.out.println(e.getLocalizedMessage());
        }
    }

    public boolean getPatientById(int id)
    {

        String query = "Select * from Patients WHERE pId = ?";

        try
        {
            PreparedStatement pst = connect.prepareStatement(query);
            pst.setInt(1, id);

            ResultSet rst = pst.executeQuery();
            
            if(rst.next())
            {
                return true;
            }
        }
        catch (SQLException s )
        {
            s.printStackTrace();
        }
        return false; 
    }

}
