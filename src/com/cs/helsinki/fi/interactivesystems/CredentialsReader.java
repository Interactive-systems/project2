package com.cs.helsinki.fi.interactivesystems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;

public class CredentialsReader {

	private String address;
	private String username;
	private String password;
	private Context context;
	
	public CredentialsReader(Context context) {
		this.context = context;
	}
	
	public void readCredentials() {
		AssetManager assetManager = context.getResources().getAssets();
		InputStream inputStream = null;

		try {
			inputStream = assetManager.open("credentials.txt");
			if (inputStream != null) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				int counter = 0;
				while ((line =  bufferedReader.readLine()) != null) {
					if (counter == 0) {
						address = line;
					}
					else if (counter == 1) {
						username = line;
					}
					else if (counter == 2) {
						password = line;
					}
					counter++;
				}
			}
			inputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getAddress() {
		return address;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}