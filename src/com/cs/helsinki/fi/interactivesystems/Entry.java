package com.cs.helsinki.fi.interactivesystems;

import android.os.Parcel;
import android.os.Parcelable;

public class Entry implements Parcelable {
	
	public final String job; //nimetus
	public final String id; //id
	public final String isco; //isco //loomakasvatuse töötaja
	public final String ametValdkond; //ametValdkond //taimekasvatus, loomakasvatus, aiandus
	public final String tasks; //tooylesanded
	public final String requirements; //nouded
	public final String training; //omaltPooltPakume
	public final String applicationDate; //kandideerimiseKp
	public final String country; //riik
	public final String county; //maakond
	public final String locality; //asula
	public final String address; //aadress
	public final String employer; //tooandja
	public final String contact; //kontaktisik
	public final String email; //email
	public final String jobLength; //toosuhteKestus
	public final String experience; //tookogemus
	public final String hours; //tooaeg
	public final String salary; //tootasuTyyp
	public final String dateAdded; //lisamiseAeg
	public final String moreInfo; //lisainfo
	public final String phone; //telefon
	public final String education; //haridusTase
	
	public Entry(String job, String id, String isco, String ametValdkond, String tasks,
			String requirements, String training, String applicationDate,
			String country, String county, String locality, String address,
			String employer, String contact, String email, String jobLength,
			String experience, String hours, String salary, String dateAdded,
			String moreInfo, String phone, String education) {
		
		this.job = job;
		this.id = id;
		this.isco = isco;
		this.ametValdkond = ametValdkond;
		this.tasks = tasks;
		this.requirements = requirements;
		this.training = training;
		this.applicationDate = applicationDate;
		this.country = country;
		this.county = county;
		this.locality = locality;
		this.address = address;
		this.employer = employer;
		this.contact = contact;
		this.email = email;
		this.jobLength = jobLength;
		this.experience = experience;
		this.hours = hours;
		this.salary = salary;
		this.dateAdded = dateAdded;
		this.moreInfo = moreInfo;
		this.phone = phone;
		this.education = education;
	}
	
    private Entry(Parcel in) {
        this.job = in.readString();
        this.id = in.readString();
        this.isco = in.readString();
        this.ametValdkond = in.readString();
        this.tasks = in.readString();
        this.requirements = in.readString();
        this.training = in.readString();
        this.applicationDate = in.readString();
        this.country = in.readString();
        this.county = in.readString();
        this.locality = in.readString();
        this.address = in.readString();
        this.employer = in.readString();
        this.contact = in.readString();
        this.email = in.readString();
        this.jobLength = in.readString();
        this.experience = in.readString();
        this.hours = in.readString();
        this.salary = in.readString();
        this.dateAdded = in.readString();
        this.moreInfo = in.readString();
        this.phone = in.readString();
        this.education = in.readString();
    }

	public String getJob() {
		return job;
	}

	public String getId() {
	    return id;
	}
	
	public String getIsco() {
		return isco;
	}

	public String getAmetValdkond() {
		return ametValdkond;
	}

	public String getTasks() {
		return tasks;
	}

	public String getRequirements() {
		return requirements;
	}

	public String getTraining() {
		return training;
	}

	public String getApplicationDate() {
		return applicationDate;
	}

	public String getCountry() {
		return country;
	}

	public String getCounty() {
		return county;
	}

	public String getLocality() {
		return locality;
	}

	public String getAddress() {
		return address;
	}

	public String getEmployer() {
		return employer;
	}

	public String getContact() {
		return contact;
	}

	public String getEmail() {
		return email;
	}

	public String getJobLength() {
		return jobLength;
	}

	public String getExperience() {
		return experience;
	}

	public String getHours() {
		return hours;
	}

	public String getSalary() {
		return salary;
	}

	public String getDateAdded() {
		return dateAdded;
	}

	public String getMoreInfo() {
		return moreInfo;
	}

	public String getPhone() {
		return phone;
	}

	public String getEducation() {
		return education;
	}
	
	public String getSearchString() {
	    StringBuilder builder = new StringBuilder();
	    builder.append(job);
	    builder.append(tasks);
	    builder.append(requirements);
	    builder.append(training);
	    builder.append(country);
	    builder.append(locality);
	    builder.append(address);
	    builder.append(employer);
	    builder.append(experience);
	    builder.append(moreInfo);
	    builder.append(education);
	    
	    return builder.toString();
	}

	// not needed but must be overridden
    @Override
    public int describeContents() {
        return 0;
    }

    // flatten Entry object into a Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(job);
        out.writeString(id);
        out.writeString(isco);
        out.writeString(ametValdkond);
        out.writeString(tasks);
        out.writeString(requirements);
        out.writeString(training);
        out.writeString(applicationDate);
        out.writeString(country);
        out.writeString(county);
        out.writeString(locality);
        out.writeString(address);
        out.writeString(employer);
        out.writeString(contact);
        out.writeString(email);
        out.writeString(jobLength);
        out.writeString(experience);
        out.writeString(hours);
        out.writeString(salary);
        out.writeString(dateAdded);
        out.writeString(moreInfo);
        out.writeString(phone);
        out.writeString(education);
    }
    
    // generate Entry instances from Parcels
    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };
}
