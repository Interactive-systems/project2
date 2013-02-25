package com.cs.helsinki.fi.interactivesystems;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class XMLParser {

	private static final String ns = null;
	private Context context;

	public XMLParser(Context context) {
		this.context = context;
	}

	public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} finally {
			in.close();
		}
	}
	
	public void printContents(List<Entry>list) {
		for (int i = 0; i < list.size(); i++) {
			Entry e = list.get(i);
			
			
			Log.d("test", "entry "+e.toString());
			Log.d("test", "address "+e.getAddress());
		}
	}

	private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
		List<Entry> entries = new ArrayList();

		parser.require(XmlPullParser.START_TAG, ns, "toopakkumised");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("toopakkumine")) {
				entries.add(readEntry(parser));
			} else {
				skip(parser);
			}
		}  
		return entries;
	}

	// Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the tag.
	private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "toopakkumine");
	
		String job = null;
		String isco = null;
		String ametValdkond = null;
		String tasks = null;
		String requirements = null;
		String training = null;
		String applicationDate = null;
		String country = null;
		String county = null;
		String locality = null;
		String address = null;
		String employer = null;
		String contact = null;
		String email = null;
		String jobLength = null;
		String experience = null;
		String hours = null;
		String salary = null;
		String dateAdded = null;
		String moreInfo = null;
		String phone = null;
		String education = null;
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("nimetus")) {
				job = readJob(parser);
			} else if (name.equals("isco")) {
				isco = readIsco(parser);
			} else if (name.equals("ametValdkond")) {
				ametValdkond = readAmetValdkond(parser);
			} else if (name.equals("tooylesanded")) {
				tasks = readTasks(parser);
			} else if (name.equals("requirements")) {
				requirements = readRequirements(parser);
			} else if (name.equals("training")) {
				training = readTraining(parser);
			} else if (name.equals("applicationDate")) {
				applicationDate = readApplicationDate(parser);
			} else if (name.equals("riik")) {
				country = readCountry(parser);
			} else if (name.equals("maakond")) {
				county = readCounty(parser);
			} else if (name.equals("asula")) {
				locality = readLocality(parser);
			} else if (name.equals("aadress")) {
				address = readAddress(parser);
			} else if (name.equals("tooandja")) {
				employer = readEmployer(parser);
			} else if (name.equals("kontaktisik")) {
				contact = readContact(parser);
			} else if (name.equals("email")) {
				email = readEmail(parser);
			} else if (name.equals("toosuhteKestus")) {
				jobLength = readJobLength(parser);
			} else if (name.equals("tookogemus")) {
				experience = readExperience(parser);
			} else if (name.equals("tooaeg")) {
				hours = readHours(parser);
			} else if (name.equals("tootasuTyyp")) {
				salary = readSalary(parser);
			} else if (name.equals("lisamiseAeg")) {
				dateAdded = readDateAdded(parser);
			} else if (name.equals("lisainfo")) {
				moreInfo = readMoreInfo(parser);
			} else if (name.equals("telefon")) {
				phone = readPhone(parser);
			} else if (name.equals("haridusTase")) {
				education = readEducation(parser);
			} else {
				skip(parser);
			}
		}
		return new Entry(job, isco, ametValdkond, tasks, requirements, training, applicationDate, country, county, locality,
				address, employer, contact, email, jobLength, experience, hours, salary, dateAdded, moreInfo, phone, education);
	}
	
	private String readIsco(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "isco");
		String isco = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "isco");
		return isco;
	}

	private String readJob(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "nimetus");
		String job = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "nimetus");
		return job;
	}

	private String readAmetValdkond(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "ametValdkond");
		String ametValdkond = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "ametValdkond");
		return ametValdkond;
	}
	
	private String readTasks(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "tooylesanded");
		String tasks = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "tooylesanded");
		return tasks;
	}
	
	private String readRequirements(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "requirements");
		String requirements = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "requirements");
		return requirements;
	}
	
	private String readTraining(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "training");
		String training = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "training");
		return training;
	}
	
	private String readApplicationDate(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "applicationDate");
		String applicationDate = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "applicationDate");
		return applicationDate;
	}
	
	private String readCountry(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "riik");
		String country = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "riik");
		return country;
	}
	
	private String readCounty(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "maakond");
		String county = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "maakond");
		return county;
	}
	
	private String readLocality(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "asula");
		String locality = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "asula");
		return locality;
	}
	
	private String readAddress(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "aadress");
		String address = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "aadress");
		return address;
	}
	
	private String readEmployer(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "tooandja");
		String employer = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "tooandja");
		return employer;
	}
	
	private String readContact(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "kontaktisik");
		String contact = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "kontaktisik");
		return contact;
	}
	
	private String readEmail(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "email");
		String email = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "email");
		return email;
	}
	
	private String readJobLength(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "toosuhteKestus");
		String jobLength = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "toosuhteKestus");
		return jobLength;
	}
	
	private String readExperience(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "tookogemus");
		String experience = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "tookogemus");
		return experience;
	}
	
	private String readHours(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "tooaeg");
		String hours = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "tooaeg");
		return hours;
	}
	
	private String readSalary(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "tootasuTyyp");
		String salary = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "tootasuTyyp");
		return salary;
	}
	
	private String readDateAdded(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "lisamiseAeg");
		String dateAdded = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "lisamiseAeg");
		return dateAdded;
	}
	
	private String readMoreInfo(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "lisainfo");
		String moreInfo = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "lisainfo");
		return moreInfo;
	}
	
	private String readPhone(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "telefon");
		String phone = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "telefon");
		return phone;
	}
	
	private String readEducation(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "haridusTase");
		String education = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "haridusTase");
		return education;
	}

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}