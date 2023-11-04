package application;

// import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class PublicHolidays{
        
    
    public String Holidays(LocalDate selectedDate) {
        HashMap<String, String> map = new HashMap<>();
        try {
            
            String country = "US";
            String date = String.valueOf(selectedDate.getYear());
            
            String userURL = "https://date.nager.at/api/v3/PublicHolidays/" + date + "/" + country;
            // Create a URL object with the API endpoint
            //URL url = new URL("https://date.nager.at/api/v3/PublicHolidays/2023/US");
            URL url = new URL(userURL);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            reader.close();
            connection.disconnect();
            
            
            String[] parts = response.toString().split("[,{}]");
            ArrayList<String> arrList = new ArrayList<>();
            
            for (String part : parts) {
                if (!part.isEmpty() ) {
                    arrList.add(part.replace("\"", " ").trim());
                }
            }
            
            for (int i = arrList.size() - 1; i >= 0; i--) {
                if (arrList.get(i).charAt(0) != 'd' && arrList.get(i).charAt(0) != 'n') {
                    arrList.remove(i);
                }
            }
            
            
            for(int i = 0; i < arrList.size(); i++) {
                map.put(arrList.get(i).substring(7), arrList.get(i + 1).substring(7));
                i++;
            }

            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return map.get(selectedDate.toString());


    }
    

}