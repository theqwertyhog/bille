package com.bille;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import com.bille.exceptions.BilleException;
import com.bille.exceptions.ServerConnectionException;

public class API {

	private static final int version = 1;
	private static final String url = "http://" + "pos_api_v" + version
			+ ".ellypsys.local";

	public static License refreshLicense() throws ServerConnectionException {
		try {
			String response = readPOST(url + "/license/getinfo", "id="
					+ Application.getInstance().getStore().getStoreID());
			JSONParser parser = new JSONParser();

			Object obj = parser.parse(response);
			JSONObject jsonObject = (JSONObject) obj;

			String name = (String) jsonObject.get("name");
			String address = (String) jsonObject.get("address");
			String phoneOne = (String) jsonObject.get("phone_one");
			String phoneTwo = (String) jsonObject.get("phone_two");
			String phoneThree = (String) jsonObject.get("phone_three");

			String typeStr = (String) jsonObject.get("type");
			StoreType type = StoreType.valueOf(typeStr);

			String email = (String) jsonObject.get("email");
			String website = (String) jsonObject.get("website");

			String expiresStr = (String) jsonObject.get("expires");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date expires = df.parse(expiresStr);

			License lic = new License();
			lic.setName(name);
			lic.setAddress(address);
			lic.setPhoneOne(phoneOne);
			lic.setPhoneTwo(phoneTwo);
			lic.setPhoneThree(phoneThree);
			lic.setType(type);
			lic.setEmail(email);
			lic.setWebsite(website);
			lic.setExpires(expires);

			return lic;
		} catch (Exception e) {
			throw new ServerConnectionException();
		}
	}

	private static String readPOST(String url, String query)
			throws ServerConnectionException {
		// Read response
		StringBuilder responseSB;
		try {
			// Encode the query
			String encodedQuery = URLEncoder.encode(query, "UTF-8");

			URL dest = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) dest
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length",
					String.valueOf(encodedQuery.length()));

			// Write data
			OutputStream os = connection.getOutputStream();
			os.write(encodedQuery.getBytes());

			responseSB = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line;
			while ((line = br.readLine()) != null)
				responseSB.append(line);

			// Close streams
			br.close();
			os.close();

			return responseSB.toString();
		} catch (Exception e) {
			throw new ServerConnectionException();
		}

	}

}
