// File IO 
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Timing and formatting
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import java.time.format.DateTimeFormatter;

class AuditLogManager{
    
    private String host; // Server is hosted locally. 
    private String remoteUser;
    private String responseSize;
    private String filename;

    public AuditLogManager(){

        this.host = "127.0.0.1"; // Server is hosted locally. 
        this.remoteUser = "-";
        this.responseSize = "-"; // No response size
        this.filename = "audit.log";
        // Make log file
        File log = new File(this.filename);
        try{
            boolean created = log.createNewFile();
            if (!created){ // file already existed
                log.delete();
                log.createNewFile();
            }
        } catch (IOException e){
            System.err.println("Error creating audit log: " + e.getMessage());
        }
    }

    public void addLoginAttempt(String username, Boolean success){


        String dateTime = getTime();
        int statusCode = getStatusCode(200, 401, success);
        String loginReq = String.format("Login Attempt: User=%s",username);

        // Get in log format
        String logMessage = String.format("%s - %s [%s] \"%s\" %d -", this.host, this.remoteUser, dateTime, loginReq, statusCode);
        
        // Write to log file
        writeToLog(logMessage);
    }

    public void addRegistrationAttempt(String username, Boolean success){

        String dateTime = getTime();
        int statusCode = getStatusCode(201, 409, success);

        String regReq = String.format("Registration Attempt: User=%s",username);
        String logMessage = String.format("%s - %s [%s] \"%s\" %d -", this.host, this.remoteUser, dateTime, regReq, statusCode);
        
        // Write to log file
        writeToLog(logMessage);
    }

    public void addPasswordReset(String username, Boolean success){

        String dateTime = getTime();
        int statusCode = getStatusCode(200, 401, success);

        String regReq = String.format("Password Reset Attempt: User=%s",username);
        String logMessage = String.format("%s - %s [%s] \"%s\" %d -", this.host, this.remoteUser, dateTime, regReq, statusCode);
        
        // Write to log file
        writeToLog(logMessage);
    }

    private String getTime(){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
        OffsetDateTime time = OffsetDateTime.now();
        return time.format(formatter);
    }

    private int getStatusCode(int successCode, int failureCode, boolean success){
        if (success){
            return successCode;
        } else {
            return failureCode;
        }
    }

    private void writeToLog(String message){
        try {
            // Create a FileWriter object
            FileWriter writer = new FileWriter(this.filename, true); // append

            // Write text to the file
            writer.write(message + "\n");

            // Close the FileWriter
            writer.close();

        } catch (IOException e) {
            // Handle any potential IO exceptions
            e.printStackTrace();
        }
    }
}