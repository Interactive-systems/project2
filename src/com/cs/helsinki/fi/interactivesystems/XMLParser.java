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
		String id = null;
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
				job = readValue(parser, "nimetus");
			} else if (name.equals("id")) {
			    id = readValue(parser, "id");
			} else if (name.equals("isco")) {
				isco = readValue(parser, "isco");
			} else if (name.equals("ametValdkond")) {
				ametValdkond = readValue(parser, "ametValdkond");
			} else if (name.equals("tooylesanded")) {
				tasks = readValue(parser, "tooylesanded");
			} else if (name.equals("requirements")) {
				requirements = readValue(parser, "requirements");
			} else if (name.equals("training")) {
				training = readValue(parser, "training");
			} else if (name.equals("applicationDate")) {
				applicationDate = readValue(parser, "applicationDate");
			} else if (name.equals("riik")) {
				country = readValue(parser, "riik");
			} else if (name.equals("maakond")) {
				county = readValue(parser, "maakond");
			} else if (name.equals("asula")) {
				locality = readValue(parser, "asula");
			} else if (name.equals("aadress")) {
				address = readValue(parser, "aadress");
			} else if (name.equals("tooandja")) {
				employer = readValue(parser, "tooandja");
			} else if (name.equals("kontaktisik")) {
				contact = readValue(parser, "kontaktisik");
			} else if (name.equals("email")) {
				email = readValue(parser, "email");
			} else if (name.equals("toosuhteKestus")) {
				jobLength = readValue(parser, "toosuhteKestus");
			} else if (name.equals("tookogemus")) {
				experience = readValue(parser, "tookogemus");
			} else if (name.equals("tooaeg")) {
				hours = readValue(parser, "tooaeg");
			} else if (name.equals("tootasuTyyp")) {
				salary = readValue(parser, "tootasuTyyp");
			} else if (name.equals("lisamiseAeg")) {
				dateAdded = readValue(parser, "lisamiseAeg");
			} else if (name.equals("lisainfo")) {
				moreInfo = readValue(parser, "lisainfo");
			} else if (name.equals("telefon")) {
				phone = readValue(parser, "telefon");
			} else if (name.equals("haridusTase")) {
				education = readValue(parser, "haridusTase");
			} else {
				skip(parser);
			}
		}
		return new Entry(job, id, isco, ametValdkond, tasks, requirements, training, applicationDate, country, county, locality,
				address, employer, contact, email, jobLength, experience, hours, salary, dateAdded, moreInfo, phone, education);
	}
	
	private String readValue(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, tagName);
	    String value = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, tagName);
	    return value;
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