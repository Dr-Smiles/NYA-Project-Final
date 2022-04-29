package pkt.Connection;

import pkt.Interface.IConnection;
import pkt.Interface.IObserver;

import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

public class postgreSQLConnection implements IConnection{
    
    private Connection connection;
    private int unitID;
    private double targetTemp;

    private String username;
    private String password;

    private ArrayList<IObserver> observers = new ArrayList<IObserver>();

    public postgreSQLConnection(){
        targetTemp = 0;
        connect();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            cout("Failed to construct connection");
            e.printStackTrace();
        }
        setModuleStatus(true);
    }

    private void connect(){
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/NYA_Server","postgres","sifre");
        } catch (SQLException e) {
            cout("Failed to connect to database");
            e.printStackTrace();
        }
    }

    private void update(){
        observers.forEach(observer -> observer.update(this.targetTemp));
    }

    @Override
    public boolean login_username(String username) {
        this.username = username;
        try {
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery("SELECT COUNT(user_data.user_id) AS data FROM user_data WHERE user_id = '" + username + "'");
            while(results.next()) {
            	int count = results.getInt("data");
            	if (count == 1) {return true;}
            	else if (count == 0) {return false;}
            }
            results.close();
        } catch (SQLException e) {
            cout("Failed to login --> username");
            e.printStackTrace();
        }
		return false;
    }

    @Override
    public boolean login_password(String password) {
        this.password = password;
        try {
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery("SELECT COUNT(user_data.user_id) AS data FROM user_data WHERE user_id = '" + username + "' AND password = '" + password + "'");
            while(results.next()) {
            	int count = results.getInt("data");
            	if (count == 1) {return true;}
            	else if (count == 0) {return false;}
            }
            results.close();
        } catch (SQLException e) {
            cout("Failed to login --> password");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setModuleStatus(boolean moduleStatus) {
        try {
            PreparedStatement queryStatement = connection.prepareStatement("UPDATE env_modulators SET module_status = '" + moduleStatus + "' WHERE module_id = '" + unitID + "'");
            queryStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            cout("Failed to set module status");
            e.printStackTrace();
        }
    }

    @Override
    public void setTargetTemp(double targetTemp) {
        try {
            PreparedStatement queryStatement = connection.prepareStatement("UPDATE env_modulators SET target_temperature = '" + targetTemp + "' WHERE module_id = '" + unitID + "'");
            queryStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            cout("Failed to set target temperature");
            e.printStackTrace();
        }
    }

    @Override
    public void setRoomTemp(double roomTemp) {
        PreparedStatement queryStatement;
        try {
            queryStatement = connection.prepareStatement("UPDATE env_modulators SET room_temperature = '" + String.format(Locale.US, "%.2f", roomTemp) + "' WHERE module_id = '" + unitID + "'");
            queryStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            cout("Failed to set room temperature");
            e.printStackTrace();
        }
    }

    @Override
    public boolean getModuleStatus() {
        try {
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery("SELECT module_status AS module, env_modulators.module_id AS mod_id FROM env_modulators " + 
            "INNER JOIN user_module_id ON env_modulators.index = user_module_id.module_id INNER JOIN user_data ON user_data.user_no = user_module_id.user_id" + 
            " WHERE user_data.user_id = '"+username+"' AND \"user_data\".\"password\" = '"+password+"'");
            while(results.next()) {
            	boolean target = results.getBoolean("module");
                this.unitID = results.getInt("mod_id");
            	return target;
            }
            results.close();
        } catch (SQLException e) {
            cout("Failed to get module status");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public double getTargetTemp() {
        try {
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery("SELECT target_temperature AS target FROM env_modulators WHERE module_id ='" + unitID + "'");
            while(results.next()) {
            	int target = results.getInt("target");
                if (target != this.targetTemp) {
                    this.targetTemp = target;
                    update();
                    return target;
                }
            	return target;
            }
            results.close();
        } catch (SQLException e) {
            cout("Failed to get target temperature");
            e.printStackTrace();
        }
		return -9999999;
    }

    @Override
    public double getRoomTemp() {
        try {
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery("SELECT room_temperature AS target FROM env_modulators WHERE module_id ='" + unitID + "'");
            while(results.next()) {
            	int target = results.getInt("target");
            	return target;
            }
            results.close();
        } catch (SQLException e) {
            cout("Failed to get room temperature");
            e.printStackTrace();
        }
		return -9999999;
    }

    @Override
    public void disconnect() {
        setModuleStatus(false);
		try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            cout("Failed to disconnect");
            e.printStackTrace();
        }
    }

    @Override
    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    private void cout(String message){
        System.out.println(message);
    }

    @Override
    public void setCoolingStatus(boolean status) {
        PreparedStatement queryStatement;
        try {
            queryStatement = connection.prepareStatement("UPDATE env_modulators SET cooling_status = '" + status + "' WHERE module_id = '" + unitID + "'");
            queryStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            cout("Failed to set room temperature");
            e.printStackTrace();
        }
    }

    @Override
    public void setHeatingStatus(boolean status) {
        PreparedStatement queryStatement;
        try {
            queryStatement = connection.prepareStatement("UPDATE env_modulators SET heating_status = '" + status + "' WHERE module_id = '" + unitID + "'");
            queryStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            cout("Failed to set room temperature");
            e.printStackTrace();
        }
    }

    @Override
    public boolean getCoolingStatus() {
        try {
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery("SELECT cooling_status AS module FROM env_modulators WHERE module_id ='" + unitID + "'");
            while(results.next()) {
            	boolean target = results.getBoolean("module");
            	return target;
            }
            results.close();
        } catch (SQLException e) {
            cout("Failed to get cooling status");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean getHeatingStatus() {
        try {
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery("SELECT heating_status AS module FROM env_modulators WHERE module_id ='" + unitID + "'");
            while(results.next()) {
            	boolean target = results.getBoolean("module");
            	return target;
            }
            results.close();
        } catch (SQLException e) {
            cout("Failed to get heating status");
            e.printStackTrace();
        }
        return false;
    }
    
}