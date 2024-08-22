package Hospital_Management_System;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Doctor
{
    private Connection connect;
    
    public Doctor(Connection connect)
    {
        this.connect = connect;
    }

    public void viewDoctors()
    {
        String query = "Select * from Doctors";

        try 
        {
            PreparedStatement pst = connect.prepareStatement(query);

            ResultSet rst = pst.executeQuery();
            
            System.out.println("Doctors :");
            System.out.println("+------------+-------------------+--------------------+");
            System.out.println("| Doctor Id  |   Doctor's Name   |   Specialization   |");
            System.out.println("+------------+-------------------+--------------------+");
            
            while(rst.next())
            {
                int id = rst.getInt("docId");
                String name = rst.getString("docName");
                String specialized = rst.getString("Specialization");
                
                System.out.printf("|%-12s|%-19s|%-20s|\n",id,name,specialized);
                System.out.println("+------------+-------------------+--------------------+");  
            }   
        }
        catch (Exception e) 
        {
        System.out.println(e.getLocalizedMessage());
        }
    }

    public boolean getDoctorById(int id)
    {

        String query = "Select * from Doctors WHERE docId = ?";

        try
        {
            PreparedStatement pst = connect.prepareStatement(query);
            pst.setInt(1, id);

            ResultSet rst = pst.executeQuery();
            
            if(rst.next())
            {
                return true;
            }
            else
            {
            	return false;
            }
        }
        catch (SQLException s )
        {
            s.printStackTrace();
        }
        return false; 
    }
    
}
